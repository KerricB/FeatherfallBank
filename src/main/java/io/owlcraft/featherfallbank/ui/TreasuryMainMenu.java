package io.owlcraft.featherfallbank.ui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public final class TreasuryMainMenu implements InventoryHolder {
    private final Inventory inv;

    public TreasuryMainMenu(Player viewer, long pouchBalance, long treasuryBalance) {
        this.inv = Bukkit.createInventory(this, 27, ItemBuilder.color(UiTheme.titleMain()));
        draw(pouchBalance, treasuryBalance);
    }

    private void draw(long pouch, long treasury) {
        ItemStack fill = UiTheme.filler();
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, fill);

        // Top row info tiles
        inv.setItem(2, UiTheme.pouchInfo(pouch));
        inv.setItem(4, ItemBuilder.of(Material.FEATHER)
                .name(UiTheme.ACCENT + "OwlCraft")
                .addLore(UiTheme.MUTED + "A Survival World of Exploration & Conquest")
                .build());
        inv.setItem(6, UiTheme.treasuryInfo(treasury));

        // Middle row actions
        inv.setItem(12, UiTheme.depositBtn());
        inv.setItem(13, UiTheme.quickDepositBtn());
        inv.setItem(14, UiTheme.withdrawBtn());

        // Bottom row utility
        inv.setItem(18, UiTheme.helpBtn());
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

    public static void handleClick(InventoryClickEvent e, Player p,
                                   Runnable openDepositMenu,
                                   Runnable runQuickDeposit,
                                   Runnable openWithdrawMenu,
                                   Runnable showHelp) {
        if (!(e.getInventory().getHolder() instanceof TreasuryMainMenu)) return;
        e.setCancelled(true);

        int slot = e.getRawSlot();
        switch (slot) {
            case 12 -> { // Deposit
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
                openDepositMenu.run();
            }
            case 13 -> { // Quick Deposit
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.8f);
                runQuickDeposit.run();
            }
            case 14 -> { // Withdraw
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 0.8f);
                openWithdrawMenu.run();
            }
            case 18 -> { // Help
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.8f);
                showHelp.run();
            }
            case 26 -> { // Close
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.6f, 1.0f);
                p.closeInventory();
            }
            default -> {}
        }
    }
}
