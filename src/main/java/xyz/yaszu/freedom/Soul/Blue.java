package xyz.yaszu.freedom.Soul;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Util.Util.dess;

public class Blue extends Util implements Base_Soul {
    @Override
    public String Name_For_Container() {
        return "Blue";
    }

    @Override
    public Component Name() {
        return dess("<blue>Blue</blue>");
    }

    @Override
    public Component Description() {
        return dess("You are shelled within a castle of your own making");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.SHIELD);
    }

    @Override
    public Component AbilityOneName() {
        return dess("<blue> Ability One </blue> - ⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void AbilityOne(Player player) {

    }

    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.SHIELD);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("<blue> Ability Two </blue> - ⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) {

    }

    @Override
    public Component Passive_Description() {
        return dess("⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void Passive(Player player, Object event) {

    }

    @Override
    public Component ActivePassive_Description() {
        return dess("⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void ActivePassive(Player player) {

    }
}
