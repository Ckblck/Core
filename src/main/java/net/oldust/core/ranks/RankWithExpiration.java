package net.oldust.core.ranks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;

@Getter
@RequiredArgsConstructor
public class RankWithExpiration {
    private final PlayerRank rank;
    private final Timestamp expiresAt;
}