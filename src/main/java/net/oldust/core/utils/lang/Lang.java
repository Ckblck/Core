package net.oldust.core.utils.lang;

import net.md_5.bungee.api.ChatColor;
import net.oldust.core.utils.CUtils;

public class Lang {
    public static final ChatColor ERROR_COLOR = ChatColor.of("#ff443b");
    public static final ChatColor SUCCESS_COLOR = ChatColor.of("#e0cb6e");
    public static final ChatColor SUCCESS_COLOR_ALT = ChatColor.of("#E8C39E");
    public static final String MUST_BE_PLAYER = ERROR_COLOR + "You must be a player to execute that command!";
    public static final String MISSING_ARGUMENT_FORMATTABLE = ERROR_COLOR + "Wrong syntax! <%s> expected.";
    public static final String DB_DISAPPEARED = ERROR_COLOR + "Uh oh! Your database disappeared.";
    public static final String NO_PERMISSIONS = ERROR_COLOR + "You do not have permission to use this command!";
    public static final String ARROW = CUtils.color("#a19e6d &m &r#a19e6d»");
    public static final String PLAYER_OFFLINE = ERROR_COLOR + "That player is not connected!";
    public static final String SPECIFIC_PLAYER_OFFLINE_FORMATABLE = ERROR_COLOR + "The player <%s> is not connected!";
    public static final String ERROR_FORMATTABLE = ERROR_COLOR + "An error occurred (%s).";
}
