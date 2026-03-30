package xyz.yaszu.freedom.Soul.Base;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BaseRed extends Util implements Base_Soul {

    public long ability_one_cooldown = 1000;
    public long ability_two_cooldown = 30000;

    public static HashMap<UUID, Long> ability_two_cooldowns = new HashMap<>();

    public static HashMap<UUID,Long> ability_one_cooldowns = new HashMap<UUID,Long>();

    @Override
    public String Name_For_Container() {
        return "BaseRed";
    }

    @Override
    public Component Name() {
        return dess("<red>Red</red>");
    }

    @Override
    public Component Description() {
        return dess("Your ambition burns like a fire.");
    }

    @Override
    public ItemStack Icon() {
        ItemStack workingItem = ItemStack.of(Material.FIRE_CHARGE);
        ItemMeta workingMeta = workingItem.getItemMeta();
        workingMeta.displayName(dess("<red>Red</red>"));
        workingMeta.lore(List.of(Description()));
        workingItem.setItemMeta(workingMeta);
        return workingItem;
    }

    @Override
    public Component AbilityOneName() {
        return dess("<red>Ability One</red> - Flame Dash ");
    }

    @Override
    public Component AbilityOneDescription() {

        return dess("   <color:#ff6a00>Dash</color> launches yourself forward with a boost of flame towards your <dark_red>enemies</dark_red>\n");
    }

    @Override
    // Flame Dash -- Player Dashes after Crouching and Jumping in the direction of their movement
    public void AbilityOne(Player player) {
        PersistentDataContainer playerContainer = player.getPersistentDataContainer();
        if (can_ability(ability_one_cooldown,ability_one_cooldowns,player.getUniqueId())) {
                player.setVelocity(player.getLocation().getDirection().multiply(1.5));
                spawnFlames(player).runTaskTimer(Bukkit.getPluginManager().getPlugin("Freedom"), 0, 1);
                ability_one_cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        } else {
            double seconds = (double) (ability_one_cooldown - (System.currentTimeMillis() - ability_one_cooldowns.get(player.getUniqueId()))) / 1000;
            player.sendActionBar(dess("You can't use this ability yet, wait " + seconds + " seconds"));
        }


    }

    public BukkitRunnable spawnFlames(Player player) {
        return new BukkitRunnable() {
            public int tick = 0;

            @Override
            public void run() {
                if (tick >= 20 || player.isOnGround()) {
                    this.cancel();
                }
                tick = tick + 1;
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 0, 0), 5);
            }
        };
    }

    @Override
    public ItemStack Related_Item() {
        ItemStack workingItem = ItemStack.of(Material.COMPASS);
        ItemMeta workingMeta = workingItem.getItemMeta();
        workingMeta.displayName(dess("<red>Promised</red> Timepiece"));
        workingMeta.lore(List.of(dess("<red>Always,</red> on time.")));
        workingMeta.getPersistentDataContainer().set(keygen("timepiece"), PersistentDataType.BOOLEAN, true);
        workingMeta.setItemModel(NamespacedKey.minecraft("timepiece"));
        workingItem.setItemMeta(workingMeta);
        return workingItem;
    }

    @Override
    public Component AbilityTwoName() {
        return dess("<red>Ability Two</red> - Fireball");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("<red>Burn</red> your enemies in one fell swoop with a <red>fireball</red>.");
    }

    @Override
    //
    public void AbilityTwo(Player player,ItemStack abilityItem) {
        if (can_ability(ability_two_cooldown,ability_two_cooldowns,player.getUniqueId())) {
            ability_two_cooldowns.put(player.getUniqueId(),System.currentTimeMillis());
            handleFireball(player).runTaskTimer(Bukkit.getPluginManager().getPlugin("Freedom"), 0, 1);
        } else {
            double seconds = (double) (ability_two_cooldown - (System.currentTimeMillis() - ability_two_cooldowns.get(player.getUniqueId()))) / 1000;
            player.sendActionBar(dess("You can't use this ability yet, wait " + seconds + " seconds"));
        }
    }

    public ItemStack Fireball() {
        ItemStack Workingitem = ItemStack.of(Material.FIRE_CHARGE);
        ItemMeta meta = Workingitem.getItemMeta();
        meta.setItemModel(NamespacedKey.minecraft("fireball"));
        Workingitem.setItemMeta(meta);
        return Workingitem;
    }
    public void showSoulPoints(Player player) {
        double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        for (BossBar bossBar : player.activeBossBars() ) {
            bossBar.name().toString().contains("SoulPoints");
            player.hideBossBar(bossBar);
        }

        player.showBossBar(BossBar.bossBar(dess("SoulPoints"), (float) SoulPoints/10, BossBar.Color.BLUE, BossBar.Overlay.NOTCHED_10));
    }
    public BukkitRunnable handleFireball(Player player) {
        return new BukkitRunnable() {
            public Entity fireball;
            public Vector direction = player.getLocation().getDirection();
            public boolean rocketjump = false;
            @Override
            public void run() {
                if (fireball == null) {
                    fireball = player.getWorld().spawnEntity(player.getLocation(), EntityType.ITEM_DISPLAY);
                    ItemDisplay itemDisplay = (ItemDisplay) fireball;
                    itemDisplay.setItemStack(Fireball());
                    itemDisplay.teleport(player.getLocation().add(player.getLocation().getDirection()));
                    itemDisplay.setRotation(90,90);
                } else {
                    ItemDisplay itemDisplay = (ItemDisplay) fireball;
                    itemDisplay.teleport(itemDisplay.getLocation().add(direction.multiply(1)));
                    int entitycount = 0;
                    for (Entity entity : itemDisplay.getNearbyEntities(1,3,1)) {
                        if (entity instanceof Player instanceplayer) {
                            if (instanceplayer != player) {
                                entitycount = entitycount + 1;

                            } else {
                                Bukkit.getPluginManager().getPlugin("Freedom").getLogger().info("Player is in the fireball with the distance, " + itemDisplay.getLocation().distance(player.getLocation()));
                                if (itemDisplay.getLocation().distance(player.getLocation()) > 2) {
                                    Bukkit.getPluginManager().getPlugin("Freedom").getLogger().info("Player is in the fireball with the distance, " + itemDisplay.getLocation().distance(player.getLocation()));
                                    rocketjump = true;
                                }

                            }
                        } else {
                            entitycount = entitycount + 1;
                        }

                    }
                    if (itemDisplay.collidesAt(itemDisplay.getLocation())) {
                        if (rocketjump == true) {
                            player.setVelocity(new Vector(player.getVelocity().getX(), 2 + player.getPersistentDataContainer().get(keygen("SoulPoint"),PersistentDataType.DOUBLE), player.getVelocity().getY()));
                            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, 0d);
                            showSoulPoints(player);

                        }
                        player.getWorld().createExplosion(itemDisplay.getLocation(), 1.0F + entitycount, true, true);
                        itemDisplay.remove();
                        this.cancel();
                    }
                    if (entitycount >= 1) {
                        //spawn explosion
                        player.getWorld().createExplosion(itemDisplay.getLocation(), 1.0F + entitycount, true, true);
                        itemDisplay.remove();
                        this.cancel();
                    }

                }

            }
        };
    }

    @Override
    public Component Passive_Description() {
        return dess("You always have passive fire resistance.");
    }

    //Fire Resistance
    @Override
    public void Passive(Player player,Object event) {
            if (event instanceof PrePlayerAttackEntityEvent AttackEvent) {

                Entity entity = AttackEvent.getAttacked();
                entity.setFireTicks(200);
            }

    }

    @Override
    public Component ActivePassive_Description() {
        return dess("If you have this item, you have passive fire aspect");
    }

    @Override
    public void ActivePassive(Player player) {
        if (!player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 6000, 0));
        double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, SoulPoints - 5);
    }
    }


}
