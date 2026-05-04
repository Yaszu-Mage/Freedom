package xyz.yaszu.freedom.Soul.Base;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Util.Util;

public class BaseNone extends Util implements Base_Soul, Listener {
    @Override
    public String Name_For_Container() {
        return "BaseNone";
    }

    @Override
    public Component Name() {
        return dess("None");
    }

    @Override
    public Component Description() {
        return dess("Be yourself");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.OAK_LOG);
    }

    @Override
    public Component AbilityOneName() {
        return dess("⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("⬛⬛⬛⬛⬛⬛");
    }

    @Override
 public void AbilityOne(Player player) {
        AbilityOne(player, false);
    }

    @Override
 public void AbilityOne(Player player, boolean is_imbue) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }

    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.OAK_LOG);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("⬛⬛⬛⬛⬛⬛");
    }

    @Override
 public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        AbilityTwo(player, ability_item, false);
    }

    @Override
 public void AbilityTwo(Player player, ItemStack ability_item, boolean is_imbue) throws MineSkinException, DataRequestException {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }

    @Override
    public Component Passive_Description() {
        return dess("⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void Passive(Player player, Object event) {

    }

    @Override
    public Component ActivePassive_Description() {
        return dess("⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 0;
    }

    @Override
    public long AbilityOne_Cooldown(Object obj) {
        return 0;
    }

    @Override
    public void ActivePassive(Player player) {

    }


}


