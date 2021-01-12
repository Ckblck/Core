package net.oldust.core.models.items.commands;

import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.models.ModelPlugin;
import net.oldust.core.models.items.ItemModels;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.ItemBuilder;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class ItemModelCommand extends InheritedCommand<ModelPlugin> {

    public ItemModelCommand(ModelPlugin plugin, String name, @Nullable List<String> aliases) {
        super(plugin, name, aliases);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;
            if (isNotAboveOrEqual(sender, PlayerRank.ADMIN)) return;

            if (args.length < 1) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "item_name", LangSound.ERROR));
                return;
            }

            Player player = (Player) sender;
            String itemName = args[0];
            ItemModels itemModel = ItemModels.getFromName(itemName);
            if (itemModel == null) {
                CUtils.msg(player, Lang.ERROR_COLOR + "That item name does not exists!", LangSound.ERROR);
                return;
            }

            Location playerLocation = player.getLocation();
            Location location = playerLocation.getBlock().getLocation().clone().add(0.5, 0, 0.5);
            ArmorStand armorStand = player.getWorld().spawn(location, ArmorStand.class);
            Objects.requireNonNull(armorStand.getEquipment()).setItemInMainHand(new ItemBuilder(Material.PAPER)
                    .setModelData(itemModel.getModelData()).build());

            CUtils.msg(sender, Lang.SUCCESS_COLOR + String.format("You spawned a %s armor stand.", itemModel.getItemName()));
        };
    }


}
