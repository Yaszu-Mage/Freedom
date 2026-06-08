package xyz.yaszu.freedom.Soul.Base;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Soul.Ultra.Black;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;

public class BaseBlack extends Util implements Base_Soul, Listener {

    @Override
    public String Name_For_Container() {
        return "BaseBlack";
    }

    @Override
    public Component Name() {
        return dess("<shadow:#000000FF><b><yellow><gradient:#0f000f:#555555:#aa00aa>Black</gradient>");
    }

    @Override
    public Component Description() {
        return dess("Your appearance is as malleable as clay");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.WOODEN_SHOVEL);
    }

    @Override
    public Component AbilityOneName() {
        return dess("Trickster");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("Teleport to a saved location, it will take you 5 seconds.");
    }

    @Override
 public void AbilityOne(Player player) {
        AbilityOne(player, false);
    }
    @Override
 public void AbilityOne(Player player, boolean is_imbue) {
        BlackInformation blackInformation = BlackInformation.people.getOrDefault(player.getUniqueId(),new BlackInformation());
        BlackInformation.BlackMenu blackMenu = new BlackInformation.BlackMenu();
        blackMenu.constructMenu(blackInformation,deleteSlotKey);
        player.openInventory(blackMenu.getInventory());
//        if (!player.getPersistentDataContainer().has(keygen("black_save"), PersistentDataType.BOOLEAN)) {
//            save(player);
//            return;
//        }
//        if (player.getPersistentDataContainer().get(keygen("black_save"), PersistentDataType.BOOLEAN)) {
//
//            load(player);
//        }
    }

    public void load(Player player) {

        //VFX
        try {
            new BukkitRunnable() {

                @Override
                public void run() {
                    if (!player.getPersistentDataContainer().has(keygen("blacksave"))) {
                        this.cancel();
                    }
                    Location loadingLocation = new Location(
                            Bukkit.getWorld(player.getPersistentDataContainer().get(keygen("blackworld"),PersistentDataType.STRING)),
                            player.getPersistentDataContainer().get(keygen("blacksaveX"),PersistentDataType.DOUBLE),
                            player.getPersistentDataContainer().get(keygen("blacksaveY"),PersistentDataType.DOUBLE),
                            player.getPersistentDataContainer().get(keygen("blacksaveZ"),PersistentDataType.DOUBLE)

                    );
                    if (loadingLocation != null) {
                        if (loadingLocation.distanceSquared(player.getLocation()) >= 10000) {
                            player.sendMessage(dess("Too Far! (100 blocks)"));
                            player.getPersistentDataContainer().remove(keygen("black_save"));
                            player.getPersistentDataContainer().remove(keygen("blacksaveX"));
                            player.getPersistentDataContainer().remove(keygen("blacksaveY"));
                            player.getPersistentDataContainer().remove(keygen("blacksaveZ"));
                            player.getPersistentDataContainer().remove(keygen("blackworld"));
                            this.cancel();
                            return;
                        }
                    }
                    drawCircle(player.getLocation(),1,player.getWorld(),16,Particle.SMOKE);
                    drawCircle(player.getLocation(),0.5,player.getWorld(),8,Particle.LARGE_SMOKE);
                    drawCircle(loadingLocation,1,player.getWorld(),16,Particle.SMOKE);
                    drawCircle(loadingLocation,0.5,player.getWorld(),8,Particle.DRAGON_BREATH);
                }
            }.runTaskTimer(Freedom.get_plugin(),20,20);
        } catch (Exception ignored) {}
        try {
            new BukkitRunnable() {
                public int tick = 0;
                public double last_health = player.getHealth();
                Location loadingLocation = player.getLocation();
                @Override
                public void run() {
                    if (loadingLocation != null) {
                        if (loadingLocation.distanceSquared(player.getLocation()) >= 10000) {
                            player.sendMessage(dess("Too Far! (100 blocks)"));
                            player.getPersistentDataContainer().remove(keygen("black_save"));
                            player.getPersistentDataContainer().remove(keygen("blacksaveX"));
                            player.getPersistentDataContainer().remove(keygen("blacksaveY"));
                            player.getPersistentDataContainer().remove(keygen("blacksaveZ"));
                            player.getPersistentDataContainer().remove(keygen("blackworld"));
                            this.cancel();
                            return;
                        }
                    }
                    if (player.getPersistentDataContainer().has(keygen("blackworld"),PersistentDataType.STRING)) {
                        Location loadingLocation = new Location(
                                Bukkit.getWorld(player.getPersistentDataContainer().get(keygen("blackworld"),PersistentDataType.STRING)),
                                player.getPersistentDataContainer().get(keygen("blacksaveX"),PersistentDataType.DOUBLE),
                                player.getPersistentDataContainer().get(keygen("blacksaveY"),PersistentDataType.DOUBLE),
                                player.getPersistentDataContainer().get(keygen("blacksaveZ"),PersistentDataType.DOUBLE)
                        );
                    }

                    if (player.getHealth() > last_health || !player.isSneaking() || player.isDead()) {
                        this.cancel();
                        player.sendActionBar(dess("Teleport Cancelled."));
                    }
                    if (tick >= 4) {
                        drawCircle(player.getLocation(),1,player.getWorld(),32,Particle.GUST);
                        player.playSound(player.getLocation(),Sound.ENTITY_WIND_CHARGE_WIND_BURST,1,1);
                        List<Entity> entities = player.getNearbyEntities(2,2,2);
                        for (Entity entity : entities) {
                            if (entity instanceof Player trustcheck) {
                                if (player.getPersistentDataContainer().has(keygen("trustedby"))) {
                                    String trustedby = trustcheck.getPersistentDataContainer().get(keygen("trustedby"),PersistentDataType.STRING);
                                    if (trustedby.contains(player.getName())) {
                                        drawCircle(trustcheck.getLocation(),1,player.getWorld(),32,Particle.REVERSE_PORTAL);
                                        trustcheck.teleport(loadingLocation);
                                        drawCircle(loadingLocation,1,loadingLocation.getWorld(),32,Particle.REVERSE_PORTAL);
                                        player.getWorld().playSound(player.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,1,1);
                                    }

                                }
                            }
                        }
                        player.teleport(loadingLocation);
                        drawCircle(loadingLocation,1,loadingLocation.getWorld(),32,Particle.REVERSE_PORTAL);
                        player.playSound(player.getLocation(),Sound.ENTITY_ENDERMAN_TELEPORT,1,1);
                        this.cancel();
                    } else {
                        if (threes(tick) == 3) {
                            player.sendActionBar(dess("Teleporting in " + (4-tick) + " seconds..."));
                        } else if (threes(tick) == 2) {
                            player.sendActionBar(dess("Teleporting in " + (4-tick) + " seconds.."));
                        } else {
                            player.sendActionBar(dess("Teleporting in " + (4-tick) + " seconds."));
                        }
                        drawCircle(player.getLocation(),1.5,player.getWorld(),32,Particle.SOUL_FIRE_FLAME);
                        drawCircle(player.getLocation(),1,player.getWorld(),32,Particle.VAULT_CONNECTION);
                        drawCircle(loadingLocation,1,player.getWorld(),32,Particle.VAULT_CONNECTION);
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO,SoundCategory.PLAYERS,1,tick);
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL,SoundCategory.PLAYERS,1,tick-1);
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 1.0f, 0.5f);
                    }
                    tick = tick + 1;
                }
                @Override
                public synchronized void cancel() throws IllegalStateException {
                    player.getPersistentDataContainer().remove(keygen("black_save"));
                    player.getPersistentDataContainer().remove(keygen("blacksaveX"));
                    player.getPersistentDataContainer().remove(keygen("blacksaveY"));
                    player.getPersistentDataContainer().remove(keygen("blacksaveZ"));
                    player.getPersistentDataContainer().remove(keygen("blackworld"));
                    Bukkit.getScheduler().cancelTask(getTaskId());
                }
            }.runTaskTimer(Freedom.get_plugin(),0,20);
        } catch (Exception ignored) {}
    }
    public static double threes(int num) {
        if (num % 3 == 0) {
            return 3;
        }
        if (num % 2 == 0) {
            return 2;
        }
        return 1;
    }

    public static boolean isMultipleofTwenty(int num) {
        // The condition (num % 10 == 0) is true if the remainder is 0.
        return (num % 20 == 0);
    }
    public void save(Player player) {
        player.getPersistentDataContainer().set(keygen("black_save"), PersistentDataType.BOOLEAN, true);
        player.getPersistentDataContainer().set(keygen("blacksaveX"), PersistentDataType.DOUBLE, player.getLocation().getX());
        player.getPersistentDataContainer().set(keygen("blacksaveY"), PersistentDataType.DOUBLE, player.getLocation().getY());
        player.getPersistentDataContainer().set(keygen("blacksaveZ"), PersistentDataType.DOUBLE, player.getLocation().getZ());
        player.getPersistentDataContainer().set(keygen("blackworld"), PersistentDataType.STRING, player.getWorld().getName());
        player.sendActionBar(dess("Saved Location at (" + player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + ")"));
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick > 8) {
                    this.cancel();
                }
                if (tick == 0) {
                    player.getWorld().playSound(player.getLocation(),Sound.BLOCK_NOTE_BLOCK_GUITAR,1,1 + Math.min((tick/10),0));
                }
                if (tick % 5 == 0) {
                    player.getWorld().playSound(player.getLocation(),Sound.BLOCK_NOTE_BLOCK_GUITAR,1,1 + Math.min((tick/10),0));
                }

                tick = tick + 1;
            }
        }.runTaskTimer(Freedom.get_plugin(),0,1);
    }
    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.ACACIA_HANGING_SIGN);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("Mimicry");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("Take mimic the Apearance and name of a player.");
    }





    @Override
 public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        AbilityTwo(player, ability_item, false);
    }

    @Override
 public void AbilityTwo(Player player, ItemStack ability_item, boolean is_imbue) throws MineSkinException, DataRequestException {
        if (can_ability(AbilityTwo_Cooldown(),abilityTwoCooldowns,player.getUniqueId()) && !player.getPersistentDataContainer().has(keygen("disguised"), PersistentDataType.BOOLEAN)) {
            player.playSound(player.getLocation(),Sound.BLOCK_DISPENSER_DISPENSE,1,1);
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.5f);
            player.getWorld().spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
            InventoryGui inventoryGui = new InventoryGui();
            inventoryGui.setInventory(player);
            player.openInventory(inventoryGui.getInventory());

        } else {
            // no no ability
            if (abilityTwoCooldowns.get(player.getUniqueId()) != null) {
                double seconds = (double) (effective_cooldown(AbilityTwo_Cooldown(), player.getUniqueId()) - (System.currentTimeMillis() - abilityTwoCooldowns.get(player.getUniqueId()))) / 1000;
                player.sendActionBar(dess("You can't use this ability yet, wait " + seconds + " seconds"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            }
        }
        if (player.getPersistentDataContainer().has(keygen("disguised"), PersistentDataType.BOOLEAN)) {
            Freedom.get_plugin().getLogger().info(String.valueOf(Black.originalProfiles.get(player.getUniqueId())));
            if (Black.originalProfiles.get(player.getUniqueId()) != null) {
                Freedom.get_plugin().getLogger().info("Removing Disguises!");
                DisguiseAPI.undisguiseToAll(player);
                PlayerDisguise disguise =Black.originalProfiles.get(player.getUniqueId());
                player.getWorld();
                World world = player.getWorld();
                Location location = player.getLocation();
                world.spawnParticle(Particle.SMOKE, location,128);
                world.playSound(location,Sound.ENTITY_WARDEN_HEARTBEAT,1,1);
                disguise.removePlayer(player);
                disguise.stopDisguise();
                disguise.removeDisguise();
                player.playerListName(player.name());
                if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) == null) {
                    player.getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(keygen("black"),-0.10, AttributeModifier.Operation.ADD_NUMBER));
                }
                setSkinByName(player,player.getName());
                Black.originalProfiles.remove(player.getUniqueId());
            }
            player.getPersistentDataContainer().remove(keygen("disguised"));
            player.sendActionBar(dess("Disguise Removed."));
        }
    }
    public static Random random = new Random();
    public static HashMap<UUID,PlayerDisguise> originalProfiles = Black.originalProfiles;
    @EventHandler
    public void InventoryClickEvent (InventoryClickEvent event) throws MineSkinException, DataRequestException {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof InventoryGui inventoryGui && event.getCurrentItem() != null) {
            // yayas correct holder
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();
            Player baller = Bukkit.getPlayer(UUID.fromString(item.getPersistentDataContainer().get(keygen("player_uuid"),PersistentDataType.STRING)));
            PlayerDisguise disguise = new PlayerDisguise(baller);
            disguise.setHearSelfDisguise(false);
            disguise.setEntity(player);
            player.getPersistentDataContainer().set(keygen("disguised"),PersistentDataType.BOOLEAN,true);
            disguise.startDisguise();
            if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) != null) {
                player.getAttribute(Attribute.SCALE).removeModifier(keygen("black"));
            }
            player.playerListName(baller.name());
            setSkinByName(player,baller.getName());
            originalProfiles.put(player.getUniqueId(),disguise);
            World world = player.getWorld();
            Location location = player.getLocation();
            //VFX
            int disguiseid = random.nextInt();
            player.getPersistentDataContainer().set(keygen("diguiseid"),PersistentDataType.INTEGER,disguiseid);
            world.playSound(location,Sound.ENTITY_WARDEN_EMERGE,1,1);
            world.spawnParticle(Particle.SMOKE, location,128);
            player.closeInventory();
            abilityTwoCooldowns.put(player.getUniqueId(),System.currentTimeMillis());
            new BukkitRunnable() {
                @Override
                public void run() {
                    disguise.stopDisguise();
                    disguise.removeDisguise();
                    player.playerListName(player.name());
                    if (player.getPersistentDataContainer().has(keygen("disguiseid"),PersistentDataType.INTEGER)) {
                        if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) == null) {
                            player.getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(keygen("black"),0.80, AttributeModifier.Operation.ADD_NUMBER));
                        }
                        int gotid = player.getPersistentDataContainer().get(keygen("disguiseid"), PersistentDataType.INTEGER);
                        player.playerListName(player.name());
                        if (disguise.isDisguiseInUse() && disguiseid == gotid) {
                            world.spawnParticle(Particle.SMOKE, location, 128);
                            try {
                                setSkinByName(player,player.getName());
                            } catch (MineSkinException e) {
                                throw new RuntimeException(e);
                            } catch (DataRequestException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    this.cancel();
                }
            }.runTaskLater(Freedom.get_plugin(),3000);
            event.setCancelled(true);
        }
    }




    public HashMap<Player,InventoryGui> inventoryGui = new HashMap<>();

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
                    meta.displayName(soulListener.SOULS.get(soulType).Name().append(dess(" " + name)));
                    skull.displayName();
                    skull.setItemMeta(meta);
                    inventory.setItem(iteration,skull);
                    iteration++;
                }
            }
        }

    }

    public static void join (Player player) throws MineSkinException, DataRequestException {
        setSkinByName(player,player.getName());
        if (player.getPersistentDataContainer().has(keygen("soul"))) {
        for (PotionEffect potion : player.getActivePotionEffects()) {
            if (potion.getDuration() == PotionEffect.INFINITE_DURATION) {
                player.removePotionEffect(potion.getType());
            }
        }

        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        if (soulType == SoulTypes.BaseBlack) {
            if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) == null) {
                player.getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(keygen("black"),-0.10, AttributeModifier.Operation.ADD_NUMBER));
            }
        } else {
            if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) != null) {
                player.getAttribute(Attribute.SCALE).removeModifier(keygen("black"));
            }

        }
            if (soulType == SoulTypes.Orange) {
                if (!player.getPersistentDataContainer().has(keygen("cancurse"))) {
                    player.getPersistentDataContainer().set(keygen("cancurse"),PersistentDataType.BOOLEAN,true);
                }
            }
    }
    }

// reviewers SOS why expand this system
    @EventHandler
    public void Respawnevent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        if (soulType == SoulTypes.None) {
            player.addPotionEffect(PotionEffectType.STRENGTH.createEffect(PotionEffect.INFINITE_DURATION,1));
            player.addPotionEffect(PotionEffectType.SPEED.createEffect(PotionEffect.INFINITE_DURATION,1));
            player.addPotionEffect(PotionEffectType.HEALTH_BOOST.createEffect(PotionEffect.INFINITE_DURATION,1));
        }
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws MineSkinException, DataRequestException {
        setSkinByName(event.getPlayer(),event.getPlayer().getName());
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().has(keygen("soul"))) {
            Black.join(player);
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        if (soulType == SoulTypes.Black) {
            if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) == null) {
                player.getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(keygen("black"),-0.10, AttributeModifier.Operation.ADD_NUMBER));
            }
        }
        if (soulType == SoulTypes.Orange) {
            if (!player.getPersistentDataContainer().has(keygen("cancurse"))) {
                player.getPersistentDataContainer().set(keygen("cancurse"),PersistentDataType.BOOLEAN,true);
            }
        }
        if (!inventoryGui.isEmpty()) {
            for (Player iteratedplayer : inventoryGui.keySet()) {
                inventoryGui.get(iteratedplayer).setInventory(player);
            }
        }
    }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (!inventoryGui.isEmpty()) {
            for (Player player : inventoryGui.keySet()) {
                if (event.getPlayer() == player) {
                    inventoryGui.remove(player);
                    break;
                }
                inventoryGui.get(player).setInventory(player);
            }
        }
    }

    public void playerSneakEvent(Player player) {
        Freedom.get_plugin().getLogger().info("RAN");
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        player.getPersistentDataContainer().set(keygen("SoulPoint"),PersistentDataType.DOUBLE,SoulPoints - 5);
        if (soulType == SoulTypes.Black || soulType == SoulTypes.BaseBlack) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(player.isSneaking()) {
                        player.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(70,1));
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Freedom.get_plugin(),1,60);

        }
    }

    @Override
    public Component Passive_Description() {
        return dess("You are slightly shorter");
    }

    @Override
    public void Passive(Player player, Object event) {

    }

    @Override
    public Component ActivePassive_Description() {
        return dess("When you sneak you are invisible");
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 30000;
    }

    @Override
    public long AbilityOne_Cooldown(Object obj) {
        return 0;
    }

    @Override
    public void ActivePassive(Player player) {

    }
    // ─── PDC key ─────────────────────────────────────────────────────────────────

    // Must match the `self` key inside BlackInformation.
    NamespacedKey blackInformationKey = keygen("blackinformation");

    // PDC tag stamped on delete-button ItemStacks so we can identify them by slot.
    NamespacedKey deleteSlotKey = keygen("black_delete_slot");

    // ─── Inventory click handler ──────────────────────────────────────────────────

    @EventHandler
    public void onPlayerClickInventory(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlackInformation.BlackMenu)) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        BlackInformation blackInformation = BlackInformation.people.computeIfAbsent(
                player.getUniqueId(), id -> new BlackInformation(player));

        int rawSlot = event.getRawSlot();

        // ── Delete button row (slots 9–17) ───────────────────────────────────────
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.hasItemMeta()) {
            var pdc = clicked.getItemMeta().getPersistentDataContainer();
            if (pdc.has(deleteSlotKey, PersistentDataType.INTEGER)) {
                int targetSlot = pdc.get(deleteSlotKey, PersistentDataType.INTEGER);
                if (targetSlot >= 0 && targetSlot < 9) {
                    boolean hadData = targetSlot < blackInformation.locations.size()
                            && blackInformation.locations.get(targetSlot) != null;

                    if (!hadData) {
                        player.sendMessage(dess("<red>✗ Slot " + (targetSlot + 1) + " is already empty."));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                        return;
                    }

                    // Clear the slot.
                    while (blackInformation.items.size() <= targetSlot)     blackInformation.items.add(null);
                    while (blackInformation.locations.size() <= targetSlot) blackInformation.locations.add(null);
                    blackInformation.items.set(targetSlot, null);
                    blackInformation.locations.set(targetSlot, null);

                    persist(blackInformation, player);

                    // Refresh the open inventory so the player sees the change immediately.
                    BlackInformation.BlackMenu menu = (BlackInformation.BlackMenu) event.getInventory().getHolder();
                    menu.constructMenu(blackInformation, deleteSlotKey);

                    player.sendMessage(dess("<yellow>🗑 Slot " + (targetSlot + 1) + " cleared."));
                    player.sendActionBar(dess("<yellow><b>🗑 Slot " + (targetSlot + 1) + " cleared"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.8f);
                }
                return; // Always consume delete-row clicks even if no-op.
            }
        }

        // ── Top row (slots 0–8): save or teleport ─────────────────────────────────
        if (rawSlot < 0 || rawSlot >= 9) return;

        ItemStack currentItem = event.getCurrentItem();
        // A slot is "empty" if and only if it carries the black_placeholder PDC marker
        // that constructMenu stamps on every glass-pane placeholder.
        // The old isSimilar(amount=1) check failed for slots 1–8 because placeholders
        // are created with amount = i+1, so slots beyond the first were never recognised
        // as empty and were incorrectly treated as saved-location clicks.
        boolean clickedEmptySlot = currentItem == null
                || currentItem.getType().isAir()
                || (currentItem.hasItemMeta()
                && currentItem.getItemMeta()
                .getPersistentDataContainer()
                .has(keygen("black_placeholder"), PersistentDataType.BOOLEAN));

        if (clickedEmptySlot) {
            // ── Save current location into this slot ──────────────────────────────
            long savedCount = blackInformation.locations.stream().filter(l -> l != null).count();
            if (savedCount >= 9) {
                player.sendMessage(dess("<red>✗ All 9 slots are full. Delete a slot first (bottom row)."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                return;
            }

            while (blackInformation.items.size() <= rawSlot)     blackInformation.items.add(null);
            while (blackInformation.locations.size() <= rawSlot) blackInformation.locations.add(null);

            Location loc = player.getLocation().clone();
            blackInformation.items.set(rawSlot, player.getInventory().getItemInMainHand().clone());
            blackInformation.locations.set(rawSlot, loc);

            persist(blackInformation, player);

            player.closeInventory();
            player.sendMessage(dess("<green>✔ Location saved in slot " + (rawSlot + 1) + " — <gray>("
                    + Math.round(loc.getX()) + ", " + Math.round(loc.getY()) + ", " + Math.round(loc.getZ())
                    + ") in " + loc.getWorld().getName()));
            player.sendActionBar(dess("<green><b>✔ Location saved in slot " + (rawSlot + 1)));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f, 1.2f);

        } else {
            // ── Teleport to the saved location in this slot ───────────────────────
            if (rawSlot >= blackInformation.locations.size()) return;

            final Location destination = blackInformation.locations.get(rawSlot);
            if (destination == null) {
                player.sendMessage(dess("<red>✗ No location saved in slot " + (rawSlot + 1) + "."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.0f);
                return;
            }

            player.closeInventory();

            final int slot = rawSlot;
            String destStr = "<gray>(" + Math.round(destination.getX()) + ", "
                    + Math.round(destination.getY()) + ", "
                    + Math.round(destination.getZ()) + ") in " + destination.getWorld().getName();

            player.sendMessage(dess("<yellow>⌛ Teleporting to slot " + (slot + 1) + " — " + destStr
                    + " <yellow>in 5 seconds. <red>Stand still!"));
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.6f, 1.0f);

            new BukkitRunnable() {
                final Location startLocation = player.getLocation().clone();
                final int time = 5 * 20;
                int tick = 0;

                @Override
                public void run() {
                    if (player.isDead() || !player.isOnline()) {
                        sendCancelFeedback("You died.");
                        this.cancel();
                        return;
                    }

                    if (player.getLocation().distanceSquared(startLocation) >= 4) {
                        sendCancelFeedback("You moved.");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 0.5f);
                        this.cancel();
                        return;
                    }

                    tick++;

                    if (tick >= time) {
                        persist(blackInformation, player);
                        player.teleport(destination);
                        player.sendMessage(dess("<green>✔ Teleported to slot " + (slot + 1) + " — " + destStr));
                        player.sendActionBar(dess("<green><b>✔ Teleported!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                        drawCircle(destination, 1, destination.getWorld(), 32, Particle.REVERSE_PORTAL);
                        this.cancel();
                        return;
                    }

                    int secondsLeft = (time - tick) / 20 + 1;
                    String dots = ".".repeat((int) threes(tick));
                    player.sendActionBar(dess("<yellow>⌛ Teleporting in <b>" + secondsLeft + "s</b>" + dots));

                    if (tick % 20 == 0) {
                        int secondsElapsed = tick / 20;
                        float pitch = 0.5f + (secondsElapsed * 0.15f);
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, pitch);
                        player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
                        int rand = random.nextInt(0, 4);
                        switch (rand) {
                            case 0 -> drawSpiral(player.getLocation(), 1, 4, player.getWorld(), 32, Particle.DUST, new Particle.DustOptions(DyeColor.BLACK.getColor(), 4f));
                            case 1 -> drawStar(player.getLocation(), 1, player.getWorld(), 32, Particle.DUST, new Particle.DustOptions(DyeColor.BLACK.getColor(), 4f));
                            case 2 -> drawCircle(player.getLocation(), 1, player.getWorld(), 32, Particle.DUST, new Particle.DustOptions(DyeColor.BLACK.getColor(), 4f));
                            default -> drawSquare(player.getLocation(), 1, 32, Particle.DUST, new Particle.DustOptions(DyeColor.BLACK.getColor(), 4f), 0, 0, 0);
                        }
                    }
                }

                private void sendCancelFeedback(String reason) {
                    player.sendMessage(dess("<red>✗ Teleport cancelled — <gray>" + reason));
                    player.sendActionBar(dess("<red><b>✗ Cancelled"));
                }
            }.runTaskTimer(Freedom.get_plugin(), 0, 1);
        }
    }

    // ─── Persist helper ───────────────────────────────────────────────────────────

    /** Writes BlackInformation to PDC and keeps the in-memory cache in sync. */
    private void persist(BlackInformation info, Player player) {
        player.getPersistentDataContainer().set(blackInformationKey, PersistentDataType.STRING, info.toString());
        BlackInformation.people.put(player.getUniqueId(), info);
    }

    // ─── Inventory close handler ──────────────────────────────────────────────────

    @EventHandler
    public void onBlackInformationClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlackInformation.BlackMenu)) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        BlackInformation blackInformation = BlackInformation.people.get(player.getUniqueId());
        if (blackInformation == null) return; // nothing to save; leave PDC untouched

        player.getPersistentDataContainer().set(blackInformationKey, PersistentDataType.STRING, blackInformation.toString());
    }

    // ─── BlackInformation data class ──────────────────────────────────────────────

    public static class BlackInformation {
        public static HashMap<UUID, BlackInformation> people = new HashMap<>();

        public ArrayList<ItemStack> items    = new ArrayList<>();
        public ArrayList<Location>  locations = new ArrayList<>();
        boolean isFresh = false;

        // Must match blackInformationKey in the outer class.
        NamespacedKey self = keygen("blackinformation");

        public BlackInformation(Player player) {
            String construct = player.getPersistentDataContainer().getOrDefault(self, PersistentDataType.STRING, "");
            if (!construct.isEmpty()) {
                BlackInformation info = fromString(construct);
                if (info != null) {
                    items     = info.items;
                    locations = info.locations;
                    isFresh   = false;
                    return;
                }
            }
            isFresh = true;
        }

        public BlackInformation() {
            isFresh = true;
        }

        public BlackInformation fromString(String string) {
            BlackInformation output = new BlackInformation();

            // Format: <locations>|<items>
            //   locations : 9 entries separated by ';', each "worldName,x,y,z" or empty
            //   items     : 9 entries separated by ';;' — safe because itemToString()
            //               now produces Base64 which never contains ';'.
            String[] parts = string.split("\\|", 2);
            if (parts.length != 2) return null;

            // --- locations ---
            String[] locEntries = parts[0].split(";", -1);
            for (int i = 0; i < 9; i++) {
                if (i >= locEntries.length) { output.locations.add(null); continue; }
                String e = locEntries[i];
                if (e == null || e.isEmpty()) { output.locations.add(null); continue; }
                int c1 = e.indexOf(',');
                int c2 = c1 < 0 ? -1 : e.indexOf(',', c1 + 1);
                int c3 = c2 < 0 ? -1 : e.indexOf(',', c2 + 1);
                if (c1 < 0 || c2 < 0 || c3 < 0) { output.locations.add(null); continue; }
                try {
                    String worldName = e.substring(0, c1);
                    double x = Double.parseDouble(e.substring(c1 + 1, c2));
                    double y = Double.parseDouble(e.substring(c2 + 1, c3));
                    double z = Double.parseDouble(e.substring(c3 + 1));
                    World w = Bukkit.getWorld(worldName);
                    output.locations.add(w != null ? new Location(w, x, y, z) : null);
                } catch (NumberFormatException ex) {
                    output.locations.add(null);
                }
            }

            // --- items ---
            String[] itemEntries = parts[1].split(";;", -1);
            for (int i = 0; i < 9; i++) {
                if (i >= itemEntries.length) { output.items.add(null); continue; }
                String e = itemEntries[i];
                if (e == null || e.isEmpty()) { output.items.add(null); continue; }
                output.items.add(stringToItem(e));
            }

            return output;
        }

        public String toString() {
            StringBuilder locBuilder = new StringBuilder();
            for (int i = 0; i < 9; i++) {
                if (i > 0) locBuilder.append(';');
                Location loc = (i < locations.size()) ? locations.get(i) : null;
                if (loc != null && loc.getWorld() != null) {
                    locBuilder.append(loc.getWorld().getName()).append(',')
                            .append(loc.getX()).append(',')
                            .append(loc.getY()).append(',')
                            .append(loc.getZ());
                }
            }

            StringBuilder itemBuilder = new StringBuilder();
            for (int i = 0; i < 9; i++) {
                if (i > 0) itemBuilder.append(";;");
                ItemStack it = (i < items.size()) ? items.get(i) : null;
                if (it != null) itemBuilder.append(itemToString(it));
            }

            return locBuilder.toString() + "|" + itemBuilder.toString();
        }

        // ─── BlackMenu ────────────────────────────────────────────────────────────

        public static class BlackMenu implements InventoryHolder {
            Inventory inventory;

            /**
             * Builds an 18-slot (2-row) inventory:
             *   Row 1 (slots 0–8)  : saved location items, or black glass-pane placeholders.
             *   Row 2 (slots 9–17) : red glass-pane delete buttons, one per slot above.
             *
             * Each delete button has the `deleteSlotKey` PDC tag set to its target slot (0–8)
             * so the click handler can identify and clear the correct entry without any
             * fragile slot-number arithmetic.
             */
            public void constructMenu(BlackInformation information, NamespacedKey deleteSlotKey) {
                inventory = Bukkit.createInventory(this, 18, dess("<dark_gray>☽ Black Locations"));

                for (int i = 0; i < 9; i++) {
                    // ── Row 1: location slots ─────────────────────────────────────────────
                    ItemStack locationItem;
                    Location loc = (i < information.locations.size()) ? information.locations.get(i) : null;
                    ItemStack savedItem  = (i < information.items.size())    ? information.items.get(i)    : null;

                    if (loc != null && savedItem != null) {
                        // Clone the stored item so we never mutate the live BlackInformation.items
                        // reference. Without this, setItemMeta() would write the display lore back
                        // into the stored object, which then gets serialized with the extra lore
                        // permanently attached — corrupting every subsequent save/load cycle.
                        locationItem = savedItem.clone();
                        ItemMeta meta = locationItem.getItemMeta();
                        String coordLine = "<gray>(" + Math.round(loc.getX()) + ", "
                                + Math.round(loc.getY()) + ", "
                                + Math.round(loc.getZ()) + ") in " + loc.getWorld().getName();
                        // Start fresh lore — do NOT copy meta.lore() from the stored item, because
                        // that would accumulate our injected lines on every menu open.
                        List<Component> lore = new ArrayList<>();
                        lore.add(dess("<dark_gray>Slot " + (i + 1)));
                        lore.add(dess(coordLine));
                        lore.add(dess("<yellow>Click to teleport here"));
                        meta.lore(lore);
                        locationItem.setItemMeta(meta);
                    } else {
                        // Empty slot placeholder — stamp a PDC marker so the click handler
                        // can identify these items reliably regardless of amount or display name.
                        locationItem = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE, i + 1);
                        ItemMeta meta = locationItem.getItemMeta();
                        meta.displayName(dess("<gray>Slot " + (i + 1) + " — Empty"));
                        meta.lore(List.of(dess("<dark_gray>Click with an item in hand to save your location here.")));
                        meta.getPersistentDataContainer().set(keygen("black_placeholder"), PersistentDataType.BOOLEAN, true);
                        locationItem.setItemMeta(meta);
                    }
                    inventory.setItem(i, locationItem);

                    // ── Row 2: delete buttons ─────────────────────────────────────────────
                    boolean occupied = loc != null;
                    ItemStack deleteButton = ItemStack.of(
                            occupied ? Material.RED_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE, 1);
                    ItemMeta deleteMeta = deleteButton.getItemMeta();
                    deleteMeta.displayName(occupied
                            ? dess("<red>🗑 Delete slot " + (i + 1))
                            : dess("<dark_gray>🗑 Slot " + (i + 1) + " — Empty"));
                    if (occupied) {
                        deleteMeta.lore(List.of(dess("<gray>Click to clear this saved location.")));
                    }
                    // Tag with target slot so the click handler knows which slot to clear.
                    deleteMeta.getPersistentDataContainer().set(deleteSlotKey, PersistentDataType.INTEGER, i);
                    deleteButton.setItemMeta(deleteMeta);
                    inventory.setItem(9 + i, deleteButton);
                }
            }

            @Override
            public @NotNull Inventory getInventory() {
                return inventory;
            }
        }
    }
}


