package io.owlcraft.featherfallbank;

import io.owlcraft.featherfallbank.commands.FFBCommand;           // <-- NEW
import io.owlcraft.featherfallbank.commands.PouchAdminCommand;    // <-- already added
import io.owlcraft.featherfallbank.commands.PouchCommand;
import io.owlcraft.featherfallbank.economy.FFBEconomyProvider;
import io.owlcraft.featherfallbank.economy.VaultEconomy;
import io.owlcraft.featherfallbank.listeners.BellInteractListener;
import io.owlcraft.featherfallbank.listeners.DeathDropListener;
import io.owlcraft.featherfallbank.listeners.DropPickupGuardListener;
import io.owlcraft.featherfallbank.listeners.MenuClickRouter;      // global GUI router
import io.owlcraft.featherfallbank.listeners.PickupListener;
import io.owlcraft.featherfallbank.pouch.PouchService;
import io.owlcraft.featherfallbank.treasury.TreasuryService;
import io.owlcraft.featherfallbank.util.RegionGuard;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class FeatherfallBankPlugin extends JavaPlugin implements Listener {

    private static FeatherfallBankPlugin instance;

    private TreasuryService treasuryService;

    // Pouch + Vault provider
    private PouchService pouchService;
    private FFBEconomyProvider vaultProvider;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // Safety net: make sure the tellers list exists
        if (!getConfig().isList("tellers")) {
            getConfig().set("tellers", new java.util.ArrayList<String>());
            saveConfig();
        }

        copyIfBundled("messages.yml");
        copyIfBundled("README-Server.md");

        // --- Services ---
        pouchService = new PouchService(getDataFolder());

        // Register as Vault economy (we want to be the active one)
        vaultProvider = new FFBEconomyProvider(this, pouchService);
        ensureVaultProviderRegistered("onEnable");

        // Treasury store
        treasuryService = new TreasuryService(this);
        treasuryService.load();

        // Listeners
        Bukkit.getPluginManager().registerEvents(new BellInteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PickupListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DropPickupGuardListener(this), this);

        // Death drop behavior
        VaultEconomy ve = new VaultEconomy(this);
        RegionGuard rg = new RegionGuard(this);
        Bukkit.getPluginManager().registerEvents(new DeathDropListener(this, ve, rg), this);

        // One router to handle all GUI clicks (Main/Deposit/Withdraw/Help)
        Bukkit.getPluginManager().registerEvents(
                new MenuClickRouter(
                        this::getPouchBalance,       // getPouch(Player) -> long
                        this::getTreasuryBalance,    // getTreasury(Player) -> long
                        this::movePouchToTreasury,   // deposit(Player, amount)  pouch -> treasury
                        this::moveTreasuryToPouch,   // withdraw(Player, amount) treasury -> pouch
                        this::quickDepositAll        // quickDepositAll(Player)
                ),
                this
        );

        // Commands
        PouchCommand pouch = new PouchCommand(this);
        if (getCommand("pouch") != null) {
            getCommand("pouch").setExecutor(pouch);
            getCommand("pouch").setTabCompleter(pouch);
        } else {
            getLogger().severe("Command 'pouch' missing from plugin.yml!");
        }

        // /pouchadmin give|take|set (always routes through FFB/Vault)
        PouchAdminCommand pouchAdmin = new PouchAdminCommand(this);
        if (getCommand("pouchadmin") != null) {
            getCommand("pouchadmin").setExecutor(pouchAdmin);
        } else {
            getLogger().severe("Command 'pouchadmin' missing from plugin.yml!");
        }

        // NEW: /ffb root command (help + forwards `/ffb admin ...` to pouchadmin)
        FFBCommand ffb = new FFBCommand(this);
        if (getCommand("ffb") != null) {
            getCommand("ffb").setExecutor(ffb);
            getCommand("ffb").setTabCompleter(ffb);
        } else {
            getLogger().severe("Command 'ffb' missing from plugin.yml!");
        }

        // Listen for late plugin enables / service changes that might try to override us
        Bukkit.getPluginManager().registerEvents(this, this);

        // Re-check shortly after startup (in case something registered after us)
        Bukkit.getScheduler().runTaskLater(this, () -> ensureVaultProviderRegistered("delayed-check-40t"), 40L);

        int tellerCount = getConfig().getStringList("tellers").size();
        getLogger().info("[FFB] Loaded " + tellerCount + " teller bell(s) from config.");
        getLogger().info("FeatherfallBank enabled. Registered Pouch-backed Vault economy.");
    }

    @Override
    public void onDisable() {
        if (treasuryService != null) {
            treasuryService.save();
        }
        if (vaultProvider != null) {
            getServer().getServicesManager().unregister(Economy.class, vaultProvider);
        }
    }

    // --- Keep us active if other plugins register later ---
    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent e) {
        if (e.getProvider().getService().equals(Economy.class)) {
            Bukkit.getScheduler().runTask(this, () -> ensureVaultProviderRegistered("ServiceRegisterEvent"));
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e) {
        String n = e.getPlugin().getName().toLowerCase();
        if (n.contains("essential")) {
            Bukkit.getScheduler().runTask(this, () -> ensureVaultProviderRegistered("PluginEnable:" + e.getPlugin().getName()));
        }
    }

    private void ensureVaultProviderRegistered(String reason) {
        ServicesManager sm = getServer().getServicesManager();
        RegisteredServiceProvider<Economy> rsp = sm.getRegistration(Economy.class);
        Economy current = (rsp != null) ? rsp.getProvider() : null;

        if (!(current instanceof FFBEconomyProvider)) {
            getLogger().warning("[Vault] Active economy is "
                    + (current != null ? current.getName() + " (" + current.getClass().getName() + ")" : "NONE")
                    + " — re-registering FeatherfallBank (" + reason + ").");
            sm.register(Economy.class, vaultProvider, this, ServicePriority.Highest);
        } else {
            getLogger().info("[Vault] FeatherfallBank is the active economy (" + reason + ").");
        }
    }

    private void copyIfBundled(String resourceName) {
        if (getResource(resourceName) == null) return;
        File out = new File(getDataFolder(), resourceName);
        if (!out.exists()) saveResource(resourceName, false);
    }

    public static FeatherfallBankPlugin get() { return instance; }
    public TreasuryService treasury() { return treasuryService; }
    public PouchService pouch() { return pouchService; }
    public FileConfiguration cfg() { return getConfig(); }
    public Economy economy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        return (rsp != null) ? rsp.getProvider() : null;
    }

    // =========================================================================
    // GUI <-> Service bridge helpers (used by MenuClickRouter)
    // =========================================================================

    private long getPouchBalance(Player p) {
        // We require Vault (provided by FFBEconomyProvider) for pouch I/O
        Economy econ = economy();
        if (econ == null) {
            getLogger().warning("[FFB] Vault economy unavailable while reading pouch. Returning 0.");
            return 0L;
        }
        return (long) Math.floor(econ.getBalance(p)); // Vault uses double
    }

    private long getTreasuryBalance(Player p) {
        return treasuryService.getBalance(p.getUniqueId());
    }

    /** Move Shillings from Pouch (Vault) to Treasury store. */
    private void movePouchToTreasury(Player p, long amount) {
        if (amount <= 0) return;
        UUID id = p.getUniqueId();

        long pouchBal = getPouchBalance(p);
        if (pouchBal <= 0) return;
        amount = Math.min(amount, pouchBal);

        Economy econ = economy();
        if (econ == null) {
            getLogger().warning("[FFB] Vault economy unavailable during deposit; aborting.");
            return;
        }

        var resp = econ.withdrawPlayer(p, amount);
        if (resp != null && resp.transactionSuccess()) {
            treasuryService.deposit(id, amount);
        }
    }

    /** Move Shillings from Treasury store back into Pouch (Vault). */
    private void moveTreasuryToPouch(Player p, long amount) {
        if (amount <= 0) return;
        UUID id = p.getUniqueId();

        long treBal = treasuryService.getBalance(id);
        if (treBal <= 0) return;
        amount = Math.min(amount, treBal);

        treasuryService.withdraw(id, amount);

        Economy econ = economy();
        if (econ == null) {
            getLogger().warning("[FFB] Vault economy unavailable during withdraw; treasury was debited.");
            return;
        }
        econ.depositPlayer(p, amount);
    }

    /** Convenience: deposit the player's entire pouch into the Treasury. */
    private void quickDepositAll(Player p) {
        long pouchBal = getPouchBalance(p);
        if (pouchBal > 0) movePouchToTreasury(p, pouchBal);
    }
}
