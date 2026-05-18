package xyz.yaszu.freedom.Soul.Alchemy;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Alchemy.SpellCompiler;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Subsystems.CurrencyManager;
import xyz.yaszu.freedom.Util.Util;

import static xyz.yaszu.freedom.Alchemy.SpellCompiler.*;
import static xyz.yaszu.freedom.Items.Parts.SpellFocus.castFromOffhand;

public class Arcanus extends Util implements Base_Soul {
    //based off azuri here https://www.youtube.com/watch?v=mpt5UPAUEw4
    @Override
    public String Name_For_Container() {
        return "Arcanus";
    }

    @Override
    public Component Name() {
        return dess("Arcanus");
    }

    @Override
    public Component Description() {
        return dess("Magic, alchemy, whatever you call it");
    }

    @Override
    public ItemStack Icon() {
        //TODO make this special
        ItemStack stack = ItemStack.of(Material.RECOVERY_COMPASS);
        return stack;
    }

    @Override
    public Component AbilityOneName() {
        return dess("Ability One: Embedded Process");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("Cast spells without a spell focus and with + 500 max cost");
    }

    @Override
    public void AbilityOne(Player player) {
        //TODO make this work
        castFromOffhand(player,500);
    }

    @Override
    public ItemStack Related_Item() {
        //TODO make this special
        ItemStack stack = ItemStack.of(Material.RECOVERY_COMPASS);
        return stack;
    }

    @Override
    public Component AbilityTwoName() {
        return dess("Ability Two: The Red Castle");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("Cast a spell that massively harms enemies within a radius of the ending location");
    }

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        //TODO Charge, then projectile that can be blocked by shield
        new BukkitRunnable() {
            enum Stages {
                Charge,
                Projectile,
                Detonating
            }
            int stageTick = 0;
            int tick = 0;
            int chargeTime = 1000;
            int detonatingTime = 625;

            Stages stage = Stages.Charge;
            Location starting = player.getLocation();
            Location currentLocation = starting;
            @Override
            public void run() {
                switch (stage) {
                    case Charge -> {
                        if (stageTick == 0) {
                            stageTick = 1;
                            tick = 0;
                        }
                        //AKA stop if moved
                        if (player.getLocation().getX() != starting.getX() || player.getLocation().getY() != starting.getY() || player.getLocation().getZ() != starting.getZ()) {
                            this.cancel();
                        }
                        if (stageTick + tick >= stageTick + chargeTime) {
                            stage = Stages.Projectile;
                            stageTick = 0;
                        }
                    }
                    case Projectile -> {
                        if (stageTick == 0) {
                            stageTick = 1;
                            tick = 0;
                        }
                        //so lets make a projectile starting at player
                        if (tick % 10 == 0) {
                            currentLocation.getWorld().playSound(currentLocation, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.5f);
                        }
                        double radius = 0.5 + ((double) (tick - 200)/20);
                        drawSphere(currentLocation, radius,16, Particle.DUST,new Particle.DustOptions(Color.PURPLE,8f));
                        currentLocation = currentLocation.add(player.getLocation().getDirection().multiply(0.5));
                        if (stageTick + player.getLocation().distanceSquared(currentLocation) >= stageTick + detonatingTime || currentLocation.getBlock().getType() != Material.AIR || currentLocation.getBlock().isLiquid() || !currentLocation.getNearbyEntities(radius,radius,radius).isEmpty() || radius >= 10) {
                            stage = Stages.Detonating;
                            stageTick = 0;
                        }
                    }
                    case Detonating -> {
                        double radius = 0.5 + ((double) (tick - 200)/20);
                        drawSphere(currentLocation, radius,16, Particle.DUST,new Particle.DustOptions(Color.RED,8f));
                        drawEye(currentLocation,1);
                        createVerticleMinMagicCircle(currentLocation,15, SoulTypes.BaseBlack,player.getLocation().getYaw(),player.getLocation(),50,1);
                        player.getWorld().playSound(currentLocation, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.5f);
                        currentLocation.getNearbyEntities(radius,radius,radius).forEach(entity -> {
                            if (entity instanceof LivingEntity living) {
                                if (living instanceof Player per) {
                                    if (!per.isBlocking()) {
                                        per.damage(40);
                                        per.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(100,4));
                                    } else {
                                        per.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(1,0));
                                    }
                                } else {
                                    living.damage(40);
                                    living.addPotionEffect(PotionEffectType.SLOWNESS.createEffect(100,4));
                                }
                            }
                        });
                        this.cancel();
                    }
                }
                tick = tick + 1;

            }
        }.runTaskTimer(player.getServer().getPluginManager().getPlugin("Freedom"), 0,0);
    }

    @Override
    public Component Passive_Description() {
        return dess("Word of Mouth: Cast Spells through chat");
    }

    @Override
    public void Passive(Player player, Object event) {
        if (event instanceof AsyncPlayerChatEvent chatEvent) {
            //actually kinda sick
            String msg = chatEvent.getMessage();
            var tokens = tokenize(msg);
            var ast = parse(tokens, player.getLocation(),player);
            var errors = validate(ast);
            if (errors.isEmpty()) {
                //actually cast

                //FIXME FIX MOBILE CASTING
                castMobileSpell(msg, player, 0);
            }
        }
    }

    @Override
    public Component ActivePassive_Description() {
        return dess("Arcanus: Generate cost passively");
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 300000;
    }

    @Override
    public long AbilityOne_Cooldown(Object given) {
        return 4000;
    }

    @Override
    public void ActivePassive(Player player) {
        double SoulPoints = player.getPersistentDataContainer().get(keygen("SoulPoint"), PersistentDataType.DOUBLE);
        if (SoulPoints < 5) return;
        CurrencyManager.addCurrency(50, player);
        player.getPersistentDataContainer().set(keygen("SoulPoint"), PersistentDataType.DOUBLE, SoulPoints - 5);
    }
}
