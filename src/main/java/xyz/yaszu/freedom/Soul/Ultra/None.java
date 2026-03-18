package xyz.yaszu.freedom.Soul.Ultra;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Util.Util;

public class None extends Util implements Base_Soul, Listener {
    @Override
    public String Name_For_Container() {
        return "None";
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
    public void ActivePassive(Player player) {

    }


}
