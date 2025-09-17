package io.owlcraft.featherfallbank.ui;

import io.owlcraft.featherfallbank.FeatherfallBankPlugin;
import io.owlcraft.featherfallbank.util.Money;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

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

        // Leaderboard (hover to view)
        inv.setItem(16, buildTopBalancesButton());

        // Bottom row utility
        inv.setItem(18, UiTheme.helpBtn());
        inv.setItem(26, UiTheme.closeBtn());
    }

    private ItemStack buildTopBalancesButton() {
        FeatherfallBankPlugin plugin = JavaPlugin.getPlugin(FeatherfallBankPlugin.class);

        List<AbstractMap.SimpleEntry<UUID, Long>> rows = new ArrayList<>();
        try {
            File storeFile = new File(plugin.getDataFolder(), "treasury.yml");
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(storeFile);
            ConfigurationSection accounts = yml.getConfigurationSection("accounts");
            if (accounts != null) {
                for (String key : accounts.getKeys(false)) {
                    try {
                        UUID id = UUID.fromString(key);
                        long amt = accounts.getLong(key, 0L);
                        if (amt > 0) {
                            rows.add(new AbstractMap.SimpleEntry<>(id, amt));
                        }
                    } catch (IllegalArgumentException ignored) {
                        // bad UUID string; skip
                    }
                }
            }
        } catch (Exception ex) {
            plugin.getLogger().warning("[FFB] Leaderboard read failed: " + ex.getMessage());
        }

        // Sort desc by amount and cap to Top 10
        rows.sort(Comparator.comparingLong(AbstractMap.SimpleEntry<UUID, Long>::getValue).reversed());
        if (rows.size() > 10) rows = new ArrayList<>(rows.subList(0, 10));

        ItemBuilder b = ItemBuilder.of(Material.PAPER)
                .name(UiTheme.ACCENT + "Top Treasury Balances");

        if (rows.isEmpty()) {
            b.addLore(UiTheme.MUTED + "No balances recorded yet.");
        } else {
            int rank = 1;
            for (AbstractMap.SimpleEntry<UUID, Long> row : rows) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(row.getKey());
                String name = (op.getName() != null) ? op.getName() : row.getKey().toString().substring(0, 8);
                b.addLore(UiTheme.MUTED + "#" + rank + " " + name + " — "
                        + UiTheme.ACCENT + formatShillings(row.getValue()));
                rank++;
            }
        }
        return b.build();
    }

    private String formatShillings(long amount) {
        return String.format(Locale.US, "%,d %s", amount, Money.CURRENCY);
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
            // Slot 16 (Top Balances) is hover-only; no click action required.
            default -> {}
        }
    }
}
