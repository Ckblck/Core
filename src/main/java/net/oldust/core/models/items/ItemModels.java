package net.oldust.core.models.items;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public enum ItemModels {

    EXAMPLE("example", 1, false);

    private final String itemName;
    private final int modelData;
    //TODO: Make this work
    private final boolean modifiable;

    @Nullable
    public static ItemModels getFromName(String itemName) {
        for (ItemModels itemModels : ItemModels.values()) {
            if (itemModels.getItemName().equalsIgnoreCase(itemName)) {
                return itemModels;
            }
        }

        return null;
    }

}
