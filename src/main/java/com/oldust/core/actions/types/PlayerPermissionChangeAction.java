package com.oldust.core.actions.types;

import com.oldust.core.actions.Action;
import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.ranks.permission.BukkitCompat;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.WrappedPlayerDatabase;
import org.bukkit.Bukkit;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * Acción ejecutada al momento
 * de un cambio de permisos en un jugador.
 */

public class PlayerPermissionChangeAction extends Action<PlayerPermissionChangeAction> {
    private final UUID player;
    private final String permission;
    private final boolean enabled;

    public PlayerPermissionChangeAction(UUID player, String permission, boolean enabled) {
        super(ActionsReceiver.PREFIX);

        this.player = player;
        this.permission = permission;
        this.enabled = enabled;
    }

    @Override
    protected void execute() {
        if (!PlayerUtils.isConnected(player)) return;

        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player);
        Map<String, Boolean> permissions = database.getValue(PlayerDatabaseKeys.PERSONAL_PERMISSIONS).asMap(String.class, Boolean.class);

        permissions.put(permission, enabled); // Actualizamos con los nuevos.
        database.put(PlayerDatabaseKeys.PERSONAL_PERMISSIONS, (Serializable) permissions);

        BukkitCompat.setPermissions(Bukkit.getPlayer(player), permissions);
        PlayerManager.getInstance().update(database);
    }

}
