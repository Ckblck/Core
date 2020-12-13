package net.oldust.core.staff.playerdata;

import com.google.common.base.Preconditions;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import net.oldust.core.Core;
import net.oldust.core.commons.internal.inventories.AbstractInventoryProvider;
import net.oldust.core.staff.punish.Punishment;
import net.oldust.core.staff.punish.PunishmentType;
import net.oldust.core.staff.punish.types.BanPunishment;
import net.oldust.core.staff.punish.types.KickPunishment;
import net.oldust.core.staff.punish.types.MutePunishment;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.ItemBuilder;
import net.oldust.core.utils.lang.Async;
import net.oldust.core.utils.lang.Lang;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class PlayerDataInventory extends AbstractInventoryProvider {
    private static final String INV_NAME = "Player Data";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private static final KickPunishment KICK_PUNISHMENT = (KickPunishment) PunishmentType.KICK.getHandler();
    private static final BanPunishment BAN_PUNISHMENT = (BanPunishment) PunishmentType.BAN.getHandler();
    private static final MutePunishment MUTE_PUNISHMENT = (MutePunishment) PunishmentType.MUTE.getHandler();

    private final String punishedName;
    private final UUID punishedUuid;
    private List<Punishment> punishments;

    @Async
    public PlayerDataInventory(Player player, String punishedName, UUID punishedUuid) {
        super(player);

        this.punishedName = punishedName;
        this.punishedUuid = punishedUuid;

        Preconditions.checkNotNull(punishedUuid);

        SmartInventory inventory = build();
        CUtils.runSync(() -> inventory.open(player));
    }

    private PlayerDataInventory(Player player, String punishedName, UUID punishedUuid, List<Punishment> punishments) {
        super(player);

        this.punishedName = punishedName;
        this.punishedUuid = punishedUuid;
        this.punishments = punishments;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ClickableItem[] items = new ClickableItem[punishments.size()];

        for (int i = 0; i < punishments.size(); i++) {
            Punishment punishment = punishments.get(i);
            ClickableItem item = ClickableItem.of(buildItem(punishment), (click) -> {
            });

            items[i] = item;
        }

        Pagination pagination = contents.pagination();

        pagination.setItems(items);
        pagination.setItemsPerPage(16);

        SlotIterator iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 3);

        for (int i = 1; i < 7; i++) {
            iterator.blacklist(i, 0);
        }

        for (int i = 1; i < 7; i++) {
            iterator.blacklist(i, 1);
        }

        iterator.blacklist(4, 2);

        iterator.blacklist(1, 6);
        iterator.blacklist(4, 6);

        for (int i = 1; i < 5; i++) {
            iterator.blacklist(i, 7);
        }

        for (int i = 1; i < 5; i++) {
            iterator.blacklist(i, 8);
        }

        pagination.addToIterator(iterator);

        List<String> lore = getInformationItemLore();

        contents.set(0, 4, ClickableItem.of(new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
                .setDisplayName("#fcba03 Information")
                .addItemFlag(ItemFlag.HIDE_POTION_EFFECTS)
                .setLore(lore).build(), (click) -> {
        }));

        contents.set(5, 4, ClickableItem.of(NAVIGATOR, (click) -> {
            handleClick(pagination, click);
        }));

    }

    @NotNull
    private List<String> getInformationItemLore() {
        long banCount = punishments.stream()
                .filter(punishment -> punishment.getType() == PunishmentType.BAN)
                .count();

        long muteCount = punishments.stream()
                .filter(punishment -> punishment.getType() == PunishmentType.MUTE)
                .count();

        long kickCount = punishments.stream()
                .filter(punishment -> punishment.getType() == PunishmentType.KICK)
                .count();

        String[] currentPunishmentInfo = new String[]{
                " ",
                Lang.ERROR_COLOR + "The player has no active punishment."
        };

        if (!punishments.isEmpty()) {
            Punishment lastPunishment = punishments.get(0);

            Timestamp expiration = lastPunishment.getExpiration();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            boolean isPunished = expiration == null || expiration.after(now);

            String date = FORMAT.format(lastPunishment.getDate());
            String expires = FORMAT.format(expiration);

            if (isPunished) {
                currentPunishmentInfo = new String[]{
                        "   " + Lang.ERROR_COLOR + "* #a6a6a6 Type: &f" + lastPunishment.getType().name().toLowerCase(),
                        "   " + Lang.ERROR_COLOR + "* #a6a6a6 Punisher: &f" + lastPunishment.getPunisherName(),
                        "   " + Lang.ERROR_COLOR + "* #a6a6a6 Reason: &f" + lastPunishment.getReason(),
                        "   " + Lang.ERROR_COLOR + "* #a6a6a6 Date: &f" + date,
                        "   " + Lang.ERROR_COLOR + "* #a6a6a6 Expires: &f" + expires
                };
            }

        }

        List<String> lore = new ArrayList<>(Arrays.asList(
                " ",
                "#a6a6a6 Bans: &f" + banCount,
                "#a6a6a6 Mutes: &f" + muteCount,
                "#a6a6a6 Kicks: &f" + kickCount,
                " ",
                "#fcba03 Current Punishment"
        ));

        lore.addAll(Arrays.asList(currentPunishmentInfo));
        lore.add(" ");

        return lore;
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

    private ItemStack buildItem(Punishment punishment) {
        PunishmentType type = punishment.getType();

        String word;
        ItemBuilder builder;

        if (type == PunishmentType.BAN) {
            word = "Banned by: &f";
            builder = new ItemBuilder(Material.FERMENTED_SPIDER_EYE);
        } else if (type == PunishmentType.MUTE) {
            word = "Muted by: &f";
            builder = new ItemBuilder(Material.PHANTOM_MEMBRANE);
        } else {
            word = "Kicked by: &f";
            builder = new ItemBuilder(Material.SCUTE);
        }

        String unpunishedDate = "";
        String unpunisher = "";

        if (punishment instanceof Punishment.ExpiredPunishment) {
            Punishment.ExpiredPunishment expiredPunishment = (Punishment.ExpiredPunishment) punishment;

            Timestamp unpunishedAt = expiredPunishment.getUnpunishedAt();
            String unpunishedBy = expiredPunishment.getUnpunishedBy();

            if (unpunishedAt != null) {
                String unpunishedFormat = FORMAT.format(unpunishedAt);

                unpunishedDate = "#a6a6a6 Unpunished at: &f" + unpunishedFormat;
            }

            if (unpunishedBy != null) {
                unpunisher = "#a6a6a6 Unpunished by: &f" + unpunishedBy;
            }

        }

        String itemName = "#fcba03" + WordUtils.capitalizeFully(type.name());
        String date = FORMAT.format(punishment.getDate());
        String expires = (punishment.getExpiration() != null)
                ? FORMAT.format(punishment.getExpiration())
                : Lang.ERROR_COLOR + "never";

        builder.setDisplayName(itemName);

        List<String> lore = new ArrayList<>(Arrays.asList(
                "#404040 #" + punishment.getPunishmentId(),
                " ",
                "#a6a6a6 " + word + punishment.getPunisherName(),
                "#a6a6a6 " + "Reason: &f" + punishment.getReason(),
                "#a6a6a6 " + "Date: &f" + date,
                "#a6a6a6 " + "Expiration: &f" + expires,
                unpunishedDate,
                unpunisher));

        if (unpunisher.equals("")) {
            lore.remove(lore.size() - 1);
        } else {
            lore.add(" ");
        }

        builder.setLore(lore);

        return builder.build();
    }

    @Async
    private List<Punishment> fetchPunishments() {
        List<Punishment> punishments = new ArrayList<>();

        Optional<Punishment> currentBan = BAN_PUNISHMENT.currentPunishment(punishedUuid);
        currentBan.ifPresent(punishments::add);

        Optional<Punishment> currentMute = MUTE_PUNISHMENT.currentPunishment(punishedUuid);
        currentMute.ifPresent(punishments::add);

        List<Punishment.ExpiredPunishment> expiredBans = BAN_PUNISHMENT.fetchPunishments(punishedName);
        punishments.addAll(expiredBans);

        List<Punishment.ExpiredPunishment> expiredMutes = MUTE_PUNISHMENT.fetchPunishments(punishedName);
        punishments.addAll(expiredMutes);

        List<Punishment> kicks = KICK_PUNISHMENT.fetchPunishments(punishedName);
        punishments.addAll(kicks);

        punishments.sort(
                Comparator.comparingLong(punishment -> ((Punishment) punishment).getDate().getTime())
                        .reversed()
        );

        return punishments;
    }

    @Override
    protected SmartInventory buildInventory() {
        return SmartInventory.builder()
                .title(INV_NAME)
                .size(6, 9)
                .provider(new PlayerDataInventory(player, punishedName, punishedUuid, fetchPunishments()))
                .manager(Core.getInstance().getInventoryManager())
                .build();
    }

}
