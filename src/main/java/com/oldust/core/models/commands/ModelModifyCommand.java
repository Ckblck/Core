package com.oldust.core.models.commands;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.models.ModelPlugin;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.ItemBuilder;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.interactive.InteractivePanel;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import uk.lewdev.standmodels.events.custom.ModelInteractEvent;
import uk.lewdev.standmodels.model.Model;

import java.util.*;
import java.util.function.BiConsumer;

public class ModelModifyCommand extends InheritedCommand<ModelPlugin> implements Listener {
    private static final ItemStack TELEPORT_MODEL = new ItemBuilder(Material.ENDER_PEARL)
            .setDisplayName(ChatColor.of("#0f8c69") + "Teleport here").build();
    private static final ItemStack ROTATE_MINUS_5 = new ItemBuilder(Material.LANTERN)
            .setDisplayName(ChatColor.of("#e1c699") + "Rotate -5°").build();
    private static final ItemStack ROTATE_1 = new ItemBuilder(Material.GOLD_NUGGET)
            .setDisplayName(ChatColor.of("#e1c699") + "Rotate +-1°")
            .setLore(Arrays.asList(
                    ChatColor.of("#f7be60") + "Right Click: +1",
                    ChatColor.of("#f7be60") + "Left Click: -1")).build();
    private static final ItemStack ROTATE_PLUS_5 = new ItemBuilder(Material.SOUL_LANTERN)
            .setDisplayName(ChatColor.of("#e1c699") + "Rotate +5°").build();
    private static final ItemStack REMOVE = new ItemBuilder(Material.NETHER_WART)
            .setDisplayName(ChatColor.of("#e1c699") + "Remove model").build();
    private static final ItemStack EXIT = new ItemBuilder(Material.NETHERITE_SCRAP)
            .setDisplayName(Lang.ERROR_COLOR + "Exit").build();

    private final Map<UUID, InteractivePanel> playersModifying = new HashMap<>();

    public ModelModifyCommand(ModelPlugin plugin, String name, @Nullable List<String> aliases) {
        super(plugin, name, aliases);

        CUtils.registerEvents(this);
    }

    @Override
    public BiConsumer<CommandSender, String[]> onCommand() {
        return (sender, args) -> {
            if (isNotPlayer(sender)) return;

            if (args.length == 0) {
                CUtils.msg(sender, Lang.ERROR_COLOR + "Insufficient arguments!");
                CUtils.msg(sender, Lang.ERROR_COLOR + "/modelmodify enter");
                CUtils.msg(sender, Lang.ERROR_COLOR + "/modelmodify exit");

                return;
            }

            UUID uuid = ((Player) sender).getUniqueId();
            String arg = args[0];
            boolean isNotAlready = !playersModifying.containsKey(uuid);

            if (arg.equalsIgnoreCase("enter")) {

                if (isNotAlready) {
                    playersModifying.put(uuid, null);
                    CUtils.msg(sender, Lang.SUCCESS_COLOR + "Entered the modification panel. Interact with a model to modify it.");
                } else {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "You already are in the modification panel! Use «/mmodify exit» to exit.");
                }

                return;
            }

            if (arg.equalsIgnoreCase("exit")) {

                if (isNotAlready) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "You are not modifying any models!");
                } else {
                    InteractivePanel panel = playersModifying.remove(uuid);
                    if (panel != null) panel.exit((Player) sender);

                    CUtils.msg(sender, Lang.SUCCESS_COLOR + "Exited from the modification panel.");
                }

            }

        };
    }

    @EventHandler
    public void onInteract(ModelInteractEvent e) {
        Player player = e.getInteractor();
        UUID uuid = e.getInteractor().getUniqueId();

        if (!playersModifying.containsKey(uuid)) return;

        boolean isEditing = playersModifying.get(uuid) != null;

        if (isEditing) return;

        Model model = e.getModel();

        InteractivePanel panel = createPanel(player, model);
        playersModifying.replace(uuid, panel);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        playersModifying.remove(e.getPlayer().getUniqueId());
    }

    public InteractivePanel createPanel(Player player, Model model) {
        InteractivePanel interactivePanel = new InteractivePanel(player);

        interactivePanel.add(0, TELEPORT_MODEL, (click) -> {
            Location center = player.getLocation().getBlock().getLocation().clone().add(0.5, 0, 0.5);
            model.setCenter(center);
        });

        interactivePanel.add(3, ROTATE_MINUS_5, (click) -> model.rotate(-5));

        interactivePanel.add(4, ROTATE_1, (click) -> {
            int amount = (click.getAction().name().startsWith("RIGHT")) ? 1 : -1;
            model.rotate(amount);
        });

        interactivePanel.add(5, ROTATE_PLUS_5, (click) -> model.rotate(5));

        interactivePanel.add(7, REMOVE, (click) -> {
            getPlugin().getStandModelLib().getModelManager().removeModel(model);
            playersModifying.remove(player.getUniqueId());
            interactivePanel.exit(player);
        });

        interactivePanel.add(8, EXIT, (click) -> {
            playersModifying.remove(player.getUniqueId());
            interactivePanel.exit(player);
        });

        interactivePanel.enter(player);

        return interactivePanel;
    }

}
