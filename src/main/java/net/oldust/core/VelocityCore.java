package net.oldust.core;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import lombok.Getter;
import net.oldust.core.actions.ActionsReceiver;
import net.oldust.core.actions.reliable.ack.AckReceiver;
import net.oldust.core.utils.CUtils;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.jedis.JedisManager;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Dummy class for the load of the Core
 * in a Velocity environment.
 */

@Getter
@Plugin(id = "oldustcore", name = "OldustCore", version = "1.0.0-SNAPSHOT",
        url = "https://oldust.net", authors = "Ckblck")
public class VelocityCore implements OldustPlugin {
    @Getter private static VelocityCore instance;

    private final Logger logger;
    private final AckReceiver ackReceiver;

    @Inject
    public VelocityCore(Logger logger, @DataDirectory Path dataDirectory) {
        CUtils.IS_PROXY = true;

        instance = this;
        Core.setMultiplatformPlugin(this);

        new JedisManager(new File(dataDirectory.toFile(), "redis_credentials.yml"));
        new PlayerManager();
        new ActionsReceiver();

        this.logger = logger;
        this.ackReceiver = null;
    }

}
