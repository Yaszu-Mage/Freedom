package xyz.yaszu.freedom.Magic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static xyz.yaszu.freedom.Util.Util.dess;

public class runeHandler {

    public static enum BehaviorTypes {
        FLY,
        DAMAGE,
        HEAL,
        TIME,
        SHOCK,
        BLAST,
        FREEZE,
        KNOCKBACK,
        LIFESTEAL,
        HEMORRHAGE,
        STARSTRUCK,
        GRAVITY,
        TELEPORT,
        GROUNDED
    }

    public static enum Rune {
        FIRE,
        EARTH,
        LIGHTNING,
        WATER,
        AIR,
        BLOOD,
        STEAM,
        ICE,
        LIGHT,
        SYMPHONIC,
        CYBER,
        COSMIC,
        HOLY,
        ANTI,
        PSYCHIC,
        CHAOS,
        DARK,
        GRAVITY,
        DEMONIC,
        LIFE,
        BLANK;

        public static List<Rune> getWeakness(Rune rune) {
            ArrayList<Rune> list = new ArrayList<>();
            switch (rune) {
                case AIR -> {
                    list.add(Rune.LIGHTNING);
                    list.add(Rune.COSMIC);
                    list.add(Rune.ANTI);
                    list.add(Rune.GRAVITY);
                }
                case FIRE -> {
                    list.add(Rune.WATER);
                    list.add(Rune.STEAM);
                    list.add(Rune.BLOOD);
                    list.add(Rune.ANTI);

                }
                case WATER -> {
                    list.add(Rune.EARTH);
                    list.add(Rune.LIGHTNING);
                    list.add(Rune.LIFE);
                    list.add(Rune.ANTI);

                }
                case EARTH -> {
                    list.add(Rune.FIRE);
                    list.add(Rune.LIFE);
                    list.add(Rune.CYBER);
                    list.add(Rune.ANTI);
                    list.add(Rune.ICE);
                }
                case LIGHTNING -> {
                    list.add(Rune.EARTH);
                    list.add(Rune.AIR);
                    list.add(Rune.ANTI);
                }
                case STEAM -> {
                    list.add(Rune.GRAVITY);
                    list.add(Rune.WATER);
                    list.add(Rune.ANTI);
                }
                case BLOOD -> {
                    list.add(Rune.CYBER);
                    list.add(Rune.ANTI);
                    list.add(Rune.DEMONIC);
                }

            }
            return list;
        }






        public static boolean isWeakness(Rune rune, Rune weakness) {
            return getWeakness(rune).contains(weakness);
        }
    }

    public class completedRune {


        public Rune downSlot;
        public Rune upSlot;
        public List<Rune> centerSlots;
        static List<BehaviorTypes> getBehavior() {
            return List.of(BehaviorTypes.values());
        }
    }


    public class spellCrafting implements InventoryHolder, Listener {

        public static HashMap<UUID, Inventory> openMenus = new HashMap<>();
        public Inventory inventory;


        public void set(Player player) {
            UUID uuid = player.getUniqueId();
            Inventory inv = Bukkit.createInventory(this, 27, dess("<shadow:#0fffb><b><aqua>RuneCrafter</aqua>"));




            this.inventory = inv;
            openMenus.put(uuid, inv);
        }
        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (event.getClickedInventory().getHolder() instanceof spellCrafting crafter) {













                if (!isAllowedSlot(event.getSlot())) {
                    event.setCancelled(true);
                }

            }
        }


        public List allowedSlots = List.of(0,2,3,4,11,12,13,18,20,21,22);
        public boolean isAllowedSlot(int slot) {
            return allowedSlots.contains(slot);
        }


    }


}
