package xyz.yaszu.freedom.Soul.Alchemy;

import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.SoulTypes;
import xyz.yaszu.freedom.Util.Util;

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

    public static ItemStack GrappleItem() {
        ItemStack stack = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(dess("Grapple Hook"));
        meta.lore(List.of(dess("Grapple with the stars")));
        meta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Capella.name());
        stack.setItemMeta(meta);
        return stack;
    }
    @Override
    public void AbilityOne(Player player) {
        if (can_ability(AbilityOne_Cooldown(player),abilityOneCooldowns,player.getUniqueId())) {
            if (player.getInventory().getItemInMainHand() != null) {
                Constilation nova = Constilation.valueOf(player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(keygen("const"), PersistentDataType.STRING));
                switch (nova) {
                    case Crux -> {

                    }
                    case Vega -> {

                    }
                    case Rigal -> {

                    }
                    case Capella -> {

                    }
                    case none -> {
                        player.sendActionBar(dess("Your Stella is missing a constellation"));
                    }
                    case Sirius -> {
                        
                    }
                    case Polaris -> {

                    }

                }
            } else {
                player.sendActionBar(dess("You must have a Stella in your main hand"));
            }
        } else {
            player.sendActionBar(dess("Ability one is on cooldown."));
        }
    }
    @Override
    public ItemStack Related_Item() {
        ItemStack Stella = ItemStack.of(Material.WOODEN_SWORD);
        ItemMeta swordMeta = Stella.getItemMeta();
        swordMeta.displayName(dess("Astral Blade"));
        swordMeta.displayName(dess("Stella: Empty"));
        swordMeta.lore(List.of(dess("The stars are left un-tapped")));
        swordMeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Crux.name());
        swordMeta.setUnbreakable(true);

        return Stella;
    }

    //
    @Override
    public Component AbilityTwoName() {
        return dess("Stella");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("Constellation of the stars");
    }

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) throws MineSkinException, DataRequestException {
        Constilation nova = Constilation.valueOf(ability_item.getItemMeta().getPersistentDataContainer().get(keygen("const"), PersistentDataType.STRING));
        ItemMeta workingmeta = ability_item.getItemMeta();
        try {
            workingmeta.removeAttributeModifier(Attribute.ATTACK_DAMAGE,new AttributeModifier(keygen("const"),0.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        } catch (Exception ignored) {}

        switch (nova) {
            case none -> {
                workingmeta.displayName(dess("Stella: empty"));
                workingmeta.lore(List.of(dess("the stars are left un-tapped")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Crux.name());
                workingmeta.setItemModel(NamespacedKey.minecraft("stella_empty"));
                workingmeta.addAttributeModifier(Attribute.ATTACK_DAMAGE,new AttributeModifier(keygen("const"),0.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
            }
            case Crux -> {
                workingmeta.displayName(dess("Stella: Crux"));
                workingmeta.lore(List.of(dess("A blade made of the light of the stars")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Sirius.name());
                workingmeta.setItemModel(NamespacedKey.minecraft(Constilation.Crux.name()));
                workingmeta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(keygen("const"),2.0, AttributeModifier.Operation.ADD_SCALAR));
            }
            case Sirius -> {
                workingmeta.displayName(dess("Stella: Sirius"));
                workingmeta.lore(List.of(dess("SUPER NOVA-")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Polaris.name());
                workingmeta.setItemModel(NamespacedKey.minecraft(Constilation.Sirius.name()));
                workingmeta.addAttributeModifier(Attribute.ATTACK_DAMAGE,new AttributeModifier(keygen("const"),0.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
            }
            case Polaris -> {
                workingmeta.displayName(dess("Stella: Polaris"));
                workingmeta.lore(List.of(dess("A beam of light that can pierce the very heavens")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Vega.name());
                workingmeta.setItemModel(NamespacedKey.minecraft(Constilation.Polaris.name()));

            }
            case Vega -> {
                workingmeta.displayName(dess("Stella: Vega"));
                workingmeta.lore(List.of(dess("The CRUSHING gravity of the universe")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Rigal.name());
                workingmeta.setItemModel(NamespacedKey.minecraft(Constilation.Vega.name()));
                workingmeta.addAttributeModifier(Attribute.BLOCK_INTERACTION_RANGE,new AttributeModifier(keygen("const"), 2.0, AttributeModifier.Operation.ADD_SCALAR));
                workingmeta.addAttributeModifier(Attribute.MINING_EFFICIENCY,new AttributeModifier(keygen("const"), 4.0, AttributeModifier.Operation.ADD_SCALAR));
                workingmeta.addAttributeModifier(Attribute.BLOCK_BREAK_SPEED,new AttributeModifier(keygen("const"), 30.0, AttributeModifier.Operation.ADD_SCALAR));
            }
            case Rigal -> {
                workingmeta.displayName(dess("Stella: Rigal"));
                workingmeta.lore(List.of(dess("The stars are yours to command")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Capella.name());
                workingmeta.setItemModel(NamespacedKey.minecraft(Constilation.Rigal.name()));
            }
            case Capella -> {
                workingmeta.displayName(dess("Stella: Capella"));
                workingmeta.lore(List.of(dess("MOVEMENT")));
                workingmeta.getPersistentDataContainer().set(keygen("const"), PersistentDataType.STRING, Constilation.Crux.name());
                workingmeta.setItemModel(NamespacedKey.minecraft(Constilation.Capella.name()));
                workingmeta.addAttributeModifier(Attribute.MOVEMENT_SPEED,new AttributeModifier(keygen("const"), 1.5, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                workingmeta.addAttributeModifier(Attribute.WATER_MOVEMENT_EFFICIENCY,new AttributeModifier(keygen("const"), 2, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                workingmeta.addAttributeModifier(Attribute.OXYGEN_BONUS,new AttributeModifier(keygen("const"), 3, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
                workingmeta.addAttributeModifier(Attribute.FLYING_SPEED,new AttributeModifier(keygen("const"), 1.5, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
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
        if (given instanceof Player player) {
            if (getSoulType(player) != SoulTypes.Astral) return 0;
            Constilation lastAbilityUse = Constilation.valueOf(player.getPersistentDataContainer().getOrDefault(keygen("const"),PersistentDataType.STRING,"none"));
            switch (lastAbilityUse) {
                case Crux -> {
                    return 120000;
                }
                case Sirius -> {
                    return 5;
                }
                case Polaris -> {
                    return 4;
                }
                case Vega -> {
                    return 3;
                }
                case Rigal -> {
                    return 2;
                }
                case Capella -> {
                    return 1;
                }
                case none -> {
                    return 0;
                }
            }
        }

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
