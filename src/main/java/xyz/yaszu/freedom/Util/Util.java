package xyz.yaszu.freedom.Util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.property.SkinProperty;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Optional;
import java.util.UUID;

public class Util {
    public static SkinsRestorer skinsRestorerAPI;
    public static NamespacedKey keygen(String key) {
        return new NamespacedKey(Bukkit.getPluginManager().getPlugin("Freedom"),key);
    }

    public static Component dess(String minimessage) {
        return MiniMessage.miniMessage().deserialize(minimessage);
    }
    public void drawCircle(Location center, double radius, World world, int points,Particle particle) {
        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(i * 360.0 / points); // Calculate angle in radians
            double x = center.getX() + (radius * Math.cos(angle)); // Calculate X coordinate
            double z = center.getZ() + (radius * Math.sin(angle)); // Calculate Z coordinate
            double y = center.getY(); // Y remains constant for a flat circle

            // Create a new location for the point
            Location pointLocation = new Location(world, x, y, z);

            // 2. Spawn particles
            world.spawnParticle(particle, pointLocation, 1);
        }
    }
    public static ItemStack getSkull(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
        item.setItemMeta(meta);
        return item;
    }
    public static ItemStack getCustomSkull(String base64) {

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (base64.isEmpty()) return head;

        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        PlayerProfile profile = skullMeta.getPlayerProfile();
        profile = Bukkit.createProfile(UUID.randomUUID(),null);
        profile.getProperties().add(new ProfileProperty("textures", base64));
        skullMeta.setPlayerProfile(profile);
        head.setItemMeta(skullMeta);
        return head;
    }
    public SkinProperty getPlayerSkin(Player player) throws DataRequestException {
        PlayerStorage playerStorage = skinsRestorerAPI.getPlayerStorage();
        Optional<SkinProperty> property = playerStorage.getSkinForPlayer(
                player.getUniqueId(),
                player.getName()
        );
        return property.orElse(null);
    }



    public void setSkinByName(Player player, String skinName) throws MineSkinException, DataRequestException {
        SkinStorage skinStorage = skinsRestorerAPI.getSkinStorage();
        PlayerStorage playerStorage = skinsRestorerAPI.getPlayerStorage();
        // Find or fetch the skin data
        Optional<InputDataResult> result = skinStorage.findOrCreateSkinData(skinName);
        if (result.isPresent()) {
            // Set the skin identifier for the player
            playerStorage.setSkinIdOfPlayer(
                    player.getUniqueId(),
                    result.get().getIdentifier()
            );
            // Apply the skin visually
            skinsRestorerAPI.getSkinApplier(Player.class).applySkin(player);
        }
    }
}
