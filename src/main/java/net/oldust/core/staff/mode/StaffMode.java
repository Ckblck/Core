package net.oldust.core.staff.mode;

import net.md_5.bungee.api.chat.TextComponent;
import net.oldust.core.Core;
import net.oldust.core.inherited.plugins.InheritedPluginsManager;
import net.oldust.core.interactive.components.InteractiveComponent;
import net.oldust.core.interactive.components.creator.EasyTextComponent;
import net.oldust.core.interactive.panels.InteractiveItem;
import net.oldust.core.interactive.panels.InteractiveListener;
import net.oldust.core.interactive.panels.InteractivePanel;
import net.oldust.core.interactive.panels.InteractivePanelManager;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.ServerManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.ServerDatabaseKeys;
import net.oldust.sync.wrappers.defaults.OldustServer;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.Serializable;
import java.util.UUID;

public class StaffMode implements Serializable {
    private static final TextComponent RETURN_COMPONENT = new EasyTextComponent()
            .setText("&6« &eExit Spectator &6»")
            .runCommand(new InteractiveComponent((pl) -> pl.setGameMode(GameMode.CREATIVE)).create())
            .showText("&6« &r&lCLICK &6»\n&8&m&l           \n\n&7In order to switch back your gamemode.\n&7You can also click the &9'Tools' &7item.")
            .getComponent();

    private final UUID player;

    private final GameMode previousGamemode;
    private final boolean previousAllowFlight;
    private final boolean previousFlying;

    protected StaffMode(StaffModeManager manager, Player player, WrappedPlayerDatabase database) {
        this.player = player.getUniqueId();
        this.previousGamemode = player.getGameMode();
        this.previousAllowFlight = player.getAllowFlight();
        this.previousFlying = player.isFlying();

        init(manager, player, database);
    }

    public void init(StaffModeManager manager, Player staff, WrappedPlayerDatabase database) {
        PlayerManager playerManager = PlayerManager.getInstance();
        InteractivePanel panel = new InteractivePanel(staff);

        panel.add(0, ModeItems.STAFF_TOOLS, (click) -> new StaffToolsInv(Bukkit.getPlayer(player), this).open());

        panel.add(1, ModeItems.RANDOM_TELEPORT, (click) -> randomTeleport(Bukkit.getPlayer(player)));

        panel.add(4, ModeItems.STICK_ANTI_KB, (click) -> {
        });

        panel.add(8, ModeItems.EXIT, (click) -> {
            Player player = Bukkit.getPlayer(this.player);

            manager.exitStaffMode(player, playerManager.getDatabase(this.player));
        });

        panel.addListener(new InteractiveListener() {
            @Override
            public void onInventoryClick(Player player, InteractiveItem item) {
                if (item.getSlot() == 0) player.setGameMode(GameMode.SURVIVAL);
            }
        });

        Bukkit.getScheduler().runTaskLater(Core.getInstance(), () -> {
            staff.setGameMode(GameMode.CREATIVE);
            staff.setAllowFlight(true);
            staff.setFlying(true);
        }, 3);

        InteractivePanelManager.getInstance().setPanel(staff, panel);

        database.put(PlayerDatabaseKeys.STAFF_MODE, this);
        playerManager.update(database);

        vanish(staff);
    }

    protected void exit(Player staff, WrappedPlayerDatabase database) {
        database.remove(PlayerDatabaseKeys.STAFF_MODE);

        PlayerManager.getInstance().update(database);

        staff.removePotionEffect(PotionEffectType.NIGHT_VISION);
        staff.getInventory().clear();

        InteractivePanelManager.getInstance().exitPanel(staff);

        staff.setGameMode(previousGamemode);
        staff.setAllowFlight(previousAllowFlight);
        staff.setFlying(previousFlying);

        unvanish(staff);
    }

    public void muteChat(Player staff) {
        ServerManager serverManager = Core.getInstance().getServerManager();
        OldustServer server = serverManager.getCurrentServer();

        boolean muted = server.getValue(ServerDatabaseKeys.MUTED).asBoolean();
        String message = (muted) ? "unmuted" : "muted";

        CUtils.runAsync(() -> {
            server.put(ServerDatabaseKeys.MUTED, !muted);
            serverManager.update(server);
        });

        CUtils.msg(staff, Lang.SUCCESS_COLOR + "The chat has been " + message + ".");
    }

    public void switchMode(Player staff) {
        GameMode gameMode = (staff.getGameMode() == GameMode.SPECTATOR) ? GameMode.CREATIVE : GameMode.SPECTATOR;
        staff.setGameMode(gameMode);

        staff.spigot().sendMessage(RETURN_COMPONENT);
    }

    public void randomTeleport(Player staff) {
        PlayerUtils.getPlayers().stream()
                .filter(randomPlayer -> randomPlayer.getUniqueId() != this.player)
                .findAny()
                .ifPresentOrElse(staff::teleport,
                        () -> CUtils.msg(staff, Lang.ERROR_COLOR + "There aren't any players!", LangSound.ERROR));
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
            CUtils.msg(staff, Lang.ERROR_COLOR + "Could not place fake chest.", LangSound.ERROR);
        }

    }

    public void vanish(Player staff) {
        StaffPlugin plugin = InheritedPluginsManager.getPlugin(StaffPlugin.class);

        plugin.getStaffModeManager().vanish(staff);
    }

    public void unvanish(Player staff) {
        StaffPlugin plugin = InheritedPluginsManager.getPlugin(StaffPlugin.class);

        plugin.getStaffModeManager().unvanish(staff);
    }

}
