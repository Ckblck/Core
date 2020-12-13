package net.oldust.core.actions.types;

import net.oldust.core.Core;
import net.oldust.core.actions.Action;
import net.oldust.core.actions.ActionsReceiver;
import net.oldust.core.utils.CUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

public class DispatchCommandAction extends Action<DispatchCommandAction> {
    private final String sender;
    private final String serverName;
    private final String command;

    public DispatchCommandAction(String sender, String serverName, String... command) {
        super(ActionsReceiver.PREFIX);

        this.sender = sender;
        this.serverName = serverName;
        this.command = StringUtils.join(command, " ");
    }

    @Override
    protected void execute() {
        if (serverName.equals("*") || serverName.equalsIgnoreCase(Core.getInstance().getServerName())) {
            CUtils.runSync(() -> Bukkit.dispatchCommand(Core.getInstance().getServer().getConsoleSender(), command));
            CUtils.inform("Dispatch", "Se ha ejecutado el comando /" + command + " a pedido de " + sender + " correctamente.");
        }
    }

}
