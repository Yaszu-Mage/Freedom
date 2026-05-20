package xyz.yaszu.freedom.Subsystems;

import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.createChunkData;

/**
 * RedCastleManager — infinite procedural dungeon generator.
 *
 * ═══════════════════════════════════════════════════════════
 * GROUND TRUTH (measured from actual .schem files)
 * ═══════════════════════════════════════════════════════════
 *
 * VALID PIECES (W=16, H=14, L=16 unless noted):
 *   dungeon1        — ALL 4 horizontal faces open, solid floor, open ceiling
 *   hallwayall1     — ALL 4 horizontal faces open, solid floor, open ceiling
 *   hallwayEastWest1— ALL 4 horizontal faces open, solid floor, open ceiling
 *   NorthHallway    — ALL 4 horizontal faces open, solid floor, open ceiling
 *   Stair1 (H=20)   — ALL 4 horizontal faces open, bridges 2 levels vertically
 *
 * REMOVED (broken schematics):
 *   hallwayEastWest2 — H=13, broken floor (142 air at Y=0), broken ceiling
 *   Tree1            — H=13, broken floor (15 air at Y=0), broken ceiling
 *   Library1         — W=1, single-column schematic, unusable
 *
 * ARCHITECTURAL FACTS:
 *   • Every valid schematic has ALL 4 horizontal faces open (air Y=1..12).
 *   • Floor (Y=0) is solid in every piece — never touch it.
 *   • Ceiling (Y=13) is fully air — the level ABOVE provides the ceiling via its floor.
 *   • All walls between rooms come entirely from sealFace() RED_NETHER_BRICKS.
 *   • Rotation is cosmetic only — all pieces open on all 4 sides regardless.
 *   • Stair1 (H=20) occupies 2 level slots; it bridges level N and level N+1.
 *
 * COORDINATE SYSTEM:
 *   chunkX/chunkZ             — Minecraft chunk coords  (÷16)
 *   spawnStructureStatic()    — takes CHUNK coords, multiplies by 16 internally
 *   world.getBlockAt()        — takes BLOCK coords (cx*16 + offset)
 *   LEVEL_HEIGHT = 14         — floor-to-floor distance matching schematic H=14
 *   Level N floor Y           — LEVEL_BASE_Y + N * LEVEL_HEIGHT
 *
 * TIMING FIX (critical):
 *   FAWE pastes asynchronously. Operations.complete() returns before blocks land.
 *   sealFace() called immediately after paste gets overwritten by FAWE air blocks.
 *   FIX: seal runs in TWO passes — SEAL_PASS1_TICKS and SEAL_PASS2_TICKS after paste.
 *   Pass 1 catches normal FAWE completion. Pass 2 catches slow/lagging servers.
 *   Both passes write the same blocks so double-writing is harmless.
 *
 * DOUBLE-GENERATION FIX:
 *   enqueuedChunks tracks every chunk added to queue OR already generated.
 *   world.getChunkAt() (force-load) fires ChunkLoadEvent → generateCastle → enqueue.
 *   Since we add to enqueuedChunks BEFORE calling getChunkAt(), the event's enqueue
 *   call returns immediately without adding a duplicate.
 */
public class RedCastleManager extends Util {

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTANTS — all verified against actual schematic files
    // ─────────────────────────────────────────────────────────────────────────

    /** Number of vertical dungeon levels (0 to MAX_LEVELS-1). */
    public static final int MAX_LEVELS = 5;

    /** Y coordinate of level 0 floor. */
    public static final int LEVEL_BASE_Y = 64;

    /**
     * Floor-to-floor distance = schematic H = 14.
     * Level N occupies Y: [LEVEL_BASE_Y + N*14 .. LEVEL_BASE_Y + N*14 + 13]
     * Floor is Y+0 (solid), ceiling is Y+13 (air, provided by level above's floor).
     */
    public static final int LEVEL_HEIGHT = 14;

    /**
     * Interior Y range to seal (relative to level base Y).
     * Floor (Y+0) and ceiling row (Y+13) are handled by schematics / level stacking.
     * We seal Y+1 through Y+12 — the walkable interior.
     */
    private static final int SEAL_Y_MIN = 1;   // inclusive, relative to level base
    private static final int SEAL_Y_MAX = 12;  // inclusive, relative to level base

    /** % chance (0-99) that a horizontal edge between two chunks is open. */
    private static final int EDGE_OPEN_CHANCE = 60;

    /** % chance (0-99) that a vertical (stair) edge exists between levels. */
    private static final int VERTICAL_EDGE_CHANCE = 12;

    /** Ticks between queue polls (2 = every 100ms). */
    private static final long TASK_PERIOD_TICKS = 2L;

    /** Chunks processed per poll cycle. */
    private static final int NODES_PER_TICK = 2;

    /**
     * Ticks after paste before FIRST seal pass.
     * 60 ticks = 3 seconds — covers most FAWE async completions.
     */
    private static final long SEAL_PASS1_TICKS = 60L;

    /**
     * Ticks after paste before SECOND (confirmation) seal pass.
     * 120 ticks = 6 seconds — catches slow servers and large FAWE queues.
     */
    private static final long SEAL_PASS2_TICKS = 120L;

    public static boolean verbose = true;

    // PDC keys stored on chunk persistent data containers
    public static final String KEY_PIECE_NAME    = "redcastle_piece";
    public static final String KEY_PIECE_ROT     = "redcastle_rotation";
    public static final String KEY_OPEN_DIRS     = "redcastle_open_dirs";
    private static final String KEY_GENERATED    = "redcastle_generated";

    // ─────────────────────────────────────────────────────────────────────────
    // DIRECTIONS
    // ─────────────────────────────────────────────────────────────────────────

    public enum Dir {
        North, South, East, West, Up, Down;

        public Dir opposite() {
            return switch (this) {
                case North -> South; case South -> North;
                case East  -> West;  case West  -> East;
                case Up    -> Down;  case Down  -> Up;
            };
        }

        /** Clockwise rotation of horizontal directions only. */
        public Dir rotCW(int degrees) {
            if (this == Up || this == Down) return this;
            int steps = ((degrees / 90) % 4 + 4) % 4;
            Dir d = this;
            for (int i = 0; i < steps; i++) {
                d = switch (d) {
                    case North -> East;  case East  -> South;
                    case South -> West;  case West  -> North;
                    default    -> d;
                };
            }
            return d;
        }

        public int dx() { return switch(this){case East->1;case West->-1;default->0;}; }
        public int dz() { return switch(this){case South->1;case North->-1;default->0;}; }
        public int dl() { return switch(this){case Up->1;case Down->-1;default->0;}; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PIECE DEFINITION
    // ─────────────────────────────────────────────────────────────────────────

    public static class Piece {
        public final String name;
        /** How many level slots this piece occupies vertically (1 = normal, 2 = Stair1). */
        public final int levelSpan;
        /** Applied rotation (0/90/180/270). All pieces open all 4 sides so rotation is cosmetic. */
        public final int rotation;

        public Piece(String name, int levelSpan, int rotation) {
            this.name      = name;
            this.levelSpan = levelSpan;
            this.rotation  = rotation;
        }

        public Piece rotated(int r) { return new Piece(name, levelSpan, r); }
        public String schematicFile() { return "newredCastle_" + name + ".schem"; }
        @Override public String toString() { return name + "@" + rotation; }
    }

    /**
     * VALID PIECES ONLY — broken schematics (hallwayEastWest2, Tree1, Library1) excluded.
     *
     * All horizontal pieces open on all 4 sides. Rotation is cosmetic variety only.
     * Stair1 spans 2 levels and is only placed when a vertical connection is needed.
     */
    private static final List<Piece> STANDARD_PIECES = List.of(
            new Piece("dungeon1",         1, 0),
            new Piece("hallwayall1",      1, 0),
            new Piece("hallwayEastWest1", 1, 0),
            new Piece("NorthHallway",     1, 0)
    );

    private static final Piece STAIR_PIECE = new Piece("Stair1", 2, 0);

    // ─────────────────────────────────────────────────────────────────────────
    // RUNTIME STATE
    // ─────────────────────────────────────────────────────────────────────────

    private static final Queue<SlotNode>           queue           = new ConcurrentLinkedQueue<>();
    /** Keys of slots that are in the queue OR have been generated — prevents duplicates. */
    private static final Set<String>               enqueuedSlots   = Collections.synchronizedSet(new HashSet<>());
    /** Keys of slots whose paste has completed (piece is in world). */
    private static final Set<String>               generatedSlots  = Collections.synchronizedSet(new HashSet<>());
    /** Piece placed at each slot. */
    private static final Map<String, Piece>        slotPieces      = Collections.synchronizedMap(new HashMap<>());
    /** Open directions decided for each slot (edge state, not piece connections). */
    private static final Map<String, Set<Dir>>     slotOpenDirs    = Collections.synchronizedMap(new HashMap<>());
    /** Per-world worker task IDs. */
    private static final Map<String, Integer>      worldTasks      = Collections.synchronizedMap(new HashMap<>());

    /** A slot = one (chunkX, chunkZ, level) position to generate. */
    private record SlotNode(World world, int cx, int cz, int level) {}

    // ─────────────────────────────────────────────────────────────────────────
    // KEY HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private static String key(int cx, int cz, int level) {
        return level + ":" + cx + ":" + cz;
    }

    private static int floorY(int level) {
        return LEVEL_BASE_Y + level * LEVEL_HEIGHT;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called by WorldManager on every ChunkLoadEvent for redcastle worlds.
     * Seeds the queue for this chunk and ensures the worker task is running.
     */
    public static void generateCastle(World world, int chunkX, int chunkZ) {
        enqueue(world, chunkX, chunkZ, 0);
        ensureTask(world);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WORKER TASK — one per world, never cancels
    // ─────────────────────────────────────────────────────────────────────────

    private static void ensureTask(World world) {
        if (worldTasks.containsKey(world.getName())) return;
        int id = new BukkitRunnable() {
            @Override public void run() {
                for (int i = 0; i < NODES_PER_TICK; i++) {
                    SlotNode node = queue.poll();
                    if (node == null) break;
                    process(node);
                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 2L, TASK_PERIOD_TICKS).getTaskId();
        worldTasks.put(world.getName(), id);
        dbg("Worker started for world '%s' taskId=%d", world.getName(), id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ENQUEUE — add to queue IFF not already seen
    // ─────────────────────────────────────────────────────────────────────────

    private static void enqueue(World world, int cx, int cz, int level) {
        if (level < 0 || level >= MAX_LEVELS) return;
        // enqueuedSlots.add() returns false if key was already present → skip
        if (!enqueuedSlots.add(key(cx, cz, level))) return;
        queue.add(new SlotNode(world, cx, cz, level));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EDGE CONNECTIVITY — symmetric hash
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns true if the edge between (cx,cz,level) and its neighbor in dir is open. */
    private static boolean edgeOpen(World world, int cx, int cz, int level, Dir dir) {
        long seed = world.getSeed();
        return switch (dir) {
            case North, South, East, West -> hEdgeOpen(seed, cx, cz, level, dir);
            case Up   -> vEdgeOpen(seed, cx, cz, level);
            case Down -> vEdgeOpen(seed, cx, cz, level - 1);
        };
    }

    private static boolean hEdgeOpen(long seed, int cx, int cz, int level, Dir dir) {
        int nx = cx + dir.dx(), nz = cz + dir.dz();
        // Canonical order ensures (A→East) and (B→West) hash identically
        int ax = Math.min(cx,nx), az = Math.min(cz,nz);
        int bx = Math.max(cx,nx), bz = Math.max(cz,nz);
        long s = seed
                ^ (0x5DEECE66DL * ax) ^ (0x00000000BL  * az)
                ^ (0x285A1234L  * bx) ^ (0x1F3A5C7BL  * bz)
                ^ (0xCAFEBABEL  * level);
        return new Random(s).nextInt(100) < EDGE_OPEN_CHANCE;
    }

    private static boolean vEdgeOpen(long seed, int cx, int cz, int level) {
        if (level < 0 || level >= MAX_LEVELS - 1) return false;
        long s = seed ^ (0x1234567L * cx) ^ (0x7654321L * cz) ^ (0x9999999L * level);
        return new Random(s).nextInt(100) < VERTICAL_EDGE_CHANCE;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CORE PROCESSING
    // ─────────────────────────────────────────────────────────────────────────

    private static void process(SlotNode node) {
        World world = node.world;
        int   cx    = node.cx;
        int   cz    = node.cz;
        int   level = node.level;
        String k    = key(cx, cz, level);

        // Safety: generatedSlots is the final guard (enqueuedSlots prevents most dupes)
        if (generatedSlots.contains(k)) return;
        generatedSlots.add(k);

        // ── 1. Compute open edges (ground truth for this slot) ────────────────
        Set<Dir> open   = EnumSet.noneOf(Dir.class);
        Set<Dir> closed = EnumSet.noneOf(Dir.class);

        for (Dir d : Dir.values()) {
            int nl = level + d.dl();
            if (nl < 0 || nl >= MAX_LEVELS) {
                closed.add(d); continue;
            }
            if (edgeOpen(world, cx, cz, level, d)) open.add(d);
            else                                   closed.add(d);
        }

        slotOpenDirs.put(k, open);

        // ── 2. Choose piece ───────────────────────────────────────────────────
        // Stair1 when Up or Down is open (it physically bridges 2 levels).
        // Otherwise pick a random standard piece for visual variety.
        boolean needsStair = open.contains(Dir.Up) || open.contains(Dir.Down);
        Piece piece;
        if (needsStair) {
            piece = STAIR_PIECE.rotated(pickRotation(world, cx, cz, level));
        } else {
            Random rng = new Random(world.getSeed()
                    + (long) cx * 0x7A731A2BL + (long) cz * 0x3C1DE5F7L + (long) level * 0xBEEFCAFEL);
            Piece base = STANDARD_PIECES.get(rng.nextInt(STANDARD_PIECES.size()));
            piece = base.rotated(pickRotation(world, cx, cz, level));
        }

        slotPieces.put(k, piece);

        // ── 3. Paste schematic ────────────────────────────────────────────────
        int wy = floorY(level);
        WorldManager.spawnStructureStatic(world, cx, cz, wy, piece.schematicFile(), piece.rotation);

        dbg("PASTE  [%d,%d] L%d  piece=%s  open=%s  closed=%s",
                cx, cz, level, piece, open, closed);

        // ── 4. Persist to PDC ─────────────────────────────────────────────────
        Chunk chunk = world.getChunkAt(cx, cz);
        var pdc = chunk.getPersistentDataContainer();
        pdc.set(FreedomKeys.key(KEY_GENERATED), org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        pdc.set(FreedomKeys.key(KEY_PIECE_NAME), org.bukkit.persistence.PersistentDataType.STRING, piece.name);
        pdc.set(FreedomKeys.key(KEY_PIECE_ROT),  org.bukkit.persistence.PersistentDataType.INTEGER, piece.rotation);
        pdc.set(FreedomKeys.key(KEY_OPEN_DIRS),  org.bukkit.persistence.PersistentDataType.STRING,
                open.stream().map(Enum::name).collect(Collectors.joining(",")));

        // ── 5. Propagate to open neighbors ────────────────────────────────────
        // Add to enqueuedSlots BEFORE calling world.getChunkAt() so the resulting
        // ChunkLoadEvent → generateCastle → enqueue() sees them as already queued.
        for (Dir d : open) {
            int ncx = cx + d.dx(), ncz = cz + d.dz(), nl = level + d.dl();
            enqueue(world, ncx, ncz, nl);        // marks enqueuedSlots atomically
            if (d != Dir.Up && d != Dir.Down) {
                world.getChunkAt(ncx, ncz);      // force-load; event now harmless
            }
        }

        // ── 6. Schedule TWO seal passes ───────────────────────────────────────
        // Pass 1: 60 ticks (3s) — FAWE should be done by now on most servers.
        // Pass 2: 120 ticks (6s) — confirmation pass for slow/busy servers.
        // Sealing fills the interior face (Y+1..Y+12) of every CLOSED direction.
        // Because we seal AFTER FAWE finishes, our RED_NETHER_BRICKS are final.
        final Set<Dir> finalClosed = closed;
        final int fcx = cx, fcz = cz, flevel = level;

        new BukkitRunnable() {
            @Override public void run() {
                runSealPass(world, fcx, fcz, flevel, finalClosed, 1);
            }
        }.runTaskLater(Freedom.get_plugin(), SEAL_PASS1_TICKS);

        new BukkitRunnable() {
            @Override public void run() {
                runSealPass(world, fcx, fcz, flevel, finalClosed, 2);
            }
        }.runTaskLater(Freedom.get_plugin(), SEAL_PASS2_TICKS);
    }

    /** Cosmetic rotation varies piece appearance while keeping all faces open. */
    private static int pickRotation(World world, int cx, int cz, int level) {
        long s = world.getSeed() ^ ((long) cx * 0xABCDEFL) ^ ((long) cz * 0x123456L) ^ ((long) level * 0x777L);
        return new int[]{0, 90, 180, 270}[new Random(s).nextInt(4)];
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SEAL PASS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Fills the interior of every closed face with RED_NETHER_BRICKS.
     *
     * This is the ONLY source of walls in the dungeon — every schematic opens
     * all 4 horizontal faces; walls exist only where we place seal blocks.
     *
     * Face block positions (all in world block coords):
     *   North → Z = cz*16       , X sweeps cx*16 .. cx*16+15
     *   South → Z = cz*16+15    , X sweeps cx*16 .. cx*16+15
     *   East  → X = cx*16+15    , Z sweeps cz*16 .. cz*16+15
     *   West  → X = cx*16       , Z sweeps cz*16 .. cz*16+15
     *   Up    → 3×3 at centre, top of this level  (stairwell)
     *   Down  → 3×3 at centre, floor of this level (stairwell)
     *
     * Y range sealed: yBase+SEAL_Y_MIN .. yBase+SEAL_Y_MAX  (Y+1 .. Y+12)
     * Floor (Y+0) is solid schematic — never touched.
     * Ceiling (Y+13) is air — the level above's floor provides it.
     */
    private static void runSealPass(World world, int cx, int cz, int level,
                                    Set<Dir> closedDirs, int passNum) {
        int ox    = cx * 16;
        int oz    = cz * 16;
        int yBase = floorY(level);
        int yMin  = yBase + SEAL_Y_MIN;
        int yMax  = yBase + SEAL_Y_MAX;
        Material  mat = Material.RED_NETHER_BRICKS;
        int blocksSet = 0;

        for (Dir d : closedDirs) {
            switch (d) {
                case North -> {
                    for (int x = ox; x <= ox + 15; x++)
                        for (int y = yMin; y <= yMax; y++) {
                            world.getBlockAt(x, y, oz).setType(mat, false);
                            blocksSet++;
                        }
                }
                case South -> {
                    for (int x = ox; x <= ox + 15; x++)
                        for (int y = yMin; y <= yMax; y++) {
                            world.getBlockAt(x, y, oz + 15).setType(mat, false);
                            blocksSet++;
                        }
                }
                case East -> {
                    for (int z = oz; z <= oz + 15; z++)
                        for (int y = yMin; y <= yMax; y++) {
                            world.getBlockAt(ox + 15, y, z).setType(mat, false);
                            blocksSet++;
                        }
                }
                case West -> {
                    for (int z = oz; z <= oz + 15; z++)
                        for (int y = yMin; y <= yMax; y++) {
                            world.getBlockAt(ox, y, z).setType(mat, false);
                            blocksSet++;
                        }
                }
                case Up -> {
                    // Seal stairwell hole at top of this level
                    int fy = yBase + LEVEL_HEIGHT - 1;
                    for (int dx = -1; dx <= 1; dx++)
                        for (int dz = -1; dz <= 1; dz++) {
                            world.getBlockAt(ox + 8 + dx, fy, oz + 8 + dz).setType(mat, false);
                            blocksSet++;
                        }
                }
                case Down -> {
                    // Seal stairwell hole at bottom of this level
                    int fy = yBase;
                    for (int dx = -1; dx <= 1; dx++)
                        for (int dz = -1; dz <= 1; dz++) {
                            world.getBlockAt(ox + 8 + dx, fy, oz + 8 + dz).setType(mat, false);
                            blocksSet++;
                        }
                }
            }
        }

        dbg("SEAL P%d [%d,%d] L%d  closed=%s  blocks=%d", passNum, cx, cz, level, closedDirs, blocksSet);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHUNK GENERATOR — empty void world
    // ─────────────────────────────────────────────────────────────────────────

    public static class CastleGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
            return createChunkData(world);
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
        return new WorldCreator(worldName).generator(new CastleGenerator()).createWorld();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DEBUG LOGGING — writes to plugins/Freedom/redcastle_debug.log
    // ─────────────────────────────────────────────────────────────────────────

    private static PrintWriter debugLog = null;
    private static final SimpleDateFormat TS = new SimpleDateFormat("HH:mm:ss.SSS");

    private static synchronized void dbg(String fmt, Object... args) {
        if (!verbose) return;
        String msg = "[" + TS.format(new Date()) + "] " + String.format(fmt, args);
        // Also log to server console
        Freedom.get_plugin().getLogger().info(msg);
        // Write to file
        try {
            if (debugLog == null) {
                File dir = Freedom.get_plugin().getDataFolder();
                if (!dir.exists()) dir.mkdirs();
                File f = new File(dir, "redcastle_debug.log");
                debugLog = new PrintWriter(new FileWriter(f, true), true);
                debugLog.println("\n══════ Server restart ══════ " + new Date());
            }
            debugLog.println(msg);
        } catch (IOException e) {
            Freedom.get_plugin().getLogger().warning("[RedCastle] Could not write debug log: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LEGACY SHIMS
    // ─────────────────────────────────────────────────────────────────────────

    @Deprecated public static void finalizeGenerationWithConnectivityCheck(World w) {}
    @Deprecated public static void validateAllConnectivity(World w)                 {}
    @Deprecated public static void ensureAllUnusedConnectionsClosed(World w)        {}
}