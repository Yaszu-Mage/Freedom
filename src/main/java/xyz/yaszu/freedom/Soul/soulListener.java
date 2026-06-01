package xyz.yaszu.freedom.Soul;
import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.GUI.SelectionGUI.selectionUi;
import xyz.yaszu.freedom.Soul.Alchemy.Arcanus;
import xyz.yaszu.freedom.Soul.Alchemy.Astral;
import xyz.yaszu.freedom.Soul.Alchemy.Leaf;
import xyz.yaszu.freedom.Soul.Base.*;
import xyz.yaszu.freedom.Soul.Ultra.*;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.net.URI;
import java.util.*;

public class soulListener extends Util implements Listener {
    public static final Map<SoulTypes, Base_Soul> SOULS = new EnumMap<>(SoulTypes.class);

    public static void registerSouls() {
        SOULS.put(SoulTypes.Red, new Red());
        SOULS.put(SoulTypes.Cafe, new Cafe());
        SOULS.put(SoulTypes.Green, new Green());
        SOULS.put(SoulTypes.Black, new Black());
        SOULS.put(SoulTypes.Purple, new Purple());
        SOULS.put(SoulTypes.Mocha, new Mocha());
        SOULS.put(SoulTypes.Orange, new Orange());
        SOULS.put(SoulTypes.BaseRed, new BaseRed());
        SOULS.put(SoulTypes.BaseCafe, new BaseCafe());
        SOULS.put(SoulTypes.BaseGreen, new BaseGreen());
        SOULS.put(SoulTypes.BaseBlack, new BaseBlack());
        SOULS.put(SoulTypes.BasePurple, new BasePurple());
        SOULS.put(SoulTypes.BaseMocha, new BaseMocha());
        SOULS.put(SoulTypes.BaseOrange, new BaseOrange());
        SOULS.put(SoulTypes.Yellow, new Yellow());
        SOULS.put(SoulTypes.BaseYellow, new BaseYellow());
        SOULS.put(SoulTypes.BaseBlue, new BaseBlue());
        SOULS.put(SoulTypes.Blue, new Blue());
        SOULS.put(SoulTypes.Arcanus, new Arcanus());
        SOULS.put(SoulTypes.Leaf, new Leaf());
        SOULS.put(SoulTypes.Astral, new Astral());
        SOULS.put(Soultypes.BaseCyan,new BaseCyan());
    }
    //by by
    // I was thinking more custom mobs bc that shit would be cool as balls
    // what if there is a mob that uses a moveset?
    //like a shadow
    //I think it would be funny if they walk into the red castle and be like, excuse me, what the fuck why can they cast FUCKING FIREBALL
    // oh I just came up with something I am so stupid... why use mannequins when every npc can be a fucking cow or smtn
    //ugh wtv I'll work on npc system and give them abilities and combat AI
    //MAHORAGA HELPPPP HELP ME HELPPPPPPPPPPPPP
    public static Base_Soul getSoul(Player player) {
        String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
        if (soulName == null) return null;
        try {
            return SOULS.get(SoulTypes.valueOf(soulName));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void Passive(Player player) {
        if (!Life_and_Death.is_alive(player)) return;
        Base_Soul soul = getSoul(player);
        if (soul != null) {
            soul.Passive(player, null);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Send resource pack after a delay to ensure player network is ready
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                try {
                    // Resource pack URL - must be a direct link to a valid ZIP file
                    String packUrl = "https://www.dropbox.com/scl/fo/63c5re1q0w4kbcwvvr3bl/AJbxupF06JxGuMzFtThxKgQ?rlkey=32b306bpu8x7bw10kr8gd4yah&st=ubfy0ket&dl=1";
                    String baller = "https://www.dropbox.com/scl/fi/d0qlu1bcbn1slsluqizlb/build.zip?rlkey=80ko5ytz9xsndtosj49g2v0ap&st=ssmpa746&dl=1";
                    // Using the newer Paper API with ResourcePackRequest
                    try {
                        ResourcePackRequest resourcePackRequest = ResourcePackRequest.resourcePackRequest()
                                .packs(net.kyori.adventure.resource.ResourcePackInfo.resourcePackInfo()
                                        .uri(java.net.URI.create(packUrl))
                                        .hash("")  // Empty hash - server won't validate hash on client
                                        .build()
                                        ,net.kyori.adventure.resource.ResourcePackInfo.resourcePackInfo()
                                                .uri(URI.create(baller)).hash("")
                                                .build()
                                )
                                .required(false)  // Don't force - let client accept/decline
                                .prompt(net.kyori.adventure.text.Component.text("This server uses a custom resource pack"))
                                .build();
                        player.sendResourcePacks(resourcePackRequest);
                        Freedom.get_plugin().getLogger().info("Resource pack request sent to " + player.getName() + " from " + packUrl);
                    } catch (NoSuchMethodError e) {
                        // Fallback for older Paper versions
                        player.setResourcePack(packUrl, "");
                        Freedom.get_plugin().getLogger().info("Resource pack (legacy method) sent to " + player.getName());
                    }

                } catch (Exception e) {
                    Freedom.get_plugin().getLogger().warning("Failed to send resource pack to " + player.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTaskLater(Freedom.get_plugin(), 10L); // 0.5 second delay

        if (!player.getPersistentDataContainer().has(keygen("ComorAction"))) {
            player.getPersistentDataContainer().set(keygen("ComorAction"), PersistentDataType.BOOLEAN, true);
        }
        if (!player.getPersistentDataContainer().has(keygen("SoulPoint"))) {
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, 0.0);
            return;
        }
        Double soulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        if (soulPoints != null && soulPoints > 10) {
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, 10.0);
        }
        Base_Soul soul = getSoul(player);
        if (soul == null) return;
        if (!abilityOneCooldowns.containsKey(player.getUniqueId())) {
            abilityOneCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
        if (!abilityTwoCooldowns.containsKey(player.getUniqueId())) {
            abilityTwoCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onResourcePackResponse(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.ACCEPTED) {
            Freedom.get_plugin().getLogger().info(player.getName() + " accepted the resource pack");
        } else if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) {
            Freedom.get_plugin().getLogger().warning(player.getName() + " declined the resource pack");
        } else if (event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            Freedom.get_plugin().getLogger().warning(player.getName() + " failed to download the resource pack - check URL and file");
        } else if (event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            Freedom.get_plugin().getLogger().info(player.getName() + " successfully loaded the resource pack");
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getPersistentDataContainer().get(keygen("SoulPoint"),PersistentDataType.DOUBLE) < 10 && !player.isInsideVehicle()) {
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE) + 1);
            showSoulPoints(player);
        }
        }
    }




    @EventHandler
    public void onPlayerDamagedEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (player.getPersistentDataContainer().get(keygen("SoulPoint"),PersistentDataType.DOUBLE) < 10 && !player.isInsideVehicle()) {
                player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE) + 1);
                showSoulPoints(player);
            }
        }
    }



    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().has(keygen("soul"))) {
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
        } else {
            // Open the starting GUI after a small delay to ensure resource pack is sent first
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        selectionUi.open_UI(player, SOULS.get(SoulTypes.Red));
                    }
                }
            }.runTaskLater(Freedom.get_plugin(), 20L);  // 1 second delay

            player.setWalkSpeed(0);
            player.setFlySpeed(0);
        }
        if (!player.getPersistentDataContainer().has(keygen("ComorAction"))) {
            player.getPersistentDataContainer().set(keygen("ComorAction"), PersistentDataType.BOOLEAN, true);
        }
    }





    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!abilityOneCooldowns.containsKey(player.getUniqueId())) {
            abilityOneCooldowns.put(player.getUniqueId(), 0L);
        }
        if (!abilityTwoCooldowns.containsKey(player.getUniqueId())) {
            abilityTwoCooldowns.put(player.getUniqueId(), 0L);
        }
        showSoulPoints(event.getPlayer());
    }





    public static void showSoulPoints(Player player) {
        if (!player.getPersistentDataContainer().has(keygen("SoulPoint"))) {
            player.getPersistentDataContainer().set(keygen("SoulPoint"),PersistentDataType.DOUBLE,0d);
        }
        double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        for (BossBar bossBar : player.activeBossBars() ) {
            if (bossBar.name().toString().contains("SoulPoints")) {
                player.hideBossBar(bossBar);
            }

        }
        Integer life = player.getPersistentDataContainer().get(keygen("life"),PersistentDataType.INTEGER);
        if (life == null) {
            player.getPersistentDataContainer().set(keygen("life"),PersistentDataType.INTEGER,9);
        }
        open(player, (int) SoulPoints,life);
    }


    @EventHandler
    public void enableActivePassives(PlayerArmSwingEvent event) {
        if (!Life_and_Death.is_alive(event.getPlayer())) return;
        Player player = event.getPlayer();
        if (!player.getPersistentDataContainer().getOrDefault(FreedomKeys.comorAction(), PersistentDataType.BOOLEAN, true)) {
            return;
        }

        applyMovesetPassives(player);

        if (!player.getPersistentDataContainer().has(FreedomKeys.soul()) ||
                (!player.isSneaking()) ||
                !player.getPersistentDataContainer().has(FreedomKeys.soulPoint())) {
            return;
        }

        Base_Soul soul = getSoul(player);
        if (soul == null) return;

        double soulPoints = player.getPersistentDataContainer().getOrDefault(FreedomKeys.soulPoint(), PersistentDataType.DOUBLE, 0.0);

        // Special handling for some souls in enableActivePassives
        String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
        if (soulName != null) {
            if (soulName.contains("Red") || soulName.contains("Green")) {
                if (soulPoints >= 5) {
                    soul.ActivePassive(player);
                }
                return;
            } else if (soulName.toLowerCase().contains("black")) {
                Freedom.get_plugin().getLogger().info(soulPoints + " " +player.isSneaking());
                if (soulPoints >= 5 && player.isSneaking()) {
                    Freedom.get_plugin().getLogger().info("Checking");
                    soul.playerSneakEvent(player);
                }
                return;
            }
        }
        // Default behavior for other souls
        soul.ActivePassive(player);
    }

    private ItemStack findItemInHand(Player player, String key) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getPersistentDataContainer().has(keygen(key))) {
            return mainHand;
        }
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getPersistentDataContainer().has(keygen(key))) {
            if (keygen(key).equals(keygen("rifle"))) return null;
            return offHand;
        }
        return null;
    }

    private SoulTypes getOwnedSoulType(Player player) {
        String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
        if (soulName == null) return null;
        try {
            return SoulTypes.valueOf(soulName);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private Base_Soul getImbueLiteSoul(Player player) {
        SoulTypes type = getOwnedSoulType(player);
        if (type == null) return null;
        return SOULS.get(type.toBaseVariant());
    }


    public void AbilityOne(Player player) {
        Base_Soul soul = getSoul(player);
        if (soul == null) return;

        if (!Life_and_Death.is_alive(player)) {
            Player imbueHolder = soul.getImbuePlayer(player);
            Base_Soul liteSoul = getImbueLiteSoul(player);
            if (imbueHolder != null && liteSoul != null) {
                liteSoul.AbilityOneLite(imbueHolder, player);
            }
            return;
        }

        String soulName = soul.Name_For_Container();
        if (soulName.contains("Yellow") ||  soulName.contains("Blue")) {
            if (findItemInHand(player, "timepiece") != null) {
                soul.AbilityOne(player);
            }
            return;
        }
        soul.AbilityOne(player);
    }

    @EventHandler
    public void joinAndHeal(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Base_Soul soul = getSoul(player);
        if (soul != null) {
            String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
            if (soulName != null && soulName.contains("Green")) {
                soul.Passive(player, event);
            }
        }
    }

    public void AbilityTwo(Player player) throws MineSkinException, DataRequestException {

        Base_Soul soul = getSoul(player);
        if (soul == null) return;

        if (!Life_and_Death.is_alive(player)) {
            Player imbueHolder = soul.getImbuePlayer(player);
            Base_Soul liteSoul = getImbueLiteSoul(player);
            if (imbueHolder != null && liteSoul != null) {
                ItemStack imbuedItem = liteSoul.getOwnersImbuedItemInHand(imbueHolder, player);
                if (imbuedItem != null) {
                    liteSoul.AbilityTwoLite(imbueHolder, player, imbuedItem);
                }
            }
            return;
        }

        player.sendActionBar(dess("<green>Ability Two</green>"));

        String soulName = soul.Name_For_Container();
        if (soulName != null) {
            if (soulName.contains("Red") || soulName.contains("Yellow") || soulName.contains("Blue")) {
                ItemStack item = findItemInHand(player, "timepiece");
                if (item != null) {
                    soul.AbilityTwo(player, item);
                }

                return;
            } else if (soulName.contains("Purple")) {
                ItemStack item = findItemInHand(player, "rifle");
                if (item != null) {
                    soul.AbilityTwo(player, item);
                }
                return;
            }
        }

        // For others, use first item slot as per original logic
        soul.AbilityTwo(player, player.getInventory().getItem(0));
    }
    public void ActivePassive(Player player) {
        Base_Soul soul = getSoul(player);
        if (soul == null) return;
        if (!Life_and_Death.is_alive(player)) return;
        double soulPoints = player.getPersistentDataContainer().getOrDefault(FreedomKeys.soulPoint(), PersistentDataType.DOUBLE, 0.0);
        String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);

        if (soulName != null && soulName.toLowerCase().contains("black")) {
            if (soulPoints >= 5 && player.isSneaking()) {
                soul.playerSneakEvent(player);
            }
        } else {
            Freedom.get_plugin().getLogger().info(soulName);
            soul.ActivePassive(player);
        }
    }

    @EventHandler
    public void onPlayerXPgain(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
        if ("Purple".equals(soulName)) {
            event.setAmount(event.getAmount() * 4);
        } else if ("BasePurple".equals(soulName)) {
            event.setAmount(event.getAmount() * 2);
        }
    }

    @EventHandler
    public void AbilityOneListener(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().getOrDefault(FreedomKeys.comorAction(), PersistentDataType.BOOLEAN, true)) {
            Base_Soul soul = getSoul(player);
            if (soul == null) return;

            if (!Life_and_Death.is_alive(player) && soul.ImbueActive(player)) {
                AbilityOne(player);
                return;
            }

            String soulName = soul.Name_For_Container();
            if (soulName.contains("Yellow")) {
                if (findItemInHand(player, "timepiece") != null) {
                    AbilityOne(player);
                }
                return;
            }

            if (soulName != null && soulName.contains("Orange")) {
                AbilityOne(player);
            } else if (player.isSneaking()) {
                AbilityOne(player);
            }
        }
    }

    @EventHandler
    public void AbilityTwoListener(PlayerDropItemEvent event) throws MineSkinException, DataRequestException {
        Player player = event.getPlayer();
        ItemStack drop = event.getItemDrop().getItemStack();
        if (player.getPersistentDataContainer().getOrDefault(FreedomKeys.comorAction(), PersistentDataType.BOOLEAN, true)) {
            Base_Soul soul = getSoul(player);
            if (soul == null) return;

            if (!Life_and_Death.is_alive(player) && soul.ImbueActive(player)) {
                AbilityTwo(player);
                event.setCancelled(true);
                return;
            }

            String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
            if (soulName != null) {
                if ((soulName.contains("Red") || soulName.contains("Yellow") || soulName.contains("Blue")) && drop.getPersistentDataContainer().has(keygen("timepiece"))) {
                    soul.AbilityTwo(player, drop);
                    event.setCancelled(true);
                    return;
                } else if (soulName.contains("Purple") && drop.getPersistentDataContainer().has(keygen("rifle"))) {
                    soul.AbilityTwo(player, drop);
                    event.setCancelled(true);
                    return;
                }
            }

            soul.AbilityTwo(player, player.getInventory().getItem(0));
        }
    }

    @EventHandler
    public void activateAttackPassive(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        if (!Life_and_Death.is_alive(event.getPlayer())) return;
        Base_Soul soul = getSoul(player);

        applyMovesetAttackPassives(player, event);

        if (soul == null) return;

        String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
        if (soulName != null && soulName.contains("Red")) {
            soul.Passive(player, event);
        }
    }

    private static class MovesetData {
        Set<String> elements = new HashSet<>();
        int amp = 0;
        int range = 0;
    }

    private MovesetData parseMoveset(Player player) {
        String moveset = player.getPersistentDataContainer().get(FreedomKeys.moveset(), PersistentDataType.STRING);
        if (moveset == null) return null;

        MovesetData data = new MovesetData();
        if (!moveset.contains(";")) {
            // Old format
            data.elements.addAll(Arrays.asList(moveset.split(",")));
            return data;
        }

        String[] parts = moveset.split(";");
        for (String part : parts) {
            String[] kv = part.split(":");
            if (kv.length != 2) continue;
            try {
                switch (kv[0]) {
                    case "elements" -> data.elements.addAll(Arrays.asList(kv[1].split(",")));
                    case "amp" -> data.amp = Integer.parseInt(kv[1]);
                    case "range" -> data.range = Integer.parseInt(kv[1]);
                }
            } catch (NumberFormatException ignored) {}
        }
        return data;
    }

    private void applyMovesetPassives(Player player) {
        MovesetData data = parseMoveset(player);
        if (data == null) return;

        Set<String> elementSet = data.elements;
        int amp = data.amp;
        int range = data.range;

        // Basic Elements
        if (elementSet.contains("fire")) player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, amp));
        if (elementSet.contains("water")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 60, amp));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 60, amp));
        }
        if (elementSet.contains("earth")) player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, amp));
        if (elementSet.contains("air")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1 + amp));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 60, 1 + amp));
        }
        if (elementSet.contains("soul")) player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 60, 1 + amp));
        if (elementSet.contains("poison")) player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60, amp));
        if (elementSet.contains("wither")) player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60, 1 + amp));

        // Combinations
        if (elementSet.contains("fire") && elementSet.contains("water")) {
            // Steam: Invisibility + Smoke
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
            player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE, player.getLocation(), 5 + range, 0.5, 0.5, 0.5, 0.05);
        }
        if (elementSet.contains("fire") && elementSet.contains("earth")) {
            // Magma: Strength + Fire Resistance
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, amp));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 1 + amp));
        }
        if (elementSet.contains("water") && elementSet.contains("earth")) {
            // Mud: Slow nearby entities
            for (org.bukkit.entity.Entity e : player.getNearbyEntities(5 + range, 5 + range, 5 + range)) {
                if (e instanceof org.bukkit.entity.LivingEntity le && le != player) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1 + amp));
                }
            }
        }
        if (elementSet.contains("air") && elementSet.contains("soul")) {
            // Spirit: Regeneration + Levitation (briefly or slow fall)
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, amp));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, amp));
        }
        if (elementSet.contains("soul") && elementSet.contains("fire")) {
            // Cursed Fire: Strength II + Speed II
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 1 + amp));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1 + amp));
        }
        if (elementSet.contains("fire") && elementSet.contains("air")) {
            // Lightning: Speed II + Haste
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1 + amp));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60, amp));
        }
        if (elementSet.contains("water") && elementSet.contains("air")) {
            // Ice: Resistance + Night Vision
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, amp));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0));
        }
        if (elementSet.contains("earth") && elementSet.contains("soul")) {
            // Golem: Resistance II + Slowness (heavy but strong)
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1 + amp));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
        }
        if (elementSet.contains("poison") && elementSet.contains("water")) {
            // Acid: Wither nearby
            for (org.bukkit.entity.Entity e : player.getNearbyEntities(5 + range, 5 + range, 5 + range)) {
                if (e instanceof org.bukkit.entity.LivingEntity le && le != player) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, amp));
                }
            }
        }
        if (elementSet.contains("wither") && elementSet.contains("air")) {
            // Void: Levitation nearby
            for (org.bukkit.entity.Entity e : player.getNearbyEntities(5 + range, 5 + range, 5 + range)) {
                if (e instanceof org.bukkit.entity.LivingEntity le && le != player) {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, amp));
                }
            }
        }
    }

    private void applyMovesetAttackPassives(Player player, PrePlayerAttackEntityEvent event) {
        MovesetData data = parseMoveset(player);
        if (data == null) return;

        Set<String> elementSet = data.elements;
        int amp = data.amp;

        if (elementSet.contains("fire")) {
            event.getAttacked().setFireTicks(100 + (amp * 20));
        }
        if (elementSet.contains("water")) {
            if (event.getAttacked() instanceof org.bukkit.entity.LivingEntity le) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40 + (amp * 20), amp));
            }
        }
        if (elementSet.contains("earth")) {
            event.getAttacked().setVelocity(event.getAttacked().getVelocity().add(new org.bukkit.util.Vector(0, 0.5 + (amp * 0.2), 0)));
        }
        if (elementSet.contains("air")) {
            org.bukkit.util.Vector dir = player.getLocation().getDirection().normalize().multiply(1.5 + (amp * 0.5));
            event.getAttacked().setVelocity(event.getAttacked().getVelocity().add(dir));
        }
        if (elementSet.contains("soul")) {
            if (player.getHealth() < player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()) {
                player.setHealth(Math.min(player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue(), player.getHealth() + 1 + amp));
            }
        }
        if (elementSet.contains("poison")) {
            if (event.getAttacked() instanceof org.bukkit.entity.LivingEntity le) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100 + (amp * 20), amp));
            }
        }
        if (elementSet.contains("wither")) {
            if (event.getAttacked() instanceof org.bukkit.entity.LivingEntity le) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100 + (amp * 20), amp));
            }
        }
        if (elementSet.contains("fire") && elementSet.contains("air")) {
            // Lightning: Strike lightning on attack (purely visual?) or extra damage
            event.getAttacked().getWorld().strikeLightningEffect(event.getAttacked().getLocation());
            if (event.getAttacked() instanceof org.bukkit.entity.LivingEntity le) {
                le.damage(2.0 + (amp * 1.0));
            }
        }
        if (elementSet.contains("water") && elementSet.contains("air")) {
            // Ice: High slowness
            if (event.getAttacked() instanceof org.bukkit.entity.LivingEntity le) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60 + (amp * 20), 2 + amp));
            }
        }
    }
}
