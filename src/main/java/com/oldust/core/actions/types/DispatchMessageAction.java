package com.oldust.core.actions.types;

import com.oldust.core.Core;
import com.oldust.core.actions.Action;
import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.core.utils.lambda.SerializablePredicate;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;

public class DispatchMessageAction extends Action<DispatchMessageAction> {
    private final Channel channel;
    private final SerializablePredicate<PlayerRank> rankRequirement;
    private final boolean usesTextComponent;
    private final String message;
    private final Sound sound;
    private final float volume, pitch;

    /**
     * Construye un mensaje que se envía al servidor local o globalmente.
     *
     * @param channel           canal, el cual especifica a qué servidor se enviará el mensaje
     * @param rankRequirement   predicado que permite funcionalmente decidir a qué rangos se enviará el mensaje
     * @param usesTextComponent true si message proviene de {@link ComponentSerializer#toString(BaseComponent...)}
     * @param message           mensaje común, como por ej: '#fffff &nHola', o producto de {@link ComponentSerializer#toString(BaseComponent...)}
     * @param sound             sonido el cual mandar al momento del mensaje
     * @param volume            volumen
     * @param pitch             pitch
     */

    public DispatchMessageAction(Channel channel, SerializablePredicate<PlayerRank> rankRequirement, boolean usesTextComponent, String message, Sound sound, float volume, float pitch) {
        super(ActionsReceiver.PREFIX);

        this.channel = channel;
        this.rankRequirement = rankRequirement;
        this.usesTextComponent = usesTextComponent;
        this.message = message;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public DispatchMessageAction(Channel channel, SerializablePredicate<PlayerRank> rankRequirement, boolean usesTextComponent, String message) {
        this(channel, rankRequirement, usesTextComponent, message, null, -1, -1);
    }

    public DispatchMessageAction(Channel channel, SerializablePredicate<PlayerRank> rankRequirement, String message) {
        this(channel, rankRequirement, false, message, null, -1, -1);
    }

    @Override
    protected void execute() {
        boolean shouldBroadcast = channel == Channel.NETWORK_WIDE || channel.serverName.equalsIgnoreCase(Core.getInstance().getServerName());

        if (!shouldBroadcast) return;
        Collection<? extends Player> players = PlayerUtils.getPlayers();

        for (Player player : players) {
            WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player.getUniqueId());
            PlayerRank playerRank = database.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);

            boolean applies = rankRequirement.test(playerRank);

            if (applies) {
                if (sound != null) {
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }

                if (usesTextComponent) {
                    try {
                        BaseComponent[] components = ComponentSerializer.parse(message);

                        player.spigot().sendMessage(components);
                    } catch (Exception e) {
                        CUtils.inform("Action", "Not JSON encoded message was sent, 'usesTextComponent' should be false then.");
                    }
                } else {
                    CUtils.msg(player, message);
                }

            }
        }

    }

    @RequiredArgsConstructor
    public enum Channel {
        SERVER_WIDE(Core.getInstance().getServerName()),
        NETWORK_WIDE("*");

        private final String serverName;
    }

}
