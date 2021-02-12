package net.oldust.core.staff.chest;

import lombok.RequiredArgsConstructor;
import net.oldust.core.actions.types.DispatchMessageAction;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.SerializablePredicate;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.jedis.JedisManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class FakeChest {
    private static final SerializablePredicate<WrappedPlayerDatabase> PREDICATE = (db) -> {
        PlayerRank rank = db.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);

        return rank.isEqualOrHigher(PlayerRank.MOD);
    };

    private final Location chestLocation;
    private BlockData blockData;

    protected void place() {
        Block prevBlock = chestLocation.getBlock();
        blockData = prevBlock.getBlockData();

        prevBlock.setType(Material.CHEST);
    }

    protected void remove(Player player) {
        Block block = chestLocation.getBlock();

        block.setType(Material.AIR);
        block.setBlockData(blockData);

        CUtils.msg(player, Lang.SUCCESS_COLOR + "The fake chest has been removed.");
    }

    protected void alert(Player player) {
        String message = "#696969 (#f5c000 ALERT#696969) " + Lang.ARROW + "#f5c000 "
                + player.getName()
                + " has opened a fake chest.";

        new DispatchMessageAction(DispatchMessageAction.Channel.SERVER_WIDE, PREDICATE, message).push(JedisManager.getInstance().getPool());
    }

}
