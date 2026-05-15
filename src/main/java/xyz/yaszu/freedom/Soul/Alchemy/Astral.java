package xyz.yaszu.freedom.Soul.Alchemy;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Util.Util;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;


public class Astral extends Util implements Base_Soul {
    public enum Constilation {
        Sirius, Polaris, Vega, Rigal, Capella, Crux, none
    }

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
        ItemStack Stella = ItemStack.of(Material.WOODEN_SWORD);
        ItemMeta swordMeta = Stella.getItemMeta();

        swordMeta.displayName(dess("<color:#3700ff>My</color> rifle"));
        return Stella;
    }

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

        Constilation nova = Constilation.valueOf(ability_item.getItemMeta().getPersistentDataContainer().get(keygen("const"), PersistentDataType.STRING));

        ItemMeta workingmeta = ability_item.getItemMeta();
        switch (nova) {
            case none -> {
                workingmeta.displayName(dess("Stella: empty"));
                workingmeta.lore(List.of(dess("the stars are left un-tapped")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Crux.name());
                //workingmeta.setItemModel();
            }
            case Crux -> {
                workingmeta.displayName(dess("Stella: Crux"));
                workingmeta.lore(List.of(dess("A blade made of the light of the stars")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Sirius.name());
                //workingmeta.setItemModel();
            }
            case Sirius -> {
                workingmeta.displayName(dess("Stella: Sirius"));
                workingmeta.lore(List.of(dess("SUPER NOVA-")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Polaris.name());
                //workingmeta.setItemModel();
            }
            case Polaris -> {
                workingmeta.displayName(dess("Stella: Polaris"));
                workingmeta.lore(List.of(dess("A beam of light that can pierce the very heavens")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Vega.name());
                //workingmeta.setItemModel();
            }
            case Vega -> {
                workingmeta.displayName(dess("Stella: Vega"));
                workingmeta.lore(List.of(dess("The CRUSHING gravity of the universe")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Rigal.name());
                //workingmeta.setItemModel();
            }
            case Rigal -> {
                workingmeta.displayName(dess("Stella: Rigal"));
                workingmeta.lore(List.of(dess("The stars follow your will")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Capella.name());
                //workingmeta.setItemModel();
            }
            case Capella -> {
                workingmeta.displayName(dess("Stella: Capella"));
                workingmeta.lore(List.of(dess("the slashing force of a comet in your hand")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Crux.name());
                //workingmeta.setItemModel();
            }
        }



        ability_item.setItemMeta(workingmeta);

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
