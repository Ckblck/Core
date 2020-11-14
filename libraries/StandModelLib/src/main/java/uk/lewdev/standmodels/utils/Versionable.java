package uk.lewdev.standmodels.utils;

import org.bukkit.Bukkit;

/*
    UMaterial Version: 8

    This software is created and owned by RandomHashTags, and is licensed under the GNU Affero General Public License v3.0 (https://choosealicense.com/licenses/agpl-3.0/)
    You can only find this software at https://gitlab.com/RandomHashTags/umaterial
    You can find RandomHashTags on
        Discord - RandomHashTags#1948
        Discord Server - https://discord.gg/CPTsc5X
        Dlive - https://dlive.tv/RandomHashTags
        Email - imrandomhashtags@gmail.com
        GitHub - https://github.com/RandomHashTags
        GitLab - https://gitlab.com/RandomHashTags
        MCMarket - https://www.mc-market.org/members/20858/
        Minecraft - RandomHashTags
        Mixer - https://mixer.com/randomhashtags
        PayPal - imrandomhashtags@gmail.com
        Reddit - https://www.reddit.com/user/randomhashtags/
        SpigotMC - https://www.spigotmc.org/members/76364/
        Spotify - https://open.spotify.com/user/randomhashtags
        Stackoverflow - https://stackoverflow.com/users/12508938/
        Subnautica Mods - https://www.nexusmods.com/users/77115308
        Twitch - https://www.twitch.tv/randomhashtags/
        Twitter - https://twitter.com/irandomhashtags
        YouTube - https://www.youtube.com/channel/UC3L6Egnt0xuMoz8Ss5k51jw
 */
public interface Versionable {
    String VERSION = Bukkit.getVersion();
    boolean EIGHT = VERSION.contains("1.8"), NINE = VERSION.contains("1.9"), TEN = VERSION.contains("1.10"), ELEVEN = VERSION.contains("1.11"), TWELVE = VERSION.contains("1.12"), THIRTEEN = VERSION.contains("1.13"), FOURTEEN = VERSION.contains("1.14"), FIFTEEN = VERSION.contains("1.15");
    boolean LEGACY = EIGHT || NINE || TEN || ELEVEN || TWELVE;
}
