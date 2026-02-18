package xyz.yaszu.freedom.Soul;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public interface Base_Soul {
    //Name Used in Components
    public String Name_For_Container();
    //Name Used in UI / Nametag
    public Component Name();
    //Description used in UI
    public Component Description();
    // Icon Used in UI
    public ItemStack Icon();
    //Name for Ability One
    public Component AbilityOneName();

    // Description for Ability One
    public Component AbilityOneDescription();
    //Ability One - An ability that can be triggered with Input
    public void AbilityOne(Player player);
    public ItemStack Related_Item();
    //Name for Ability Two
    public Component AbilityTwoName();
    // Description for Ability Two
    public Component AbilityTwoDescription();
    //Ability Two - An ability that can be triggered using an ITEM and/or with Inputs
    public void AbilityTwo(Player player,ItemStack ability_item);
    public Component Passive_Description();
    //Passive - A passive that is active no matter what
    public void Passive(Player player, Object event);
    public Component ActivePassive_Description();

    //Active Passive - A passive that requires a condition to activate
    public void ActivePassive(Player player);

    public default boolean can_ability(long cooldown, HashMap<UUID, Long> cooldown_list, UUID player) {
        if (cooldown_list.get(player) == null) {
            return true;
        } else {
            if (cooldown_list.get(player) + cooldown > System.currentTimeMillis()) {
                return false;
            }
        }
        return true;
    }



}
