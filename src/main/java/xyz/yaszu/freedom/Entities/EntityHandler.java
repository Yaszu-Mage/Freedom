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
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EntityHandler extends Util implements Listener {
    public HashMap<Biome,List<BaseEntity>> BiomeEntities = new HashMap<>();
    public HashMap<UUID, EntityTracker> entityTrackers = new HashMap<>();
    public HashMap<String,BaseEntity> customEntities = new HashMap<>();


    public void register(BaseEntity entity) {
        entity.spawnBiomes().forEach(biome -> {
            customEntities.put(entity.baseType().name(),entity);
            if (BiomeEntities.containsKey(biome)) {
                List<BaseEntity> list = BiomeEntities.get(biome);
                list.add(entity);
                BiomeEntities.put(biome,list);
            } else {
                BiomeEntities.put(biome,List.of(entity));
            }
        });
    }
    Random random = new Random();

    int maxSpawnCount = 32;
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        int customEntityCount = 0;
        for (Entity entity : event.getChunk().getEntities()) {
            if (entity.getPersistentDataContainer().has(keygen("customEntity"))) {
                customEntityCount++;
                if (!entityTrackers.containsKey(entity.getUniqueId())) {
                    customEntities.get(
                            entity.getPersistentDataContainer().get(keygen("customEntity"),
                                    PersistentDataType.STRING
                            )).restore(entity);
                }
            }

        }
        if (customEntityCount > maxSpawnCount) return;
        int pos = random.nextInt(-64,200);
        if (random.nextInt(10) == 0) {
            spawnCustomEntityInChunk(event.getChunk(),pos);
        }
    }

    private void spawnCustomEntityInChunk(Chunk chunk,int y) {
        Biome biome = chunk.getBlock(0,y,0).getComputedBiome();
        List<BaseEntity> entities = BiomeEntities.get(biome);
        if (entities != null) {
            for (int x = 0; x < random.nextInt(entities.size()); x++) {
                //get random pos in biomechunk
                byte pos = (byte) random.nextInt(entities.size());
                BaseEntity entity = entities.get(pos);
                int randomX = random.nextInt(15);
                int randomZ = random.nextInt(15);
                Location spawnLocation = chunk.getBlock(randomX,y,randomZ).getLocation();
                EntityTracker tracker = entity.initialize(spawnLocation);
            }

        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity().getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            if (random.nextInt(100) == 0) {
                spawnCustomEntityInChunk(event.getEntity().getChunk(),event.getEntity().getLocation().getBlockY());
            }
        }
    }

}