package com.oldust.core.ranks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;

@Getter
@RequiredArgsConstructor
public enum PlayerRank {

    ADMIN(ChatColor.of("#ff3019") + "ADMIN ", 1000, 0),
    MOD(ChatColor.of("#0f60ff") + "MOD ", 500, 1),
    BUILDER(ChatColor.of("#baa988") + "BUILDER ", 200, 2),
    USER(ChatColor.of("#88baa7") + "", 0, 3);

    private static final PlayerRank[] VALUES = values();
    private final String prefix;
    private final int priority;
    private final int databaseId;

    public static PlayerRank getById(int id) {
        for (PlayerRank rank : VALUES) {
            if (rank.databaseId == id) return rank;
        }

        throw new IllegalArgumentException("No se pudo encontrar rango para la ID proporcionada: " + id);
    }

    public boolean isEqualOrHigher(PlayerRank rank) {
        return this.priority >= rank.priority;
    }

}
