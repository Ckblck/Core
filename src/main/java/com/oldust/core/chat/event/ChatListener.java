package com.oldust.core.chat.event;

import com.oldust.core.Core;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.ServerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.OldustServer;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        if (chatMuted() && !PlayerRank.getPlayerRank(player).isStaff()) {
            e.setCancelled(true);
            CUtils.msg(player, Lang.ERROR_COLOR + "The chat is muted!");

            return;
        }

        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player.getUniqueId());
        Optional<WrappedPlayerDatabase.WrappedValue> optRank = database.getValueOptional(PlayerDatabaseKeys.RANK);

        optRank.ifPresentOrElse(playerRank -> {
            PlayerRank rank = playerRank.asClass(PlayerRank.class);

            String format = rank.getPrefix()
                    + "%s"
                    + ChatColor.of("#fcba03")
                    + " » "
                    + ChatColor.RESET
                    + "%s";

            e.setFormat(format);
        }, () -> player.kickPlayer(Lang.DB_DISAPPEARED));

    }

    private boolean chatMuted() {
        OldustServer currentServer = Core.getInstance().getServerManager().getCurrentServer();

        return currentServer.getValue(ServerDatabaseKeys.MUTED).asBoolean();
    }

}
