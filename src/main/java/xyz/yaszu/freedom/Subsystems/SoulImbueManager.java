package xyz.yaszu.freedom.Subsystems;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

public class SoulImbueManager extends Util implements Listener {


    @EventHandler
    public void ApplyBuff(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (player.getInventory().getItemInMainHand() != null) {
                if (isImbued(player.getInventory().getItemInMainHand())) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    SoulTypes soulType = SoulTypes.valueOf(item.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
                    switch (soulType) {
                        case Red,BaseRed -> {
                            event.getEntity().setFireTicks(100);
                            player.getLocation().getWorld().playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1, 1);
                        }
                        case Green,BaseGreen -> {
                            player.setHealth(player.getHealth() + 1);
                        }
                        case Purple,BasePurple -> {
                            //more xp
                        }
                        case Yellow,BaseYellow,Blue,BaseBlue -> {
                            if (item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable) {
                                // damage = 0 is full health, higher = more used
                                damageable.setDamage(Math.max(0,damageable.getDamage() - 1));
                                item.setItemMeta((ItemMeta) damageable);
                                player.getPersistentDataContainer().set(FreedomKeys.soulPoint(),PersistentDataType.DOUBLE,player.getPersistentDataContainer().get(FreedomKeys.soulPoint(),PersistentDataType.DOUBLE) - 5);
                                player.sendMessage(dess("Healed 25 durability."));
                            }
                        }
                        case Orange,BaseOrange -> {
                            //turn into a cat
                        }
                        case Cafe,BaseCafe -> {

                        }
                        case Black,BaseBlack -> {
                            //Idek
                        }
                        case Mocha,BaseMocha -> {

                        }
                    }
                }
            }
        }
    }




    public boolean isImbued(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().has(keygen("soul")) || item.getItemMeta().getPersistentDataContainer().has(keygen("soultwo"));
    }


    /**
     * Adds alcohol to a player.
     *
     * @param item   Itemstack to imbue
     * @param player Player who's soul is being imbued.
     * @param soulType soultype to imbue
     * @param selfimbue if the soul is being imbued by the player itself
     */
    public static void ImbueItem(ItemStack item, Player player, SoulTypes soulType, boolean selfimbue) {
        if (selfimbue) {
            ItemMeta meta = item.getItemMeta();
            if (meta.getPersistentDataContainer().has(keygen("soul"))) {
                meta.getPersistentDataContainer().set(keygen("soultwo"), PersistentDataType.STRING, soulType.name());
                meta.getPersistentDataContainer().set(keygen("soulownertwo"), PersistentDataType.STRING, player.getUniqueId().toString());
            } else {
                meta.getPersistentDataContainer().set(keygen("soul"), PersistentDataType.STRING, soulType.name());
                meta.getPersistentDataContainer().set(keygen("soulowner"), PersistentDataType.STRING, player.getUniqueId().toString());
            }
            item.setItemMeta(meta);
        } else {

        }
    }
    /**
     * Adds alcohol to a player.
     *
     * @param item   Itemstack to unimbue
     * @param slot   slot within item to unimbue
     */
    public static void UnImbueItem(ItemStack item, int slot) {
        switch (slot) {
            case 0 -> {
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().remove(keygen("soul"));
                meta.getPersistentDataContainer().remove(keygen("soulowner"));
                item.setItemMeta(meta);
            }
            case 1 -> {
                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().remove(keygen("soultwo"));
                meta.getPersistentDataContainer().remove(keygen("soulownertwo"));
                item.setItemMeta(meta);
            }
        }
    }
}
