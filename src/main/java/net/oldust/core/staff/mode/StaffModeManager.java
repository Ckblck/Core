package net.oldust.core.staff.mode;

import net.oldust.core.Core;
import net.oldust.core.commons.internal.EventsProvider;
import net.oldust.core.commons.internal.Operation;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Encargado de administrar
 * lo relacionado a Staff Mode y vanish.
 * <p>
 * El Staff Mode es guardado en la base del datos
 * del jugador mientras lo tenga activado.
 * Si el jugador cambia de servidor, se borrará localmente
 * en el mapa {@link #staffs}. De esta forma podemos mantener una instancia
 * de {@link StaffMode} en su {@link WrappedPlayerDatabase}.
 * Si el jugador deja el servidor, su base de datos será borrada.
 */

public class StaffModeManager implements Listener {
    private final Map<UUID, StaffMode> staffs = new HashMap<>();
    private final Set<UUID> vanished = new HashSet<>();

    public StaffModeManager() {
        for (Player player : PlayerUtils.getPlayers()) {
            WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player);
            boolean vanished = database.contains(PlayerDatabaseKeys.VANISH);
            boolean staffMode = database.contains(PlayerDatabaseKeys.STAFF_MODE);

            if (vanished) {
                vanish(player);
            }

            if (staffMode) {
                setStaffMode(player, database);
            }

        }

        joinEvent();

        CUtils.registerEvents(this);
    }

    public void setStaffMode(Player player, WrappedPlayerDatabase database) {
        boolean staffMode = database.contains(PlayerDatabaseKeys.STAFF_MODE);
        StaffMode mode;

        if (staffMode) { // Cambió de server, tiene la instancia guardada en su DB.
            mode = database.getValue(PlayerDatabaseKeys.STAFF_MODE).asClass(StaffMode.class);
            mode.init(this, player, database);
        } else {
            mode = new StaffMode(this, player, database);
        }

        staffs.put(player.getUniqueId(), mode);
    }

    /**
     * Utilizado para salirse del modo staff
     * completamente. Este método borra tanto
     * en Redis como en el server (localmente).
     */

    public void exitStaffMode(Player player, WrappedPlayerDatabase database) {
        boolean staffMode = database.contains(PlayerDatabaseKeys.STAFF_MODE);

        if (staffMode) {
            StaffMode mode = database.getValue(PlayerDatabaseKeys.STAFF_MODE).asClass(StaffMode.class);
            mode.exit(player, database);
        }

        deleteStaffModeLocally(player);
    }

    /**
     * Utilizado para borrar el modo staff
     * localmente. Utilizado en el momento de cambiar de servidores,
     * ya que no queremos borrar de su redis.
     */

    public void deleteStaffModeLocally(Player player) {
        staffs.remove(player.getUniqueId());
        vanished.remove(player.getUniqueId());
    }

    public void vanish(Player player) {
        PlayerManager playerManager = PlayerManager.getInstance();
        Collection<? extends Player> list = PlayerUtils.getPlayers();

        for (Player otherPlayer : list) {
            otherPlayer.hidePlayer(Core.getInstance(), player);
        }

        vanished.add(player.getUniqueId());

        WrappedPlayerDatabase database = playerManager.getDatabase(player);
        database.put(PlayerDatabaseKeys.VANISH, true);

        playerManager.update(database);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !vanished.contains(player.getUniqueId())) {
                    cancel();

                    return;
                }

                player.sendActionBar(CUtils.color("#a7ab8c You are currently #e0895a vanished#a7ab8c."));
            }
        }.runTaskTimerAsynchronously(Core.getInstance(), 0, 20);

    }

    public void unvanish(Player player) {
        PlayerManager playerManager = PlayerManager.getInstance();
        Collection<? extends Player> list = PlayerUtils.getPlayers();

        for (Player otherPlayer : list) {
            otherPlayer.showPlayer(Core.getInstance(), player);
        }

        vanished.remove(player.getUniqueId());

        WrappedPlayerDatabase database = playerManager.getDatabase(player);
        database.remove(PlayerDatabaseKeys.VANISH);

        playerManager.update(database);

    }

    public void joinEvent() {
        EventsProvider provider = Core.getInstance().getEventsProvider();

        provider.newOperation(PlayerJoinEvent.class, new Operation<PlayerJoinEvent>((join, db) -> {
            Player player = join.getPlayer();

            boolean vanished = db.contains(PlayerDatabaseKeys.VANISH);
            boolean staffMode = db.contains(PlayerDatabaseKeys.STAFF_MODE);

            if (vanished) {
                vanish(player);
            }

            this.vanished.stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(vanishedPl -> player.hidePlayer(Core.getInstance(), vanishedPl));

            if (staffMode) {
                setStaffMode(player);
            }

        }));

        provider.newOperation(PlayerQuitEvent.class, new Operation<PlayerQuitEvent>((quit, db) -> {
            deleteStaffModeLocally(quit.getPlayer()); // Borramos localmente, no de Redis.
        }));

    }

    private void setStaffMode(Player player) {
        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player);

        setStaffMode(player, database);
    }

    public void switchState(Player player) {
        boolean vanished = this.vanished.contains(player.getUniqueId());

        if (vanished) {
            unvanish(player);

            CUtils.msg(player, Lang.SUCCESS_COLOR + "You are no longer vanished.");
        } else {
            vanish(player);

            CUtils.msg(player, Lang.SUCCESS_COLOR + "You are currently vanished.");
        }

    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        if (hasStaffMode(player)) {
            e.setCancelled(true);
            CUtils.msg(player, Lang.ERROR_COLOR + "You can't break any blocks while in Staff Mode!");
        }
    }

    @EventHandler
    public void onHit(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && hasStaffMode(e.getEntity())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player && hasStaffMode(e.getEntity())) {
            e.setCancelled(true);
        }
    }

    private boolean hasStaffMode(Entity player) {
        return staffs.containsKey(player.getUniqueId());
    }

}
