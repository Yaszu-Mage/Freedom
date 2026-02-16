package xyz.yaszu.freedom.Soul;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util;

public class Green extends Util implements Base_Soul{

    @Override
    public String Name_For_Container() {
        return "Green";
    }

    @Override
    public Component Name() {
        return dess("<green>Green</green>");
    }

    @Override
    public Component Description() {
        return dess("You want to help, you want to mend what has been broken");
    }

    @Override
    public ItemStack Icon() {
        return ItemStack.of(Material.GOLDEN_APPLE);
    }

    @Override
    public Component AbilityOneName() {
        return dess("<green>Ability One</green> - ⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public Component AbilityOneDescription() {
        return dess("⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void AbilityOne(Player player) {

    }

    @Override
    public ItemStack Related_Item() {
        return ItemStack.of(Material.STICK);
    }

    @Override
    public Component AbilityTwoName() {
        return dess("Ability Two - ⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public Component AbilityTwoDescription() {
        return dess("⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void AbilityTwo(Player player, ItemStack ability_item) {

    }

    @Override
    public Component Passive_Description() {
        return dess("⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void Passive(Player player, Object event) {
        heal_all_trusted(player).runTaskTimer(Bukkit.getPluginManager().getPlugin("Freedom"),0,60);
    }

    public BukkitRunnable heal_all_trusted(Player player) {
        return new BukkitRunnable() {

            @Override
            public void run() {
                World world = player.getWorld();
              for (Player iterator : world.getPlayers()) {
                  if (iterator != player) {
                      String trusted;
                      Freedom.get_plugin().getLogger().info("Iterating through" + iterator.getName());
                      if (iterator.getPersistentDataContainer().has(keygen("trustedby"), PersistentDataType.STRING)) {
                          trusted = iterator.getPersistentDataContainer().get(keygen("trustedby"), PersistentDataType.STRING);
                          if (trusted.contains(player.getName())) {
                              iterator.addPotionEffect(PotionEffectType.REGENERATION.createEffect(60, 0));
                              Freedom.get_plugin().getLogger().info("healed " + iterator.getName());
                          }
                      }

                  }
              }
            }
        };
    }

    @Override
    public Component ActivePassive_Description() {
        return dess("⬛⬛⬛⬛⬛⬛⬛");
    }

    @Override
    public void ActivePassive(Player player) {

    }
}
