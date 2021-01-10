package net.oldust.core.staff.punish.types;

import net.oldust.core.staff.punish.Punishment;
import net.oldust.core.utils.Async;

import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Async
public interface Punishable<T extends Punishment> {

    boolean punish(String punisherName, String punishedName, TemporalAmount duration, String reason, boolean banIp);

    boolean removePunishment(String punishedName);

    boolean hasActivePunishment(UUID punishedUuid);

    Optional<Punishment> currentPunishment(UUID punishedUuid);

    String getPunishmentMessage(Punishment punishment);

    List<T> fetchPunishments(String punishedName);

}
