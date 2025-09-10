package io.owlcraft.featherfallbank.commands;

import io.owlcraft.featherfallbank.bank.BankService;
import io.owlcraft.featherfallbank.economy.VaultEconomy;
import io.owlcraft.featherfallbank.util.RegionGuard;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /bank [balance|deposit|withdraw] [amount|max]
 * Phase 1: runs anywhere; in Phase 2 we’ll gate to Treasury region.
 */
public class BankCommand implements CommandExecutor {
    private final BankService bank;
    private final VaultEconomy vault;
    private final RegionGuard guard;

    public BankCommand(org.bukkit.plugin.Plugin plugin,
                       BankService bank,
                       VaultEconomy vault,
                       RegionGuard guard) {
        this.bank = bank;
        this.vault = vault;
        this.guard = guard;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Player only."); return true; }
        if (!p.hasPermission("owlbank.use")) { p.sendMessage("§cNo permission."); return true; }

        // Phase 2: enable this gate
        // if (!guard.isInTreasuryRegion(p.getLocation())) {
        //     p.sendMessage("§eYou must be inside The Treasury to do that.");
        //     return true;
        // }

        if (args.length == 0 || args[0].equalsIgnoreCase("balance")) {
            long bankBal = bank.getBank(p.getUniqueId());
            long wallet  = vault.getBalanceAsLong(p);
            p.sendMessage("§6[The Treasury] §fBank: §a" + fmt(bankBal) + " §7| Pouch: §a" + fmt(wallet));
            p.sendMessage("§7Use §e/bank deposit <amount|max> §7or §e/bank withdraw <amount|max>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "deposit" -> {
                long wallet = vault.getBalanceAsLong(p);
                long amt = parseAmount(args, 1, wallet);
                if (amt <= 0) { p.sendMessage("§cNot enough Shillings in your Pouch."); return true; }
                if (bank.deposit(p, amt))
                    p.sendMessage("§aDeposited " + fmt(amt) + " into The Treasury.");
                else
                    p.sendMessage("§cDeposit failed (insufficient Pouch or provider error).");
            }
            case "withdraw" -> {
                long bankBal = bank.getBank(p.getUniqueId());
                long amt = parseAmount(args, 1, bankBal);
                if (amt <= 0) { p.sendMessage("§cNot enough Shillings in The Treasury."); return true; }
                if (bank.withdraw(p, amt))
                    p.sendMessage("§aWithdrew " + fmt(amt) + " to your Pouch.");
                else
                    p.sendMessage("§cWithdraw failed (insufficient Treasury or provider error).");
            }
            default -> p.sendMessage("§7Use §e/bank [balance|deposit|withdraw] [amount|max]");
        }
        return true;
    }

    private long parseAmount(String[] args, int idx, long max) {
        if (args.length <= idx) return -1;
        if (args[idx].equalsIgnoreCase("max")) return Math.max(0, max);
        try {
            long v = Long.parseLong(args[idx]);
            return (v < 1 || v > max) ? -1 : v;
        } catch (NumberFormatException e) { return -1; }
    }

    private String fmt(long n) {
        return String.format("%,d Shillings", n);
    }
}
