package xyz.yaszu.freedom.Soul.Ultra;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base.BaseBlue;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.Util;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Yellow extends Util implements Base_Soul, Listener {
    @Override
    public String Name_For_Container() {
        return "Yellow";
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
        return dess("<Yellow>Ability One</Yellow> - COIL");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("While triggering the clock item, Yellow gains speed I for 10 seconds.");
    }

    @Override
    public void AbilityOne(Player player) {
        if (can_ability(AbilityOne_Cooldown(),abilityOneCooldowns,player.getUniqueId())){
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
            abilityOneCooldowns.put(player.getUniqueId(),System.currentTimeMillis());
            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (!player.getPersistentDataContainer().has(keygen("clockcoil"))) {
                        player.getPersistentDataContainer().set(keygen("clockcoil"), PersistentDataType.INTEGER, 1);
                    }
                    if (player.getPersistentDataContainer().has(keygen("doubleclock"))) {
                        if (Bukkit.getPlayer(player.getPersistentDataContainer().get(keygen("doubleclock"), PersistentDataType.STRING)) != null) {
                            //apply buffs + draw Line
                            Player doubleclock = Bukkit.getPlayer(player.getPersistentDataContainer().get(keygen("doubleclock"), PersistentDataType.STRING));
                            try {


                            if (doubleclock.getLocation().distanceSquared(player.getLocation()) <= 100) {
                            Color color = Color.YELLOW;
                            int clockcoil = player.getPersistentDataContainer().get(keygen("clockcoil"), PersistentDataType.INTEGER);
                            switch (clockcoil) {
                                case 1 -> {
                                    color = Color.RED;
                                    doubleclock.addPotionEffect(PotionEffectType.STRENGTH.createEffect(40,1));
                                }
                                case 2 -> {
                                    color = Color.LIME;
                                    doubleclock.addPotionEffect(PotionEffectType.REGENERATION.createEffect(40,1));
                                }
                                case 3 -> {
                                    color = Color.AQUA;
                                    doubleclock.addPotionEffect(PotionEffectType.SPEED.createEffect(40,1));
                                }
                            }
                            drawLine(doubleclock.getLocation().clone().add(0,1,0),player.getLocation().clone().add(0,1,0),player.getWorld(),32,Particle.DUST,new Particle.DustOptions(color,0.5f));
                        }} catch (Exception ignored) {}
                        }

                    }
                    int clockcoil = player.getPersistentDataContainer().get(keygen("clockcoil"), PersistentDataType.INTEGER);
                    switch (clockcoil) {
                        case 1 -> {
                            player.addPotionEffect(PotionEffectType.STRENGTH.createEffect(40,1));
                        }
                        case 2 -> {
                            player.addPotionEffect(PotionEffectType.REGENERATION.createEffect(40,1));
                        }
                        case 3 -> {
                            player.addPotionEffect(PotionEffectType.SPEED.createEffect(40,1));
                        }
                    }
                    if (tick >= 3600) {
                        this.cancel();
                    }
                    tick++;
                }
            }.runTaskTimer(Freedom.get_plugin(),0,1);
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
        ItemStack helmet = ItemStack.of(Material.DIAMOND_HELMET);
        helmet.addEnchantment(Enchantment.PROTECTION,4);
        helmet.addEnchantment(Enchantment.UNBREAKING,3);
        helmet.addEnchantment(Enchantment.VANISHING_CURSE,1);
        helmet.addEnchantment(Enchantment.MENDING,1);
        ItemStack chestplate = ItemStack.of(Material.DIAMOND_CHESTPLATE);
        chestplate.addEnchantment(Enchantment.PROTECTION,4);
        chestplate.addEnchantment(Enchantment.UNBREAKING,3);
        chestplate.addEnchantment(Enchantment.VANISHING_CURSE,1);
        chestplate.addEnchantment(Enchantment.MENDING,1);
        ItemStack leggings = ItemStack.of(Material.DIAMOND_LEGGINGS);
        leggings.addEnchantment(Enchantment.PROTECTION,4);
        leggings.addEnchantment(Enchantment.UNBREAKING,3);
        leggings.addEnchantment(Enchantment.VANISHING_CURSE,1);
        leggings.addEnchantment(Enchantment.MENDING,1);
        ItemStack boots = ItemStack.of(Material.DIAMOND_BOOTS);
        boots.addEnchantment(Enchantment.PROTECTION,4);
        boots.addEnchantment(Enchantment.UNBREAKING,3);
        boots.addEnchantment(Enchantment.VANISHING_CURSE,1);
        boots.addEnchantment(Enchantment.MENDING,1);
        for (int iteration = 5; iteration < 12; iteration++) {
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
        return 60000;
    }

    @Override
    public long AbilityOne_Cooldown() {
        return 120000;
    }



    @Override
    public void ActivePassive(Player player) {
        if (player.getPersistentDataContainer().has(keygen("clockcoil"))) {

            int clockcoil = player.getPersistentDataContainer().get(keygen("clockcoil"), PersistentDataType.INTEGER);
            switch (clockcoil) {
                case 1 -> {
                    player.getPersistentDataContainer().set(keygen("clockcoil"), PersistentDataType.INTEGER, 2);
                    player.sendActionBar(dess("<Green>Healing Mode"));
                    try {
                        player.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(new AttributeModifier(keygen("clockcoil"),0.05, AttributeModifier.Operation.ADD_NUMBER));
                        player.getAttribute(Attribute.MAX_HEALTH).addModifier(new AttributeModifier(keygen("clockcoil"),5, AttributeModifier.Operation.ADD_NUMBER));
                        player.getAttribute(Attribute.ATTACK_DAMAGE).removeModifier(new AttributeModifier(keygen("clockcoil"),2, AttributeModifier.Operation.ADD_NUMBER));
                        player.getAttribute(Attribute.ATTACK_SPEED).removeModifier(new AttributeModifier(keygen("clockcoil"),-0.2, AttributeModifier.Operation.ADD_NUMBER));
                    } catch (Exception ignored) {}

                    player.getWorld().playSound(player.getLocation(), "custom.orchestra0",1,1);
                }
                case 2 -> {
                    player.getPersistentDataContainer().set(keygen("clockcoil"), PersistentDataType.INTEGER, 3);
                    player.sendActionBar(dess("<Blue>Speed Mode"));
                    try {
                        player.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(new AttributeModifier(keygen("clockcoil"),0.05, AttributeModifier.Operation.ADD_NUMBER));
                        player.getAttribute(Attribute.MAX_HEALTH).removeModifier(new AttributeModifier(keygen("clockcoil"),5, AttributeModifier.Operation.ADD_NUMBER));
                        player.getAttribute(Attribute.ATTACK_DAMAGE).removeModifier(new AttributeModifier(keygen("clockcoil"),2, AttributeModifier.Operation.ADD_NUMBER));
                        player.getAttribute(Attribute.ATTACK_SPEED).removeModifier(new AttributeModifier(keygen("clockcoil"),-0.2, AttributeModifier.Operation.ADD_NUMBER));
                    } catch (Exception ignored) {}
                    player.getWorld().playSound(player.getLocation(), "custom.orchestra0",1,1.1f);
                }
                case 3 -> {
                    player.getPersistentDataContainer().set(keygen("clockcoil"), PersistentDataType.INTEGER, 1);
                    player.sendActionBar(dess("<Red>Damage Mode"));
                    try {
                        player.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(new AttributeModifier(keygen("clockcoil"),0.05, AttributeModifier.Operation.ADD_NUMBER));
                        player.getAttribute(Attribute.MAX_HEALTH).removeModifier(new AttributeModifier(keygen("clockcoil"),5, AttributeModifier.Operation.ADD_NUMBER));
                        player.getAttribute(Attribute.ATTACK_DAMAGE).addModifier(new AttributeModifier(keygen("clockcoil"),4, AttributeModifier.Operation.ADD_NUMBER));
                        player.getAttribute(Attribute.ATTACK_SPEED).addModifier(new AttributeModifier(keygen("clockcoil"),-0.2, AttributeModifier.Operation.ADD_NUMBER));
                    } catch (Exception ignored) {}
                    player.getWorld().playSound(player.getLocation(), "custom.orchestra0",1,1.2f);
                }
            }
        } else {
            player.getPersistentDataContainer().set(keygen("clockcoil"), PersistentDataType.INTEGER, 1);
            player.getWorld().playSound(player.getLocation(), "custom.orchestra0",1,0);
        }

    }
}
