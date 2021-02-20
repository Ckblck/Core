package net.oldust.core.actionbar.components;

import net.oldust.core.actionbar.Actionbar;

/**
 * A Unicode is a group of words
 * (commonly unicode characters) that
 * are used in a {@link Actionbar}.
 */

public enum Unicode {
    HEALTH_BAR('\uE002', Space.from(143)),
    GRAY_HEALTH_BAR('\uE001', Space.from(131));

    private final char character;
    private final Space space;

    Unicode(char character) {
        this.character = character;
        this.space = null;
    }

    Unicode(char character, Space space) {
        this.character = character;
        this.space = space;
    }

    public String build() {
        return space.getUnicode() + character;
    }

}
