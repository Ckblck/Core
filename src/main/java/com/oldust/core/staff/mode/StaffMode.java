package com.oldust.core.staff.mode;

import com.oldust.core.Core;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.core.utils.interactive.InteractivePanel;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.ServerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.ServerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.OldustServer;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StaffMode {
    private final UUID player;

    public StaffMode(Player player, WrappedPlayerDatabase database) {
        this.player = player.getUniqueId();

        init(player, database);
    }

    private void init(Player player, WrappedPlayerDatabase database) {
        InteractivePanel panel = new InteractivePanel(player);

        panel.add(0, ModeItems.STAFF_TOOLS, (click) -> new StaffToolsInv(Bukkit.getPlayer(this.player), this).open());

        panel.add(1, ModeItems.RANDOM_TELEPORT, (click) -> randomTeleport(Bukkit.getPlayer(this.player)));

        panel.add(4, ModeItems.STICK_ANTI_KB, (click) -> {
        });
        panel.add(8, ModeItems.EXIT, (click) -> exit(Bukkit.getPlayer(this.player), database));

        panel.enter(player);

        database.put(PlayerDatabaseKeys.STAFF_MODE, panel);
        PlayerManager.getInstance().saveDatabase(database);
    }

    public void exit(Player player, WrappedPlayerDatabase database) {
        InteractivePanel panel = database.getValue(PlayerDatabaseKeys.STAFF_MODE).asClass(InteractivePanel.class);
        panel.exit(player);

        database.remove(PlayerDatabaseKeys.STAFF_MODE);
        PlayerManager.getInstance().update(database);
    }

    public void muteChat(Player staff) {
        ServerManager serverManager = Core.getInstance().getServerManager();
        OldustServer server = serverManager.getCurrentServer();

        boolean muted = server.getValue(ServerDatabaseKeys.MUTED).asBoolean();
        String message = (muted) ? "unmuted" : "muted";

        server.put(ServerDatabaseKeys.MUTED, !muted);
        serverManager.update(server);

        CUtils.msg(staff, Lang.SUCCESS_COLOR + "The server has been " + message + ".");
    }

    public void randomTeleport(Player player) {
        PlayerUtils.getPlayers().stream()
                .filter(randomPlayer -> randomPlayer.getUniqueId() != this.player)
                .findAny()
                .ifPresentOrElse(player::teleport, () -> CUtils.msg(player, Lang.ERROR_COLOR + "There aren't any players!"));
    }

}
