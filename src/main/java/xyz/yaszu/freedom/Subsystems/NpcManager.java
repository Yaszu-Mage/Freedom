package xyz.yaszu.freedom.Subsystems;

import com.destroystokyo.paper.entity.ai.VanillaGoal;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.github.javafaker.Faker;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.google.gson.Gson;
import com.ibm.icu.impl.Pair;
import io.papermc.paper.event.entity.EntityMoveEvent;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.InventoryPersistentDataType;
import xyz.yaszu.freedom.Util.StructureUtil;
import xyz.yaszu.freedom.Util.Util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class NpcManager extends Util implements Listener {
    public static Faker faker = new Faker();
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
                 NPCPickup(npc);
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
                        goHomeBitch(npc);

                     }
                 }

             }
         }
        };
    }


    void StructoInstructions(NPC npc, String struc){
        //mahoraga help
        //npc actually constructing it
        //so first get resources
        List<ItemStack> list = StructureUtil.getSchemMaterialsFromResource(struc);
        //then let's check if we have the resources

    }

    void createFarm(NPC npc) {
        //so let's offset all npc fucking farming by 20 blocks
        //lets make an organic looking sequence where the person actually builds farm, but wait there a problem we need fucking resources
        StructureUtil.getSchemMaterialsFromResource("farm.schem");
    }
    int moveTicks = 120;
    // this is how many move runs need to happen until hunger goes down
    @EventHandler
    public void EntityMoveEvent(EntityMoveEvent event) {
        Entity entity = event.getEntity();
        if (entity.getPersistentDataContainer().has(keygen("isNpc"))) {
            //we gotta do ticks or this will be awful
            // so every
            if (!entity.getPersistentDataContainer().has(keygen("moveTicks"))) {
                entity.getPersistentDataContainer().set(keygen("moveTicks"), PersistentDataType.INTEGER, 1);
            } else {
                if (entity.getPersistentDataContainer().get(keygen("moveTicks"), PersistentDataType.INTEGER) >= moveTicks) {
                    NPC npc = NPCs.getOrDefault(UUID.fromString(entity.getPersistentDataContainer().get(keygen("NPCID"), PersistentDataType.STRING)), null);
                    npc.hunger--;
                    npc.BaseEntity.getPersistentDataContainer().set(keygen("hunger"), PersistentDataType.DOUBLE, npc.hunger);
                }
            }
        }
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
                    block = findMyWood(currentLocation,lookingMaterial, 100);
                    for (ItemStack otherResource : resources) {
                        if (otherResource.getType() == block.getType()) {
                            resources.set(resources.indexOf(otherResource),ItemStack.of(block.getType(), 1));
                        }
                        // literally replace the all fucking WRONG wood types with the right one
                    }
                }
            }
            if (block.getY() < getGroundLocation(block.getLocation())) {
                //THIS SHIT IS UNDERGROUND
                // I should prolly add a thing to ensure we got a fucking pickaxe...
                if (getPreferredTool(block, npc) == -1) {
                    //GET A FUCKING TOOL

                }
            } else {

            }
        }
    }



    public Location pathtoBlock(Block block, NPC npc){
        //our goal is to get into the general radius of the block, within a one block range, if we can't we will need to break blocks
        Location nextLocation = block.getLocation();
        if (block.getWorld().getBlockAt(block.getLocation().clone().add(1,0,0)).getType() == Material.AIR) {
            nextLocation = new Location(block.getWorld(),block.getLocation().getBlockX() + 1,getGroundLocation(block.getLocation()),block.getLocation().getBlockZ());
        }
        if (block.getWorld().getBlockAt(block.getLocation().clone().add(1,0,0)).getType() == Material.AIR) {
            nextLocation = new Location(block.getWorld(),block.getLocation().getBlockX(),getGroundLocation(block.getLocation()),block.getLocation().getBlockZ() + 1);
        }
        return nextLocation;
    }
    public int getPreferredTool(Block block, NPC npc) {
        AtomicReference<ItemStack> tool = new AtomicReference<>();
        npc.inventory.forEach(item -> {
            if (block.isPreferredTool(item)) {
                tool.set(item);
            }
        });
        if (tool.get() == null) {
            return -1;
        }
        return npc.inventory.first(tool.get());
    }
    public boolean NPCBreakBlock(NPC npc, Block block) {
        boolean output = false;
        // so we gonna make sure FUCKASS is actually near the block
        if (npc.BaseEntity.getLocation().distanceSquared(block.getLocation()) > 10) {
            npc.move(pathtoBlock(block, npc));
        }
        new BukkitRunnable() {
            int ticks = 0;
            float tickstobreak = 0;
            @Override
            public void run() {
                if (npc.BaseEntity.isDead()) {
                    this.cancel();
                    return;
                }
                if (ticks == 0) {
                    // we need to find the base speed at which including potion effects, water, and tool, that it would take to break the block
                    if (getPreferredTool(block, npc) == -1) {
                        //uhh construct a tool
                        if (block.getType().toString().toUpperCase().contains("LOG")) {
                            //just break it bro
                            tickstobreak = block.getDestroySpeed(ItemStack.of(block.getType(), 1));
                            // we are just gonna assume the type bc that's equal to hand
                        }
                    }
                }
                if (ticks >= tickstobreak) {
                    this.cancel();
                    block.breakNaturally();
                    return;
                }
                ticks++;
            }
        }.runTaskTimer(Freedom.get_plugin(), 0, 0);
        return output;
    }
    public static boolean NPCPickup(NPC npc) {
        AtomicBoolean output = new AtomicBoolean(false);
        if (npc.getInventory().getContents().length >= 27) {
            return false;
        }
        npc.BaseEntity.getLocation().getNearbyEntitiesByType(Item.class,2).forEach(entity -> {
            if (npc.getInventory().getContents().length >= 27) {
                output.set(false);
            } else {
                npc.getInventory().addItem(entity.getItemStack());
                entity.remove();
                output.set(true);
            }
        });
        return output.get();
    }

    public void sortNpcInventory(NPC npc) {
        //combine like stacks until max stack size
        Inventory inventory = npc.getInventory();
        inventory.forEach(itemStack -> {

        });
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

    static void goHomeBitch(NPC npc) {
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

    public static NPC createNPC(Location location) {
        NPC npc = new NPC((Cow) location.getWorld().spawn(location, Cow.class), 20d, NpcGoal.Wander);
        return npc;
    }


    public static void recompileNPC(Entity entity){
        if (entity.getPersistentDataContainer().has(keygen("isNpc"))) {
            Cow npcEntity = (Cow) entity;
            String NPCID = npcEntity.getPersistentDataContainer().get(keygen("NPCID"), PersistentDataType.STRING);
            String Name = npcEntity.getPersistentDataContainer().get(keygen("name"), PersistentDataType.STRING);
            UUID uuid = UUID.fromString(NPCID);
            Freedom.get_plugin().getLogger().info("NPCID: " + NPCID);
            Freedom.get_plugin().getLogger().info("UUID: " + uuid);
            Freedom.get_plugin().getLogger().info("NPCs: " + NPCs.toString());
            Freedom.get_plugin().getLogger().info("NPCs.containsKey(UUID.fromString(NPCID)): " + NPCs.containsKey(UUID.fromString(NPCID)));
            if (NPCs.containsKey(UUID.fromString(NPCID))) return;
            NpcGoal goal = NpcGoal.valueOf(npcEntity.getPersistentDataContainer().get(keygen("goal"), PersistentDataType.STRING));
            Double hunger = npcEntity.getPersistentDataContainer().get(keygen("hunger"), PersistentDataType.DOUBLE);
            String skin = npcEntity.getPersistentDataContainer().get(keygen("skin"), PersistentDataType.STRING);
            boolean isPopular = npcEntity.getPersistentDataContainer().get(keygen("popular"), PersistentDataType.BOOLEAN);
            UserProfile profile;
            if (isPopular) {
                profile = new UserProfile(uuid,Name,SkinLoader.actuallyConstruct(skin));
            } else {
                profile = new UserProfile(uuid,Name);
            }

            //recompile npc based on goal and other factors
            PlayerDisguise disguise = new PlayerDisguise("Yaszu");
            PlayerWatcher playerwatcher = (PlayerWatcher) disguise.getWatcher();
            playerwatcher.setSkin(profile);
            playerwatcher.setName(Name);
            disguise.setEntity(entity);
            disguise.startDisguise();
            Freedom.get_plugin().getLogger().info("Recompiled NPC with goal: " + goal.toString() + " and hunger: " + hunger);
            NPC npc = new NPC(npcEntity, hunger, goal);
            npc.disguise = disguise;
            NPCs.put(uuid, npc);
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
        public Disguise disguise;

        public List<String> randomPopularNames = List.of(
                "d3rlord3",
                "TheMostMayo"
        );
        //so we need to figure out how to make a fucking ass inventory
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
                BaseEntity.getPersistentDataContainer().set(keygen("name"),PersistentDataType.STRING, "ZeepZorp");
                BaseEntity.getPersistentDataContainer().set(keygen("skin"), PersistentDataType.STRING, "SweetVikki");
                data = BaseEntity.getPersistentDataContainer();
                UUID npcid = UUID.fromString(BaseEntity.getPersistentDataContainer().get(keygen("NPCID"), PersistentDataType.STRING));
                //create new disguise and put it on them
                String name = "";
                Random random = new Random();
                boolean popular = false;
                if (random.nextInt(0,1000) == 0) {
                    popular = true;
                    name = randomPopularNames.get(random.nextInt(randomPopularNames.size()));
                }
                PlayerDisguise disguise = new PlayerDisguise("Yaszu");
                PlayerWatcher playerwatcher = (PlayerWatcher) disguise.getWatcher();
                Pair<List<TextureProperty>,String> pair;
                if (!popular) {
                    name = faker.name().firstName();
                    //lets find a random skin
                    pair = SkinLoader.loadRandomSkin();
                    List<TextureProperty> textures = pair.first;
                    UserProfile profile = new UserProfile(npcid, name,textures);
                    playerwatcher.setSkin(profile);
                    BaseEntity.getPersistentDataContainer().set(keygen("skin"),PersistentDataType.STRING,pair.second);
                } else {
                    playerwatcher.setSkin(name);
                }
                playerwatcher.setName(name);
                disguise.setEntity(BaseEntity);
                disguise.startDisguise();
                NpcManager.NPCs.put(UUID.fromString(BaseEntity.getPersistentDataContainer().get(keygen("NPCID"),PersistentDataType.STRING)), this);
                this.disguise = disguise;
                BaseEntity.getAttribute(Attribute.TEMPT_RANGE).setBaseValue(0);
                BaseEntity.getPersistentDataContainer().set(keygen("name"),PersistentDataType.STRING, name);
                BaseEntity.getPersistentDataContainer().set(keygen("isPopular"),PersistentDataType.BOOLEAN, popular);

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
