package xyz.yaszu.freedom.Soul.Base;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.Util;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BaseYellow extends Util implements Base_Soul, Listener {
    @Override
    public String Name_For_Container() {
        return "BaseYellow";
    }
    public static BaseBlue blue = new BaseBlue();
    @Override
    public Component Name() {
        return dess("<Yellow>Yellow</Yellow>");
    }

    @Override
    public Component Description() {
        return dess("What is it but time?");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.CLOCK);
    }

    @Override
    public Component AbilityOneName() {
        return dess("<Yellow>Ability One</Yellow> - FAST FORWARD");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("While triggering the clock item, Yellow gains speed I for 10 seconds.");
    }

    @Override
    public void AbilityOne(Player player) {
        if (player.hasPotionEffect(PotionEffectType.SPEED)) return;
        if (can_ability(AbilityOne_Cooldown(),abilityOneCooldowns,player.getUniqueId())){
            player.addPotionEffect(PotionEffectType.SPEED.createEffect(200,0));
            abilityOneCooldowns.put(player.getUniqueId(),System.currentTimeMillis());
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
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE,1,1);
        }

    }

    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.CLOCK);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("<Yellow>Ability Two</Yellow> - CLOCK YES I WILL JOIN THE FIGHT");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("When triggered, Yellow summons 5 zombies.");
    }
    public static int zombdistance = 2;
    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        if (can_ability(AbilityTwo_Cooldown(),abilityTwoCooldowns,player.getUniqueId())) {


        Scoreboard score = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team;
        if (score.getTeam(player.getUniqueId().toString()) == null) score.registerNewTeam(player.getUniqueId().toString());
        team = score.getTeam(player.getUniqueId().toString());
        team.addPlayer(player);

        Location center = player.getLocation();
        World world = center.getWorld();
        Location originalspawn = center.clone().add(center.getDirection().multiply(-zombdistance));
        team.setAllowFriendlyFire(false);
        List<Entity> spawned = new ArrayList<>();
        ItemStack helmet = ItemStack.of(Material.IRON_HELMET);
        helmet.addEnchantment(Enchantment.PROTECTION,2);
        helmet.addEnchantment(Enchantment.UNBREAKING,3);
        helmet.addEnchantment(Enchantment.VANISHING_CURSE,1);
        helmet.addEnchantment(Enchantment.MENDING,1);
        ItemStack chestplate = ItemStack.of(Material.IRON_CHESTPLATE);
        chestplate.addEnchantment(Enchantment.PROTECTION,2);
        chestplate.addEnchantment(Enchantment.UNBREAKING,3);
        chestplate.addEnchantment(Enchantment.VANISHING_CURSE,1);
        chestplate.addEnchantment(Enchantment.MENDING,1);
        ItemStack leggings = ItemStack.of(Material.IRON_LEGGINGS);
        leggings.addEnchantment(Enchantment.PROTECTION,2);
        leggings.addEnchantment(Enchantment.UNBREAKING,3);
        leggings.addEnchantment(Enchantment.VANISHING_CURSE,1);
        leggings.addEnchantment(Enchantment.MENDING,1);
        ItemStack boots = ItemStack.of(Material.IRON_BOOTS);
        boots.addEnchantment(Enchantment.PROTECTION,2);
        boots.addEnchantment(Enchantment.UNBREAKING,3);
        boots.addEnchantment(Enchantment.VANISHING_CURSE,1);
        boots.addEnchantment(Enchantment.MENDING,1);
        for (int iteration = 5; iteration < 10; iteration++) {
            Location spawn = rotpointX(center,iteration*5,originalspawn);
            Entity entity = world.spawnEntity(spawn, EntityType.ZOMBIE);
            spawned.add(entity);
            team.addEntity(entity);
            Zombie zombie = (Zombie) entity;
            zombie.getEquipment().setHelmet(helmet);
            zombie.getEquipment().setChestplate(chestplate);
            zombie.getEquipment().setLeggings(leggings);
            zombie.getEquipment().setBoots(boots);
            world.playSound(spawn, "custom.clockyes",1,1);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : spawned) {
                    Zombie zombie = (Zombie) entity;
                    if (zombie.getLocation().distanceSquared(player.getLocation()) >= 625) {
                        zombie.teleport(player.getLocation());
                    }
                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 0, 20);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : spawned) {
                    entity.remove();
                }
            }
        }.runTaskLater(Freedom.get_plugin(), 1800);
        team.setAllowFriendlyFire(false);
            if (player.getPersistentDataContainer().has(keygen("doubleclock"))) {
                if (Bukkit.getPlayer(player.getPersistentDataContainer().get(keygen("doubleclock"), PersistentDataType.STRING)) != null) {
                    team.addPlayer(Bukkit.getPlayer(player.getPersistentDataContainer().get(keygen("doubleclock"), PersistentDataType.STRING)));
                    Bukkit.getPlayer(player.getPersistentDataContainer().get(keygen("doubleclock"), PersistentDataType.STRING)).setScoreboard(score);
                }

            }
        player.setScoreboard(score);
        abilityTwoCooldowns.put(player.getUniqueId(),System.currentTimeMillis());
        }
    }

    @Override
    public Component Passive_Description() {
        return dess("Linked in Park: You can link with another player, when that player is killed, you are enraged.");
    }

    @Override
    public void Passive(Player player, Object event) {
        for (Entity entity : player.getNearbyEntities(2,2,2)) {
            if (entity instanceof  Player lookedat)
                if (lookedat != player) {
                    if (lookedat.getPersistentDataContainer().has(keygen("soul"))) {

                        SoulTypes soulType = SoulTypes.valueOf(lookedat.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
                        Freedom.get_plugin().getLogger().info(String.valueOf(soulType == SoulTypes.Blue || soulType == SoulTypes.Yellow));
                        SoulTypes selfsoulType = SoulTypes.valueOf(lookedat.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
                        if ((soulType == SoulTypes.Blue || selfsoulType == SoulTypes.Yellow) || (soulType == SoulTypes.Yellow || selfsoulType == SoulTypes.Blue)|| (soulType == SoulTypes.BaseYellow || soulType == SoulTypes.BaseBlue)){
                            player.getPersistentDataContainer().set(keygen("doubleclock"),PersistentDataType.STRING,lookedat.getName());

                        }
                    }
                }
        }
    }

    @Override
    public Component ActivePassive_Description() {
        return dess("Light Mend: Using soul points, you can rewind time and heal 25 durability of the Item you are holding");
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 120000;
    }

    @Override
    public long AbilityOne_Cooldown() {
        return 30000;
    }

    @Override
    public void ActivePassive(Player player) {
        blue.ActivePassive(player);
    }
}
