package net.oldust.core;

import net.md_5.bungee.api.plugin.Plugin;

/**
 * Clase dummy para el cargado
 * de la dependencia del core
 * en Bungee.
 */

public class BungeeCore extends Plugin {
    public static boolean IS_BUNGEE;

    @Override
    public void onEnable() {
        IS_BUNGEE = true;
    }

}
