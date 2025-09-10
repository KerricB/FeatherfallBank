package io.owlcraft.featherfallbank.economy;

import io.owlcraft.featherfallbank.FeatherfallBankPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private final FeatherfallBankPlugin plugin;
    private Economy economy;

    public VaultHook(FeatherfallBankPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() { return economy; }
}
