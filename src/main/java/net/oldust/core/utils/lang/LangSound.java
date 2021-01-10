package net.oldust.core.utils.lang;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Miscellaneous sounds.
 */

@Getter
@RequiredArgsConstructor
public enum LangSound {
    ERROR("oldust.misc.error1", 0.75F, 1);

    private final String soundName;
    private final float pitch, volume;

}
