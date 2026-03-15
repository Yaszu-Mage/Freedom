package xyz.yaszu.freedom.Soul;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import xyz.yaszu.freedom.Util.Util;

public class Orange extends Util implements Base_Soul, Listener {
    @Override
    public String Name_For_Container() {
        return "";
    }

    @Override
    public Component Name() {
        return null;
    }

    @Override
    public Component Description() {
        return null;
    }

    @Override
    public ItemStack Icon() {
        return null;
    }

    @Override
    public Component AbilityOneName() {
        return null;
    }

    @Override
    public Component AbilityOneDescription() {
        return null;
    }

    @Override
    public void AbilityOne(Player player) {

    }

    @Override
    public ItemStack Related_Item() {
        return null;
    }

    @Override
    public Component AbilityTwoName() {
        return null;
    }

    @Override
    public Component AbilityTwoDescription() {
        return null;
    }

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {

    }

    @Override
    public Component Passive_Description() {
        return null;
    }

    @Override
    public void Passive(Player player, Object event) {

    }

    @Override
    public Component ActivePassive_Description() {
        return null;
    }

    @Override
    public void ActivePassive(Player player) {

    }
}
