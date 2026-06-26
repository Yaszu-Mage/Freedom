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

import static xyz.yaszu.freedom.Util.Util.*;

public class soulListener implements Listener {
    public static final Map<SoulTypes, Base_Soul> SOULS = new EnumMap<>(SoulTypes.class);

    /**
     * Registers all predefined soul-type instances to the SOULS map.
     * Each soul type defined in the {@code SoulTypes} enumeration is
     * associated with a corresponding instance of its specific implementation.
     *
     * This method is static and initializes the mapping of soul types to their
     * respective implementations, such as {@code Red}, {@code Green}, and
     * {@code Blue}, among others. The mapping includes both base variants
     * (e.g., {@code BaseRed}, {@code BaseGreen}) and main variants of soul types.
     *
     * Once this method has been invoked, the {@code SOULS} map will contain
     * all predefined relationship bindings between {@code SoulTypes} and their
     * respective concrete implementations.
     */
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
        SOULS.put(SoulTypes.BaseCyan,new BaseCyan());
    }
    public static boolean canAbility = true;

    /**
     * Retrieves the soul associated with the given player based on their persistent data.
     *
     * This method attempts to find the player's soul by querying the {@code PersistentDataContainer}
     * of the provided {@code Player} object for a stored soul identifier. If a valid soul identifier
     * is found, it is used to determine the corresponding soul instance from the predefined
     * {@code SOULS} map. If no identifier is found or if it cannot be resolved to a valid soul,
     * the method returns {@code null}.
     *
     * @param player the player whose soul is to be retrieved; must not be {@code null}
     * @return the {@code Base_Soul} instance associated with the player, or {@code null} if no valid
     *         soul is found for the player
     */
    public static Base_Soul getSoul(Player player) {
        String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
        if (soulName == null) return null;
        try {
            return SOULS.get(SoulTypes.valueOf(soulName));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Handles the passive abilities for a player if they are alive and have an associated soul.
     * This method checks whether the provided player is alive using {@code Life_and_Death.is_alive}.
     * If the player is alive, their associated soul is obtained via {@code getSoul(Player)}.
     * If a valid soul instance is found, the {@code Passive} method of the soul is invoked.
     *
     * @param player the player for whom the passive ability is executed; must not be null
     */
    public void Passive(Player player) {
        if (!Life_and_Death.is_alive(player)) return;
        Base_Soul soul = getSoul(player);
        if (soul != null) {
            soul.Passive(player, null);
        }
    }

    /**
     * Initial Player Join Function, it sends ResourcePack, then sets default values such as ComorAction, Soulpoints, and Ability One Cooldowns
     * @param event Actual Player Join Event, this is called whenever a player Joins
     */
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
        if (soul != null || canAbility == false) {
            String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
            if (soulName != null && soulName.contains("Green")) {
                soul.Passive(player, event);
            }
        }
    }


    /**
     * Telemetry Data to know about Resource Pack Responses
     * @param event Event that is called during the ResourcePack Status update
     */
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

    /**
     * Player Damage SoulPoints: Whenever a player is damaged their soul points go up
     * @param event Event that is called during the Entity Damage Event Update
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getPersistentDataContainer().get(keygen("SoulPoint"),PersistentDataType.DOUBLE) < 10 && !player.isInsideVehicle()) {
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE) + 1);
            showSoulPoints(player);
        }
        }
    }


    /**
     * Entity Damage Player Soulpoints: Whenever a player damages an Entity their SoulPoints go up
     * @param event Event that is called during the Entity Damaged by Entity Event Update
     */
    @EventHandler
    public void onPlayerDamagedEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (player.getPersistentDataContainer().get(keygen("SoulPoint"),PersistentDataType.DOUBLE) < 10 && !player.isInsideVehicle()) {
                player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE) + 1);
                showSoulPoints(player);
            }
        }
    }


    /**
     * Player Move Update: Whenever a player moves, this event checks if they have a soul and sets their walk and fly speed accordingly. If they don't have a soul, it opens the starting GUI after a small delay to ensure the resource pack is sent first. It also initializes the ComorAction persistent data if it doesn't exist.
     * @param event Event that is called during the Player Move Event Update
     */
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
        if (!abilityOneCooldowns.containsKey(player.getUniqueId())) {
            abilityOneCooldowns.put(player.getUniqueId(), 0L);
        }
        if (!abilityTwoCooldowns.containsKey(player.getUniqueId())) {
            abilityTwoCooldowns.put(player.getUniqueId(), 0L);
        }
        showSoulPoints(event.getPlayer());
    }


    /**
     * Shows the SoulPoints of a Given Player Within their UI, and resets it if they do not have it
     * @param player to show SoulPoints of
     */
    public static void showSoulPoints(Player player) {
        if (!player.getPersistentDataContainer().has(keygen("SoulPoint"))) {
            player.getPersistentDataContainer().set(keygen("SoulPoint"),PersistentDataType.DOUBLE,0d);
        }
        double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
//        for (BossBar bossBar : player.activeBossBars() ) {
//            if (bossBar.name().toString().contains("SoulPoints")) {
//                player.hideBossBar(bossBar);
//            }
//
//        }
        Integer life = player.getPersistentDataContainer().get(keygen("life"),PersistentDataType.INTEGER);
        if (life == null) {
            player.getPersistentDataContainer().set(keygen("life"),PersistentDataType.INTEGER,9);
        }
        open(player, (int) SoulPoints,life);
    }

    /**
     * Enable the Active Passives of a Player, this is the Event Key for the Player Arm Swing Event
     * Whenever a player swings their arm, this event checks if they have a soul and if they do, it calls the ActivePassive method of the soul. If they don't have a soul, it does nothing.
     * Also it checks ComorAction to see if the method should rlly be run
     * @param event Event that is called during the Player Arm Swing Event
     */
    @EventHandler
    public void enableActivePassives(PlayerArmSwingEvent event) {
        if (!Life_and_Death.is_alive(event.getPlayer()) || canAbility == false) return;
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

    /**
     * Finds an item in the player's hand that has the specified key.
     * @param player The player to search for the item.
     * @param key The key to check for in the item's persistent data.
     * @return The item in the player's hand that has the specified key, or null if not found.
     */
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


    /**
     * Retrieves the soul type associated with the given player based on their persistent data.
     *
     * This method attempts to resolve the player's soul type by querying their {@code PersistentDataContainer}
     * for a stored soul identifier. If the identifier is found and matches a valid {@code SoulTypes} value,
     * the corresponding {@code SoulTypes} enum is returned. If no valid identifier is found, the method returns {@code null}.
     *
     * @param player the player whose soul type is to be retrieved; must not be {@code null}
     * @return the {@code SoulTypes} value associated with the player, or {@code null}
     *         if no valid soul type identifier is found or is improperly formatted
     */
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


    /**
     * Ability One: This method is responsible for handling the Ability One functionality of a player. This is the Keyboard / Command Triggered Function
     * @param player Player to handle Ability One for
     */
    public void AbilityOne(Player player) {
        Base_Soul soul = getSoul(player);
        if (soul == null || canAbility == false) return;

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

    /**
     * Ability Two: This method is responsible for handling the Ability Two functionality of a player. This is the Keyboard / Command Triggered Function
     * @param player Player to handle Ability Two for
     * @throws MineSkinException Data Exception
     * @throws DataRequestException Data Exception
     */
    public void AbilityTwo(Player player) throws MineSkinException, DataRequestException {

        Base_Soul soul = getSoul(player);
        if (soul == null || canAbility == false) return;

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
            if (soulName.contains("Yellow") || soulName.contains("Blue")) {
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

    /**
     * Active Passive: This method is responsible for handling the Active Passive functionality of a player. This is the Keyboard / Command Triggered Function
     *
     * @param player the player triggering the active or passive ability; must not be null
     */
    public void ActivePassive(Player player) {
        Base_Soul soul = getSoul(player);
        if (soul == null || canAbility == false) return;
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

    /**
     * Event handler for when a player gains experience points.
     * @param event The event that triggered the XP gain.
     */
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


    /**
     * Ability One Listener, event that is triggered every time a player jumps, checks if they have com or action
     * @param event Event that is called during the Player Jump Event Update
     */
    @EventHandler
    public void AbilityOneListener(PlayerJumpEvent event) {
        if (canAbility == false) return;
        Player player = event.getPlayer();
        if (!player.getPersistentDataContainer().getOrDefault(FreedomKeys.comorAction(), PersistentDataType.BOOLEAN, true)) return;
        if (player.getPersistentDataContainer().getOrDefault(FreedomKeys.comorAction(), PersistentDataType.BOOLEAN, true)) {
            Base_Soul soul = getSoul(player);
            if (soul == null) return;

            if (!Life_and_Death.is_alive(player) && soul.ImbueActive(player)) {
                AbilityOne(player);
                return;
            }


            String soulName = soul.Name_For_Container();
            if (soulName.contains("Yellow") || soulName.contains("Blue") || soulName.contains("Red")) {
                if (findItemInHand(player, "timepiece") != null) {
                    AbilityOne(player);
                }
                return;
            }
            if (soulName.contains("Purple")) {
                if (findItemInHand(player, "rifle") != null) {
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

    /**
     * Handles the Ability Two action triggered by a player dropping an item.
     * This listener is invoked when a {@link PlayerDropItemEvent} is fired.
     * It checks multiple conditions, including the player's state, the soul's state,
     * and specific ability logic, to determine the appropriate actions.
     *
     * The method may cancel the event based on condition evaluations.
     *
     * @param event The {@link PlayerDropItemEvent} that represents the action of a player dropping an item.
     *              This event contains information about the player and the dropped item,
     *              which is used for evaluating the ability logic.
     * @throws MineSkinException      If there is an issue related to MineSkin operations during ability execution.
     * @throws DataRequestException   If there is an issue retrieving required data for the ability.
     */
    @EventHandler
    public void AbilityTwoListener(PlayerDropItemEvent event) throws MineSkinException, DataRequestException {
        if (canAbility == false) return;
        Player player = event.getPlayer();
        if (!player.getPersistentDataContainer().getOrDefault(FreedomKeys.comorAction(), PersistentDataType.BOOLEAN, true)) return;
        ItemStack item = event.getItemDrop().getItemStack();
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
            if (soulName.contains("Yellow") || soulName.contains("Blue")) {
                if (item != null) {
                    soul.AbilityTwo(player, item);
                    event.setCancelled(true);
                }

                return;
            } else if (soulName.contains("Purple")) {
                if (item != null) {
                    soul.AbilityTwo(player, item);
                    event.setCancelled(true);
                }
                return;
            }
        }

        // For others, use first item slot as per original logic
        soul.AbilityTwo(player, player.getInventory().getItem(0));
    }

    /**
     * Handles the activation of attack passives for a player during a pre-attack event.
     *
     * This method checks the player's state, applies any associated moveset attack
     * passives, and triggers the passive effect of the player's soul if applicable.
     *
     * @param event the PrePlayerAttackEntityEvent triggered when a player is about to attack an entity
     */
    @EventHandler
    public void activateAttackPassive(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        if (!Life_and_Death.is_alive(event.getPlayer()) || canAbility == false) return;
        Base_Soul soul = getSoul(player);

        applyMovesetAttackPassives(player, event);

        if (soul == null) return;

        String soulName = player.getPersistentDataContainer().get(FreedomKeys.soul(), PersistentDataType.STRING);
        if (soulName != null && soulName.contains("Red")) {
            soul.Passive(player, event);
        }
    }

    /**
     * Represents the data structure for a moveset, encapsulating its elements,
     * amplification factor, and range.
     *
     * This class is a helper utility for organizing and managing moveset data,
     * such as elemental types, amplification, and the range of the moveset.
     * It is designed to store data related to a specific set of moves.
     *
     * Thread-safety: This class is not thread-safe as it uses a HashSet and
     * mutable integer fields without synchronization.
     */
    private static class MovesetData {
        Set<String> elements = new HashSet<>();
        int amp = 0;
        int range = 0;
    }

    /**
     * Parses the moveset data from a given player.
     * The moveset is retrieved from the player's persistent data container.
     * It can handle both old and new formatted moveset strings.
     *
     * @param player The player whose moveset data is being parsed.
     * @return A MovesetData object containing the parsed moveset data, or null if no valid moveset data exists.
     */
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

    /**
     * Applies passive abilities to a player based on their moveset. This method determines the
     * player's elemental abilities and amplifies their power and range accordingly. If the
     * moveset elements include predefined combinations, additional effects may be applied
     * either to the player or to nearby entities.
     *
     * @param player The player to whom passive abilities will be applied. This player will
     *               receive effects based on their moveset elements and corresponding abilities.
     */
    private void applyMovesetPassives(Player player) {
        if (canAbility == false) return;
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

    /**
     * Applies passive attack effects from the player's moveset when they attack an entity.
     * The effects are based on the elements in the player's moveset and the amplification level.
     * Each element in the moveset triggers its own unique effect, with special interactions for combined elements.
     *
     * @param player The player performing the attack. This player's moveset and attributes are checked to determine the effects.
     * @param event The event triggered by the player attacking an entity. Contains information about the attacked entity and event context.
     */
    private void applyMovesetAttackPassives(Player player, PrePlayerAttackEntityEvent event) {
        if (canAbility == false) return;
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
