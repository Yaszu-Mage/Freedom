package xyz.yaszu.freedom.Items.Swords;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.util.Random;

public class VisionHandler extends Util implements Listener {


    public static final VisionHandler instance = new VisionHandler();



    public static enum HandTypes {
        Main,
        Off,
        Both,
        None
    }
    public static void sendVision(Component message, Sword.SwordType type, Player player) {
        new VisionEvent(message, type,player).callEvent();
    }

    public static boolean isHoldingSword(Player player) {
        if (player.getInventory().getItemInMainHand() != null) {
            ItemStack item = player.getInventory().getItemInMainHand();
            return item.getPersistentDataContainer().has(keygen("sword"));
        }
        if (player.getInventory().getItemInOffHand() != null) {
            ItemStack item = player.getInventory().getItemInOffHand();
            return item.getPersistentDataContainer().has(keygen("sword"));
        }
        return false;
    }


    public static HandTypes whichHandHoldingSword(Player player) {
        if (isHoldingSword(player)) {
            ItemStack MainHand = player.getInventory().getItemInMainHand();
            ItemStack OffHand = player.getInventory().getItemInOffHand();
            if (MainHand.getPersistentDataContainer().has(keygen("sword")) && OffHand.getPersistentDataContainer().has(keygen("sword"))) {
                return HandTypes.Both;
            }
            if (MainHand.getPersistentDataContainer().has(keygen("sword"))) {
                return HandTypes.Main;
            }
            if (OffHand.getPersistentDataContainer().has(keygen("sword"))) {
                return HandTypes.Off;
            }
        }
        return HandTypes.None;
    }
    public static void randomVisions() {
        new BukkitRunnable() {
            Random random = new Random();
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (random.nextInt(0,1000) == 0) {
                        switch (whichHandHoldingSword(player)) {
                            case Both -> {
                                Sword.SwordType mainHand = Sword.SwordType.valueOf(player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(keygen("sword"), PersistentDataType.STRING));
                                Sword.SwordType offHand = Sword.SwordType.valueOf(player.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer().get(keygen("sword"), PersistentDataType.STRING));
                                Sword mainHandSword = Sword.getSwordfromEnum(mainHand);
                                Sword offHandSword = Sword.getSwordfromEnum(offHand);

                                if (random.nextBoolean()) {
                                    sendVision(mainHandSword.visions().get(random.nextInt(mainHandSword.visions().size() -1 )), mainHand, player);
                                } else {
                                    sendVision(offHandSword.visions().get(random.nextInt(offHandSword.visions().size() - 1)), offHand, player);
                                }
                            }
                            case Main -> {
                                Sword.SwordType mainHand = Sword.SwordType.valueOf(player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(keygen("sword"), PersistentDataType.STRING));
                                Sword mainHandSword = Sword.getSwordfromEnum(mainHand);
                                sendVision(mainHandSword.visions().get(random.nextInt(mainHandSword.visions().size() -1 )), mainHand, player);
                            }
                            case Off -> {
                                Sword.SwordType offHand = Sword.SwordType.valueOf(player.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer().get(keygen("sword"), PersistentDataType.STRING));
                                Sword offHandSword = Sword.getSwordfromEnum(offHand);
                                sendVision(offHandSword.visions().get(random.nextInt(offHandSword.visions().size() - 1)), offHand, player);
                            }
                            case None -> {}
                        }
                    }
                });
            }
        }.runTaskTimer(Freedom.get_plugin(), 0,20);
    }


    @EventHandler
    public void onVision(VisionEvent event) {
        Player player = event.getPlayer();
        player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(20,0));
        player.addPotionEffect(PotionEffectType.DARKNESS.createEffect(20,0));
        player.addPotionEffect(PotionEffectType.NAUSEA.createEffect(20,0));
        player.setFoodLevel(event.getPlayer().getFoodLevel()-8);
        player.sendMessage(event.getMessage());
        player.damage(2);
        player.playSound(player.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE,10f,2f);
    }
    public static class VisionEvent extends Event {
        private static final HandlerList HANDLER_LIST = new HandlerList();
        private final Component message;
        private final Sword.SwordType type;
        private final Player player;
        public Player getPlayer() {
            return player;
        }
        public Component getMessage() {
            return message;
        }
        public Sword.SwordType getType() {
            return type;
        }
        public VisionEvent(Component message, Sword.SwordType type, Player player) {
            this.message = message;
            this.type = type;
            this.player = player;
        }
        public static HandlerList getHandlerList() {
            return HANDLER_LIST;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLER_LIST;
        }
    }


}
