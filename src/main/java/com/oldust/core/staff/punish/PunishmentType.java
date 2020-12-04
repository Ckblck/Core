package com.oldust.core.staff.punish;

import com.oldust.core.staff.punish.types.BanPunishment;
import com.oldust.core.staff.punish.types.Punishable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PunishmentType {
    BAN(new BanPunishment()), KICK(new BanPunishment()), MUTE(new BanPunishment());

    private final Punishable handler;
}
