package com.oldust.core.staff.mode;

import com.oldust.core.Core;
import com.oldust.core.inherited.plugins.InheritedPluginsManager;
import com.oldust.core.interactive.components.InteractiveComponent;
import com.oldust.core.interactive.components.creator.EasyTextComponent;
import com.oldust.core.interactive.panels.InteractiveItem;
import com.oldust.core.interactive.panels.InteractiveListener;
import com.oldust.core.interactive.panels.InteractivePanel;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.ServerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.ServerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.OldustServer;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.Serializable;
import java.util.UUID;

public class StaffMode implements Serializable, Listener {
    private static final long serialVersionUID = 453634506934853L;
    private static final transient TextComponent RETURN_COMPONENT = new EasyTextComponent()
            .setText("&6« &eExit Spectator &6»")
            .runCommand(new InteractiveComponent((pl) -> pl.setGameMode(GameMode.SURVIVAL)).create())
            .showText("&6« &r&lCLICK &6»\n&8&m&l           \n\n&7In order to switch back your gamemode.\n&7You can also click the &9'Tools' &7item.")
            .getComponent();

    private final UUID player;

    private final GameMode previousGamemode;
    private final boolean previousAllowFlight;
    private final boolean previousFlying;

    public StaffMode(Player player, WrappedPlayerDatabase database) {
        this.player = player.getUniqueId();
        this.previousGamemode = player.getGameMode();
        this.previousAllowFlight = player.getAllowFlight();
        this.previousFlying = player.isFlying();

        init(player, database);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        if (isSamePlayer(player)) {
            e.setCancelled(true);
            CUtils.msg(player, Lang.ERROR_COLOR + "You can't break any block while in Staff Mode!");
        }
    }

    @EventHandler
    public void onHit(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && isSamePlayer(e.getEntity())) {
            e.setCancelled(true);
        }
    }

    private void init(Player staff, WrappedPlayerDatabase database) {
        InteractivePanel panel = new InteractivePanel(staff);

        panel.add(0, ModeItems.STAFF_TOOLS, (click) -> new StaffToolsInv(Bukkit.getPlayer(this.player), this).open());

        panel.add(1, ModeItems.RANDOM_TELEPORT, (click) -> randomTeleport(Bukkit.getPlayer(this.player)));

        panel.add(4, ModeItems.STICK_ANTI_KB, (click) -> {
        });

        panel.add(8, ModeItems.EXIT, (click) -> exit(Bukkit.getPlayer(this.player), database));

        panel.addListener(new InteractiveListener() {
            @Override
            public void onInventoryClick(Player player, InteractiveItem item) {
                if (item.getSlot() == 0) player.setGameMode(GameMode.SURVIVAL);
            }
        });

        staff.setGameMode(GameMode.SURVIVAL);
        staff.setAllowFlight(true);
        staff.setFlying(true);

        panel.enter(staff);
        vanish(staff);

        database.put(PlayerDatabaseKeys.STAFF_MODE, panel);
        PlayerManager.getInstance().saveDatabase(database);
        CUtils.registerEvents(this);
    }

    public void exit(Player staff, WrappedPlayerDatabase database) {
        InteractivePanel panel = database.getValue(PlayerDatabaseKeys.STAFF_MODE).asClass(InteractivePanel.class);
        panel.exit(staff);

        staff.removePotionEffect(PotionEffectType.NIGHT_VISION);
        unvanish(staff);

        staff.setGameMode(previousGamemode);
        staff.setAllowFlight(previousAllowFlight);
        staff.setFlying(previousFlying);

        database.remove(PlayerDatabaseKeys.STAFF_MODE);
        PlayerManager.getInstance().update(database);

        CUtils.unregisterEvents(this);
    }

    public void muteChat(Player staff) {
        ServerManager serverManager = Core.getInstance().getServerManager();
        OldustServer server = serverManager.getCurrentServer();

        boolean muted = server.getValue(ServerDatabaseKeys.MUTED).asBoolean();
        String message = (muted) ? "unmuted" : "muted";

        server.put(ServerDatabaseKeys.MUTED, !muted);
        serverManager.update(server);

        CUtils.msg(staff, Lang.SUCCESS_COLOR + "The chat has been " + message + ".");
    }

    public void switchMode(Player staff) {
        GameMode gameMode = (staff.getGameMode() == GameMode.SPECTATOR) ? GameMode.SURVIVAL : GameMode.SPECTATOR;
        staff.setGameMode(gameMode);

        staff.spigot().sendMessage(RETURN_COMPONENT);
    }

    public void randomTeleport(Player staff) {
        PlayerUtils.getPlayers().stream()
                .filter(randomPlayer -> randomPlayer.getUniqueId() != this.player)
                .findAny()
                .ifPresentOrElse(staff::teleport, () -> CUtils.msg(staff, Lang.ERROR_COLOR + "There aren't any players!"));
    }

    public void switchVision(Player staff) {
        if (staff.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            staff.removePotionEffect(PotionEffectType.NIGHT_VISION);
        } else {
            staff.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 1, false, false));
        }
    }

    public void createChest(Player staff) {
        StaffPlugin plugin = InheritedPluginsManager.getPlugin(StaffPlugin.class);
        boolean success = plugin.getFakeChestsManager().newChest(staff.getLocation().getBlock());

        if (success) {
            CUtils.msg(staff, Lang.SUCCESS_COLOR + "Successfully placed fake chest.");
        } else {
            CUtils.msg(staff, Lang.ERROR_COLOR + "Could not place fake chest.");
        }

    }

    public void vanish(Player staff) {
        StaffPlugin plugin = InheritedPluginsManager.getPlugin(StaffPlugin.class);
        plugin.getVanishHandler().vanish(staff);
    }

    public void unvanish(Player staff) {
        StaffPlugin plugin = InheritedPluginsManager.getPlugin(StaffPlugin.class);
        plugin.getVanishHandler().unvanish(staff);
    }

    private boolean isSamePlayer(Entity entity) {
        return entity.getUniqueId().equals(this.player);
    }

}
