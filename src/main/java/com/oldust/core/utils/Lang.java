package com.oldust.core.utils;

import net.md_5.bungee.api.ChatColor;

public class Lang {
    public static final ChatColor ERROR_COLOR = ChatColor.of("#ff443b");
    public static final ChatColor SUCCESS_COLOR = ChatColor.of("#11d11e");
    public static final String MUST_BE_PLAYER = ERROR_COLOR + "¡Debes ser un jugador para ejecutar este comando!";
    public static final String MISSING_ARGUMENT_FORMATABLE = ERROR_COLOR + "Error! <%s> expected.";
    public static final String DB_DISAPPEARED = ERROR_COLOR + "Uh oh! Your database disappeared.";
    public static final String NO_PERMISSIONS = ERROR_COLOR + "You do not have permission to use this command!";
}
