package xyz.yaszu.freedom.Subsystems;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
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

    public class NPC {
        public static Mannequin mannequin;
        public static Double hunger = 20d;
        public static NpcGoal goal = NpcGoal.Think;
        public static double StepCuts = 1; // In Blocks
        public Mannequin constructMannequin(Location location) throws IOException, InterruptedException {
            Mannequin mannequin = location.getWorld().spawn(location, Mannequin.class);
            String baseUrl = "https://files.yaszu.xyz/skins/";
            // 1. Get file list as JSON
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "?ls"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = new Gson().fromJson(response.body(), JsonObject.class);
            JsonArray files = jsonResponse.getAsJsonArray("files");

            if (files != null && !files.isEmpty()) {
                Random rand = new Random();
                JsonObject randomFile = files.asList().get(rand.nextInt(files.size())).getAsJsonObject();
                String fileName = randomFile.get("name").getAsString();

                HttpRequest skinRequest = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + fileName))
                        .GET()
                        .build();
                String skinResponse = client.send(skinRequest, HttpResponse.BodyHandlers.ofString()).body().trim();

                String textureValue = skinResponse;
                if (skinResponse.startsWith("{")) {
                    JsonObject skinJson = new Gson().fromJson(skinResponse, JsonObject.class);
                    if (skinJson != null) {
                        if (skinJson.has("value")) {
                            textureValue = skinJson.get("value").getAsString();
                        } else if (skinJson.has("textures")) {
                            textureValue = skinJson.get("textures").getAsString();
                        }
                    }
                }

                PlayerProfile playerProfile = Bukkit.createProfile(UUID.randomUUID(), fileName);
                playerProfile.getProperties().add(new ProfileProperty("textures", textureValue));

                ResolvableProfile profile = ResolvableProfile.resolvableProfile(playerProfile);
                mannequin.setProfile(profile);
            }

            return mannequin;
        }
        public NPC(Mannequin Givenmannequin, Double GivenHunger, String givengoal){
            NpcGoal goalInstance = NpcGoal.valueOf(givengoal);
            if (goalInstance != null) goal = goalInstance;
            mannequin = Givenmannequin;
            hunger = GivenHunger;
        }

        public void move(Location targetLocation) {
            if (mannequin == null || targetLocation == null || !mannequin.isValid()) return;

            Location currentLocation = mannequin.getLocation().clone();
            if (currentLocation.getWorld() == null || targetLocation.getWorld() == null) return;

            if (!currentLocation.getWorld().equals(targetLocation.getWorld())) {
                mannequin.teleport(targetLocation);
                return;
            }

            double stepSize = Math.max(0.1d, StepCuts);
            double stopDistanceSq = stepSize * stepSize;
            int maxIterations = (int) Math.max(16, (currentLocation.distance(targetLocation) / stepSize) * 4);

            for (int i = 0; i < maxIterations; i++) {
                if (currentLocation.distanceSquared(targetLocation) <= stopDistanceSq) break;

                Vector direction = targetLocation.toVector().subtract(currentLocation.toVector());
                if (direction.lengthSquared() == 0) break;
                direction.normalize().multiply(stepSize);

                Location projectedStep = currentLocation.clone().add(direction);
                PathNode bestNode = null;
                double bestCost = Double.MAX_VALUE;

                // Sample a 3x3 grid around the projected step and choose the cheapest valid node.
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        Location candidate = projectedStep.clone().add(x * stepSize, 0, z * stepSize);
                        if (!isStandable(candidate)) continue;

                        PathNode node = new PathNode(candidate);
                        node.calculateCost(currentLocation, targetLocation);
                        double nodeCost = node.Fcost();

                        if (nodeCost < bestCost) {
                            bestCost = nodeCost;
                            bestNode = node;
                        }
                    }
                }

                if (bestNode == null) break;
                if (bestNode.location.distanceSquared(targetLocation) >= currentLocation.distanceSquared(targetLocation)) break;

                mannequin.teleport(bestNode.location);
                currentLocation = bestNode.location.clone();
            }

            if (currentLocation.distanceSquared(targetLocation) <= stopDistanceSq) {
                mannequin.teleport(targetLocation);
            }
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
