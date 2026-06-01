package xyz.yaszu.freedom.Entities;

import kr.toxicity.model.api.tracker.EntityTracker;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EntityHandler extends Util implements Listener {

    // Biome -> list of entities that can spawn there
    public final HashMap<Biome, List<BaseEntity>> biomeEntities = new HashMap<>();

    // UUID -> active tracker for loaded custom entities
    public final HashMap<UUID, EntityTracker> entityTrackers = new HashMap<>();

    // name() -> BaseEntity definition (e.g. "capybara" -> Capybara instance)
    public final HashMap<String, BaseEntity> customEntities = new HashMap<>();

    private final Random random = new Random();
    private final int maxSpawnCount = 32;

    // -------------------------
    // Registration
    // -------------------------

    public void register(BaseEntity entity) {
        // Register by name() instead of baseType().name() so multiple
        // custom entities can share the same base type (e.g. two PIG variants)
        customEntities.put(entity.name(), entity);

        entity.spawnBiomes().forEach(biome -> {
            biomeEntities.computeIfAbsent(biome, k -> new ArrayList<>()).add(entity);
        });
    }

    // -------------------------
    // Chunk Load - restore or spawn
    // -------------------------

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        int customEntityCount = 0;

        for (Entity entity : event.getChunk().getEntities()) {
            if (!entity.getPersistentDataContainer().has(keygen("customEntity"))) continue;

            customEntityCount++;

            // Restore entity if not already tracked
            if (!entityTrackers.containsKey(entity.getUniqueId())) {
                String entityName = entity.getPersistentDataContainer()
                        .get(keygen("customEntity"), PersistentDataType.STRING);

                if (entityName == null) continue;

                BaseEntity base = customEntities.get(entityName);
                if (base == null) continue;

                EntityTracker tracker = base.restore(entity);
                if (tracker != null) {
                    entityTrackers.put(entity.getUniqueId(), tracker);
                }
            }
        }

        // Don't spawn if chunk is already dense with custom entities
        if (customEntityCount >= maxSpawnCount) return;

        // 1 in 10 chance to attempt a spawn when chunk loads
        if (random.nextInt(10) == 0) {
            int y = random.nextInt(-64, 200);
            spawnCustomEntityInChunk(event.getChunk(), y);
        }
    }

    // -------------------------
    // Natural Spawn - piggyback a custom spawn
    // -------------------------

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity().getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;

        // 1 in 100 chance to also spawn a custom entity nearby
        if (random.nextInt(100) == 0) {
            spawnCustomEntityInChunk(
                    event.getEntity().getChunk(),
                    event.getEntity().getLocation().getBlockY()
            );
        }
    }

    // -------------------------
    // Spawn logic
    // -------------------------

    private void spawnCustomEntityInChunk(Chunk chunk, int y) {
        Biome biome = chunk.getBlock(0, y, 0).getComputedBiome();
        List<BaseEntity> entities = biomeEntities.get(biome);

        if (entities == null || entities.isEmpty()) return;

        // Spawn a random number of entities (1 up to list size)
        int spawnCount = random.nextInt(1, entities.size() + 1);

        for (int i = 0; i < spawnCount; i++) {
            BaseEntity entity = entities.get(random.nextInt(entities.size()));

            int randomX = random.nextInt(16);
            int randomZ = random.nextInt(16);
            Location spawnLocation = chunk.getBlock(randomX, y, randomZ).getLocation();

            EntityTracker tracker = entity.initialize(spawnLocation);
            if (tracker != null) {
                // Track the spawned entity by its UUID if initialize exposes it
                // If initialize returns the tracker, you can store it here once
                // you have access to the entity UUID from the tracker
            }
        }
    }
}