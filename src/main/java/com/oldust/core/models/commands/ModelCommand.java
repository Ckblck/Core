package com.oldust.core.models.commands;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.models.ModelPlugin;
import com.oldust.core.models.file.FileLoader;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import uk.lewdev.standmodels.exceptions.MaterialMismatchException;
import uk.lewdev.standmodels.model.Model;
import uk.lewdev.standmodels.parser.ModelBuildInstruction;
import uk.lewdev.standmodels.utils.Axis;

import javax.annotation.Nullable;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class ModelCommand extends InheritedCommand<ModelPlugin> {
    private static final FileLoader LOADER = new FileLoader();

    public ModelCommand(ModelPlugin plugin, String name, @Nullable List<String> aliases) {
        super(plugin, name, aliases);
    }

    @Override
    public BiConsumer<CommandSender, String[]> onCommand() {
        return (sender, args) -> {
            if (isNotPlayer(sender)) return;
            if (isNotAboveOrEqual(sender, PlayerRank.ADMIN)) return;

            if (args.length < 1) {
                CUtils.msg(sender, Lang.ERROR_COLOR + String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "file_name"));

                return;
            }

            Player player = (Player) sender;
            String fileName = args[0];
            String extension = FilenameUtils.getExtension(fileName);

            if (extension.equals("")) {
                fileName += ".txt";
            } else if (!extension.equals("txt")) {
                CUtils.msg(player, Lang.ERROR_COLOR + "The file must have a .txt extension!");

                return;
            }

            try {
                Set<ModelBuildInstruction> instructions = LOADER.getInstructions(fileName);
                Location playerLocation = player.getLocation();
                Location location = playerLocation.getBlock().getLocation().clone().add(0.5, 0, 0.5);
                Axis axis = Axis.getAxis(playerLocation);

                Model model = new Model(instructions, location, Axis.WEST, axis, 15);

                getPlugin().getStandModelLib().getModelManager().spawnModel(model);
                model.render();

                getPlugin().getModelModifyCommand().createPanel(player, model);
            } catch (NoSuchFileException e) {
                CUtils.msg(sender, Lang.ERROR_COLOR + "That file does not exist.");
            } catch (IllegalArgumentException e) {
                CUtils.msg(sender, Lang.ERROR_COLOR + "The file does not contain any readable model.");
            } catch (MaterialMismatchException e) {
                CUtils.msg(sender, Lang.ERROR_COLOR + "There is a material mismatch in the model provided. Please contact a developer.");
                e.getMismatched().forEach(mismatch -> CUtils.msg(sender, ChatColor.of("#6e6e6e") + "  - " + mismatch + " does not exist in the materials table."));
            }

        };
    }

}
