package io.owlcraft.featherfallbank.ui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class HelpMenu implements InventoryHolder {
    private final Inventory inv;

    public HelpMenu() {
        this.inv = Bukkit.createInventory(this, 27, ItemBuilder.color(UiTheme.ACCENT + "Help & Tips"));
        draw();
    }

    private void draw() {
        var fill = UiTheme.filler();
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, fill);

        inv.setItem(10, ItemBuilder.of(Material.BUNDLE)
                .name(UiTheme.EMPHASIS + "Pouch")
                .addLore(UiTheme.MUTED + "Spendable money you carry.")
                .addLore(UiTheme.MUTED + "Drops 50% on death (outside safe zones).")
                .build());

        inv.setItem(13, ItemBuilder.of(Material.ENDER_CHEST)
                .name(UiTheme.EMPHASIS + "Treasury")
                .addLore(UiTheme.MUTED + "Your safe bank balance.")
                .addLore(UiTheme.MUTED + "No drops on death.")
                .build());

        inv.setItem(16, ItemBuilder.of(Material.BELL)
                .name(UiTheme.ACCENT + "Using the Teller")
                .addLore(UiTheme.MUTED + "Ring / right-click the Teller Bell")
                .addLore(UiTheme.MUTED + "to open the Treasury menus.")
                .build());

        inv.setItem(18, UiTheme.backBtn());
        inv.setItem(26, UiTheme.closeBtn());
    }

    public void open(Player p) {
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.UI_TOAST_IN, 0.6f, 1.6f);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    public static void handleClick(InventoryClickEvent e, Player p, Runnable backToMain) {
        if (!(e.getInventory().getHolder() instanceof HelpMenu)) return;
        e.setCancelled(true);

        int slot = e.getRawSlot();
        switch (slot) {
            case 18 -> {
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                backToMain.run();
            }
            case 26 -> {
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.6f, 1.0f);
                p.closeInventory();
            }
            default -> {}
        }
    }
}
