package com.oldust.core.staff.mode;

import com.oldust.core.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public final class ModeItems {
    private static final List<String> STAFF_TOOLS_LORE = Arrays.asList(
            "#333436 General tools",
            " ",
            "#b0b0b0 Provides you with a variety",
            "#b0b0b0 of useful tools."
    );
    public static final ItemStack STAFF_TOOLS = new ItemBuilder(Material.KELP)
            .setDisplayName("#2133eb Tools")
            .setLore(STAFF_TOOLS_LORE).build();
    private static final List<String> RANDOM_TELEPORT_LORE = Arrays.asList(
            "#333436 Others",
            " ",
            "#b0b0b0 Teleports yourself at a",
            "#b0b0b0 random player."
    );
    public static final ItemStack RANDOM_TELEPORT = new ItemBuilder(Material.COMPASS)
            .setDisplayName("#2133eb Random TP")
            .setLore(RANDOM_TELEPORT_LORE).build();
    private static final List<String> STICK_LORE = Arrays.asList(
            "#333436 Others",
            " ",
            "#b0b0b0 A stick whose function is to",
            "#b0b0b0 test players supposed to be",
            "#b0b0b0 Anti-KB cheating."
    );
    public static final ItemStack STICK_ANTI_KB = new ItemBuilder(Material.STICK)
            .addEnchant(Enchantment.KNOCKBACK, 2)
            .addItemFlag(ItemFlag.HIDE_ENCHANTS)
            .setDisplayName("#2133eb Stick")
            .setLore(STICK_LORE).build();
    private static final List<String> EXIT_LORE = Arrays.asList(
            "#333436 Others",
            " ",
            "#b0b0b0 Exits from the moderation panel"
    );
    public static final ItemStack EXIT = new ItemBuilder(Material.ENDER_EYE)
            .setDisplayName("#2133eb Exit")
            .setLore(EXIT_LORE).build();
}
