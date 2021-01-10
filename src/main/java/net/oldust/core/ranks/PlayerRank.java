package net.oldust.core.ranks;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.oldust.core.utils.Async;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public enum PlayerRank {

    ADMIN(ChatColor.of("#ff3019") + "ADMIN ", 500, 3),
    MOD(ChatColor.of("#0f60ff") + "MOD ", 80, 2),
    BUILDER(ChatColor.of("#baa988") + "BUILDER ", 50, 1),
    USER(ChatColor.of("#88baa7") + "", 0, 0);

    public static final PlayerRank[] VALUES = values();
    private static final long serialVersionUID = 45346357647452345L;
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

    @Async
    public static PlayerRank getPlayerRank(String playerName) {
        CUtils.warnSyncCall();

        Player player = Bukkit.getPlayer(playerName);

        if (player != null) {
            return getPlayerRank(player);
        }

        UUID uuid = PlayerUtils.getUUIDByName(playerName);
        List<RankWithExpiration> ranks = PlayerUtils.getRanks(uuid.toString());

        return ranks.get(0).getRank();
    }

    public static PlayerRank getPlayerRank(CommandSender sender) {
        if (sender instanceof Player) {
            return getPlayerRank((Player) sender);
        }

        return ADMIN;
    }

    private static PlayerRank getPlayerRank(Player sender) {
        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(sender);

        return database.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);
    }

    public boolean isEqualOrHigher(PlayerRank rank) {
        return this.priority >= rank.priority;
    }

    public boolean isStaff() {
        return this == ADMIN || this == MOD || this == BUILDER;
    }

    public void setTabPrefix(Player player) {
        String prefix = CUtils.color(getPrefix() + ((this == USER) ? "" : "&r"));

        player.setPlayerListName(prefix + player.getName());
    }

}
