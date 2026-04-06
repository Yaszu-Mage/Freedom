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

public class BaseBlue extends Util implements Base_Soul, Listener {
    @Override
    public String Name_For_Container() {
        return "BaseBlue";
    }

    @Override
    public Component Name() {
        return dess("<Blue>Blue</Blue>");
    }

    @Override
    public Component Description() {
        return dess("?em1t tub ti si tahW");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.CLOCK);
    }

    @Override
    public Component AbilityOneName() {
        return dess("<Blue>Ability One</Blue> - Strings Attached");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("Using your Custom Item, you can freeze your enemies in time");
    }

    @Override
    public void AbilityOne(Player player) {
        if (can_ability(AbilityOne_Cooldown(), abilityOneCooldowns, player.getUniqueId()) == false) return;
        abilityOneCooldowns.put(player.getUniqueId(),System.currentTimeMillis());
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
                Player doubleclock = Bukkit.getPlayer(player.getPersistentDataContainer().get(keygen("doubleclock"),PersistentDataType.STRING));
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

    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.CLOCK);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("<Blue>Ability Two</Blue> - Rewind");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("While triggering the anti clock item, Blue is able to set a teleport location.");
    }
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
                drawCircle(loadingLocation,1,player.getWorld(),16,Particle.SMOKE);
            }
        }.runTaskTimer(Freedom.get_plugin(),20,20);
        new BukkitRunnable() {
            public int tick = 0;
            public double last_health = player.getHealth();
            Location loadingLocation = player.getLocation();
            @Override
            public void run() {
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
    public static double threes(int num) {
        if (num % 3 == 0) {
            return 3;
        }
        if (num % 2 == 0) {
            return 2;
        }
        return 1;
    }
    public void save(Player player) {
        player.getPersistentDataContainer().set(keygen("blue_save"), PersistentDataType.BOOLEAN, true);
        player.getPersistentDataContainer().set(keygen("bluesaveX"), PersistentDataType.DOUBLE, player.getLocation().getX());
        player.getPersistentDataContainer().set(keygen("bluesaveY"), PersistentDataType.DOUBLE, player.getLocation().getY());
        player.getPersistentDataContainer().set(keygen("bluesaveZ"), PersistentDataType.DOUBLE, player.getLocation().getZ());
        player.getPersistentDataContainer().set(keygen("blueworld"), PersistentDataType.STRING, player.getWorld().getName());
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
    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        if (!player.getPersistentDataContainer().has(keygen("blue_save"), PersistentDataType.BOOLEAN)) {
            save(player);
            return;
        }
        if (player.getPersistentDataContainer().get(keygen("blue_save"), PersistentDataType.BOOLEAN)) {

            load(player);
        }
    }

    @Override
    public Component Passive_Description() {
        return dess("Linked in Park: You can link with another player, when that player is killed, you are enraged.");
    }
BaseYellow yellow = new BaseYellow();
    @Override
    public void Passive(Player player, Object event) {
        yellow.Passive(player,event);
    }

    @Override
    public Component ActivePassive_Description() {
        return dess("Light Mend: Using soul points, you can rewind time and heal 25 durability of the Item you are holding");
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 30000;
    }

    @Override
    public long AbilityOne_Cooldown() {
        return 30000;
    }

    @Override
    public void ActivePassive(Player player) {
        if (!player.getPersistentDataContainer().has(FreedomKeys.soulPoint())) return;
        if (player.getPersistentDataContainer().get(FreedomKeys.soulPoint(),PersistentDataType.DOUBLE) >= 5) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable) {
                // damage = 0 is full health, higher = more used
                Freedom.get_plugin().getLogger().info(String.valueOf(damageable.getDamage()));
                Freedom.get_plugin().getLogger().info(String.valueOf(damageable.getDamage() - 25));
                damageable.setDamage(Math.max(0,damageable.getDamage() - 25));
                item.setItemMeta((ItemMeta) damageable);
                player.getPersistentDataContainer().set(FreedomKeys.soulPoint(),PersistentDataType.DOUBLE,player.getPersistentDataContainer().get(FreedomKeys.soulPoint(),PersistentDataType.DOUBLE) - 5);
                player.sendMessage(dess("Healed 25 durability."));
            }


        }
    }
}
