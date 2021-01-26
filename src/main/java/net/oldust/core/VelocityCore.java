package net.oldust.core;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import net.oldust.core.utils.CUtils;
import net.oldust.sync.JedisManager;
import net.oldust.sync.PlayerManager;

/**
 * Dummy class for the load of the Core
 * in a Velocity environment.
 */

@Plugin(id = "oldustcore", name = "OldustCore", version = "1.0.0-SNAPSHOT",
        url = "https://oldust.net", authors = "Ckblck")
public class VelocityCore {

    @Inject
    public VelocityCore() {
        CUtils.IS_PROXY = true;

        new JedisManager();
        new PlayerManager();
    }

}
