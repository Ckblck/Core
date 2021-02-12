package net.oldust.core;

import net.md_5.bungee.api.plugin.Plugin;
import net.oldust.core.actions.ActionsReceiver;
import net.oldust.core.actions.reliable.ack.AckReceiver;
import net.oldust.core.utils.CUtils;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.jedis.JedisManager;

/**
 * Dummy class for the load of the Core
 * in a Bungee environment.
 */

public class BungeeCore extends Plugin implements OldustPlugin {
    private AckReceiver ackReceiver;

    @Override
    public void onEnable() {
        CUtils.IS_PROXY = true;
        Core.setMultiplatformPlugin(this);

        new JedisManager();
        new PlayerManager();
        new ActionsReceiver();

        this.ackReceiver = new AckReceiver();
    }

    @Override
    public AckReceiver getAckReceiver() {
        return ackReceiver;
    }

}
