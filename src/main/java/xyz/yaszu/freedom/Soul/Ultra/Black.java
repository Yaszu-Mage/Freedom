package xyz.yaszu.freedom.Soul.Ultra;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Subsystems.CurseManager;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Black extends Util implements Base_Soul, Listener {

    @Override
    public String Name_For_Container() {
        return "Black";
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
        if (!player.getPersistentDataContainer().has(keygen("black_save"), PersistentDataType.BOOLEAN)) {
            save(player);
            return;
        }
        if (player.getPersistentDataContainer().get(keygen("black_save"), PersistentDataType.BOOLEAN)) {
            load(player);
        }
    }

    public void load(Player player) {
        //VFX
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
                drawCircle(player.getLocation(),1,player.getWorld(),16,Particle.SMOKE);
                drawCircle(loadingLocation,1,player.getWorld(),16,Particle.SMOKE);
            }
        }.runTaskTimer(Freedom.get_plugin(),20,20);
        new BukkitRunnable() {
            public int tick = 0;
            public double last_health = player.getHealth();
            Location loadingLocation = player.getLocation();
            @Override
            public void run() {
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
        if (can_ability(AbilityTwo_Cooldown(),abilityTwoCooldowns,player.getUniqueId()) && !player.getPersistentDataContainer().has(keygen("disguised"), PersistentDataType.BOOLEAN)) {
            player.playSound(player.getLocation(),Sound.BLOCK_DISPENSER_DISPENSE,1,1);
            InventoryGui inventoryGui = new InventoryGui();
            inventoryGui.setInventory(player);
            player.openInventory(inventoryGui.getInventory());

        } else {
            // no no ability
            if (abilityTwoCooldowns.get(player.getUniqueId()) != null) {
                double seconds = (double) (effective_cooldown(AbilityTwo_Cooldown(), player.getUniqueId()) - (System.currentTimeMillis() - abilityTwoCooldowns.get(player.getUniqueId()))) / 1000;
                player.sendActionBar(dess("You can't use this ability yet, wait " + seconds + " seconds"));
            }
        }
        if (player.getPersistentDataContainer().has(keygen("disguised"), PersistentDataType.BOOLEAN)) {
            if (originalProfiles.get(player.getUniqueId()) != null) {
                DisguiseAPI.undisguiseToAll(player);

                PlayerDisguise disguise =originalProfiles.get(player.getUniqueId());
                player.getWorld();
                World world = player.getWorld();
                Location location = player.getLocation();
                world.spawnParticle(Particle.SMOKE, location,128);
                world.playSound(location,Sound.ENTITY_WARDEN_HEARTBEAT,1,1);
                disguise.removePlayer(player);
                disguise.stopDisguise();
                disguise.removeDisguise();


                try {
                    join(player);
                } catch (MineSkinException | DataRequestException e) {
                    throw new RuntimeException(e);
                }
                if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) == null) {
                    player.getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(keygen("black"),-0.20, AttributeModifier.Operation.ADD_NUMBER));
                }
                setSkinByName(player,player.getName());
                originalProfiles.remove(player.getUniqueId());
            }
            player.playerListName(player.name());
            player.displayName(player.name());
            player.customName(null);
            player.setCustomNameVisible(false);
            player.customName(player.name());
            player.getPersistentDataContainer().remove(keygen("disguiseid"));
            player.getPersistentDataContainer().remove(keygen("disguised"));
            player.sendActionBar(dess("Disguise Removed."));
        }
    }
    public static Random random = new Random();
    public static HashMap<UUID,PlayerDisguise> originalProfiles = new HashMap<>();
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
            
            // Comprehensive Impersonation: Mimic Equipment
            PlayerWatcher watcher = disguise.getWatcher();
            watcher.setArmor(baller.getInventory().getArmorContents());
            watcher.setItemInMainHand(baller.getInventory().getItemInMainHand());
            watcher.setItemInOffHand(baller.getInventory().getItemInOffHand());
            
            player.getPersistentDataContainer().set(keygen("disguised"),PersistentDataType.BOOLEAN,true);
            disguise.startDisguise();
            if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) != null) {
                player.getAttribute(Attribute.SCALE).removeModifier(keygen("black"));
            }
            player.playerListName(baller.name());
            player.displayName(baller.displayName());
            player.customName(Component.text(baller.getName()));
            player.setCustomNameVisible(true);
            setSkinByName(player,baller.getName());
            originalProfiles.put(player.getUniqueId(),disguise);

            World world = player.getWorld();
            Location location = player.getLocation();
            //VFX
            int disguiseid = random.nextInt();
            player.getPersistentDataContainer().set(keygen("disguiseid"),PersistentDataType.INTEGER,disguiseid);
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
                    player.displayName(player.name());
                    player.customName(null);
                    player.setCustomNameVisible(false);
                    try {
                        join(player);
                    } catch (MineSkinException | DataRequestException e) {
                        throw new RuntimeException(e);
                    }
                    if (player.getPersistentDataContainer().has(keygen("disguiseid"),PersistentDataType.INTEGER)) {
                        if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) == null) {
                            player.getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(keygen("black"),0.80, AttributeModifier.Operation.ADD_NUMBER));
                        }
                        int gotid = player.getPersistentDataContainer().get(keygen("disguiseid"), PersistentDataType.INTEGER);
                        player.playerListName(player.name());
                        player.displayName(player.name());
                        player.customName(null);
                        player.setCustomNameVisible(false);
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
            }.runTaskLater(Freedom.get_plugin(),6000);
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // 1. Handle victim name in death message
        if (player.getPersistentDataContainer().has(keygen("disguised"), PersistentDataType.BOOLEAN)) {
            PlayerDisguise disguise = originalProfiles.get(player.getUniqueId());
            if (disguise != null) {
                String targetName = disguise.getName();
                Component deathMessage = event.deathMessage();
                if (deathMessage != null) {
                    // Impersonate victim name in death message
                    String originalName = player.getName();
                    event.deathMessage(deathMessage.replaceText(builder -> builder.matchLiteral(originalName).replacement(targetName)));
                }
            }
        }
        
        // 2. Handle killer name in death message
        Player killer = player.getKiller();
        if (killer != null && killer.getPersistentDataContainer().has(keygen("disguised"), PersistentDataType.BOOLEAN)) {
            PlayerDisguise killerDisguise = originalProfiles.get(killer.getUniqueId());
            if (killerDisguise != null) {
                String killerTargetName = killerDisguise.getName();
                Component deathMessage = event.deathMessage();
                if (deathMessage != null) {
                    // Impersonate killer name in death message
                    String originalKillerName = killer.getName();
                    event.deathMessage(deathMessage.replaceText(builder -> builder.matchLiteral(originalKillerName).replacement(killerTargetName)));
                }
            }
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
            if (remainder == 0) {
                inventory = Bukkit.createInventory(this,max_players);
            } else {
                inventory = Bukkit.createInventory(this,max_players + (9 - remainder));
            }
            int iteration = 0;
            for (Player instancedPlayer : Bukkit.getOnlinePlayers()) {
                if (instancedPlayer.getUniqueId() != player.getUniqueId()){
                    ItemStack skull = getSkull(instancedPlayer);
                    SkullMeta meta = (SkullMeta) skull.getItemMeta();
                    meta.getPersistentDataContainer().set(keygen("player_uuid"), PersistentDataType.STRING, instancedPlayer.getUniqueId().toString());
                    
                    String soulString = instancedPlayer.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING);
                    SoulTypes soulType = (soulString != null) ? SoulTypes.valueOf(soulString) : SoulTypes.None;
                    
                    String name = instancedPlayer.getName();
                    meta.displayName(soulListener.SOULS.get(soulType).Name().append(dess(" " + name)));
                    
                    // Add Lore for better selection
                    List<Component> lore = new ArrayList<>();
                    boolean isAlive = Life_and_Death.is_alive(instancedPlayer);
                    lore.add(dess(isAlive ? "<green>Status: ALIVE" : "<gray>Status: GHOST"));
                    lore.add(dess("<aqua>Soul: " + soulType.name()));
                    meta.lore(lore);
                    
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
            // Reset to original name before applying soul-specific prefix
            player.displayName(player.name());
            player.customName(player.name());
            
            for (PotionEffect potion : player.getActivePotionEffects()) {
                if (potion.getDuration() == PotionEffect.INFINITE_DURATION) {
                    player.removePotionEffect(potion.getType());
                }
            }

        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        if (soulType == SoulTypes.Black) {
            if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) == null) {
                player.getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(keygen("black"),-0.20, AttributeModifier.Operation.ADD_NUMBER));
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
            if (soulType == SoulTypes.None) {
                player.addPotionEffect(PotionEffectType.STRENGTH.createEffect(PotionEffect.INFINITE_DURATION,1));
                player.addPotionEffect(PotionEffectType.SPEED.createEffect(PotionEffect.INFINITE_DURATION,1));
                player.addPotionEffect(PotionEffectType.HEALTH_BOOST.createEffect(PotionEffect.INFINITE_DURATION,1));
            }
            switch (soulType) {
                case BaseRed,Red -> {
                    player.displayName(Component.text("", NamedTextColor.RED).append(player.name()));
                }
                case BaseGreen,Green -> {
                    player.displayName(Component.text("", NamedTextColor.GREEN).append(player.name()));
                }
                case BaseMocha, Mocha -> {
                    player.displayName(Component.text("", NamedTextColor.BLUE).append(player.name()));
                }
                case BasePurple,Purple -> {
                    player.displayName(Component.text("", NamedTextColor.DARK_PURPLE).append(player.name()));
                }
                case Black,BaseBlack -> {
                    player.displayName(Component.text("", NamedTextColor.WHITE).append(player.name()));
                }
                case Orange,BaseOrange -> {
                    player.displayName(Component.text("", TextColor.color(0xff6f00)).append(player.name()));
                }
                case Cafe, BaseCafe -> {
                    player.displayName(Component.text("", NamedTextColor.GOLD).append(player.name()));
                }
                case None,BaseNone -> {
                    player.displayName(Component.text("", TextColor.color(0x555555)).append(player.name()));
                }
            }
    }
    }


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
        join(player);
        if (player.getPersistentDataContainer().has(keygen("soul"))) {
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        if (soulType == SoulTypes.Black) {
            if (player.getAttribute(Attribute.SCALE).getModifier(keygen("black")) == null) {
                player.getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(keygen("black"),-0.20, AttributeModifier.Operation.ADD_NUMBER));
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
        SoulTypes soulType = SoulTypes.valueOf(player.getPersistentDataContainer().get(keygen("soul"), PersistentDataType.STRING));
        double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        player.getPersistentDataContainer().set(keygen("SoulPoint"),PersistentDataType.DOUBLE,SoulPoints - 5);
        if (soulType == SoulTypes.Black) {
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
    public long AbilityOne_Cooldown() {
        return 0;
    }

    @Override
    public void ActivePassive(Player player) {

    }
}
