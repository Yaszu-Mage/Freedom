package xyz.yaszu.freedom.Subsystems;

import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;
import xyz.yaszu.freedom.Util.Util;

import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.createChunkData;

public class RedCastleManager extends Util {


    public static List<castlePiece> redCastlePieces = List.of(
            new castlePiece(
                    "4way",
                    List.of(Directions.South, Directions.North, Directions.East, Directions.West),
                    List.of(),
                    false
            ),
            new castlePiece(
                    "prison",
                    List.of(Directions.South, Directions.West),
                    List.of(),
                    false
            ),
            new castlePiece(
                    "TreeRoom",
                    List.of(Directions.South, Directions.North, Directions.East, Directions.West),
                    List.of(),
                    false
            ),
            new castlePiece(
                    "stairs",
                    List.of(Directions.South, Directions.North, Directions.Up, Directions.Down),
                    List.of(),
                    false

            ),
            new castlePiece(
                    "Walkway",
                    List.of(Directions.South, Directions.North),
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
    public static boolean verbose = false;
    

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

    public static void generateCastle(World world, int chunkX, int chunkZ) {
        // Use PDC to ensure we only spawn structures once per chunk
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        if (chunk.getPersistentDataContainer().has(xyz.yaszu.freedom.Util.FreedomKeys.key("redcastle_generated"), org.bukkit.persistence.PersistentDataType.BOOLEAN)) {
            return;
        }
        chunk.getPersistentDataContainer().set(xyz.yaszu.freedom.Util.FreedomKeys.key("redcastle_generated"), org.bukkit.persistence.PersistentDataType.BOOLEAN, true);

        long startTime = System.currentTimeMillis();
        new BukkitRunnable() {
            int level = 0;
            @Override
            public void run() {
                if (level >= maxLevels) {
                    if (verbose) {
                        Freedom.get_plugin().getLogger().info(String.format("[RedCastle] Chunk [%d, %d] generation finished in %dms",
                                chunkX, chunkZ, System.currentTimeMillis() - startTime));
                    }
                    this.cancel();
                    return;
                }

                castlePiece piece = selectPieceForChunk(world, chunkX, chunkZ, level);
                if (piece != null) {
                    int y = 64 + (level * 15);
                    if (verbose) {
                        Freedom.get_plugin().getLogger().info(String.format("[RedCastle] Spawning %s at [%d, %d] Level %d (Y=%d) Rot=%d",
                                piece.schematicName, chunkX, chunkZ, level, y, piece.rotation));
                    }
                    WorldManager.spawnStructureStatic(world, chunkX, chunkZ, y, "redCastle_" + piece.schematicName + ".schem", piece.rotation);
                } else if (verbose) {
                    Freedom.get_plugin().getLogger().info(String.format("[RedCastle] No piece for [%d, %d] Level %d", chunkX, chunkZ, level));
                }

                level++;
            }
        }.runTaskTimer(Freedom.get_plugin(), 2, 5); // Start after 2 ticks, run every 5 ticks (0.25s)
    }

    private static castlePiece selectPieceForChunk(World world, int chunkX, int chunkZ, int level) {

        Random random = new Random(world.getSeed() + (long) chunkX * 31213L + (long) chunkZ * 43241L + level * 777L);

        // Required connections based on neighbors
        List<Directions> required = new ArrayList<>();
        List<Directions> forbidden = new ArrayList<>();

        checkNeighbor(world, chunkX + 1, chunkZ, level, Directions.East, required, forbidden);
        checkNeighbor(world, chunkX - 1, chunkZ, level, Directions.West, required, forbidden);
        checkNeighbor(world, chunkX, chunkZ + 1, level, Directions.South, required, forbidden);
        checkNeighbor(world, chunkX, chunkZ - 1, level, Directions.North, required, forbidden);
        checkNeighbor(world, chunkX, chunkZ, level + 1, Directions.Up, required, forbidden);
        checkNeighbor(world, chunkX, chunkZ, level - 1, Directions.Down, required, forbidden);

        if (verbose) {
            Freedom.get_plugin().getLogger().info(String.format("[RedCastle] Selection for [%d, %d] L%d: Req=%s Forb=%s",
                    chunkX, chunkZ, level, required, forbidden));
        }

        if (required.isEmpty() && !isChunkActive(world, chunkX, chunkZ, level)) {
            return null;
        }

        List<castlePiece> possible = new ArrayList<>();
        for (castlePiece basePiece : redCastlePieces) {
            for (int rot : List.of(0, 90, 180, 270)) {
                castlePiece rotated = basePiece.rotated(rot);
                if (matches(rotated, required, forbidden)) {
                    possible.add(rotated);
                }
            }
        }

        // If no piece matches EXACTLY, try to match only the required ones
        if (possible.isEmpty()) {
            // Pick the piece that has ALL required connections AND no forbidden ones
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

        if (possible.isEmpty()) {
            // Absolute fallback: try to find anything that matches required
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
            return null; // Don't spawn anything if we can't match required connections
        }

        // Weight pieces to prefer those that ONLY have the required connections (reducing dead ends)
        List<castlePiece> bestMatches = possible.stream()
                .filter(p -> p.connections.size() == required.size())
                .collect(Collectors.toList());

        if (!bestMatches.isEmpty()) {
            return bestMatches.get(random.nextInt(bestMatches.size()));
        }

        return possible.get(random.nextInt(possible.size()));
    }

    private static void checkNeighbor(World world, int nx, int nz, int level, Directions dirToNeighbor, List<Directions> required, List<Directions> forbidden) {
        if (doesNeighborWantToConnect(world, nx, nz, level, getOpposite(dirToNeighbor))) {
            required.add(dirToNeighbor);
        } else {
            forbidden.add(dirToNeighbor);
        }
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
}
