package com.oldust.core.staff.chest;

import com.oldust.core.actions.types.DispatchMessageAction;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.lambda.SerializablePredicate;
import com.oldust.sync.JedisManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class FakeChest {
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

        SerializablePredicate<PlayerRank> predicate = (rank) -> rank.isEqualOrHigher(PlayerRank.MOD);

        new DispatchMessageAction(DispatchMessageAction.Channel.SERVER_WIDE, predicate, message).push(JedisManager.getInstance().getPool());
    }

}
