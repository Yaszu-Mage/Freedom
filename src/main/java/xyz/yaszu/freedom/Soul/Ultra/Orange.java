package xyz.yaszu.freedom.Soul.Ultra;

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
import xyz.yaszu.freedom.Soul.Base.BaseOrange;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Soul.soulListener;
import xyz.yaszu.freedom.Subsystems.CurseManager;
import xyz.yaszu.freedom.Subsystems.Life_and_Death;
import xyz.yaszu.freedom.Subsystems.TabDistance;
import xyz.yaszu.freedom.Util.Util;

import java.util.*;

public class Orange extends Util implements Base_Soul, Listener {
    @Override
    public String Name_For_Container() {
        return "Orange";
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
        return dess("Increased your control over distance.");
    }



    @Override
    public void AbilityOne(Player player) {
        if (can_ability(AbilityOne_Cooldown(),abilityOneCooldowns,player.getUniqueId())) {
            boolean hasactivated = false;
            Collection<PotionEffect> pots = player.getActivePotionEffects();
            if (!pots.isEmpty()) {
                hasactivated = true;
                player.sendMessage(dess("<shadow:#000000FF><b><green>Status Report</green>:</b></shadow>"));
                abilityOneCooldowns.put(player.getUniqueId(),System.currentTimeMillis());
            }
            player.clearActivePotionEffects();
            for (PotionEffect pot : pots) {
                player.sendMessage(dess("<shadow:#000000FF><b><green>Amplified: </shadow>" + pot.getType().getName() + ", duration " + pot.getDuration()/20 + "s."));
                player.addPotionEffect(new PotionEffect(pot.getType(),pot.getDuration(),pot.getAmplifier() + 1,pot.isAmbient(),pot.hasParticles()));
                Color color = Color.ORANGE;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,4);
                drawSpiral(player.getLocation(),10, 3,player.getWorld(),64, Particle.DUST,new Particle.DustOptions(color,8f));
            }
            List<Entity> entities = player.getNearbyEntities(4,4,4);
            for (Entity entity : entities) {
                if (entity instanceof Player playeriterated) {
                    if (player.getPersistentDataContainer().has(keygen("trustedby"))) {
                        if (player.getPersistentDataContainer().get(keygen("trustedby"),PersistentDataType.STRING).contains(playeriterated.getName())) {
                            Collection<PotionEffect> iterpots = playeriterated.getActivePotionEffects();
                            playeriterated.clearActivePotionEffects();
                            if (!iterpots.isEmpty()) {
                                if (!hasactivated) {
                                    player.sendMessage(dess("<shadow:#000000FF><b><green>Status Report</green>:</b></shadow> Status Report"));
                                }
                                hasactivated = true;
                                abilityOneCooldowns.put(player.getUniqueId(),System.currentTimeMillis());
                            }
                            for (PotionEffect pot : iterpots) {
                                player.sendMessage(dess("<shadow:#000000FF><b><green>Amplified: </shadow></b>" + pot.getType().toString() + ", duration " + pot.getDuration() + "."));
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,1);
                                playeriterated.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,1);
                                playeriterated.addPotionEffect(new PotionEffect(pot.getType(),pot.getDuration(),pot.getAmplifier() + 1,pot.isAmbient(),pot.hasParticles()));
                                Color color = Color.ORANGE;
                                drawSpiral(player.getLocation(),6, 3,player.getWorld(),16, Particle.DUST,new Particle.DustOptions(color,1.0f));
                            }
                        }
                    }
                }
            }
            if (!hasactivated) {
                player.sendMessage(dess("<shadow:#000000FF><b><Red>ERROR</Red>:</b></shadow> You have no potions to increase!"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,-1);
            }

        } else {
            double seconds = (double) (effective_cooldown(AbilityOne_Cooldown(), player.getUniqueId()) - (System.currentTimeMillis() - abilityOneCooldowns.get(player.getUniqueId()))) / 1000;
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,-1);
            player.sendActionBar(dess("<Red>Cooldown!</Red> Please wait " + Math.round(seconds) + " seconds!"));
        }
    }




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
        return dess("Curse one person to become a frog for 3 minutes. They cannot speak and they have weakness 2");
    }


    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        if (can_ability(AbilityTwo_Cooldown(),abilityTwoCooldowns,player.getUniqueId()) && !player.getPersistentDataContainer().getOrDefault(keygen("disguised"), PersistentDataType.BOOLEAN, false)) {
            if (Bukkit.getOnlinePlayers().size() >= 2) {
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE,1,1);
                InventoryGui inventoryGui = new InventoryGui();
                inventoryGui.setInventory(player);
                player.openInventory(inventoryGui.getInventory());
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,1,-1);
                player.sendMessage(dess("<shadow:#000000FF><b><Red>ERROR</Red>:</shadow> You need at least <shadow:#000000FF><b>2</shadow> players online to use this ability!"));
            }

        } else {
            // no no ability
            if (abilityTwoCooldowns.get(player.getUniqueId()) != null) {
                double seconds = (double) (effective_cooldown(AbilityTwo_Cooldown(), player.getUniqueId()) - (System.currentTimeMillis() - abilityTwoCooldowns.get(player.getUniqueId()))) / 1000;
                player.sendActionBar(dess("You can't use this ability yet, wait " + seconds + " seconds"));
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
            if (baller != null) {
                if (!CurseManager.isCursed(baller)) {
                    if (player.getPersistentDataContainer().getOrDefault(keygen("cancurse"), PersistentDataType.BOOLEAN, true)) {
                        player.getPersistentDataContainer().set(keygen("cancurse"), PersistentDataType.BOOLEAN, false);
                        CurseManager.curse(baller, player);
                    } else {
                        player.sendMessage(dess("<red>You cannot curse anyone right now!</red>"));
                    }
                } else {
                    player.sendMessage(dess("<red>This person is already cursed!</red>"));
                }
                player.closeInventory();
                event.setCancelled(true);
            }
        }
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
    public long AbilityTwo_Cooldown() {
        return 30000;
    }

    @Override
    public long AbilityOne_Cooldown() {
        return 30000;
    }

    @Override
    public void ActivePassive(Player player) {
        // witch broom flight
        double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        Freedom.get_plugin().getLogger().info("Active active!");
        if (SoulPoints > 1  && !player.getPersistentDataContainer().has(keygen("combattimer"))) {


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
                    double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
                    if (broom.isDead() || !player.isOnline() || !player.isInsideVehicle() || SoulPoints < 1) {

                        cancel();
                    } else {
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

                        if (tick >= 600) {
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
