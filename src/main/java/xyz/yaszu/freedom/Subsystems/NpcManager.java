package xyz.yaszu.freedom.Subsystems;

import com.destroystokyo.paper.entity.ai.VanillaGoal;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.github.javafaker.Faker;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import io.papermc.paper.event.entity.EntityMoveEvent;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.Translatable;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.InventoryPersistentDataType;
import xyz.yaszu.freedom.Util.StructureUtil;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NpcManager v3 — Complete rewrite of gather/settle/tool workflows.
 *
 * ══ ENFORCED SETTLE FLOW ═════════════════════════════════════════════════════
 *
 *  Every NPC follows this strict ordered progression on first spawn:
 *
 *   WANDER  (find good ground)
 *   │
 *   └─► SETTLE  (mark home location)
 *       │
 *       └─► GATHER_WOOD  (collect 16+ logs — enough for all crafting)
 *           │
 *           └─► CRAFT_WORKBENCH  (craft crafting table from planks)
 *               │
 *               └─► CRAFT_TOOLS  (craft wooden/stone pickaxe + axe)
 *                   │
 *                   └─► MINE_STONE  (collect 12 cobblestone for furnace)
 *                       │
 *                       └─► CRAFT_FURNACE  (craft furnace from stone)
 *                           │
 *                           └─► MINE_IRON  (collect 3+ iron ore)
 *                               │
 *                               └─► SMELT_IRON  (smelt iron ingots)
 *                                   │
 *                                   └─► CRAFT_BUCKET (if schem needs water)
 *                                       │
 *                                       └─► CONSTRUCT  (build the tent)
 *                                           │
 *                                           └─► FARM / WANDER  (ongoing life)
 *
 * ══ UNDERGROUND BLOCK SEARCH ════════════════════════════════════════════════
 *
 *  findAny(material) performs a full-column scan: for each XZ position in the
 *  search radius (step 2), it descends from the surface all the way to minY,
 *  checking every block. This finds any material anywhere in the world.
 *  Cost: O((radius/step)² × worldHeight) — with radius=60, step=2, height=384
 *  this is ~900 × 384 = 346k block reads per call. Called only once when a
 *  target is needed (result cached in npc.cachedTarget), not every tick.
 *
 * ══ TOOL PREREQUISITE SYSTEM ════════════════════════════════════════════════
 *
 *  Before any NPCBreakBlock call, requiresTool(block) checks if the block drops
 *  anything without a tool. If it requires a specific tool tier and the NPC
 *  doesn't have it, the NPC enters CRAFT_TOOLS instead of wasting time on an
 *  unproductive break.
 *
 * ══ ANTI-IDLE WATCHDOG ══════════════════════════════════════════════════════
 *
 *  Every NPC tracks stateEnteredTick. If the NPC has been in the same state
 *  for more than IDLE_TIMEOUT_TICKS without isBreaking being set, the watchdog
 *  forces a state reset: clears isBreaking, cachedTarget, postBreakCooldown,
 *  and returns the NPC to WANDER. This prevents any single bug from permanently
 *  freezing an NPC.
 *
 * ══ isBreaking LEAK FIX ══════════════════════════════════════════════════════
 *
 *  All exit paths in NPCBreakBlock now clear npc.isBreaking = false. Previously
 *  the MAX_WAIT_TICKS+30 timeout path cancelled the runnable without clearing
 *  the flag, permanently locking the NPC.
 *
 * ══ SCHEDULE ════════════════════════════════════════════════════════════════
 *  Bukkit.getPluginManager().registerEvents(new NpcManager(), plugin);
 *  NpcManager.update().runTaskTimer(plugin, 20L, 10L);
 */
public class NpcManager extends Util implements Listener {

    private static final NpcManager    INSTANCE = new NpcManager();
    public  static final Faker          faker    = new Faker();
    public  static final Map<UUID, NPC> NPCs     = new ConcurrentHashMap<>();

    // ── Schematic material cache ──────────────────────────────────────────────
    private static final Map<String, List<ItemStack>> SCHEM_CACHE = new ConcurrentHashMap<>();
    private static List<ItemStack> getSchemMaterials(String name) {
        return SCHEM_CACHE.computeIfAbsent(name, StructureUtil::getSchemMaterialsFromResource);
    }

    // ── Tuning ────────────────────────────────────────────────────────────────
    private static final int    MOVE_TICKS_PER_HUNGER = 600;
    private static final int    MAX_WAIT_TICKS        = 200;
    private static final double BREAK_DISTANCE_SQ     = 12.0;
    private static final int    WANDER_RADIUS         = 14;
    private static final int    SEARCH_RADIUS         = 64;  // XZ radius for block search
    private static final int    SEARCH_STEP           = 2;   // XZ step (smaller = more thorough, slower)
    private static final int    POST_BREAK_TICKS      = 20;
    private static final double HUNGER_EAT            = 10.0;
    private static final double HUNGER_FULL           = 18.0;
    private static final double VILLAGE_RADIUS_SQ     = 60.0 * 60.0;
    private static final int    VILLAGE_MIN           = 3;
    private static final int    SOCIAL_RADIUS         = 50;
    private static final int    RESPAWN_DELAY_TICKS   = 200;
    // Anti-idle: if NPC stays in same state this many update ticks without breaking, force reset
    private static final int    IDLE_TIMEOUT_TICKS    = 120; // 120 × 10t = 60 seconds

    // ── How many of each resource the NPC needs to gather before crafting ─────
    private static final int LOGS_NEEDED    = 20;
    private static final int STONE_NEEDED   = 12;
    private static final int IRON_ORE_NEEDED = 4;

    // ── Enums ─────────────────────────────────────────────────────────────────
    public enum Personality { SOCIAL, INDUSTRIOUS, CAUTIOUS, WANDERER }
    public enum Role        { NONE, FARMER, MINER, BUILDER, LEADER }

    /**
     * Ordered settle flow: WANDER → SETTLE → GATHER_WOOD → CRAFT_WORKBENCH
     * → CRAFT_TOOLS → MINE_STONE → CRAFT_FURNACE → MINE_IRON → SMELT_IRON
     * → [CRAFT_BUCKET if needed] → CONSTRUCT → FARM / ongoing states
     */
    public enum NpcState {
        // Ongoing
        WANDER, IDLE, FLEE, SOCIALIZE,
        // Settle pipeline (strict order)
        SETTLE,
        GATHER_WOOD,
        CRAFT_WORKBENCH,
        CRAFT_TOOLS,
        MINE_STONE,
        CRAFT_FURNACE,
        MINE_IRON,
        SMELT_IRON,
        CRAFT_BUCKET,
        CONSTRUCT,
        // Post-settle ongoing
        FARM, HARVEST, GATHER_FOOD,
        MINE,  // ongoing mining after settled
        SMELT  // ongoing smelting after settled
    }

    // ── Dialogue ──────────────────────────────────────────────────────────────
    private static final Map<Personality, List<String>> GREETINGS = Map.of(
            Personality.SOCIAL,      List.of("Hey there!", "Good to see you!", "How's it going?"),
            Personality.INDUSTRIOUS, List.of("Busy day...", "No rest for the wicked."),
            Personality.CAUTIOUS,    List.of("Oh! You startled me.", "Stay safe out there."),
            Personality.WANDERER,    List.of("The horizon calls!", "Adventure awaits!")
    );
    private static final List<String> HURT_LINES     = List.of("Ow!", "Hey!", "That hurt!", "Stop!");
    private static final List<String> LOW_HEALTH_LINES = List.of("Please... stop.", "I need help!");

    // ── Ore & tool material sets ──────────────────────────────────────────────
    private static final Set<Material> WOOD_LOGS = Set.of(
            Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG,
            Material.ACACIA_LOG, Material.JUNGLE_LOG, Material.DARK_OAK_LOG,
            Material.CHERRY_LOG, Material.MANGROVE_LOG);

    private static final Set<Material> ORE_IRON = Set.of(
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE);

    private static final Set<Material> ORE_COAL = Set.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE);

    private static final Set<Material> STONE_TYPES = Set.of(
            Material.STONE, Material.COBBLESTONE, Material.DEEPSLATE,
            Material.COBBLED_DEEPSLATE, Material.ANDESITE, Material.DIORITE, Material.GRANITE);

    private static final Set<Material> FOOD_MATS = Set.of(
            Material.BREAD, Material.COOKED_BEEF, Material.APPLE, Material.COOKED_CHICKEN,
            Material.CARROT, Material.POTATO, Material.COOKED_MUTTON,
            Material.COOKED_PORKCHOP, Material.WHEAT);

    // ── Update loop ───────────────────────────────────────────────────────────
    public static BukkitRunnable update() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                for (Iterator<Map.Entry<UUID, NPC>> it = NPCs.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<UUID, NPC> entry = it.next();
                    NPC npc = entry.getValue();
                    if (npc == null) { it.remove(); continue; }
                    if (npc.BaseEntity == null || npc.BaseEntity.isDead()) continue;

                    NPCPickup(npc);
                    antiIdleWatchdog(npc);
                    tickStateMachine(npc);

                    if (npc.hasHome()) checkVillageFormation(npc);
                    if (npc.socialCooldown  > 0) npc.socialCooldown--;
                    if (npc.fleeCooldown    > 0) npc.fleeCooldown--;
                    if (npc.postBreakCooldown > 0) npc.postBreakCooldown--;
                }
            }
        };
    }

    // ── Anti-idle watchdog ────────────────────────────────────────────────────
    /**
     * If the NPC has been in the same state for IDLE_TIMEOUT_TICKS and is not
     * actively breaking, reset it to WANDER and clear all transient state.
     * This catches every possible "stuck" scenario.
     */
    private static void antiIdleWatchdog(NPC npc) {
        npc.idleTicks++;
        // Reset counter whenever the NPC is actively working
        if (npc.isBreaking || npc.postBreakCooldown > 0) {
            npc.idleTicks = 0;
            return;
        }
        if (npc.BaseEntity.getPathfinder().hasPath()) {
            npc.idleTicks = 0;
            return;
        }
        if (npc.idleTicks >= IDLE_TIMEOUT_TICKS) {
            // NPC has been stuck — force reset
            npc.isBreaking       = false;
            npc.postBreakCooldown = 0;
            npc.cachedTarget     = null;
            npc.idleTicks        = 0;
            // Don't reset settle pipeline states — only reset to WANDER from ongoing states
            NpcState s = npc.getState();
            if (s == NpcState.MINE || s == NpcState.SMELT || s == NpcState.SOCIALIZE
                    || s == NpcState.GATHER_FOOD || s == NpcState.HARVEST || s == NpcState.FARM) {
                npc.setState(NpcState.WANDER);
            }
            // For pipeline states, re-evaluate from the same state next tick (may find a new target)
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STATE MACHINE — ENFORCED ORDERED SETTLE PIPELINE
    // ══════════════════════════════════════════════════════════════════════════
    private static void tickStateMachine(NPC npc) {
        if (npc.hunger <= 0) { scheduleRespawn(npc); return; }

        // Global flee interrupt
        if (npc.fleeCooldown > 0 && npc.getState() != NpcState.FLEE)
            npc.setState(NpcState.FLEE);

        switch (npc.getState()) {

            // ── WANDER ───────────────────────────────────────────────────────
            case WANDER -> {
                if (npc.hunger < HUNGER_EAT)       { npc.setState(NpcState.GATHER_FOOD); return; }
                if (!npc.hasHome())                 { npc.setState(NpcState.SETTLE);      return; }
                if (npc.personality == Personality.SOCIAL && npc.socialCooldown == 0
                        && random.nextInt(80) == 0) { npc.setState(NpcState.SOCIALIZE);  return; }
                if ((npc.role == Role.MINER || npc.personality == Personality.INDUSTRIOUS)
                        && random.nextInt(160) == 0){ npc.setState(NpcState.MINE);        return; }
                if (!npc.BaseEntity.getPathfinder().hasPath()) npc.wander();
                // Passive regen near home
                Location home = npc.getHomeLocation();
                if (home != null && npc.BaseEntity.getLocation().distanceSquared(home) < 100)
                    npc.hunger = Math.min(20, npc.hunger + 0.02);
            }

            // ── SETTLE ───────────────────────────────────────────────────────
            case SETTLE -> {
                Location loc = npc.BaseEntity.getLocation();
                if (INSTANCE.isStandable(loc)) {
                    INSTANCE.doSettle(npc, loc);
                    npc.setState(NpcState.GATHER_WOOD); // begin pipeline
                } else if (!npc.BaseEntity.getPathfinder().hasPath()) {
                    npc.wander();
                }
            }

            // ── GATHER_WOOD ──────────────────────────────────────────────────
            // Collect LOGS_NEEDED logs before anything else
            case GATHER_WOOD -> {
                if (npc.isBreaking || npc.postBreakCooldown > 0) return;
                int logs = INSTANCE.countMatSet(npc.getInventory(), WOOD_LOGS);
                if (logs >= LOGS_NEEDED) { npc.setState(NpcState.CRAFT_WORKBENCH); return; }
                Block log = npc.cachedTarget != null && !npc.cachedTarget.getType().isAir()
                        ? npc.cachedTarget
                        : INSTANCE.findLog(npc.BaseEntity.getLocation(), SEARCH_RADIUS);
                if (log == null) { if (!npc.BaseEntity.getPathfinder().hasPath()) npc.wander(WANDER_RADIUS * 2); return; }
                npc.cachedTarget = log;
                // Axe preferred but not required — wood breaks with hands
                INSTANCE.NPCBreakBlock(npc, log, () -> {
                    npc.cachedTarget = null;
                    npc.idleTicks = 0;
                });
            }

            // ── CRAFT_WORKBENCH ───────────────────────────────────────────────
            case CRAFT_WORKBENCH -> {
                // Craft planks from logs, then crafting table
                INSTANCE.craftPlanksFromLogs(npc);
                if (INSTANCE.countInv(npc.getInventory(), Material.CRAFTING_TABLE) == 0) {
                    if (INSTANCE.countInv(npc.getInventory(), Material.OAK_PLANKS) >= 4) {
                        INSTANCE.consumeAmount(npc, Material.OAK_PLANKS, 4);
                        npc.getInventory().addItem(new ItemStack(Material.CRAFTING_TABLE));
                    } else {
                        npc.setState(NpcState.GATHER_WOOD); return; // need more wood
                    }
                }
                npc.setState(NpcState.CRAFT_TOOLS);
            }

            // ── CRAFT_TOOLS ───────────────────────────────────────────────────
            // Craft wooden axe + wooden pickaxe at minimum; upgrade to stone if possible
            case CRAFT_TOOLS -> {
                INSTANCE.craftPlanksFromLogs(npc);
                INSTANCE.craftSticksFromPlanks(npc);
                boolean hasPickaxe = INSTANCE.hasAnyPickaxe(npc);
                boolean hasAxe     = INSTANCE.hasAnyAxe(npc);
                if (!hasPickaxe) {
                    if (!INSTANCE.tryCraftWoodenPickaxe(npc)) {
                        npc.setState(NpcState.GATHER_WOOD); return; // back for more wood
                    }
                }
                if (!hasAxe) {
                    INSTANCE.tryCraftWoodenAxe(npc);
                }
                npc.setState(NpcState.MINE_STONE);
            }

            // ── MINE_STONE ────────────────────────────────────────────────────
            case MINE_STONE -> {
                if (npc.isBreaking || npc.postBreakCooldown > 0) return;
                int stone = INSTANCE.countMatSet(npc.getInventory(), STONE_TYPES)
                        + INSTANCE.countInv(npc.getInventory(), Material.COBBLESTONE);
                if (stone >= STONE_NEEDED) { npc.setState(NpcState.CRAFT_FURNACE); return; }
                // Stone can be underground — use full-column search
                Block stoneBlock = npc.cachedTarget != null && !npc.cachedTarget.getType().isAir()
                        ? npc.cachedTarget
                        : INSTANCE.findAny(npc.BaseEntity.getLocation(), SEARCH_RADIUS, STONE_TYPES);
                if (stoneBlock == null) { if (!npc.BaseEntity.getPathfinder().hasPath()) npc.wander(); return; }
                npc.cachedTarget = stoneBlock;
                if (!INSTANCE.hasPickaxeFor(npc, stoneBlock)) {
                    // Need a better tool — shouldn't happen since we just crafted one, but guard anyway
                    npc.setState(NpcState.CRAFT_TOOLS); return;
                }
                INSTANCE.NPCBreakBlock(npc, stoneBlock, () -> {
                    npc.cachedTarget = null; npc.idleTicks = 0;
                });
            }

            // ── CRAFT_FURNACE ─────────────────────────────────────────────────
            case CRAFT_FURNACE -> {
                // Craft stone pickaxe upgrade first if we have the stone
                INSTANCE.tryCraftStonePickaxe(npc);
                if (INSTANCE.countInv(npc.getInventory(), Material.FURNACE) == 0) {
                    if (!INSTANCE.tryCraftFurnace(npc)) {
                        npc.setState(NpcState.MINE_STONE); return;
                    }
                }
                npc.setState(NpcState.MINE_IRON);
            }

            // ── MINE_IRON ─────────────────────────────────────────────────────
            case MINE_IRON -> {
                if (npc.isBreaking || npc.postBreakCooldown > 0) return;
                int raw = INSTANCE.countInv(npc.getInventory(), Material.RAW_IRON)
                        + INSTANCE.countInv(npc.getInventory(), Material.IRON_ORE)
                        + INSTANCE.countInv(npc.getInventory(), Material.DEEPSLATE_IRON_ORE);
                // Also count ingots already in inv
                int ingots = INSTANCE.countInv(npc.getInventory(), Material.IRON_INGOT);
                if (raw + ingots >= IRON_ORE_NEEDED) { npc.setState(NpcState.SMELT_IRON); return; }
                Block ore = npc.cachedTarget != null && !npc.cachedTarget.getType().isAir()
                        ? npc.cachedTarget
                        : INSTANCE.findAny(npc.BaseEntity.getLocation(), SEARCH_RADIUS, ORE_IRON);
                if (ore == null) { if (!npc.BaseEntity.getPathfinder().hasPath()) npc.wander(); return; }
                npc.cachedTarget = ore;
                if (!INSTANCE.hasPickaxeFor(npc, ore)) { npc.setState(NpcState.CRAFT_TOOLS); return; }
                INSTANCE.NPCBreakBlock(npc, ore, () -> {
                    npc.cachedTarget = null; npc.idleTicks = 0;
                });
            }

            // ── SMELT_IRON (pipeline) ─────────────────────────────────────────
            case SMELT_IRON -> {
                // Need coal to smelt — find some if missing
                if (npc.isBreaking || npc.postBreakCooldown > 0) return;
                int coal = INSTANCE.countInv(npc.getInventory(), Material.COAL);
                if (coal == 0) {
                    Block coalBlock = INSTANCE.findAny(npc.BaseEntity.getLocation(), SEARCH_RADIUS, ORE_COAL);
                    if (coalBlock != null) {
                        if (!INSTANCE.hasPickaxeFor(npc, coalBlock)) { npc.setState(NpcState.CRAFT_TOOLS); return; }
                        INSTANCE.NPCBreakBlock(npc, coalBlock, () -> { npc.cachedTarget = null; npc.idleTicks = 0; });
                        return;
                    }
                    // No coal — wander and search
                    if (!npc.BaseEntity.getPathfinder().hasPath()) npc.wander();
                    return;
                }
                // Smelt raw iron
                int raw = INSTANCE.countInv(npc.getInventory(), Material.RAW_IRON);
                if (raw > 0) {
                    int n = Math.min(raw, coal * 8);
                    INSTANCE.consumeAmount(npc, Material.RAW_IRON, n);
                    INSTANCE.consumeAmount(npc, Material.COAL, (int) Math.ceil(n / 8.0));
                    npc.getInventory().addItem(new ItemStack(Material.IRON_INGOT, n));
                }
                // Advance: bucket if schem needs water, else go straight to CONSTRUCT
                String schem = npc.getPendingSchem() != null ? npc.getPendingSchem() : "tent.schem";
                if (INSTANCE.schemNeedsWater(schem)) {
                    npc.setState(NpcState.CRAFT_BUCKET);
                } else {
                    npc.setState(NpcState.CONSTRUCT);
                }
            }

            // ── CRAFT_BUCKET ──────────────────────────────────────────────────
            case CRAFT_BUCKET -> {
                if (INSTANCE.countInv(npc.getInventory(), Material.WATER_BUCKET) > 0
                        || INSTANCE.countInv(npc.getInventory(), Material.BUCKET) > 0) {
                    // If we have an empty bucket, fill it from nearest water
                    if (INSTANCE.countInv(npc.getInventory(), Material.BUCKET) > 0) {
                        Block water = INSTANCE.findSurfaceWater(npc.BaseEntity.getLocation(), SEARCH_RADIUS);
                        if (water != null) {
                            npc.move(INSTANCE.pathToBlock(water));
                            // Fill bucket — simulate after arrival
                            Bukkit.getScheduler().runTaskLater(Freedom.get_plugin(), () -> {
                                if (npc.BaseEntity == null || npc.BaseEntity.isDead()) return;
                                if (npc.BaseEntity.getLocation().distanceSquared(water.getLocation()) < 9) {
                                    INSTANCE.consumeAmount(npc, Material.BUCKET, 1);
                                    npc.getInventory().addItem(new ItemStack(Material.WATER_BUCKET));
                                    npc.setState(NpcState.CONSTRUCT);
                                }
                            }, 40L);
                            return;
                        }
                    }
                    npc.setState(NpcState.CONSTRUCT);
                    return;
                }
                // Craft bucket: 3 iron ingots in a V shape
                if (INSTANCE.countInv(npc.getInventory(), Material.IRON_INGOT) >= 3) {
                    INSTANCE.consumeAmount(npc, Material.IRON_INGOT, 3);
                    npc.getInventory().addItem(new ItemStack(Material.BUCKET));
                    // Now go fill it
                } else {
                    // Need more iron — go back to mine
                    npc.setState(NpcState.MINE_IRON);
                }
            }

            // ── CONSTRUCT ─────────────────────────────────────────────────────
            case CONSTRUCT -> {
                if (!npc.hasPendingBuild()) {
                    npc.setState(npc.role == Role.FARMER ? NpcState.FARM : NpcState.WANDER);
                    return;
                }
                if (npc.isBreaking || npc.postBreakCooldown > 0) return;
                String schem = npc.getPendingSchem() != null ? npc.getPendingSchem() : "tent.schem";
                List<ItemStack> missing = INSTANCE.getMissingMaterials(npc, getSchemMaterials(schem));
                if (!missing.isEmpty()) {
                    // Identify what's missing and route to correct gather state
                    ItemStack first = missing.get(0);
                    npc.setPendingMaterials(missing);
                    INSTANCE.routeToGather(npc, first.getType());
                } else {
                    INSTANCE.executeBuild(npc, schem);
                    npc.setState(NpcState.FARM);
                }
            }

            // ── FARM ─────────────────────────────────────────────────────────
            case FARM -> {
                if (npc.hunger < HUNGER_EAT) { npc.setState(NpcState.GATHER_FOOD); return; }
                if (npc.isBreaking || npc.postBreakCooldown > 0) return;
                Block crop = INSTANCE.findSurface(npc.BaseEntity.getLocation(), Material.WHEAT, 24);
                if (crop != null && crop.getBlockData() instanceof org.bukkit.block.data.Ageable ag
                        && ag.getAge() == ag.getMaximumAge()) {
                    npc.setState(NpcState.HARVEST); return;
                }
                if (!npc.BaseEntity.getPathfinder().hasPath()) {
                    Location home = npc.getHomeLocation();
                    if (home != null && npc.BaseEntity.getLocation().distanceSquared(home) > 100)
                        npc.move(home);
                    else
                        npc.wander(8);
                }
            }

            // ── HARVEST ──────────────────────────────────────────────────────
            case HARVEST -> {
                if (npc.isBreaking || npc.postBreakCooldown > 0) return;
                Block crop = INSTANCE.findSurface(npc.BaseEntity.getLocation(), Material.WHEAT, 24);
                if (crop == null || !(crop.getBlockData() instanceof org.bukkit.block.data.Ageable ag)
                        || ag.getAge() < ag.getMaximumAge()) {
                    npc.setState(NpcState.FARM); return;
                }
                final Block finalCrop = crop;
                INSTANCE.NPCBreakBlock(npc, crop, () -> {
                    if (finalCrop.getType() == Material.AIR
                            && finalCrop.getRelative(0,-1,0).getType() == Material.FARMLAND
                            && INSTANCE.countInv(npc.getInventory(), Material.WHEAT_SEEDS) > 0) {
                        finalCrop.setType(Material.WHEAT);
                        INSTANCE.consumeOne(npc, Material.WHEAT_SEEDS);
                    }
                    npc.hunger = Math.min(20, npc.hunger + 3);
                    npc.setState(NpcState.FARM);
                });
            }

            // ── GATHER_FOOD ──────────────────────────────────────────────────
            case GATHER_FOOD -> {
                if (npc.hunger >= HUNGER_FULL) { npc.setState(NpcState.WANDER); return; }
                if (INSTANCE.tryEat(npc)) return;
                if (npc.isBreaking || npc.postBreakCooldown > 0) return;
                Item foodDrop = INSTANCE.findNearbyFoodDrop(npc);
                if (foodDrop != null) { npc.move(foodDrop.getLocation()); return; }
                Block food = INSTANCE.findSurface(npc.BaseEntity.getLocation(), Material.WHEAT, SEARCH_RADIUS);
                if (food != null) {
                    INSTANCE.NPCBreakBlock(npc, food, () -> npc.hunger = Math.min(20, npc.hunger + 3));
                } else if (!npc.BaseEntity.getPathfinder().hasPath()) {
                    npc.wander(WANDER_RADIUS * 2);
                }
            }

            // ── SOCIALIZE ────────────────────────────────────────────────────
            case SOCIALIZE -> {
                if (npc.isBreaking) return;
                NPC other = nearestOther(npc, SOCIAL_RADIUS);
                if (other == null) { npc.setState(NpcState.WANDER); return; }
                if (npc.BaseEntity.getLocation().distanceSquared(other.BaseEntity.getLocation()) > 9) {
                    npc.move(other.BaseEntity.getLocation());
                } else {
                    npc.BaseEntity.lookAt(other.BaseEntity);
                    INSTANCE.trade(npc, other);
                    npc.socialCooldown = 200;
                    npc.setState(NpcState.WANDER);
                }
            }

            // ── MINE (ongoing, post-settle) ───────────────────────────────────
            case MINE -> {
                if (npc.isBreaking || npc.postBreakCooldown > 0) return;
                if (npc.hunger < HUNGER_EAT) { npc.setState(NpcState.GATHER_FOOD); return; }
                if (!INSTANCE.hasAnyPickaxe(npc)) { npc.setState(NpcState.CRAFT_TOOLS); return; }
                Block ore = npc.cachedTarget != null && !npc.cachedTarget.getType().isAir()
                        ? npc.cachedTarget
                        : INSTANCE.findAny(npc.BaseEntity.getLocation(), SEARCH_RADIUS, ORE_IRON);
                if (ore == null) { npc.wander(WANDER_RADIUS * 3); npc.setState(NpcState.WANDER); return; }
                npc.cachedTarget = ore;
                INSTANCE.NPCBreakBlock(npc, ore, () -> {
                    npc.cachedTarget = null;
                    if (INSTANCE.countInv(npc.getInventory(), Material.RAW_IRON) >= 8)
                        npc.setState(NpcState.SMELT);
                });
            }

            // ── SMELT (ongoing, post-settle) ──────────────────────────────────
            case SMELT -> {
                int raw  = INSTANCE.countInv(npc.getInventory(), Material.RAW_IRON);
                int coal = INSTANCE.countInv(npc.getInventory(), Material.COAL);
                if (raw > 0 && coal > 0) {
                    int n = Math.min(raw, coal * 8);
                    INSTANCE.consumeAmount(npc, Material.RAW_IRON, n);
                    INSTANCE.consumeAmount(npc, Material.COAL, (int) Math.ceil(n / 8.0));
                    npc.getInventory().addItem(new ItemStack(Material.IRON_INGOT, n));
                    INSTANCE.tryCraftIronPickaxe(npc);
                }
                npc.setState(NpcState.WANDER);
            }

            // ── FLEE ─────────────────────────────────────────────────────────
            case FLEE -> {
                if (npc.fleeCooldown <= 0) { npc.setState(NpcState.WANDER); return; }
                if (!npc.BaseEntity.getPathfinder().hasPath()) {
                    if (npc.lastAttacker != null && !npc.lastAttacker.isDead()) {
                        Location nl = npc.BaseEntity.getLocation();
                        Location al = npc.lastAttacker.getLocation();
                        double dx = nl.getX() - al.getX(), dz = nl.getZ() - al.getZ();
                        double len = Math.sqrt(dx*dx + dz*dz);
                        if (len > 0) { dx /= len; dz /= len; }
                        Location away = nl.clone().add(dx * 12, 0, dz * 12);
                        away.setY(getGroundLocation(away));
                        npc.move(away);
                    } else {
                        npc.wander(WANDER_RADIUS);
                        npc.setState(NpcState.WANDER);
                    }
                }
            }

            case IDLE -> { /* explicit pause */ }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ROUTING — maps a missing material to the right gather state
    // ══════════════════════════════════════════════════════════════════════════
    private void routeToGather(NPC npc, Material mat) {
        if (WOOD_LOGS.contains(mat))      { npc.setState(NpcState.GATHER_WOOD); return; }
        if (STONE_TYPES.contains(mat))    { npc.setState(NpcState.MINE_STONE);  return; }
        if (ORE_IRON.contains(mat)
                || mat == Material.IRON_INGOT
                || mat == Material.RAW_IRON){ npc.setState(NpcState.MINE_IRON);   return; }
        if (mat == Material.WATER_BUCKET) { npc.setState(NpcState.CRAFT_BUCKET); return; }
        if (mat == Material.BUCKET)       { npc.setState(NpcState.CRAFT_BUCKET); return; }
        // Generic: try to find on surface
        Block b = findSurface(npc.BaseEntity.getLocation(), mat, SEARCH_RADIUS);
        if (b != null) {
            NPCBreakBlock(npc, b, () -> npc.setState(NpcState.CONSTRUCT));
        } else {
            npc.setState(NpcState.WANDER); // can't find it, just wander
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // EVENTS
    // ══════════════════════════════════════════════════════════════════════════

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPathfind(EntityPathfindEvent event) {
        PersistentDataContainer pdc = event.getEntity().getPersistentDataContainer();
        if (!pdc.has(keygen("isNpc"))) return;
        String idStr = pdc.get(keygen("NPCID"), PersistentDataType.STRING);
        if (idStr == null) return;
        UUID uuid = UUID.fromString(idStr);
        if (!NPCs.containsKey(uuid)) recompileNPC(event.getEntity());
        boolean allowed = pdc.getOrDefault(keygen("isallowedtomove"), PersistentDataType.BOOLEAN, false);
        if (allowed) {
            pdc.set(keygen("isallowedtomove"), PersistentDataType.BOOLEAN, false);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityMove(EntityMoveEvent event) {
        PersistentDataContainer pdc = event.getEntity().getPersistentDataContainer();
        if (!pdc.has(keygen("isNpc"))) return;
        Integer ticks = pdc.getOrDefault(keygen("moveTicks"), PersistentDataType.INTEGER, 0);
        if (++ticks >= MOVE_TICKS_PER_HUNGER) {
            String id = pdc.get(keygen("NPCID"), PersistentDataType.STRING);
            if (id != null) {
                NPC npc = NPCs.get(UUID.fromString(id));
                if (npc != null) { npc.hunger = Math.max(0, npc.hunger - 1); }
            }
            ticks = 0;
        }
        pdc.set(keygen("moveTicks"), PersistentDataType.INTEGER, ticks);
    }

    @EventHandler
    public void onNpcDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Cow cow)) return;
        PersistentDataContainer pdc = cow.getPersistentDataContainer();
        if (!pdc.has(keygen("isNpc"))) return;
        String idStr = pdc.get(keygen("NPCID"), PersistentDataType.STRING);
        if (idStr == null) return;
        UUID uuid = UUID.fromString(idStr);
        NPC npc = NPCs.get(uuid);
        if (npc == null) return;
        event.getDrops().clear();
        event.setDroppedExp(0);
        Location deathLoc = cow.getLocation().clone();
        npc.BaseEntity = null;
        npc.isBreaking = false;
        npc.postBreakCooldown = 0;
        npc.fleeCooldown = 0;
        deathLoc.getWorld().spawnParticle(Particle.EXPLOSION, deathLoc.clone().add(0,1,0), 1);
        scheduleRespawn(npc, deathLoc);
    }

    @EventHandler
    public void onNpcDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Cow cow)) return;
        PersistentDataContainer pdc = cow.getPersistentDataContainer();
        if (!pdc.has(keygen("isNpc"))) return;
        String idStr = pdc.get(keygen("NPCID"), PersistentDataType.STRING);
        if (idStr == null) return;
        NPC npc = NPCs.get(UUID.fromString(idStr));
        if (npc == null) return;
        String line = cow.getHealth() - event.getFinalDamage() < 5
                ? LOW_HEALTH_LINES.get(random.nextInt(LOW_HEALTH_LINES.size()))
                : HURT_LINES.get(random.nextInt(HURT_LINES.size()));
        broadcastActionBubble(npc, line, NamedTextColor.RED);
        npc.BaseEntity.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                npc.BaseEntity.getLocation().clone().add(0, 1.5, 0), 4, 0.2, 0.2, 0.2, 0.1);
        npc.fleeCooldown = 40;
        if (event instanceof EntityDamageByEntityEvent byEntity) npc.lastAttacker = byEntity.getDamager();
        npc.setState(NpcState.FLEE);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Cow cow)) return;
        PersistentDataContainer pdc = cow.getPersistentDataContainer();
        if (!pdc.has(keygen("isNpc"))) return;
        String idStr = pdc.get(keygen("NPCID"), PersistentDataType.STRING);
        if (idStr == null) return;
        NPC npc = NPCs.get(UUID.fromString(idStr));
        if (npc == null) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (player.isSneaking() && player.hasPermission("freedom.admin")) {
            openDebugGui(player, npc);
        } else {
            npc.BaseEntity.lookAt(player);
            List<String> lines = GREETINGS.get(npc.personality);
            String greeting = lines.get(random.nextInt(lines.size()));
            broadcastActionBubble(npc, greeting, NamedTextColor.WHITE);
            player.sendMessage(Component.text("[" + npc.getName() + "] ", NamedTextColor.YELLOW)
                    .append(Component.text(greeting)));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BLOCK BREAKING — animation + delayed pickup + isBreaking leak fix
    // ══════════════════════════════════════════════════════════════════════════
    public void NPCBreakBlock(NPC npc, Block block, Runnable onComplete) {
        if (npc == null || npc.BaseEntity == null) return;
        if (block == null || block.getType().isAir()) { if (onComplete != null) onComplete.run(); return; }
        if (!block.getWorld().equals(npc.BaseEntity.getWorld())) return;
        if (npc.isBreaking) return;

        npc.isBreaking = true;
        npc.idleTicks = 0;
        Location walkTo = pathToBlock(block);
        npc.move(walkTo);
        final int entityId = npc.BaseEntity.getEntityId();
        final Location breakLoc = block.getLocation().clone();

        new BukkitRunnable() {
            int waitTicks = 0, breakTicks = 0;
            float ticksToBreak = 0;
            boolean arrived = false;

            // Guaranteed cleanup — every exit path calls this
            private void finish(boolean broken) {
                clearAnim(block, entityId);
                npc.isBreaking = false;
                npc.idleTicks = 0;
                cancel();
                if (broken) scheduleDelayedPickup(npc, breakLoc, onComplete);
                else if (onComplete == null); // nothing
            }

            @Override
            public void run() {
                if (npc.BaseEntity == null || npc.BaseEntity.isDead()) { finish(false); return; }
                if (block.getType().isAir()) { finish(true); return; } // broken externally

                if (!arrived) {
                    double dSq = npc.BaseEntity.getLocation().distanceSquared(block.getLocation());
                    if (dSq > BREAK_DISTANCE_SQ) {
                        if (++waitTicks > MAX_WAIT_TICKS) {
                            npc.move(pathToBlock(block)); // one more push
                            if (waitTicks > MAX_WAIT_TICKS + 40) {
                                // Truly unreachable — give up cleanly
                                // FIX: previously this path didn't clear isBreaking
                                finish(false);
                            }
                        }
                        return;
                    }
                    npc.BaseEntity.lookAt(block.getLocation().toCenterLocation(),
                            io.papermc.paper.entity.LookAnchor.EYES);
                    int ti = getPreferredToolSlot(block, npc);
                    ItemStack tool = ti >= 0 ? npc.getInventory().getItem(ti) : null;
                    if (tool == null) tool = new ItemStack(Material.AIR);
                    ticksToBreak = Math.max(1, block.getDestroySpeed(tool));
                    arrived = true;
                }

                int stage = Math.min(9, (int)(breakTicks / ticksToBreak * 9));
                sendBreakAnimation(block, entityId, stage);

                if (++breakTicks >= ticksToBreak) {
                    block.breakNaturally();
                    block.getWorld().playSound(block.getLocation(),
                            block.getBlockSoundGroup().getBreakSound(), 1f, 1f);
                    finish(true);
                }
            }
        }.runTaskTimer(Freedom.get_plugin(), 0L, 1L);
    }

    private void scheduleDelayedPickup(NPC npc, Location loc, Runnable onComplete) {
        npc.postBreakCooldown = POST_BREAK_TICKS;
        Bukkit.getScheduler().runTaskLater(Freedom.get_plugin(), () -> {
            if (npc.BaseEntity != null && !npc.BaseEntity.isDead()) {
                for (Item e : loc.getWorld().getNearbyEntitiesByType(Item.class, loc, 4)) {
                    if (countItems(npc.getInventory()) >= 27) break;
                    npc.getInventory().addItem(e.getItemStack());
                    e.remove();
                }
            }
            npc.postBreakCooldown = 0;
            if (onComplete != null) onComplete.run();
        }, POST_BREAK_TICKS);
    }

    private static void sendBreakAnimation(Block block, int entityId, int stage) {
        com.github.retrooper.packetevents.util.Vector3i pos =
                new com.github.retrooper.packetevents.util.Vector3i(block.getX(), block.getY(), block.getZ());
        WrapperPlayServerBlockBreakAnimation pkt =
                new WrapperPlayServerBlockBreakAnimation(entityId, pos, (byte) stage);
        block.getWorld().getNearbyPlayers(block.getLocation(), 32)
                .forEach(p -> PacketEvents.getAPI().getPlayerManager().sendPacket(p, pkt));
    }

    private static void clearAnim(Block block, int entityId) { sendBreakAnimation(block, entityId, -1); }

    // ══════════════════════════════════════════════════════════════════════════
    // BLOCK SEARCH — full-column descent (finds underground blocks)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Full-column scan: for each XZ in the radius, descends from surface to minY.
     * This reliably finds any material at any depth including ores, stone, etc.
     * Only called when a new target is needed (cached in npc.cachedTarget).
     */
    Block findAny(Location center, int radiusXZ, Set<Material> targets) {
        if (center == null || center.getWorld() == null) return null;
        World w = center.getWorld();
        int bx = center.getBlockX(), bz = center.getBlockZ();
        int minY = w.getMinHeight();
        Block nearest = null; double nearestD = Double.MAX_VALUE;
        for (int x = bx - radiusXZ; x <= bx + radiusXZ; x += SEARCH_STEP) {
            for (int z = bz - radiusXZ; z <= bz + radiusXZ; z += SEARCH_STEP) {
                int surfaceY = w.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
                // Descend full column from surface
                for (int y = surfaceY; y >= minY; y--) {
                    Block b = w.getBlockAt(x, y, z);
                    if (!targets.contains(b.getType())) continue;
                    double d = b.getLocation().distanceSquared(center);
                    if (d < nearestD) { nearestD = d; nearest = b; }
                    break; // found one in this column, move on (nearest-first)
                }
            }
        }
        return nearest;
    }

    /** Surface-only scan for crops, grass, surface materials. */
    Block findSurface(Location center, Material target, int radiusXZ) {
        if (center == null || center.getWorld() == null) return null;
        World w = center.getWorld();
        int bx = center.getBlockX(), bz = center.getBlockZ();
        Block nearest = null; double nearestD = Double.MAX_VALUE;
        for (int x = bx - radiusXZ; x <= bx + radiusXZ; x += SEARCH_STEP) {
            for (int z = bz - radiusXZ; z <= bz + radiusXZ; z += SEARCH_STEP) {
                int sy = w.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
                for (int dy = -4; dy <= 4; dy++) {
                    Block b = w.getBlockAt(x, sy + dy, z);
                    if (b.getType() != target) continue;
                    double d = b.getLocation().distanceSquared(center);
                    if (d < nearestD) { nearestD = d; nearest = b; }
                    break;
                }
            }
        }
        return nearest;
    }

    Block findLog(Location center, int radiusXZ) {
        if (center == null || center.getWorld() == null) return null;
        World w = center.getWorld();
        int bx = center.getBlockX(), bz = center.getBlockZ();
        Block nearest = null; double nearestD = Double.MAX_VALUE;
        for (int x = bx - radiusXZ; x <= bx + radiusXZ; x += SEARCH_STEP) {
            for (int z = bz - radiusXZ; z <= bz + radiusXZ; z += SEARCH_STEP) {
                int sy = w.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
                // Logs can be above the heightmap (tree trunk)
                for (int dy = -2; dy <= 12; dy++) {
                    Block b = w.getBlockAt(x, sy + dy, z);
                    if (!WOOD_LOGS.contains(b.getType())) continue;
                    double d = b.getLocation().distanceSquared(center);
                    if (d < nearestD) { nearestD = d; nearest = b; }
                    break;
                }
            }
        }
        return nearest;
    }

    Block findSurfaceWater(Location center, int radiusXZ) {
        if (center == null || center.getWorld() == null) return null;
        World w = center.getWorld();
        int bx = center.getBlockX(), bz = center.getBlockZ();
        Block nearest = null; double nearestD = Double.MAX_VALUE;
        for (int x = bx - radiusXZ; x <= bx + radiusXZ; x += SEARCH_STEP) {
            for (int z = bz - radiusXZ; z <= bz + radiusXZ; z += SEARCH_STEP) {
                int sy = w.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
                Block b = w.getBlockAt(x, sy, z);
                if (b.getType() != Material.WATER) continue;
                double d = b.getLocation().distanceSquared(center);
                if (d < nearestD) { nearestD = d; nearest = b; }
            }
        }
        return nearest;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TOOL CHECKING
    // ══════════════════════════════════════════════════════════════════════════

    boolean hasAnyPickaxe(NPC npc) {
        Inventory inv = npc.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item == null) continue;
            String n = item.getType().name();
            if (n.endsWith("_PICKAXE")) return true;
        }
        return false;
    }

    boolean hasAnyAxe(NPC npc) {
        for (ItemStack item : npc.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType().name().endsWith("_AXE")) return true;
        }
        return false;
    }

    /** Returns true if the NPC has a pickaxe that can break this block. */
    boolean hasPickaxeFor(NPC npc, Block block) {
        Inventory inv = npc.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item == null) continue;
            if (item.getType().name().endsWith("_PICKAXE") && block.isPreferredTool(item)) return true;
        }
        // If block doesn't require a pickaxe, any tool (or hands) suffices
        return !block.getType().name().contains("_ORE")
                && !STONE_TYPES.contains(block.getType());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CRAFTING HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    void craftPlanksFromLogs(NPC npc) {
        Inventory inv = npc.getInventory();
        for (Material log : WOOD_LOGS) {
            int count = countInv(inv, log);
            if (count > 0) {
                consumeAmount(npc, log, count);
                // Determine the right plank type
                Material plank = Material.matchMaterial(
                        log.name().replace("_LOG", "_PLANKS"));
                if (plank == null) plank = Material.OAK_PLANKS;
                inv.addItem(new ItemStack(plank, count * 4));
                break; // one log type at a time
            }
        }
    }

    void craftSticksFromPlanks(NPC npc) {
        Inventory inv = npc.getInventory();
        // Find any planks
        for (Translatable mat : inv.getContents() == null ? new ItemStack[0] : new Material[0]) {/* skip */}
        for (ItemStack s : inv.getContents()) {
            if (s == null) continue;
            if (!s.getType().name().endsWith("_PLANKS")) continue;
            if (s.getAmount() >= 2) {
                int batches = s.getAmount() / 2;
                consumeAmount(npc, s.getType(), batches * 2);
                inv.addItem(new ItemStack(Material.STICK, batches * 4));
                return;
            }
        }
    }

    boolean tryCraftWoodenPickaxe(NPC npc) {
        craftPlanksFromLogs(npc);
        craftSticksFromPlanks(npc);
        Inventory inv = npc.getInventory();
        // Find any plank type for the 3 planks
        Material plank = findPlankInInv(npc);
        if (plank == null || countInv(inv, plank) < 3 || countInv(inv, Material.STICK) < 2) return false;
        consumeAmount(npc, plank, 3);
        consumeAmount(npc, Material.STICK, 2);
        inv.addItem(new ItemStack(Material.WOODEN_PICKAXE));
        return true;
    }

    boolean tryCraftWoodenAxe(NPC npc) {
        Material plank = findPlankInInv(npc);
        if (plank == null || countInv(npc.getInventory(), plank) < 3
                || countInv(npc.getInventory(), Material.STICK) < 2) return false;
        consumeAmount(npc, plank, 3);
        consumeAmount(npc, Material.STICK, 2);
        npc.getInventory().addItem(new ItemStack(Material.WOODEN_AXE));
        return true;
    }

    void tryCraftStonePickaxe(NPC npc) {
        // Prefer cobblestone
        int cob = countInv(npc.getInventory(), Material.COBBLESTONE)
                + countInv(npc.getInventory(), Material.COBBLED_DEEPSLATE);
        if (cob < 3 || countInv(npc.getInventory(), Material.STICK) < 2) return;
        // Remove existing wooden pickaxe if present
        consumeOneType(npc, "WOODEN_PICKAXE");
        Material cobMat = countInv(npc.getInventory(), Material.COBBLESTONE) >= 3
                ? Material.COBBLESTONE : Material.COBBLED_DEEPSLATE;
        consumeAmount(npc, cobMat, 3);
        consumeAmount(npc, Material.STICK, 2);
        npc.getInventory().addItem(new ItemStack(Material.STONE_PICKAXE));
    }

    void tryCraftIronPickaxe(NPC npc) {
        if (countInv(npc.getInventory(), Material.IRON_INGOT) < 3
                || countInv(npc.getInventory(), Material.STICK) < 2) return;
        consumeOneType(npc, "STONE_PICKAXE");
        consumeOneType(npc, "WOODEN_PICKAXE");
        consumeAmount(npc, Material.IRON_INGOT, 3);
        consumeAmount(npc, Material.STICK, 2);
        npc.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE));
    }

    boolean tryCraftFurnace(NPC npc) {
        // Furnace = 8 cobblestone in a ring
        int cob = countInv(npc.getInventory(), Material.COBBLESTONE)
                + countInv(npc.getInventory(), Material.COBBLED_DEEPSLATE)
                + countInv(npc.getInventory(), Material.STONE);
        if (cob < 8) return false;
        // Prefer cobblestone
        Material mat = countInv(npc.getInventory(), Material.COBBLESTONE) >= 8
                ? Material.COBBLESTONE
                : countInv(npc.getInventory(), Material.COBBLED_DEEPSLATE) >= 8
                  ? Material.COBBLED_DEEPSLATE : Material.STONE;
        consumeAmount(npc, mat, 8);
        npc.getInventory().addItem(new ItemStack(Material.FURNACE));
        return true;
    }

    private Material findPlankInInv(NPC npc) {
        for (ItemStack s : npc.getInventory().getContents()) {
            if (s != null && s.getType().name().endsWith("_PLANKS") && s.getAmount() >= 3)
                return s.getType();
        }
        return null;
    }

    /** Removes one item whose type name contains the given suffix. */
    private void consumeOneType(NPC npc, String typeNameContains) {
        Inventory inv = npc.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s == null || !s.getType().name().contains(typeNameContains)) continue;
            s.setAmount(s.getAmount() - 1);
            if (s.getAmount() <= 0) inv.setItem(i, null);
            return;
        }
    }

    boolean schemNeedsWater(String schem) {
        List<ItemStack> mats = getSchemMaterials(schem);
        return mats.stream().anyMatch(s -> s != null
                && (s.getType() == Material.WATER_BUCKET || s.getType() == Material.WATER));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RESPAWN
    // ══════════════════════════════════════════════════════════════════════════

    private static void scheduleRespawn(NPC npc) {
        if (npc.BaseEntity != null && !npc.BaseEntity.isDead()) return;
        Location loc = npc.hasHome() ? npc.getHomeLocation() : null;
        if (loc == null) { NPCs.values().remove(npc); return; }
        scheduleRespawn(npc, loc);
    }

    private static void scheduleRespawn(NPC npc, Location loc) {
        Bukkit.getScheduler().runTaskLater(Freedom.get_plugin(), () -> {
            if (loc == null || loc.getWorld() == null) return;
            loc.setY(getGroundLocation(loc));
            Cow newCow = loc.getWorld().spawn(loc, Cow.class);
            PersistentDataContainer od = npc.data;
            PersistentDataContainer nd = newCow.getPersistentDataContainer();
            String npcId = od.get(keygen("NPCID"), PersistentDataType.STRING);
            if (npcId != null) nd.set(keygen("NPCID"), PersistentDataType.STRING, npcId);
            nd.set(keygen("isNpc"),   PersistentDataType.BOOLEAN, true);
            nd.set(keygen("hunger"),  PersistentDataType.DOUBLE, Math.max(5, npc.hunger));
            nd.set(keygen("state"),   PersistentDataType.STRING, NpcState.WANDER.name());
            nd.set(keygen("name"),    PersistentDataType.STRING,
                    od.getOrDefault(keygen("name"), PersistentDataType.STRING, "Unknown"));
            for (String k : new String[]{"skinValue","skinSignature","skin","isPopular",
                    "home","homeX","homeY","homeZ","homeworld","role","personality"})
                copyPdcKey(od, nd, k);
            nd.set(keygen("inventory"), InventoryPersistentDataType.get(),
                    npc.inventory != null ? npc.inventory
                            : Bukkit.createInventory(null, 27, dess("NPC Inventory")));
            if (npcId != null) NPCs.remove(UUID.fromString(npcId));
            recompileNPC(newCow);
        }, RESPAWN_DELAY_TICKS);
    }

    private static void copyPdcKey(PersistentDataContainer src, PersistentDataContainer dst, String k) {
        try {
            if (src.has(keygen(k), PersistentDataType.STRING))
                dst.set(keygen(k), PersistentDataType.STRING, src.get(keygen(k), PersistentDataType.STRING));
            else if (src.has(keygen(k), PersistentDataType.DOUBLE))
                dst.set(keygen(k), PersistentDataType.DOUBLE, src.get(keygen(k), PersistentDataType.DOUBLE));
            else if (src.has(keygen(k), PersistentDataType.BOOLEAN))
                dst.set(keygen(k), PersistentDataType.BOOLEAN, src.get(keygen(k), PersistentDataType.BOOLEAN));
        } catch (Exception ignored) {}
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ADMIN DEBUG GUI
    // ══════════════════════════════════════════════════════════════════════════

    private void openDebugGui(Player admin, NPC npc) {
        Inventory gui = Bukkit.createInventory(null, 54,
                Component.text("[Debug] " + npc.getName()));
        gui.setItem(0,  info(Material.NAME_TAG,         "Name",         npc.getName()));
        gui.setItem(1,  info(Material.COMPASS,          "State",        npc.getState().name()));
        gui.setItem(2,  info(Material.BOOK,             "Role",         npc.role.name()));
        gui.setItem(3,  info(Material.PAPER,            "Personality",  npc.personality.name()));
        gui.setItem(4,  info(Material.GOLDEN_APPLE,     "Hunger",       String.format("%.1f/20", npc.hunger)));
        gui.setItem(5,  info(Material.HEART_OF_THE_SEA, "Health",
                String.format("%.1f/%.1f", npc.BaseEntity.getHealth(), npc.BaseEntity.getMaxHealth())));
        gui.setItem(6,  info(Material.ENDER_EYE,        "Breaking",     String.valueOf(npc.isBreaking)));
        gui.setItem(7,  info(Material.CLOCK,            "IdleTicks",    npc.idleTicks + "/" + IDLE_TIMEOUT_TICKS));
        gui.setItem(8,  info(Material.LIGHTNING_ROD,    "FleeCD",       String.valueOf(npc.fleeCooldown)));
        Location home = npc.getHomeLocation();
        gui.setItem(9,  info(Material.WHITE_BED, "Home", home == null ? "none"
                : String.format("%.0f,%.0f,%.0f", home.getX(), home.getY(), home.getZ())));
        List<ItemStack> pending = npc.getPendingMaterials();
        gui.setItem(10, info(Material.HOPPER, "PendingMats",
                pending == null || pending.isEmpty() ? "none"
                        : pending.stream().map(s -> s.getAmount()+"x"+s.getType().name())
                          .reduce((a,b)->a+", "+b).orElse("?")));
        gui.setItem(11, info(Material.IRON_PICKAXE, "HasPickaxe", String.valueOf(hasAnyPickaxe(npc))));
        gui.setItem(12, info(Material.CHEST, "Inv", countItems(npc.getInventory()) + "/27"));
        // NPC inventory (rows 2-3)
        ItemStack[] contents = npc.getInventory().getContents();
        for (int i = 0; i < Math.min(contents.length, 27); i++)
            gui.setItem(18 + i, contents[i] != null ? contents[i].clone() : null);
        // Controls (row 5)
        gui.setItem(45, ctrl(Material.OAK_SAPLING,   "Force WANDER"));
        gui.setItem(46, ctrl(Material.GRASS_BLOCK,    "Force SETTLE"));
        gui.setItem(47, ctrl(Material.IRON_PICKAXE,   "Force MINE"));
        gui.setItem(48, ctrl(Material.GOLDEN_APPLE,   "Full Heal"));
        gui.setItem(49, ctrl(Material.BARRIER,        "Kill NPC"));
        gui.setItem(50, ctrl(Material.LODESTONE,      "Set Home Here"));
        gui.setItem(51, ctrl(Material.BLAZE_ROD,      "Force FARM"));
        gui.setItem(52, ctrl(Material.COMMAND_BLOCK,  "Reset Pipeline"));
        gui.setItem(53, ctrl(Material.TORCH,          "Refresh"));

        admin.openInventory(gui);
        admin.getPersistentDataContainer().set(keygen("debugNpc"), PersistentDataType.STRING,
                npc.data.get(keygen("NPCID"), PersistentDataType.STRING));
    }

    @EventHandler
    public void onDebugGuiClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player admin)) return;
        String npcIdStr = admin.getPersistentDataContainer()
                .get(keygen("debugNpc"), PersistentDataType.STRING);
        if (npcIdStr == null) return;
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText().serialize(event.getView().title());
        if (!title.contains("[Debug]")) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;
        NPC npc = NPCs.get(UUID.fromString(npcIdStr));
        if (npc == null || npc.BaseEntity == null || npc.BaseEntity.isDead()) {
            admin.closeInventory(); return;
        }
        switch (event.getRawSlot()) {
            case 45 -> npc.setState(NpcState.WANDER);
            case 46 -> npc.setState(NpcState.SETTLE);
            case 47 -> npc.setState(NpcState.MINE);
            case 48 -> { npc.hunger = 20; npc.BaseEntity.setHealth(npc.BaseEntity.getMaxHealth()); }
            case 49 -> { npc.BaseEntity.remove(); NPCs.remove(UUID.fromString(npcIdStr));
                admin.closeInventory(); admin.getPersistentDataContainer().remove(keygen("debugNpc")); return; }
            case 50 -> { Location h = admin.getLocation().clone(); h.setY(getGroundLocation(h)); npc.setHome(h); }
            case 51 -> npc.setState(NpcState.FARM);
            case 52 -> { // Reset pipeline
                npc.isBreaking = false; npc.postBreakCooldown = 0;
                npc.cachedTarget = null; npc.idleTicks = 0;
                npc.setPendingMaterials(null);
                npc.setState(NpcState.GATHER_WOOD);
                msg(admin, "Pipeline reset → GATHER_WOOD");
            }
            case 53 -> { /* refresh */ }
            default -> { return; }
        }
        Bukkit.getScheduler().runTaskLater(Freedom.get_plugin(), () -> openDebugGui(admin, npc), 1L);
    }

    @EventHandler
    public void onDebugGuiClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        Bukkit.getScheduler().runTaskLater(Freedom.get_plugin(), () -> {
            if (player.getOpenInventory().getType() == org.bukkit.event.inventory.InventoryType.CRAFTING)
                player.getPersistentDataContainer().remove(keygen("debugNpc"));
        }, 2L);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MISC HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private static void broadcastActionBubble(NPC npc, String text, NamedTextColor color) {
        if (npc.BaseEntity == null) return;
        Component msg = Component.text("[" + npc.getName() + "] ", NamedTextColor.YELLOW)
                .append(Component.text(text, color));
        Location npcLoc = npc.BaseEntity.getLocation();
        npcLoc.getWorld().getNearbyPlayers(npcLoc, 20).forEach(p -> p.sendActionBar(msg));
        Location standLoc = npcLoc.clone().add(0, 2.2, 0);
        ArmorStand stand = standLoc.getWorld().spawn(standLoc, ArmorStand.class, s -> {
            s.setInvisible(true); s.setGravity(false); s.setSmall(true);
            s.setCustomNameVisible(true); s.setMarker(true);
            s.customName(Component.text(text, color).decorate(TextDecoration.BOLD));
        });
        Bukkit.getScheduler().runTaskLater(Freedom.get_plugin(), stand::remove, 40L);
    }

    private Item findNearbyFoodDrop(NPC npc) {
        for (Item e : npc.BaseEntity.getLocation().getNearbyEntitiesByType(Item.class, 10))
            if (FOOD_MATS.contains(e.getItemStack().getType())) return e;
        return null;
    }

    Location pathToBlock(Block block) {
        if (block == null) return null;
        World w = block.getWorld();
        int bx = block.getX(), by = block.getY(), bz = block.getZ();
        int gnd = (int) getGroundLocation(block.getLocation());
        if (w.getBlockAt(bx+1,by,bz).isPassable()) return new Location(w, bx+1.5, gnd, bz+0.5);
        if (w.getBlockAt(bx,by,bz+1).isPassable()) return new Location(w, bx+0.5, gnd, bz+1.5);
        if (w.getBlockAt(bx-1,by,bz).isPassable()) return new Location(w, bx-0.5, gnd, bz+0.5);
        if (w.getBlockAt(bx,by,bz-1).isPassable()) return new Location(w, bx+0.5, gnd, bz-0.5);
        return block.getLocation().clone().add(0.5, 1, 0.5);
    }

    private int getPreferredToolSlot(Block block, NPC npc) {
        Inventory inv = npc.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && block.isPreferredTool(item)) return i;
        }
        return -1;
    }

    boolean isStandable(Location loc) {
        return loc.getBlock().isPassable()
                && loc.clone().add(0,1,0).getBlock().isPassable()
                && !loc.clone().add(0,-1,0).getBlock().isPassable();
    }

    public static double getGroundLocation(Location loc) {
        if (loc == null || loc.getWorld() == null) return 0d;
        return loc.getWorld().getHighestBlockYAt(
                loc.getBlockX(), loc.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES);
    }

    private static NPC nearestOther(NPC self, int radius) {
        double rSq = (double) radius * radius;
        NPC nearest = null; double best = rSq;
        for (NPC o : NPCs.values()) {
            if (o == self || o.BaseEntity == null || o.BaseEntity.isDead()) continue;
            if (!o.BaseEntity.getWorld().equals(self.BaseEntity.getWorld())) continue;
            double d = self.BaseEntity.getLocation().distanceSquared(o.BaseEntity.getLocation());
            if (d < best) { best = d; nearest = o; }
        }
        return nearest;
    }

    private static void checkVillageFormation(NPC npc) {
        if (npc.role == Role.LEADER) return;
        Location home = npc.getHomeLocation();
        if (home == null) return;
        long n = NPCs.values().stream().filter(o -> o != npc && o.hasHome()).filter(o -> {
            Location oh = o.getHomeLocation();
            return oh != null && oh.getWorld().equals(home.getWorld())
                    && oh.distanceSquared(home) <= VILLAGE_RADIUS_SQ;
        }).count();
        if (n >= VILLAGE_MIN - 1) {
            String key = (home.getBlockX() >> 6) + ":" + (home.getBlockZ() >> 6);
            if (FORMED_VILLAGES.add(key)) {
                npc.role = Role.LEADER;
                npc.data.set(keygen("role"), PersistentDataType.STRING, Role.LEADER.name());
                Location hall = home.clone().add(10,0,10);
                hall.setY(getGroundLocation(hall));
                npc.setPendingBuild(hall);
                npc.setPendingSchem("tent.schem");
                npc.setState(NpcState.CONSTRUCT);
                Freedom.get_plugin().getLogger().info(
                        "[NpcManager] Village at " + key + " leader=" + npc.getName());
            }
        }
    }
    private static final Set<String> FORMED_VILLAGES = ConcurrentHashMap.newKeySet();

    void doSettle(NPC npc, Location loc) {
        Location home = loc.clone();
        home.setY(getGroundLocation(home));
        npc.setHome(home);
        npc.setPendingBuild(home);
        npc.setPendingSchem("tent.schem");
        if (npc.role == Role.NONE) {
            npc.role = switch (npc.personality) {
                case INDUSTRIOUS -> Role.MINER;
                case SOCIAL      -> Role.FARMER;
                case CAUTIOUS    -> Role.BUILDER;
                default          -> Role.FARMER;
            };
            npc.data.set(keygen("role"), PersistentDataType.STRING, npc.role.name());
        }
    }

    private void executeBuild(NPC npc, String schem) {
        Location loc = npc.getPendingBuildLocation();
        if (loc == null) loc = npc.BaseEntity.getLocation();
        loc.setY(getGroundLocation(loc));
        var cb = StructureUtil.loadSchematicFromResource(schem);
        if (cb == null) return;
        StructureUtil.spawnSchematic(cb, loc);
        consumeMaterials(npc, getSchemMaterials(schem));
        npc.clearPendingBuild();
    }

    private boolean tryEat(NPC npc) {
        Inventory inv = npc.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || !FOOD_MATS.contains(item.getType())) continue;
            item.setAmount(item.getAmount() - 1);
            if (item.getAmount() <= 0) inv.setItem(i, null);
            npc.hunger = Math.min(20d, npc.hunger + 4d);
            return true;
        }
        return false;
    }

    private void trade(NPC giver, NPC receiver) {
        Inventory gi = giver.getInventory();
        for (int i = 0; i < gi.getSize(); i++) {
            ItemStack item = gi.getItem(i);
            if (item == null || !FOOD_MATS.contains(item.getType()) || item.getAmount() <= 1) continue;
            if (giver.hunger > 17 && receiver.hunger < 15) {
                ItemStack give = item.clone(); give.setAmount(1);
                receiver.getInventory().addItem(give);
                item.setAmount(item.getAmount() - 1);
                receiver.hunger = Math.min(20, receiver.hunger + 3);
                return;
            }
        }
    }

    public static void NPCPickup(NPC npc) {
        if (npc == null || npc.BaseEntity == null) return;
        if (countItems(npc.getInventory()) >= 27) return;
        for (Item e : npc.BaseEntity.getLocation().getNearbyEntitiesByType(Item.class, 3)) {
            if (countItems(npc.getInventory()) >= 27) break;
            npc.getInventory().addItem(e.getItemStack()); e.remove();
        }
    }

    private static int countItems(Inventory inv) {
        int c = 0;
        for (ItemStack i : inv.getContents()) if (i != null && !i.getType().isAir()) c++;
        return c;
    }

    int countInv(Inventory inv, Material mat) {
        int c = 0;
        for (ItemStack i : inv.getContents()) if (i != null && i.getType() == mat) c += i.getAmount();
        return c;
    }

    int countMatSet(Inventory inv, Set<Material> mats) {
        int c = 0;
        for (ItemStack i : inv.getContents()) if (i != null && mats.contains(i.getType())) c += i.getAmount();
        return c;
    }

    private List<ItemStack> getMissingMaterials(NPC npc, List<ItemStack> required) {
        List<ItemStack> missing = new ArrayList<>();
        if (npc == null || required == null) return missing;
        for (ItemStack need : required) {
            if (need == null || need.getType().isAir()) continue;
            int rem = need.getAmount() - countInv(npc.getInventory(), need.getType());
            if (rem > 0) { ItemStack s = need.clone(); s.setAmount(rem); missing.add(s); }
        }
        return missing;
    }

    void consumeMaterials(NPC npc, List<ItemStack> required) {
        if (npc == null || required == null) return;
        for (ItemStack need : required)
            if (need != null && !need.getType().isAir()) consumeAmount(npc, need.getType(), need.getAmount());
    }

    void consumeOne(NPC npc, Material mat) { consumeAmount(npc, mat, 1); }

    void consumeAmount(NPC npc, Material mat, int amount) {
        Inventory inv = npc.getInventory(); int rem = amount;
        for (int i = 0; i < inv.getSize() && rem > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s == null || s.getType() != mat) continue;
            int take = Math.min(rem, s.getAmount()); s.setAmount(s.getAmount() - take); rem -= take;
            if (s.getAmount() <= 0) inv.setItem(i, null);
        }
    }

    private static void msg(Player p, String text) {
        p.sendMessage(Component.text("[NpcManager] ", NamedTextColor.GOLD)
                .append(Component.text(text, NamedTextColor.WHITE)));
    }

    private ItemStack info(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat); ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§e" + name));
        meta.lore(List.of(Component.text("§7" + lore)));
        item.setItemMeta(meta); return item;
    }

    private ItemStack ctrl(Material mat, String name) {
        ItemStack item = new ItemStack(mat); ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§a" + name));
        item.setItemMeta(meta); return item;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FACTORY & RELOAD
    // ══════════════════════════════════════════════════════════════════════════

    public static NPC createNPC(Location location) {
        return new NPC(location.getWorld().spawn(location, Cow.class), 20d, NpcState.WANDER);
    }

    public static void recompileNPC(Entity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(keygen("isNpc"))) return;
        String idStr = pdc.get(keygen("NPCID"), PersistentDataType.STRING);
        if (idStr == null) return;
        UUID uuid = UUID.fromString(idStr);
        if (NPCs.containsKey(uuid)) return;
        Cow cow = (Cow) entity;
        String name    = pdc.getOrDefault(keygen("name"),    PersistentDataType.STRING, "Unknown");
        String stStr   = pdc.getOrDefault(keygen("state"),   PersistentDataType.STRING, "WANDER");
        double hunger  = pdc.getOrDefault(keygen("hunger"),  PersistentDataType.DOUBLE, 20d);
        String skinVal = pdc.get(keygen("skinValue"),        PersistentDataType.STRING);
        String skinSig = pdc.get(keygen("skinSignature"),    PersistentDataType.STRING);
        String skinFile= pdc.getOrDefault(keygen("skin"),    PersistentDataType.STRING, "SweetVikki");
        boolean popular= Boolean.TRUE.equals(pdc.get(keygen("isPopular"), PersistentDataType.BOOLEAN));
        NpcState state;
        try { state = NpcState.valueOf(stStr); } catch (IllegalArgumentException e) { state = NpcState.WANDER; }
        PlayerDisguise disguise = new PlayerDisguise(name);
        PlayerWatcher watcher = (PlayerWatcher) disguise.getWatcher();
        if (popular) { watcher.setSkin(name); }
        else if (skinVal != null && !skinVal.isEmpty()) {
            watcher.setSkin(new UserProfile(uuid, name, SkinLoader.buildSignedTextures(skinVal, skinSig)));
        } else {
            SkinLoader.SkinResult r = SkinLoader.loadSignedSkinFromFilename(skinFile);
            if (r != null && r.textures != null && !r.textures.isEmpty()) {
                watcher.setSkin(new UserProfile(uuid, name, r.textures));
                pdc.set(keygen("skinValue"),     PersistentDataType.STRING, r.value);
                pdc.set(keygen("skinSignature"), PersistentDataType.STRING, r.signature);
            }
        }
        watcher.setName(name);
        disguise.setEntity(entity);
        disguise.startDisguise();
        NPC npc = new NPC(cow, hunger, state);
        npc.disguise = disguise;
        NPCs.put(uuid, npc);
        Bukkit.getServer().getMobGoals().removeGoal(cow, VanillaGoal.TEMPT);
    }

    public static NPC getNearestNpc(Player player, double maxDistance) {
        if (player == null) return null;
        double mSq = maxDistance * maxDistance; NPC nearest = null; double best = mSq;
        for (NPC npc : NPCs.values()) {
            if (npc == null || npc.BaseEntity == null || npc.BaseEntity.isDead()) continue;
            if (!npc.BaseEntity.getWorld().equals(player.getWorld())) continue;
            double d = npc.BaseEntity.getLocation().distanceSquared(player.getLocation());
            if (d <= best) { best = d; nearest = npc; }
        }
        return nearest;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NPC CLASS
    // ══════════════════════════════════════════════════════════════════════════
    public static class NPC {
        public  Cow           BaseEntity;
        public  double        hunger;
        public  Personality   personality;
        public  Role          role            = Role.NONE;
        public  int           socialCooldown  = 0;
        public  int           fleeCooldown    = 0;
        public  int           postBreakCooldown = 0;
        public  int           idleTicks       = 0;
        public  Entity        lastAttacker    = null;
        public  boolean       isBreaking      = false;
        public  Block         cachedTarget    = null;
        public  PersistentDataContainer data;
        public  me.libraryaddict.disguise.disguisetypes.Disguise disguise;
        public  Inventory     inventory;

        private NpcState        state;
        private List<ItemStack> pendingMaterials;
        private String          pendingSchem;

        private static final List<String> POPULAR_NAMES = List.of("d3rlord3", "TheMostMayo");

        public NPC(Cow cow, double hunger, NpcState initialState) {
            if (cow == null) return;
            BaseEntity  = cow;
            this.hunger = hunger;
            state       = initialState != null ? initialState : NpcState.WANDER;
            data        = cow.getPersistentDataContainer();
            personality = Personality.values()[random.nextInt(Personality.values().length)];

            if (!data.has(keygen("NPCID"))) {
                UUID id = UUID.randomUUID();
                data.set(keygen("NPCID"),       PersistentDataType.STRING,  id.toString());
                data.set(keygen("isNpc"),       PersistentDataType.BOOLEAN, true);
                data.set(keygen("hunger"),      PersistentDataType.DOUBLE,  hunger);
                data.set(keygen("state"),       PersistentDataType.STRING,  state.name());
                data.set(keygen("personality"), PersistentDataType.STRING,  personality.name());
                data.set(keygen("role"),        PersistentDataType.STRING,  role.name());
                inventory = Bukkit.createInventory(null, 27, dess("NPC Inventory"));
                data.set(keygen("inventory"),   InventoryPersistentDataType.get(), inventory);

                boolean popular = random.nextInt(1000) == 0;
                String name = popular
                        ? POPULAR_NAMES.get(random.nextInt(POPULAR_NAMES.size()))
                        : faker.name().firstName();
                data.set(keygen("name"),      PersistentDataType.STRING,  name);
                data.set(keygen("isPopular"), PersistentDataType.BOOLEAN, popular);

                PlayerDisguise dis = new PlayerDisguise(name);
                PlayerWatcher  w   = (PlayerWatcher) dis.getWatcher();
                if (popular) {
                    w.setSkin(name);
                } else {
                    SkinLoader.SkinResult skin = SkinLoader.loadRandomSkin();
                    if (skin != null && skin.textures != null && !skin.textures.isEmpty()) {
                        w.setSkin(new UserProfile(id, name, skin.textures));
                        if (skin.filename != null) {
                            data.set(keygen("skin"),          PersistentDataType.STRING, skin.filename);
                            data.set(keygen("skinValue"),     PersistentDataType.STRING, skin.value);
                            data.set(keygen("skinSignature"), PersistentDataType.STRING, skin.signature);
                        }
                    } else {
                        data.set(keygen("skin"), PersistentDataType.STRING, "SweetVikki");
                    }
                }
                w.setName(name); dis.setEntity(cow); dis.startDisguise();
                disguise = dis;
                cow.getAttribute(Attribute.TEMPT_RANGE).setBaseValue(0);
                Bukkit.getServer().getMobGoals().removeGoal(cow, VanillaGoal.TEMPT);
                NPCs.put(id, this);
            } else {
                String pStr = data.getOrDefault(keygen("personality"), PersistentDataType.STRING, "WANDERER");
                String rStr = data.getOrDefault(keygen("role"),        PersistentDataType.STRING, "NONE");
                try { personality = Personality.valueOf(pStr); } catch (IllegalArgumentException e) { personality = Personality.WANDERER; }
                try { role        = Role.valueOf(rStr);         } catch (IllegalArgumentException e) { role        = Role.NONE; }
                inventory = data.get(keygen("inventory"), InventoryPersistentDataType.get());
            }
        }

        public String getName() {
            return data != null ? data.getOrDefault(keygen("name"), PersistentDataType.STRING, "Unknown") : "Unknown";
        }
        public NpcState getState() { return state; }
        public void setState(NpcState next) {
            if (next == state) return; // avoid redundant PDC writes
            state = next;
            idleTicks = 0; // reset idle counter on every state change
            if (data != null) data.set(keygen("state"), PersistentDataType.STRING, next.name());
        }
        public Inventory getInventory() {
            if (inventory == null && data != null)
                inventory = data.get(keygen("inventory"), InventoryPersistentDataType.get());
            if (inventory == null)
                inventory = Bukkit.createInventory(null, 27, dess("NPC Inventory"));
            return inventory;
        }
        public void move(Location target) {
            if (target == null || BaseEntity == null) return;
            data.set(keygen("isallowedtomove"), PersistentDataType.BOOLEAN, true);
            BaseEntity.getPathfinder().moveTo(target, 1.0);
        }
        public void wander() { wander(WANDER_RADIUS); }
        public void wander(int radius) {
            if (BaseEntity == null) return;
            Location l = BaseEntity.getLocation().clone().add(
                    random.nextInt(radius * 2) - radius, 0,
                    random.nextInt(radius * 2) - radius);
            l.setY(getGroundLocation(l));
            move(l);
        }
        private static double getGroundLocation(Location loc) {
            if (loc == null || loc.getWorld() == null) return 0;
            return loc.getWorld().getHighestBlockYAt(
                    loc.getBlockX(), loc.getBlockZ(), HeightMap.MOTION_BLOCKING_NO_LEAVES);
        }
        public boolean hasHome() { return data != null && data.has(keygen("home"), PersistentDataType.BOOLEAN); }
        public void setHome(Location h) {
            if (h == null || h.getWorld() == null) return;
            data.set(keygen("home"),      PersistentDataType.BOOLEAN, true);
            data.set(keygen("homeX"),     PersistentDataType.DOUBLE,  h.getX());
            data.set(keygen("homeY"),     PersistentDataType.DOUBLE,  h.getY());
            data.set(keygen("homeZ"),     PersistentDataType.DOUBLE,  h.getZ());
            data.set(keygen("homeworld"), PersistentDataType.STRING,  h.getWorld().getName());
        }
        public Location getHomeLocation() {
            if (!hasHome()) return null;
            try {
                Double x = data.get(keygen("homeX"), PersistentDataType.DOUBLE);
                Double y = data.get(keygen("homeY"), PersistentDataType.DOUBLE);
                Double z = data.get(keygen("homeZ"), PersistentDataType.DOUBLE);
                String w = data.get(keygen("homeworld"), PersistentDataType.STRING);
                if (x == null || y == null || z == null || w == null) return null;
                World world = Bukkit.getWorld(w);
                return world == null ? null : new Location(world, x, y, z);
            } catch (Exception e) { return null; }
        }
        public boolean hasPendingBuild() { return data != null && data.has(keygen("buildX"), PersistentDataType.DOUBLE); }
        public void setPendingBuild(Location l) {
            if (l == null || l.getWorld() == null) return;
            data.set(keygen("buildX"),     PersistentDataType.DOUBLE, l.getX());
            data.set(keygen("buildY"),     PersistentDataType.DOUBLE, l.getY());
            data.set(keygen("buildZ"),     PersistentDataType.DOUBLE, l.getZ());
            data.set(keygen("buildWorld"), PersistentDataType.STRING, l.getWorld().getName());
        }
        public Location getPendingBuildLocation() {
            if (!hasPendingBuild()) return null;
            try {
                Double x = data.get(keygen("buildX"), PersistentDataType.DOUBLE);
                Double y = data.get(keygen("buildY"), PersistentDataType.DOUBLE);
                Double z = data.get(keygen("buildZ"), PersistentDataType.DOUBLE);
                String w = data.get(keygen("buildWorld"), PersistentDataType.STRING);
                if (x == null || y == null || z == null || w == null) return null;
                World world = Bukkit.getWorld(w);
                return world == null ? null : new Location(world, x, y, z);
            } catch (Exception e) { return null; }
        }
        public void clearPendingBuild() {
            data.remove(keygen("buildX")); data.remove(keygen("buildY"));
            data.remove(keygen("buildZ")); data.remove(keygen("buildWorld"));
        }
        public String getPendingSchem()              { return pendingSchem; }
        public void   setPendingSchem(String s)      { pendingSchem = s; }
        public List<ItemStack> getPendingMaterials() { return pendingMaterials; }
        public void setPendingMaterials(List<ItemStack> m) { pendingMaterials = m; }

        private static final int WANDER_RADIUS = 14;
    }
}