package com.oldust.core.actions.types;

import com.oldust.core.actions.Action;
import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SendToServerAction extends Action<SendToServerAction> {
    private final String player;
    private final String server;

    public SendToServerAction(String player, String server) {
        super(ActionsReceiver.PREFIX);

        this.player = player;
        this.server = server;
    }

    @Override
    protected void execute() {
        Player player = Bukkit.getPlayer(this.player);

        if (player != null) {
            PlayerUtils.sendToServer(player, server);
        }

    }

}
