package net.oldust.core.staff.punish;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.oldust.core.staff.punish.types.BanPunishment;
import net.oldust.core.staff.punish.types.KickPunishment;
import net.oldust.core.staff.punish.types.MutePunishment;
import net.oldust.core.staff.punish.types.Punishable;

@Getter
@RequiredArgsConstructor
public enum PunishmentType {
    BAN(new BanPunishment()), KICK(new KickPunishment()), MUTE(new MutePunishment());

    private final Punishable<?> handler;
}
