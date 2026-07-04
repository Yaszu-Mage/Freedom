package xyz.yaszu.freedom.Subsystems;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;
import xyz.yaszu.freedom.Freedom;

import java.util.List;
import java.util.UUID;

import static xyz.yaszu.freedom.Util.Util.dess;

/**
 * Wraps a genuine, network-less {@link ServerPlayer} so ordinary Bukkit
 * plugins (anything listening for PlayerMoveEvent, EntityDamageEvent, etc.)
 * cannot tell it apart from a real connected client. It isn't a disguise
 * layered on top of some other entity - from the server's point of view,
 * this IS a player, the same way a Geyser-bridged Bedrock client or a
 * Citizens2 "playerlist" NPC is a player.
 *
 * PROTOTYPE. Written against Paper 1.20.2+ / 1.21.x Mojang mappings via
 * paperweight-userdev. The class/method names on Connection,
 * ClientInformation, CommonListenerCookie, and PlayerList have changed
 * across Minecraft versions before and will again - if this doesn't
 * compile as-is on your build, it's almost certainly a version mismatch,
 * not a logic error. This has NOT been compiled against real Paper
 * internals; treat it as a verified-architecture, unverified-signatures
 * starting point.
 *
 * Requires the paperweight-userdev Gradle plugin (or equivalent) so these
 * net.minecraft.* classes resolve at compile time - a plain paper-api
 * dependency alone will not expose them.
 */
public class FakePlayerHandle {

    private final ServerPlayer nmsPlayer;
    private final Player bukkitPlayer;
    private volatile boolean removed = false;


    public  Location getLocation(){
        return bukkitPlayer.getLocation();
    }
    public Chunk getChunk(){
        return bukkitPlayer.getLocation().getChunk();
    }
    public boolean isDead() {
        return bukkitPlayer.isDead();
    }
    public Vector getVelocity() {
        return bukkitPlayer.getVelocity();
    }
    public List<Entity> getNearbyEntities(double rx, double ry, double rz) {
        return bukkitPlayer.getNearbyEntities(rx, ry, rz);
    }
    public void moveFakePlayer(double x, double y, double z, float yRot, float xRot) {
//        nmsPlayer.moveOrInterpolateTo(new Vec3(x, y, z));
//        nmsPlayer.setKnownMovement(new Vec3(x,y,z));
//        nmsPlayer.move(MoverType.PLAYER, new Vec3(Math.clamp(x,-1,1),Math.clamp(y,-1,1),Math.clamp(z,-1,1)));
        nmsPlayer.teleportTo(x,y,z);
        ServerboundMovePlayerPacket.PosRot packet = new ServerboundMovePlayerPacket.PosRot(x, y, z, yRot, xRot, isOnGround(x,y,z), false);
        nmsPlayer.connection.handleMovePlayer(packet);
    }

    /**
     * Respawns the FakePlayer
     * @param Removalreason Reason for removal
     * @param RespawnReason Reson for respawn
     */
    public void respawnPlayer(net.minecraft.world.entity.Entity.RemovalReason Removalreason, PlayerRespawnEvent.RespawnReason RespawnReason) {
        ServerLevel world = nmsPlayer.level();
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        nmsServer.getPlayerList().respawn(nmsPlayer,true,Removalreason,RespawnReason);
    }
    private static final double GROUND_PROBE_DEPTH = 0.05;
    private boolean isOnGround(double x, double y, double z) {
        AABB feet = nmsPlayer.getBoundingBox()
                .move(x - nmsPlayer.getX(), y - nmsPlayer.getY(), z - nmsPlayer.getZ())
                .inflate(-0.01, 0, -0.01);
        AABB probe = new AABB(feet.minX, y - GROUND_PROBE_DEPTH, feet.minZ,
                feet.maxX, y, feet.maxZ);
        return !nmsPlayer.level().noCollision(nmsPlayer, probe);
    }

    public InteractionResult fakePlayerInteract(boolean isRight,Vec3 location) {
        nmsPlayer.swing(isRight ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);
        BlockHitResult hitResult = new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.UP, new BlockPos((int) location.x,(int) location.y,(int) location.z),false);
        nmsPlayer.gameMode.useItemOn(nmsPlayer, nmsPlayer.gameMode.level, ItemStack.fromBukkitCopy(bukkitPlayer.getInventory().getItemInMainHand()),isRight ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,hitResult);
        return nmsPlayer.interact(nmsPlayer,isRight ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,location);
    }

    public FakePlayerHandle(String displayName, Location spawnLocation) {
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel nmsWorld = ((CraftWorld) spawnLocation.getWorld()).getHandle();

        // Deterministic UUID per bridged name, so a Roblox player who leaves
        // and rejoins maps back to the same profile instead of a new one
        // each session (matters for anything keyed off UUID, e.g. PDC-backed
        // stats, permissions, econ balances).
        GameProfile profile = new GameProfile(
                UUID.nameUUIDFromBytes(("Roblox:" + displayName).getBytes()),
                displayName
        );

        Connection fakeConnection = new Connection(PacketFlow.SERVERBOUND);
//        {
//            @Override
//            public void send(Packet<?> packet) {
//                // No real client on the other end - discard outbound packets.
//                if (packet instanceof ClientboundPlayerPositionPacket positionPacket) {
//                    nmsPlayer.connection.handleAcceptTeleportPacket(
//                            new ServerboundAcceptTeleportationPacket(positionPacket.id())
//                    );
//                }
//                if (packet instanceof ClientboundResourcePackPushPacket popPacket) {
//                    nmsPlayer.connection.handleResourcePackResponse(
//                            new ServerboundResourcePackPacket(nmsPlayer.getUUID(), ServerboundResourcePackPacket.Action.DOWNLOADED)
//                    );
//                }
//
//            }
////
////            @Override
////            public void send(Packet<?> packet, PacketSendListener listener) {
////                // Still satisfy anything waiting on the send callback.
////                if (listener != null) listener.onSuccess();
////            }
//        };
        // Attach to a real (in-memory only, never touches a socket) Netty
        // channel instead of leaving the Connection totally unbound - some
        // server-side logging and anti-cheat code reads the remote address
        // back off the channel and will NPE on a null one otherwise.
        new EmbeddedChannel(fakeConnection);
        Channel channel = fakeConnection.channel;
        nmsPlayer = new ServerPlayer(nmsServer, nmsWorld, profile, ClientInformation.createDefault());
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        nmsPlayer.connection = new ServerGamePacketListenerImpl(nmsServer, fakeConnection, nmsPlayer, cookie);
        // Runs the same code path a genuine login goes through: registers
        // the player in the level, adds it to the tab list, fires
        // PlayerJoinEvent. NOTE: Bukkit's AsyncPlayerPreLoginEvent and
        // PlayerLoginEvent are NOT fired by this - it skips past the
        // connection-gate checks entirely. If other plugins (whitelist,
        // ban lists, join-throttling) depend on those specifically, fire
        // them yourself before calling this constructor.
        nmsServer.getPlayerList().placeNewPlayer(fakeConnection, nmsPlayer, cookie);

        this.bukkitPlayer = nmsPlayer.getBukkitEntity();
        this.bukkitPlayer.teleport(spawnLocation);
        this.bukkitPlayer.setGameMode(GameMode.SURVIVAL);
        channel.pipeline().addLast(displayName, new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
                // Client -> Server (Serverbound)
                readIncomingPacket(bukkitPlayer, packet);
                super.channelRead(ctx, packet);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                // Server -> Client (Clientbound)
                readOutgoingPacket(bukkitPlayer, packet);
                super.write(ctx, packet, promise);
            }
            private void readIncomingPacket(Player player, Object packet) {
                // Client -> Server (Serverbound)

            }
            private void readOutgoingPacket(Player player, Object packet) {
                // Server -> Client (Clientbound)
                if (packet instanceof ClientboundPlayerPositionPacket positionPacket) {
                    nmsPlayer.connection.handleAcceptTeleportPacket(
                            new ServerboundAcceptTeleportationPacket(positionPacket.id())
                    );
                }

            }
        });
    }

    public Player bukkit() {
        return bukkitPlayer;
    }

    public boolean isRemoved() {
        return removed || bukkitPlayer.isDead();
    }

    /**
     * Tears the fake player down the same way a real disconnect would.
     * Call this instead of entity.remove() - the same class of bug as the
     * earlier missing removeContext() applies here: skip the real teardown
     * path and the profile/UUID stays half-registered, breaking the next
     * session in a new way.
     */
    public void remove() {
        if (removed) return;
        removed = true;
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        nmsPlayer.disconnect();
        nmsPlayer.connection.disconnect(dess("Fake player removed"));
//        nmsServer.getPlayerList().remove(nmsPlayer);

    }
}