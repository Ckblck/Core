package com.oldust.core.ranks.permission;

import com.oldust.core.Core;
import org.bukkit.entity.Player;
import org.bukkit.permissions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This code is distributed for your use and modification. Do what you like with
 * it, but credit me for the original!
 * <p>
 * Also I'd be interested to see what you do with it.
 *
 * @author codename_B
 */
public class BukkitCompat {

    public static PermissionAttachment setPermissions(Permissible p, Map<String, Boolean> perm) {
        try {
            return doBukkitPermissions(p, perm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PermissionAttachment doBukkitPermissions(Permissible p, Map<String, Boolean> permissions) {
        Core plugin = Core.getInstance();
        Player player = (Player) p;

        // Bukkit does some weird shit where they make a tree and the root is the player's name
        // This code removes any permissions that were already set in the tree
        Permission positive = plugin.getServer().getPluginManager().getPermission(player.getName());
        Permission negative = plugin.getServer().getPluginManager().getPermission("^" + player.getName());

        if (positive != null) {
            plugin.getServer().getPluginManager().removePermission(positive);
        }

        if (negative != null) {
            plugin.getServer().getPluginManager().removePermission(negative);
        }

        Map<String, Boolean> po = new HashMap<>();
        Map<String, Boolean> ne = new HashMap<>();

        for (String key : permissions.keySet()) {
            if (permissions.get(key)) {
                po.put(key, true);
            } else {
                ne.put(key, false);
            }
        }

        positive = new Permission(player.getName(), PermissionDefault.FALSE, po);
        negative = new Permission("^" + player.getName(), PermissionDefault.FALSE, ne);

        // Re-establish our tree with the root node as the player's name
        plugin.getServer().getPluginManager().addPermission(positive);
        plugin.getServer().getPluginManager().addPermission(negative);

        PermissionAttachment att = null;

        Optional<PermissionAttachmentInfo> info = player.getEffectivePermissions().stream()
                .filter(pai -> pai.getAttachment() != null)
                .filter(pai -> pai.getAttachment().getPlugin() instanceof Core)
                .findAny();

        if (info.isPresent()) {
            att = info.get().getAttachment();
        }

        if (att == null) {
            att = player.addAttachment(plugin);
            att.setPermission(player.getName(), true);
            att.setPermission("^" + player.getName(), true);
        }

        // recalculate permissions
        player.recalculatePermissions();

        return att;
    }

}