package com.oldust.core.staff.punish.types;

import com.oldust.core.staff.punish.Punishment;

import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.UUID;

public interface Punishable {

    boolean punish(String punisherName, String punishedName, TemporalAmount duration, String reason, boolean banIp);

    boolean removePunishment(String punishedName);

    boolean hasActivePunishment(UUID punishedUuid);

    Optional<Punishment> currentPunishment(UUID punishedUuid);

    String getPunishmentMessage(Punishment punishment);

}
