package net.oldust.core.scoreboard.entries;

import net.oldust.core.scoreboard.objects.Line;

@FunctionalInterface
public interface DynamicProperty {
    String modify(Line line);
}
