package net.oldust.core.models.panel;

import net.oldust.core.interactive.panels.InteractivePanel;
import net.oldust.core.interactive.panels.InteractivePanelManager;
import net.oldust.core.models.ModelPlugin;
import net.oldust.core.utils.ItemBuilder;
import net.oldust.core.utils.lang.Lang;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import uk.lewdev.standmodels.model.Model;

import java.util.Arrays;

public class ModelInteractivePanel extends InteractivePanel {
    private static final ItemStack TELEPORT_MODEL = new ItemBuilder(Material.ENDER_PEARL)
            .setDisplayName("#0f8c69 Teleport here").build();
    private static final ItemStack ROTATE_MINUS_5 = new ItemBuilder(Material.LANTERN)
            .setDisplayName("#e1c699 Rotate -5°").build();
    private static final ItemStack ROTATE_1 = new ItemBuilder(Material.GOLD_NUGGET)
            .setDisplayName("#e1c699 Rotate +-1°")
            .setLore(Arrays.asList(
                    "#f7be60 Right Click: +1",
                    "#f7be60 Left Click: -1")).build();
    private static final ItemStack ROTATE_PLUS_5 = new ItemBuilder(Material.SOUL_LANTERN)
            .setDisplayName("#e1c699 Rotate +5°").build();
    private static final ItemStack REMOVE = new ItemBuilder(Material.NETHER_WART)
            .setDisplayName("#e1c699 Remove model").build();
    private static final ItemStack EXIT = new ItemBuilder(Material.NETHERITE_SCRAP)
            .setDisplayName(Lang.ERROR_COLOR + "Exit").build();

    public ModelInteractivePanel(Player player, Model model, ModelPlugin plugin) {
        super(player);

        InteractivePanelManager manager = InteractivePanelManager.getInstance();

        add(0, TELEPORT_MODEL, (click) -> {
            Location center = player.getLocation().clone();
            model.setCenter(center);
        });

        add(3, ROTATE_MINUS_5, (click) -> model.rotate(-5));

        add(4, ROTATE_1, (click) -> {
            int amount = (click.getAction().name().startsWith("RIGHT")) ? 1 : -1;
            model.rotate(amount);
        });

        add(5, ROTATE_PLUS_5, (click) -> model.rotate(5));

        add(7, REMOVE, (click) -> {
            plugin.getStandModelLib().getModelManager().removeModel(model);
            plugin.getModelModifyCommand().getPlayersModifying().remove(player.getUniqueId());
            manager.exitPanel(player);
        });

        add(8, EXIT, (click) -> {
            plugin.getModelModifyCommand().getPlayersModifying().remove(player.getUniqueId());
            manager.exitPanel(player);
        });

        manager.setPanel(player, this);
    }

}
