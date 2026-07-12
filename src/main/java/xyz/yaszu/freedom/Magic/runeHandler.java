package xyz.yaszu.freedom.Magic;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static xyz.yaszu.freedom.Util.Util.dess;
import static xyz.yaszu.freedom.Util.Util.emptyItem;

public class runeHandler {
    public static enum BehaviorType {
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
        GROUND,
        EXPAND,
        SHEILD,
        DASH,
        DROWN,
        GLITCH,
        MOVE,
        BLIND,
        AUDIO,
        PURIFY,
        SILENCE,
        MIND,
        NONE
    }
    public static enum Rune {
        FIRE("#ff9100",BehaviorType.DAMAGE,BehaviorType.EXPAND,BehaviorType.DAMAGE,BehaviorType.SHEILD,BehaviorType.FLY,new ParticleBuilder(Particle.FLAME).count(10)),
        EARTH("",BehaviorType.SHEILD,BehaviorType.GROUND,BehaviorType.SHEILD,BehaviorType.SHEILD,BehaviorType.GROUND,new ParticleBuilder(Particle.BLOCK).data(Material.DIRT.createBlockData())),
        LIGHTNING("", BehaviorType.BLAST,BehaviorType.DASH,BehaviorType.BLAST,BehaviorType.SHOCK,BehaviorType.DASH,new ParticleBuilder(Particle.ELECTRIC_SPARK).count(10)),
        WATER("",BehaviorType.EXPAND,BehaviorType.DROWN,BehaviorType.EXPAND,BehaviorType.DROWN,BehaviorType.DROWN,new ParticleBuilder(Particle.BUBBLE_POP).count(4)),
        AIR("",BehaviorType.MOVE,BehaviorType.EXPAND,BehaviorType.EXPAND,BehaviorType.MOVE,BehaviorType.MOVE,new ParticleBuilder(Particle.CLOUD).count(10)),
        BLOOD("",BehaviorType.LIFESTEAL,BehaviorType.LIFESTEAL,BehaviorType.LIFESTEAL,BehaviorType.LIFESTEAL,BehaviorType.LIFESTEAL,Particle.DUST.builder().color(Color.RED).count(2)),
        STEAM("", BehaviorType.EXPAND,BehaviorType.EXPAND,BehaviorType.EXPAND,BehaviorType.EXPAND,BehaviorType.EXPAND,new ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE).count(10)),
        ICE("",BehaviorType.FREEZE,BehaviorType.GROUND,BehaviorType.FREEZE,BehaviorType.FREEZE,BehaviorType.FREEZE,new ParticleBuilder(Particle.SNOWFLAKE).count(10)),
        LIGHT("",BehaviorType.BLIND,BehaviorType.FLY, BehaviorType.BLIND,BehaviorType.BLIND,BehaviorType.FLY,Particle.DUST.builder().color(Color.YELLOW).count(2)),
        SYMPHONIC("",BehaviorType.AUDIO,BehaviorType.EXPAND,BehaviorType.AUDIO,BehaviorType.AUDIO,BehaviorType.AUDIO,Particle.NOTE.builder()),
        CYBER("",BehaviorType.GLITCH,BehaviorType.GLITCH,BehaviorType.GLITCH,BehaviorType.GLITCH,BehaviorType.GLITCH,Particle.ENCHANT.builder()),
        COSMIC("",BehaviorType.MOVE,BehaviorType.TELEPORT,BehaviorType.MOVE,BehaviorType.FLY,BehaviorType.TELEPORT,Particle.DRAGON_BREATH.builder()),
        HOLY("",BehaviorType.PURIFY,BehaviorType.HEAL,BehaviorType.PURIFY,BehaviorType.PURIFY,BehaviorType.HEAL,Particle.DUST.builder().color(Color.GREEN).count(2)),
        ANTI("",BehaviorType.SILENCE,BehaviorType.TIME,BehaviorType.SILENCE,BehaviorType.SILENCE,BehaviorType.TIME,Particle.DUST.builder().color(Color.BLUE).count(2)),
        PSYCHIC("",BehaviorType.MOVE,BehaviorType.MIND,BehaviorType.MIND,BehaviorType.MIND,BehaviorType.MIND,Particle.DUST.builder().color(Color.PURPLE).count(2)),
        CHAOS("",BehaviorType.NONE,BehaviorType.NONE,BehaviorType.NONE,BehaviorType.NONE,BehaviorType.NONE,new ParticleBuilder(Particle.PORTAL).count(10)),
        DARK("",BehaviorType.MIND,BehaviorType.BLIND,BehaviorType.TELEPORT,BehaviorType.TELEPORT,BehaviorType.TELEPORT,Particle.DUST.builder().color(Color.GRAY).count(2)),
        GRAVITY("",BehaviorType.GRAVITY,BehaviorType.GRAVITY,BehaviorType.GRAVITY,BehaviorType.GRAVITY,BehaviorType.GROUND,Particle.DUST.builder().color(Color.BLACK).count(2)),
        DEMONIC("",BehaviorType.DAMAGE,BehaviorType.DAMAGE,BehaviorType.DAMAGE,BehaviorType.DAMAGE,BehaviorType.DAMAGE,Particle.DUST.builder().color(Color.ORANGE).count(2)),
        LIFE("",BehaviorType.EXPAND,BehaviorType.HEAL,BehaviorType.EXPAND,BehaviorType.HEAL,BehaviorType.HEAL,Particle.DUST.builder().color(Color.LIME).count(2)),
        BLANK("",BehaviorType.NONE,BehaviorType.NONE,BehaviorType.NONE,BehaviorType.NONE,BehaviorType.NONE,new ParticleBuilder(Particle.BLOCK).data(Material.AIR.createBlockData()));
        private String color;
        private BehaviorType cornerBehavior;
        private BehaviorType centerBehavior;
        private BehaviorType edgeBehavior;
        private BehaviorType upBehavior;
        private BehaviorType downBehavior;
        private ParticleBuilder particle;

        /**
         * Construction for a Rune Type
         * @param color HEXCODE Color of the Rune
         * @param cornerBehavior Behavior when in the corners of crafting
         * @param centerBehavior Behavior when in the center of crafting
         * @param edgeBehavior Behavior when on the edges of crafting
         * @param upBehavior Behavior when in the UP slot
         * @param downBehavior Behavior when in the DOWN slot
         */
        private Rune (String color, BehaviorType cornerBehavior, BehaviorType centerBehavior, BehaviorType edgeBehavior, BehaviorType upBehavior, BehaviorType downBehavior, ParticleBuilder particle) {
            this.color = color;
            this.cornerBehavior = cornerBehavior;
            this.centerBehavior = centerBehavior;
            this.edgeBehavior = edgeBehavior;
            this.upBehavior = upBehavior;
            this.downBehavior = downBehavior;
            this.particle = particle;
        }
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
                    list.add(Rune.LIGHTNING);
                    list.add(Rune.LIFE);
                    list.add(Rune.BLOOD);
                    list.add(Rune.ANTI);
                }
                case EARTH -> {
                    list.add(Rune.WATER);
                    list.add(Rune.FIRE);
                    list.add(Rune.LIFE);
                    list.add(Rune.ANTI);
                    list.add(Rune.ICE);
                }
                case LIGHTNING -> {
                    list.add(Rune.EARTH);
                    list.add(Rune.AIR);
                    list.add(Rune.ANTI);
                }
                case STEAM, CYBER -> {
                    list.add(Rune.WATER);
                    list.add(Rune.ANTI);
                    list.add(Rune.CHAOS);
                }
                case BLOOD -> {
                    list.add(Rune.ANTI);
                    list.add(Rune.DEMONIC);
                    list.add(Rune.FIRE);
                    list.add(Rune.LIGHT);
                }
                case LIFE ->  {
                    list.add(Rune.DEMONIC);
                    list.add(Rune.BLOOD);
                    list.add(Rune.FIRE);
                    list.add(Rune.ANTI);
                }
                case DEMONIC ->  {
                    list.add(Rune.HOLY);
                    list.add(Rune.COSMIC);
                    list.add(Rune.LIGHT);
                    list.add(Rune.ANTI);
                }
                case ICE -> {
                    list.add(Rune.FIRE);
                    list.add(Rune.STEAM);
                    list.add(Rune.LIGHT);
                    list.add(Rune.ANTI);
                }
                case PSYCHIC -> {
                    list.add(Rune.GRAVITY);
                    list.add(Rune.SYMPHONIC);
                    list.add(Rune.ANTI);
                }
                case DARK -> {
                    list.add(Rune.LIGHT);
                    list.add(Rune.HOLY);
                    list.add(Rune.LIGHTNING);
                    list.add(Rune.FIRE);
                    list.add(Rune.ANTI);
                }
                case LIGHT -> {
                    list.add(Rune.EARTH);
                    list.add(Rune.LIFE);
                    list.add(Rune.DARK);
                    list.add(Rune.ANTI);
                }
                case SYMPHONIC -> {
                    list.add(Rune.COSMIC);
                    list.add(Rune.WATER);
                    list.add(Rune.AIR);
                    list.add(Rune.EARTH);
                    list.add(Rune.ANTI);
                }
                case GRAVITY -> {
                    list.add(Rune.ANTI);
                    list.add(Rune.LIGHTNING);
                    list.add(Rune.COSMIC);
                }
                case CHAOS -> {
                    list.add(Rune.PSYCHIC);
                    list.add(Rune.HOLY);
                    list.add(Rune.GRAVITY);
                    list.add(Rune.ANTI);
                }
                case COSMIC -> {
                    list.add(Rune.EARTH);
                    list.add(Rune.CHAOS);
                    list.add(Rune.GRAVITY);
                    list.add(Rune.ANTI);
                }
                case HOLY -> {
                    list.add(Rune.DARK);
                    list.add(Rune.PSYCHIC);
                    list.add(Rune.BLOOD);
                }
                case ANTI -> {
                    list.add(Rune.HOLY);
                }
            }
            return list;
        }
        public ItemStack constructRuneItem(Rune rune) {
            ItemStack item = ItemStack.of(Material.LEATHER_HORSE_ARMOR);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(dess("<color:#ff0000><shadow:#0fffb><b>" + rune.toString().toUpperCase() + " RUNE </color>"));
            meta.setItemModel(NamespacedKey.minecraft("rune-" + rune.toString().toLowerCase()));
            item.setItemMeta(meta);
            return item;
        }
        public static boolean isWeakness(Rune rune, Rune weakness) {
            return getWeakness(rune).contains(weakness);
        }
    }

    public class completedRune {



        public ArrayList<Rune> runes;

//        public Rune downSlot;
//        public Rune upSlot;
//        public Rune centerSlot;
//        public List<Rune> edgeSlots;
//        public List<Rune> cornerSlots;

        public void execute(Location location) {

        }






        public completedRune (@Nullable Rune upSlot, @Nullable Rune downSlot,@Nullable Rune topLeftCorner,@Nullable Rune topRightCorner,@Nullable Rune bottomLeftCorner,@Nullable Rune bottomRightCorner,@Nullable Rune topMiddle,@Nullable Rune leftMiddle,@Nullable Rune rightMiddle,@Nullable Rune bottomMiddle,Rune center) {
            upSlot = ifNullReplace(upSlot);
            downSlot = ifNullReplace(downSlot);
            topLeftCorner = ifNullReplace(topLeftCorner);
            topRightCorner = ifNullReplace(topRightCorner);
            bottomLeftCorner = ifNullReplace(bottomLeftCorner);
            bottomRightCorner = ifNullReplace(bottomRightCorner);
            topMiddle = ifNullReplace(topMiddle);
            leftMiddle = ifNullReplace(leftMiddle);
            rightMiddle = ifNullReplace(rightMiddle);
            bottomMiddle = ifNullReplace(bottomMiddle);
            center = ifNullReplace(center);
            runes.add(topLeftCorner);
            runes.add(topMiddle);
            runes.add(topRightCorner);
            runes.add(leftMiddle);
            runes.add(center);
            runes.add(rightMiddle);
            runes.add(bottomLeftCorner);
            runes.add(bottomMiddle);
            runes.add(bottomRightCorner);
            runes.add(upSlot);
            runes.add(downSlot);
        }
        public Rune ifNullReplace(Rune rune) {
            if (rune == null) {
                return Rune.BLANK;
            }
            return rune;
        }
    }





    public class spellCrafting implements InventoryHolder, Listener {

        public static HashMap<UUID, Inventory> openMenus = new HashMap<>();
        public Inventory inventory;


        /**
         * SLOTS THAT ARE ABLE TO BE INTERACTED WITH WITHIN THE GUI
         */
        public List allowedSlots = List.of(0,2,3,4,11,12,13,18,20,21,22);
        /**
         * SLOTS THAT ARE BOOLEANS AND CANNOT BE INTERACTED WITH
         */
        public List booleanSlots = List.of(6,7,8,15,16,17,24,25,26);
        public void set(Player player) {
            UUID uuid = player.getUniqueId();
            Inventory inv = Bukkit.createInventory(this, 27, dess("<shadow:#0fffb><b><aqua>RuneCrafter</aqua>"));
            try {
                for (int x = 0; x < inv.getSize(); x++) {
                    if (!isAllowedSlot(x) && !isBooleanSlot(x)) {
                        inv.setItem(x, emptyItem(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE)));
                    }
                    if (isBooleanSlot(x)) {
                        inv.setItem(x, emptyItem(ItemStack.of(Material.COAL_BLOCK)));
                    }
                }

            } catch (Exception e) {}
            ItemStack up = ItemStack.of(Material.EMERALD_BLOCK);
            ItemMeta meta = up.getItemMeta();
            meta.displayName(dess("<color:#1eff00><shadow:#0fffb><b>UP</color>"));
            up.setItemMeta(meta);
            inv.setItem(1,up);
            ItemStack down = ItemStack.of(Material.REDSTONE_BLOCK);
            meta = down.getItemMeta();
            meta.displayName(dess("<color:#ff0000><shadow:#0fffb><b>DOWN</color>"));
            down.setItemMeta(meta);
            inv.setItem(19,down);
            ItemStack craftButton = ItemStack.of(Material.CRAFTING_TABLE);
            meta = craftButton.getItemMeta();
            meta.displayName(dess("<color:#0000ff><shadow:#0fffb><b>CRAFT</color>"));
            craftButton.setItemMeta(meta);
            inv.setItem(22,craftButton);
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


        public boolean isBooleanSlot(int slot) {return booleanSlots.contains(slot);}
        public boolean isAllowedSlot(int slot) {return allowedSlots.contains(slot);}


    }


}
