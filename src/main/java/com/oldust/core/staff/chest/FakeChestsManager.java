package com.oldust.core.staff.chest;

import com.oldust.core.utils.CUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class FakeChestsManager implements Listener {
    private final Map<Location, FakeChest> fakeChests = new HashMap<>();

    public FakeChestsManager() {
        CUtils.registerEvents(this);
    }

    public boolean newChest(Block block) {
        Location location = block.getLocation();
        boolean contains = fakeChests.containsKey(location);

        if (contains) {
            return false;
        }

        FakeChest fakeChest = new FakeChest(location);
        fakeChest.place();

        fakeChests.put(location, fakeChest);

        return true;
    }

    @EventHandler
    public void onBreak(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (!e.hasBlock()) return;

        Block block = e.getClickedBlock();
        assert block != null;

        Location location = block.getLocation();

        if (block.getType() == Material.CHEST && fakeChests.containsKey(location)) {
            FakeChest fakeChest = fakeChests.get(location);
            fakeChest.alert(player);

            fakeChests.remove(location);
        }

    }

}
