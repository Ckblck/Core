package net.oldust.core.models.panel;

import net.oldust.core.interactive.panels.InteractivePanel;
import net.oldust.core.interactive.panels.InteractivePanelManager;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.ItemBuilder;
import net.oldust.core.utils.lang.Lang;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ItemModelInteractivePanel extends InteractivePanel {
    private static final ItemStack TELEPORT_MODEL = new ItemBuilder(Material.ENDER_PEARL)
            .setDisplayName("#0f8c69 Teleport here").build();
    private static final ItemStack CHANGE_ROTATION_AXIS = new ItemBuilder(Material.REDSTONE)
            .setDisplayName("#e1c699 Change Rotation Axis°").build();
    private static final ItemStack ROTATE_1 = new ItemBuilder(Material.GOLD_NUGGET)
            .setDisplayName("#e1c699 Rotate +-1°")
            .setLore(Arrays.asList(
                    "#f7be60 Right Click: +1",
                    "#f7be60 Left Click: -1")).build();
    private static final ItemStack ROTATE_5 = new ItemBuilder(Material.GOLD_NUGGET)
            .setDisplayName("#e1c699 Rotate +-5°")
            .setLore(Arrays.asList(
                    "#f7be60 Right Click: +5",
                    "#f7be60 Left Click: -5")).build();
    private static final ItemStack ROTATE_10 = new ItemBuilder(Material.GOLD_NUGGET)
            .setDisplayName("#e1c699 Rotate +-15°")
            .setLore(Arrays.asList(
                    "#f7be60 Right Click: +15",
                    "#f7be60 Left Click: -15")).build();
    private static final ItemStack REMOVE = new ItemBuilder(Material.NETHER_WART)
            .setDisplayName("#e1c699 Remove model").build();
    private static final ItemStack EXIT = new ItemBuilder(Material.NETHERITE_SCRAP)
            .setDisplayName(Lang.ERROR_COLOR + "Exit").build();

    private RotationType rotationType;

    public ItemModelInteractivePanel(Player player, ArmorStand armorStand) {
        super(player);

        InteractivePanelManager manager = InteractivePanelManager.getInstance();

        this.rotationType = RotationType.X;

        add(0, TELEPORT_MODEL, (click) -> {
            Location center = player.getLocation().clone();
            armorStand.teleport(center);
        });

        add(1, CHANGE_ROTATION_AXIS, (click) -> {
            switch (rotationType) {
                case X:
                    this.rotationType = RotationType.Y;

                    break;
                case Y:
                    this.rotationType = RotationType.Z;

                    break;
                case Z:
                    this.rotationType = RotationType.X;

                    break;
            }
            CUtils.msg(player, Lang.SUCCESS_COLOR + "You changed the rotation axis to " + rotationType.name() + ".");
        });

        add(3, ROTATE_1, (click) -> {
            int amount = (click.getAction().name().startsWith("RIGHT")) ? 1 : -1;
            processRotation(armorStand, rotationType, amount);
        });

        add(4, ROTATE_5, (click) -> {
            int amount = (click.getAction().name().startsWith("RIGHT")) ? 5 : -5;
            processRotation(armorStand, rotationType, amount);
        });

        add(5, ROTATE_10, (click) -> {
            int amount = (click.getAction().name().startsWith("RIGHT")) ? 10 : -10;
            processRotation(armorStand, rotationType, amount);
        });

        add(7, REMOVE, (click) -> {
            armorStand.remove();
            manager.exitPanel(player);
        });

        add(8, EXIT, (click) -> manager.exitPanel(player));

        manager.setPanel(player, this);
    }

    private void processRotation(ArmorStand armorStand, RotationType rotationType, int amount) {
        switch (rotationType) {
            case X:
                armorStand.setRightArmPose(armorStand.getRightArmPose().add(amount, 0, 0));

                break;
            case Y:
                armorStand.setRightArmPose(armorStand.getRightArmPose().add(0, amount, 0));

                break;
            case Z:
                armorStand.setRightArmPose(armorStand.getRightArmPose().add(0, 0, amount));

                break;
        }
    }

    private enum RotationType {
        X, Y, Z
    }

}
