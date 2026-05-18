package xyz.yaszu.freedom.Subsystems;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.generator.ChunkGenerator;
import xyz.yaszu.freedom.Util.Util;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.createChunkData;

public class RedCastleManager extends Util {

    public static List<castlePiece> redCastlePieces = List.of(
            new castlePiece(
                    "dungeon1",
                    List.of(Directions.South, Directions.North, Directions.West),
                    List.of(),
                    false
            ),
            new castlePiece(
                    "hallwayall1",
                    List.of(Directions.South, Directions.West, Directions.East, Directions.North),
                    List.of(),
                    false
            ),
            new castlePiece(
                    "Library1",
                    List.of(Directions.South, Directions.North, Directions.East, Directions.West),
                    List.of(),
                    false
            ),
            new castlePiece(
                    "NorthHallway",
                    List.of(Directions.South, Directions.North),
                    List.of(),
                    false
            ),
            new castlePiece(
                    "Stair1",
                    List.of(Directions.South, Directions.Up, Directions.Down),
                    List.of(),
                    true
            ),
            new castlePiece(
                    "hallwayEastWest1",
                    List.of(Directions.East, Directions.West),
                    List.of(),
                    false
            ),
            new castlePiece(
                    "hallwayEastWest2",
                    List.of(Directions.East, Directions.West),
                    List.of(),
                    false
            ),
            new castlePiece(
                    "Tree1",
                    List.of(Directions.East, Directions.West),
                    List.of(),
                    false
            )
    );

    public enum Directions {
        North,
        South,
        East,
        West,
        Up,
        Down
    }

    static int maxLevels = 2;
    public static boolean verbose = true;
    public static final String KEY_PIECE_ROTATION = "redcastle_rotation";
    public static final String KEY_PIECE_CONNECTIONS = "redcastle_connections";

    // PDC key used to mark a chunk as having been processed (attempted)
    private static final String KEY_GENERATED = "redcastle_generated";
    // PDC key used to mark a chunk as having a valid piece placed
    private static final String KEY_HAS_PIECE = "redcastle_has_piece";

    public static class CastleGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
            ChunkData chunk = createChunkData(world);
            return chunk;
        }

        @Override
        public boolean shouldGenerateCaves() { return false; }
        @Override
        public boolean shouldGenerateDecorations() { return false; }
        @Override
        public boolean shouldGenerateMobs() { return false; }
        @Override
        public boolean shouldGenerateStructures() { return false; }
    }

    private static final Queue<CastleNode> generationQueue = new ConcurrentLinkedQueue<>();
    private static boolean isGenerating = false;
    private static final Set<String> generatedChunks = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, castlePiece> chunkPieces = Collections.synchronizedMap(new HashMap<>());
    private static final Map<String, ChunkNodeInfo> chunkNodeMap = Collections.synchronizedMap(new HashMap<>());

    private record CastleNode(World world, int chunkX, int chunkZ, int level) {}

    private static class ChunkNodeInfo {
        public final int chunkX, chunkZ, level;
        public castlePiece piece;
        public Set<String> connections = Collections.synchronizedSet(new HashSet<>());
        public Map<Directions, String> neighbors = Collections.synchronizedMap(new HashMap<>());

        public ChunkNodeInfo(int chunkX, int chunkZ, int level) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.level = level;
        }
    }

    private static String getChunkKey(int chunkX, int chunkZ, int level) {
        return level + ":" + chunkX + ":" + chunkZ;
    }

    private static ChunkNodeInfo getOrCreateNode(int chunkX, int chunkZ, int level) {
        String key = getChunkKey(chunkX, chunkZ, level);
        return chunkNodeMap.computeIfAbsent(key, k -> new ChunkNodeInfo(chunkX, chunkZ, level));
    }

    // -------------------------------------------------------------------------
    // EDGE-BASED CONNECTIVITY (symmetric — both sides of an edge use same seed)
    // -------------------------------------------------------------------------

    /**
     * Returns true if there is an open passage on the horizontal edge between
     * chunk (cx,cz) and its neighbor in direction {@code dir} at the given level.
     *
     * The key fix: both sides of an edge compute the SAME boolean because we
     * always hash the edge endpoints in a canonical (sorted) order.
     */
    private static boolean edgeIsOpen(long worldSeed, int cx, int cz, int level, Directions dir) {
        // Derive the neighbor coordinates
        int nx = cx, nz = cz;
        switch (dir) {
            case North -> nz--;
            case South -> nz++;
            case East  -> nx++;
            case West  -> nx--;
            default    -> { return false; } // Up/Down handled separately
        }

        // Canonical ordering so (cx,cz)->East gives the same seed as (nx,nz)->West
        int ax = Math.min(cx, nx), az = Math.min(cz, nz);
        int bx = Math.max(cx, nx), bz = Math.max(cz, nz);

        long edgeSeed = worldSeed
                ^ ((long) ax * 0x5DEECE66DL)
                ^ ((long) az * 0xBL)
                ^ ((long) bx * 0x285A1234L)
                ^ ((long) bz * 0x1F3A5C7BL)
                ^ ((long) level * 0xCAFEBABEL);

        Random rand = new Random(edgeSeed);
        // ~65 % of edges open → keeps corridors well-connected without being all-open
        return rand.nextInt(100) < 65;
    }

    /**
     * Returns true if the vertical connection (stair) at (cx,cz,level)->Up exists.
     * The Down direction at (cx,cz,level+1) returns the same value.
     */
    private static boolean verticalEdgeIsOpen(long worldSeed, int cx, int cz, int level) {
        if (level < 0 || level >= maxLevels - 1) return false;
        Random r = new Random(worldSeed ^ ((long) cx * 0x1234567L) ^ ((long) cz * 0x7654321L) ^ ((long) level * 0x999L));
        return r.nextInt(8) == 0; // ~12 % → sparse stairs
    }

    /**
     * Returns whether the edge between this chunk and its neighbor in {@code dir}
     * is open.  Replaces the old asymmetric doesNeighborWantToConnect.
     */
    private static boolean connectionOpen(World world, int cx, int cz, int level, Directions dir) {
        long seed = world.getSeed();
        return switch (dir) {
            case North, South, East, West -> edgeIsOpen(seed, cx, cz, level, dir);
            case Up   -> verticalEdgeIsOpen(seed, cx, cz, level);
            case Down -> verticalEdgeIsOpen(seed, cx, cz, level - 1);
        };
    }

    // -------------------------------------------------------------------------
    // GENERATION ENTRY POINT
    // -------------------------------------------------------------------------

    public static void generateCastle(World world, int chunkX, int chunkZ) {
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        if (chunk.getPersistentDataContainer().has(
                xyz.yaszu.freedom.Util.FreedomKeys.key(KEY_GENERATED),
                org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
            return;
        }
        addToQueue(world, chunkX, chunkZ, 0);
        startGenerationTask();
    }

    private static void addToQueue(World world, int x, int z, int level) {
        if (level < 0 || level >= maxLevels) return;

        // Do NOT skip if the chunk PDC already has KEY_GENERATED — that is now only
        // written AFTER a piece is successfully placed so this check is safe.
        String key = getChunkKey(x, z, level);
        if (generatedChunks.contains(key)) return;

        for (CastleNode node : generationQueue) {
            if (node.chunkX == x && node.chunkZ == z && node.level == level) return;
        }

        generationQueue.add(new CastleNode(world, x, z, level));
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
                    // Run a final connectivity pass after everything is placed
                    World lastWorld = getLastWorld();
                    if (lastWorld != null) {
                        finalizeGenerationWithConnectivityCheck(lastWorld);
                    }
                    return;
                }

                CastleNode node = generationQueue.poll();
                if (node == null) return;
                processNode(node);
            }
        }.runTaskTimer(Freedom.get_plugin(), 2, 10);
    }

    /** Utility: retrieve any world reference from the node map for finalization. */
    private static World lastWorldRef = null;
    private static World getLastWorld() { return lastWorldRef; }

    // -------------------------------------------------------------------------
    // CORE NODE PROCESSING
    // -------------------------------------------------------------------------

    private static void processNode(CastleNode node) {
        World world = node.world;
        lastWorldRef = world;
        int chunkX = node.chunkX;
        int chunkZ = node.chunkZ;
        int level  = node.level;
        String chunkKey = getChunkKey(chunkX, chunkZ, level);

        // Prevent double-processing
        if (generatedChunks.contains(chunkKey)) return;

        // Compute which directions this chunk should open based on the symmetric edge function
        List<Directions> requiredConnections = new ArrayList<>();
        List<Directions> forbiddenConnections = new ArrayList<>();

        for (Directions dir : Directions.values()) {
            if (connectionOpen(world, chunkX, chunkZ, level, dir)) {
                // Only open toward a valid neighbor
                int nl = level;
                if (dir == Directions.Up)   nl++;
                if (dir == Directions.Down) nl--;
                if (nl >= 0 && nl < maxLevels) {
                    requiredConnections.add(dir);
                }
            } else {
                forbiddenConnections.add(dir);
            }
        }

        if (verbose) {
            Freedom.get_plugin().getLogger().info(String.format(
                    "[RedCastle] Processing [%d, %d] L%d | Required=%s | Forbidden=%s",
                    chunkX, chunkZ, level, requiredConnections, forbiddenConnections));
        }

        // Select a piece
        castlePiece piece = selectBestPiece(world, chunkX, chunkZ, level, requiredConnections, forbiddenConnections);

        // Mark chunk as attempted regardless so we never re-queue it
        generatedChunks.add(chunkKey);

        if (piece == null) {
            if (verbose) {
                Freedom.get_plugin().getLogger().warning(String.format(
                        "[RedCastle] No piece found for [%d, %d] L%d — branch stops.", chunkX, chunkZ, level));
            }
            return;
        }

        // Store metadata
        ChunkNodeInfo nodeInfo = getOrCreateNode(chunkX, chunkZ, level);
        nodeInfo.piece = piece;
        chunkPieces.put(chunkKey, piece);

        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        chunk.getPersistentDataContainer().set(
                xyz.yaszu.freedom.Util.FreedomKeys.key(KEY_GENERATED),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        chunk.getPersistentDataContainer().set(
                xyz.yaszu.freedom.Util.FreedomKeys.key(KEY_HAS_PIECE),
                org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        chunk.getPersistentDataContainer().set(
                xyz.yaszu.freedom.Util.FreedomKeys.key(KEY_PIECE_ROTATION),
                org.bukkit.persistence.PersistentDataType.INTEGER, piece.rotation);
        String connectionsStr = piece.connections.stream().map(Enum::name).collect(Collectors.joining(","));
        chunk.getPersistentDataContainer().set(
                xyz.yaszu.freedom.Util.FreedomKeys.key(KEY_PIECE_CONNECTIONS),
                org.bukkit.persistence.PersistentDataType.STRING, connectionsStr);

        int y = 64 + (level * 15);

        if (verbose) {
            Freedom.get_plugin().getLogger().info(String.format(
                    "[RedCastle] Spawning %s at [%d, %d] L%d (Y=%d) Rot=%d Connections=%s",
                    piece.schematicName, chunkX, chunkZ, level, y, piece.rotation, piece.connections));
        }

        WorldManager.spawnStructureStatic(world, chunkX, chunkZ, y, "newredCastle_" + piece.schematicName + ".schem", piece.rotation);

        // Enqueue neighbors for every connection the piece actually has
        for (Directions dir : piece.connections) {
            int nx = chunkX, nz = chunkZ, nl = level;
            switch (dir) {
                case North -> nz--;
                case South -> nz++;
                case East  -> nx++;
                case West  -> nx--;
                case Up    -> nl++;
                case Down  -> nl--;
            }
            nodeInfo.neighbors.put(dir, getChunkKey(nx, nz, nl));
            nodeInfo.connections.add(dir.name());
            addToQueue(world, nx, nz, nl);
        }
    }

    // -------------------------------------------------------------------------
    // PIECE SELECTION
    // -------------------------------------------------------------------------

    /**
     * Selects the best matching castlePiece (with rotation) for the given constraints.
     *
     * Priority:
     *  1. Exact match: has ALL required, has NONE of forbidden
     *  2. Superset match: has ALL required, may have some forbidden (extra openings
     *     will be sealed in the finalization pass)
     *  3. Subset match: has SOME required (last resort — means a corridor dead-ends here)
     */
    private static castlePiece selectBestPiece(World world, int chunkX, int chunkZ, int level,
                                               List<Directions> required, List<Directions> forbidden) {

        Random random = new Random(world.getSeed()
                + (long) chunkX * 31213L
                + (long) chunkZ * 43241L
                + (long) level  * 777L);

        List<castlePiece> exactMatches    = new ArrayList<>();
        List<castlePiece> supersetMatches = new ArrayList<>();
        List<castlePiece> subsetMatches   = new ArrayList<>();

        for (castlePiece base : redCastlePieces) {
            for (int rot : List.of(0, 90, 180, 270)) {
                castlePiece rotated = base.rotated(rot);

                boolean hasAllRequired = required.stream().allMatch(r -> rotated.connections.contains(r));
                boolean hasAnyForbidden = forbidden.stream().anyMatch(f -> rotated.connections.contains(f));
                boolean hasAnyRequired = required.isEmpty() || required.stream().anyMatch(r -> rotated.connections.contains(r));

                if (hasAllRequired && !hasAnyForbidden) {
                    exactMatches.add(rotated);
                } else if (hasAllRequired) {
                    supersetMatches.add(rotated); // has extra connections toward forbidden sides
                } else if (hasAnyRequired) {
                    subsetMatches.add(rotated);   // covers only some required
                }
            }
        }

        if (!exactMatches.isEmpty()) {
            // Among exact matches prefer those with the fewest connections (tidier corridors)
            int minConn = exactMatches.stream().mapToInt(p -> p.connections.size()).min().orElse(0);
            List<castlePiece> tight = exactMatches.stream()
                    .filter(p -> p.connections.size() == minConn).collect(Collectors.toList());
            return tight.get(random.nextInt(tight.size()));
        }
        if (!supersetMatches.isEmpty()) {
            return supersetMatches.get(random.nextInt(supersetMatches.size()));
        }
        if (!subsetMatches.isEmpty()) {
            return subsetMatches.get(random.nextInt(subsetMatches.size()));
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // DOOR / WALL PLACEMENT  (fully rewritten — no double-offset bugs)
    // -------------------------------------------------------------------------

    /**
     * Seals the face of chunk (chunkX, chunkZ) in the given direction with a wall
     * of RED_NETHER_BRICKS.  All coordinates are computed from scratch here —
     * no pre-multiplied values are passed in.
     *
     * Face centres (relative to chunk origin in world coords):
     *   North  → x+8,  z+0
     *   South  → x+8,  z+15
     *   East   → x+15, z+8
     *   West   → x+0,  z+8
     *   Up     → x+8,  y+14, z+8  (ceiling)
     *   Down   → x+8,  y+0,  z+8  (floor)
     */
    private static void sealFace(World world, int chunkX, int chunkZ, int level, Directions dir) {
        int originX = chunkX * 16;
        int originZ = chunkZ * 16;
        int yBase   = 64 + (level * 15);
        Material mat = Material.RED_NETHER_BRICKS;

        if (dir == Directions.Up || dir == Directions.Down) {
            int y = (dir == Directions.Up) ? (yBase + 14) : yBase;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    setBlockSafe(world, originX + 8 + dx, y, originZ + 8 + dz, mat);
                }
            }
            return;
        }

        // Horizontal face: determine the fixed axis coordinate and the "spread" axis
        int faceX, faceZ;
        boolean spreadAlongX;

        switch (dir) {
            case North -> { faceX = originX + 8; faceZ = originZ;      spreadAlongX = true;  }
            case South -> { faceX = originX + 8; faceZ = originZ + 15; spreadAlongX = true;  }
            case East  -> { faceX = originX + 15; faceZ = originZ + 8; spreadAlongX = false; }
            case West  -> { faceX = originX;      faceZ = originZ + 8; spreadAlongX = false; }
            default    -> { return; }
        }

        // 3 blocks wide, 3 blocks tall (Y+1 to Y+3) to cover doorway openings
        for (int dy = 1; dy <= 3; dy++) {
            for (int spread = -1; spread <= 1; spread++) {
                int wx = spreadAlongX ? (faceX + spread) : faceX;
                int wz = spreadAlongX ? faceZ : (faceZ + spread);
                setBlockSafe(world, wx, yBase + dy, wz, mat);
            }
        }
    }

    /** Safely sets a block and optionally logs failures. */
    private static void setBlockSafe(World world, int x, int y, int z, Material mat) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(mat);
        if (verbose && block.getType() != mat) {
            Freedom.get_plugin().getLogger().warning(String.format(
                    "[RedCastle] Failed to set block %s at %d,%d,%d (got %s)",
                    mat, x, y, z, block.getType()));
        }
    }

    // -------------------------------------------------------------------------
    // POST-GENERATION VALIDATION & FINALIZATION
    // -------------------------------------------------------------------------

    /**
     * After all chunks have been generated:
     *  1. For every connection that points to a chunk that was never generated,
     *     seal the opening.
     *  2. For every direction a piece does NOT open toward, ensure the face is sealed
     *     (catches anything the schematic leaves open by default).
     *  3. Where two neighboring pieces both have an open connection toward each other,
     *     leave the passage open (no sealing needed there).
     */
    public static void finalizeGenerationWithConnectivityCheck(World world) {
        if (verbose) {
            Freedom.get_plugin().getLogger().info("[RedCastle] ===== FINALIZATION PASS =====");
        }

        int sealed = 0;

        for (String chunkKey : new HashSet<>(generatedChunks)) {
            castlePiece piece = chunkPieces.get(chunkKey);
            if (piece == null) continue;

            String[] parts = chunkKey.split(":");
            if (parts.length != 3) continue;
            int level  = Integer.parseInt(parts[0]);
            int chunkX = Integer.parseInt(parts[1]);
            int chunkZ = Integer.parseInt(parts[2]);

            for (Directions dir : Directions.values()) {
                boolean pieceOpens = piece.connections.contains(dir);

                int nx = chunkX, nz = chunkZ, nl = level;
                switch (dir) {
                    case North -> nz--;
                    case South -> nz++;
                    case East  -> nx++;
                    case West  -> nx--;
                    case Up    -> nl++;
                    case Down  -> nl--;
                }

                String neighborKey = getChunkKey(nx, nz, nl);
                boolean neighborExists = generatedChunks.contains(neighborKey);
                castlePiece neighborPiece = neighborExists ? chunkPieces.get(neighborKey) : null;
                boolean neighborOpens = (neighborPiece != null) && neighborPiece.connections.contains(getOpposite(dir));

                if (pieceOpens && (!neighborExists || !neighborOpens)) {
                    // Seal: we open toward a missing or mis-matched neighbor
                    if (verbose) {
                        Freedom.get_plugin().getLogger().warning(String.format(
                                "[RedCastle] Sealing orphaned opening %s at [%d,%d] L%d (neighbor=%s, neighborOpens=%b)",
                                dir, chunkX, chunkZ, level, neighborExists ? neighborKey : "none", neighborOpens));
                    }
                    sealFace(world, chunkX, chunkZ, level, dir);
                    sealed++;
                } else if (!pieceOpens) {
                    // Seal any faces the piece does NOT open (ensures no schematic-leaked gaps)
                    sealFace(world, chunkX, chunkZ, level, dir);
                    sealed++;
                }
                // If pieceOpens && neighborOpens → leave open (valid corridor)
            }
        }

        if (verbose) {
            Freedom.get_plugin().getLogger().info(String.format(
                    "[RedCastle] Finalization done. Chunks=%d, Faces sealed=%d",
                    generatedChunks.size(), sealed));
        }
    }

    // -------------------------------------------------------------------------
    // WORLD CREATION
    // -------------------------------------------------------------------------

    public static World createRedCastleWorld(String worldName) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new CastleGenerator());
        return creator.createWorld();
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private static Directions getOpposite(Directions dir) {
        return switch (dir) {
            case North -> Directions.South;
            case South -> Directions.North;
            case East  -> Directions.West;
            case West  -> Directions.East;
            case Up    -> Directions.Down;
            case Down  -> Directions.Up;
        };
    }

    private static boolean matches(castlePiece piece, List<Directions> required, List<Directions> forbidden) {
        for (Directions req : required) {
            if (!piece.connections.contains(req)) return false;
        }
        for (Directions forb : forbidden) {
            if (piece.connections.contains(forb)) return false;
        }
        return true;
    }

    private static boolean matchesRequired(castlePiece piece, List<Directions> required) {
        for (Directions req : required) {
            if (!piece.connections.contains(req)) return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // LEGACY / UNUSED — kept for API compatibility but no longer called internally
    // -------------------------------------------------------------------------

    /** @deprecated Use {@link #connectionOpen} instead. */
    @Deprecated
    private static boolean doesNeighborWantToConnect(World world, int nx, int nz, int level, Directions dir) {
        return connectionOpen(world, nx, nz, level, dir);
    }

    /** @deprecated Use {@link #sealFace} instead. */
    @Deprecated
    private static void placeDoorAtConnection(World world, int cx, int cz, int yBase, Directions dir, int rotation) {
        sealFace(world, cx, cz, (yBase - 64) / 15, dir);
    }

    /** @deprecated No longer used — sealing handled in finalizeGenerationWithConnectivityCheck. */
    @Deprecated
    public static void validateAllConnectivity(World world) {
        finalizeGenerationWithConnectivityCheck(world);
    }

    /** @deprecated No longer used — sealing handled in finalizeGenerationWithConnectivityCheck. */
    @Deprecated
    public static void ensureAllUnusedConnectionsClosed(World world) {
        // Subsumed by finalizeGenerationWithConnectivityCheck
    }

    private static boolean isValidChunkLocation(World world, int chunkX, int chunkZ, int level) {
        return level >= 0 && level < maxLevels;
    }

    private static boolean isChunkActive(World world, int cx, int cz, int level) {
        return level == 0 || Math.abs(cx) + Math.abs(cz) < 100;
    }

    // Context helper — kept for reference but selectBestPiece no longer needs it
    private static Map<String, ChunkNodeInfo> getNearby5ChunksContext(int chunkX, int chunkZ, int level) {
        Map<String, ChunkNodeInfo> context = new HashMap<>();
        int[][] offsets = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] offset : offsets) {
            String key = getChunkKey(chunkX + offset[0], chunkZ + offset[1], level);
            ChunkNodeInfo node = chunkNodeMap.get(key);
            if (node != null) context.put(key, node);
        }
        return context;
    }
}