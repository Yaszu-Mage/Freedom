package xyz.yaszu.freedom.Soul.Base;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;

public class BaseMocha extends Util implements Base_Soul, Listener {

    @Override
    public String Name_For_Container() {
        return "BaseMocha";
    }

    @Override
    public Component Name() {
        return dess("<color:#F9EBDE>Mocha</color:#F9EBDE>");
    }

    @Override
    public Component Description() {
        return dess("Creates a bond when your other half is found");
    }

    @Override
    public ItemStack Icon() {
        //TODO replace with blue clock
        return ItemStack.of(Material.COCOA_BEANS);
    }

    @Override
    public Component AbilityOneName() {
        return dess("<color:#F9EBDE> Selective TP </color>");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("You can selectively with the Cafe you are bonded with");
    }
    @Override
    public long AbilityOne_Cooldown() {return 45000L;}
    @Override
    public void AbilityOne(Player player) {
        if (!player.getPersistentDataContainer().has(keygen("tpyes"))) player.getPersistentDataContainer().set(keygen("tpyes"),PersistentDataType.BOOLEAN, true);
        player.getPersistentDataContainer().set(keygen("tpyes"),PersistentDataType.BOOLEAN, !player.getPersistentDataContainer().get(keygen("tpyes"),PersistentDataType.BOOLEAN));
        if (can_ability(AbilityOne_Cooldown(),abilityOneCooldowns,player.getUniqueId())) {
            //Do ability
            //TODO implement VFX
            if (player.getPersistentDataContainer().has(keygen("mochacafe"))) {

                Player mochacafe = Bukkit.getPlayer(player.getPersistentDataContainer().get(keygen("mochacafe"), PersistentDataType.STRING));
                if (mochacafe != null) {
                if (Bukkit.getPlayer(mochacafe.getPersistentDataContainer().get(keygen("mochacafe"),PersistentDataType.STRING)) == player) {
                    if ( player.getPersistentDataContainer().get(keygen("tpyes"),PersistentDataType.BOOLEAN) == true && mochacafe.getPersistentDataContainer().get(keygen("tpyes"),PersistentDataType.BOOLEAN) == false) {
                        mochacafe.sendMessage(player.getName() + " has requested to swap!");
                    } else {
                        mochacafe.sendMessage(player.getName() + " has rescinded their request to swap.");
                    }

                if (mochacafe.getLocation().distanceSquared(player.getLocation()) <= 2500) {
                if (mochacafe.getPersistentDataContainer().get(keygen("tpyes"),PersistentDataType.BOOLEAN)) {
                    player.getPersistentDataContainer().set(keygen("tpyes"),PersistentDataType.BOOLEAN,false);
                    mochacafe.getPersistentDataContainer().set(keygen("tpyes"),PersistentDataType.BOOLEAN,false);
                    Location doubleloc = mochacafe.getLocation().setRotation(0,0);
                    Location location = player.getLocation().setRotation(0,0);

                    player.teleport(doubleloc);
                    mochacafe.teleport(location);
                    abilityOneCooldowns.put(player.getUniqueId(),System.currentTimeMillis());
                    abilityOneCooldowns.put(mochacafe.getUniqueId(),System.currentTimeMillis());
                    location = location.add(0,0.5,0);
                    doubleloc = doubleloc.add(0,0.5,0);
                    ItemDisplay onedisplay = location.getWorld().spawn(location, ItemDisplay.class);
                    ItemDisplay twodisplay = doubleloc.getWorld().spawn(doubleloc, ItemDisplay.class);
                    onedisplay.setItemStack(ItemStack.of(Material.CLOCK));
                    twodisplay.setItemStack(ItemStack.of(Material.CLOCK));
                    mochacafe.getWorld().playSound(location,Sound.UI_BUTTON_CLICK,1,1);
                    player.getWorld().playSound(location,Sound.UI_BUTTON_CLICK,1,1);
                    drawCircle(player.getLocation(),1,player.getWorld(),16,Particle.REVERSE_PORTAL);
                    drawCircle(mochacafe.getLocation(),1,mochacafe.getWorld(),16,Particle.REVERSE_PORTAL);
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            onedisplay.remove();
                            twodisplay.remove();
                        }
                    }.runTaskLater(Freedom.get_plugin(),20);

                }
            } else {
                    player.sendMessage(dess("YOU ARE TOO FAR TO SWAP!"));
                    mochacafe.sendMessage(dess("YOU ARE TOO FAR TO SWAP!"));
                }
        } else {
            //TODO send message
        }
    }}}}
    public static void init(Player player) {
        player.getPersistentDataContainer().set(keygen("tpyes"),PersistentDataType.BOOLEAN,false);
    }
    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.CLOCK);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("Time slow");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("Slows time for all but the one you are bonded with");
    }
    public static HashMap<UUID,Long> abilityTwoCooldowns = new HashMap<>();
    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {

        if (can_ability(AbilityTwo_Cooldown(), abilityTwoCooldowns,player.getUniqueId())) {
            new BukkitRunnable() {
                public static List<Entity> affectedEntities = new ArrayList<>();
                int tick = 0;
                @Override
                public void run() {
                    List<Entity> nearguys = player.getNearbyEntities(5,5,5);
                    Player ignoreplayer = player;
                    for (Entity affected : affectedEntities) {
                        if (!nearguys.contains(affected)) {
                            if (affected instanceof Player instplayer) {
                                if (instplayer.getAttribute(Attribute.GRAVITY).getModifier(keygen("anticlock")) != null && instplayer != ignoreplayer) {
                                    instplayer.getAttribute(Attribute.GRAVITY).removeModifier(keygen("anticlock"));
                                    instplayer.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(keygen("anticlock"));
                                    instplayer.getAttribute(Attribute.JUMP_STRENGTH).removeModifier(keygen("anticlock"));
                                }
                            }
                        }
                    }
                    if (player.getPersistentDataContainer().has(keygen("mochacafe"))) {
                        Player mochacafe = Bukkit.getPlayer(player.getPersistentDataContainer().get(keygen("mochacafe"), PersistentDataType.STRING));
                        if (mochacafe != null) {
                            ignoreplayer = Bukkit.getPlayer(player.getPersistentDataContainer().get(keygen("mochacafe"), PersistentDataType.STRING));
                        }
                    }
                    for (Entity entity : nearguys) {
                        if (entity instanceof Player instplayer) {
                            affectedEntities.add(instplayer);
                            if (instplayer.getAttribute(Attribute.GRAVITY).getModifier(keygen("anticlock")) == null && instplayer != ignoreplayer) {
                                instplayer.getAttribute(Attribute.JUMP_STRENGTH).addModifier(new AttributeModifier(keygen("anticlock"),-100000000000d, AttributeModifier.Operation.ADD_NUMBER));
                                instplayer.getAttribute(Attribute.GRAVITY).addModifier(new AttributeModifier(keygen("anticlock"),10000000d, AttributeModifier.Operation.ADD_NUMBER));
                                instplayer.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(new AttributeModifier(keygen("anticlock"),-0.0375d, AttributeModifier.Operation.ADD_NUMBER));
                            }
                        }
                    }

                    if (tick > 10) {
                        this.cancel();
                    }
                    player.getLocation().getWorld().playSound(player.getLocation(),Sound.UI_BUTTON_CLICK,1,1);
                    drawCircle(player.getLocation(),1,player.getLocation().getWorld(),16,Particle.SOUL_FIRE_FLAME);
                    drawCircle(player.getLocation(),5,player.getLocation().getWorld(),16,Particle.SOUL_FIRE_FLAME);
                    tick++;
                }
                @Override
                public synchronized void cancel() throws IllegalStateException {
                    for (Entity affected : affectedEntities) {

                            if (affected instanceof Player instplayer) {
                                if (instplayer.getAttribute(Attribute.GRAVITY).getModifier(keygen("anticlock")) != null) {
                                    instplayer.getAttribute(Attribute.GRAVITY).removeModifier(keygen("anticlock"));
                                    instplayer.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(keygen("anticlock"));
                                    instplayer.getAttribute(Attribute.JUMP_STRENGTH).removeModifier(keygen("anticlock"));
                                }
                            }

                    }
                    Bukkit.getScheduler().cancelTask(getTaskId());
                }

            }.runTaskTimer(Freedom.get_plugin(),0,20);

            abilityTwoCooldowns.put(player.getUniqueId(),System.currentTimeMillis());
        } else {
            // TODO no no ability + time
        }
    }

    @Override
    public Component Passive_Description() {
        return dess("Shares you potion effects with your bonded half");
    }

    @Override
    public void Passive(Player player, Object event) {

    }
    public static HashMap<UUID,Boolean> haspassive = new HashMap<>();
    @EventHandler
    public void Passive_Potion(PlayerMoveEvent event) {

        Player player = event.getPlayer();
            if (getSoulType(player) == SoulTypes.Mocha || getSoulType(player) == SoulTypes.Cafe) {
                if (player.getPersistentDataContainer().has(keygen("mochacafe"))) {
                    Player mochacafe = Bukkit.getPlayer(player.getPersistentDataContainer().get(keygen("mochacafe"), PersistentDataType.STRING));
                    if (mochacafe != null) {
                        if (Bukkit.getPlayer(mochacafe.getPersistentDataContainer().get(keygen("mochacafe"),PersistentDataType.STRING)) == player) {
                        Collection<PotionEffect> potions = player.getActivePotionEffects();
                        Collection<PotionEffect> doublepotions = player.getActivePotionEffects();
                        potioncheck(player,mochacafe,potions,doublepotions);
                        }
                }
            }
        }
    }

    public static HashMap<UUID,List> potionchecker = new HashMap<>();
    public void potionCheck(Player clock, Player anticlock) {
        for (PotionEffect potionEffect : clock.getActivePotionEffects()) {
            if (!anticlock.hasPotionEffect(potionEffect.getType())) {
                anticlock.addPotionEffect(potionEffect);
            }
        }
    }
    public void potioncheck(Player player, Player mochacafe, Collection<PotionEffect> clockpotion,Collection<PotionEffect> anticlockpotion) {
        List<PotionEffect> effects = new ArrayList<>();
        potionchecker.put(player.getUniqueId(),effects);
        potionrealcheck(clockpotion, anticlockpotion, effects,player.getUniqueId());
        potionrealcheck(anticlockpotion, clockpotion, effects,player.getUniqueId());
        mochacafe.clearActivePotionEffects();
        player.clearActivePotionEffects();
        for (Object objpotion : potionchecker.get(player.getUniqueId())) {
            if (objpotion instanceof PotionEffect potion) {
                mochacafe.addPotionEffect(potion);
                player.addPotionEffect(potion);
            }

        }

    }

    public void potionrealcheck(Collection<PotionEffect> clockpotion, Collection<PotionEffect> anticlockpotion, Collection<PotionEffect> effects,UUID uuid) {
        for (PotionEffect potion : clockpotion) {
            int potionduration = potion.getDuration();
            int potionamplifier = potion.getAmplifier();
            int finaleffectduration = potionduration;
            int finaleffectamplifier = potionamplifier;
            for (PotionEffect antipotion : anticlockpotion) {
                int antipotionduration = antipotion.getDuration();
                int antipotionamplifier = antipotion.getAmplifier();
                finaleffectduration = Math.max(potionduration, antipotionduration);
                finaleffectamplifier = Math.max(potionamplifier, antipotionamplifier);
            }
            potionchecker.get(uuid).add(new PotionEffect(potion.getType(),finaleffectduration,finaleffectamplifier));
        }
    }

    @Override
    public Component ActivePassive_Description() {
        return dess("Chose those to bond with");
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 9000L;
    }

    @Override
    public void ActivePassive(Player player) {
        for (Entity entity : player.getNearbyEntities(2,2,2)) {
            if (entity instanceof  Player lookedat)
            if (lookedat != player) {
            if (lookedat.getPersistentDataContainer().has(keygen("soul"))) {

                SoulTypes soulType = SoulTypes.valueOf(lookedat.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
                Freedom.get_plugin().getLogger().info(String.valueOf(soulType == SoulTypes.Mocha || soulType == SoulTypes.Cafe));
                SoulTypes selfsoulType = SoulTypes.valueOf(lookedat.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
                if ((soulType == SoulTypes.Mocha || selfsoulType == SoulTypes.Cafe) || (soulType == SoulTypes.Cafe || selfsoulType == SoulTypes.Mocha)|| (soulType == SoulTypes.BaseCafe || soulType == SoulTypes.BaseMocha)){
                    player.getPersistentDataContainer().set(keygen("mochacafe"),PersistentDataType.STRING,lookedat.getName());

                }
            }
        }
    }
}
}


