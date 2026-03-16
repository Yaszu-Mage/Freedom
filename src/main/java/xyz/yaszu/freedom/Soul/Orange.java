package xyz.yaszu.freedom.Soul;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.CatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FrogWatcher;
import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Orange extends Util implements Base_Soul, Listener {
    @Override
    public String Name_For_Container() {
        return "Orange";
    }

    @Override
    public Component Name() {
        return dess("Orange");
    }

    @Override
    public Component Description() {
        return dess("What is it but magic?");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.CAULDRON);
    }

    @Override
    public Component AbilityOneName() {
        return dess("⬛⬛⬛⬛");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("⬛⬛⬛⬛");
    }

    @Override
    public void AbilityOne(Player player) {

    }

    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.WHEAT);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("⬛⬛⬛⬛");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("⬛⬛⬛⬛");
    }

    public long AbilityTwo_Cooldown = 30000;

    public static HashMap<UUID,Long> abilityTwoCooldownTime = new HashMap<>();

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        if (can_ability(AbilityTwo_Cooldown,abilityTwoCooldownTime,player.getUniqueId()) && !player.getPersistentDataContainer().has(keygen("disguised"), PersistentDataType.BOOLEAN)) {
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE,1,1);
            InventoryGui inventoryGui = new InventoryGui();
            inventoryGui.setInventory(player);
            player.openInventory(inventoryGui.getInventory());

        } else {
            // no no ability
            if (abilityTwoCooldownTime.get(player.getUniqueId()) != null) {
                double seconds = (double) (AbilityTwo_Cooldown - (System.currentTimeMillis() - abilityTwoCooldownTime.get(player.getUniqueId()))) / 1000;
                player.sendActionBar(dess("You can't use this ability yet, wait " + seconds + " seconds"));
            }
        }
    }

    public HashMap<UUID,MobDisguise> curses = new HashMap<>();


    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.getPersistentDataContainer().has(keygen("cursed"))) {
            if (Objects.equals(player.getPersistentDataContainer().get(keygen("cursed"), PersistentDataType.STRING), "Frog")) {
                curse(player);
            } else {
                player.getPersistentDataContainer().remove(keygen("cursed"));
            }
        }
    }

    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent damageByEntity) {
            Entity damager = damageByEntity.getDamager();

            if (damager instanceof Player killer) {
                Player player = event.getPlayer();
                if (player.getPersistentDataContainer().has(keygen("cursed"))) {
                    if (player.getPersistentDataContainer().get(keygen("cursed"),PersistentDataType.STRING) == "Frog") {
                        uncurse(player);
                    }
                }
            }
        } else {
            curse(event.getPlayer());
        }

    }

    @EventHandler
    public void InventoryClickEvent (InventoryClickEvent event) throws MineSkinException, DataRequestException {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof InventoryGui inventoryGui && event.getCurrentItem() != null) {
            // yayas correct holder
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();
            Player baller = Bukkit.getPlayer(UUID.fromString(item.getPersistentDataContainer().get(keygen("player_uuid"),PersistentDataType.STRING)));
            if (baller != null) {
            if (!baller.getPersistentDataContainer().has(keygen("cursed"))) {
                if (player.getPersistentDataContainer().get(keygen("cancurse"),PersistentDataType.BOOLEAN) == true) {
                    player.getPersistentDataContainer().set(keygen("cancurse"),PersistentDataType.BOOLEAN,false);
                    curse(baller);
                }
            } else {
                if (baller.getPersistentDataContainer().get(keygen("cursed"),PersistentDataType.STRING) == "Frog") {
                    player.getPersistentDataContainer().set(keygen("cancurse"),PersistentDataType.BOOLEAN,true);
                    uncurse(baller);
                } else {
                    if (player.getPersistentDataContainer().get(keygen("cancurse"),PersistentDataType.BOOLEAN) == true) {
                        player.getPersistentDataContainer().set(keygen("cancurse"),PersistentDataType.BOOLEAN,false);
                        curse(baller);
                    }

                }
            }
            player.closeInventory();
            event.setCancelled(true);

        }
    }
    }

    public void uncurse(Player baller) {
        baller.getPersistentDataContainer().remove(keygen("cursed"));
        baller.removePotionEffect(PotionEffectType.WEAKNESS);
        curses.get(baller.getUniqueId()).removeDisguise();
        curses.remove(baller.getUniqueId());
    }

    public void curse(Player baller) {
        baller.getPersistentDataContainer().set(keygen("cursed"), PersistentDataType.STRING,"Frog");
        MobDisguise mobDisguise = new MobDisguise(DisguiseType.FROG);
        mobDisguise.addPlayer(baller);
        mobDisguise.setEntity(baller);
        baller.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,PotionEffect.INFINITE_DURATION,1,true,false));
        mobDisguise.startDisguise();
        FrogWatcher watcher = (FrogWatcher) mobDisguise.getWatcher();
        watcher.setVariant(Frog.Variant.COLD);
        curses.put(baller.getUniqueId(),mobDisguise);
    }

    @Override
    public Component Passive_Description() {
        return dess("⬛⬛⬛⬛");
    }


    public static HashMap<UUID, MobDisguise> disguiseCat = new HashMap<>();
    public static enum Cursetype {
        Cat,
        Frog
    }
    public class InventoryGui implements InventoryHolder {
        public Inventory inventory;
        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Player player) {
            int max_players = Bukkit.getMaxPlayers();
            int remainder = max_players % 9;
            Freedom.get_plugin().getLogger().info(String.valueOf(remainder));
            if (remainder == 0) {
                inventory = Bukkit.createInventory(this,max_players);
            } else {
                inventory = Bukkit.createInventory(this,max_players - (remainder));
            }
            int iteration = 0;
            for (Player instancedPlayer : Bukkit.getOnlinePlayers()) {
                if (instancedPlayer.getUniqueId() != player.getUniqueId()){
                    ItemStack skull = getSkull(instancedPlayer);
                    SkullMeta meta = (SkullMeta) skull.getItemMeta();
                    meta.getPersistentDataContainer().set(keygen("player_uuid"), PersistentDataType.STRING, instancedPlayer.getUniqueId().toString());
                    SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
                    String name = instancedPlayer.getName();
                    switch (soulType) {
                        case Black:
                            meta.displayName(soulListener.black.Name().append(dess(" " + name)));
                            break;
                        case Green:
                            meta.displayName(soulListener.green.Name().append(dess(" " + name)));
                            break;
                        case Red:
                            meta.displayName(soulListener.red.Name().append(dess(" " + name)));
                            break;
                        case Blue:
                            meta.displayName(soulListener.blue.Name().append(dess(" " + name)));
                            break;
                        case Purple:
                            meta.displayName(soulListener.purple.Name().append(dess(" " + name)));
                            break;

                    }
                    skull.displayName();
                    skull.setItemMeta(meta);
                    inventory.setItem(iteration,skull);
                    iteration++;
                }
            }
        }

    }
    @Override
    public void Passive(Player player, Object event) {
        if (player.getPersistentDataContainer().has(keygen("cursed"))) {
            if (player.getPersistentDataContainer().get(keygen("cursed"),PersistentDataType.STRING) == "Frog") {
                player.sendMessage(dess("You cannot uncurse yourself!"));
                return;
            }
        }
        if (disguiseCat.containsKey(player.getUniqueId())) {
            player.sendMessage(dess("You are not a cat"));
            MobDisguise disguise = disguiseCat.get(player.getUniqueId());
            disguise.removeDisguise();
            disguiseCat.remove(player.getUniqueId());
            player.getPersistentDataContainer().remove(keygen("cursed"));
        } else {
            player.sendMessage(dess("You are now a cat"));
            MobDisguise disguise = new MobDisguise(DisguiseType.CAT);
            disguise.addPlayer(player);
            disguise.setEntity(player);
            disguise.startDisguise();
            CatWatcher watcher = (CatWatcher) disguise.getWatcher();
            watcher.setType(Cat.Type.ALL_BLACK);
            player.getWorld().spawnParticle(Particle.ASH,player.getLocation(),16);
            player.getPersistentDataContainer().set(keygen("cursed"),PersistentDataType.STRING,"Cat");
            disguiseCat.put(player.getUniqueId(),disguise);
        }

    }






    @Override
    public Component ActivePassive_Description() {
        return dess("⬛⬛⬛⬛");
    }

    @Override
    public void ActivePassive(Player player) {

    }
}
