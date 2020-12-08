package com.oldust.core.actions.types;

import com.oldust.core.actions.Action;
import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.utils.CUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SendPlayerMessageAction extends Action<SendPlayerMessageAction> {
    private final String playerName;
    private final String message;

    public SendPlayerMessageAction(String playerName, String message) {
        super(ActionsReceiver.PREFIX);

        this.playerName = playerName;
        this.message = CUtils.color(message);
    }

    @Override
    protected void execute() {
        Player player = Bukkit.getPlayer(playerName);

        if (player != null) {
            player.sendMessage(message);
        }

    }

}
