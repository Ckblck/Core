package net.oldust.core.actions.types;

import net.oldust.core.Core;
import net.oldust.core.actions.Action;
import net.oldust.core.actions.ActionsReceiver;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;

import java.util.UUID;

/**
 * Acción enviada al momento en el que un jugador
 * entra a un servidor de Oldust. Su función es
 * cachear localmente la base de datos para que el método
 * {@link net.oldust.sync.PlayerManager#getDatabase(UUID)} no retorne null.
 */

public class MandatoryCacheWrappedAction extends Action<MandatoryCacheWrappedAction> {
    private final String desiredServer;
    private final WrappedPlayerDatabase database;

    public MandatoryCacheWrappedAction(String desiredServer, WrappedPlayerDatabase database) {
        super(ActionsReceiver.PREFIX);

        this.desiredServer = desiredServer;
        this.database = database;
    }

    @Override
    protected void execute() {
        boolean thisServer = Core.getInstance().getServerName().equalsIgnoreCase(desiredServer);

        if (thisServer) {
            PlayerManager.getInstance().cacheDatabase(database);
        }

    }

}
