package io.owlcraft.featherfallbank.commands;

import io.owlcraft.featherfallbank.FeatherfallBankPlugin;
import io.owlcraft.featherfallbank.ui.ItemBuilder;
import io.owlcraft.featherfallbank.util.DropUtil;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PouchCommand implements CommandExecutor, TabCompleter {
    private final FeatherfallBankPlugin plugin;

    public PouchCommand(FeatherfallBankPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }

        Economy econ = plugin.economy();
        if (econ == null) {
            p.sendMessage(ItemBuilder.color("&cEconomy unavailable."));
            return true;
        }

        // /pouch — show ONLY the pouch amount (no treasury line)
        if (args.length == 0) {
            long pouch = (long) Math.floor(econ.getBalance(p));
            p.sendMessage(ItemBuilder.color("&6Your Pouch: &b" + ItemBuilder.money(pouch) + " &7Shillings"));
            return true;
        }

        // /pouch drop <amount>
        if (args.length >= 1 && args[0].equalsIgnoreCase("drop")) {
            if (args.length < 2) {
                p.sendMessage(ItemBuilder.color("&cUsage: &e/" + label + " drop <amount>"));
                return true;
            }

            long amount;
            try {
                amount = Long.parseLong(args[1]);
            } catch (NumberFormatException ex) {
                p.sendMessage(ItemBuilder.color("&cAmount must be a whole number."));
                return true;
            }

            if (amount <= 0) {
                p.sendMessage(ItemBuilder.color("&cAmount must be positive."));
                return true;
            }

            long pouch = (long) Math.floor(econ.getBalance(p));
            if (pouch < amount) {
                p.sendMessage(ItemBuilder.color("&cYou only have &e" + ItemBuilder.money(pouch) + " &cShillings in your pouch."));
                return true;
            }

            // Withdraw first — if this fails, nothing drops (prevents dupes)
            EconomyResponse resp = econ.withdrawPlayer(p, amount);
            if (resp == null || !resp.transactionSuccess()) {
                p.sendMessage(ItemBuilder.color("&cCould not withdraw that amount: &7" + (resp != null ? resp.errorMessage : "unknown error")));
                return true;
            }

            // Item tag for your PickupListener to recognize "money" items
            NamespacedKey keyCurrency = new NamespacedKey(FeatherfallBankPlugin.get(), "ffb_currency");

            // Split into stacks of 64, create the nugget item, and drop it IN FRONT with a short pickup guard
            long remaining = amount;
            while (remaining > 0) {
                int stack = (int) Math.min(remaining, 64);

                ItemStack is = new ItemStack(Material.GOLD_NUGGET, stack);
                ItemMeta meta = is.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ItemBuilder.color("&eShilling"));
                    meta.setLore(Arrays.asList(
                            ItemBuilder.color("&7FeatherfallBank Currency"),
                            ItemBuilder.color("&8Pick up to deposit")
                    ));
                    meta.getPersistentDataContainer().set(keyCurrency, PersistentDataType.STRING, "shilling");
                    is.setItemMeta(meta);
                }

                // ⬇️ This is the whole fix: forward offset + gentle push + self-pickup block
                DropUtil.dropInFront(plugin, p, is);

                remaining -= stack;
            }

            p.sendMessage(ItemBuilder.color("&aDropped &b" + ItemBuilder.money(amount) + " &7Shillings from your pouch."));
            return true;
        }

        // anything else -> just show help
        p.sendMessage(ItemBuilder.color("&7Try &e/" + label + " &7or &e/" + label + " drop <amount>"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> opts = new ArrayList<>();
            if ("drop".startsWith(args[0].toLowerCase())) opts.add("drop");
            return opts;
        }
        return Collections.emptyList();
    }
}
