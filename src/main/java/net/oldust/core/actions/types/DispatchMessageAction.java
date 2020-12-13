package net.oldust.core.actions.types;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.oldust.core.Core;
import net.oldust.core.actions.Action;
import net.oldust.core.actions.ActionsReceiver;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.core.utils.lambda.SerializablePredicate;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Una clase que utiliza {@link SerializablePredicate}
 * con genérico {@link WrappedPlayerDatabase}
 * brindando así la posibilidad de utilizar la base de datos del jugador
 * con diversos y amplios motivos.
 * Ejemplo, esta acción se puede emplear al estilo de canales, y enviar
 * mensajes a un grupo determinado que contiene una específica {@link PlayerDatabaseKeys}.
 */

public class DispatchMessageAction extends Action<DispatchMessageAction> {
    private final Channel channel;
    private final SerializablePredicate<WrappedPlayerDatabase> requirement;
    private final boolean usesTextComponent;
    private final String message;
    private final Sound sound;
    private final float volume, pitch;

    /**
     * Construye un mensaje que se envía al servidor local o globalmente.
     *
     * @param channel           canal, el cual especifica a qué servidor se enviará el mensaje
     * @param requirement       predicado que permite funcionalmente decidir a qué personas se enviará el mensaje
     * @param usesTextComponent true si message proviene de {@link ComponentSerializer#toString(BaseComponent...)}
     * @param message           mensaje común, como por ej: '#fffff &nHola', o producto de {@link ComponentSerializer#toString(BaseComponent...)}
     * @param sound             sonido el cual mandar al momento del mensaje
     * @param volume            volumen
     * @param pitch             pitch
     */

    public DispatchMessageAction(Channel channel, SerializablePredicate<WrappedPlayerDatabase> requirement, boolean usesTextComponent, String message, Sound sound, float volume, float pitch) {
        super(ActionsReceiver.PREFIX);

        this.channel = channel;
        this.requirement = requirement;
        this.usesTextComponent = usesTextComponent;
        this.message = message;
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public DispatchMessageAction(Channel channel, SerializablePredicate<WrappedPlayerDatabase> requirement, boolean usesTextComponent, String message) {
        this(channel, requirement, usesTextComponent, message, null, -1, -1);
    }

    public DispatchMessageAction(Channel channel, SerializablePredicate<WrappedPlayerDatabase> requirement, String message) {
        this(channel, requirement, false, message, null, -1, -1);
    }

    @Override
    protected void execute() {
        boolean shouldBroadcast = channel == Channel.NETWORK_WIDE || channel.serverName.equalsIgnoreCase(Core.getInstance().getServerName());

        if (!shouldBroadcast) return;
        Collection<? extends Player> players = PlayerUtils.getPlayers();

        for (Player player : players) {
            WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player.getUniqueId());

            boolean applies = requirement.test(database);

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
