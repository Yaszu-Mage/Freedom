package xyz.yaszu.freedom.Information;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.ItemListener;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookCompiler extends Util {
    public static void registerAllRecipes(Map<String, BaseInformation> registry) {
        for (Map.Entry<String, BaseItem> entry : ItemListener.ITEMS.entrySet()) {
            String key = entry.getKey();
            BaseItem baseItem = entry.getValue();
            Recipe recipe = baseItem.recipe();
            
            if (recipe != null) {
                // Add prefix to avoid collisions with "circle" etc.
                String id = "recipe_" + key;
                
                // Handle duplicates just in case, though key should be unique in ItemListener
                int count = 1;
                String baseId = id;
                while (registry.containsKey(id)) {
                    id = baseId + "_" + count++;
                }
                
                final Recipe finalRecipe = recipe;
                registry.put(id, new BaseInformation() {
                    @Override
                    public ItemStack information() {
                        return createRecipeBook(finalRecipe);
                    }
                });
            }
        }
    }

    private static ItemStack createRecipeBook(Recipe recipe) {
        ItemStack book = ItemStack.of(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        ItemStack result = recipe.getResult();
        
        String itemName = result.getType().name().replace("_", " ").toLowerCase();
        if (result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
            itemName = PlainTextComponentSerializer.plainText().serialize(result.getItemMeta().displayName());
        }
        
        String title = "Recipe for " + itemName + " for Stupid Stupid know nothing crafters";
        meta.setTitle(title);
        meta.setAuthor("B.L. Fegore");
        meta.displayName(dess(title));
        
        List<Component> pages = new ArrayList<>();
        
        // Page 1: Intro
        pages.add(dess(title + "\n-------------------\n" +
                "Thanks to my patron S.A. Tan. You will be forever remembered in our hearts."));

        // Page 2: What is this?
        pages.add(dess("What is " + itemName + "?\n-------------------\n" +
                "It is a crafted item that serves its purpose for all people in this world. Use it wisely."));

        // Page 3: How it works
        StringBuilder recipeDetails = new StringBuilder();
        recipeDetails.append("How to craft it.\n-------------------\n");
        
        if (recipe instanceof ShapedRecipe shaped) {
            recipeDetails.append("Type: Shaped\n\n");
            recipeDetails.append("Shape:\n");
            for (String row : shaped.getShape()) {
                recipeDetails.append("  [").append(row).append("]\n");
            }
            recipeDetails.append("\nIngredients:\n");
            Map<Character, ItemStack> ingredients = shaped.getIngredientMap();
            for (Map.Entry<Character, ItemStack> entry : ingredients.entrySet()) {
                if (entry.getValue() != null) {
                    recipeDetails.append(entry.getKey()).append(": ").append(entry.getValue().getType().name().toLowerCase()).append("\n");
                }
            }
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            recipeDetails.append("Type: Shapeless\n\n");
            recipeDetails.append("Ingredients:\n");
            for (ItemStack ingredient : shapeless.getIngredientList()) {
                if (ingredient != null) {
                    recipeDetails.append("- ").append(ingredient.getType().name().toLowerCase()).append("\n");
                }
            }
        } else if (recipe instanceof SmithingTransformRecipe smithing) {
            recipeDetails.append("Type: Smithing\n\n");
            recipeDetails.append("Base: ").append(getChoiceName(smithing.getBase())).append("\n");
            recipeDetails.append("Template: ").append(getChoiceName(smithing.getTemplate())).append("\n");
            recipeDetails.append("Addition: ").append(getChoiceName(smithing.getAddition())).append("\n");
        } else if (recipe instanceof SmithingTrimRecipe smithing) {
            recipeDetails.append("Type: Smithing Trim\n\n");
            recipeDetails.append("Base: ").append(getChoiceName(smithing.getBase())).append("\n");
            recipeDetails.append("Template: ").append(getChoiceName(smithing.getTemplate())).append("\n");
            recipeDetails.append("Addition: ").append(getChoiceName(smithing.getAddition())).append("\n");
        } else {
            recipeDetails.append("Type: Unknown (Check recipe list)\n");
        }
        pages.add(dess(recipeDetails.toString()));
        meta.pages(pages);
        book.setItemMeta(meta);
        return book;
    }

    private static String getChoiceName(RecipeChoice choice) {
        if (choice == null) return "none";
        if (choice instanceof RecipeChoice.MaterialChoice material) {
            if (material.getChoices().isEmpty()) return "empty";
            return material.getChoices().get(0).name().toLowerCase();
        } else if (choice instanceof RecipeChoice.ExactChoice exact) {
            if (exact.getChoices().isEmpty()) return "empty";
            ItemStack stack = exact.getChoices().get(0);
            if (stack.hasItemMeta() && stack.getItemMeta().hasDisplayName()) {
                return PlainTextComponentSerializer.plainText().serialize(stack.getItemMeta().displayName());
            }
            return stack.getType().name().toLowerCase();
        }
        return "unknown choice";
    }
}
