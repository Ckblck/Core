package com.oldust.core.ranks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PlayerRank {

    ADMIN(ChatColor.of("#ff3019") + "ADMIN ", 500, 3),
    MOD(ChatColor.of("#0f60ff") + "MOD ", 20, 2),
    BUILDER(ChatColor.of("#baa988") + "BUILDER ", 10, 1),
    USER(ChatColor.of("#88baa7") + "", 0, 0);

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

    public static PlayerRank getByName(String name) {
        return Arrays.stream(VALUES)
                .filter(rank -> rank.name().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No se pudo encontrar rango para el nombre proporcionado: " + name));
    }

    public boolean isEqualOrHigher(PlayerRank rank) {
        return this.priority >= rank.priority;
    }

}
