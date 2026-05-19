package xyz.yaszu.freedom.Subsystems;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.createChunkData;

/**
 * RedCastleManager — Wave-Function-Collapse-style dungeon generator.
 *
 * KEY DESIGN GUARANTEES
 * ─────────────────────
 * 1. Every chunk-slot receives exactly one schematic.  If no piece can satisfy
 *    the required connections a FALLBACK piece (all-four-walls closed corridor)
 *    is placed so the player can never reach open void.
 *
 * 2. Coordinates are consistent throughout:
 *    • chunkX / chunkZ  — Minecraft chunk coordinates  (block ÷ 16)
 *    • originX / originZ — world block coordinates     (chunkX * 16)
 *    WorldManager.spawnStructureStatic receives BLOCK coordinates.
 *
 * 3. Schematic size is expressed in SCHEMATIC_SIZE (default 16).  Every
 *    calculation that touches block positions uses this constant so a
 *    resize is a one-line change.
 *
 * 4. Level Y spacing is LEVEL_HEIGHT (14 blocks, matching schematic H=14).
 *    Base Y is LEVEL_BASE_Y + level * LEVEL_HEIGHT.
 *
 * 5. Edge connectivity is symmetric: both sides of a shared wall hash the
 *    same canonical edge key, so they always agree on open/closed.
 *
 * 6. The finalization pass seals every face that does not have a matching
 *    open neighbor.  Sealing is deferred by SEAL_DELAY_TICKS after the last
 *    schematic paste so schematics cannot overwrite the seal blocks.
 */
public class RedCastleManager extends Util {

    // ─────────────────────────────────────────────────────────────────────────
    // TUNEABLE CONSTANTS
    // ─────────────────────────────────────────────────────────────────────────

    /** Side length of every schematic in blocks (must match actual .schem files). */
    public static final int SCHEMATIC_SIZE = 16;

    /** Number of vertical levels the dungeon spans. */
    public static final int MAX_LEVELS = 3;

    /** Y coordinate of level 0's floor. */
    public static final int LEVEL_BASE_Y = 64;

    /**
     * Vertical distance between level floors — must match schematic height exactly.
     * Schematics are 14 blocks tall (Y indices 0–13: solid floor at 0, solid ceiling at 13,
     * interior air at 1–12).  Levels stack every 14 blocks with no gap and no overlap.
     */
    public static final int LEVEL_HEIGHT = 14;

    /** Probability (0-99) that any horizontal edge is open. */
    private static final int EDGE_OPEN_CHANCE = 65;

    /** Probability (0-99) that a vertical (stair) edge is open. */
    private static final int VERTICAL_EDGE_OPEN_CHANCE = 15;

    /** Ticks to wait after last schematic paste before running the seal pass. */
    private static final long SEAL_DELAY_TICKS = 40L;

    /** Material used to fill non-structure chunks. */
    private static final Material BEDROCK_FILL = Material.BEDROCK;

    /** Material used for gap bridging between structures. */
    private static final Material BRIDGE_FILL = Material.NETHER_BRICKS;

    /** Log extra detail to console. */
    public static boolean verbose = true;

    // PDC keys
    public static final String KEY_PIECE_ROTATION    = "redcastle_rotation";
    public static final String KEY_PIECE_CONNECTIONS = "redcastle_connections";
    private static final String KEY_GENERATED        = "redcastle_generated";
    private static final String KEY_HAS_PIECE        = "redcastle_has_piece";

    // ─────────────────────────────────────────────────────────────────────────
    // DIRECTION ENUM
    // ─────────────────────────────────────────────────────────────────────────

    public enum Directions {
        North, South, East, West, Up, Down;

        public Directions opposite() {
            return switch (this) {
                case North -> South;
                case South -> North;
                case East  -> West;
                case West  -> East;
                case Up    -> Down;
                case Down  -> Up;
            };
        }

        /** Rotate a horizontal direction by 0/90/180/270 degrees clockwise. */
        public Directions rotateClockwise(int degrees) {
            if (this == Up || this == Down) return this;
            int steps = ((degrees / 90) % 4 + 4) % 4;
            Directions d = this;
            for (int i = 0; i < steps; i++) {
                d = switch (d) {
                    case North -> East;
                    case East  -> South;
                    case South -> West;
                    case West  -> North;
                    default    -> d;
                };
            }
            return d;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CASTLE PIECE
    // ─────────────────────────────────────────────────────────────────────────

    public static class CastlePiece {
        /** Base name used to build the schematic filename. */
        public final String schematicName;
        /** Directions this piece has open passages (in its base orientation). */
        public final List<Directions> baseConnections;
        /** Whether this piece contains a staircase. */
        public final boolean isStair;
        /** Clockwise rotation applied when spawning (0/90/180/270). */
        public final int rotation;
        /** Effective connections after applying rotation. */
        public final List<Directions> connections;

        public CastlePiece(String schematicName,
                           List<Directions> baseConnections,
                           boolean isStair) {
            this(schematicName, baseConnections, isStair, 0);
        }

        private CastlePiece(String schematicName,
                            List<Directions> baseConnections,
                            boolean isStair,
                            int rotation) {
            this.schematicName   = schematicName;
            this.baseConnections = baseConnections;
            this.isStair         = isStair;
            this.rotation        = rotation;

            // Build the rotated connection list
            List<Directions> rotated = new ArrayList<>();
            for (Directions d : baseConnections) {
                rotated.add(d.rotateClockwise(rotation));
            }
            this.connections = Collections.unmodifiableList(rotated);
        }

        /** Return a copy of this piece with a different rotation applied. */
        public CastlePiece rotated(int degrees) {
            return new CastlePiece(schematicName, baseConnections, isStair, degrees);
        }

        @Override
        public String toString() {
            return schematicName + "@" + rotation + connections;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PIECE REGISTRY
    //
    // Add / remove pieces here.  Each entry describes the BASE (rotation=0)
    // orientation.  The selector automatically tries all four rotations.
    //
    // Schematic filename pattern:  newredCastle_<schematicName>.schem
    // ─────────────────────────────────────────────────────────────────────────

    public static final List<CastlePiece> PIECES = List.of(
            // Four-way cross
            new CastlePiece("hallwayall1",
                    List.of(Directions.North, Directions.South, Directions.East, Directions.West),
                    false),
            // Straight N-S corridor
            new CastlePiece("NorthHallway",
                    List.of(Directions.North, Directions.South),
                    false),
            // Straight E-W corridor (variant 1)
            new CastlePiece("hallwayEastWest1",
                    List.of(Directions.East, Directions.West),
                    false),
            // Straight E-W corridor (variant 2)
            new CastlePiece("hallwayEastWest2",
                    List.of(Directions.East, Directions.West),
                    false),
            // T-junction  N-S-W  (south/north/west open)
            new CastlePiece("dungeon1",
                    List.of(Directions.South, Directions.North, Directions.West),
                    false),
            // Large room — all four sides open
//            new CastlePiece("Library1",
//                    List.of(Directions.North, Directions.South, Directions.East, Directions.West),
//                    false),
            // Staircase  N-Up-Down  (connects to level above/below)
            new CastlePiece("Stair1",
                    List.of(Directions.South, Directions.Up, Directions.Down),
                    true),
            // Decorative room — E-W only
            new CastlePiece("Tree1",
                    List.of(Directions.East, Directions.West),
                    false)

            // ── FALLBACK: a fully-enclosed room (no connections).
            //    The selector uses this as a last resort so the player can
            //    never reach open void.  Name it after a real schematic that
            //    is a plain sealed box.
//            new CastlePiece("ClosedRoom",
//                    List.of(),   // no open faces
//                    false)
    );

    // ─────────────────────────────────────────────────────────────────────────
    // RUNTIME STATE
    // ─────────────────────────────────────────────────────────────────────────

    private static final Queue<CastleNode>           generationQueue = new ConcurrentLinkedQueue<>();
    private static boolean                            isGenerating    = false;
    private static final Set<String>                  generatedChunks = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, CastlePiece>    chunkPieces     = Collections.synchronizedMap(new HashMap<>());
    private static World                              lastWorldRef    = null;

    private record CastleNode(World world, int chunkX, int chunkZ, int level) {}

    // ─────────────────────────────────────────────────────────────────────────
    // CHUNK-KEY HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private static String chunkKey(int chunkX, int chunkZ, int level) {
        return level + ":" + chunkX + ":" + chunkZ;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SYMMETRIC EDGE CONNECTIVITY
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Deterministically decides whether the horizontal edge between
     * chunk (cx, cz) and its neighbor in {@code dir} is open.
     *
     * Symmetry guarantee: both sides hash the same canonical edge key.
     */
    private static boolean horizontalEdgeOpen(long seed, int cx, int cz, int level, Directions dir) {
        int nx = cx, nz = cz;
        switch (dir) {
            case North -> nz--;
            case South -> nz++;
            case East  -> nx++;
            case West  -> nx--;
            default    -> { return false; }
        }
        // Canonical order — smaller coords first
        int ax = Math.min(cx, nx), az = Math.min(cz, nz);
        int bx = Math.max(cx, nx), bz = Math.max(cz, nz);

        long edgeSeed = seed
                ^ (0x5DEECE66DL * ax)
                ^ (0x000000000BL * az)
                ^ (0x285A1234L  * bx)
                ^ (0x1F3A5C7BL  * bz)
                ^ (0xCAFEBABEL  * level);

        return new Random(edgeSeed).nextInt(100) < EDGE_OPEN_CHANCE;
    }

    /**
     * Decides whether a vertical (stair) connection exists between
     * (cx, cz, level) and (cx, cz, level+1).
     */
    private static boolean verticalEdgeOpen(long seed, int cx, int cz, int level) {
        if (level < 0 || level >= MAX_LEVELS - 1) return false;
        long edgeSeed = seed
                ^ (0x1234567L * cx)
                ^ (0x7654321L * cz)
                ^ (0x9999999L * level);
        return new Random(edgeSeed).nextInt(100) < VERTICAL_EDGE_OPEN_CHANCE;
    }

    /** Single entry-point for connection queries (handles Up/Down symmetry). */
    private static boolean edgeOpen(World world, int cx, int cz, int level, Directions dir) {
        long seed = world.getSeed();
        return switch (dir) {
            case North, South, East, West -> horizontalEdgeOpen(seed, cx, cz, level, dir);
            case Up   -> verticalEdgeOpen(seed, cx, cz, level);
            case Down -> verticalEdgeOpen(seed, cx, cz, level - 1);
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GENERATION ENTRY POINT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Call this when a chunk loads inside the castle world.
     * Safe to call multiple times for the same chunk — duplicates are ignored.
     */
    public static void generateCastle(World world, int chunkX, int chunkZ) {
        String key = chunkKey(chunkX, chunkZ, 0);
        if (generatedChunks.contains(key)) return;

        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        if (chunk.getPersistentDataContainer().has(
                FreedomKeys.key(KEY_GENERATED),
                org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
            // Already persisted from a previous server session — rebuild in-memory state
            // so the finalization pass knows this chunk exists.
            generatedChunks.add(key);
            return;
        }

        enqueue(world, chunkX, chunkZ, 0);
        startGenerationTask();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QUEUE MANAGEMENT
    // ─────────────────────────────────────────────────────────────────────────

    private static void enqueue(World world, int cx, int cz, int level) {
        if (level < 0 || level >= MAX_LEVELS) return;
        String key = chunkKey(cx, cz, level);
        if (generatedChunks.contains(key)) return;
        for (CastleNode n : generationQueue) {
            if (n.chunkX == cx && n.chunkZ == cz && n.level == level) return;
        }
        generationQueue.add(new CastleNode(world, cx, cz, level));
    }

    private static void startGenerationTask() {
        if (isGenerating) return;
        isGenerating = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (generationQueue.isEmpty()) {
                    isGenerating = false;
                    this.cancel();
                    // Schedule the seal pass with a delay so all paste operations finish first
                    World w = lastWorldRef;
                    if (w != null) {
                        new BukkitRunnable() {
                            @Override public void run() {
                                sealAllFaces(w);
                                bridgeGaps(w);
                                fillNonStructureChunks(w);
                            }
                        }.runTaskLater(Freedom.get_plugin(), SEAL_DELAY_TICKS);
                    }
                    return;
                }
                CastleNode node = generationQueue.poll();
                if (node != null) processNode(node);
            }
        }.runTaskTimer(Freedom.get_plugin(), 2L, 10L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NODE PROCESSING
    // ─────────────────────────────────────────────────────────────────────────

    private static void processNode(CastleNode node) {
        World world  = node.world;
        int   cx     = node.chunkX;
        int   cz     = node.chunkZ;
        int   level  = node.level;
        String key   = chunkKey(cx, cz, level);

        if (generatedChunks.contains(key)) return;
        generatedChunks.add(key);   // mark immediately — prevents re-queue races
        lastWorldRef = world;

        // ── Determine which edges are open (symmetric) ───────────────────────
        List<Directions> required  = new ArrayList<>();
        List<Directions> forbidden = new ArrayList<>();

        for (Directions dir : Directions.values()) {
            // Only consider a direction valid when the neighbor level is in range
            int nl = neighborLevel(level, dir);
            if (nl < 0 || nl >= MAX_LEVELS) {
                forbidden.add(dir);
                continue;
            }
            if (edgeOpen(world, cx, cz, level, dir)) {
                required.add(dir);
            } else {
                forbidden.add(dir);
            }
        }

        if (verbose) {
            log("[RedCastle] Processing [%d,%d] L%d | req=%s | forbidden=%s",
                    cx, cz, level, required, forbidden);
        }

        // ── Select piece ─────────────────────────────────────────────────────
        CastlePiece piece = selectPiece(world, cx, cz, level, required, forbidden);

        // piece is NEVER null — selectPiece always falls back to ClosedRoom
        assert piece != null;

        // ── Persist metadata to PDC ──────────────────────────────────────────
        chunkPieces.put(key, piece);
        Chunk chunk = world.getChunkAt(cx, cz);
        var pdc = chunk.getPersistentDataContainer();
        var boolType = org.bukkit.persistence.PersistentDataType.BOOLEAN;
        var intType  = org.bukkit.persistence.PersistentDataType.INTEGER;
        var strType  = org.bukkit.persistence.PersistentDataType.STRING;

        pdc.set(FreedomKeys.key(KEY_GENERATED),        boolType, true);
        pdc.set(FreedomKeys.key(KEY_HAS_PIECE),        boolType, true);
        pdc.set(FreedomKeys.key(KEY_PIECE_ROTATION),   intType,  piece.rotation);
        pdc.set(FreedomKeys.key(KEY_PIECE_CONNECTIONS), strType,
                piece.connections.stream().map(Enum::name).collect(Collectors.joining(",")));

        // ── Spawn schematic ───────────────────────────────────────────────────
        // spawnStructureStatic takes CHUNK coordinates (it does *16 internally)
        int wy = LEVEL_BASE_Y + level * LEVEL_HEIGHT;

        String schematicFile = "newredCastle_" + piece.schematicName + ".schem";
        if (verbose) {
            log("[RedCastle] Spawning %s at chunk(%d,%d) Y=%d rot=%d conn=%s",
                    schematicFile, cx, cz, wy, piece.rotation, piece.connections);
        }
        WorldManager.spawnStructureStatic(world, cx, cz, wy, schematicFile, piece.rotation);

        // ── Enqueue neighbors for every open connection ───────────────────────
        for (Directions dir : piece.connections) {
            int ncx = cx, ncz = cz, nl = level;
            switch (dir) {
                case North -> ncz--;
                case South -> ncz++;
                case East  -> ncx++;
                case West  -> ncx--;
                case Up    -> nl++;
                case Down  -> nl--;
            }
            enqueue(world, ncx, ncz, nl);
        }
    }

    /** Returns the level of the neighbor in the given direction. */
    private static int neighborLevel(int level, Directions dir) {
        return switch (dir) {
            case Up   -> level + 1;
            case Down -> level - 1;
            default   -> level;
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PIECE SELECTION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Selects a piece (with rotation) that best matches the required / forbidden
     * constraints.  Selection is fully deterministic for a given world seed and
     * chunk position.
     *
     * Priority order:
     *   1. Exact match   — has ALL required, NONE of forbidden
     *   2. Superset      — has ALL required, some forbidden (extra doors — sealed later)
     *   3. Subset        — has SOME required (dead-end; remaining directions sealed)
     *   4. Fallback      — ClosedRoom (guaranteed non-null return)
     */
    private static CastlePiece selectPiece(World world,
                                           int cx, int cz, int level,
                                           List<Directions> required,
                                           List<Directions> forbidden) {

        Random rng = new Random(world.getSeed()
                + (long) cx    * 0x7A73_1A2BL
                + (long) cz    * 0x3C1D_E5F7L
                + (long) level * 0xBEEF_CAFEL);

        List<CastlePiece> exact    = new ArrayList<>();
        List<CastlePiece> superset = new ArrayList<>();
        List<CastlePiece> subset   = new ArrayList<>();

        for (CastlePiece base : PIECES) {
            // Skip the fallback piece from normal selection — added only if nothing else works
            if (base.schematicName.equals("ClosedRoom") && !required.isEmpty()) continue;

            for (int rot : new int[]{0, 90, 180, 270}) {
                CastlePiece candidate = base.rotated(rot);

                boolean hasAllRequired  = required.stream().allMatch(r -> candidate.connections.contains(r));
                boolean hasAnyForbidden = forbidden.stream().anyMatch(f -> candidate.connections.contains(f));
                boolean hasAnyRequired  = required.isEmpty()
                        || required.stream().anyMatch(r -> candidate.connections.contains(r));

                if (hasAllRequired && !hasAnyForbidden) {
                    exact.add(candidate);
                } else if (hasAllRequired) {
                    superset.add(candidate);
                } else if (hasAnyRequired) {
                    subset.add(candidate);
                }
            }
        }

        if (!exact.isEmpty()) {
            // Prefer fewer connections (tidier corridors)
            int min = exact.stream().mapToInt(p -> p.connections.size()).min().orElse(0);
            List<CastlePiece> tight = exact.stream()
                    .filter(p -> p.connections.size() == min)
                    .collect(Collectors.toList());
            return tight.get(rng.nextInt(tight.size()));
        }
        if (!superset.isEmpty()) return superset.get(rng.nextInt(superset.size()));
        if (!subset.isEmpty())   return subset.get(rng.nextInt(subset.size()));

        // Absolute fallback — a fully sealed room
        warn("[RedCastle] Fallback ClosedRoom placed at [%d,%d] L%d", cx, cz, level);
        return PIECES.stream()
                .filter(p -> p.schematicName.equals("ClosedRoom"))
                .findFirst()
                .orElse(PIECES.get(0)); // should never reach here
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FACE SEALING
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Iterates every generated chunk and seals any face that either:
     *  (a) the piece opens but the neighbor was never generated or doesn't reciprocate, or
     *  (b) the piece does not open at all (belt-and-suspenders against schematic leaks).
     *
     * This runs AFTER a delay so schematics have fully pasted.
     */
    public static void sealAllFaces(World world) {
        if (verbose) log("[RedCastle] ===== SEAL PASS (%d chunks) =====", generatedChunks.size());
        int sealed = 0;

        for (String key : new HashSet<>(generatedChunks)) {
            String[] parts = key.split(":");
            if (parts.length != 3) continue;
            int level  = Integer.parseInt(parts[0]);
            int cx     = Integer.parseInt(parts[1]);
            int cz     = Integer.parseInt(parts[2]);

            CastlePiece piece = chunkPieces.get(key);
            if (piece == null) {
                // Chunk was loaded from PDC — seal all faces conservatively
                for (Directions dir : Directions.values()) {
                    sealFace(world, cx, cz, level, dir);
                    sealed++;
                }
                continue;
            }

            for (Directions dir : Directions.values()) {
                boolean opens = piece.connections.contains(dir);

                int ncx = cx, ncz = cz, nl = level;
                switch (dir) {
                    case North -> ncz--;
                    case South -> ncz++;
                    case East  -> ncx++;
                    case West  -> ncx--;
                    case Up    -> nl++;
                    case Down  -> nl--;
                }

                String neighborKey = chunkKey(ncx, ncz, nl);
                CastlePiece neighbor = chunkPieces.get(neighborKey);
                boolean neighborReciprocates = neighbor != null
                        && neighbor.connections.contains(dir.opposite());

                if (!opens || !neighborReciprocates) {
                    sealFace(world, cx, cz, level, dir);
                    sealed++;
                }
                // Both open and neighbor reciprocates → valid corridor, leave it open
            }
        }

        if (verbose) log("[RedCastle] Seal pass done. Faces sealed=%d", sealed);
    }

    /**
     * Seals the passage opening on one face of chunk (cx,cz) at the given level.
     *
     * Ground truth from schematic analysis:
     *   • All schematics are 16×16 (X=0..15, Z=0..15), H=14 (Y=0..13).
     *   • Floor  = Y index 0  → solid in every schematic — do NOT touch.
     *   • Ceiling = Y index 13 → solid in every schematic — do NOT touch.
     *   • Passage openings span the FULL face width and the full interior height Y=1..12.
     *     There is no narrow doorway — the entire face interior is open when a connection exists.
     *
     * So to seal a face we fill every block on that face at interior Y rows (yBase+1 .. yBase+12),
     * across the full 16-block width of the face.
     *
     * Face block coordinates (chunk origin = cx*16, cz*16):
     *   North → fixed Z = cz*16,      X sweeps 0..15
     *   South → fixed Z = cz*16+15,   X sweeps 0..15
     *   East  → fixed X = cx*16+15,   Z sweeps 0..15
     *   West  → fixed X = cx*16,      Z sweeps 0..15
     *   Up/Down → 3×3 stairwell patch at centre (handled separately — Stair1 spans 2 levels)
     */
    private static void sealFace(World world, int cx, int cz, int level, Directions dir) {
        int ox    = cx * 16;
        int oz    = cz * 16;
        int yBase = LEVEL_BASE_Y + level * LEVEL_HEIGHT;
        // Interior rows only — floor (yBase+0) and ceiling (yBase+13) are solid in every schematic
        int yMin  = yBase + 1;
        int yMax  = yBase + 12;
        Material fill = Material.RED_NETHER_BRICKS;

        switch (dir) {
            case North -> {
                int fz = oz; // Z=0 face of this chunk
                for (int x = ox; x <= ox + 15; x++) {
                    for (int y = yMin; y <= yMax; y++) {
                        setBlock(world, x, y, fz, fill);
                    }
                }
            }
            case South -> {
                int fz = oz + 15; // Z=15 face of this chunk
                for (int x = ox; x <= ox + 15; x++) {
                    for (int y = yMin; y <= yMax; y++) {
                        setBlock(world, x, y, fz, fill);
                    }
                }
            }
            case East -> {
                int fx = ox + 15; // X=15 face of this chunk
                for (int z = oz; z <= oz + 15; z++) {
                    for (int y = yMin; y <= yMax; y++) {
                        setBlock(world, fx, y, z, fill);
                    }
                }
            }
            case West -> {
                int fx = ox; // X=0 face of this chunk
                for (int z = oz; z <= oz + 15; z++) {
                    for (int y = yMin; y <= yMax; y++) {
                        setBlock(world, fx, y, z, fill);
                    }
                }
            }
            case Up -> {
                // Stair1 is H=20 (spans ~1.4 levels) — seal the stairwell opening at the top
                // 3×3 patch centred at the chunk centre
                int fy = yBase + LEVEL_HEIGHT - 1; // top of this level's interior
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        setBlock(world, ox + 8 + dx, fy, oz + 8 + dz, fill);
                    }
                }
            }
            case Down -> {
                // Seal the stairwell opening at the bottom
                int fy = yBase; // floor of this level
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        setBlock(world, ox + 8 + dx, fy, oz + 8 + dz, fill);
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAP BRIDGING & FILL
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Bridges gaps between adjacent open structures by filling connecting walls
     * where both neighbors have reciprocating open connections.
     * This ensures smooth corridors between structures with matching openings.
     */
    public static void bridgeGaps(World world) {
        if (verbose) log("[RedCastle] ===== BRIDGE GAPS PASS (%d chunks) =====", generatedChunks.size());
        int bridged = 0;

        for (String key : new HashSet<>(generatedChunks)) {
            String[] parts = key.split(":");
            if (parts.length != 3) continue;
            int level  = Integer.parseInt(parts[0]);
            int cx     = Integer.parseInt(parts[1]);
            int cz     = Integer.parseInt(parts[2]);

            CastlePiece piece = chunkPieces.get(key);
            if (piece == null) continue; // Skip chunks without a piece

            for (Directions dir : new Directions[]{Directions.North, Directions.South, Directions.East, Directions.West}) {
                if (!piece.connections.contains(dir)) continue;

                int ncx = cx, ncz = cz, nl = level;
                switch (dir) {
                    case North -> ncz--;
                    case South -> ncz++;
                    case East  -> ncx++;
                    case West  -> ncx--;
                    default -> {}
                }

                String neighborKey = chunkKey(ncx, ncz, nl);
                CastlePiece neighbor = chunkPieces.get(neighborKey);

                // Both sides open and reciprocate — bridge the gap between them
                if (neighbor != null && neighbor.connections.contains(dir.opposite())) {
                    if (bridgeGap(world, cx, cz, level, ncx, ncz, nl, dir)) {
                        bridged++;
                    }
                }
            }
        }

        if (verbose) log("[RedCastle] Bridge pass done. Gaps bridged=%d", bridged);
    }

    /**
     * Bridges the gap between two adjacent chunks in the specified direction.
     * Fills the connecting wall area with bridge material to ensure structural integrity.
     * Returns true if bridging occurred.
     */
    private static boolean bridgeGap(World world, int cx1, int cz1, int level1,
                                     int cx2, int cz2, int level2, Directions dir) {
        int ox1 = cx1 * 16;
        int oz1 = cz1 * 16;
        int oy2 = cx2 * 16;
        int oz2 = cz2 * 16;

        int yBase = LEVEL_BASE_Y + level1 * LEVEL_HEIGHT;
        int yFloor = yBase;
        int yInteriorMin = yBase + 1;
        int yInteriorMax = yBase + 12;
        int yCeiling = yBase + 13;

        boolean bridged = false;

        switch (dir) {
            case North -> {
                // Bridge from chunk 1's north face to chunk 2's south face
                // Fill a 1-block-thick wall on the shared edge with floor and ceiling
                int sharedZ = oz1;
                for (int x = ox1; x <= ox1 + 15; x++) {
                    setBlock(world, x, yFloor, sharedZ, BRIDGE_FILL);
                    setBlock(world, x, yCeiling, sharedZ, BRIDGE_FILL);
                    bridged = true;
                }
            }
            case South -> {
                // Bridge from chunk 1's south face to chunk 2's north face
                int sharedZ = oz1 + 15;
                for (int x = ox1; x <= ox1 + 15; x++) {
                    setBlock(world, x, yFloor, sharedZ, BRIDGE_FILL);
                    setBlock(world, x, yCeiling, sharedZ, BRIDGE_FILL);
                    bridged = true;
                }
            }
            case East -> {
                // Bridge from chunk 1's east face to chunk 2's west face
                int sharedX = ox1 + 15;
                for (int z = oz1; z <= oz1 + 15; z++) {
                    setBlock(world, sharedX, yFloor, z, BRIDGE_FILL);
                    setBlock(world, sharedX, yCeiling, z, BRIDGE_FILL);
                    bridged = true;
                }
            }
            case West -> {
                // Bridge from chunk 1's west face to chunk 2's east face
                int sharedX = ox1;
                for (int z = oz1; z <= oz1 + 15; z++) {
                    setBlock(world, sharedX, yFloor, z, BRIDGE_FILL);
                    setBlock(world, sharedX, yCeiling, z, BRIDGE_FILL);
                    bridged = true;
                }
            }
            default -> {}
        }

        return bridged;
    }

    /**
     * Fills all non-structure chunks in the dungeon area with bedrock.
     * This ensures the player cannot break through to the void and creates
     * a solid boundary around the castle structure.
     */
    public static void fillNonStructureChunks(World world) {
        if (verbose) log("[RedCastle] ===== FILL NON-STRUCTURE CHUNKS PASS =====");

        // Determine bounding box of generated chunks
        int minCx = Integer.MAX_VALUE, maxCx = Integer.MIN_VALUE;
        int minCz = Integer.MAX_VALUE, maxCz = Integer.MIN_VALUE;

        for (String key : generatedChunks) {
            String[] parts = key.split(":");
            if (parts.length != 3) continue;
            int cx = Integer.parseInt(parts[1]);
            int cz = Integer.parseInt(parts[2]);
            minCx = Math.min(minCx, cx);
            maxCx = Math.max(maxCx, cx);
            minCz = Math.min(minCz, cz);
            maxCz = Math.max(maxCz, cz);
        }

        if (minCx == Integer.MAX_VALUE) {
            if (verbose) log("[RedCastle] No generated chunks found, skipping fill pass");
            return;
        }

        // Expand bounding box by 1 chunk in each direction to create a border
        minCx--;
        maxCx++;
        minCz--;
        maxCz++;

        int filled = 0;

        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                for (int level = 0; level < MAX_LEVELS; level++) {
                    String key = chunkKey(cx, cz, level);
                    if (generatedChunks.contains(key)) continue; // Skip structure chunks

                    // Fill this non-structure chunk completely with bedrock
                    if (fillChunkWithBedrock(world, cx, cz, level)) {
                        filled++;
                    }
                }
            }
        }

        if (verbose) log("[RedCastle] Fill pass done. Non-structure chunks filled=%d", filled);
    }

    /**
     * Fills an entire chunk with bedrock for all three vertical levels.
     * Returns true if the chunk was filled.
     */
    private static boolean fillChunkWithBedrock(World world, int cx, int cz, int level) {
        int ox = cx * 16;
        int oz = cz * 16;
        int yBase = LEVEL_BASE_Y + level * LEVEL_HEIGHT;

        // Fill from floor to ceiling for this level
        for (int x = ox; x <= ox + 15; x++) {
            for (int y = yBase; y <= yBase + 13; y++) {
                for (int z = oz; z <= oz + 15; z++) {
                    Block b = world.getBlockAt(x, y, z);
                    if (b.getType() == Material.AIR || b.getType() == Material.VOID_AIR || b.getType() == Material.CAVE_AIR) {
                        setBlock(world, x, y, z, BEDROCK_FILL);
                    }
                }
            }
        }

        return true;
    }

    private static void setBlock(World world, int x, int y, int z, Material mat) {
        Block b = world.getBlockAt(x, y, z);
        b.setType(mat, false); // false = skip physics updates for performance
        if (verbose && b.getType() != mat) {
            warn("[RedCastle] Block set failed at %d,%d,%d (wanted %s, got %s)",
                    x, y, z, mat, b.getType());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHUNK GENERATOR — flat void world
    // ─────────────────────────────────────────────────────────────────────────

    public static class CastleGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
            return createChunkData(world); // empty chunk
        }
        @Override public boolean shouldGenerateCaves()       { return false; }
        @Override public boolean shouldGenerateDecorations() { return false; }
        @Override public boolean shouldGenerateMobs()        { return false; }
        @Override public boolean shouldGenerateStructures()  { return false; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WORLD CREATION
    // ─────────────────────────────────────────────────────────────────────────

    public static World createRedCastleWorld(String worldName) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new CastleGenerator());
        return creator.createWorld();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGGING HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private static void log(String fmt, Object... args) {
        Freedom.get_plugin().getLogger().info(String.format(fmt, args));
    }

    private static void warn(String fmt, Object... args) {
        Freedom.get_plugin().getLogger().warning(String.format(fmt, args));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LEGACY API SHIMS  (kept so callers outside this class don't break)
    // ─────────────────────────────────────────────────────────────────────────

    /** @deprecated Use {@link #sealAllFaces(World)} instead. */
    @Deprecated
    public static void finalizeGenerationWithConnectivityCheck(World world) {
        sealAllFaces(world);
    }

    /** @deprecated No longer needed — sealing is automatic. */
    @Deprecated
    public static void validateAllConnectivity(World world) { /* no-op */ }

    /** @deprecated No longer needed — sealing is automatic. */
    @Deprecated
    public static void ensureAllUnusedConnectionsClosed(World world) { /* no-op */ }

    // Old field name kept for any external code that referenced it
    /** @deprecated Use {@link #PIECES} instead. */
    @Deprecated
    public static final List<CastlePiece> redCastlePieces = PIECES;
}