package com.oldust.core.staff.punish;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Punishment {
    private final int banId;
    private final PunishmentType type;
    private final UUID punishedUuid;
    private final String punisherName;
    private final String reason;
    private final Timestamp date;
    @Nullable
    private final Timestamp expiration;
}
