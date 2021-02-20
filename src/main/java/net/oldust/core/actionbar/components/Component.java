package net.oldust.core.actionbar.components;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Component {
    private final Unicode unicode;
    private final Space[] spaces;

    public String build() {
        StringBuilder build = new StringBuilder();

        for (Space space : spaces) {
            build.append(space.getUnicode());
        }

        build.append(unicode.build());

        return build.toString();
    }

}
