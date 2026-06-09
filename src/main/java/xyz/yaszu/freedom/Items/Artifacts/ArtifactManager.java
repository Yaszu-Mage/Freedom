package xyz.yaszu.freedom.Items.Artifacts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Subsystems.CustomSongHandler;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ArtifactManager extends Util implements Listener {

    public static final Map<String, Base_Artifact> ARTIFACTS = new HashMap<>();

    public static void registerArtifacts() {
        //register(new Base_Artifact("scholar", dess("<yellow>Scholar's Scroll</yellow>"), dess("<gray>Grants Luck III.</gray>"), Material.PAPER, NamespacedKey.minecraft("scholar"), List.of(new PotionEffect(PotionEffectType.LUCK, 12000, 2))));
        //register(new Base_Artifact("gourmet", dess("<gold>Gourmet's Delight</gold>"), dess("<gray>Grants Saturation.</gray>"), Material.COOKED_BEEF, NamespacedKey.minecraft("saturation"), List.of(new PotionEffect(PotionEffectType.SATURATION, 12000, 0))));
        register(new Base_Artifact("bastion", dess("<blue>Bastion's Heart</blue>"), dess("<gray>Grants Absorption II.</gray>"), Material.SHIELD, NamespacedKey.minecraft("bastionheart"), List.of(new PotionEffect(PotionEffectType.ABSORPTION, 12000, 1))));
        //register(new Base_Artifact("ambassador", dess("<green>Ambassador's Seal</green>"), dess("<gray>Grants Hero of the Village.</gray>"), Material.EMERALD, NamespacedKey.minecraft("ambassadors"), List.of(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 12000, 0))));
        //register(new Base_Artifact("lumi", dess("<white>Lumi's Essence</white>"), dess("<gray>Grants Glowing.</gray>"), Material.GLOWSTONE_DUST, NamespacedKey.minecraft("lumi"), List.of(new PotionEffect(PotionEffectType.GLOWING, 12000, 0))));
        //register(new Base_Artifact("wind", dess("<aqua>Gusty Feather</aqua>"), dess("<gray>Grants Wind Charged.</gray>"), Material.FEATHER, NamespacedKey.minecraft("wind"), List.of(new PotionEffect(PotionEffectType.WIND_CHARGED, 12000, 0))));
        //register(new Base_Artifact("spider", dess("<dark_gray>Silk Weaver</dark_gray>"), dess("<gray>Grants Weaving.</gray>"), Material.COBWEB, NamespacedKey.minecraft("silk"), List.of(new PotionEffect(PotionEffectType.WEAVING, 12000, 0))));
        //register(new Base_Artifact("slime", dess("<green>Sticky Core</green>"), dess("<gray>Grants Oozing.</gray>"), Material.SLIME_BALL, NamespacedKey.minecraft("slime"), List.of(new PotionEffect(PotionEffectType.OOZING, 12000, 0))));
        //register(new Base_Artifact("hive", dess("<yellow>Bug Nest</yellow>"), dess("<gray>Grants Infested.</gray>"), Material.BEE_NEST, NamespacedKey.minecraft("hive"), List.of(new PotionEffect(PotionEffectType.INFESTED, 12000, 0))));
        //register(new Base_Artifact("tide", dess("<blue>Tidal Prism</blue>"), dess("<gray>Grants Conduit Power.</gray>"), Material.PRISMARINE_SHARD, NamespacedKey.minecraft("tide"), List.of(new PotionEffect(PotionEffectType.CONDUIT_POWER, 12000, 0))));
        register(new Base_Artifact("fishbowl", dess("<aqua>Small Fish Bowl</aqua>"), dess("<gray>Increases the odds of fishing a rare drop by 30%.</gray>"), Material.FLOWER_POT, NamespacedKey.minecraft("fishbowl"), List.of()));
        //register(new Base_Artifact("chronos", dess("<light_purple>Chronos' Hourglass</light_purple>"), dess("<gray>Reduces soul ability cooldowns by 30%.</gray>"), Material.CLOCK, NamespacedKey.minecraft("chronos"), List.of()));
    }

    private static void register(Base_Artifact artifact) {
        ARTIFACTS.put(artifact.getID(), artifact);
    }

    @EventHandler
    public void onSleep(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        // Check if they were in bed long enough or if it's morning
        // player.isDeeplySleeping() is true if they slept through the night
        if (player.isDeeplySleeping() || (player.getWorld().getTime() >= 0 && player.getWorld().getTime() <= 1000)) {
            List<String> activeIds = new ArrayList<>();
            for (Base_Artifact artifact : ARTIFACTS.values()) {
                if (artifact.hasArtifact(player)) {
                    activeIds.add(artifact.getID());
                    // Apply effects immediately
                    for (PotionEffect effect : artifact.getBuffs()) {
                        player.addPotionEffect(effect);
                    }
                }
            }
            if (!activeIds.isEmpty()) {
                player.getPersistentDataContainer().set(FreedomKeys.activeArtifact(), PersistentDataType.STRING, String.join(",", activeIds));
                Component msg = dess("<green>You feel well rested thanks to your:</green>");
                for (int i = 0; i < activeIds.size(); i++) {
                    Base_Artifact art = ARTIFACTS.get(activeIds.get(i));
                    if (art != null) {
                        msg = msg.append(art.Name());
                        if (i < activeIds.size() - 1) {
                            msg = msg.append(Component.text(", "));
                        }
                    }
                }
                player.sendMessage(msg);
            }
        }
    }

    public void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Freedom.get_plugin().getServer().getOnlinePlayers()) {
                    String activeData = player.getPersistentDataContainer().get(FreedomKeys.activeArtifact(), PersistentDataType.STRING);
                    if (activeData != null) {
                        List<String> activeIds = new ArrayList<>(Arrays.asList(activeData.split(",")));
                        List<String> remainingIds = new ArrayList<>();
                        boolean changed = false;

                        for (String id : activeIds) {
                            Base_Artifact artifact = ARTIFACTS.get(id);
                            if (artifact != null && artifact.hasArtifact(player)) {
                                // Maintain effects
                                for (PotionEffect buff : artifact.getBuffs()) {
                                    PotionEffect current = player.getPotionEffect(buff.getType());
                                    if (current == null || current.getDuration() < 100) {
                                        player.addPotionEffect(buff);
                                    }
                                }
                                remainingIds.add(id);
                            } else {
                                // Artifact lost, removed, or doesn't exist anymore
                                if (artifact != null) {
                                    for (PotionEffect buff : artifact.getBuffs()) {
                                        player.removePotionEffect(buff.getType());
                                    }
                                    player.sendMessage(dess("<red>The blessing of " + artifact.getID() + " has faded as it is no longer with you.</red>"));
                                }
                                changed = true;
                            }
                        }

                        if (changed) {
                            if (remainingIds.isEmpty()) {
                                player.getPersistentDataContainer().remove(FreedomKeys.activeArtifact());
                            } else {
                                player.getPersistentDataContainer().set(FreedomKeys.activeArtifact(), PersistentDataType.STRING, String.join(",", remainingIds));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 20L, 20L);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Mob)) return;
        if (event.getEntity().getKiller() == null) return;
        Random random = new Random();
        if (random.nextFloat() < 0.05) {
            boolean DiskArtifact = random.nextBoolean();
            if (DiskArtifact) {
                List<String> ids = new ArrayList<>(ARTIFACTS.keySet());
                String chosen = ids.get(random.nextInt(ids.size()));
                Base_Artifact artifact = ARTIFACTS.get(chosen);
                if (artifact != null) {
                    event.getDrops().add(artifact.item());
                }
            } else {
                CustomSongHandler.CustomSong[] ids = CustomSongHandler.CustomSong.values();
                CustomSongHandler.CustomSong chosen;
                try {
                    chosen = ids[random.nextInt(ids.length)];
                } catch (Exception e) {
                    chosen = CustomSongHandler.CustomSong.third_sanctuary;
                }
                CustomSongHandler.constructSong(chosen);
                event.getDrops().add(CustomSongHandler.constructSong(chosen));
            }

        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Random random = new Random();
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!(event.getCaught() instanceof Item itemEntity)) return;

        Player player = event.getPlayer();
        String activeData = player.getPersistentDataContainer().get(FreedomKeys.activeArtifact(), PersistentDataType.STRING);
        if (activeData == null) return;
        List<String> activeList = Arrays.asList(activeData.split(","));
        if (!activeList.contains("fishbowl")) return;

        ItemStack caught = itemEntity.getItemStack();
        if (isTreasure(caught)) return;
        float baller = random.nextFloat(0,1);
        Freedom.get_plugin().getLogger().info(baller + " Rated as a fishbowl catcher.");
        if (baller > 0.75) {
            ItemStack newTreasure = generateFishingTreasure(random);
            itemEntity.setItemStack(newTreasure);
            player.sendMessage(dess("<aqua>Your Fish Bowl luck kicked in!</aqua>"));
        }
    }

    private ItemStack generateFishingTreasure(Random random) {
        int roll = random.nextInt(6);
        ItemStack item;
        switch (roll) {
            case 0:
                item = new ItemStack(Material.NAME_TAG);
                break;
            case 1:
                item = new ItemStack(Material.SADDLE);
                break;
            case 2:
                item = new ItemStack(Material.BOW);
                item = item.enchantWithLevels(30, true, random);
                applyRandomDamage(item, 0.25f, random);
                break;
            case 3:
                item = new ItemStack(Material.FISHING_ROD);
                item = item.enchantWithLevels(30, true, random);
                applyRandomDamage(item, 0.25f, random);
                break;
            case 4:
                item = new ItemStack(Material.BOOK);
                item = item.enchantWithLevels(30, true, random);
                if (item.getType() == Material.BOOK && !item.getEnchantments().isEmpty()) {
                    ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
                    org.bukkit.inventory.meta.EnchantmentStorageMeta meta = (org.bukkit.inventory.meta.EnchantmentStorageMeta) enchantedBook.getItemMeta();
                    if (meta != null) {
                        item.getEnchantments().forEach((ench, lvl) -> meta.addStoredEnchant(ench, lvl, true));
                        enchantedBook.setItemMeta(meta);
                        item = enchantedBook;
                    }
                }
                break;
            case 5:
            default:
                item = new ItemStack(Material.NAUTILUS_SHELL);
                break;
        }
        return item;
    }

    private void applyRandomDamage(ItemStack item, float maxDamagePercent, Random random) {
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof org.bukkit.inventory.meta.Damageable damageable)) return;
        int maxDurability = item.getType().getMaxDurability();
        int damage = (int) (random.nextFloat() * maxDamagePercent * maxDurability);
        damageable.setDamage(damage);
        item.setItemMeta(damageable);
    }

    private boolean isTreasure(ItemStack item) {
        Material type = item.getType();
        return type == Material.ENCHANTED_BOOK || type == Material.NAME_TAG || type == Material.NAUTILUS_SHELL
                || type == Material.SADDLE || type == Material.BOW || type == Material.FISHING_ROD;
    }
}
