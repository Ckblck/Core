package net.oldust.core.actions.types;

import net.oldust.core.actions.Action;
import net.oldust.core.actions.ActionsReceiver;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lang.LangSound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class SendPlayerMessageAction extends Action<SendPlayerMessageAction> {
    private final String playerName;
    private final String message;
    @Nullable private final LangSound sound;

    public SendPlayerMessageAction(String playerName, String message) {
        super(ActionsReceiver.PREFIX);

        this.playerName = playerName;
        this.message = CUtils.color(message);
        this.sound = null;
    }

    public SendPlayerMessageAction(String playerName, String message, @Nullable LangSound sound) {
        super(ActionsReceiver.PREFIX);

        this.playerName = playerName;
        this.message = CUtils.color(message);
        this.sound = sound;
    }

    @Override
    protected void execute() {
        Player player = Bukkit.getPlayer(playerName);

        if (player != null) {
            player.sendMessage(message);

            if (sound != null) {
                sound.play(player);
            }

        }

    }

}
