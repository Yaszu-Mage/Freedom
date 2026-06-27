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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.World;
import xyz.yaszu.freedom.Blocks.BaseBlock;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.Artifacts.ArtifactManager;
import xyz.yaszu.freedom.Items.Artifacts.Base_Artifact;
import xyz.yaszu.freedom.Items.ColorSpecific.Railgun;
import xyz.yaszu.freedom.Items.ColorSpecific.Rifle;
import xyz.yaszu.freedom.Items.ColorSpecific.TimePiece;
import xyz.yaszu.freedom.Items.Default.deepslateBricks;
import xyz.yaszu.freedom.Items.Default.endRod;
import xyz.yaszu.freedom.Items.Drinks.Ale;
import xyz.yaszu.freedom.Items.Drinks.BajaBlast;
import xyz.yaszu.freedom.Items.Drinks.Beer;
import xyz.yaszu.freedom.Items.Drinks.Wine;
import xyz.yaszu.freedom.Items.Food.*;
import xyz.yaszu.freedom.Items.Parts.*;
import xyz.yaszu.freedom.Items.Relics.Glock;
import xyz.yaszu.freedom.Items.Relics.PainScythe;
import xyz.yaszu.freedom.Items.Relics.SoulGlass;
import xyz.yaszu.freedom.Items.Swords.Items.*;
import xyz.yaszu.freedom.Items.Swords.Sword;
import xyz.yaszu.freedom.Items.Upgrades.AstralItem;
import xyz.yaszu.freedom.Items.Upgrades.Evolve;
import xyz.yaszu.freedom.Items.Upgrades.Reset;
import xyz.yaszu.freedom.Items.Upgrades.Revival;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;

import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.keygen;

public class ItemListener implements Listener {
    public static final Map<String, BaseItem> ITEMS = new HashMap<>();
    public static final Map<String, BaseItem> RELICS = new HashMap<>();
    public static HashMap<UUID, HashMap<Sword.SwordType, Long>> SWORD_COOLDOWNS = new HashMap<>();

    // Shared key prefix used by both ItemListener and RandomChestGenerator
    public static final String RELIC_SPAWNED_PREFIX = "relicSpawned_";

    public static void registerItems() {
        SoulGlass soulGlass = new SoulGlass();
        register(new Ale(), "ale", false);
        register(new Beer(), "beer", false);
        register(new Wine(), "wine", false);
        register(new Evolve(), "evolutionstone", false);
        register(new Revival(), "revival", false);
        register(new Rifle(), "rifle", false);
        register(new TimePiece(), "timepiece", false);
        register(new Reset(), "resetstone", false);
        register(new Burger(), "burger", false);
        register(new Pizza(), "pizza", false);
        register(new AstralItem(), "astral", false);
        register(new PainScythe(), "painscythe", true);
        register(new Glock(), "glock", true);
        register(new deepslateBricks(), "deepslatebricks", false);
        //register(new SpellFocus.Orb(), "orb", false);
        //register(new SpellFocus.Staff(), "staff", false);
        //register(new SpellFocus.Grimoire(), "grimoire", false);
        register(new ScythePhighting(), "scythephighting", false);
        register(new Grapple_Hook(), "grapplehook", false);
        register(new Railgun(), "railgun", true);
        register(new Shawarma(), "shawarma", false);
        //register(new Alfajores(), "alfajores", false);
        register(new Lollipop(), "lollipop", false);
        register(new BaseBackpack(), "basebackpack", false);
        register(new DoubleBackpack(), "doublebackpack", false);
        register(new CrunchwrapSupreme(), "crunchwrapsupreme", false);
        register(new Falafel(), "falafel", false);
        register(new BajaBlast(), "bajablast", false);
        //register(soulGlass, "soulglass", true);
        register(new Darkheart(), "darkheart", true);
        register(new Venomshank(), "venomshank", true);
        register(new Windforce(), "windforce", true);
        register(new Firebrand(), "firebrand", true);
        Bukkit.getPluginManager().registerEvents(soulGlass, Freedom.get_plugin());
        register(new Illumina(), "illumina", false);
        register(new Ghostwalker(), "ghostwalker", false);
        register(new Icedagger(), "icedagger", false);
        register(new Darkheart(), "darkheart", true);
        register(new endRod(),"end_rod",false);
        ArtifactManager.registerArtifacts();
        for (Base_Artifact artifact : ArtifactManager.ARTIFACTS.values()) {
            register(artifact, artifact.getID(), false);
        }
    }

    public static void register(BaseItem item, String id, Boolean relic) {
        ITEMS.put(id, item);
        if (item.recipe() != null) {
            Bukkit.addRecipe(item.recipe());
        }
        if (relic || item.getType() == CustomItemType.SWORD) {
            RELICS.put(id, item);
        }
    }

    /**
     * Returns the overworld (first world) consistently.
     * Both ItemListener and RandomChestGenerator must use this same method
     * so relic-spawned flags are read/written to the same world PDC.
     */
    public static World getTrackedWorld() {
        return Bukkit.getWorlds().stream().findFirst().orElseThrow();
    }

    /**
     * Marks a relic as no longer in-world (dropped item despawned or was destroyed).
     * Clears the flag so chest generators may spawn it again.
     */
    public static void unmarkRelicSpawned(String id) {
        getTrackedWorld().getPersistentDataContainer().remove(keygen(RELIC_SPAWNED_PREFIX + id));
        Freedom.get_plugin().getLogger().info("ItemListener: relic '" + id + "' unmarked (removed from world)");
    }

    @EventHandler
    public void onOffhandItem(PlayerInventorySlotChangeEvent event) {
        ItemStack mainhand = event.getPlayer().getInventory().getItemInMainHand();
        ItemStack offhand = event.getPlayer().getInventory().getItemInOffHand();
        if (event.getSlot() != 40 && event.getSlot() >= 8 || (event.getSlot() > 8) && (event.getSlot() != 40))
            return;
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
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            ItemStack item = event.getItem();
            if (item == null) return;
            if (item.getPersistentDataContainer().has(keygen("sword"))) {
                String itemId = item.getItemMeta().getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING);
                if (itemId != null) {
                    BaseItem baseItem = ITEMS.get(itemId);
                    if (baseItem instanceof BaseBlock || baseItem.getType() == CustomItemType.BLOCK) {
                        return;
                    }
                    baseItem.effect(event.getPlayer(), event, item);
                    event.setCancelled(true);
                    return;
                }
            }

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

            item = event.getItem();
            ItemStack handItem = event.getHand() == EquipmentSlot.HAND ?
                    event.getPlayer().getInventory().getItemInMainHand() :
                    event.getPlayer().getInventory().getItemInOffHand();

            if (item != null && !item.getType().isAir() && item.hasItemMeta() && item.equals(handItem)) {
                if (item.getItemMeta().getPersistentDataContainer().has(FreedomKeys.itemId())) {
                    if (item.getItemMeta().getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING).equals("grapplehook")) {
                        return;
                    }
                }
                if (item.getItemMeta().getPersistentDataContainer().has(keygen("rifle"))) {
                    if (event.getPlayer().getInventory().getItemInMainHand().getItemMeta() != null) {
                        if (xyz.yaszu.freedom.Subsystems.AdminManager.isSudo(event.getPlayer()) || !event.getPlayer().hasCooldown(ITEMS.get("rifle").item())) {
                            ITEMS.get("rifle").effect(event.getPlayer(), event, item);
                        }
                    }
                    event.setCancelled(true);
                }
                String itemId = item.getItemMeta().getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING);
                if (itemId != null) {
                    BaseItem baseItem = ITEMS.get(itemId);
                    if (baseItem != null) {
                        if (itemId.equals("soulglass")) {
                            baseItem.effect(event.getPlayer(), event, item);
                            return;
                        }
                        baseItem.effect(event.getPlayer(), event, item);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (droppedItem != null && droppedItem.hasItemMeta()) {
            if (droppedItem.getItemMeta().getPersistentDataContainer().has(FreedomKeys.itemId())) {
                // Custom item dropped normally — no effect triggered
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
            if (offhand.getItemMeta().getPersistentDataContainer().has(FreedomKeys.itemId()) || offhand.getItemMeta().getPersistentDataContainer().has(keygen("rifle"))) {
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
                if (offhand.getItemMeta().getPersistentDataContainer().has(FreedomKeys.itemId()) || offhand.getItemMeta().getPersistentDataContainer().has(keygen("rifle"))) {
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
    public void ItemDespawnEvent(ItemDespawnEvent event) {
        if (event.getEntity() instanceof Item item) {
            ItemStack stack = item.getItemStack();
            if (stack != null) {
                RELICS.forEach((id, relic) -> {
                    // isSimilar ignores stack size so a dropped relic (amount=1) still matches
                    if (stack.isSimilar(relic.item())) {
                        unmarkRelicSpawned(id);
                    }
                });
            }
        }
    }

    @EventHandler
    public void ItemDestruction(EntityDamageEvent event) {
        if (event.getEntity() instanceof Item item) {
            ItemStack stack = item.getItemStack();
            if (stack != null) {
                RELICS.forEach((id, relic) -> {
                    if (stack.isSimilar(relic.item())) {
                        unmarkRelicSpawned(id);
                    }
                });
            }
        }
    }

    @EventHandler
    public void PlayerCraftEvent(CraftItemEvent event) {
        if (event.getRecipe() != null) {
            if (event.getRecipe().getResult() != null) {
                RELICS.forEach((id, item) -> {
                    if (event.getRecipe().getResult().isSimilar(item.item())) {
                        // Use the same tracked world as everywhere else
                        if (!getTrackedWorld().getPersistentDataContainer().has(keygen(RELIC_SPAWNED_PREFIX + id))) {
                            getTrackedWorld().getPersistentDataContainer().set(
                                    keygen(RELIC_SPAWNED_PREFIX + id), PersistentDataType.BYTE, (byte) 1);
                        } else {
                            if (event.getWhoClicked() instanceof Player player) {
                                player.sendMessage(dess("YOU CANNOT CRAFT THIS. ONE OF A KIND"));
                            }
                            event.setCancelled(true);
                        }
                    }
                });
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