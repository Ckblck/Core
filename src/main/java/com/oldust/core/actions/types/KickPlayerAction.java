package com.oldust.core.actions.types;

import com.oldust.core.actions.Action;
import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.utils.CUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Acción que expulsa el jugador del servidor.
 * Se recomienda comprobar previamente si aquel jugador
 * está conectado en el servidor para evitar un
 * procesamiento innecesario.
 */

public class KickPlayerAction extends Action<KickPlayerAction> {
    private final String player;
    private final String reason;

    public KickPlayerAction(String player, String reason) {
        super(ActionsReceiver.PREFIX);

        this.player = player;
        this.reason = reason;
    }

    @Override
    protected void execute() {
        Player player = Bukkit.getPlayer(this.player);

        if (player == null) return;

        CUtils.runSync(() -> player.kickPlayer(reason));
    }

}
