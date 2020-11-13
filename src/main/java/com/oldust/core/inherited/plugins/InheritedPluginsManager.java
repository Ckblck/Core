package com.oldust.core.inherited.plugins;

import com.oldust.core.utils.CUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase encargada de el manejo
 * de plugins internos del Core.
 */

public class InheritedPluginsManager {
    private static final Map<String, Plugin> plugins = new HashMap<>();

    public static void loadInheritedPlugin(Class<? extends Plugin> plugin) {
        InheritedPlugin pluginAnnotation = plugin.getAnnotation(InheritedPlugin.class);

        if (pluginAnnotation == null) {
            CUtils.inform("INHPlugins", plugin.getSimpleName() + " carece de la anotación @InheritedPlugin.");

            return;
        }

        String name = pluginAnnotation.name();

        try {
            Plugin inheritedPlugin = plugin.getDeclaredConstructor().newInstance();
            plugins.putIfAbsent(name, inheritedPlugin);
        } catch (Exception e) {
            CUtils.inform("INHPlugins", "Ocurrió un error al cargar el plugin interno: " + name);
            e.printStackTrace();
        }

    }

    public static void onEnable() {
        plugins.values().forEach(Plugin::onEnable);
    }

    public static void onDisable() {
        plugins.values().forEach(Plugin::onDisable);
    }

}
