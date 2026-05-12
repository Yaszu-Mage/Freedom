package xyz.yaszu.freedom.Subsystems;

import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Cow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.Util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.Random;


public class NpcManager extends Util implements Listener {
    public static enum NpcGoal {
        Socialize,
        Mine,
        Construct,
        Farm,
        Wander,
        Harvest,
        Think
    }
    @EventHandler
    public void onEntityPathfind(EntityPathfindEvent event) {
        if (event.getEntity().getPersistentDataContainer().has(keygen("isNpc"))) {
            try {
                if (event.getEntity().getPersistentDataContainer().get(keygen("isallowedtomove"), PersistentDataType.BOOLEAN) != null &&
                        !event.getEntity().getPersistentDataContainer().get(keygen("isallowedtomove"), PersistentDataType.BOOLEAN)) {
                    event.setCancelled(true);
                }
            } catch (Exception ignored) {}

        }
    }

    public class NPC {
        public static Cow BaseEntity;
        public static Double hunger = 20d;
        public static NpcGoal goal = NpcGoal.Think;

        public NPC(Cow givenBaseEntity, Double GivenHunger, String givengoal){
            NpcGoal goalInstance = NpcGoal.valueOf(givengoal);
            if (goalInstance != null) goal = goalInstance;
            BaseEntity = givenBaseEntity;
            hunger = GivenHunger;
            BaseEntity.getPersistentDataContainer().set(keygen("isNpc"), PersistentDataType.BOOLEAN, true);
            BaseEntity.getPersistentDataContainer().set(keygen("isallowedtomove"), PersistentDataType.BOOLEAN, false);
        }

        public void move(Location targetLocation) {
            BaseEntity.getPersistentDataContainer().set(keygen("isallowedtomove"), PersistentDataType.BOOLEAN, true);
            BaseEntity.getPathfinder().moveTo(targetLocation, 1);
            //when path is finished change isallowedtomove to false
             new BukkitRunnable() {
                @Override
                public void run() {
                    if (BaseEntity.getPersistentDataContainer().get(keygen("isallowedtomove"), PersistentDataType.BOOLEAN) != null &&
                            BaseEntity.getPersistentDataContainer().get(keygen("isallowedtomove"), PersistentDataType.BOOLEAN)) {
                        if (BaseEntity.getLocation().distanceSquared(targetLocation) < 1) {
                            BaseEntity.getPersistentDataContainer().set(keygen("isallowedtomove"), PersistentDataType.BOOLEAN, false);
                            this.cancel();
                        }
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(Freedom.get_plugin(), 0, 20);
        }

        private boolean isStandable(Location location) {
            return location.getBlock().isPassable()
                    && location.clone().add(0, 1, 0).getBlock().isPassable()
                    && !location.clone().add(0, -1, 0).getBlock().isPassable();
        }

    }

    public class PathNode {
        public double Hcost = 0d;
        public double Gcost = 0d;
        public Location location;
        public PathNode(Location givenLocation) {
            location = givenLocation;
        }


        public void calculateCost(Location startingLocation, Location targetLocation) {
            Hcost = startingLocation.distanceSquared(location);
            Gcost = targetLocation.distanceSquared(location);
        }
        public double Fcost() {
            return Hcost + Gcost;
        }
    }
}
