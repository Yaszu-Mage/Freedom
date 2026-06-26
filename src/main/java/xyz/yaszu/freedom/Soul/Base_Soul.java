package xyz.yaszu.freedom.Soul;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Util.FreedomKeys;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static xyz.yaszu.freedom.Subsystems.SoulImbueManager.getWhoImbued;
import static xyz.yaszu.freedom.Subsystems.SoulImbueManager.isImbued;
import static xyz.yaszu.freedom.Util.Util.*;

public interface Base_Soul {
    /**
     * Name Used in Components
     */
    public String Name_For_Container();
    /**
     * //Name Used in UI / Nametag
     * @return Component used in UI and Nametag
     */
    public Component Name();
    /**
     * Description Used in UI
     * @return Component used in UI
     */
    public Component Description();

    /**
     * Icon Used in UI
     * @return ItemStack used in UI
     */
    public ItemStack Icon();
    /**
     * Ability One Name
     * @return Component used in UI
     */
    public Component AbilityOneName();
    /**
     * Description for Ability One
     * @return Component used in UI
     */
    public Component AbilityOneDescription();
    /**
     * Ability One - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability One for
     */
    public void AbilityOne(Player player);

    /**
     * Ability One - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability One for
     * @param is_imbue checking if it's imbued
     */
    default void AbilityOne(Player player, boolean is_imbue) {
        AbilityOne(player);
    }

    /**
     * Related Item - An item that is required to use this ability
     * @return ItemStack used in ability usage
     */
    public ItemStack Related_Item();
    /**
     * Ability Two Name
     * @return Component used in UI
     */
    public Component AbilityTwoName();
    /**
     * Description for Ability Two
     * @return Component used in UI
     */
    public Component AbilityTwoDescription();
    /**
     * Ability Two - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability Two for
     * @param ability_item ItemStack used in ability usage
     * @throws MineSkinException if there's an error with MineSkin
     * @throws DataRequestException if there's an error with data request
     */
    public void AbilityTwo(Player player,ItemStack ability_item) throws MineSkinException, DataRequestException;

    /**
     * Ability Two - An ability that can be triggered using an ITEM and/or with Inputs
     * @param player Player to handle Ability Two for
     * @param ability_item ItemStack used in ability usage
     * @param is_imbue checking if it's imbued
     * @throws MineSkinException if there's an error with MineSkin
     * @throws DataRequestException if there's an error with the data request
     */
    default void AbilityTwo(Player player, ItemStack ability_item, boolean is_imbue) throws MineSkinException, DataRequestException {
        AbilityTwo(player, ability_item);
    }

    /**
     * Passive Description
     * @return Component used in UI
     */
    public Component Passive_Description();

    /**
     * Passive - A passive that is active no matter what
     * @param player Player to handle passive for
     * @param event Event that triggered the passive
     */
    public void Passive(Player player, Object event);

    /**
     * Active Passive - A passive that requires a condition to activate (like sneaking or specific stats)
     * @param player Player to handle active passive for
     */
    public default void playerSneakEvent(Player player) {}

    /**
     * Active Passive Description
     * @return Component used in UI
     */
    public Component ActivePassive_Description();

    /**
     * Ability Two Cooldown Time
     * @return Cooldown time in milliseconds
     */
    public long AbilityTwo_Cooldown();

    /**
     * Ability One Cooldown Time
     * @param given Given object, depends on the moveset, could be a player
     * @return Cooldown time in milliseconds
     */
    public long AbilityOne_Cooldown(Object given);
    /**
     * Active Passive - A passive that requires a condition to activate (like sneaking or specific stats)
     * @param player Player to handle active passive for
     */
    public void ActivePassive(Player player);

    /**
     * Chronos Artifact Cooldown Reduction - If the player has the Chronos artifact, reduce the cooldown by 30%
     * @param base_cooldown Cooldown time in milliseconds
     * @param playerUUID    Player UUID to check for the Chronos artifact
     * @return Cooldown time in milliseconds, reduced by 30% if the player has the Chronos artifact
     */
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

    /**
     * Boolean check to see if the player can use the ability
     * @param cooldown Cooldown time in milliseconds, given from AbilityOne_Cooldown or AbilityTwo_Cooldown
     * @see Base_Soul#AbilityOne_Cooldown(Object)
     * @see Base_Soul#AbilityTwo_Cooldown()
     * @param cooldown_list Cooldown list to check against, given from AbilityOneCooldowns or AbilityTwoCooldowns within Util
     * @param player Player to check for ability use
     * @return boolean if player can use ability
     */
    default boolean can_ability(long cooldown, java.util.HashMap<java.util.UUID, Long> cooldown_list, java.util.UUID player) {
        org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(player);
        assert p!= null;
        if (p.getPersistentDataContainer().has(FreedomKeys.silence())) {
            p.sendMessage(dess("<red>You are silenced and cannot use this ability.</red>"));
            return false;
        }
        if (p != null && xyz.yaszu.freedom.Subsystems.AdminManager.isSudo(p)) {
            return true;
        }
        if (cooldown_list.get(player) == null) {
            return true;
        } else {
            if (cooldown_list.get(player) + effective_cooldown(cooldown,player) > System.currentTimeMillis()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the player is alive
     * @param player Player to check for alive status
     * @return boolean if player is alive
     */
    default boolean alive(Player player) {
        return Life_and_Death.is_alive(player);
    }

    /**
     * Checks if the player is imbued
     * @param player Player to check for imbued status
     * @return boolean if player is imbued
     */
    default boolean ImbueActive(Player player) {
        return getImbuePlayer(player) != null;
    }

    /**
     * Checks if the player is imbued by the soul owner
     * @param holder Player that is holding the imbued item
     * @param soulOwner Player that is the soul owner
     * @return boolean if player is imbued by the soul owner
     */
    default ItemStack getOwnersImbuedItemInHand(Player holder, Player soulOwner) {
        ItemStack mainHand = holder.getInventory().getItemInMainHand();
        if (isImbuedBy(mainHand, soulOwner)) return mainHand;

        ItemStack offHand = holder.getInventory().getItemInOffHand();
        if (isImbuedBy(offHand, soulOwner)) return offHand;

        return null;
    }

    /**
     * Determines if the provided item is imbued by the specified soul owner.
     *
     * @param item The {@code ItemStack} to check if it is imbued.
     * @param soulOwner The {@code Player} who is being checked as the potential soul owner of the imbued item.
     * @return {@code true} if the item is imbued and the specified {@code soulOwner} is among the players who imbued it;
     *         {@code false} otherwise.
     */
    default boolean isImbuedBy(ItemStack item, Player soulOwner) {
        if (item == null || item.getType().isAir() || !isImbued(item)) return false;
        List<Player> owners = getWhoImbued(item);
        if (owners == null) return false;
        for (Player owner : owners) {
            if (owner != null && owner.getUniqueId().equals(soulOwner.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the player who is imbued by the specified soul owner.
     * @param player Player to check for imbued player
     * @return Player who is imbued by the specified soul owner, or null if no such player is found.
     */
    default Player getImbuePlayer(Player player){
        AtomicReference<Player> imbuePlayer = new AtomicReference<>();
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (imbuePlayer.get() != null) return;

            if (getOwnersImbuedItemInHand(p, player) != null) {
                imbuePlayer.set(p);
            }
        });
        return imbuePlayer.get();
    }

    /**
     * Lite methods are used by dead soul owners and executed through the current imbued-item holder.
     * @param holder Player that is holding the imbued item
     * @param soulOwner Player that is the soul owner
     * @return boolean if the player can use the lite method
     */
    default boolean canUseLiteOne(Player holder, Player soulOwner) {

        if (!isValidLiteLink(holder, soulOwner)) return false;
        if (!holder.isSneaking()) return false;

        long now = System.currentTimeMillis();
        long liteCooldown = Math.max(2000L, (long) (AbilityOne_Cooldown(null) * 1.5));
        Long lastUsed = abilityOneCooldowns.get(soulOwner.getUniqueId());
        if (lastUsed != null && lastUsed + liteCooldown > now) return false;

        abilityOneCooldowns.put(soulOwner.getUniqueId(), now);
        return true;
    }

    /**
     * Lite methods are used by dead soul owners and executed through the current imbued-item holder. Checks if the ability can be used
     * @param holder Player that is holding the imbued item
     * @param soulOwner Player that is the soul owner
     * @return boolean if the player can use the lite method
     */
    default boolean canUseLiteTwo(Player holder, Player soulOwner) {
        if (!isValidLiteLink(holder, soulOwner)) return false;
        if (!holder.isSneaking()) return false;

        long now = System.currentTimeMillis();
        long liteCooldown = Math.max(5000L, (long) (AbilityTwo_Cooldown() * 2.0));
        Long lastUsed = abilityTwoCooldowns.get(soulOwner.getUniqueId());
        if (lastUsed != null && lastUsed + liteCooldown > now) return false;

        abilityTwoCooldowns.put(soulOwner.getUniqueId(), now);
        return true;
    }

    /**
     * Checks if the provided player is a valid link between the holder and the soul owner.
     * @param holder Player that is holding the imbued item
     * @param soulOwner Player that is the soul owner
     * @return boolean if the player is a valid link between the holder and the soul owner
     */
    default boolean isValidLiteLink(Player holder, Player soulOwner) {
        if (holder == null || soulOwner == null) return false;
        if (!soulOwner.isOnline() || !holder.isOnline()) return false;
        if (alive(soulOwner)) return false;
        if (!alive(holder)) return false;
        if (holder.isDead() || soulOwner.isDead()) return false;
        if (holder.getWorld() != soulOwner.getWorld()) return false;
        if (holder.getLocation().distanceSquared(soulOwner.getLocation()) > 2500) return false;
        return getOwnersImbuedItemInHand(holder, soulOwner) != null;
    }

    // Lite methods are used by dead soul owners and executed through the current imbued-item holder.

    /**
     * Lite method for Ability One.
     * @param holder Player that is holding the imbued item
     * @param soulOwner Player that is the soul owner
     */
    default void AbilityOneLite(Player holder, Player soulOwner) {
        if (!canUseLiteOne(holder, soulOwner)) return;
        AbilityOne(holder, true);
    }

    /**
     * Lite method for Ability Two.
     * @param holder Player that is holding the imbued item
     * @param soulOwner Player that is the soul owner
     * @param abilityItem ItemStack used in ability usage
     * @throws MineSkinException
     * @throws DataRequestException
     */
    default void AbilityTwoLite(Player holder, Player soulOwner, ItemStack abilityItem) throws MineSkinException, DataRequestException {
        if (!canUseLiteTwo(holder, soulOwner)) return;
        AbilityTwo(holder, abilityItem, true);
    }

}
