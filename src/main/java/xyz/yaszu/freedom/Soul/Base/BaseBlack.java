package xyz.yaszu.freedom.Soul.Base;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Soul.Ultra.Black;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Util.Util;
import static xyz.yaszu.freedom.Util.Util.*;
import java.util.*;

/**
 * Black Moveset
 */
public class BaseBlack implements Base_Soul, Listener {
    /**
     * Name Used in Components
     */
    @Override
    public String Name_For_Container() {
        return "BaseBlack";
    }
    /**
     * //Name Used in UI / Nametag
     * @return Component used in UI and Nametag
     */
    @Override
    public Component Name() {
        return dess("<shadow:#000000FF><b><yellow><gradient:#0f000f:#555555:#aa00aa>Black</gradient>");
    }
    /**
     * Description Used in UI
     * @return Component used in UI
     */
    @Override
    public Component Description() {
        return dess("Your appearance is as malleable as clay");
    }
    /**
     * Icon Used in UI
     * @return ItemStack used in UI
     */
    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.WOODEN_SHOVEL);
    }
    /**
     * Ability One Name
     * @return Component used in UI
     */
    @Override
    public Component AbilityOneName() {
        return dess("<gradient:#0f000f:#555555:#aa00aa>Ability One</gradient>Trickster");
    }
    /**
     * Description for Ability One
     * @return Component used in UI
     */
    @Override
    public Component AbilityOneDescription() {
        return dess("Teleport to a saved location, it will take you 5 seconds.");
    }
    /**
     * Ability One - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability One for
     */
    @Override
 public void AbilityOne(Player player) {
        AbilityOne(player, false);
    }
    /**
     * Ability One - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability One for
     * @param is_imbue If the ability is imbued, it will not be triggered by a player input.
     * Constructs a Menu, that has 9 slots to allow for saving and deleting saved locations, and teleporting to said locations
     */
    @Override
 public void AbilityOne(Player player, boolean is_imbue) {
        BlackInformation blackInformation = BlackInformation.people.getOrDefault(player.getUniqueId(),new BlackInformation());
        BlackInformation.BlackMenu blackMenu = new BlackInformation.BlackMenu();
        blackMenu.constructMenu(blackInformation,deleteSlotKey);
        player.openInventory(blackMenu.getInventory());
//        if (!player.getPersistentDataContainer().has(keygen("black_save"), PersistentDataType.BOOLEAN)) {
//            save(player);
//            return;
//        }
//        if (player.getPersistentDataContainer().get(keygen("black_save"), PersistentDataType.BOOLEAN)) {
//
//            load(player);
//        }
    }

    /**
     * Load Function for OLD Ability one
     * Loads COORDINATES saved within the Persistent Data Container
     * @param player to load location from
     * @deprecated
     */
    @Deprecated
    public void load(Player player) {

        //VFX
        try {
            new BukkitRunnable() {

                @Override
                public void run() {
                    if (!player.getPersistentDataContainer().has(keygen("blacksave"))) {
                        this.cancel();
                    }
                    Location loadingLocation = new Location(
                            Bukkit.getWorld(player.getPersistentDataContainer().get(keygen("blackworld"),PersistentDataType.STRING)),
                            player.getPersistentDataContainer().get(keygen("blacksaveX"),PersistentDataType.DOUBLE),
                            player.getPersistentDataContainer().get(keygen("blacksaveY"),PersistentDataType.DOUBLE),
                            player.getPersistentDataContainer().get(keygen("blacksaveZ"),PersistentDataType.DOUBLE)

                    );
                    if (loadingLocation != null) {
                        if (loadingLocation.distanceSquared(player.getLocation()) >= 10000) {
                            player.sendMessage(dess("Too Far! (100 blocks)"));
                            player.getPersistentDataContainer().remove(keygen("black_save"));
                            player.getPersistentDataContainer().remove(keygen("blacksaveX"));
                            player.getPersistentDataContainer().remove(keygen("blacksaveY"));
                            player.getPersistentDataContainer().remove(keygen("blacksaveZ"));
                            player.getPersistentDataContainer().remove(keygen("blackworld"));
                            this.cancel();
                            return;
                        }
                    }
                    drawCircle(player.getLocation(),1,player.getWorld(),16,Particle.SMOKE);
                    drawCircle(player.getLocation(),0.5,player.getWorld(),8,Particle.LARGE_SMOKE);
                    drawCircle(loadingLocation,1,player.getWorld(),16,Particle.SMOKE);
                    drawCircle(loadingLocation,0.5,player.getWorld(),8,Particle.DRAGON_BREATH);
                }
            }.runTaskTimer(Freedom.get_plugin(),20,20);
        } catch (Exception ignored) {}
        try {
            new BukkitRunnable() {
                public int tick = 0;
                public double last_health = player.getHealth();
                Location loadingLocation = player.getLocation();
                @Override
                public void run() {
                    if (loadingLocation != null) {
                        if (loadingLocation.distanceSquared(player.getLocation()) >= 10000) {
                            player.sendMessage(dess("Too Far! (100 blocks)"));
                            player.getPersistentDataContainer().remove(keygen("black_save"));
                            player.getPersistentDataContainer().remove(keygen("blacksaveX"));
                            player.getPersistentDataContainer().remove(keygen("blacksaveY"));
                            player.getPersistentDataContainer().remove(keygen("blacksaveZ"));
                            player.getPersistentDataContainer().remove(keygen("blackworld"));
                            this.cancel();
                            return;
                        }
                    }
                    if (player.getPersistentDataContainer().has(keygen("blackworld"),PersistentDataType.STRING)) {
                        Location loadingLocation = new Location(
                                Bukkit.getWorld(player.getPersistentDataContainer().get(keygen("blackworld"),PersistentDataType.STRING)),
                                player.getPersistentDataContainer().get(keygen("blacksaveX"),PersistentDataType.DOUBLE),
                                player.getPersistentDataContainer().get(keygen("blacksaveY"),PersistentDataType.DOUBLE),
                                player.getPersistentDataContainer().get(keygen("blacksaveZ"),PersistentDataType.DOUBLE)
                        );
                    }

                    if (player.getHealth() > last_health || !player.isSneaking() || player.isDead()) {
                        this.cancel();
                        player.sendActionBar(dess("Teleport Cancelled."));
                    }
                    if (tick >= 4) {
                        drawCircle(player.getLocation(),1,player.getWorld(),32,Particle.GUST);
                        player.playSound(player.getLocation(),Sound.ENTITY_WIND_CHARGE_WIND_BURST,1,1);
                        List<Entity> entities = player.getNearbyEntities(2,2,2);
                        for (Entity entity : entities) {
                            if (entity instanceof Player trustcheck) {
                                if (player.getPersistentDataContainer().has(keygen("trustedby"))) {
                                    String trustedby = trustcheck.getPersistentDataContainer().get(keygen("trustedby"),PersistentDataType.STRING);
                                    if (trustedby.contains(player.getName())) {
                                        drawCircle(trustcheck.getLocation(),1,player.getWorld(),32,Particle.REVERSE_PORTAL);
                                        trustcheck.teleport(loadingLocation);
                                        drawCircle(loadingLocation,1,loadingLocation.getWorld(),32,Particle.REVERSE_PORTAL);
                                        player.getWorld().playSound(player.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,1,1);
                                    }

                                }
                            }
                        }
                        player.teleport(loadingLocation);
                        drawCircle(loadingLocation,1,loadingLocation.getWorld(),32,Particle.REVERSE_PORTAL);
                        player.playSound(player.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,1,1);
                        this.cancel();
                    } else {
                        if (threes(tick) == 3) {
                            player.sendActionBar(dess("Teleporting in " + (4-tick) + " seconds..."));
                        } else if (threes(tick) == 2) {
                            player.sendActionBar(dess("Teleporting in " + (4-tick) + " seconds.."));
                        } else {
                            player.sendActionBar(dess("Teleporting in " + (4-tick) + " seconds."));
                        }
                        drawCircle(player.getLocation(),1.5,player.getWorld(),32,Particle.SOUL_FIRE_FLAME);
                        drawCircle(player.getLocation(),1,player.getWorld(),32,Particle.VAULT_CONNECTION);
                        drawCircle(loadingLocation,1,player.getWorld(),32,Particle.VAULT_CONNECTION);
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO,SoundCategory.PLAYERS,1,tick);
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL,SoundCategory.PLAYERS,1,tick-1);
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.0f, 0.5f);
                    }
                    tick = tick + 1;
                }
                @Override
                public synchronized void cancel() throws IllegalStateException {
                    player.getPersistentDataContainer().remove(keygen("black_save"));
                    player.getPersistentDataContainer().remove(keygen("blacksaveX"));
                    player.getPersistentDataContainer().remove(keygen("blacksaveY"));
                    player.getPersistentDataContainer().remove(keygen("blacksaveZ"));
                    player.getPersistentDataContainer().remove(keygen("blackworld"));
                    Bukkit.getScheduler().cancelTask(getTaskId());
                }
            }.runTaskTimer(Freedom.get_plugin(),0,20);
        } catch (Exception ignored) {}
    }
    public static double threes(int num) {
        if (num % 3 == 0) {
            return 3;
        }
        if (num % 2 == 0) {
            return 2;
        }
        return 1;
    }

    /**
     * Boolean to Check if the number given is a multiple of twenty
     * @param num Number to check if it is a multiple of twenty
     * @return if the number is a multiple of twenty
     */
    public static boolean isMultipleofTwenty(int num) {
        // The condition (num % 10 == 0) is true if the remainder is 0.
        return (num % 20 == 0);
    }

    /**
     * Saves the player's location to the persistent data container.
     * @param player Player to save the location to.
     * @deprecated
     */
    @Deprecated
    public void save(Player player) {
        player.getPersistentDataContainer().set(keygen("black_save"), PersistentDataType.BOOLEAN, true);
        player.getPersistentDataContainer().set(keygen("blacksaveX"), PersistentDataType.DOUBLE, player.getLocation().getX());
        player.getPersistentDataContainer().set(keygen("blacksaveY"), PersistentDataType.DOUBLE, player.getLocation().getY());
        player.getPersistentDataContainer().set(keygen("blacksaveZ"), PersistentDataType.DOUBLE, player.getLocation().getZ());
        player.getPersistentDataContainer().set(keygen("blackworld"), PersistentDataType.STRING, player.getWorld().getName());
        player.sendActionBar(dess("Saved Location at (" + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + ")"));
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick > 8) {
                    this.cancel();
                }
                if (tick == 0) {
                    player.getWorld().playSound(player.getLocation(),Sound.BLOCK_NOTE_BLOCK_GUITAR,1,1 + Math.min((tick/10),0));
                }
                if (tick % 5 == 0) {
                    player.getWorld().playSound(player.getLocation(),Sound.BLOCK_NOTE_BLOCK_GUITAR,1,1 + Math.min((tick/10),0));
                }

                tick = tick + 1;
            }
        }.runTaskTimer(Freedom.get_plugin(),0,1);
    }

    /**
     * Related Item - An item that is required to use this ability
     * @return ItemStack used in ability usage
     */
    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.ACACIA_HANGING_SIGN);
    }

    /**
     * Ability Two Name
     * @return Component used in UI
     */
    @Override
    public Component AbilityTwoName() {
        return dess("<gradient:#0f000f:#555555:#aa00aa>Ability Two:<gradient>Mimicry");
    }
    /**
     * Description for Ability Two
     * @return Component used in UI
     */
    @Override
    public Component AbilityTwoDescription() {
        return dess("Take mimic the Apearance and name of a player.");
    }


    /**
     * Ability Two - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability Two for
     * @param ability_item ItemStack used in ability usage
     * @throws MineSkinException if there's an error with MineSkin
     * @throws DataRequestException if there's an error with data request
     */
    @Override
 public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        AbilityTwo(player, ability_item, false);
    }

    /**
     * Ability Two - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability Two for
     * @param ability_item ItemStack used in ability usage
     * @param is_imbue checking if it's imbued
     * @throws MineSkinException if there's an error with MineSkin
     * @throws DataRequestException if there's an error with data request
     * Disguise as any player online on the server
     */
    @Override
 public void AbilityTwo(Player player, ItemStack ability_item, boolean is_imbue) throws MineSkinException, DataRequestException {
        if (can_ability(AbilityTwo_Cooldown(),abilityTwoCooldowns,player.getUniqueId()) && !player.getPersistentDataContainer().has(keygen("disguised"), PersistentDataType.BOOLEAN)) {
            player.playSound(player.getLocation(),Sound.BLOCK_DISPENSER_DISPENSE,1,1);
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.5f);
            player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
            InventoryGui inventoryGui = new InventoryGui();
            inventoryGui.setInventory(player);
            player.openInventory(inventoryGui.getInventory());

        } else {
            // no no ability
            if (abilityTwoCooldowns.get(player.getUniqueId()) != null) {
                double seconds = (double) (effective_cooldown(AbilityTwo_Cooldown(), player.getUniqueId()) - (System.currentTimeMillis() - abilityTwoCooldowns.get(player.getUniqueId()))) / 1000;
                player.sendActionBar(dess("You can't use this ability yet, wait " + seconds + " seconds"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            }
        }
        if (player.getPersistentDataContainer().has(keygen("disguised"), PersistentDataType.BOOLEAN)) {
            Freedom.get_plugin().getLogger().info(String.valueOf(Black.originalProfiles.get(player.getUniqueId())));
            if (Black.originalProfiles.get(player.getUniqueId()) != null) {
                Freedom.get_plugin().getLogger().info("Removing Disguises!");
                DisguiseAPI.undisguiseToAll(player);
                PlayerDisguise disguise =Black.originalProfiles.get(player.getUniqueId());
                player.getWorld();
                World world = player.getWorld();
                Location location = player.getLocation();
                world.spawnParticle(Particle.SMOKE, location,128);
                world.playSound(location,Sound.ENTITY_WARDEN_HEARTBEAT,1,1);
                disguise.removePlayer(player);
                disguise.stopDisguise();
                disguise.removeDisguise();
                player.playerListName(player.name());
                if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) == null) {
                    player.getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(keygen("black"),-0.10, AttributeModifier.Operation.ADD_NUMBER));
                }
                setSkinByName(player,player.getName());
                Black.originalProfiles.remove(player.getUniqueId());
            }
            player.getPersistentDataContainer().remove(keygen("disguised"));
            player.sendActionBar(dess("Disguise Removed."));
        }
    }

    /**
     * A shared Random instance used to generate random numbers throughout the application.
     * This is a static variable,*/
    public static Random random = new Random();
    /**
     * A mapping that associates unique player identifiers (UUIDs) with their respective PlayerDisguise instances.
     * This map is used to store and manage the original disguise profiles of players.
     */
    public static HashMap<UUID,PlayerDisguise> originalProfiles = Black.originalProfiles;

    /**
     * Event Handler for Inventory Click Event
     * Allows for Disguise and Undisguise
     * @param event InventoryClickEvent
     * @throws MineSkinException if there's an error with MineSkin
     * @throws DataRequestException if there's an error with data request
     */
    @EventHandler
    public void InventoryClickEvent (InventoryClickEvent event) throws MineSkinException, DataRequestException {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof InventoryGui inventoryGui && event.getCurrentItem() != null) {
            // yayas correct holder
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();
            Player baller = Bukkit.getPlayer(UUID.fromString(item.getPersistentDataContainer().get(keygen("player_uuid"),PersistentDataType.STRING)));
            PlayerDisguise disguise = new PlayerDisguise(baller);
            disguise.setHearSelfDisguise(false);
            disguise.setEntity(player);
            player.getPersistentDataContainer().set(keygen("disguised"),PersistentDataType.BOOLEAN,true);
            disguise.startDisguise();
            if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) != null) {
                player.getAttribute(Attribute.SCALE).removeModifier(keygen("black"));
            }
            player.playerListName(baller.name());
            setSkinByName(player,baller.getName());
            originalProfiles.put(player.getUniqueId(),disguise);
            World world = player.getWorld();
            Location location = player.getLocation();
            //VFX
            int disguiseid = random.nextInt();
            player.getPersistentDataContainer().set(keygen("diguiseid"),PersistentDataType.INTEGER,disguiseid);
            world.playSound(location,Sound.ENTITY_WARDEN_EMERGE,1,1);
            world.spawnParticle(Particle.SMOKE, location,128);
            player.closeInventory();
            abilityTwoCooldowns.put(player.getUniqueId(),System.currentTimeMillis());
            new BukkitRunnable() {
                @Override
                public void run() {
                    disguise.stopDisguise();
                    disguise.removeDisguise();
                    player.playerListName(player.name());
                    if (player.getPersistentDataContainer().has(keygen("disguiseid"),PersistentDataType.INTEGER)) {
                        if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) == null) {
                            player.getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(keygen("black"),0.80, AttributeModifier.Operation.ADD_NUMBER));
                        }
                        int gotid = player.getPersistentDataContainer().get(keygen("disguiseid"), PersistentDataType.INTEGER);
                        player.playerListName(player.name());
                        if (disguise.isDisguiseInUse() && disguiseid == gotid) {
                            world.spawnParticle(Particle.SMOKE, location, 128);
                            try {
                                setSkinByName(player,player.getName());
                            } catch (MineSkinException e) {
                                throw new RuntimeException(e);
                            } catch (DataRequestException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    this.cancel();
                }
            }.runTaskLater(Freedom.get_plugin(),3000);
            event.setCancelled(true);
        }
    }


    /**
     * A mapping of players to their respective InventoryGui instances.
     *
     * This field is used to store and manage GUI inventories tied to individual players.
     * The key in the map represents a player, while the value is an InventoryGui object
     * which holds the custom inventory associated with that player.
     */
    public HashMap<Player,InventoryGui> inventoryGui = new HashMap<>();

    /**
     * Represents a graphical user interface (GUI) for inventory customization or management.
     * Implements the {@code InventoryHolder} interface to encapsulate and manage inventory interactions.
     */
    public class InventoryGui implements InventoryHolder {
        /**
         * Represents the inventory associated with the {@code InventoryGui} instance.
         * This inventory is customized to display items based on the current context,
         * such as skulls representing online players, with metadata for further interaction handling.
         * The inventory size is dynamically adjusted based on the maximum number of players
         * allowed by the server, ensuring compatibility with Minecraft's inventory slot constraints.
         */
        public Inventory inventory;

        /**
         * Retrieves the inventory associated with this {@code InventoryGui} instance.
         * The inventory is dynamically generated and customized to display relevant
         * items, such as player-specific skulls, based on the current context.
         *
         * @return A non-null {@code Inventory} instance managed by this {@code InventoryGui}.
         */
        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }

        /**
         * Configures and initializes the inventory associated with the {@code InventoryGui} instance.
         * The inventory is dynamically populated with skull items that represent other online players,
         * excluding the player who triggered the inventory setup. Each skull contains metadata for
         * further interactions, such as the UUID of the represented player.
         *
         * @param player The player for whom the inventory is being set up. This player is excluded
         *               from the inventory's displayed items.
         */
        public void setInventory(Player player) {
            int max_players = Bukkit.getMaxPlayers();
            int remainder = max_players % 9;
            Freedom.get_plugin().getLogger().info(String.valueOf(remainder));
            if (remainder == 0) {
                inventory = Bukkit.createInventory(this,max_players,dess("Disguises"));
            } else {
                inventory = Bukkit.createInventory(this,max_players - (remainder),dess("Disguises"));
            }
            int iteration = 0;
            for (Player instancedPlayer : Bukkit.getOnlinePlayers()) {
                if (instancedPlayer.getUniqueId() != player.getUniqueId()){
                    ItemStack skull = getSkull(instancedPlayer);
                    SkullMeta meta = (SkullMeta) skull.getItemMeta();
                    meta.getPersistentDataContainer().set(keygen("player_uuid"), PersistentDataType.STRING, instancedPlayer.getUniqueId().toString());
                    SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
                    String name = instancedPlayer.getName();
                    meta.displayName(soulListener.SOULS.get(soulType).Name().append(dess(" " + name)));
                    skull.displayName();
                    skull.setItemMeta(meta);
                    inventory.setItem(iteration,skull);
                    iteration++;
                }
            }
        }

    }

    /**
     * Handles the process of allowing a player to join with specific configurations
     * and attributes based on their persistent data and soul type.
     *
     * @param player The player who is attempting to join.
     * @throws MineSkinException If an error occurs while processing the player's skin data.
     * @throws DataRequestException If an error occurs while requesting or handling data.
     */
    public static void join (Player player) throws MineSkinException, DataRequestException {
        setSkinByName(player,player.getName());
        if (player.getPersistentDataContainer().has(keygen("soul"))) {
        for (PotionEffect potion : player.getActivePotionEffects()) {
            if (potion.getDuration() == PotionEffect.INFINITE_DURATION) {
                player.removePotionEffect(potion.getType());
            }
        }

        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        if (soulType == SoulTypes.BaseBlack) {
            if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) == null) {
                player.getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(keygen("black"),-0.10, AttributeModifier.Operation.ADD_NUMBER));
            }
        } else {
            if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) != null) {
                player.getAttribute(Attribute.SCALE).removeModifier(keygen("black"));
            }

        }
            if (soulType == SoulTypes.Orange) {
                if (!player.getPersistentDataContainer().has(keygen("cancurse"))) {
                    player.getPersistentDataContainer().set(keygen("cancurse"),PersistentDataType.BOOLEAN,true);
                }
            }
    }
    }

// reviewers SOS why expand this system

    /**
     * Handles the PlayerRespawnEvent by applying certain effects to the player
     * based on their SoulType, which is retrieved from their PersistentDataContainer.
     * If the player's SoulType is `None`, they are given Strength, Speed, and Health Boost
     * potion effects with infinite duration.
     *
     * @param event The PlayerRespawnEvent triggered when a player respawns.
     */
    @EventHandler
    public void Respawnevent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        SoulTypes soulType = getSoulType(player);
        if (soulType == SoulTypes.None) {
            player.addPotionEffect(PotionEffectType.STRENGTH.createEffect(PotionEffect.INFINITE_DURATION,1));
            player.addPotionEffect(PotionEffectType.SPEED.createEffect(PotionEffect.INFINITE_DURATION,1));
            player.addPotionEffect(PotionEffectType.HEALTH_BOOST.createEffect(PotionEffect.INFINITE_DURATION,1));
        }
    }

    /**
     * Event handler for the PlayerJoinEvent that processes the player's data,
     * sets their skin, and updates their attributes or other configurations based
     * on their persistent data and soul type. It also ensures inventory GUI synchronization
     * for relevant players.
     *
     * @param event The PlayerJoinEvent triggered when a player joins the server.
     * @throws MineSkinException If there is an error while processing the player's skin data.
     * @throws DataRequestException If there is an error while requesting or handling data.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws MineSkinException, DataRequestException {
        setSkinByName(event.getPlayer(),event.getPlayer().getName());
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().has(keygen("soul"))) {
            Black.join(player);
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        if (soulType == SoulTypes.Black) {
            if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) == null) {
                player.getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(keygen("black"),-0.10, AttributeModifier.Operation.ADD_NUMBER));
            }
        }
        if (soulType == SoulTypes.Orange) {
            if (!player.getPersistentDataContainer().has(keygen("cancurse"))) {
                player.getPersistentDataContainer().set(keygen("cancurse"),PersistentDataType.BOOLEAN,true);
            }
        }
        if (!inventoryGui.isEmpty()) {
            for (Player iteratedplayer : inventoryGui.keySet()) {
                inventoryGui.get(iteratedplayer).setInventory(player);
            }
        }
    }
    }

    /**
     * Handles the PlayerQuitEvent when a player leaves the server.
     * This method ensures that the player's inventory GUI is properly
     * removed from the internal tracking map to avoid memory leaks
     * or inconsistent data.
     *
     * @param event The PlayerQuitEvent triggered when a player quits the server.
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (!inventoryGui.isEmpty()) {
            for (Player player : inventoryGui.keySet()) {
                if (event.getPlayer() == player) {
                    inventoryGui.remove(player);
                    break;
                }
                inventoryGui.get(player).setInventory(player);
            }
        }
    }

    /**
     * Handles the sneaking behavior of a player by deducting Soul Points and
     * applying specific effects based on the player's SoulType.
     * If the player's SoulType is `Black` or `BaseBlack`, the player gains
     * temporary invisibility while sneaking.
     *
     * @param player The player who triggered the sneaking event.
     */
    public void playerSneakEvent(Player player) {
        Freedom.get_plugin().getLogger().info("RAN");
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        player.getPersistentDataContainer().set(keygen("SoulPoint"),PersistentDataType.DOUBLE,SoulPoints - 5);
        if (soulType == SoulTypes.Black || soulType == SoulTypes.BaseBlack) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(player.isSneaking()) {
                        player.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(70,1));
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Freedom.get_plugin(),1,60);

        }
    }
    /**
     * Passive Description
     * @return Component used in UI
     */
    @Override
    public Component Passive_Description() {
        return dess("You are slightly shorter");
    }

    /**
     * Passive - A passive that is active no matter what
     * @param player Player to handle passive for
     * @param event Event that triggered the passive
     */
    @Override
    public void Passive(Player player, Object event) {

    }
    /**
     * Active Passive Description
     * @return Component used in UI
     */
    @Override
    public Component ActivePassive_Description() {
        return dess("When you sneak you are invisible");
    }
    /**
     * Ability Two Cooldown Time
     * @return Cooldown time in milliseconds
     */
    @Override
    public long AbilityTwo_Cooldown() {
        return 30000;
    }
    /**
     * Ability One Cooldown Time
     * @param obj Given object, depends on the moveset, could be a player
     * @return Cooldown time in milliseconds
     */
    @Override
    public long AbilityOne_Cooldown(Object obj) {
        return 0;
    }
    /**
     * Active Passive - A passive that requires a condition to activate (like sneaking or specific stats)
     * @param player Player to handle active passive for
     */
    @Override
    public void ActivePassive(Player player) {

    }
    // ─── PDC key ─────────────────────────────────────────────────────────────────

    // Must match the `self` key inside BlackInformation.
    /**
     * Represents a unique identifier for the "blackinformation" key within the
     * namespace system. This key is typically used for storing or accessing
     * information related to the "blackinformation" entity in a structured
     * data context.
     */
    NamespacedKey blackInformationKey = keygen("blackinformation");

    // PDC tag stamped on delete-button ItemStacks so we can identify them by slot.
    /**
     * A NamespacedKey representing the identifier for the delete slot functionality,
     * used to designate the key associated with the removal or deletion slot in the system.
     * The key is generated with the namespace "black" and the key name "delete_slot".
     */
    NamespacedKey deleteSlotKey = keygen("black_delete_slot");

    // ─── Inventory click handler ──────────────────────────────────────────────────

    /**
     * Handles the event when a player interacts with a custom inventory menu.
     *
     * This method is responsible for controlling actions such as deleting saved locations,
     * saving the player's current location into a slot, or teleporting the player to a saved location,
     * depending on the slot and item clicked within the inventory. The interaction logic ensures
     * that changes are persisted and the inventory appearance is updated dynamically to reflect actions taken.
     *
     * @param event The {@link InventoryClickEvent} triggered when a player interacts with an inventory.
     *              It provides information about the click, the inventory, and the player involved.
     */
    @EventHandler
    public void onPlayerClickInventory(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlackInformation.BlackMenu)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        BlackInformation blackInformation = BlackInformation.people.computeIfAbsent(
                player.getUniqueId(), id -> new BlackInformation(player));

        int rawSlot = event.getRawSlot();

        // ── Delete button row (slots 9–17) ───────────────────────────────────────
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.hasItemMeta()) {
            var pdc = clicked.getItemMeta().getPersistentDataContainer();
            if (pdc.has(deleteSlotKey, PersistentDataType.INTEGER)) {
                int targetSlot = pdc.get(deleteSlotKey, PersistentDataType.INTEGER);
                if (targetSlot >= 0 && targetSlot < 9) {
                    boolean hadData = targetSlot < blackInformation.locations.size()
                            && blackInformation.locations.get(targetSlot) != null;

                    if (!hadData) {
                        player.sendMessage(dess("<red>✗ Slot " + (targetSlot + 1) + " is already empty."));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                        return;
                    }

                    // Clear the slot.
                    while (blackInformation.items.size() <= targetSlot)     blackInformation.items.add(null);
                    while (blackInformation.locations.size() <= targetSlot) blackInformation.locations.add(null);
                    blackInformation.items.set(targetSlot, null);
                    blackInformation.locations.set(targetSlot, null);

                    persist(blackInformation, player);

                    // Refresh the open inventory so the player sees the change immediately.
                    BlackInformation.BlackMenu menu = (BlackInformation.BlackMenu) event.getInventory().getHolder();
                    menu.constructMenu(blackInformation, deleteSlotKey);

                    player.sendMessage(dess("<yellow>🗑 Slot " + (targetSlot + 1) + " cleared."));
                    player.sendActionBar(dess("<yellow><b>🗑 Slot " + (targetSlot + 1) + " cleared"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
                }
                return; // Always consume delete-row clicks even if no-op.
            }
        }

        // ── Top row (slots 0–8): save or teleport ─────────────────────────────────
        if (rawSlot < 0 || rawSlot >= 9) return;

        ItemStack currentItem = event.getCurrentItem();
        // A slot is "empty" if and only if it carries the black_placeholder PDC marker
        // that constructMenu stamps on every glass-pane placeholder.
        // The old isSimilar(amount=1) check failed for slots 1–8 because placeholders
        // are created with amount = i+1, so slots beyond the first were never recognised
        // as empty and were incorrectly treated as saved-location clicks.
        boolean clickedEmptySlot = currentItem == null
                || currentItem.getType().isAir()
                || (currentItem.hasItemMeta()
                && currentItem.getItemMeta()
                .getPersistentDataContainer()
                .has(keygen("black_placeholder"), PersistentDataType.BOOLEAN));

        if (clickedEmptySlot) {
            // ── Save current location into this slot ──────────────────────────────
            long savedCount = blackInformation.locations.stream().filter(l -> l != null).count();
            if (savedCount >= 9) {
                player.sendMessage(dess("<red>✗ All 9 slots are full. Delete a slot first (bottom row)."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                return;
            }

            while (blackInformation.items.size() <= rawSlot)     blackInformation.items.add(null);
            while (blackInformation.locations.size() <= rawSlot) blackInformation.locations.add(null);

            Location loc = player.getLocation().clone();
            blackInformation.items.set(rawSlot, player.getInventory().getItemInMainHand().clone());
            blackInformation.locations.set(rawSlot, loc);

            persist(blackInformation, player);

            player.closeInventory();
            player.sendMessage(dess("<green>✔ Location saved in slot " + (rawSlot + 1) + " — <gray>("
                    + Math.round(loc.getX()) + ", " + Math.round(loc.getY()) + ", " + Math.round(loc.getZ())
                    + ") in " + loc.getWorld().getName()));
            player.sendActionBar(dess("<green><b>✔ Location saved in slot " + (rawSlot + 1)));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.2f);

        } else {
            // ── Teleport to the saved location in this slot ───────────────────────
            if (rawSlot >= blackInformation.locations.size()) return;

            final Location destination = blackInformation.locations.get(rawSlot);
            if (destination == null) {
                player.sendMessage(dess("<red>✗ No location saved in slot " + (rawSlot + 1) + "."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                return;
            }

            player.closeInventory();

            final int slot = rawSlot;
            String destStr = "<gray>(" + Math.round(destination.getX()) + ", "
                    + Math.round(destination.getY()) + ", "
                    + Math.round(destination.getZ()) + ") in " + destination.getWorld().getName();

            player.sendMessage(dess("<yellow>⌛ Teleporting to slot " + (slot + 1) + " — " + destStr
                    + " <yellow>in 5 seconds. <red>Stand still!"));
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.6f, 1.0f);

            new BukkitRunnable() {
                final Location startLocation = player.getLocation().clone();
                final int time = 5 * 20;
                int tick = 0;

                @Override
                public void run() {
                    if (player.isDead() || !player.isOnline()) {
                        sendCancelFeedback("You died.");
                        this.cancel();
                        return;
                    }

                    if (player.getLocation().distanceSquared(startLocation) >= 4) {
                        sendCancelFeedback("You moved.");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 0.5f);
                        this.cancel();
                        return;
                    }

                    tick++;

                    if (tick >= time) {
                        persist(blackInformation, player);
                        player.teleport(destination);
                        player.sendMessage(dess("<green>✔ Teleported to slot " + (slot + 1) + " — " + destStr));
                        player.sendActionBar(dess("<green><b>✔ Teleported!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                        drawCircle(destination, 1, destination.getWorld(), 32, Particle.REVERSE_PORTAL);
                        this.cancel();
                        return;
                    }

                    int secondsLeft = (time - tick) / 20 + 1;
                    String dots = ".".repeat((int) threes(tick));
                    player.sendActionBar(dess("<yellow>⌛ Teleporting in <b>" + secondsLeft + "s</b>" + dots));

                    if (tick % 20 == 0) {
                        int secondsElapsed = tick / 20;
                        float pitch = 0.5f + (secondsElapsed * 0.15f);
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, pitch);
                        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
                        int rand = random.nextInt(0, 4);
                        switch (rand) {
                            case 0 -> drawSpiral(player.getLocation(), 1, 4, player.getWorld(), 32, Particle.DUST, new Particle.DustOptions(DyeColor.BLACK.getColor(), 4f));
                            case 1 -> drawStar(player.getLocation(), 1, player.getWorld(), 32, Particle.DUST, new Particle.DustOptions(DyeColor.BLACK.getColor(), 4f));
                            case 2 -> drawCircle(player.getLocation(), 1, player.getWorld(), 32, Particle.DUST, new Particle.DustOptions(DyeColor.BLACK.getColor(), 4f));
                            default -> drawSquare(player.getLocation(), 1, 32, Particle.DUST, new Particle.DustOptions(DyeColor.BLACK.getColor(), 4f), 0, 0, 0);
                        }
                    }
                }

                private void sendCancelFeedback(String reason) {
                    player.sendMessage(dess("<red>✗ Teleport cancelled — <gray>" + reason));
                    player.sendActionBar(dess("<red><b>✗ Cancelled"));
                }
            }.runTaskTimer(Freedom.get_plugin(), 0, 1);
        }
    }

    // ─── Persist helper ───────────────────────────────────────────────────────────

    /** Writes BlackInformation to PDC and keeps the in-memory cache in sync. */
    private void persist(BlackInformation info, Player player) {
        player.getPersistentDataContainer().set(blackInformationKey, PersistentDataType.STRING, info.toString());
        BlackInformation.people.put(player.getUniqueId(), info);
    }

    // ─── Inventory close handler ──────────────────────────────────────────────────

    /**
     * Handles the inventory close event for the BlackInformation menu.
     * Ensures that player-specific BlackInformation data is saved to the
     * player's PersistentDataContainer when the inventory is closed.
     *
     * @param event the InventoryCloseEvent triggered when a player closes an inventory.
     */
    @EventHandler
    public void onBlackInformationClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlackInformation.BlackMenu)) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        BlackInformation blackInformation = BlackInformation.people.get(player.getUniqueId());
        if (blackInformation == null) return; // nothing to save; leave PDC untouched

        player.getPersistentDataContainer().set(blackInformationKey, PersistentDataType.STRING, blackInformation.toString());
    }

    // ─── BlackInformation data class ──────────────────────────────────────────────

    /**
     * The BlackInformation class is used to store and manage information about specific player-related
     * items and locations within the game. It aims to organize and serialize information for persistent
     * storage and retrieval. Instances of this class can be used to save player-specific data related
     * to saved locations and associated items, including functionality for loading and saving from a
     * serialized string format.
     */
    public static class BlackInformation {
        /**
         * A static map that associates unique identifiers (UUIDs) with instances
         * of BlackInformation. This can be used to store and retrieve information
         * related to specific entities identified by their UUID.
         */
        public static HashMap<UUID, BlackInformation> people = new HashMap<>();
        /**
         * A list containing multiple ItemStack objects. This collection is primarily used
         * to store and manage a set of item stacks within the context of the BlackInformation class.
         * Each ItemStack represents an individual stackable unit that can contain quantities of a specific item.
         */
        public ArrayList<ItemStack> items    = new ArrayList<>();
        /**
         * A list of Location objects associated with this instance.
         * This field represents various locations that are relevant within the context
         * of the containing class.
         * It is initialized as an empty ArrayList and can be modified or accessed as needed.
         */
        public ArrayList<Location>  locations = new ArrayList<>();
        /**
         * Indicates whether the current state or condition is fresh.
         * This variable typically represents a recent or unaltered state.
         */
        boolean isFresh = false;

        // Must match blackInformationKey in the outer class.
        /**
         * Represents a unique identifier for the current context of the {@code BlackInformation} class.
         * This variable is initialized with a {@code NamespacedKey} generated using the key "blackinformation".
         * It serves as the internal representation or marker for this class instance.
         */
        NamespacedKey self = keygen("blackinformation");

        /**
         * Constructs a new instance of BlackInformation for the specified player.
         * If the player's persistent data contains a valid serialized BlackInformation object,
         * it will be deserialized and used to initialize this instance. Otherwise, a fresh
         * instance will be created.
         *
         * @param player The Player whose persistent data should be used to attempt to initialize
         *               the BlackInformation object.
         */
        public BlackInformation(Player player) {
            String construct = player.getPersistentDataContainer().getOrDefault(self, PersistentDataType.STRING, "");
            if (!construct.isEmpty()) {
                BlackInformation info = fromString(construct);
                if (info != null) {
                    items     = info.items;
                    locations = info.locations;
                    isFresh   = false;
                    return;
                }
            }
            isFresh = true;
        }

        /**
         * Default constructor for the BlackInformation class.
         * Initializes a new instance of BlackInformation with the `isFresh` field set to true.
         */
        public BlackInformation() {
            isFresh = true;
        }

        /**
         * Deserializes a string into a {@code BlackInformation} object.
         * The input string is expected to contain location and item data separated by a {@code '|'} character.
         * Location data consists of up to 9 entries, each formatted as {@code worldName,x,y,z}, separated by {@code ';'}.
         * Item data consists of up to 9 entries separated by {@code ';;'}.
         *
         * Entries that are missing or invalid will result in {@code null} values for the corresponding locations or*/
        public BlackInformation fromString(String string) {
            BlackInformation output = new BlackInformation();

            // Format: <locations>|<items>
            //   locations : 9 entries separated by ';', each "worldName,x,y,z" or empty
            //   items     : 9 entries separated by ';;' — safe because itemToString()
            //               now produces Base64 which never contains ';'.
            String[] parts = string.split("\\|", 2);
            if (parts.length != 2) return null;

            // --- locations ---
            String[] locEntries = parts[0].split(";", -1);
            for (int i = 0; i < 9; i++) {
                if (i >= locEntries.length) { output.locations.add(null); continue; }
                String e = locEntries[i];
                if (e == null || e.isEmpty()) { output.locations.add(null); continue; }
                int c1 = e.indexOf(',');
                int c2 = c1 < 0 ? -1 : e.indexOf(',', c1 + 1);
                int c3 = c2 < 0 ? -1 : e.indexOf(',', c2 + 1);
                if (c1 < 0 || c2 < 0 || c3 < 0) { output.locations.add(null); continue; }
                try {
                    String worldName = e.substring(0, c1);
                    double x = Double.parseDouble(e.substring(c1 + 1, c2));
                    double y = Double.parseDouble(e.substring(c2 + 1, c3));
                    double z = Double.parseDouble(e.substring(c3 + 1));
                    World w = Bukkit.getWorld(worldName);
                    output.locations.add(w != null ? new Location(w, x, y, z) : null);
                } catch (NumberFormatException ex) {
                    output.locations.add(null);
                }
            }

            // --- items ---
            String[] itemEntries = parts[1].split(";;", -1);
            for (int i = 0; i < 9; i++) {
                if (i >= itemEntries.length) { output.items.add(null); continue; }
                String e = itemEntries[i];
                if (e == null || e.isEmpty()) { output.items.add(null); continue; }
                output.items.add(stringToItem(e));
            }

            return output;
        }

        /**
         * Converts the state of the object into a string representation.
         * The resulting string includes serialized location and item data, separated by a `|` character.
         *
         * Locations are serialized as up to 9 entries, each formatted as `worldName,x,y,z` and separated by `;`.
         * If a location or its world is null, the corresponding entry will be skipped or represented as empty.
         *
         * Items are serialized as up to 9 entries, each separated by `;;`. The serialization format for each
         * item is determined by the `itemToString` method.
         *
         * @return A string representation of the object, comprising serialized locations and item data.
         */
        public String toString() {
            StringBuilder locBuilder = new StringBuilder();
            for (int i = 0; i < 9; i++) {
                if (i > 0) locBuilder.append(';');
                Location loc = (i < locations.size()) ? locations.get(i) : null;
                if (loc != null && loc.getWorld() != null) {
                    locBuilder.append(loc.getWorld().getName()).append(',')
                            .append(loc.getX()).append(',')
                            .append(loc.getY()).append(',')
                            .append(loc.getZ());
                }
            }

            StringBuilder itemBuilder = new StringBuilder();
            for (int i = 0; i < 9; i++) {
                if (i > 0) itemBuilder.append(";;");
                ItemStack it = (i < items.size()) ? items.get(i) : null;
                if (it != null) itemBuilder.append(itemToString(it));
            }

            return locBuilder.toString() + "|" + itemBuilder.toString();
        }

        // ─── BlackMenu ────────────────────────────────────────────────────────────

        /**
         * Represents a custom inventory implementation for managing "Black Locations".
         *
         * This class creates a specialized inventory with 18 slots divided into two rows:
         * - The first row (slots 0–8) is used to display saved locations or placeholders for empty slots.
         * - The second row (slots 9–17) consists of delete buttons for clearing saved locations.
         *
         * Each slot and button carries metadata for identification, ensuring reliable interaction handling.
         * Persistence Data Container (PDC) tags are used to associate specific behaviors with inventory items.
         */
        public static class BlackMenu implements InventoryHolder {
            Inventory inventory;

            /**
             * Builds an 18-slot (2-row) inventory:
             *   Row 1 (slots 0–8)  : saved location items, or black glass-pane placeholders.
             *   Row 2 (slots 9–17) : red glass-pane delete buttons, one per slot above.
             *
             * Each delete button has the `deleteSlotKey` PDC tag set to its target slot (0–8)
             * so the click handler can identify and clear the correct entry without any
             * fragile slot-number arithmetic.
             */
            public void constructMenu(BlackInformation information, NamespacedKey deleteSlotKey) {
                inventory = Bukkit.createInventory(this, 18, dess("<dark_gray>☽ Black Locations"));

                for (int i = 0; i < 9; i++) {
                    // ── Row 1: location slots ─────────────────────────────────────────────
                    ItemStack locationItem;
                    Location loc = (i < information.locations.size()) ? information.locations.get(i) : null;
                    ItemStack savedItem  = (i < information.items.size())    ? information.items.get(i)    : null;

                    if (loc != null && savedItem != null && savedItem.getItemMeta() != null) {
                        // Clone the stored item so we never mutate the live BlackInformation.items
                        // reference. Without this, setItemMeta() would write the display lore back
                        // into the stored object, which then gets serialized with the extra lore
                        // permanently attached — corrupting every subsequent save/load cycle.
                        locationItem = savedItem.clone();
                        ItemMeta meta = locationItem.getItemMeta();
                        String coordLine = "<gray>(" + Math.round(loc.getX()) + ", "
                                + Math.round(loc.getY()) + ", "
                                + Math.round(loc.getZ()) + ") in " + loc.getWorld().getName();
                        // Start fresh lore — do NOT copy meta.lore() from the stored item, because
                        // that would accumulate our injected lines on every menu open.
                        List<Component> lore = new ArrayList<>();
                        lore.add(dess("<dark_gray>Slot " + (i + 1)));
                        lore.add(dess(coordLine));
                        lore.add(dess("<yellow>Click to teleport here"));
                        meta.lore(lore);
                        locationItem.setItemMeta(meta);
                    } else {
                        // Empty slot placeholder — stamp a PDC marker so the click handler
                        // can identify these items reliably regardless of amount or display name.
                        locationItem = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE, i + 1);
                        ItemMeta meta = locationItem.getItemMeta();
                        meta.displayName(dess("<gray>Slot " + (i + 1) + " — Empty"));
                        meta.lore(List.of(dess("<dark_gray>Click with an item in hand to save your location here.")));
                        meta.getPersistentDataContainer().set(keygen("black_placeholder"), PersistentDataType.BOOLEAN, true);
                        locationItem.setItemMeta(meta);
                    }
                    inventory.setItem(i, locationItem);

                    // ── Row 2: delete buttons ─────────────────────────────────────────────
                    boolean occupied = loc != null;
                    ItemStack deleteButton = ItemStack.of(
                            occupied ? Material.RED_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE, 1);
                    ItemMeta deleteMeta = deleteButton.getItemMeta();
                    deleteMeta.displayName(occupied
                            ? dess("<red>🗑 Delete slot " + (i + 1))
                            : dess("<dark_gray>🗑 Slot " + (i + 1) + " — Empty"));
                    if (occupied) {
                        deleteMeta.lore(List.of(dess("<gray>Click to clear this saved location.")));
                    }
                    // Tag with target slot so the click handler knows which slot to clear.
                    deleteMeta.getPersistentDataContainer().set(deleteSlotKey, PersistentDataType.INTEGER, i);
                    deleteButton.setItemMeta(deleteMeta);
                    inventory.setItem(9 + i, deleteButton);
                }
            }

            /**
             * Retrieves the inventory associated with this menu.
             *
             * @return the inventory containing the menu items and placeholders
             *         structured according to the menu's design.
             */
            @Override
            public @NotNull Inventory getInventory() {
                return inventory;
            }
        }
    }
}


