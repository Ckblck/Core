package com.oldust.core;

import com.oldust.core.utils.CUtils;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {
    @Getter private static Core instance;

    @Override
    public void onEnable() {
        CUtils.log("Core", "Inicializando core...");
        instance = this;
    }

    @Override
    public void onDisable() {

    }

}
