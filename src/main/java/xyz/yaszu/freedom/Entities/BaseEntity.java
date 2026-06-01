package xyz.yaszu.freedom.Entities;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter;
import kr.toxicity.model.api.platform.PlatformEntity;
import kr.toxicity.model.api.tracker.EntityTracker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataType;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface BaseEntity {

    // -------------------------
    // Required implementations
    // -------------------------

    /**
     * The vanilla EntityType this custom entity is based on.
     */
    org.bukkit.entity.EntityType baseType();

    /**
     * Current health of the entity.
     */
    int health();

    /**
     * Maximum health of the entity.
     */
    int maxHealth();

    /**
     * The unique name/identifier of this custom entity.
     * Used for model lookup and PDC tagging.
     */
    String name();

    /**
     * The biomes this entity is allowed to spawn in.
     */
    List<Biome> spawnBiomes();

    /**
     * The goal keys defining this entity's AI behaviour,
     * in priority order (index 0 = highest priority).
     */
    List<GoalKey<? extends Mob>> goals();

    // -------------------------
    // Default behaviour
    // -------------------------

    /**
     * Restores this entity's attributes, PDC tag, goals, and model.
     * Goals are fetched from the live mob before clearing to avoid null lookups.
     *
     * @param entity The entity to restore state onto.
     * @return The EntityTracker for the restored entity, or null if model not found.
     */
    default EntityTracker restore(Entity entity) {

        // --- Restore Goals ---
        if (entity instanceof Mob mob) {
            var mobGoals = Bukkit.getServer().getMobGoals();
            List<GoalKey<? extends Mob>> goalKeys = goals();

            // Fetch live Goal instances BEFORE clearing
            List<Goal<Mob>> resolved = new ArrayList<>();
            for (GoalKey<? extends Mob> key : goalKeys) {
                @SuppressWarnings("unchecked")
                Goal<Mob> goal = mobGoals.getGoal(mob, (GoalKey<Mob>) key);
                if (goal != null) {
                    resolved.add(goal);
                }
            }

            // Clear all existing goals, then re-add in defined priority order
            mobGoals.removeAllGoals(mob);
            for (int i = 0; i < resolved.size(); i++) {
                mobGoals.addGoal(mob, i + 1, resolved.get(i));
            }
        }

        // --- Restore Health & Attributes ---
        if (entity instanceof LivingEntity living) {
            java.util.Objects.requireNonNull(
                    living.getAttribute(Attribute.MAX_HEALTH)
            ).setBaseValue(maxHealth());
            living.setHealth(health());
        }

        // --- Tag entity in PDC ---
        entity.getPersistentDataContainer().set(
                Util.keygen("customEntity"),
                PersistentDataType.STRING,
                name()
        );

        // --- Restore BetterModel ---
        PlatformEntity platEntity = BukkitAdapter.adapt(entity);
        return BetterModel.model(name())
                .map(r -> r.getOrCreate(platEntity))
                .orElse(null);
    }

    /**
     * Checks whether a given entity is tagged as this custom entity type.
     *
     * @param entity The entity to check.
     * @return true if the entity's PDC name matches this entity's name.
     */
    default boolean isThis(Entity entity) {
        String tag = entity.getPersistentDataContainer()
                .get(Util.keygen("customEntity"), PersistentDataType.STRING);
        return name().equals(tag);
    }

    /**
     * Checks whether a given biome is valid for this entity to spawn in.
     *
     * @param biome The biome to check.
     * @return true if the biome is in the spawn biomes list.
     */
    default boolean canSpawnIn(Biome biome) {
        return spawnBiomes().contains(biome);
    }
    /**
     * Spawns and initializes this custom entity at the given location.
     * Applies goals, attributes, PDC tag, and model.
     *
     * @param location The location to spawn the entity at.
     * @return The EntityTracker for the spawned entity, or null if model not found.
     */
    default EntityTracker initialize(Location location) {
        Entity entity = Objects.requireNonNull(location.getWorld())
                .spawnEntity(location, baseType());

        return restore(entity);
    }
}