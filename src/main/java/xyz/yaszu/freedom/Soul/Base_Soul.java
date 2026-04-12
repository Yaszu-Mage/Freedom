package xyz.yaszu.freedom.Soul;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
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
    public void AbilityTwo(Player player,ItemStack ability_item) throws MineSkinException, DataRequestException;
    public Component Passive_Description();
    //Passive - A passive that is active no matter what
    public void Passive(Player player, Object event);

    // Active Passive - A passive that requires a condition to activate (like sneaking or specific stats)
    public default void playerSneakEvent(Player player) {}

    public Component ActivePassive_Description();
    public long AbilityTwo_Cooldown();
    public long AbilityOne_Cooldown();
    //Active Passive - A passive that requires a condition to activate
    public void ActivePassive(Player player);
    default long effective_cooldown(long base_cooldown, java.util.UUID playerUUID) {
        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(playerUUID);
        if (player != null) {
            String activeData = player.getPersistentDataContainer().get(xyz.yaszu.freedom.Util.FreedomKeys.activeArtifact(), org.bukkit.persistence.PersistentDataType.STRING);
            if (activeData != null && java.util.Arrays.asList(activeData.split(",")).contains("chronos")) {
                return (long) (base_cooldown * 0.7);
            }
        }
        return base_cooldown;
    }

    default boolean can_ability(long cooldown, java.util.HashMap<java.util.UUID, Long> cooldown_list, java.util.UUID player) {
        if (cooldown_list.get(player) == null) {
            return true;
        } else {
            if (cooldown_list.get(player) + effective_cooldown(cooldown,player) > System.currentTimeMillis()) {
                return false;
            }
        }
        return true;
    }



}
