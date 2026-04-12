package xyz.yaszu.freedom.Items.Relics;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;

public class PainScythe extends Util implements BaseItem, Listener {
    @Override
    public ItemStack item() {
        ItemStack itemStack = ItemStack.of(Material.DIAMOND_SWORD);
        ItemMeta meta = itemStack.getItemMeta();
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"painscythe");
        meta.getPersistentDataContainer().set(painKey, PersistentDataType.INTEGER, 0);
        meta.setItemModel(NamespacedKey.minecraft("jx1dx1"));
        meta.displayName(Util.dess("<shadow:#000000FF><b><red>Pain Scythe</red>"));
        meta.setUnbreakable(true);
        meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(keygen("PainScythe"),-1.25,AttributeModifier.Operation.ADD_NUMBER));
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(keygen("PainScythe"), 16, AttributeModifier.Operation.ADD_NUMBER));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }

    @Override
    public Recipe recipe() {
        RecipeChoice template = new RecipeChoice.MaterialChoice(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        RecipeChoice base = new RecipeChoice.MaterialChoice(Material.NETHERITE_SWORD);
        RecipeChoice addition = new RecipeChoice.MaterialChoice(Material.ENCHANTED_GOLDEN_APPLE);
        return new SmithingTransformRecipe(painKey,item(),template,base,addition);
    }

    @EventHandler
    public void onArmSwing(PlayerArmSwingEvent swingEvent) {
        Player player = swingEvent.getPlayer();
        if (player.getInventory().getItemInMainHand().getPersistentDataContainer().has(painKey)) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getInventory().getItemInMainHand().getPersistentDataContainer().has(painKey)) {
                    if (!painmap.containsKey(player.getUniqueId())) {
                        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
                        meta.getPersistentDataContainer().set(painKey, PersistentDataType.INTEGER, 0);
                        player.getInventory().getItemInMainHand().setItemMeta(meta);
                    }
                }

            }
        }.runTaskLater(Freedom.get_plugin(),50);
    }
    }


    public List<Component> Jx1dx1 = List.of(
            dess("<shadow:#000000FF><b>IM.<red>VERY</red>.<dark_red>VERY</dark_red>.<color:#ff2200>EVIL</color>"),
            dess("<shadow:#000000FF><b>THIS.IS.NOT.A.<color:#ff2200>DAYCARE</color>"),
            dess("<shadow:#000000FF><b>ALL.IN.<color:#ff2200>DUE</color>.<color:#ffae00>TIME</color>")
    );

    public static Random random = new Random();

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand().getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING) == null) return;
        if (event.getPlayer().getInventory().getItemInMainHand() == null) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING).equals("painscythe")) {
            Integer change = random.nextInt(10000);
            if (change <= 10) {
            Integer messageIndex = random.nextInt(Jx1dx1.size());
            switch (messageIndex) {
                case 0:
                    event.getPlayer().sendActionBar(Jx1dx1.get(messageIndex));
                    break;
                case 1:
                    event.getPlayer().sendActionBar(Jx1dx1.get(messageIndex));
                    break;
                case 2:
                    event.getPlayer().sendActionBar(Jx1dx1.get(messageIndex));
                    event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_DISPENSER_DISPENSE,1,1);
                    break;
                default:
                    event.getPlayer().sendActionBar(Jx1dx1.getFirst());
                    break;
            }
        }
    }}

    public static HashMap<UUID,Boolean> painmap = new HashMap<>();
    public static NamespacedKey painKey = keygen("PainScythe");
    @EventHandler
    public void PrePlayerAttackEvent(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING) == null) return;
        if (player.getInventory().getItemInMainHand().getPersistentDataContainer().get(FreedomKeys.itemId(), PersistentDataType.STRING) == "painscythe") {
            try {
                player.setHealth(player.getHealth() - 1);
            } catch (Exception ignored) {

            }

            ItemStack stack = player.getInventory().getItemInMainHand();
            ItemMeta meta = stack.getItemMeta();
            Integer pain = meta.getPersistentDataContainer().get(painKey, PersistentDataType.INTEGER) + 1;
            painmap.put(player.getUniqueId(), true);

            if (pain >= 5) {
                if (event.getAttacked() instanceof LivingEntity entity) {
                    player.heal(5);
                    entity.damage(10, player);
                }
                meta.getPersistentDataContainer().set(painKey, PersistentDataType.INTEGER, 0);
            } else {

            meta.getPersistentDataContainer().set(painKey, PersistentDataType.INTEGER, pain);
        }
            stack.setItemMeta(meta);
            if (event.getAttacked() instanceof LivingEntity entity) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if(entity.isDead()) {
                            player.getLocation().getWorld().playSound(player.getLocation(),"custom.jx1dx1",1,1);
                            player.getLocation().getWorld().spawnParticle(Particle.SOUL, player.getLocation(), 100);
                        }
                    }
                }.runTaskLater(Freedom.get_plugin(),20);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    painmap.remove(player.getUniqueId());
                }
            }.runTaskLater(Freedom.get_plugin(),10);
        }
    }

}
