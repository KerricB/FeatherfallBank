package io.owlcraft.featherfallbank.ui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class UiTheme {
    private UiTheme() {}

    // Colors
    public static final String ACCENT = "&b";   // aqua
    public static final String EMPHASIS = "&6"; // gold
    public static final String MUTED = "&7";    // gray
    public static final String OK = "&a";       // green
    public static final String WARN = "&c";     // red

    public static String titleMain() {
        return EMPHASIS + "Featherfall " + ACCENT + "Treasury";
    }

    public static ItemStack filler() {
        return ItemBuilder.of(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
                .name(" ")
                .build();
    }

    public static ItemStack pouchInfo(long pouch) {
        return ItemBuilder.of(Material.BUNDLE)
                .name(EMPHASIS + "Your Pouch")
                .addLore(ACCENT + ItemBuilder.money(pouch) + " Shillings")
                .addLore(MUTED + "Spendable balance you carry.")
                .build();
    }

    public static ItemStack treasuryInfo(long treasury) {
        return ItemBuilder.of(Material.ENDER_CHEST)
                .name(EMPHASIS + "Treasury Balance")
                .addLore(ACCENT + ItemBuilder.money(treasury) + " Shillings")
                .addLore(MUTED + "Safe, banked funds. No drop on death.")
                .build();
    }

    public static ItemStack depositBtn() {
        return ItemBuilder.of(Material.HOPPER)
                .name(OK + "Deposit")
                .addLore(MUTED + "Move Shillings from Pouch")
                .addLore(MUTED + "to Treasury.")
                .glow(true)
                .build();
    }

    public static ItemStack quickDepositBtn() {
        return ItemBuilder.of(Material.DISPENSER)
                .name(OK + "Quick Deposit")
                .addLore(MUTED + "Deposit your pouch in one click.")
                .build();
    }

    public static ItemStack withdrawBtn() {
        return ItemBuilder.of(Material.CHEST_MINECART)
                .name(WARN + "Withdraw")
                .addLore(MUTED + "Move Shillings from Treasury")
                .addLore(MUTED + "to Pouch.")
                .glow(true)
                .build();
    }

    public static ItemStack helpBtn() {
        return ItemBuilder.of(Material.BOOK)
                .name(ACCENT + "Help & Tips")
                .addLore(MUTED + "Pouch = can drop on death (50%).")
                .addLore(MUTED + "Treasury = safe storage.")
                .addLore(MUTED + "Safe zones: OwlRun & marked regions.")
                .build();
    }

    public static ItemStack closeBtn() {
        return ItemBuilder.of(Material.BARRIER)
                .name(WARN + "Close")
                .build();
    }

    public static ItemStack backBtn() {
        return ItemBuilder.of(Material.ARROW)
                .name(ACCENT + "Back")
                .build();
    }
}
