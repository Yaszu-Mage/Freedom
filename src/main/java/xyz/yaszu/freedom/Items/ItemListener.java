package xyz.yaszu.freedom.Items;

import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.Artifacts.ArtifactManager;
import xyz.yaszu.freedom.Items.Artifacts.Base_Artifact;
import xyz.yaszu.freedom.Items.ColorSpecific.Rifle;
import xyz.yaszu.freedom.Items.ColorSpecific.TimePiece;
import xyz.yaszu.freedom.Items.Parts.Burger;
import xyz.yaszu.freedom.Items.Parts.SpellFocus;
import xyz.yaszu.freedom.Items.Relics.Glock;
import xyz.yaszu.freedom.Items.Relics.PainScythe;
import xyz.yaszu.freedom.Items.Upgrades.Evolve;
import xyz.yaszu.freedom.Items.Upgrades.Reset;
import xyz.yaszu.freedom.Items.Upgrades.Revival;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;

public class ItemListener extends Util implements Listener {
    public static final Map<String, BaseItem> ITEMS = new HashMap<>();

    public static void registerItems() {
        register(new Evolve(), "evolutionstone");
        register(new Revival(), "revival");
        register(new Rifle(), "rifle");
        register(new TimePiece(), "timepiece");
        register(new Reset(), "resetstone");
        register(new Burger(),"burger");
        register(new PainScythe(),"painscythe");
        register(new Glock(), "glock");
        register(new SpellFocus.Orb(), "orb");
        register(new SpellFocus.Staff(), "staff");
        register(new SpellFocus.Grimoire(), "grimoire");

        ArtifactManager.registerArtifacts();
        for (Base_Artifact artifact : ArtifactManager.ARTIFACTS.values()) {
            register(artifact, artifact.getID());
        }
    }

    private static void register(BaseItem item, String id) {
        ITEMS.put(id, item);
        if (item.recipe() != null) {
            Bukkit.addRecipe(item.recipe());
        }
    }

    @EventHandler
    public void onOffhandItem(PlayerInventorySlotChangeEvent event) {
        ItemStack mainhand = event.getPlayer().getInventory().getItemInMainHand();
        ItemStack offhand = event.getPlayer().getInventory().getItemInOffHand();
        if (event.getSlot() != 40 && event.getSlot() >= 8|| (event.getSlot() > 8) && (event.getSlot() != 40)) return;
        offhand = event.getNewItemStack();
            if (offhand.getItemMeta() != null) {
                if (offhand.getItemMeta().getPersistentDataContainer().has(FreedomKeys.itemId()) || offhand.getItemMeta().getPersistentDataContainer().has(keygen("rifle"))) {
                    if (offhand.getItemMeta() instanceof CrossbowMeta meta) {
                        if (event.getSlot() == 40) {
                            meta.setChargedProjectiles(new ArrayList<>());
                        } else {
                            meta.setChargedProjectiles(List.of(ItemStack.of(Material.ARROW)));
                        }
                        offhand.setItemMeta(meta);
                        event.getPlayer().getInventory().setItem(event.getSlot(), offhand);
                    }
                }
            }
        }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Cancel book opening if a spell focus is being used in either hand
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && (item.getType() == Material.WRITTEN_BOOK || item.getType() == Material.WRITABLE_BOOK)) {
                ItemStack otherHand = event.getHand() == EquipmentSlot.HAND ?
                        event.getPlayer().getInventory().getItemInOffHand() :
                        event.getPlayer().getInventory().getItemInMainHand();

                if (otherHand != null && otherHand.hasItemMeta() &&
                        otherHand.getItemMeta().getPersistentDataContainer().has(FreedomKeys.spellFocus(), PersistentDataType.BYTE)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta()) {
            if (item.getItemMeta().getPersistentDataContainer().has(keygen("rifle"))) {
                if (event.getPlayer().getInventory().getItemInMainHand().getItemMeta() != null) {
                    if (!event.getPlayer().hasCooldown(ITEMS.get("rifle").item())) {
                        ITEMS.get("rifle").effect(event.getPlayer(), event, item);
                    }
                }
                event.setCancelled(true);
            }
            String itemId = item.getItemMeta().getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING);
            if (itemId != null) {
                BaseItem baseItem = ITEMS.get(itemId);
                if (baseItem != null) {
                    baseItem.effect(event.getPlayer(), event, item);
                    event.setCancelled(true);
                }
            }
        }
    }

    private final Random random = new Random();
    private static final int SUS_AMOUNT = 2;

    @EventHandler
    public void onPlayerSwaptoOffHand(PlayerSwapHandItemsEvent event) {
        ItemStack mainhand = event.getMainHandItem();
        ItemStack offhand = event.getOffHandItem();

        if (mainhand.getItemMeta() != null) {
            if (mainhand.getItemMeta().getPersistentDataContainer().has(FreedomKeys.itemId()) || mainhand.getItemMeta().getPersistentDataContainer().has(keygen("rifle"))) {
                if (mainhand.getItemMeta() instanceof CrossbowMeta meta) {
                    meta.setChargedProjectiles(List.of(ItemStack.of(Material.ARROW)));
                    mainhand.setItemMeta(meta);
                    Freedom.get_plugin().getLogger().info("mainhand");
                }
            }
        }
        if (offhand.getItemMeta() != null) {
            if (offhand.getItemMeta().getPersistentDataContainer().has(FreedomKeys.itemId())|| offhand.getItemMeta().getPersistentDataContainer().has(keygen("rifle"))) {
                if (offhand.getItemMeta() instanceof CrossbowMeta meta) {
                    meta.setChargedProjectiles(new ArrayList<>());
                    offhand.setItemMeta(meta);
                    Freedom.get_plugin().getLogger().info("offhand");
                }
            }
        }
    }

    @EventHandler
    public void onCrossbowLoad(EntityLoadCrossbowEvent event) {
        Freedom.get_plugin().getLogger().info("loaded");
        if (event.getEntity() instanceof Player player) {
            ItemStack mainhand = player.getInventory().getItemInMainHand();
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (mainhand.getItemMeta() != null) {
                if (mainhand.getItemMeta().getPersistentDataContainer().has(FreedomKeys.itemId()) || mainhand.getItemMeta().getPersistentDataContainer().has(keygen("rifle"))) {
                    if (mainhand.getItemMeta() instanceof CrossbowMeta meta) {
                        meta.setChargedProjectiles(new ArrayList<>());
                        mainhand.setItemMeta(meta);
                        Freedom.get_plugin().getLogger().info("mainhand");
                    }
                }
            }
            if (offhand.getItemMeta() != null) {
                if (offhand.getItemMeta().getPersistentDataContainer().has(FreedomKeys.itemId())|| offhand.getItemMeta().getPersistentDataContainer().has(keygen("rifle"))) {
                    if (offhand.getItemMeta() instanceof CrossbowMeta meta) {
                        meta.setChargedProjectiles(new ArrayList<>());
                        offhand.setItemMeta(meta);
                        Freedom.get_plugin().getLogger().info("offhand");
                    }
                }
            }

        }
    }

    @EventHandler
    public void onCrossbowFire(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getBow().getItemMeta().getPersistentDataContainer().has(FreedomKeys.itemId()) || event.getBow().getItemMeta().getPersistentDataContainer().has(keygen("rifle"))) {
                if (event.getBow().getItemMeta() instanceof CrossbowMeta meta) {
                    meta.setChargedProjectiles(List.of(ItemStack.of(Material.ARROW)));
                    event.getBow().setItemMeta(meta);
                }
                event.getProjectile().remove();
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        if (event.getBlock().getType().equals(BlockType.SUSPICIOUS_GRAVEL) || event.getBlock().getType().equals(BlockType.SUSPICIOUS_SAND)) {
            if (random.nextInt(101) > 80) {
                int chancer = random.nextInt(SUS_AMOUNT + 1);
                switch (chancer) {
                    case 1 -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), ITEMS.get("revival").item());
                    case 2 -> event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), ITEMS.get("resetstone").item());
                }
            }
        }
    }
}
