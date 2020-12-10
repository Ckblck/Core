package com.oldust.core.staff.punish;

import com.oldust.core.utils.Lang;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.UUID;

@Getter
@ToString
@RequiredArgsConstructor
public class Punishment implements Serializable {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a", Locale.ENGLISH);

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

    public String formatExpiration() {
        if (expiration == null) {
            return Lang.ERROR_COLOR + "never";
        } else {
            return DATE_FORMAT.format(expiration);
        }
    }

    public String formatDate() {
        return DATE_FORMAT.format(date);
    }

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
