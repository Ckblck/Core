package net.oldust.core.utils.lang;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

/**
 * Miscellaneous sounds.
 */

@Getter
@RequiredArgsConstructor
public enum LangSound {
    ERROR("oldust.misc.error1", 0.75F, 1),
    CHAT_POPUP_1("oldust.misc.chatpopup1", 1, 0.75F),
    CHAT_POPUP_2("oldust.misc.chatpopup2", 1, 0.75F);

    private final String soundName;
    private final float pitch, volume;

    public void play(Player player) {
        player.playSound(player.getLocation(), soundName, volume, pitch);
    }

}
