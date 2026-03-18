package xyz.yaszu.freedom.Subsystems;


import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class black_flash implements Listener {
    Random random = new Random();

    @EventHandler
    public void blackflash(PrePlayerAttackEntityEvent event) {
        Player attacker = event.getPlayer();
        NamespacedKey key = new NamespacedKey(Bukkit.getPluginManager().getPlugin("Freedom"), "black_flash");

        if (!attacker.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
            attacker.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
        }

        int additive = attacker.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        Entity attacked = event.getAttacked();

        if (!attacker.equals(attacked)) {
            int value = random.nextInt(201);
            if (value >= 195) {
                Bukkit.getPluginManager().getPlugin("Freedom").getLogger().info(attacker.getDisplayName() + " hit a black flash");

                attacker.sendMessage(MiniMessage.miniMessage().deserialize("<gradient:#870700:#eb0c00>You hit a black flash!</gradient>"));
                attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 0.7f);

                // Spawn Black and Dark Red particles
                attacker.getWorld().spawnParticle(Particle.LARGE_SMOKE, attacked.getLocation(), 50, 0.5, 0.5, 0.5, 0.02);
                attacker.getWorld().spawnParticle(Particle.FLAME, attacked.getLocation(), 50, 0.5, 0.5, 0.5);

                // Apply effects
                attacker.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 10, 6));
                PotionEffect fx = new PotionEffect(PotionEffectType.SLOWNESS,120,0);
                PotionEffect fx2 = new PotionEffect(PotionEffectType.SLOWNESS,120,0);
                ((LivingEntity) attacked).addPotionEffect(fx);
                ((LivingEntity) attacked).addPotionEffect(fx2);


                // Reset counter
                attacker.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
            }
        }
    }
}
