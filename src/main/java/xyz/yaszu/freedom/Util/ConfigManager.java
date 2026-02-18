package xyz.yaszu.freedom.Util;


import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final FileConfiguration config;

    public ConfigManager(FileConfiguration config) {
        this.config = config;
    }

    public String getWorldName() {
        return config.getString("the_void.world_name", "the_void");
    }

    public boolean darknessEnabled() {
        return config.getBoolean("the_void.darkness_effect", true);
    }
    public int getresetvalue() {return config.getInt("time.reset_value");}
    public int get_seed() {return config.getInt("time.seed");}
    public void set_seed(long value) {config.set("time.seed",value);}
    public void set_reload_value(int value) {config.set("time.reset_value",value);}

    public Material getReturnBlock() {
        try {
            return Material.valueOf(config.getString("the_void.return_block", "GOLD_BLOCK").toUpperCase());
        } catch (Exception e) {
            return Material.GOLD_BLOCK;
        }
    }

    public boolean heart_destroy_enabled() {
        return config.getBoolean("heart.destroy_world");
    }

    public boolean ambientSoundsEnabled() {
        return config.getBoolean("the_void.ambient_sounds.enabled", true);
    }

    public double getAmbientChance() {
        return config.getDouble("the_void.ambient_sounds.chance", 0.3);
    }

    public List<String> getAmbientSoundList() {
        return config.getStringList("the_void.ambient_sounds.sounds");
    }

    public boolean bossbarEnabled() {
        return config.getBoolean("the_void.bossbar.enabled", true);
    }

    public String bossbarText() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("the_void.bossbar.text", "&7[&5Birch Realm&7]"));
    }

    public BarColor bossbarColor() {
        try {
            return BarColor.valueOf(config.getString("the_void.bossbar.color", "PURPLE").toUpperCase());
        } catch (Exception e) {
            return BarColor.PURPLE;
        }
    }

    public BarStyle bossbarStyle() {
        try {
            return BarStyle.valueOf(config.getString("the_void.bossbar.style", "SEGMENTED_10").toUpperCase());
        } catch (Exception e) {
            return BarStyle.SEGMENTED_10;
        }
    }

    public String getReturnMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("the_void.return_message", "&eYou escaped the Birch Realm!"));
    }

    public boolean mobSpawnsEnabled() {
        return config.getBoolean("the_void.enable_mob_spawns", true);
    }

    public double getMobSpawnChance() {
        return config.getDouble("the_void.mob_spawn_chance", 0.05);
    }

    public List<String> getMobTypes() {
        return config.getStringList("the_void.mob_types");
    }
}
