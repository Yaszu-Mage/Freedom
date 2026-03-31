package xyz.yaszu.freedom.Soul.Base;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.CatWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FrogWatcher;
import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Subsystems.CombatTimer;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Subsystems.TabDistance;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;

public class BaseOrange extends Util implements Base_Soul, Listener {
    @Override
    public String Name_For_Container() {
        return "BaseOrange";
    }

    @Override
    public Component Name() {
        return dess("Orange");
    }

    @Override
    public Component Description() {
        return dess("<color:#ff7300>What is it but magic?</color>");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.CAULDRON);
    }

    @Override
    public Component AbilityOneName() {
        return dess("<color:#ff7300> Ability One </color> Pot for All");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("Increase the amplifier of all potions of yourself and trusted team mates in a 2 block radius");
    }
    public long AbilityOne_Cooldown = 60000;

    public static HashMap<UUID,Long> abilityOneCooldownTime = new HashMap<>();


    @Override
    public void AbilityOne(Player player) {
        if (can_ability(AbilityOne_Cooldown,abilityOneCooldownTime,player.getUniqueId())) {
            Collection<PotionEffect> pots = player.getActivePotionEffects();
            player.clearActivePotionEffects();
            for (PotionEffect pot : pots) {
                player.addPotionEffect(new PotionEffect(pot.getType(),pot.getDuration(),pot.getAmplifier() + 1,pot.isAmbient(),pot.hasParticles()));
            }
            List<Entity> entities = player.getNearbyEntities(2,2,2);
            for (Entity entity : entities) {
                if (entity instanceof Player playeriterated) {
                    if (player.getPersistentDataContainer().has(keygen("trustedby"))) {
                        if (player.getPersistentDataContainer().get(keygen("trustedby"),PersistentDataType.STRING).contains(playeriterated.getName())) {
                            Collection<PotionEffect> iterpots = playeriterated.getActivePotionEffects();
                            playeriterated.clearActivePotionEffects();
                            for (PotionEffect pot : iterpots) {
                                playeriterated.addPotionEffect(new PotionEffect(pot.getType(),pot.getDuration(),pot.getAmplifier() + 1,pot.isAmbient(),pot.hasParticles()));
                            }
                        }
                    }
                }
            }
            abilityOneCooldownTime.put(player.getUniqueId(),System.currentTimeMillis());
        } else {
            double seconds = (double) (AbilityOne_Cooldown - (System.currentTimeMillis() - abilityOneCooldownTime.get(player.getUniqueId()))) / 1000;
            player.sendActionBar(dess("<Red>Cooldown!</Red> Please wait " + seconds + " seconds!"));
        }
    }



    @EventHandler

    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.WHEAT);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("<color:#ff7300> Ability Two </color> Curse");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("Curse one person to become a frog. they cannot speak and they have weakness 2");
    }

    public long AbilityTwo_Cooldown = 30000;

    public static HashMap<UUID,Long> abilityTwoCooldownTime = new HashMap<>();

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        if (can_ability(AbilityTwo_Cooldown,abilityTwoCooldownTime,player.getUniqueId()) && !player.getPersistentDataContainer().has(keygen("disguised"), PersistentDataType.BOOLEAN)) {
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE,1,1);
            InventoryGui inventoryGui = new InventoryGui();
            inventoryGui.setInventory(player);
            player.openInventory(inventoryGui.getInventory());

        } else {
            // no no ability
            if (abilityTwoCooldownTime.get(player.getUniqueId()) != null) {
                double seconds = (double) (AbilityTwo_Cooldown - (System.currentTimeMillis() - abilityTwoCooldownTime.get(player.getUniqueId()))) / 1000;
                player.sendActionBar(dess("You can't use this ability yet, wait " + seconds + " seconds"));
            }
        }
    }

    public static HashMap<UUID,MobDisguise> curses = new HashMap<>();


    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (Bukkit.getWorld("world").getPersistentDataContainer().has(keygen("recursor"))) {
            Player recursor = Bukkit.getPlayer(Bukkit.getWorld("world").getPersistentDataContainer().get(keygen("recursor"),PersistentDataType.STRING));
            if (recursor != null) {
                String recursorName = recursor.getName();
                String worlddata = Bukkit.getWorld("world").getPersistentDataContainer().get(keygen("recursor"),PersistentDataType.STRING);
                String fixedworlddata = worlddata.replace(recursorName, "");
                recursor.getPersistentDataContainer().remove(keygen("cancurse"));
                Bukkit.getWorld("world").getPersistentDataContainer().set(keygen("recursor"),PersistentDataType.STRING,fixedworlddata);
            }
        }
        if (Bukkit.getWorld("world").getPersistentDataContainer().has(keygen("uncursor"))) {
            Player recursor = Bukkit.getPlayer(Bukkit.getWorld("world").getPersistentDataContainer().get(keygen("uncursor"),PersistentDataType.STRING));
            if (recursor != null) {
                String recursorName = recursor.getName();
                String worlddata = Bukkit.getWorld("world").getPersistentDataContainer().get(keygen("uncursor"),PersistentDataType.STRING);
                String fixedworlddata = worlddata.replace(recursorName, "");
                Bukkit.getWorld("world").getPersistentDataContainer().set(keygen("uncursor"),PersistentDataType.STRING,fixedworlddata);
                uncurse(recursor);
            }
        }
    }

    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        EntityDamageEvent lastDamage = victim.getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent damageByEntity) {
            Entity damager = damageByEntity.getDamager();

            if (damager instanceof Player killer) {
                Player player = event.getPlayer();
                if (player.getPersistentDataContainer().has(keygen("cursed"))) {
                    if (player.getPersistentDataContainer().get(keygen("cursed"),PersistentDataType.STRING) == "Frog") {
                        uncurse(player);
                    }
                }
            }
        } else {
            Player player = event.getPlayer();
            if (player.getPersistentDataContainer().has(keygen("cursed"))) {
                uncurse(event.getPlayer());
            }
        }

    }

    @EventHandler
    public void InventoryClickEvent (InventoryClickEvent event) throws MineSkinException, DataRequestException {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof InventoryGui inventoryGui && event.getCurrentItem() != null) {
            // yayas correct holder
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();
            Player baller = Bukkit.getPlayer(UUID.fromString(item.getPersistentDataContainer().get(keygen("player_uuid"),PersistentDataType.STRING)));
            String curser = player.getName();
            if (baller != null) {
            if (!baller.getPersistentDataContainer().has(keygen("cursed"))) {
                if (!player.getPersistentDataContainer().has(keygen("cancurse"))) {
                    player.getPersistentDataContainer().set(keygen("cancurse"),PersistentDataType.BOOLEAN,true);
                }
                if (player.getPersistentDataContainer().get(keygen("cancurse"),PersistentDataType.BOOLEAN) == true) {
                    Freedom.get_plugin().getLogger().info("Standard Curse");
                    player.getPersistentDataContainer().set(keygen("cancurse"),PersistentDataType.BOOLEAN,false);

                    curse(baller,curser);
                }
            } else {
                if (baller.getPersistentDataContainer().get(keygen("cursed"),PersistentDataType.STRING) == "Frog") {
                    Freedom.get_plugin().getLogger().info("UNCURSE FROG");
                    player.getPersistentDataContainer().set(keygen("cancurse"),PersistentDataType.BOOLEAN,true);
                    uncurse(baller);
                } else {
                    if (player.getPersistentDataContainer().get(keygen("cancurse"),PersistentDataType.BOOLEAN) == true) {
                        Freedom.get_plugin().getLogger().info("Can curse Cat");
                        player.getPersistentDataContainer().set(keygen("cancurse"),PersistentDataType.BOOLEAN,false);
                        curse(baller,curser);
                    }

                }
            }
                Freedom.get_plugin().getLogger().info("huh");
            abilityTwoCooldownTime.put(player.getUniqueId(),System.currentTimeMillis());
            player.closeInventory();
            event.setCancelled(true);

        }
    }
    }


    public void uncurse(Player baller) {
        if (baller.isOnline()) {


            baller.getPersistentDataContainer().remove(keygen("cursed"));
            baller.removePotionEffect(PotionEffectType.WEAKNESS);
            String curser = baller.getPersistentDataContainer().get(keygen("cursedby"), PersistentDataType.STRING);
            if (curser != null) {
            if (Bukkit.getPlayer(curser) != null) {
                Bukkit.getPlayer(curser).getPersistentDataContainer().remove(keygen("cancurse"));
            }
            if (Bukkit.getWorld("world").getPersistentDataContainer().has(keygen("recursor"))) {
                Bukkit.getWorld("world").getPersistentDataContainer().set(keygen("recursor"), PersistentDataType.STRING, curser + Bukkit.getWorld("world").getPersistentDataContainer().get(keygen("recursor"), PersistentDataType.STRING));
            } else {
                Bukkit.getWorld("world").getPersistentDataContainer().set(keygen("recursor"), PersistentDataType.STRING, curser);
            }
            curses.get(baller.getUniqueId()).removeDisguise();
            curses.remove(baller.getUniqueId());
            baller.getPersistentDataContainer().remove(keygen("cursedby"));
        } else {
            if (Bukkit.getWorld("world").getPersistentDataContainer().has(keygen("uncursor"))) {
                Bukkit.getWorld("world").getPersistentDataContainer().set(keygen("uncursor"),PersistentDataType.STRING,Bukkit.getWorld("world").getPersistentDataContainer().get(keygen("uncursor"),PersistentDataType.STRING)+baller.getName());
            } else {
                Bukkit.getWorld("world").getPersistentDataContainer().set(keygen("uncursor"),PersistentDataType.STRING,baller.getName());
            }

        }
    }
    }

    public void curse(Player baller,String curser) {
        baller.getPersistentDataContainer().set(keygen("cursed"), PersistentDataType.STRING,"Frog");
        baller.getPersistentDataContainer().set(keygen("cursedby"),PersistentDataType.STRING,curser);
        baller.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,PotionEffect.INFINITE_DURATION,1,true,false));
        if (curses.get(baller.getUniqueId()) == null) {
            MobDisguise mobDisguise = new MobDisguise(DisguiseType.FROG);
            mobDisguise.addPlayer(baller);
            mobDisguise.setEntity(baller);
            mobDisguise.startDisguise();
            FrogWatcher watcher = (FrogWatcher) mobDisguise.getWatcher();
            watcher.setVariant(Frog.Variant.COLD);
            curses.put(baller.getUniqueId(),mobDisguise);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                uncurse(baller);
            }
        }.runTaskLater(Freedom.get_plugin(),2620);
    }


    @Override
    public Component Passive_Description() {
        return dess("Kitty kitty Kitty! (You can become a cat)");
    }


    public static HashMap<UUID, MobDisguise> disguiseCat = new HashMap<>();
    public static enum Cursetype {
        Cat,
        Frog
    }
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
                    switch (soulType) {
                        case Black:
                            meta.displayName(soulListener.black.Name().append(dess(" " + name)));
                            break;
                        case Green:
                            meta.displayName(soulListener.green.Name().append(dess(" " + name)));
                            break;
                        case Red:
                            meta.displayName(soulListener.red.Name().append(dess(" " + name)));
                            break;
                        case Blue:
                            meta.displayName(soulListener.blue.Name().append(dess(" " + name)));
                            break;
                        case Purple:
                            meta.displayName(soulListener.purple.Name().append(dess(" " + name)));
                            break;

                    }
                    skull.displayName();
                    skull.setItemMeta(meta);
                    inventory.setItem(iteration,skull);
                    iteration++;
                }
            }
        }

    }
    @Override
    public void Passive(Player player, Object event) {
        if (player.getPersistentDataContainer().has(keygen("cursed"))) {
            if (player.getPersistentDataContainer().get(keygen("cursed"),PersistentDataType.STRING) == "Frog") {
                player.sendMessage(dess("You cannot uncurse yourself!"));
                return;
            }
        }
        if (disguiseCat.containsKey(player.getUniqueId())) {
            player.sendMessage(dess("You are not a cat"));
            MobDisguise disguise = disguiseCat.get(player.getUniqueId());
            disguise.removeDisguise();
            disguiseCat.remove(player.getUniqueId());
            player.getPersistentDataContainer().remove(keygen("cursed"));
        } else {
            player.sendMessage(dess("You are now a cat"));
            MobDisguise disguise = new MobDisguise(DisguiseType.CAT);
            disguise.addPlayer(player);
            disguise.setEntity(player);
            disguise.startDisguise();
            CatWatcher watcher = (CatWatcher) disguise.getWatcher();
            watcher.setType(Cat.Type.ALL_BLACK);
            player.getWorld().spawnParticle(Particle.ASH,player.getLocation(),16);
            player.getPersistentDataContainer().set(keygen("cursed"),PersistentDataType.STRING,"Cat");
            disguiseCat.put(player.getUniqueId(),disguise);
        }

    }






    @Override
    public Component ActivePassive_Description() {
        return dess("You can ride a broom in exchange for soulpoints");
    }

    @Override
    public void ActivePassive(Player player) {
        // witch broom flight
        double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        if (player.getPersistentDataContainer().has(keygen("combattimer"))) {
            int seconds = (int) (CombatTimer.combatTime - (System.currentTimeMillis() - CombatTimer.combatTimer.get(player.getUniqueId()))) / 1000;
            player.sendMessage(dess("<Red>ERROR</Red> cannot deploy broom while in combat! Wait " + seconds + " seconds!"));
        }
        if (SoulPoints > 1 && !player.getPersistentDataContainer().has(keygen("combattimer"))) {


        if (!player.isInsideVehicle()) {
            player.getPersistentDataContainer().set(keygen("ridemode"),PersistentDataType.INTEGER,1);
            Entity broom = player.getWorld().spawnEntity(player.getLocation(),EntityType.BAT);
            //disable broom entity ai
            broom.addPassenger(player);
            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE,SoulPoints - 1);
            soulListener.showSoulPoints(player);
            ItemDisplay display = player.getWorld().spawn(player.getLocation(), ItemDisplay.class);
            ItemStack boom = ItemStack.of(Material.BRUSH);
            ItemMeta meta = boom.getItemMeta();
            meta.setItemModel(NamespacedKey.minecraft("broom"));
            boom.setItemMeta(meta);
            display.getPersistentDataContainer().set(keygen("sprite"),PersistentDataType.INTEGER,Freedom.version);
            broom.getPersistentDataContainer().set(keygen("sprite"),PersistentDataType.INTEGER,Freedom.version);
            display.setItemStack(boom);
            broom.addPassenger(display);
            LivingEntity live = (LivingEntity) broom;
            live.setSilent(true);
            live.setInvulnerable(true);
            live.setInvisible(true);

            new BukkitRunnable() {
                int tick = 0;
                @Override
                public void run() {
                    if (player.getPersistentDataContainer().has(keygen("combattimer"))) {
                        player.sendMessage(dess("<Red>ERROR</Red> cannot deploy broom while in combat!"));
                        cancel();
                    }
                    double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
                    if (!Life_and_Death.is_alive(player)) return;
                    ArrayList<Player> players = new ArrayList<>();
                    for (Player instancedplayer : player.getLocation().getNearbyEntitiesByType(Player.class, TabDistance.tabradius) ) {
                        if (Life_and_Death.is_alive(instancedplayer)) {
                            players.add(instancedplayer);
                        }
                    }
                    for (Player instancedPlayer : Bukkit.getOnlinePlayers()) {
                        if (players.contains(instancedPlayer)) {
                            player.showPlayer(Freedom.get_plugin(),instancedPlayer);
                        } else {
                            player.hidePlayer(Freedom.get_plugin(),instancedPlayer);
                        }
                    }
                    if (broom.isDead() || !player.isOnline() || !player.isInsideVehicle() || SoulPoints < 1) {

                        cancel();
                    } else {
                        if (player.getPersistentDataContainer().has(keygen("ridemode"),PersistentDataType.INTEGER)) {
                            int ridemode = player.getPersistentDataContainer().get(keygen("ridemode"),PersistentDataType.INTEGER);
                            switch (ridemode) {
                                case 1:
                                    //stationary
                                    broom.teleport(broom.getLocation());
                                    display.teleport(player.getLocation().setDirection(player.getLocation().getDirection().multiply(-1)).add(new Vector(0,0.5,0)));
                                    live.setAI(false);
                                    break;
                                case 2:
                                    //moving
                                    broom.setVelocity(player.getLocation().getDirection().multiply(0.75));
                                    display.teleportAsync(player.getLocation().setDirection(player.getLocation().getDirection().multiply(-1)).add(new Vector(0,0.5,0)).add(player.getLocation().getDirection().multiply(-1)));
                                    live.setAI(true);
                                    break;
                            }
                        }

                        if (tick >= 300) {
                            tick = 0;
                            player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE,SoulPoints - 1);
                            soulListener.showSoulPoints(player);
                        }
                        tick++;
                    }
                }
                @Override
                public void cancel() {
                    broom.remove();
                    display.remove();
                    player.getPersistentDataContainer().set(keygen("ridemode"),PersistentDataType.INTEGER,0);
                    super.cancel();
                }
            }.runTaskTimer(Freedom.get_plugin(),0,0);
        } else {
            if (player.getPersistentDataContainer().has(keygen("ridemode"),PersistentDataType.INTEGER)) {
                int ridemode = player.getPersistentDataContainer().get(keygen("ridemode"),PersistentDataType.INTEGER);
                switch (ridemode) {
                    case 1:
                        //stationary
                        ridemode = 2;
                        player.getPersistentDataContainer().set(keygen("ridemode"),PersistentDataType.INTEGER,ridemode);
                        break;
                    case 2:
                        //moving
                        ridemode = 1;
                        player.getPersistentDataContainer().set(keygen("ridemode"),PersistentDataType.INTEGER,ridemode);
                        break;
                }
            }
        }
    }
}
}
