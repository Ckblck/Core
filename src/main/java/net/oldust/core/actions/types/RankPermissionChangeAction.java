package net.oldust.core.actions.types;

import com.google.common.base.Preconditions;
import net.oldust.core.actions.Action;
import net.oldust.core.actions.ActionsReceiver;
import net.oldust.core.inherited.plugins.InheritedPluginsManager;
import net.oldust.core.permission.PermissionsManager;
import net.oldust.core.permission.RankPermissions;
import net.oldust.core.ranks.PlayerRank;
import org.jetbrains.annotations.Nullable;

/**
 * Acción ejecutada al momento
 * de un cambio de permisos en un rango.
 */

public class RankPermissionChangeAction extends Action<RankPermissionChangeAction> {
    private final Method method;
    private final PlayerRank rank;
    private final PlayerRank newParent;
    private final String permission;
    private final boolean enabled;

    public RankPermissionChangeAction(Method method, PlayerRank rank, @Nullable PlayerRank newParent, @Nullable String permission, boolean enabled) {
        super(ActionsReceiver.PREFIX);

        this.method = method;
        this.rank = rank;
        this.newParent = newParent;
        this.permission = permission;
        this.enabled = enabled;
    }

    @Override
    protected void execute() {
        PermissionsManager manager = InheritedPluginsManager.getPlugin(PermissionsManager.class);
        RankPermissions permissions = manager.getPermissions(rank);

        switch (method) {
            case SET:
                Preconditions.checkNotNull(permissions);

                permissions.updatePermission(permission, enabled);

                break;
            case UNSET:
                Preconditions.checkNotNull(permissions);

                permissions.removePermission(permission);

                break;
            case SET_PARENT:
                Preconditions.checkNotNull(newParent);

                permissions.updateParent(newParent);

                break;
            case CLEAR_PARENTS:
                permissions.updateParent(null);

                break;
        }
    }

    public enum Method {
        SET, UNSET, SET_PARENT, CLEAR_PARENTS
    }

}
