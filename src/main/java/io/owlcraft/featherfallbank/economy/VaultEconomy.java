package io.owlcraft.featherfallbank.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class VaultEconomy {
    private final JavaPlugin plugin;
    private Economy econ; // may be null until hook succeeds

    public VaultEconomy(JavaPlugin plugin) {
        this.plugin = plugin;
        hookEconomy();                              // try right away
        if (econ == null) {                         // and try again next tick
            plugin.getServer().getScheduler().runTask(plugin, this::hookEconomy);
        }
    }

    private void hookEconomy() {
        if (econ != null) return;

        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("[VaultEconomy] Vault not found; economy features disabled.");
            return;
        }

        RegisteredServiceProvider<Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null || rsp.getProvider() == null) {
            plugin.getLogger().warning("[VaultEconomy] No Vault economy provider is registered yet. " +
                    "Is EssentialsX loaded? (This message can be harmless if it disappears after a tick.)");
            return;
        }

        econ = rsp.getProvider();
        plugin.getLogger().info("[VaultEconomy] Hooked economy: " + econ.getName());
    }

    public boolean isReady() {
        return econ != null;
    }

    // ---- helpers that gracefully no-op if econ is missing ----

    public long getBalanceAsLong(OfflinePlayer p) {
        if (econ == null) return 0L;
        return (long) Math.floor(econ.getBalance(p));
    }

    public boolean withdraw(Player p, long amount) {
        if (econ == null) return false;
        if (amount <= 0) return true;
        EconomyResponse r = econ.withdrawPlayer(p, amount);
        return r.transactionSuccess();
    }

    public boolean deposit(Player p, long amount) {
        if (econ == null) return false;
        if (amount <= 0) return true;
        EconomyResponse r = econ.depositPlayer(p, amount);
        return r.transactionSuccess();
    }
}
