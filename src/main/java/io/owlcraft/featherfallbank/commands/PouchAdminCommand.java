package io.owlcraft.featherfallbank.commands;

import io.owlcraft.featherfallbank.FeatherfallBankPlugin;
import io.owlcraft.featherfallbank.ui.ItemBuilder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class PouchAdminCommand implements CommandExecutor {
    private final FeatherfallBankPlugin plugin;

    public PouchAdminCommand(FeatherfallBankPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("featherfallbank.admin")) {
            sender.sendMessage(ItemBuilder.color("&cNo permission."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(ItemBuilder.color("&eUsage: /pouchadmin <give|take|set> <player> <amount>"));
            return true;
        }

        String sub = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        long amount;
        try { amount = Long.parseLong(args[2]); } catch (NumberFormatException e) {
            sender.sendMessage(ItemBuilder.color("&cAmount must be a whole number.")); return true;
        }
        if (amount < 0) { sender.sendMessage(ItemBuilder.color("&cAmount must be >= 0.")); return true; }

        Economy econ = plugin.economy();
        if (econ == null) {
            sender.sendMessage(ItemBuilder.color("&cVault economy unavailable."));
            return true;
        }

        switch (sub) {
            case "give" -> {
                var resp = econ.depositPlayer(target, amount);
                if (resp.transactionSuccess()) {
                    sender.sendMessage(ItemBuilder.color("&aGave &b" + ItemBuilder.money(amount)
                            + " &7to &e" + target.getName() + "&7. New pouch: &b" + ItemBuilder.money((long) resp.balance)));
                } else sender.sendMessage(ItemBuilder.color("&cFailed: &7" + resp.errorMessage));
            }
            case "take" -> {
                var resp = econ.withdrawPlayer(target, amount);
                if (resp.transactionSuccess()) {
                    sender.sendMessage(ItemBuilder.color("&aTook &b" + ItemBuilder.money(amount)
                            + " &7from &e" + target.getName() + "&7. New pouch: &b" + ItemBuilder.money((long) resp.balance)));
                } else sender.sendMessage(ItemBuilder.color("&cFailed: &7" + resp.errorMessage));
            }
            case "set" -> {
                // Implement set via withdraw/deposit relative to current
                long cur = (long) Math.floor(econ.getBalance(target));
                if (cur == amount) {
                    sender.sendMessage(ItemBuilder.color("&7No change. Pouch already &b" + ItemBuilder.money(cur)));
                } else if (cur < amount) {
                    var resp = econ.depositPlayer(target, amount - cur);
                    sender.sendMessage(ItemBuilder.color(resp.transactionSuccess()
                            ? "&aSet pouch to &b" + ItemBuilder.money((long) resp.balance)
                            : "&cFailed: &7" + resp.errorMessage));
                } else {
                    var resp = econ.withdrawPlayer(target, cur - amount);
                    sender.sendMessage(ItemBuilder.color(resp.transactionSuccess()
                            ? "&aSet pouch to &b" + ItemBuilder.money((long) resp.balance)
                            : "&cFailed: &7" + resp.errorMessage));
                }
            }
            default -> sender.sendMessage(ItemBuilder.color("&eUsage: /pouchadmin <give|take|set> <player> <amount>"));
        }
        return true;
    }
}
