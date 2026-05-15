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
                    List.of(Directions.South, Directions.North,Directions.West),
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
                    List.of(Directions.South, Directions.Up,Directions.Down),
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
                    List.of(Directions.East,Directions.West),
                    List.of(),
                    false
            ),
            new castlePiece(
                    "Tree1",
                    List.of(Directions.East,Directions.West),
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

    public static class CastleGenerator extends ChunkGenerator {
        private final long seedOffset = 98765L;

        @Override
        public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
            ChunkData chunk = createChunkData(world);
            // The actual generation of structures (schematics) happens on chunk load via WorldManager
            // This generator just provides the empty void world for it.
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

    /**
     * Stores information about a generated chunk node including connections and adjacencies.
     */
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

    /**
     * Gets the context of nearest 5 chunks around the given position at the same level.
     * Returns information about all nearby chunks for contextual decision-making.
     */
    private static Map<String, ChunkNodeInfo> getNearby5ChunksContext(int chunkX, int chunkZ, int level) {
        Map<String, ChunkNodeInfo> context = new HashMap<>();

        // 5-chunk radius: center + 4 cardinal neighbors
        int[][] offsets = {
            {0, 0},      // Center
            {1, 0},      // East
            {-1, 0},     // West
            {0, 1},      // South
            {0, -1}      // North
        };

        for (int[] offset : offsets) {
            int nx = chunkX + offset[0];
            int nz = chunkZ + offset[1];
            String key = getChunkKey(nx, nz, level);
            ChunkNodeInfo node = chunkNodeMap.get(key);
            if (node != null) {
                context.put(key, node);
            }
        }

        return context;
    }

    public static void generateCastle(World world, int chunkX, int chunkZ) {
        // Only start generation from a "root" or if explicitly triggered.
        // For now, let's say chunk 0,0 level 0 is a root.
        // Or better: any chunk that load and is NOT generated yet can be a seed if it's the first one.
        
        if (isGenerating) return;

        // If nothing is generating, and this chunk isn't generated, start the process
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        if (!chunk.getPersistentDataContainer().has(xyz.yaszu.freedom.Util.FreedomKeys.key("redcastle_generated"), org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
            addToQueue(world, chunkX, chunkZ, 0);
            startGenerationTask();
        }
    }

    private static void addToQueue(World world, int x, int z, int level) {
        if (level < 0 || level >= maxLevels) return;
        
        // Check if already generated or already in queue (simplified check with PDC)
        Chunk chunk = world.getChunkAt(x, z);
        if (chunk.getPersistentDataContainer().has(xyz.yaszu.freedom.Util.FreedomKeys.key("redcastle_generated"), org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
            return;
        }

        // Avoid duplicates in queue
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
                    return;
                }

                CastleNode node = generationQueue.poll();
                if (node == null) return;

                processNode(node);
            }
        }.runTaskTimer(Freedom.get_plugin(), 2, 10); // Run every 10 ticks (0.5s) to be safe and sequential
    }

    private static void processNode(CastleNode node) {
        World world = node.world;
        int chunkX = node.chunkX;
        int chunkZ = node.chunkZ;
        int level = node.level;
        String chunkKey = getChunkKey(chunkX, chunkZ, level);

        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        if (chunk.getPersistentDataContainer().has(xyz.yaszu.freedom.Util.FreedomKeys.key("redcastle_generated"), org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
            return;
        }
        chunk.getPersistentDataContainer().set(xyz.yaszu.freedom.Util.FreedomKeys.key("redcastle_generated"), org.bukkit.persistence.PersistentDataType.BOOLEAN, true);

        // Get contextual information from nearby 5 chunks
        Map<String, ChunkNodeInfo> context = getNearby5ChunksContext(chunkX, chunkZ, level);

        castlePiece piece = selectPieceForChunkWithContext(world, chunkX, chunkZ, level, context);
        if (piece != null) {
            // Create and store node information
            ChunkNodeInfo nodeInfo = getOrCreateNode(chunkX, chunkZ, level);
            nodeInfo.piece = piece;
            chunkPieces.put(chunkKey, piece);
            generatedChunks.add(chunkKey);

            // Store rotation and connections in PDC for later post-processing
            chunk.getPersistentDataContainer().set(xyz.yaszu.freedom.Util.FreedomKeys.key(KEY_PIECE_ROTATION), org.bukkit.persistence.PersistentDataType.INTEGER, piece.rotation);
            String connectionsStr = piece.connections.stream().map(Enum::name).collect(Collectors.joining(","));
            chunk.getPersistentDataContainer().set(xyz.yaszu.freedom.Util.FreedomKeys.key(KEY_PIECE_CONNECTIONS), org.bukkit.persistence.PersistentDataType.STRING, connectionsStr);

            int y = 64 + (level * 15);
            if (verbose) {
                Freedom.get_plugin().getLogger().info(String.format("[RedCastle] Spawning %s at [%d, %d] Level %d (Y=%d) Rot=%d Connections=%s",
                        piece.schematicName, chunkX, chunkZ, level, y, piece.rotation, piece.connections));
            }
            WorldManager.spawnStructureStatic(world, chunkX, chunkZ, y, "newredCastle_" + piece.schematicName + ".schem", piece.rotation);

            // Register connections and add neighbors to queue
            for (Directions dir : piece.connections) {
                nodeInfo.connections.add(dir.name());
                int nx = chunkX, nz = chunkZ, nl = level;
                switch (dir) {
                    case North -> {
                        nz--;
                        nodeInfo.neighbors.put(Directions.North, getChunkKey(nx, nz, nl));
                    }
                    case South -> {
                        nz++;
                        nodeInfo.neighbors.put(Directions.South, getChunkKey(nx, nz, nl));
                    }
                    case East -> {
                        nx++;
                        nodeInfo.neighbors.put(Directions.East, getChunkKey(nx, nz, nl));
                    }
                    case West -> {
                        nx--;
                        nodeInfo.neighbors.put(Directions.West, getChunkKey(nx, nz, nl));
                    }
                    case Up -> {
                        nl++;
                        nodeInfo.neighbors.put(Directions.Up, getChunkKey(nx, nz, nl));
                    }
                    case Down -> {
                        nl--;
                        nodeInfo.neighbors.put(Directions.Down, getChunkKey(nx, nz, nl));
                    }
                }
                addToQueue(world, nx, nz, nl);
            }
            
            // Place all doors with validation
            placeAllDoorsForChunk(world, chunkX, chunkZ, level, piece, nodeInfo);
        } else {
            if (verbose) {
                Freedom.get_plugin().getLogger().info(String.format("[RedCastle] No piece for [%d, %d] Level %d, stopping this branch.", chunkX, chunkZ, level));
            }
        }
    }

    /**
     * Places doors for all unused directions with validation.
     * Ensures doors are actually placed in the world.
     */
    private static void placeAllDoorsForChunk(World world, int chunkX, int chunkZ, int level, castlePiece piece, ChunkNodeInfo nodeInfo) {
        int yBase = 64 + (level * 15);
        int x = chunkX * 16;
        int z = chunkZ * 16;

        // Check all six directions
        for (Directions dir : Directions.values()) {
            // Skip if this piece has this connection
            if (piece.connections.contains(dir)) continue;

            // Place door for unused direction
            validateAndPlaceDoor(world, x, z, yBase, dir, piece.rotation);
        }
    }

    /**
     * Places a door at a connection point with validation that blocks are actually set.
     */
    private static void validateAndPlaceDoor(World world, int x, int z, int yBase, Directions dir, int rotation) {
        // Determine position based on direction
        int lx = 8, lz = 8;
        int height = 3;

        switch (dir) {
            case North -> lz = 0;
            case South -> lz = 15;
            case East -> lx = 15;
            case West -> lx = 0;
            case Up -> {
                // Vertical up - place at ceiling
                int y = yBase + 15;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Block block = world.getBlockAt(x + 8 + dx, y, z + 8 + dz);
                        block.setType(Material.RED_NETHER_BRICKS);
                    }
                }
                return;
            }
            case Down -> {
                // Vertical down - place at floor
                int y = yBase;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Block block = world.getBlockAt(x + 8 + dx, y, z + 8 + dz);
                        block.setType(Material.RED_NETHER_BRICKS);
                    }
                }
                return;
            }
        }

        // Horizontal door placement with validation
        Material wallMat = Material.RED_NETHER_BRICKS;
        for (int dy = 0; dy < height; dy++) {
            for (int doff = -1; doff <= 1; doff++) {
                int wx = x + lx;
                int wz = z + lz;

                if (dir == Directions.North || dir == Directions.South) {
                    wx += doff;
                } else {
                    wz += doff;
                }

                Block block = world.getBlockAt(wx, yBase + 1 + dy, wz);
                block.setType(wallMat);

                // Verify the block was set
                if (block.getType() != wallMat && verbose) {
                    Freedom.get_plugin().getLogger().warning(
                        String.format("[RedCastle] Failed to place door block at %d,%d,%d for %s",
                            wx, yBase + 1 + dy, wz, dir)
                    );
                }
            }
        }
    }

    /**
     * Places a door/wall at a connection point to seal off unused pathways.
     * Creates a 3x3 wall of RED_NETHER_BRICKS at the specified direction.
     */
    private static void placeDoorAtConnection(World world, int cx, int cz, int yBase, Directions dir, int rotation) {
        int x = cx * 16;
        int z = cz * 16;

        // Horizontal centers of faces relative to chunk origin
        int lx = 8, lz = 8;
        switch (dir) {
            case North -> lz = 0;
            case South -> lz = 15;
            case East -> lx = 15;
            case West -> lx = 0;
            case Up, Down -> {
                // For vertical connections, place a 3x3 barrier at the appropriate Y level
                int y = yBase + (dir == Directions.Up ? 15 : 0);
                Material wallMat = Material.RED_NETHER_BRICKS;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        world.getBlockAt(x + 8 + dx, y, z + 8 + dz).setType(wallMat);
                    }
                }
                return;
            }
        }

        // Place a 3x3 wall of RED_NETHER_BRICKS at the connection face
        Material wallMat = Material.RED_NETHER_BRICKS;
        for (int dy = 0; dy <= 2; dy++) {
            for (int doff = -1; doff <= 1; doff++) {
                int wx = x + lx;
                int wz = z + lz;
                if (dir == Directions.North || dir == Directions.South) wx += doff;
                else wz += doff;

                world.getBlockAt(wx, yBase + 1 + dy, wz).setType(wallMat);
            }
        }
    }

    /**
     * Checks if a chunk location is valid for the castle structure.
     */
    private static boolean isValidChunkLocation(World world, int chunkX, int chunkZ, int level) {
        // Level bounds check
        return level >= 0 && level < maxLevels;
    }

    private static boolean doesNeighborWantToConnect(World world, int cx, int cz, int level, Directions dir) {
        long worldSeed = world.getSeed();
        
        // Handle vertical connectivity first
        if (dir == Directions.Up) return isStair(worldSeed, cx, cz, level);
        if (dir == Directions.Down) {
            int nx = cx;
            int nz = cz;
            int nl = level - 1;
            if (nl < 0) return false;
            return isStair(worldSeed, nx, nz, nl);
        }

        // Horizontal connectivity using MazeGenerator-style deterministic logic
        // We use a modified Binary Tree / Randomized Prim-like approach for infinite grid
        
        int nx = cx;
        int nz = cz;
        switch (dir) {
            case North -> nz--;
            case South -> nz++;
            case East -> nx++;
            case West -> nx--;
        }

        // Use a stable seed for the connection between (cx, cz) and (nx, nz)
        // Ensure (x1,z1) is always the "smaller" coordinate for consistency
        int x1 = Math.min(cx, nx);
        int z1 = Math.min(cz, nz);
        int x2 = Math.max(cx, nx);
        int z2 = Math.max(cz, nz);
        
        long connectionSeed = worldSeed ^ ((long) x1 * 312151189L) ^ ((long) z1 * 850327465L) 
                                      ^ ((long) x2 * 668265261L) ^ ((long) z2 * 511891231L) ^ (level * 1234567L);
        Random rand = new Random(connectionSeed);
        
        // Every chunk has at least one connection to either East or South (Binary Tree algorithm)
        // This guarantees global connectivity from any point to any other point in an infinite grid.
        
        if (dir == Directions.East) {
            // Check if (cx, cz) wants to connect East
            long cellSeed = worldSeed ^ ((long) cx * 312151189L) ^ ((long) cz * 850327465L) ^ (level * 777L);
            Random cellRand = new Random(cellSeed);
            double choice = cellRand.nextDouble();
            return choice < 0.55 || choice > 0.9; // 55% chance + 10% bonus for loops
        }
        
        if (dir == Directions.West) {
            // Check if (nx, nz) (neighbor to the west) wants to connect East
            long cellSeed = worldSeed ^ ((long) nx * 312151189L) ^ ((long) nz * 850327465L) ^ (level * 777L);
            Random cellRand = new Random(cellSeed);
            double choice = cellRand.nextDouble();
            return choice < 0.55 || choice > 0.9;
        }
        
        if (dir == Directions.South) {
            // Check if (cx, cz) wants to connect South
            long cellSeed = worldSeed ^ ((long) cx * 312151189L) ^ ((long) cz * 850327465L) ^ (level * 777L);
            Random cellRand = new Random(cellSeed);
            double choice = cellRand.nextDouble();
            return choice >= 0.55; // Complement of East choice
        }
        
        if (dir == Directions.North) {
            // Check if (nx, nz) (neighbor to the north) wants to connect South
            long cellSeed = worldSeed ^ ((long) nx * 312151189L) ^ ((long) nz * 850327465L) ^ (level * 777L);
            Random cellRand = new Random(cellSeed);
            double choice = cellRand.nextDouble();
            return choice >= 0.55;
        }

        return false;
    }

    private static boolean isStair(long seed, int x, int z, int level) {
        if (level < 0 || level >= maxLevels - 1) return false;
        // Deterministic stair placement (approx 1 in 8 chunks for more verticality in 2 levels)
        Random r = new Random(seed + (long) x * 1234567L + (long) z * 7654321L + level * 999L);
        return r.nextInt(8) == 0;
    }

    private static int[] findNearestRoot(long seed, int x, int z, int level) {
        if (level <= 0) return new int[]{0, 0};
        
        // Search for a stair at level-1 in a spiral
        for (int r = 0; r <= 32; r++) { // Expanded search for Binary Tree maze
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    if (isStair(seed, x + dx, z + dz, level - 1)) {
                        return new int[]{x + dx, z + dz};
                    }
                }
            }
        }
        // Fallback to (0,0) if no stair found nearby (rare with r=32)
        return new int[]{0, 0};
    }

    private static boolean isChunkActive(World world, int cx, int cz, int level) {
        if (level == 0) return true;
        
        long seed = world.getSeed();
        int[] root = findNearestRoot(seed, cx, cz, level);
        int rx = root[0];
        int rz = root[1];
        
        // This is a simplified "active" check: 
        // A chunk is active if it's within a reasonable distance of its vertical root
        // or if it's part of the connected path from that root.
        // For the Binary Tree maze, we just check if it's reachable.
        // Since Binary Tree is globally connected, we just need to bound the "castle" size if desired.
        // For now, let's say chunks within 64 chunks of origin or their vertical root are active.
        
        int distToRoot = Math.abs(cx - rx) + Math.abs(cz - rz);
        return distToRoot < 50; // Arbitrary radius for each "castle" cluster on upper levels
    }


    private static long getEdgeHash(long seed, int x1, int y1, int l1, int x2, int y2, int l2) {
        // Ensure order for consistency
        if (x1 < x2 || (x1 == x2 && y1 < y2) || (x1 == x2 && y1 == y2 && l1 < l2)) {
            return hash(seed, x1, y1, x2, y2, l1) + l2 * 7L;
        } else {
            return hash(seed, x2, y2, x1, y1, l2) + l1 * 7L;
        }
    }

    private static long hash(long seed, int x1, int y1, int x2, int y2, int level) {
        long h = seed + level * 31L;
        h ^= (long) x1 * 31213L;
        h ^= (long) y1 * 43241L;
        h ^= (long) x2 * 123457L;
        h ^= (long) y2 * 9876543L;
        return h;
    }

    private static Directions getOpposite(Directions dir) {
        return switch (dir) {
            case North -> Directions.South;
            case South -> Directions.North;
            case East -> Directions.West;
            case West -> Directions.East;
            case Up -> Directions.Down;
            case Down -> Directions.Up;
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

    public static World createRedCastleWorld(String worldName) {
        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new CastleGenerator());
        return creator.createWorld();
    }

    /**
     * Validates that all generated structures are properly connected.
     * Checks that every opened connection has a corresponding neighbor.
     * Closes any doors that lead to non-existent neighbors.
     */
    public static void validateAllConnectivity(World world) {
        if (verbose) {
            Freedom.get_plugin().getLogger().info("[RedCastle] Starting full connectivity validation...");
        }

        Set<String> processedChunks = new HashSet<>();
        int doorsPlaced = 0;

        for (String chunkKey : generatedChunks) {
            if (processedChunks.contains(chunkKey)) continue;
            processedChunks.add(chunkKey);

            // Parse chunk key
            String[] parts = chunkKey.split(":");
            if (parts.length != 3) continue;

            int level = Integer.parseInt(parts[0]);
            int chunkX = Integer.parseInt(parts[1]);
            int chunkZ = Integer.parseInt(parts[2]);

            castlePiece piece = chunkPieces.get(chunkKey);
            if (piece == null) continue;

            int yBase = 64 + (level * 15);

            // Check each connection in this piece
            for (Directions dir : piece.connections) {
                int nx = chunkX, nz = chunkZ, nl = level;
                switch (dir) {
                    case North -> nz--;
                    case South -> nz++;
                    case East -> nx++;
                    case West -> nx--;
                    case Up -> nl++;
                    case Down -> nl--;
                }

                String neighborKey = getChunkKey(nx, nz, nl);

                // If neighbor was supposed to generate but didn't, close the door
                if (!generatedChunks.contains(neighborKey)) {
                    if (verbose) {
                        Freedom.get_plugin().getLogger().warning(
                            String.format("[RedCastle] Orphaned connection %s at [%d, %d] L%d -> no neighbor at [%d, %d] L%d. Sealing door.",
                                dir, chunkX, chunkZ, level, nx, nz, nl)
                        );
                    }
                    placeDoorAtConnection(world, chunkX, chunkZ, yBase, dir, piece.rotation);
                    doorsPlaced++;
                } else {
                    // Neighbor exists, verify it also has a matching connection
                    castlePiece neighborPiece = chunkPieces.get(neighborKey);
                    if (neighborPiece != null) {
                        Directions oppositeDir = getOpposite(dir);
                        if (!neighborPiece.connections.contains(oppositeDir)) {
                            if (verbose) {
                                Freedom.get_plugin().getLogger().warning(
                                    String.format("[RedCastle] Mismatched connection: [%d, %d] L%d opens %s but [%d, %d] L%d doesn't open %s. Sealing door.",
                                        chunkX, chunkZ, level, dir, nx, nz, nl, oppositeDir)
                                );
                            }
                            placeDoorAtConnection(world, chunkX, chunkZ, yBase, dir, piece.rotation);
                            doorsPlaced++;
                        } else if (verbose) {
                            Freedom.get_plugin().getLogger().info(
                                String.format("[RedCastle] ✓ Connected: [%d, %d] L%d <%s> [%d, %d] L%d",
                                    chunkX, chunkZ, level, dir, nx, nz, nl)
                            );
                        }
                    }
                }
            }
        }

        if (verbose) {
            Freedom.get_plugin().getLogger().info(String.format("[RedCastle] Validation complete. Doors sealed: %d", doorsPlaced));
        }
    }

    /**
     * Ensures that all directions on a piece that are NOT in its connections list have sealed doors.
     * This prevents unforeseen openings in structures.
     */
    public static void ensureAllUnusedConnectionsClosed(World world) {
        if (verbose) {
            Freedom.get_plugin().getLogger().info("[RedCastle] Ensuring all unused connections are sealed...");
        }

        int doorsPlaced = 0;

        for (String chunkKey : generatedChunks) {
            String[] parts = chunkKey.split(":");
            if (parts.length != 3) continue;

            int level = Integer.parseInt(parts[0]);
            int chunkX = Integer.parseInt(parts[1]);
            int chunkZ = Integer.parseInt(parts[2]);

            castlePiece piece = chunkPieces.get(chunkKey);
            if (piece == null) continue;

            int yBase = 64 + (level * 15);

            // Check all six possible directions
            Directions[] allDirections = Directions.values();
            for (Directions dir : allDirections) {
                // Skip if this piece has this connection
                if (piece.connections.contains(dir)) continue;

                // This direction should be sealed
                int nx = chunkX, nz = chunkZ, nl = level;
                switch (dir) {
                    case North -> nz--;
                    case South -> nz++;
                    case East -> nx++;
                    case West -> nx--;
                    case Up -> nl++;
                    case Down -> nl--;
                }

                // Skip invalid locations
                if (!isValidChunkLocation(world, nx, nz, nl)) continue;

                // Place door
                if (verbose) {
                    Freedom.get_plugin().getLogger().info(
                        String.format("[RedCastle] Sealing unused direction %s at [%d, %d] L%d",
                            dir, chunkX, chunkZ, level)
                    );
                }
                placeDoorAtConnection(world, chunkX, chunkZ, yBase, dir, piece.rotation);
                doorsPlaced++;
            }
        }

        if (verbose) {
            Freedom.get_plugin().getLogger().info(String.format("[RedCastle] Sealed %d unused connections.", doorsPlaced));
        }
    }

    /**
     * Post-generation pass that performs final connectivity checks and seals all orphaned pathways.
     * Call this after the generation queue is empty.
     */
    public static void finalizeGenerationWithConnectivityCheck(World world) {
        if (verbose) {
            Freedom.get_plugin().getLogger().info("[RedCastle] ========== STARTING FINALIZATION PASS ==========");
        }

        // First pass: ensure all openings are reciprocal
        validateAllConnectivity(world);

        // Second pass: ensure no unexpected openings
        ensureAllUnusedConnectionsClosed(world);

        if (verbose) {
            Freedom.get_plugin().getLogger().info(String.format("[RedCastle] ========== FINALIZATION COMPLETE =========="));
            Freedom.get_plugin().getLogger().info(String.format("[RedCastle] Total structures generated: %d", generatedChunks.size()));
        }
    }

    /**
     * Selects a piece for the chunk based on full context of nearby 5 chunks.
     * This ensures proper connectivity by considering the entire local topology.
     */
    private static castlePiece selectPieceForChunkWithContext(World world, int chunkX, int chunkZ, int level, Map<String, ChunkNodeInfo> context) {
        Random random = new Random(world.getSeed() + (long) chunkX * 31213L + (long) chunkZ * 43241L + level * 777L);

        // Required and forbidden connections based on neighbors
        List<Directions> required = new ArrayList<>();
        List<Directions> forbidden = new ArrayList<>();

        // Check all neighbor positions
        checkNeighbor(world, chunkX + 1, chunkZ, level, Directions.East, required, forbidden);
        checkNeighbor(world, chunkX - 1, chunkZ, level, Directions.West, required, forbidden);
        checkNeighbor(world, chunkX, chunkZ + 1, level, Directions.South, required, forbidden);
        checkNeighbor(world, chunkX, chunkZ - 1, level, Directions.North, required, forbidden);
        checkNeighbor(world, chunkX, chunkZ, level + 1, Directions.Up, required, forbidden);
        checkNeighbor(world, chunkX, chunkZ, level - 1, Directions.Down, required, forbidden);

        if (verbose) {
            Freedom.get_plugin().getLogger().info(String.format("[RedCastle] Context Selection for [%d, %d] L%d: Generated in context: %d, Required=%s, Forbidden=%s",
                    chunkX, chunkZ, level, context.size(), required, forbidden));
        }

        // If no required connections and chunk isn't active, skip it
        if (required.isEmpty() && !isChunkActive(world, chunkX, chunkZ, level)) {
            return null;
        }

        // Find all possible pieces that match requirements
        List<castlePiece> possible = new ArrayList<>();

        for (castlePiece basePiece : redCastlePieces) {
            for (int rot : List.of(0, 90, 180, 270)) {
                castlePiece rotated = basePiece.rotated(rot);

                // Check if piece matches exactly
                if (matches(rotated, required, forbidden)) {
                    possible.add(rotated);
                }
            }
        }

        // Fallback 1: Find pieces with all required connections (ignore forbidden if no exact match)
        if (possible.isEmpty()) {
            for (castlePiece basePiece : redCastlePieces) {
                for (int rot : List.of(0, 90, 180, 270)) {
                    castlePiece rotated = basePiece.rotated(rot);
                    if (matchesRequired(rotated, required)) {
                        boolean hasForbidden = false;
                        for (Directions forb : forbidden) {
                            if (rotated.connections.contains(forb)) {
                                hasForbidden = true;
                                break;
                            }
                        }
                        if (!hasForbidden) {
                            possible.add(rotated);
                        }
                    }
                }
            }
        }

        // Fallback 2: Accept any piece that has required connections
        if (possible.isEmpty()) {
            for (castlePiece basePiece : redCastlePieces) {
                for (int rot : List.of(0, 90, 180, 270)) {
                    castlePiece rotated = basePiece.rotated(rot);
                    if (matchesRequired(rotated, required)) {
                        possible.add(rotated);
                    }
                }
            }
        }

        if (possible.isEmpty()) {
            return null;
        }

        // Weight: prefer pieces that EXACTLY match required (no extra connections)
        List<castlePiece> perfectMatches = possible.stream()
                .filter(p -> p.connections.size() == required.size())
                .collect(Collectors.toList());

        if (!perfectMatches.isEmpty()) {
            return perfectMatches.get(random.nextInt(perfectMatches.size()));
        }

        return possible.get(random.nextInt(possible.size()));
    }

    /**
     * Checks if a neighbor chunk wants to connect in the specified direction.
     * Updates required and forbidden connection lists.
     */
    private static void checkNeighbor(World world, int nx, int nz, int level, Directions dirToNeighbor, List<Directions> required, List<Directions> forbidden) {
        if (doesNeighborWantToConnect(world, nx, nz, level, getOpposite(dirToNeighbor))) {
            required.add(dirToNeighbor);
        } else {
            forbidden.add(dirToNeighbor);
        }
    }
}
