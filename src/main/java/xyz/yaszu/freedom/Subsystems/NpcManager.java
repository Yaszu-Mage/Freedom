package xyz.yaszu.freedom.Subsystems;

import com.destroystokyo.paper.entity.ai.VanillaGoal;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.checkerframework.checker.units.qual.N;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.InventoryPersistentDataType;
import xyz.yaszu.freedom.Util.StructureUtil;
import xyz.yaszu.freedom.Util.Util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;


public class NpcManager extends Util implements Listener {
    public static enum NpcGoal {
        Socialize,
        Mine,
        Construct,
        Farm,
        Wander,
        Harvest,
        Think
    }




    public static BukkitRunnable update() {
        return new BukkitRunnable() {
         @Override
         public void run() {
             for (UUID key : NPCs.keySet()) {
                 NPC npc = NPCs.get(key);
                 if (npc.BaseEntity.isDead()) {
                     NPCs.remove(key);
                     npc.BaseEntity.remove();
                     return;
                 }
                 npc.hunger--;
                 if (npc.hunger <= 10) {
                     if (!npc.data.has(keygen("home"))) {
                        if (npc.BaseEntity.getWorld() == Bukkit.getWorld("world")) {
                            //hey lets find somewhere habitable
                            // is where I am habitable?
                            Location currentLocation = npc.BaseEntity.getLocation();
                            if (npc.isStandable(currentLocation)) {
                                //eh it's good enough
                                npc.move(currentLocation);
                                makeTent(npc, currentLocation);
                                // next let's build a farm
                                //look for grass to get seeds
                                //TODO make function for settling
                            } else {
                                new BukkitRunnable() {

                                    @Override
                                    public void run() {
                                        if (npc.BaseEntity.isDead()) {
                                            this.cancel();
                                            return;
                                        }
                                        if (npc.BaseEntity.getPathfinder().hasPath()) {
                                            return;
                                        }
                                        Location nextLocation = npc.BaseEntity.getLocation();
                                        if (!npc.isStandable(nextLocation)) {
                                            // we find the nearest standable block then walk to it
                                            nextLocation = nextLocation.clone().add(random.nextInt(10), 0, random.nextInt(10));
                                            nextLocation = nextLocation.clone().add(0, getGroundLocation(nextLocation), 0);
                                            npc.move(nextLocation);
                                        } else {
                                            //MAKE YOUR FUCKING TENT LITTLE BRO
                                            makeTent(npc, currentLocation);
                                            this.cancel();
                                        }
                                    }
                                }.runTaskTimer(Freedom.get_plugin(), 0, 0);
                            }
                        }
                     } else {
                         //go home


                     }
                 }

             }
         }
        };
    }


    void createFarm(NPC npc) {
        //so let's offset all npc fucking farming by 20 blocks
        //lets make an organic looking sequence where the person actually builds farm, but wait there a problem we need fucking resources
        StructureUtil.getSchemMaterialsFromResource("farm.schem");
    }



    void GatherResources(List<ItemStack> resources, NPC npc) {
        //so we need to find the nearest resource of the type we want, then path to it and break it
        //if there isn't any nearby we wander, not as far as 100 blocks though
        //how the fuck we gonna do this
        Location currentLocation = npc.BaseEntity.getLocation();
        for (ItemStack resource : resources) {
            Material lookingMaterial = resource.getType();
            Block block = findNearestBlock(currentLocation,lookingMaterial, 100);
            if (block == null) {
                if (resource.getType().toString().toUpperCase().contains("LOG")) {
                    //look for other log types bitch

                }
            }
            if (block.getY() < getGroundLocation(block.getLocation())) {
                //THIS SHIT IS UNDERGROUND
                // I should prolly add a thing to ensure we got a fucking pickaxe...
            } else {

            }
        }
    }

    public Block findMyWood(Location center, Material target, int radius) {
        Block block = findNearestBlock(center,target, 100);
        if (block == null) {
            block = findNearestBlock(center,Material.OAK_LOG, 100);
        }
        if (block == null) {
            block = findNearestBlock(center,Material.SPRUCE_LOG, 100);
        }
        if (block == null) {
            block = findNearestBlock(center,Material.BIRCH_LOG, 100);
        }
        if (block == null) {
            block = findNearestBlock(center,Material.JUNGLE_LOG, 100);
        }
        if (block == null) {
            block = findNearestBlock(center,Material.ACACIA_LOG, 100);
        }
        if (block == null) {
            block = findNearestBlock(center,Material.DARK_OAK_LOG, 100);
        }
        if (block == null) {
            block = findNearestBlock(center,Material.MANGROVE_LOG, 100);
        }
        if (block == null) {
            block = findNearestBlock(center,Material.CRIMSON_STEM, 100);
        }
        if (block == null) {
            block = findNearestBlock(center,Material.WARPED_STEM, 100);
        }
        return block;

    }

    public Block findNearestBlock(Location center, Material target, int radius) {
        Block closestBlock = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.clone().add(x, y, z).getBlock();

                    if (block.getType() == target) {
                        double distanceSq = center.distanceSquared(block.getLocation());
                        if (distanceSq < closestDistanceSq) {
                            closestDistanceSq = distanceSq;
                            closestBlock = block;
                        }
                    }
                }
            }
        }
        return closestBlock;
    }

    void goHomeBitch(NPC npc) {
        //so we need to get the home location from the npc's data, then path to it
        boolean err = false;
        double homeX = 0;
        double homeY = 0;
        double homeZ = 0;
        World homeworld = npc.BaseEntity.getWorld();
        try {
            homeX = npc.data.get(keygen("homeX"), PersistentDataType.DOUBLE);
            homeY = npc.data.get(keygen("homeY"), PersistentDataType.DOUBLE);
            homeZ = npc.data.get(keygen("homeZ"), PersistentDataType.DOUBLE);
            homeworld = Bukkit.getWorld(npc.data.get(keygen("homeworld"), PersistentDataType.STRING));
        } catch (Exception e) {
            err = true;
        }
        if (err) {
            //SALVAGE HOME
            boolean homeXValid = false;
            boolean homeYValid = false;
            boolean homeZValid = false;
            boolean homeworldValid = false;
            if (homeX != 0) {
                homeXValid = true;
            }
            if (homeY != 0) {
                homeYValid = true;
            }
            if (homeZ != 0) {
                homeZValid = true;
            }
            if (homeworld != null) {
                homeworldValid = true;
            }
            if (homeXValid && homeYValid && homeZValid && homeworldValid) {
                //fuhh I musta been tripping
                try {
                    Location home = new Location(homeworld, homeX, homeY, homeZ);
                    npc.move(home);
                } catch (Exception e) {
                    //welp guess not
                }

            }
        } else if (!err && npc.BaseEntity.getWorld().equals(homeworld)) {
            Location home = new Location(homeworld, homeX, homeY, homeZ);
            npc.move(home);
        }
    }

    boolean hasHome(NPC npc) {
        boolean validHome = false;
        if (npc.data.has(keygen("home"), PersistentDataType.BOOLEAN)) {
            validHome = true;
        }
        return validHome;
    }

    //Sets Home location
    private static void makeTent(NPC npc, Location currentLocation) {
        npc.data.set(keygen("homeX"), PersistentDataType.DOUBLE, currentLocation.getX());
        npc.data.set(keygen("homeY"), PersistentDataType.DOUBLE, currentLocation.getY());
        npc.data.set(keygen("homeZ"), PersistentDataType.DOUBLE, currentLocation.getZ());
        npc.data.set(keygen("homeworld"), PersistentDataType.STRING, currentLocation.getWorld().getName());
    }

    public static HashMap<UUID,NPC> NPCs = new HashMap<>();

    public static void createNPC(Location location) {
        NPC npc = new NPC((Cow) location.getWorld().spawn(location, Cow.class), 20d, NpcGoal.Wander);
    }


    public static void recompileNPC(Entity entity){
        if (entity.getPersistentDataContainer().has(keygen("isNpc"))) {
            Cow npcEntity = (Cow) entity;
            String NPCID = npcEntity.getPersistentDataContainer().get(keygen("NPCID"), PersistentDataType.STRING);
            UUID uuid = UUID.fromString(NPCID);
            Freedom.get_plugin().getLogger().info("NPCID: " + NPCID);
            Freedom.get_plugin().getLogger().info("UUID: " + uuid);
            Freedom.get_plugin().getLogger().info("NPCs: " + NPCs.toString());
            Freedom.get_plugin().getLogger().info("NPCs.containsKey(UUID.fromString(NPCID)): " + NPCs.containsKey(UUID.fromString(NPCID)));
            if (NPCs.containsKey(UUID.fromString(NPCID))) return;
            NpcGoal goal = NpcGoal.valueOf(npcEntity.getPersistentDataContainer().get(keygen("goal"), PersistentDataType.STRING));
            Double hunger = npcEntity.getPersistentDataContainer().get(keygen("hunger"), PersistentDataType.DOUBLE);
            String skin = npcEntity.getPersistentDataContainer().get(keygen("skin"), PersistentDataType.STRING);

            //recompile npc based on goal and other factors
            PlayerDisguise disguise = new PlayerDisguise("SweetVikki"
);
            PlayerWatcher playerwatcher = (PlayerWatcher) disguise.getWatcher();
            playerwatcher.setSkin("SweetVikki"
);
            playerwatcher.setName("ZeepZorp"
);
            disguise.setEntity(entity);
            playerwatcher.setSkin("SweetVikki"
);
            npcEntity.getAttribute(Attribute.TEMPT_RANGE).setBaseValue(0);
            disguise.startDisguise();
            Freedom.get_plugin().getLogger().info("Recompiled NPC with goal: " + goal.toString() + " and hunger: " + hunger);
            NPCs.put(uuid, new NPC(npcEntity, hunger, goal));
            Bukkit.getServer().getMobGoals().removeGoal(npcEntity, VanillaGoal.TEMPT);
        }
    }



    @EventHandler
    public void onEntityPathfind(EntityPathfindEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(keygen("isNpc"))) {
            String NPCID = event.getEntity().getPersistentDataContainer().get(keygen("NPCID"), PersistentDataType.STRING);
            if (NPCs.getOrDefault(UUID.fromString(NPCID), null) == null) {
                recompileNPC(event.getEntity());
            }
            Cow npcEntity = (Cow) event.getEntity();
            NpcGoal goal = NpcGoal.valueOf(npcEntity.getPersistentDataContainer().get(keygen("goal"), PersistentDataType.STRING));

            if (goal == NpcGoal.Wander) {
                // Allow pathfinding to continue
            } else {
                event.setCancelled(true);
            }

        }
    }

    public static class NPC {
        public Cow BaseEntity;
        public Double hunger = 20d;
        public NpcGoal goal = NpcGoal.Think;
        public PersistentDataContainer data;
        private Inventory inventory;
        //so we need to figure out how to make a fucking ass fucking ass inventory
        public NPC(Cow givenBaseEntity, Double GivenHunger, NpcGoal givengoal){
            if (givengoal != null) goal = givengoal;
            if (givenBaseEntity == null) return;
            BaseEntity = givenBaseEntity;
            hunger = GivenHunger;
            goal = givengoal;
            if (!BaseEntity.getPersistentDataContainer().has(keygen("NPCID"))) {
                givenBaseEntity.getPersistentDataContainer().set(keygen("goal"), PersistentDataType.STRING, goal.toString());
                BaseEntity.getPersistentDataContainer().set(keygen("inventory"), InventoryPersistentDataType.get(), Bukkit.createInventory(null, 27, dess("NPC Inventory")));
                BaseEntity.getPersistentDataContainer().set(keygen("NPCID"), PersistentDataType.STRING, UUID.randomUUID().toString());
                BaseEntity.getPersistentDataContainer().set(keygen("isNpc"), PersistentDataType.BOOLEAN, true);
                BaseEntity.getPersistentDataContainer().set(keygen("hunger"), PersistentDataType.DOUBLE, hunger);
                BaseEntity.getPersistentDataContainer().set(keygen("skin"), PersistentDataType.STRING, "SweetVikki");
                data = BaseEntity.getPersistentDataContainer();
                //create new disguise and put it on them
                PlayerDisguise disguise = new PlayerDisguise("SweetVikki"
                );
                PlayerWatcher playerwatcher = (PlayerWatcher) disguise.getWatcher();

                disguise.setEntity(BaseEntity);
                playerwatcher.setSkin("SweetVikki"
                );
                disguise.startDisguise();
                NpcManager.NPCs.put(UUID.fromString(BaseEntity.getPersistentDataContainer().get(keygen("NPCID"),PersistentDataType.STRING)), this);
                playerwatcher.setSkin("SweetVikki");
                playerwatcher.setName("ZeepZorp");
                BaseEntity.getAttribute(Attribute.TEMPT_RANGE).setBaseValue(0);
                Bukkit.getServer().getMobGoals().removeGoal(BaseEntity, VanillaGoal.TEMPT);
            }



        }
        public Inventory getInventory() {
            if (inventory == null) {
                inventory = BaseEntity.getPersistentDataContainer().get(keygen("inventory"), InventoryPersistentDataType.get());
            }
            return inventory;
        }
        public void move(Location targetLocation) {
            BaseEntity.getPersistentDataContainer().set(keygen("isallowedtomove"), PersistentDataType.BOOLEAN, true);
            BaseEntity.getPathfinder().moveTo(targetLocation, 1);
            //when path is finished change isallowedtomove to false
        }

        private boolean isStandable(Location location) {
            return location.getBlock().isPassable()
                    && location.clone().add(0, 1, 0).getBlock().isPassable()
                    && !location.clone().add(0, -1, 0).getBlock().isPassable();
        }

    }



    public static double getGroundLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return 0d;

        World world = loc.getWorld();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        // Paper-specific: ignores leaves, finds solid ground
        int y = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);

        return y;
    }
}
