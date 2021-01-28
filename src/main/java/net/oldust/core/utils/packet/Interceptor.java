package net.oldust.core.utils.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.ImmutableSet;
import net.oldust.core.Core;

import java.util.Set;

public class Interceptor {

    private static final Set<PacketType> DISABLED_PACKETS = ImmutableSet.of(
            PacketType.Play.Client.FLYING,
            PacketType.Play.Client.POSITION,
            PacketType.Play.Client.POSITION_LOOK,
            PacketType.Play.Client.KEEP_ALIVE,
            PacketType.Play.Client.LOOK,
            PacketType.Play.Client.CLOSE_WINDOW,
            PacketType.Play.Client.ENTITY_ACTION,
            PacketType.Play.Client.ARM_ANIMATION,
            PacketType.Play.Client.BLOCK_DIG,
            PacketType.Play.Client.BLOCK_PLACE,
            PacketType.Play.Client.CLIENT_COMMAND,
            PacketType.Play.Client.CHAT,
            PacketType.Play.Client.TAB_COMPLETE,
            PacketType.Play.Client.SETTINGS,
            PacketType.Play.Client.TRANSACTION,
            PacketType.Play.Client.ABILITIES,
            PacketType.Play.Client.USE_ENTITY,
            PacketType.Play.Client.HELD_ITEM_SLOT
    );

    private static final Set<PacketType> DISABLED_PACKETS_CLIENT_SIDE = ImmutableSet.of(
            PacketType.Play.Server.CHAT,
            PacketType.Play.Server.UNLOAD_CHUNK,
            PacketType.Play.Server.BLOCK_CHANGE,
            PacketType.Play.Server.LIGHT_UPDATE,
            PacketType.Play.Server.TAB_COMPLETE,
            PacketType.Play.Server.ABILITIES,
            PacketType.Play.Server.EXPERIENCE,
            PacketType.Play.Server.RECIPES,
            PacketType.Play.Server.RECIPE_UPDATE,
            PacketType.Play.Server.VIEW_CENTRE,
            PacketType.Play.Server.REMOVE_ENTITY_EFFECT,
            PacketType.Play.Server.MULTI_BLOCK_CHANGE,
            PacketType.Play.Server.ENTITY_EFFECT,
            PacketType.Play.Server.ENTITY_STATUS,
            PacketType.Play.Server.KEEP_ALIVE,
            PacketType.Play.Server.REL_ENTITY_MOVE,
            PacketType.Play.Server.UPDATE_HEALTH,
            PacketType.Play.Server.UPDATE_ATTRIBUTES,
            PacketType.Play.Server.UPDATE_TIME,
            PacketType.Play.Server.WORLD_PARTICLES,
            PacketType.Play.Server.ENTITY_HEAD_ROTATION,
            PacketType.Play.Server.GAME_STATE_CHANGE,
            PacketType.Play.Server.ENTITY_LOOK,
            PacketType.Play.Server.PLAYER_INFO,
            PacketType.Play.Server.NAMED_SOUND_EFFECT,
            PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
            PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
            PacketType.Play.Server.ANIMATION,
            PacketType.Play.Server.ENTITY_TELEPORT,
            PacketType.Play.Server.EXPERIENCE,
            PacketType.Play.Server.OPEN_WINDOW,
            PacketType.Play.Server.ENTITY_VELOCITY,
            PacketType.Play.Server.ENTITY_METADATA,
            PacketType.Play.Server.STATISTIC,
            PacketType.Play.Server.MAP_CHUNK_BULK,
            PacketType.Play.Server.MAP_CHUNK,
            PacketType.Play.Server.TRANSACTION,
            PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER,
            PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER
    );

    public Interceptor() {
        start();
    }

    public void start() {
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(Core.getInstance(), ListenerPriority.MONITOR, PacketType.values()) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (DISABLED_PACKETS.contains(event.getPacketType())) return;

                        System.out.println("Client sent packet - " + event.getPacketType().name());
                    }

                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (DISABLED_PACKETS_CLIENT_SIDE.contains(event.getPacketType())) return;

                        System.out.println("Server sent packet - " + event.getPacketType().name());
                    }
                });
    }

}
