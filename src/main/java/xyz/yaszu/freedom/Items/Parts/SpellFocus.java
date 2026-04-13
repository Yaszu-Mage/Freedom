package xyz.yaszu.freedom.Items.Parts;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Alchemy.SpellCompiler;
import xyz.yaszu.freedom.GUI.BaseItems;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class SpellFocus implements Listener {


    public static class Orb extends Util implements BaseItem {

        @Override
        public ItemStack item() {
            ItemStack item  = ItemStack.of(Material.RECOVERY_COMPASS);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(dess("<gradient:#9300ff:#00ffff>Spell Focus: Orb</gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(dess("<gray>A portable focus for casting mobile spells.</gray>"));
            lore.add(dess("<gray>Cost Limit: 1000</gray>"));
            lore.add(dess("<gray>Hold a spell book in your off-hand to cast.</gray>"));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"orb");
            meta.getPersistentDataContainer().set(FreedomKeys.spellFocus(), PersistentDataType.BYTE, (byte) 1);
            meta.setItemModel(NamespacedKey.minecraft("orb"));
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
            castFromOffhand(player);
        }

        @Override
        public Recipe recipe() {
            ShapedRecipe recipe = new ShapedRecipe(keygen("orb"), item());
            recipe.shape(" G ", "GEG", " G ");
            recipe.setIngredient('G', Material.GLASS);
            recipe.setIngredient('E', Material.ENDER_PEARL);
            return recipe;
        }
    }

    public static class Staff extends Util implements BaseItem {

        @Override
        public ItemStack item() {
            ItemStack item  = ItemStack.of(Material.RECOVERY_COMPASS);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(dess("<gradient:#ff0000:#ff8800>Spell Focus: Staff</gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(dess("<gray>A powerful focus for channeling mobile spells.</gray>"));
            lore.add(dess("<gray>Cost Limit: 1000</gray>"));
            lore.add(dess("<gray>Hold a spell book in your off-hand to cast.</gray>"));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"staff");
            meta.getPersistentDataContainer().set(FreedomKeys.spellFocus(), PersistentDataType.BYTE, (byte) 1);
            meta.setItemModel(NamespacedKey.minecraft("staff"));
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
            castFromOffhand(player);
        }

        @Override
        public Recipe recipe() {
            ShapedRecipe recipe = new ShapedRecipe(keygen("staff"), item());
            recipe.shape("  D", " B ", "B  ");
            recipe.setIngredient('B', Material.BLAZE_ROD);
            recipe.setIngredient('D', Material.DIAMOND);
            return recipe;
        }
    }

    public static class Grimoire extends Util implements BaseItem {

        @Override
        public ItemStack item() {
            ItemStack item  = ItemStack.of(Material.RECOVERY_COMPASS);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(dess("<gradient:#555555:#000000>Spell Focus: Grimoire</gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(dess("<gray>An ancient grimoire that stabilizes mobile spells.</gray>"));
            lore.add(dess("<gray>Cost Limit: 1000</gray>"));
            lore.add(dess("<gray>Hold a spell book in your off-hand to cast.</gray>"));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"grimoire");
            meta.getPersistentDataContainer().set(FreedomKeys.spellFocus(), PersistentDataType.BYTE, (byte) 1);
            meta.setItemModel(NamespacedKey.minecraft("grimoire"));
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public void effect(Player player, PlayerInteractEvent event, ItemStack item) {
            castFromOffhand(player);
        }

        @Override
        public Recipe recipe() {
            ShapedRecipe recipe = new ShapedRecipe(keygen("grimoire"), item());
            recipe.shape("ODO", "DBD", "ODO");
            recipe.setIngredient('B', Material.BOOK);
            recipe.setIngredient('D', Material.DIAMOND);
            recipe.setIngredient('O', Material.OBSIDIAN);
            return recipe;
        }
    }

    private static void castFromOffhand(Player player) {
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType() == Material.WRITTEN_BOOK || offhand.getType() == Material.WRITABLE_BOOK) {
            BookMeta meta = (BookMeta) offhand.getItemMeta();
            String text = String.join(" ", meta.getPages());
            SpellCompiler.castMobileSpell(text, player);
        } else {
            player.sendMessage("§cYou must hold a spell book in your off-hand to use this focus!");
        }
    }
}
