package io.owlcraft.featherfallbank.commands;

import io.owlcraft.featherfallbank.FeatherfallBankPlugin;
import io.owlcraft.featherfallbank.ui.ItemBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FFBCommand implements CommandExecutor, TabCompleter {
    private final FeatherfallBankPlugin plugin;

    public FFBCommand(FeatherfallBankPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ItemBuilder.color("&6FeatherfallBank &7— commands:"));
            sender.sendMessage(ItemBuilder.color("&e/pouch &7| &e/pouch drop <amount>"));
            sender.sendMessage(ItemBuilder.color("&e/ffb admin <give|take|set> <player> <amount> &7(perm: featherfallbank.admin)"));
            return true;
        }

        // /ffb admin -> forward to pouchadmin
        if (args[0].equalsIgnoreCase("admin")) {
            if (!sender.hasPermission("featherfallbank.admin")) {
                sender.sendMessage(ItemBuilder.color("&cNo permission."));
                return true;
            }
            String rest = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "";
            if (rest.isEmpty()) {
                sender.sendMessage(ItemBuilder.color("&eUsage: /ffb admin <give|take|set> <player> <amount>"));
                return true;
            }
            plugin.getServer().dispatchCommand(sender, "pouchadmin " + rest);
            return true;
        }

        sender.sendMessage(ItemBuilder.color("&7Unknown subcommand. Try &e/ffb &7or &e/ffb admin ..."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            List<String> opts = new ArrayList<>();
            if ("admin".startsWith(args[0].toLowerCase())) opts.add("admin");
            return opts;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            return Arrays.asList("give", "take", "set");
        }
        return Collections.emptyList();
    }
}
