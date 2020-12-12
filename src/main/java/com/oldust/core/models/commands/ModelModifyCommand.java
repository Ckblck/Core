package com.oldust.core.models.commands;

import com.oldust.core.Core;
import com.oldust.core.commons.internal.EventsProvider;
import com.oldust.core.commons.internal.Operation;
import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.interactive.panels.InteractivePanel;
import com.oldust.core.interactive.panels.InteractivePanelManager;
import com.oldust.core.models.ModelPlugin;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.ItemBuilder;
import com.oldust.core.utils.lambda.TriConsumer;
import com.oldust.core.utils.lang.Lang;
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
import java.util.concurrent.CompletableFuture;

public class ModelModifyCommand extends InheritedCommand<ModelPlugin> implements Listener {
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

    private final Map<UUID, InteractivePanel> playersModifying = new HashMap<>();

    public ModelModifyCommand(ModelPlugin plugin, String name, @Nullable List<String> aliases) {
        super(plugin, name, aliases);

        EventsProvider provider = Core.getInstance().getEventsProvider();

        provider.newOperation(PlayerQuitEvent.class, new Operation<PlayerQuitEvent>((event, db)
                -> playersModifying.remove(event.getPlayer().getUniqueId())));

        CUtils.registerEvents(this);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;

            CompletableFuture<Boolean> future = isNotAboveOrEqual(sender, PlayerRank.ADMIN);

            future.thenAcceptAsync(notAbove -> {
                if (notAbove) return;

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
                    CUtils.runSync(() -> {
                        if (isNotAlready) {
                            CUtils.msg(sender, Lang.ERROR_COLOR + "You are not modifying any models!");
                        } else {
                            InteractivePanel panel = playersModifying.remove(uuid);
                            if (panel != null) InteractivePanelManager.getInstance().exitPanel((Player) sender);

                            CUtils.msg(sender, Lang.SUCCESS_COLOR + "Exited from the modification panel.");
                        }
                    });
                }
            });

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

    public InteractivePanel createPanel(Player player, Model model) {
        InteractivePanelManager manager = InteractivePanelManager.getInstance();
        InteractivePanel interactivePanel = new InteractivePanel(player);

        interactivePanel.add(0, TELEPORT_MODEL, (click) -> {
            Location center = player.getLocation().clone();
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
            manager.exitPanel(player);
        });

        interactivePanel.add(8, EXIT, (click) -> {
            playersModifying.remove(player.getUniqueId());
            manager.exitPanel(player);
        });

        manager.setPanel(player, interactivePanel);

        return interactivePanel;
    }

}
