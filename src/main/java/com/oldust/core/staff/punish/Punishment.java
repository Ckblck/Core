package com.oldust.core.staff.punish;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Punishment implements Serializable {
    private final int punishmentId;
    private final PunishmentType type;
    private final UUID punishedUuid;
    private final String punisherName;
    private final String reason;
    private final Timestamp date;
    @Nullable
    private final Timestamp expiration;
    @Nullable
    private final String ip;

    @Getter
    public static class ExpiredPunishment extends Punishment {
        private final String unpunishedBy;
        private final Timestamp unpunishedAt;

        public ExpiredPunishment(Punishment punishment, @Nullable String unbannedBy, @Nullable Timestamp unbannedAt) {
            super(punishment.getPunishmentId(), punishment.getType(), punishment.getPunishedUuid(),
                    punishment.getPunisherName(), punishment.getReason(), punishment.getDate(),
                    punishment.getExpiration(), punishment.getIp());

            this.unpunishedBy = unbannedBy;
            this.unpunishedAt = unbannedAt;
        }

        public ExpiredPunishment(int banId, PunishmentType type, UUID punishedUuid, String punisherName,
                                 String reason, Timestamp date, @Nullable Timestamp expiration,
                                 @Nullable String ipAddress, @Nullable String unbannedBy, @Nullable Timestamp unbannedAt) {
            super(banId, type, punishedUuid, punisherName, reason, date, expiration, ipAddress);

            this.unpunishedBy = unbannedBy;
            this.unpunishedAt = unbannedAt;
        }
    }

}
