package net.oldust.core;

import net.md_5.bungee.api.plugin.Plugin;
import net.oldust.core.utils.CUtils;
import net.oldust.sync.JedisManager;
import net.oldust.sync.PlayerManager;

/**
 * Clase dummy para el cargado
 * de la dependencia del core
 * en Bungee.
 */

public class BungeeCore extends Plugin {

    @Override
    public void onEnable() {
        CUtils.IS_BUNGEE = true;

        new JedisManager();
        new PlayerManager();
    }

}
