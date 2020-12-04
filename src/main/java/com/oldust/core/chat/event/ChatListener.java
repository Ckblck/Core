package com.oldust.core.chat.event;

import com.oldust.core.Core;
import com.oldust.core.actions.types.DispatchMessageAction;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.sync.JedisManager;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.ServerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.OldustServer;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String playerName = player.getName();
        String msg = e.getMessage();

        if (chatMuted() && !PlayerRank.getPlayerRank(player).isStaff()) {
            e.setCancelled(true);
            CUtils.msg(player, Lang.ERROR_COLOR + "The chat is muted!");

            return;
        }

        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player.getUniqueId());
        boolean staffChat = database.contains(PlayerDatabaseKeys.STAFF_CHAT);

        if (staffChat) {
            e.setCancelled(true);

            BaseComponent[] base = new ComponentBuilder("[SC]")
                    .color(ChatColor.of("#329ea8"))
                    .italic(true)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(database.getBungeeServer())))
                    .append(" ").reset()
                    .append(playerName).color(ChatColor.of("#80918a"))
                    .append(" » ").color(ChatColor.of("#fcba03"))
                    .append(CUtils.color(msg)).reset().create();

            String serialized = ComponentSerializer.toString(base);

            new DispatchMessageAction(DispatchMessageAction.Channel.SERVER_WIDE, PlayerRank::isStaff, true, serialized, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.5F, 1F)
                    .push(JedisManager.getInstance().getPool());

            return;
        }

        Optional<WrappedPlayerDatabase.WrappedValue> optRank = database.getValueOptional(PlayerDatabaseKeys.RANK);

        optRank.ifPresentOrElse(playerRank -> {
            PlayerRank rank = playerRank.asClass(PlayerRank.class);

            String format = rank.getPrefix()
                    + "%s"
                    + ChatColor.of("#fcba03")
                    + " » "
                    + ChatColor.RESET
                    + "%s";

            e.setFormat((rank.isEqualOrHigher(PlayerRank.ADMIN)) ? CUtils.color(format) : format);
        }, () -> player.kickPlayer(Lang.DB_DISAPPEARED));

    }

    private boolean chatMuted() {
        OldustServer currentServer = Core.getInstance().getServerManager().getCurrentServer();

        return currentServer.getValue(ServerDatabaseKeys.MUTED).asBoolean();
    }

}
