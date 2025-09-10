package io.owlcraft.featherfallbank.ui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

public final class DepositMenu implements InventoryHolder {
    private final Inventory inv;

    private final LongSupplier pouchSupplier;
    private final LongSupplier treasurySupplier;
    private final LongConsumer performDeposit;

    public DepositMenu(LongSupplier pouchSupplier,
                       LongSupplier treasurySupplier,
                       LongConsumer performDeposit) {
        this.pouchSupplier = pouchSupplier;
        this.treasurySupplier = treasurySupplier;
        this.performDeposit = performDeposit;
        this.inv = Bukkit.createInventory(this, 27, ItemBuilder.color(UiTheme.EMPHASIS + "Deposit " + UiTheme.ACCENT + "Shillings"));
        draw();
    }

    private void draw() {
        ItemStack fill = UiTheme.filler();
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, fill);

        long pouch = pouchSupplier.getAsLong();
        long treasury = treasurySupplier.getAsLong();

        inv.setItem(4, ItemBuilder.of(Material.HOPPER)
                .name(UiTheme.OK + "Deposit to Treasury")
                .addLore(UiTheme.MUTED + "Pouch: " + UiTheme.ACCENT + ItemBuilder.money(pouch) + " Shillings")
                .addLore(UiTheme.MUTED + "Treasury: " + UiTheme.ACCENT + ItemBuilder.money(treasury) + " Shillings")
                .build());

        inv.setItem(10, amountButton(1));
        inv.setItem(11, amountButton(10));
        inv.setItem(12, amountButton(50));
        inv.setItem(14, amountButton(100));
        inv.setItem(15, amountButton(500));
        inv.setItem(16, amountButton(1000));

        inv.setItem(18, UiTheme.backBtn());
        inv.setItem(20, specialButton("Half", Material.CAULDRON, "Deposit half your pouch"));
        inv.setItem(24, specialButton("All", Material.DISPENSER, "Deposit your entire pouch"));
        inv.setItem(26, UiTheme.closeBtn());
    }

    private ItemStack amountButton(long amount) {
        Material icon = switch ((int) Math.min(amount, 1000)) {
            case 1, 10 -> Material.GOLD_NUGGET;
            case 50, 100 -> Material.GOLD_INGOT;
            default -> Material.GOLD_BLOCK; // 500 / 1000
        };
        return ItemBuilder.of(icon)
                .name(UiTheme.OK + "Deposit " + ItemBuilder.money(amount))
                .addLore(UiTheme.MUTED + "Move from Pouch to Treasury.")
                .build();
    }

    private ItemStack specialButton(String label, Material material, String tip) {
        return ItemBuilder.of(material)
                .name(UiTheme.OK + label)
                .addLore(UiTheme.MUTED + tip)
                .build();
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
                                   Runnable backToMain,
                                   LongSupplier pouchSupplier,
                                   LongConsumer performDeposit) {
        if (!(e.getInventory().getHolder() instanceof DepositMenu)) return;
        e.setCancelled(true);

        int slot = e.getRawSlot();

        // ✅ Navigation FIRST so it works even when pouch = 0
        if (slot == 18) { // Back
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            backToMain.run();
            return;
        }
        if (slot == 26) { // Close
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.6f, 1.0f);
            p.closeInventory();
            return;
        }

        long pouch = Math.max(0L, pouchSupplier.getAsLong());

        ClickType click = e.getClick();
        boolean x10 = (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT);

        Long amount = switch (slot) {
            case 10 -> 1L;
            case 11 -> 10L;
            case 12 -> 50L;
            case 14 -> 100L;
            case 15 -> 500L;
            case 16 -> 1000L;
            case 20 -> Math.max(1L, pouch / 2);  // Half
            case 24 -> pouch;                    // All
            default -> null;                     // filler/anything else
        };

        if (amount == null) return;

        if (pouch <= 0L) {
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 0.7f);
            p.sendMessage(ItemBuilder.color(UiTheme.WARN + "You have no Shillings in your Pouch to deposit."));
            return;
        }

        if (x10) amount = Math.min(amount * 10L, pouch);
        amount = Math.min(amount, pouch);

        if (amount <= 0) {
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 0.7f);
            p.sendMessage(ItemBuilder.color(UiTheme.WARN + "Nothing to deposit."));
            return;
        }

        performDeposit.accept(amount);
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.9f, 1.8f);
        p.sendMessage(ItemBuilder.color(UiTheme.OK + "Deposited " + UiTheme.ACCENT + ItemBuilder.money(amount) + " Shillings&7 to your Treasury."));
    }
}
