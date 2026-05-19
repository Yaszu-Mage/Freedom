package xyz.yaszu.freedom.Items.Parts;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Items.BaseItem;
import xyz.yaszu.freedom.Items.CustomItemType;
import xyz.yaszu.freedom.Soul.Alchemy.Astral;
import xyz.yaszu.freedom.Util.FreedomKeys;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.yaszu.freedom.Util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Grapple_Hook extends Util implements Listener, BaseItem {

    @Override
    public ItemStack item() {
        ItemStack item = ItemStack.of(Material.FISHING_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(FreedomKeys.itemId(), PersistentDataType.STRING,"grapplehook");
        meta.displayName(Util.dess("<shadow:#000000FF><b><yellow>Grapple Hook</yellow></b>"));
        meta.setItemModel(NamespacedKey.minecraft("grapple_hook"));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void effect(Player player, PlayerInteractEvent event, ItemStack item) {

    }

    @Override
    public Recipe recipe() {
        ShapedRecipe recipe = new ShapedRecipe(keygen("grapple_hook"), item());
        recipe.shape(
                " IS",
                " TS",
                "TG "
        );
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('T', Material.STICK);
        return recipe;
    }

    @Override
    public CustomItemType getType() {
        return CustomItemType.PART;
    }


    private final Map<UUID, BukkitTask> grappleTasks = new HashMap<>();

    @EventHandler
    public void onFish(PlayerFishEvent event) {

        Player player = event.getPlayer();
        FishHook hook = event.getHook();

        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();

        boolean usingGrapple =
                main.isSimilar(item()) ||
                        off.isSimilar(item()) ||
                        main.isSimilar(Astral.GrappleItem());

        if (!usingGrapple) return;
        Freedom.get_plugin().getLogger().info("Grapple hooked");
        PlayerFishEvent.State state = event.getState();

        /*
         * Stop grapple
         */
        if (state == PlayerFishEvent.State.REEL_IN
                || state == PlayerFishEvent.State.FAILED_ATTEMPT) {

            BukkitTask old = grappleTasks.remove(player.getUniqueId());

            if (old != null) {
                old.cancel();
            }

            return;
        }

        /*
         * Grapple connected
         */
        if (state == PlayerFishEvent.State.FISHING
                || state == PlayerFishEvent.State.CAUGHT_ENTITY) {

            BukkitTask old = grappleTasks.remove(player.getUniqueId());

            if (old != null) {
                old.cancel();
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    BukkitTask task = new BukkitRunnable() {


                        final double ropeLength =
                                player.getLocation().distance(hook.getLocation());

                        boolean launched = false;

                        @Override
                        public void run() {
                            if (hook.isOnGround()) {


                                if (!player.isOnline()
                                        || player.isDead()
                                        || !hook.isValid()
                                        || hook.isDead()) {

                                    cancel();
                                    return;
                                }

                                Location playerLoc = player.getLocation();
                                Location hookLoc = hook.getLocation();

                                Vector rope =
                                        playerLoc.toVector().subtract(hookLoc.toVector());

                                double distance = rope.length();

                                if (distance <= 0.001) return;

                                Vector ropeDir = rope.normalize();

                                Vector velocity = player.getVelocity();

                                /*
                                 * INITIAL SWING IMPULSE
                                 */
                                if (!launched) {

                                    launched = true;

                                    Vector tangent = new Vector(
                                            -ropeDir.getZ(),
                                            0,
                                            ropeDir.getX()
                                    ).normalize();

                                    tangent.add(
                                            player.getLocation()
                                                    .getDirection()
                                                    .multiply(0.8)
                                    ).normalize();

                                    velocity.add(tangent.multiply(1.3));
                                    velocity.setY(0.4);
                                }

                                /*
                                 * REMOVE OUTWARD VELOCITY
                                 */
                                double outwardSpeed = velocity.dot(ropeDir);

                                if (outwardSpeed > 0) {

                                    Vector outward =
                                            ropeDir.clone().multiply(outwardSpeed);

                                    velocity.subtract(outward);
                                }

                                /*
                                 * HARD POSITION CONSTRAINT
                                 *
                                 * THIS IS WHAT MAKES HANGING WORK
                                 */
                                if (distance > ropeLength) {

                                    double excess = distance - ropeLength;

                                    /*
                                     * Move player back onto rope sphere
                                     */
                                    Vector correction =
                                            ropeDir.multiply(excess);

                                    Location corrected =
                                            playerLoc.clone().subtract(correction);


                                    /*
                                     * Remove velocity away from hook
                                     */
                                    double radial =
                                            velocity.dot(ropeDir);

                                    if (radial > 0) {

                                        velocity.subtract(
                                                ropeDir.clone().multiply(radial)
                                        );
                                    }
                                }

                                /*
                                 * Small inward tension
                                 */
                                velocity.subtract(
                                        ropeDir.clone().multiply(0.015)
                                );

                                /*
                                 * Air resistance
                                 */
                                velocity.multiply(0.995);

                                /*
                                 * Clamp speed
                                 */
                                double maxSpeed = 5;

                                if (velocity.length() > maxSpeed) {
                                    velocity = velocity.normalize().multiply(maxSpeed);
                                }
                                Freedom.get_plugin().getLogger().info("Grapple speed: " + velocity.length());
                                player.setVelocity(velocity);
                            }
                        }

                    }.runTaskTimer(Freedom.get_plugin(), 0L, 1L);
                    grappleTasks.put(player.getUniqueId(), task);
                }
            }.runTaskLater(Freedom.get_plugin(), 1);



        }
    }
}
