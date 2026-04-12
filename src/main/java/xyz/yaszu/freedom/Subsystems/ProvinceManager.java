package xyz.yaszu.freedom.Subsystems;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Lectern;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.Util;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProvinceManager extends Util implements Listener {

    private static final Map<Location, Province> provinces = new ConcurrentHashMap<>();

    public static class Province {
        public UUID owner;
        public Location center;
        public int range; // in chunks
        public Set<Location> structureBlocks;
        public long startTime;
        public boolean blockBreakProtected = true;
        public boolean blockPlaceProtected = true;
        public boolean fireSpreadAllowed = false;
        public boolean explosionsAllowed = false;

        public Province(UUID owner, Location center, int range, Set<Location> structureBlocks) {
            this.owner = owner;
            this.center = center;
            this.range = range;
            this.structureBlocks = structureBlocks;
            this.startTime = System.currentTimeMillis();
        }

        public boolean isProtected(Location loc) {
            if (loc.getWorld() != center.getWorld()) return false;

            // Check if within chunk range
            int centerX = center.getBlockX() >> 4;
            int centerZ = center.getBlockZ() >> 4;
            int locX = loc.getBlockX() >> 4;
            int locZ = loc.getBlockZ() >> 4;

            boolean withinRange = Math.abs(centerX - locX) <= range && Math.abs(centerZ - locZ) <= range;
            if (!withinRange) return false;

            // The structure itself is NOT protected
            if (structureBlocks.contains(loc.getBlock().getLocation())) {
                return false;
            }

            // The center chunk (where the ritual circle is) is NOT protected
            if (locX == centerX && locZ == centerZ) {
                return false;
            }

            return true;
        }
    }

    public static void claimProvince(Player owner, Location rawCenter, int range, Set<Location> structureBlocks) {
        Location center = rawCenter.getBlock().getLocation();
        // Remove any existing province at this center
        provinces.remove(center);
        
        // Ensure range is at least 1 so it actually protects something around the center
        int finalRange = Math.max(1, range);
        Province province = new Province(owner.getUniqueId(), center, finalRange, structureBlocks);
        
        // Initialize settings from player PDC
        province.blockBreakProtected = owner.getPersistentDataContainer().getOrDefault(xyz.yaszu.freedom.Util.FreedomKeys.provinceBlockBreak(), org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        province.blockPlaceProtected = owner.getPersistentDataContainer().getOrDefault(xyz.yaszu.freedom.Util.FreedomKeys.provinceBlockPlace(), org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        province.fireSpreadAllowed = owner.getPersistentDataContainer().getOrDefault(xyz.yaszu.freedom.Util.FreedomKeys.provinceFireSpread(), org.bukkit.persistence.PersistentDataType.BOOLEAN, false);
        province.explosionsAllowed = owner.getPersistentDataContainer().getOrDefault(xyz.yaszu.freedom.Util.FreedomKeys.provinceExplosions(), org.bukkit.persistence.PersistentDataType.BOOLEAN, false);

        provinces.put(center, province);
        
        owner.sendMessage("§aProvince claimed! Protected area: " + (finalRange * 2 + 1) + "x" + (finalRange * 2 + 1) + " chunks.");
        
        // Start visual task for this province
        startIntegrityTask(province);
        saveProvinces(); // Save when a new province is claimed
    }

    public static void saveProvinces() {
        File file = new File(Freedom.get_plugin().getDataFolder(), "provinces.yml");
        YamlConfiguration config = new YamlConfiguration();
        
        int i = 0;
        for (Province p : provinces.values()) {
            String path = "provinces." + i;
            config.set(path + ".owner", p.owner.toString());
            config.set(path + ".center", p.center);
            config.set(path + ".range", p.range);
            config.set(path + ".startTime", p.startTime);
            config.set(path + ".blockBreakProtected", p.blockBreakProtected);
            config.set(path + ".blockPlaceProtected", p.blockPlaceProtected);
            config.set(path + ".fireSpreadAllowed", p.fireSpreadAllowed);
            config.set(path + ".explosionsAllowed", p.explosionsAllowed);
            
            List<Location> structureList = new ArrayList<>(p.structureBlocks);
            config.set(path + ".structureBlocks", structureList);
            i++;
        }
        
        try {
            config.save(file);
        } catch (IOException e) {
            Freedom.get_plugin().getLogger().severe("Could not save provinces.yml!");
            e.printStackTrace();
        }
    }

    public static void loadProvinces() {
        File file = new File(Freedom.get_plugin().getDataFolder(), "provinces.yml");
        if (!file.exists()) return;
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("provinces")) return;
        
        provinces.clear();
        for (String key : config.getConfigurationSection("provinces").getKeys(false)) {
            String path = "provinces." + key;
            UUID owner = UUID.fromString(config.getString(path + ".owner"));
            Location center = (Location) config.get(path + ".center");
            int range = config.getInt(path + ".range");
            long startTime = config.getLong(path + ".startTime");
            
            @SuppressWarnings("unchecked")
            List<Location> structureList = (List<Location>) config.getList(path + ".structureBlocks");
            Set<Location> structureBlocks = new HashSet<>(structureList);
            
            Province province = new Province(owner, center, range, structureBlocks);
            province.startTime = startTime;
            province.blockBreakProtected = config.getBoolean(path + ".blockBreakProtected", true);
            province.blockPlaceProtected = config.getBoolean(path + ".blockPlaceProtected", true);
            province.fireSpreadAllowed = config.getBoolean(path + ".fireSpreadAllowed", false);
            province.explosionsAllowed = config.getBoolean(path + ".explosionsAllowed", false);
            provinces.put(center, province);
            Util.createMinMagicCircle(center.clone().add(0.5,0,0.5),15, SoulTypes.BaseBlack);
            // Re-start visual/integrity tasks
            startIntegrityTask(province);
        }
    }

    private static void startIntegrityTask(Province province) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!provinces.containsKey(province.center)) {
                    this.cancel();
                    return;
                }

                // Check if structure is still intact
                Block centerBlock = province.center.getBlock();
                if (centerBlock.getType() != Material.LECTERN) {
                    unclaim(province.center, "Ritual lectern was removed!");
                    this.cancel();
                    return;
                }

                Lectern lectern = (Lectern) centerBlock.getState();
                if (lectern.getInventory().getItem(0) == null) {
                    unclaim(province.center, "Ritual book was removed!");
                    this.cancel();
                    return;
                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 0, 20);
    }

    private static void showProvinceVisuals(Province province) {
        // Display ritual circle in the center of EACH protected chunk
        int centerX = province.center.getBlockX() >> 4;
        int centerZ = province.center.getBlockZ() >> 4;
        for (int x = centerX - province.range; x <= centerX + province.range; x++) {
            for (int z = centerZ - province.range; z <= centerZ + province.range; z++) {
                if (x == centerX && z == centerZ) continue; // Center chunk is not protected
                if (province.center.getWorld().isChunkLoaded(x, z)) {
                    Location chunkCenter = new Location(province.center.getWorld(), (x << 4) + 8, province.center.getY(), (z << 4) + 8);
                    Util.drawCircle(chunkCenter.add(0.5, 0.1, 0.5), 3.0, province.center.getWorld(), 40, Particle.SOUL_FIRE_FLAME);
                }
            }
        }
    }

    public static void unclaim(Location center, String reason) {
        Province p = provinces.remove(center);
        if (p != null) {
            saveProvinces(); // Save when a province is removed
            Player owner = Bukkit.getPlayer(p.owner);
            if (owner != null && owner.isOnline()) {
                owner.sendMessage("§cProvince lost: " + reason);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        for (Province p : provinces.values()) {
            if (p.isProtected(loc)) {

                if (!p.blockBreakProtected) continue;

                UUID owner = p.owner;
                UUID guest = event.getPlayer().getUniqueId();
                if (!owner.equals(guest)) {
                    if (!TrustManager.hasFlag(owner, guest, TrustManager.ProvinceTrustFlag.BREAK_BLOCKS)) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("§cYou don't have permission to break blocks here!");

                        createMinMagicCircle(event.getPlayer().getLocation(), 15, getSoulType(event.getPlayer()), 60);
                        showProvinceVisuals(p);
                        return;
                    }
                }
            } else if (p.structureBlocks.contains(loc)) {
                // Ritual circle is vulnerable!
                String blockName = event.getBlock().getType().name().replace("_", " ").toLowerCase();
                String breakerName = event.getPlayer().getName();
                unclaim(p.center, "Ritual circle's " + blockName + " was broken by " + breakerName + "!");
            }
        }
    }

    public static void interruptRitual(Player player) {
        Location playerLoc = player.getLocation();
        Province closest = null;
        double minDist = Double.MAX_VALUE;

        for (Province p : provinces.values()) {
            if (p.owner.equals(player.getUniqueId())) {
                double dist = p.center.distanceSquared(playerLoc);
                if (dist < minDist) {
                    minDist = dist;
                    closest = p;
                }
            }
        }

        if (closest != null) {
            unclaim(closest.center, "Interrupted by owner command.");
        } else {
            player.sendMessage("§cYou don't have any active rituals.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlock().getLocation();
        for (Province p : provinces.values()) {
            if (p.isProtected(loc)) {
                if (!p.blockPlaceProtected) continue;
                
                UUID owner = p.owner;
                UUID guest = event.getPlayer().getUniqueId();
                if (!owner.equals(guest)) {
                    if (!TrustManager.hasFlag(owner, guest, TrustManager.ProvinceTrustFlag.PLACE_BLOCKS)) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("§cYou don't have permission to place blocks here!");
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        if (event.getClickedBlock() == null) return;
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.PHYSICAL) return;
        
        Location loc = event.getClickedBlock().getLocation();
        for (Province p : provinces.values()) {
            if (p.isProtected(loc)) {
                UUID owner = p.owner;
                UUID guest = event.getPlayer().getUniqueId();
                if (!owner.equals(guest)) {
                    Block block = event.getClickedBlock();
                    boolean isContainer = block.getState() instanceof Container;
                    
                    if (isContainer) {
                        if (!TrustManager.hasFlag(owner, guest, TrustManager.ProvinceTrustFlag.OPEN_CHESTS)) {
                            event.setCancelled(true);
                            event.getPlayer().sendMessage("§cYou don't have permission to open containers here!");
                            return;
                        }
                    } else {
                        // General interaction (buttons, levers, etc.)
                        if (!TrustManager.hasFlag(owner, guest, TrustManager.ProvinceTrustFlag.INTERACT_BLOCKS)) {
                            // Only cancel if it's a right click interaction that matters
                            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                                event.setCancelled(true);
                                event.getPlayer().sendMessage("§cYou don't have permission to interact with blocks here!");
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        
        Location loc = event.getEntity().getLocation();
        for (Province p : provinces.values()) {
            if (p.isProtected(loc)) {
                UUID owner = p.owner;
                UUID guest = player.getUniqueId();
                if (!owner.equals(guest)) {
                    if (!TrustManager.hasFlag(owner, guest, TrustManager.ProvinceTrustFlag.ATTACK_MOBS)) {
                        event.setCancelled(true);
                        player.sendMessage("§cYou don't have permission to attack entities here!");
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosion(EntityExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockExplosion(BlockExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    private void handleExplosion(List<Block> blocks) {
        Iterator<Block> it = blocks.iterator();
        while (it.hasNext()) {
            Block b = it.next();
            Location loc = b.getLocation();
            boolean protected_ = false;
            for (Province p : provinces.values()) {
                if (p.isProtected(loc)) {
                    if (!p.explosionsAllowed) {
                        protected_ = true;
                        break;
                    }
                } else if (p.structureBlocks.contains(loc)) {
                    unclaim(p.center, "Ritual circle was destroyed by an explosion!");
                }
            }
            if (protected_) {
                it.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFireSpread(org.bukkit.event.block.BlockIgniteEvent event) {
        Location loc = event.getBlock().getLocation();
        for (Province p : provinces.values()) {
            if (p.isProtected(loc)) {
                if (!p.fireSpreadAllowed) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBurn(org.bukkit.event.block.BlockBurnEvent event) {
        Location loc = event.getBlock().getLocation();
        for (Province p : provinces.values()) {
            if (p.isProtected(loc)) {
                if (!p.fireSpreadAllowed) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    public static void updatePlayerSettings(UUID owner, org.bukkit.NamespacedKey key, boolean value) {
        for (Province p : provinces.values()) {
            if (p.owner.equals(owner)) {
                if (key.equals(xyz.yaszu.freedom.Util.FreedomKeys.provinceBlockBreak())) {
                    p.blockBreakProtected = value;
                } else if (key.equals(xyz.yaszu.freedom.Util.FreedomKeys.provinceBlockPlace())) {
                    p.blockPlaceProtected = value;
                } else if (key.equals(xyz.yaszu.freedom.Util.FreedomKeys.provinceFireSpread())) {
                    p.fireSpreadAllowed = value;
                } else if (key.equals(xyz.yaszu.freedom.Util.FreedomKeys.provinceExplosions())) {
                    p.explosionsAllowed = value;
                }
            }
        }
        saveProvinces();
    }
}
