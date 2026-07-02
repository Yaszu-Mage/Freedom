package xyz.yaszu.freedom.Soul.Base;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.time.LocalTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static xyz.yaszu.freedom.Util.Util.*;

/**
 * The BaseBlue class represents a derived implementation of the Base_Soul class
 * providing specific functionality related to abilities, descriptions, and interactions
 * for a particular type of resource or ability container. This class might be designed
 * for use in a game or application involving player-based or item-based actions.
 * @author yaszu
 */
public class BaseBlue implements Base_Soul, Listener {
    /**
     * Returns the name of the container associated with this instance.
     *
     * @return A string representing the name of the container, in this case "BaseBlue".
     */
    @Override
    public String Name_For_Container() {
        return "BaseBlue";
    }

    /**
     * Returns a formatted component representing the name, constructed using MiniMessage.
     *
     * @return A Component instance representing the name, in this case formatted as "Blue".
     */
    @Override
    public Component Name() {
        return dess("<Blue>Blue</Blue>");
    }

    /**
     * Constructs a descriptive component based on a predefined reverse-encoded message.
     * The message is processed and returned as a formatted text component using the MiniMessage system.
     *
     * @return A Component object representing the formatted description derived from the encoded message.
     */
    @Override
    public Component Description() {
        return dess("?em1t tub ti si tahW");
    }

    /**
     * Returns an ItemStack representing the icon associated with this instance.
     *
     * @return An ItemStack of type Material.CLOCK.
     */
    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.CLOCK);
    }

    /**
     * Constructs and returns a labeled component representing the name of the first ability.
     * The label is formatted using the MiniMessage system, styled as "<Blue>Ability One</Blue> - Strings Attached".
     *
     * @return A Component object containing the formatted name of the first ability.
     */
    @Override
    public Component AbilityOneName() {
        return dess("<Blue>Ability One</Blue> - Strings Attached");
    }

    /**
     * Constructs and returns a descriptive component for the first ability.
     * @return A Component object containing the formatted description of the first ability.
     */
    @Override
    public Component AbilityOneDescription() {
        return dess("Using your Custom Item, you can freeze your enemies in time");
    }

    /**
     * Triggers the first ability for the specified player. Primarily serves as a simplified version
     * of the overloaded method, delegating its execution.
     *
     * @param player The player who will activate the first ability.
     */
    @Override
 public void AbilityOne(Player player) {
        AbilityOne(player, false);
    }

    /**
     * Handles the execution of the first ability for a player.
      * @param player Player to handle Ability One for
     * @param is_imbue checking if it's imbued
     */
    @Override
 public void AbilityOne(Player player, boolean is_imbue) {
        if (can_ability(AbilityOne_Cooldown(null), abilityOneCooldowns, player.getUniqueId()) == false) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }
        abilityOneCooldowns.put(player.getUniqueId(),System.currentTimeMillis());
        player.getWorld().playSound(player.getLocation(),"custom.timestop",1.5f,1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 2.0f);
        new BukkitRunnable() {
            int tick = 0;
            HashMap<UUID, Location> locations = new HashMap<>();
            @Override
            public void run() {
                drawClock(
                        player.getLocation(),
                        1.5,
                        32,
                        12,
                        LocalTime.now().getHour(),
                        LocalTime.now().getMinute(),
                        Particle.DUST,
                        new Particle.DustOptions(Color.AQUA,1f),
                        Particle.DUST,
                        Particle.DUST,
                        new Particle.DustOptions(Color.AQUA,1f),
                        new Particle.DustOptions(Color.AQUA,1f)
                );
                Collection<Player> near = player.getLocation().getNearbyEntitiesByType(Player.class,2);
                if (player.getPersistentDataContainer().has(keygen("doubleclock"),PersistentDataType.STRING)) {
                    Player doubleclock = Bukkit.getPlayer(player.getPersistentDataContainer().get(keygen("doubleclock"),PersistentDataType.STRING));
                }
                Player doubleclock = player;
                near.forEach(iterator -> {
                    if (iterator != player) {
                        if (doubleclock != null) {
                            if (iterator == doubleclock) return;
                        }
                        if (locations.containsKey(iterator.getUniqueId())) {
                            iterator.teleport(locations.get(iterator.getUniqueId()));
                        } else {
                            locations.put(iterator.getUniqueId(),iterator.getLocation());
                            iterator.teleport(locations.get(iterator.getUniqueId()));
                            iterator.getWorld().spawnParticle(Particle.SNOWFLAKE, iterator.getLocation().add(0, 1, 0), 20, 0.5, 0.8, 0.5, 0.05);
                            iterator.getWorld().playSound(iterator.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
                        }
                        iterator.setVelocity(new Vector(0,0,0));
                    }
                });
                tick++;
                if (tick >= 100) {
                    this.cancel();
                }
            }
        }.runTaskTimer(Freedom.get_plugin(),0,1);

    }

    /**
     * Returns the related item for this instance.
     * @return An ItemStack representing the related item, in this case Material.CLOCK.
     */
    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.CLOCK);
    }

    /**
     * Returns the name of the second ability.
     * @return A Component representing the name of the second ability.
     */
    @Override
    public Component AbilityTwoName() {
        return dess("<Blue>Ability Two</Blue> - Rewind");
    }

    /**
     * Constructs and returns a descriptive component for the second ability.
     * The description provides details about the ability triggered by the anti clock item,
     * where Blue can set a teleport location.
     *
     * @return A Component object containing the formatted description of the second ability.
     */
    @Override
    public Component AbilityTwoDescription() {
        return dess("While triggering the anti clock item, Blue is able to set a teleport location.");
    }

    /**
     * Initiates a teleportation process for the specified player to a saved location.
     * This method implements visual effects during the loading process and allows
     * nearby trusted players to be teleported with the initiating player.
     * The teleportation is canceled if certain conditions fail, such as player movement,
     * exceeding the allowed distance, or health increase during the process.
     *
     * @param player The player who is initiating the teleportation, whose persistent data
     *               container holds the saved location and necessary data for teleportation.
     */
    public void load(Player player) {
        //VFX
        new BukkitRunnable() {

            @Override
            public void run() {
                if (!player.getPersistentDataContainer().has(keygen("bluesave"))) {
                    this.cancel();
                }
                Location loadingLocation = new Location(
                        Bukkit.getWorld(player.getPersistentDataContainer().get(keygen("blueworld"),PersistentDataType.STRING)),
                        player.getPersistentDataContainer().get(keygen("bluesaveX"),PersistentDataType.DOUBLE),
                        player.getPersistentDataContainer().get(keygen("bluesaveY"),PersistentDataType.DOUBLE),
                        player.getPersistentDataContainer().get(keygen("bluesaveZ"),PersistentDataType.DOUBLE)

                );
                if (loadingLocation != null) {
                    if (loadingLocation.distanceSquared(player.getLocation()) >= 10000) {
                        player.sendMessage(dess("Too Far! (100 blocks)"));
                        player.getPersistentDataContainer().remove(keygen("blue_save"));
                        player.getPersistentDataContainer().remove(keygen("bluesaveX"));
                        player.getPersistentDataContainer().remove(keygen("bluesaveY"));
                        player.getPersistentDataContainer().remove(keygen("bluesaveZ"));
                        player.getPersistentDataContainer().remove(keygen("blueworld"));
                        this.cancel();
                        return;
                    }
                }
                drawCircle(player.getLocation(),1,player.getWorld(),16, Particle.SMOKE);
                drawCircle(player.getLocation(),1,player.getWorld(),8, Particle.BUBBLE);
                drawCircle(loadingLocation,1,player.getWorld(),16,Particle.SMOKE);
                drawCircle(loadingLocation,1,player.getWorld(),8, Particle.SOUL);
            }
        }.runTaskTimer(Freedom.get_plugin(),20,20);
        new BukkitRunnable() {
            public int tick = 0;
            public double last_health = player.getHealth();
            Location loadingLocation = player.getLocation();
            @Override
            public void run() {
                try{
                if (loadingLocation != null) {
                    if (loadingLocation.distanceSquared(player.getLocation()) >= 10000) {
                        player.sendMessage(dess("Too Far! (100 blocks)"));
                        player.getPersistentDataContainer().remove(keygen("blue_save"));
                        player.getPersistentDataContainer().remove(keygen("bluesaveX"));
                        player.getPersistentDataContainer().remove(keygen("bluesaveY"));
                        player.getPersistentDataContainer().remove(keygen("bluesaveZ"));
                        player.getPersistentDataContainer().remove(keygen("blueworld"));
                        this.cancel();
                        return;
                    }
                }
                if (player.getPersistentDataContainer().has(keygen("blueworld"),PersistentDataType.STRING)) {
                    loadingLocation = new Location(
                            Bukkit.getWorld(player.getPersistentDataContainer().get(keygen("blueworld"),PersistentDataType.STRING)),
                            player.getPersistentDataContainer().get(keygen("bluesaveX"),PersistentDataType.DOUBLE),
                            player.getPersistentDataContainer().get(keygen("bluesaveY"),PersistentDataType.DOUBLE),
                            player.getPersistentDataContainer().get(keygen("bluesaveZ"),PersistentDataType.DOUBLE)
                    );
                }

                if (player.getHealth() > last_health || !player.isSneaking() || player.isDead()) {
                    this.cancel();
                    player.sendActionBar(dess("Teleport Cancelled."));
                }
                if (tick >= 4) {
                    drawClock(
                            player.getLocation(),
                            1.5,
                            32,
                            12,
                            LocalTime.now().getHour(),
                            LocalTime.now().getMinute(),
                            Particle.DUST,
                            new Particle.DustOptions(Color.YELLOW,1f),
                            Particle.DUST,
                            Particle.DUST,
                            new Particle.DustOptions(Color.YELLOW,1f),
                            new Particle.DustOptions(Color.YELLOW,1f)
                    );
                    player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST,1,1);
                    List<Entity> entities = player.getNearbyEntities(2,2,2);
                    for (Entity entity : entities) {
                        if (entity instanceof Player trustcheck) {
                            if (player.getPersistentDataContainer().has(keygen("trustedby"))) {
                                String trustedby = trustcheck.getPersistentDataContainer().get(keygen("trustedby"),PersistentDataType.STRING);
                                if (trustedby.contains(player.getName())) {
                                    drawClock(
                                            trustcheck.getLocation(),
                                            1.5,
                                            32,
                                            12,
                                            LocalTime.now().getHour(),
                                            LocalTime.now().getMinute(),
                                            Particle.DUST,
                                            new Particle.DustOptions(Color.YELLOW,1f),
                                            Particle.DUST,
                                            Particle.DUST,
                                            new Particle.DustOptions(Color.YELLOW,1f),
                                            new Particle.DustOptions(Color.YELLOW,1f)
                                    );
                                    trustcheck.teleport(loadingLocation);
                                    drawClock(
                                            loadingLocation,
                                            1.5,
                                            32,
                                            12,
                                            LocalTime.now().getHour(),
                                            LocalTime.now().getMinute(),
                                            Particle.DUST,
                                            new Particle.DustOptions(Color.AQUA,1f),
                                            Particle.DUST,
                                            Particle.DUST,
                                            new Particle.DustOptions(Color.AQUA,1f),
                                            new Particle.DustOptions(Color.AQUA,1f)
                                    );
                                    player.getWorld().playSound(player.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,1,1);
                                }

                            }
                        }
                    }

                    drawClock(
                            player.getLocation(),
                            1.5,
                            32,
                            12,
                            LocalTime.now().getHour() - tick/2,
                            LocalTime.now().getMinute() - tick,
                            Particle.DUST,
                            new Particle.DustOptions(Color.AQUA,1f),
                            Particle.DUST,
                            Particle.DUST,
                            new Particle.DustOptions(Color.AQUA,1f),
                            new Particle.DustOptions(Color.AQUA,1f)
                    );
                    player.playSound(player.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,1,1);
                    player.teleport(loadingLocation);
                    player.teleport(loadingLocation);
                    player.teleport(loadingLocation);
                    player.teleport(loadingLocation);
                    this.cancel();
                } else {
                    if (threes(tick) == 3) {
                        player.sendActionBar(dess("Teleporting in " + (4-tick) + " seconds..."));
                    } else if (threes(tick) == 2) {
                        player.sendActionBar(dess("Teleporting in " + (4-tick) + " seconds.."));
                    } else {
                        player.sendActionBar(dess("Teleporting in " + (4-tick) + " seconds."));
                    }
                    drawClock(
                            player.getLocation(),
                            1.5,
                            32,
                            12,
                            LocalTime.now().getHour() - tick/2,
                            LocalTime.now().getMinute() - tick,
                            Particle.DUST,
                            new Particle.DustOptions(Color.AQUA,1f),
                            Particle.DUST,
                            Particle.DUST,
                            new Particle.DustOptions(Color.AQUA,1f),
                            new Particle.DustOptions(Color.AQUA,1f)
                    );
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO,SoundCategory.PLAYERS,1,tick);
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL,SoundCategory.PLAYERS,1,tick-1);
                }
                tick = tick + 1;
            } catch (Exception ignored) {}
            }
            @Override
            public synchronized void cancel() throws IllegalStateException {
                player.getPersistentDataContainer().remove(keygen("blue_save"));
                player.getPersistentDataContainer().remove(keygen("bluesaveX"));
                player.getPersistentDataContainer().remove(keygen("bluesaveY"));
                player.getPersistentDataContainer().remove(keygen("bluesaveZ"));
                player.getPersistentDataContainer().remove(keygen("blueworld"));
                Bukkit.getScheduler().cancelTask(getTaskId());
            }
        }.runTaskTimer(Freedom.get_plugin(),0,20);


    }

    /**
     * Checks if a number is a multiple of 3, 2, or 1.
     * @param num The number to check.
     * @return 3 if the number is a multiple of 3, 2 if it is a multiple of 2, and 1 otherwise.
     */
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
     * Saves the current location of the player to a persistent data container.
     * @param player The player whose location is to be saved.
     */
    public void save(Player player) {
        player.getPersistentDataContainer().set(keygen("blue_save"), PersistentDataType.BOOLEAN, true);
        player.getPersistentDataContainer().set(keygen("bluesaveX"), PersistentDataType.DOUBLE, player.getLocation().getX());
        player.getPersistentDataContainer().set(keygen("bluesaveY"), PersistentDataType.DOUBLE, player.getLocation().getY());
        player.getPersistentDataContainer().set(keygen("bluesaveZ"), PersistentDataType.DOUBLE, player.getLocation().getZ());
        player.getPersistentDataContainer().set(keygen("blueworld"), PersistentDataType.STRING, player.getWorld().getName());
        player.sendActionBar(dess("Saved Location at (" + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + ")"));
        player.getWorld().spawnParticle(Particle.ENTITY_EFFECT, player.getLocation(), 30, 0.5, 1.0, 0.5, 1.0, Color.AQUA);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.5f);
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
     * Handles the ability two usage for a player.
     * @param player Player to handle Ability Two for
     * @param ability_item ItemStack used in ability usage
     * @throws MineSkinException
     * @throws DataRequestException
     */
    @Override
 public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        AbilityTwo(player, ability_item, false);
    }

    /**
     * Handles the ability two usage for a player.
     * @param player Player to handle Ability Two for
     * @param ability_item ItemStack used in ability usage
     * @param is_imbue checking if it's imbued
     * @throws MineSkinException
     * @throws DataRequestException
     */
    @Override
 public void AbilityTwo(Player player, ItemStack ability_item, boolean is_imbue) throws MineSkinException, DataRequestException {
        if (!player.getPersistentDataContainer().has(keygen("blue_save"), PersistentDataType.BOOLEAN)) {
            save(player);
            return;
        }
        if (player.getPersistentDataContainer().get(keygen("blue_save"), PersistentDataType.BOOLEAN)) {

            load(player);
        }
    }

    /**
     * Component description of the Passive Ability
     * @return Component constructed from the description
     * @see Component
     * @see Util
     */
    @Override
    public Component Passive_Description() {
        return dess("Linked in Park: You can link with another player, when that player is killed, you are enraged.");
    }

    /**
     * Creates a new BaseYellow instance to recycle passives
     */
    BaseYellow yellow = new BaseYellow();
    @Override
    public void Passive(Player player, Object event) {
        yellow.Passive(player,event);
    }

    /**
     * Component description of the Active Ability
     * @return Component constructed from the description
     */
    @Override
    public Component ActivePassive_Description() {
        return dess("Light Mend: Using soul points, you can rewind time and heal 25 durability of the Item you are holding");
    }

    /**
     * Returns the cooldown duration for the second ability.
     * This is the amount of time (in milliseconds)*/
    @Override
    public long AbilityTwo_Cooldown() {
        return 30000;
    }

    /**
     * Ability one Cooldown measured in milliseconds
     * @param obj Given object, depends on the moveset, could be a player
     * @return Cooldown time in milliseconds
     */
    @Override
    public long AbilityOne_Cooldown(Object obj) {
        return 30000;
    }

    /**
     * Ability two Cooldown measured in milliseconds
     * @param player Player to handle active passive for
     */
    @Override
    public void ActivePassive(Player player) {
        if (!player.getPersistentDataContainer().has(FreedomKeys.soulPoint())) return;
        if (player.getPersistentDataContainer().get(FreedomKeys.soulPoint(),PersistentDataType.DOUBLE) >= 5) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!(item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable)) {
                item = player.getInventory().getItemInOffHand();
            }
            if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable) {
                // damage = 0 is full health, higher = more used
                damageable.setDamage(Math.max(0,damageable.getDamage() - 25));
                item.setItemMeta((ItemMeta) damageable);
                player.getPersistentDataContainer().set(FreedomKeys.soulPoint(),PersistentDataType.DOUBLE,player.getPersistentDataContainer().get(FreedomKeys.soulPoint(),PersistentDataType.DOUBLE) - 5);
                player.sendMessage(dess("Healed 25 durability."));
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 2.0f);
                player.getWorld().spawnParticle(Particle.SCRAPE, player.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.05);
            }


        }
    }
}


