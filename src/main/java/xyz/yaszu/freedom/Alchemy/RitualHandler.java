package xyz.yaszu.freedom.Alchemy;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class RitualHandler {





    public static class RitualRecipe {
        public ItemStack podiumOne;
        public ItemStack podiumTwo;
        public ItemStack podiumThree;
        public ItemStack podiumFour;
        public ItemStack podiumFive;
        public ItemStack podiumSix;
        public ItemStack podiumSeven;
        public ItemStack podiumEight;
        public RitualRecipe(ItemStack podiumOne, ItemStack podiumTwo, ItemStack podiumThree, ItemStack podiumFour, ItemStack podiumFive, ItemStack podiumSix, ItemStack podiumSeven, ItemStack podiumEight) {
            this.podiumOne = podiumOne;
            this.podiumTwo = podiumTwo;
            this.podiumThree = podiumThree;
            this.podiumFour = podiumFour;
            this.podiumFive = podiumFive;
            this.podiumSix = podiumSix;
            this.podiumSeven = podiumSeven;
            this.podiumEight = podiumEight;
        }
        public void output(Location location) {

        }

    }

}
