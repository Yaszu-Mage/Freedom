package xyz.yaszu.freedom.Soul;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static xyz.yaszu.freedom.Subsystems.SoulImbueManager.getWhoImbued;
import static xyz.yaszu.freedom.Subsystems.SoulImbueManager.isImbued;
import static xyz.yaszu.freedom.Util.Util.abilityOneCooldowns;
import static xyz.yaszu.freedom.Util.Util.abilityTwoCooldowns;

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
    default void AbilityOne(Player player, boolean is_imbue) {
        AbilityOne(player);
    }
    public ItemStack Related_Item();
    //Name for Ability Two
    public Component AbilityTwoName();
    // Description for Ability Two
    public Component AbilityTwoDescription();
    //Ability Two - An ability that can be triggered using an ITEM and/or with Inputs
    public void AbilityTwo(Player player,ItemStack ability_item) throws MineSkinException, DataRequestException;
    default void AbilityTwo(Player player, ItemStack ability_item, boolean is_imbue) throws MineSkinException, DataRequestException {
        AbilityTwo(player, ability_item);
    }
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
        org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(player);
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

    default boolean alive(Player player) {
        return Life_and_Death.is_alive(player);
    }

    default boolean ImbueActive(Player player) {
        return getImbuePlayer(player) != null;
    }

    default ItemStack getOwnersImbuedItemInHand(Player holder, Player soulOwner) {
        ItemStack mainHand = holder.getInventory().getItemInMainHand();
        if (isImbuedBy(mainHand, soulOwner)) return mainHand;

        ItemStack offHand = holder.getInventory().getItemInOffHand();
        if (isImbuedBy(offHand, soulOwner)) return offHand;

        return null;
    }

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

    default boolean canUseLiteOne(Player holder, Player soulOwner) {

        if (!isValidLiteLink(holder, soulOwner)) return false;
        if (!holder.isSneaking()) return false;

        long now = System.currentTimeMillis();
        long liteCooldown = Math.max(2000L, (long) (AbilityOne_Cooldown() * 1.5));
        Long lastUsed = abilityOneCooldowns.get(soulOwner.getUniqueId());
        if (lastUsed != null && lastUsed + liteCooldown > now) return false;

        abilityOneCooldowns.put(soulOwner.getUniqueId(), now);
        return true;
    }

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
    default void AbilityOneLite(Player holder, Player soulOwner) {
        if (!canUseLiteOne(holder, soulOwner)) return;
        AbilityOne(holder, true);
    }

    default void AbilityTwoLite(Player holder, Player soulOwner, ItemStack abilityItem) throws MineSkinException, DataRequestException {
        if (!canUseLiteTwo(holder, soulOwner)) return;
        AbilityTwo(holder, abilityItem, true);
    }

}
