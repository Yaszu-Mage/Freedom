package xyz.yaszu.freedom.Soul.Alchemy;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Util.Util;
import org.bukkit.event.entity.EntityDamageEvent;

public class Astral extends Util implements Base_Soul {
    @Override
    public String Name_For_Container() {
        return "Astral";
    }

    @Override
    public Component Name() {
        return dess("Astral");
    }

    @Override
    public Component Description() {
        return dess("You feel the your soul glimmer with the stars");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(org.bukkit.Material.NETHER_STAR);
    }

    @Override
    public Component AbilityOneName() {
        return dess("");
    }

    @Override
    public Component AbilityOneDescription() {
        return null;
    }

    @Override
    public void AbilityOne(Player player) {

    }

    @Override
    public ItemStack Related_Item() {
        ItemStack AternaStella = ItemStack.of(Material.WOODEN_SWORD);
        ItemMeta swordMeta = AternaStella.getItemMeta();

        swordMeta.displayName(dess("<color:#3700ff>My</color> rifle"));
        return AternaStella;
    }
    //tbh if we try and ally with Zane we should be fine
    // I'm thinking of living close enough to burger city (Syl's planned town) but far enough to run if shit hits the fan
    //bc I'm guessing it will hit the fan as soon as the EnderDragon is beat, or (if) someone gets enough cost for a nuke
    // no immediete end access, it should be a week after server start
    //hard maybe

    //
    @Override
    public Component AbilityTwoName() {
        return null;
    }

    @Override
    public Component AbilityTwoDescription() {
        return null;
    }

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {

    }

    @Override
    public Component Passive_Description() {
        return dess("Starborn: the stars bless your blade");
    }

    @Override
    public void Passive(Player player, Object event) {
        if(player.getLocation().getBlock().getLightFromSky() <= 0){
            player.addPotionEffect(PotionEffectType.SPEED.createEffect(20,0));
            player.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(20,0));
        }
    }

    @Override
    public Component ActivePassive_Description() {
        return dess("Starfall: the stars of your foes bless your soul");
    }

    @Override
    public long AbilityTwo_Cooldown() {
        return 120000;
    }

    @Override
    public long AbilityOne_Cooldown(Object given) {
        return 0;
    }

    @Override
    public void ActivePassive(Player player) {
        player.spawnParticle(org.bukkit.Particle.END_ROD, player.getLocation().add(0,1,0), 20, 0.5,0.5,0.5,0.01);
        player.getNearbyEntities(5,5,5).forEach(entity -> {
            entity.getLocation().getWorld().spawnParticle(org.bukkit.Particle.END_ROD, entity.getLocation().add(0,1,0), 5, 0.5,0.5,0.5,0.01);
            player.getHealth();
            if (entity instanceof Player other) {
                other.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(100,0));
            }

        });

    }
}
