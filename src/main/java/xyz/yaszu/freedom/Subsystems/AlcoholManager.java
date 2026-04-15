package xyz.yaszu.freedom.Subsystems;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Util.FreedomKeys;
import xyz.yaszu.freedom.Util.Util;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AlcoholManager extends Util implements Listener {

    public static int minSeconds = 4;
    public static int maxSeconds = 120;
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final ConcurrentHashMap<UUID, DrunkSwayState> swayStates = new ConcurrentHashMap<>();

    private static final class DrunkSwayState {
        private final double phaseOffset;
        private double smoothedOffset;
        private double anchorX;
        private double anchorZ;
        private double passiveOffsetX;
        private double passiveOffsetZ;
        private double passiveTargetX;
        private double passiveTargetZ;
        private long nextRetargetAtMs;
        private String worldName;

        private DrunkSwayState(double phaseOffset, Location start) {
            this.phaseOffset = phaseOffset;
            this.anchorX = start.getX();
            this.anchorZ = start.getZ();
            this.worldName = start.getWorld() == null ? "" : start.getWorld().getName();
            pickNewPassiveTarget(this, 1.0D);
        }
    }

    private static void pickNewPassiveTarget(DrunkSwayState state, double maxRadius) {
        double angle = random.nextDouble() * TWO_PI;
        double radius = Math.sqrt(random.nextDouble()) * maxRadius;
        state.passiveTargetX = Math.cos(angle) * radius;
        state.passiveTargetZ = Math.sin(angle) * radius;
        state.nextRetargetAtMs = System.currentTimeMillis() + random.nextLong(900L, 2200L);
    }


    public static void removeAlcohol(Player player,int alcoholpotency) {
        player.getPersistentDataContainer().remove(FreedomKeys.alcohol());
    }

    /**
     * Adds alcohol to a player.
     *
     * @param player    Player to add alcohol.
     * @param saturation     Saturation how much saturation to add.
     * @param foodLevel    Food to add. Food level should be under 5.
     * @param alcoholpotency     Alcohol Potency.
     */
    public static void addAlcohol(Player player,int saturation,int foodLevel,int alcoholpotency) {
        player.setSaturation(player.getSaturation()+saturation);
        player.setFoodLevel(player.getFoodLevel()+foodLevel);
        player.getPersistentDataContainer().set(FreedomKeys.alcohol(), PersistentDataType.INTEGER,alcoholpotency);
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    int alcoholvalue = player.getPersistentDataContainer().get(FreedomKeys.alcohol(), PersistentDataType.INTEGER);
                    if (alcoholvalue-1 < 1) {
                        player.getPersistentDataContainer().remove(FreedomKeys.alcohol());
                        return;
                    }
                    player.getPersistentDataContainer().set(FreedomKeys.alcohol(), PersistentDataType.INTEGER,alcoholvalue-1);
                } catch (Exception ignored) {}

            }
        }.runTaskLater(Freedom.get_plugin(),random.nextInt(minSeconds * 20, maxSeconds * 20));
    }

    @EventHandler
    public void playerdrunkenmove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        int alcoholLevel = getAlcoholLevel(player);
        if (alcoholLevel <= 0) {
            swayStates.remove(player.getUniqueId());
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || from.getWorld() == null || to.getWorld() == null || from.getWorld() != to.getWorld()) {
            return;
        }

        Vector horizontalMotion = to.toVector().subtract(from.toVector()).setY(0);
        if (horizontalMotion.lengthSquared() < 1.0E-6D) {
            return;
        }

        DrunkSwayState state = swayStates.computeIfAbsent(player.getUniqueId(), ignored -> new DrunkSwayState(random.nextDouble() * TWO_PI, to));
        String worldName = to.getWorld().getName();
        if (!worldName.equals(state.worldName)) {
            state.anchorX = to.getX();
            state.anchorZ = to.getZ();
            state.passiveOffsetX = 0.0D;
            state.passiveOffsetZ = 0.0D;
            state.worldName = worldName;
            pickNewPassiveTarget(state, 1.0D);
        }

        // Keep anchor following natural movement while passive offset moves around it.
        state.anchorX += horizontalMotion.getX();
        state.anchorZ += horizontalMotion.getZ();

        long nowMs = System.currentTimeMillis();
        if (nowMs >= state.nextRetargetAtMs) {
            pickNewPassiveTarget(state, 1.0D);
        }

        // Smoothly chase the random target to create passive drifting motion.
        double passiveBlend = 0.14D;
        state.passiveOffsetX += (state.passiveTargetX - state.passiveOffsetX) * passiveBlend;
        state.passiveOffsetZ += (state.passiveTargetZ - state.passiveOffsetZ) * passiveBlend;
        double passiveLength = Math.hypot(state.passiveOffsetX, state.passiveOffsetZ);
        if (passiveLength > 1.0D) {
            state.passiveOffsetX = (state.passiveOffsetX / passiveLength);
            state.passiveOffsetZ = (state.passiveOffsetZ / passiveLength);
        }

        double normalized = Math.min(alcoholLevel, 5) / 5.0D;
        double amplitude = 0.03D + (0.15D * normalized);
        double frequency = 0.8D + (1.4D * normalized);
        double nowSeconds = System.currentTimeMillis() / 1000.0D;
        double targetOffset = Math.sin((nowSeconds * TWO_PI * frequency) + state.phaseOffset) * amplitude;

        // Smooth the wave to prevent sharp position snaps on uneven move event timing.
        state.smoothedOffset += (targetOffset - state.smoothedOffset) * 0.35D;
        state.smoothedOffset = Math.max(-0.22D, Math.min(0.22D, state.smoothedOffset));

        Vector moveDirection = horizontalMotion.normalize();
        Vector lateral = new Vector(-moveDirection.getZ(), 0, moveDirection.getX());
        Vector activeOffset = lateral.multiply(state.smoothedOffset);

        double swayX = state.anchorX + state.passiveOffsetX + activeOffset.getX();
        double swayZ = state.anchorZ + state.passiveOffsetZ + activeOffset.getZ();
        double swayY = to.getY();
        float yaw = to.getYaw();
        float pitch = to.getPitch();

        // Apply sway directly to the destination without teleporting
        // by smoothly moving the player toward the swayed position incrementally
        Location adjusted = to.clone();
        adjusted.setX(swayX);
        adjusted.setZ(swayZ);
        event.setTo(adjusted);

        if (alcoholLevel >= 5) {
            player.addPotionEffect(PotionEffectType.NAUSEA.createEffect(20, 1));
        } else if (alcoholLevel >= 4) {
            player.addPotionEffect(PotionEffectType.NAUSEA.createEffect(20, 0));
        }
    }

    private void sendSwayPacket(Player player, double x, double y, double z, float yaw, float pitch) {
        try {
            // Sway is applied directly via event.setTo() instead of teleporting
            // This avoids jumpy client-side movements
        } catch (Exception ignored) {
            // Fallback if packet sending fails
        }
    }

    private int getAlcoholLevel(Player player) {
        Integer alcohol = player.getPersistentDataContainer().get(FreedomKeys.alcohol(), PersistentDataType.INTEGER);
        return alcohol == null ? 0 : alcohol;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        swayStates.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void PrePlayerAttackEvent(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().has(FreedomKeys.alcohol())) {
            try {
                int alcohollevel = player.getPersistentDataContainer().get(FreedomKeys.alcohol(), PersistentDataType.INTEGER);
                if (alcohollevel >= 4) {
                    if (event.getPlayer().getInventory().getItemInMainHand() == null
                            &&
                        event.getPlayer().getInventory().getItemInOffHand() == null) {
                        player.getAttribute(Attribute.ATTACK_DAMAGE).removeModifier(FreedomKeys.alcohol());
                        player.getAttribute(Attribute.ATTACK_DAMAGE).addModifier(new AttributeModifier(FreedomKeys.alcohol(),8 + (alcohollevel*2), AttributeModifier.Operation.ADD_NUMBER));

                    } else {
                        player.getAttribute(Attribute.ATTACK_DAMAGE).removeModifier(FreedomKeys.alcohol());
                    }

                }
            }catch (Exception ignored) {}
        }
    }

}
