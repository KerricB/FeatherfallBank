package io.owlcraft.featherfallbank.ui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

public final class ItemBuilder {
    private final ItemStack stack;
    private final List<String> lore = new ArrayList<>();

    public static ItemBuilder of(Material mat) {
        return new ItemBuilder(new ItemStack(mat));
    }

    private ItemBuilder(ItemStack base) {
        this.stack = base;
    }

    public ItemBuilder amount(int amount) {
        stack.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }

    public ItemBuilder name(String displayName) {
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(color(displayName));
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addLore(String line) {
        this.lore.add(color(line));
        return this;
    }

    public ItemBuilder addLore(List<String> lines) {
        for (String s : lines) this.lore.add(color(s));
        return this;
    }

    public ItemBuilder glow(boolean enabled) {
        if (!enabled) return this;
        ItemMeta meta = stack.getItemMeta();
        meta.addEnchant(Enchantment.LURE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        if (!lore.isEmpty()) {
            ItemMeta meta = stack.getItemMeta();
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String money(long amount) {
        return NumberFormat.getIntegerInstance(Locale.US).format(amount);
    }

    public static final class WithdrawMenu implements InventoryHolder {
        private final Inventory inv;

        private final LongSupplier pouchSupplier;
        private final LongSupplier treasurySupplier;
        private final LongConsumer performWithdraw;

        public WithdrawMenu(LongSupplier pouchSupplier,
                            LongSupplier treasurySupplier,
                            LongConsumer performWithdraw) {
            this.pouchSupplier = pouchSupplier;
            this.treasurySupplier = treasurySupplier;
            this.performWithdraw = performWithdraw;
            this.inv = Bukkit.createInventory(this, 27, color(UiTheme.WARN + "Withdraw " + UiTheme.ACCENT + "Shillings"));
            draw();
        }

        private void draw() {
            ItemStack fill = UiTheme.filler();
            for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, fill);

            long pouch = pouchSupplier.getAsLong();
            long treasury = treasurySupplier.getAsLong();

            inv.setItem(4, of(Material.CHEST_MINECART)
                    .name(UiTheme.WARN + "Withdraw to Pouch")
                    .addLore(UiTheme.MUTED + "Pouch: " + UiTheme.ACCENT + money(pouch) + " Shillings")
                    .addLore(UiTheme.MUTED + "Treasury: " + UiTheme.ACCENT + money(treasury) + " Shillings")
                    .build());

            inv.setItem(10, amountButton(1));
            inv.setItem(11, amountButton(10));
            inv.setItem(12, amountButton(50));
            inv.setItem(14, amountButton(100));
            inv.setItem(15, amountButton(500));
            inv.setItem(16, amountButton(1000));

            inv.setItem(18, UiTheme.backBtn());
            inv.setItem(20, specialButton("Half", Material.CAULDRON, "Withdraw half your Treasury"));
            inv.setItem(24, specialButton("All", Material.DROPPER, "Withdraw your entire Treasury"));
            inv.setItem(26, UiTheme.closeBtn());
        }

        private ItemStack amountButton(long amount) {
            Material icon = switch ((int) Math.min(amount, 1000)) {
                case 1, 10 -> Material.GOLD_NUGGET;
                case 50, 100 -> Material.GOLD_INGOT;
                default -> Material.GOLD_BLOCK;
            };
            return of(icon)
                    .name(UiTheme.WARN + "Withdraw " + money(amount))
                    .addLore(UiTheme.MUTED + "Move from Treasury to Pouch.")
                    .build();
        }

        private ItemStack specialButton(String label, Material material, String tip) {
            return of(material)
                    .name(UiTheme.WARN + label)
                    .addLore(UiTheme.MUTED + tip)
                    .build();
        }

        public void open(Player p) {
            p.openInventory(inv);
            p.playSound(p.getLocation(), Sound.UI_TOAST_IN, 0.6f, 1.2f);
        }

        @Override
        public Inventory getInventory() {
            return inv;
        }

        // Navigation-first handling so Close/Back always work
        public static void handleClick(InventoryClickEvent e, Player p,
                                       Runnable backToMain,
                                       LongSupplier treasurySupplier,
                                       LongConsumer performWithdraw) {
            if (!(e.getInventory().getHolder() instanceof WithdrawMenu)) return;
            e.setCancelled(true);

            int slot = e.getRawSlot();

            // Always allow navigation regardless of balance
            if (slot == 18) { // Back
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.0f);
                backToMain.run();
                return;
            }
            if (slot == 26) { // Close
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.6f, 1.0f);
                p.closeInventory();
                return;
            }

            long treasury = Math.max(0L, treasurySupplier.getAsLong());

            ClickType click = e.getClick();
            boolean x10 = (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT);

            Long amount = switch (slot) {
                case 10 -> 1L;
                case 11 -> 10L;
                case 12 -> 50L;
                case 14 -> 100L;
                case 15 -> 500L;
                case 16 -> 1000L;
                case 20 -> Math.max(1L, treasury / 2); // Half
                case 24 -> treasury;                   // All
                default -> null;                       // filler/anything else
            };

            if (amount == null) return;

            if (treasury <= 0L) {
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 0.7f);
                p.sendMessage(color(UiTheme.WARN + "You have no Shillings in your Treasury to withdraw."));
                return;
            }

            if (x10) amount = Math.min(amount * 10L, treasury);
            amount = Math.min(amount, treasury);

            if (amount <= 0) {
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 0.7f);
                p.sendMessage(color(UiTheme.WARN + "Nothing to withdraw."));
                return;
            }

            performWithdraw.accept(amount);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.9f, 1.6f);
            p.sendMessage(color(UiTheme.OK + "Withdrew " + UiTheme.ACCENT + money(amount) + " Shillings&7 to your Pouch."));
        }
    }
}
