package io.owlcraft.featherfallbank.commands;

import io.owlcraft.featherfallbank.FeatherfallBankPlugin;
import io.owlcraft.featherfallbank.ui.ItemBuilder;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Item;
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

            // Keys for item/entity tagging
            NamespacedKey keyCurrency   = new NamespacedKey(FeatherfallBankPlugin.get(), "ffb_currency");
            NamespacedKey keyDropper    = new NamespacedKey(FeatherfallBankPlugin.get(), "ffb_dropper");
            NamespacedKey keyBlockUntil = new NamespacedKey(FeatherfallBankPlugin.get(), "ffb_block_until");

            // Drop as tagged Shilling nuggets near the player, stacking up to 64
            long remaining = amount;
            while (remaining > 0) {
                int stack = (int) Math.min(remaining, 64);

                ItemStack is = new ItemStack(Material.GOLD_NUGGET, stack);
                // Tag the ITEM so PickupListener can recognize it
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

                // Spawn the item and tag the ENTITY so DropPickupGuardListener can delay pickup by dropper
                Item dropped = p.getWorld().dropItemNaturally(p.getLocation().add(0, 0.5, 0), is);
                dropped.getPersistentDataContainer().set(keyDropper, PersistentDataType.STRING, p.getUniqueId().toString());
                dropped.getPersistentDataContainer().set(keyBlockUntil, PersistentDataType.LONG, System.currentTimeMillis() + 1200L); // ~1.2s

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
