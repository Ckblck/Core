package com.oldust.core.chat.event;

import com.oldust.core.Core;
import com.oldust.core.actions.types.DispatchMessageAction;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.staff.punish.Punishment;
import com.oldust.core.staff.punish.PunishmentType;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.lang.Lang;
import com.oldust.sync.JedisManager;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.Savable;
import com.oldust.sync.wrappers.ServerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.OldustServer;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String playerName = player.getName();
        String msg = e.getMessage();

        // Comprobar si el chat general está silenciado.

        if (chatMuted() && !PlayerRank.getPlayerRank(player).isStaff()) {
            e.setCancelled(true);
            CUtils.msg(player, Lang.ERROR_COLOR + "The chat is muted!");

            return;
        }

        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player.getUniqueId());
        boolean staffChat = database.contains(PlayerDatabaseKeys.STAFF_CHAT);

        // Comprobar si el jugador tiene el chat del Staff activado.

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

            new DispatchMessageAction(DispatchMessageAction.Channel.SERVER_WIDE, db -> {
                PlayerRank rank = db.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);

                return rank.isStaff();
            }, true, serialized, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.5F, 1F)
                    .push(JedisManager.getInstance().getPool());

            return;
        }

        // Comprobar que el jugador no esté silenciado.

        Optional<Savable.WrappedValue> muted = database.getValueOptional(PlayerDatabaseKeys.MUTE_DURATION);

        if (muted.isPresent()) {
            Punishment punishment = muted.get().asClass(Punishment.class);
            Timestamp expires = punishment.getExpiration();

            assert expires != null;

            if (expires.before(new Timestamp(System.currentTimeMillis()))) {
                database.remove(PlayerDatabaseKeys.MUTE_DURATION);
                PlayerManager.getInstance().update(database);
            } else {
                String message = PunishmentType.MUTE.getHandler().getPunishmentMessage(punishment);
                CUtils.msg(player, message);

                e.setCancelled(true);

                return;
            }

        }

        Optional<WrappedPlayerDatabase.WrappedValue> optRank = database.getValueOptional(PlayerDatabaseKeys.RANK);

        optRank.ifPresentOrElse(playerRank -> {
            PlayerRank rank = playerRank.asClass(PlayerRank.class);

            // Comprobar si envió un mensaje muy rápido o repetidamente en un lapso de 0.5 segundos

            Optional<Savable.WrappedValue> lastMessage = database.getValueOptional(PlayerDatabaseKeys.LAST_MESSAGE);

            if (lastMessage.isPresent() && rank == PlayerRank.USER) {
                ChatMessage chatMessage = lastMessage.get().asClass(ChatMessage.class);
                String message = chatMessage.message;

                long dateSec = TimeUnit.MILLISECONDS.toSeconds(chatMessage.date);
                long nowSec = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

                if (nowSec - dateSec <= 0.5) {
                    CUtils.msg(player, Lang.ERROR_COLOR + "You cannot send messages too quickly!");
                    e.setCancelled(true);

                    return;
                }

                if (nowSec - dateSec <= 3 && StringUtils.getJaroWinklerDistance(e.getMessage(), message) * 100 > 80) { // Similitud mayor al 80%
                    CUtils.msg(player, Lang.ERROR_COLOR + "You cannot send the same message in a short period of time!");
                    e.setCancelled(true);

                    return;
                }

            }

            // Darle formato al mensaje

            String format = rank.getPrefix()
                    + "%s"
                    + ChatColor.of("#fcba03")
                    + " » "
                    + ChatColor.RESET
                    + "%s";

            e.setFormat(format);

            database.put(PlayerDatabaseKeys.LAST_MESSAGE, new ChatMessage(e.getMessage()));
            PlayerManager.getInstance().update(database);

            Bukkit.broadcastMessage(CUtils.color(e.getMessage())); // TODO: Remove
        }, () -> player.kickPlayer(Lang.DB_DISAPPEARED));

    }

    private boolean chatMuted() {
        OldustServer currentServer = Core.getInstance().getServerManager().getCurrentServer();

        return currentServer.getValue(ServerDatabaseKeys.MUTED).asBoolean();
    }

    @RequiredArgsConstructor
    private static class ChatMessage implements Serializable {
        private final String message;
        private final long date = System.currentTimeMillis();
    }

}
