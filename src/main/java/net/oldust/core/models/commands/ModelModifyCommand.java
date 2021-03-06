package net.oldust.core.models.commands;

import lombok.Getter;
import net.oldust.core.Core;
import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.interactive.panels.InteractivePanel;
import net.oldust.core.interactive.panels.InteractivePanelManager;
import net.oldust.core.internal.provider.EventsProvider;
import net.oldust.core.models.ModelPlugin;
import net.oldust.core.models.panel.ModelInteractivePanel;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import uk.lewdev.standmodels.events.custom.ModelInteractEvent;
import uk.lewdev.standmodels.model.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ModelModifyCommand extends InheritedCommand<ModelPlugin> implements Listener {

    @Getter
    private final Map<UUID, InteractivePanel> playersModifying;

    public ModelModifyCommand(ModelPlugin plugin, String name, @Nullable List<String> aliases) {
        super(plugin, name, aliases);

        this.playersModifying = new HashMap<>();

        EventsProvider provider = Core.getInstance().getEventsProvider();

        provider.newOperation(Core.class, PlayerQuitEvent.class, (event, db) -> {
            UUID uuid = event.getPlayer().getUniqueId();

            playersModifying.remove(uuid);
        });

        CUtils.registerEvents(this);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;
            if (isNotAboveOrEqual(sender, PlayerRank.ADMIN)) return;

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
                    CUtils.msg(sender, Lang.ERROR_COLOR + "You already are in the modification panel! Use «/modelmodify exit» to exit.");
                }

                return;
            }

            if (arg.equalsIgnoreCase("exit")) {
                if (isNotAlready) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "You are not modifying any models!");
                } else {
                    InteractivePanel panel = playersModifying.remove(uuid);
                    if (panel != null) InteractivePanelManager.getInstance().exitPanel((Player) sender);

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

    public InteractivePanel createPanel(Player player, Model model) {
        return new ModelInteractivePanel(player, model, getPlugin());
    }

}
