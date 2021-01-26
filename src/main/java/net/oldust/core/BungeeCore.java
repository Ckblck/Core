package net.oldust.core;

import net.md_5.bungee.api.plugin.Plugin;
import net.oldust.core.utils.CUtils;
import net.oldust.sync.JedisManager;
import net.oldust.sync.PlayerManager;

/**
 * Dummy class for the load of the Core
 * in a Bungee environment.
 */

public class BungeeCore extends Plugin {

    @Override
    public void onEnable() {
        CUtils.IS_PROXY = true;

        new JedisManager();
        new PlayerManager();
    }

}
