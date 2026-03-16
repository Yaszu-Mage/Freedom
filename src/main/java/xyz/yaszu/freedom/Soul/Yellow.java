package xyz.yaszu.freedom.Soul;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.yaszu.freedom.Util.Util;

public class Yellow  extends Util implements Base_Soul {
    @Override
    public String Name_For_Container() {
        return "Yellow";
    }

    @Override
    public Component Name() {
        return dess("<yellow>Yellow</yellow>");
    }

    @Override
    public Component Description() {
        return dess("Clock Yaoi where?");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.YELLOW_DYE);
    }

    @Override
    public Component AbilityOneName() {
        return dess("⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("⬛⬛⬛⬛⬛⬛");
    }
    Blue blue = new Blue();
    @Override
    public void AbilityOne(Player player) {
        blue.AbilityOne(player);
    }

    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.CLOCK);
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
        blue.ActivePassive(player);
    }
}
