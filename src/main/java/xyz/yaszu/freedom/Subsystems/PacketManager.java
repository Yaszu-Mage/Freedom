package xyz.yaszu.freedom.Subsystems;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChangeGameState;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTimeUpdate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PacketManager extends PacketListenerAbstract {

    private static final String TEAM_NAME_PREFIX = "hide_";
    private static final Set<UUID> sudoPlayers = Collections.synchronizedSet(new HashSet<>());





    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
            WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata(event);
            int entityId = wrapper.getEntityId();

            Player targetPlayer = null;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getEntityId() == entityId) {
                    targetPlayer = p;
                    break;
                }
            }

            if (targetPlayer != null && sudoPlayers.contains(targetPlayer.getUniqueId())) {
                List<EntityData<?>> metadata = new ArrayList<>(wrapper.getEntityMetadata());
                boolean found = false;
                for (EntityData<?> data : metadata) {
                    // Index 3 is "is custom name visible" for 1.21 (Player entities)
                    if (data.getIndex() == 3) {
                        ((EntityData<Boolean>) data).setValue(false);
                        found = true;
                    }
                }
                if (!found) {
                    metadata.add(new EntityData(3, EntityDataTypes.BOOLEAN, false));
                }
                wrapper.setEntityMetadata(metadata);
            }
        } else if (event.getPacketType() == PacketType.Play.Server.TEAMS) {
            WrapperPlayServerTeams wrapper = new WrapperPlayServerTeams(event);
            Optional<WrapperPlayServerTeams.ScoreBoardTeamInfo> infoOpt = wrapper.getTeamInfo();
            if (infoOpt.isPresent()) {
                WrapperPlayServerTeams.ScoreBoardTeamInfo info = infoOpt.get();
                // If this team contains any sudo player, ensure visibility is NEVER
                boolean containsSudo = false;
                Collection<String> players = wrapper.getPlayers();
                if (players != null) {
                    for (String entry : players) {
                        Player p = Bukkit.getPlayerExact(entry);
                        if (p != null && sudoPlayers.contains(p.getUniqueId())) {
                            containsSudo = true;
                            break;
                        }
                    }
                }

                if (containsSudo) {
                    info.setTagVisibility(WrapperPlayServerTeams.NameTagVisibility.NEVER);
                }
            }
        }
    }

    public static void updateSudoStatus(Player player, boolean isSudo) {
        if (isSudo) {
            sudoPlayers.add(player.getUniqueId());
            sendTeamPacket(player, true);
        } else {
            sudoPlayers.remove(player.getUniqueId());
            sendTeamPacket(player, false);
        }
    }

    public static void sendAllSudoTeams(Player observer) {
        for (UUID uuid : sudoPlayers) {
            Player sudoPlayer = Bukkit.getPlayer(uuid);
            if (sudoPlayer != null) {
                PacketEvents.getAPI().getPlayerManager().sendPacket(observer, createPacket(sudoPlayer, true));
            }
        }
    }

    private static void sendTeamPacket(Player sudoPlayer, boolean hide) {
        WrapperPlayServerTeams packet = createPacket(sudoPlayer, hide);
        for (Player online : Bukkit.getOnlinePlayers()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(online, packet);
        }
    }

    private static WrapperPlayServerTeams createPacket(Player player, boolean hide) {
        String teamName = TEAM_NAME_PREFIX + player.getName();
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);

        if (hide) {
            WrapperPlayServerTeams.ScoreBoardTeamInfo info = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
                    Component.text(teamName),
                    Component.empty(),
                    Component.empty(),
                    WrapperPlayServerTeams.NameTagVisibility.NEVER,
                    WrapperPlayServerTeams.CollisionRule.NEVER,
                    NamedTextColor.WHITE,
                    WrapperPlayServerTeams.OptionData.NONE
            );
            return new WrapperPlayServerTeams(
                    teamName,
                    WrapperPlayServerTeams.TeamMode.CREATE,
                    Optional.of(info),
                    Collections.singletonList(player.getName())
            );
        } else {
            return new WrapperPlayServerTeams(
                    teamName,
                    WrapperPlayServerTeams.TeamMode.REMOVE,
                    Optional.empty(),
                    Collections.emptyList()
            );
        }
    }

    public enum SkyType {
        NORMAL(0.0f, 0.0f, 6000L),
        RAIN(1.0f, 0.0f, 6000L),
        THUNDER(0.0f, 1.0f, 18000L),
        END(0.0f, 1.0f, 18000L),
        NETHER(0.0f, 1.0f, 12000L);

        private final float rainLevel;
        private final float thunderLevel;
        private final long time;

        SkyType(float rainLevel, float thunderLevel, long time) {
            this.rainLevel = rainLevel;
            this.thunderLevel = thunderLevel;
            this.time = time;
        }

        public float getRainLevel() {
            return rainLevel;
        }

        public float getThunderLevel() {
            return thunderLevel;
        }

        public long getTime() {
            return time;
        }
    }

    public static void setSky(Player player, SkyType type) {
        setSkyColor(player, WrapperPlayServerChangeGameState.Reason.RAIN_LEVEL_CHANGE, type.getRainLevel());
        setSkyColor(player, WrapperPlayServerChangeGameState.Reason.THUNDER_LEVEL_CHANGE, type.getThunderLevel());
        
        // Change time client-side for better look
        WrapperPlayServerTimeUpdate timePacket = new WrapperPlayServerTimeUpdate(player.getWorld().getFullTime(), type.getTime(), false);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, timePacket);
    }

    public static void setSkyColor(Player player, SkyType type, float value) {
        setSkyColor(player, WrapperPlayServerChangeGameState.Reason.RAIN_LEVEL_CHANGE, value);
    }

    public static void setSkyColor(Player player, WrapperPlayServerChangeGameState.Reason reason, float value) {
        WrapperPlayServerChangeGameState packet = new WrapperPlayServerChangeGameState(reason, value);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }
}
