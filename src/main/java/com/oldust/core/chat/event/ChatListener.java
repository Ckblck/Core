package com.oldust.core.chat.event;

import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.Lang;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.WrappedPlayerDatabase;
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

        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player.getUniqueId());
        Optional<WrappedPlayerDatabase.WrappedValue> optRank = database.getValueOptional(PlayerDatabaseKeys.RANK);

        optRank.ifPresentOrElse(playerRank -> {
            PlayerRank rank = playerRank.asClass(PlayerRank.class);

            String format = rank.getPrefix()
                    + player.getName()
                    + ChatColor.of("#fcba03")
                    + " » ";

            e.setFormat(format + ChatColor.RESET + e.getMessage());
        }, () -> player.kickPlayer(Lang.DB_DISAPPEARED));

    }

}
