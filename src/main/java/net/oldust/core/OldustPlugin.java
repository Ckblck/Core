package net.oldust.core;

import net.oldust.core.actions.reliable.ack.AckReceiver;

/**
 * Instances that are used cross-platform.
 * Example: the {@link AckReceiver} is both used at server
 * and proxy (Velocity).
 */

public interface OldustPlugin {
    AckReceiver getAckReceiver();
}
