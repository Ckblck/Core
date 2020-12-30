package net.oldust.core.internal.inventories;

import net.oldust.core.utils.CUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Permite la paginación de información
 * en el lore de un item. Esto significa,
 * que un mismo item puede utilizarse para
 * mostrar diverso tipo de información en su lore.
 */

public class PageableItemStack extends ItemStack {
    private final Map<Integer, List<String>> pages = new HashMap<>();
    private int currentPage = 0;

    public PageableItemStack(@NotNull final Material type) {
        super(type, 1);
    }

    public PageableItemStack(@NotNull final Material type, final int amount) {
        super(type, amount, (short) 0);
    }

    public boolean nextPage() {
        int lastPage = pages.size() - 1;

        if (currentPage == lastPage) {
            return false;
        }

        List<String> lore = pages.get(++currentPage);
        ItemMeta itemMeta = getItemMeta();

        itemMeta.setLore(lore);

        setItemMeta(itemMeta);

        return true;
    }

    public boolean previousPage() {
        if (currentPage == 0) {
            return false;
        }

        List<String> lore = pages.get(--currentPage);
        ItemMeta itemMeta = getItemMeta();

        itemMeta.setLore(lore);

        setItemMeta(itemMeta);

        return true;
    }

    public void setPage(int page, List<String> lore) {
        lore = CUtils.colorizeList(lore);
        pages.put(page, lore);

        if (page == 0) {
            ItemMeta itemMeta = getItemMeta();

            itemMeta.setLore(lore);

            setItemMeta(itemMeta);
        }

    }

}
