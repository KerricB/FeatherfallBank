package io.owlcraft.featherfallbank.treasury;

import io.owlcraft.featherfallbank.FeatherfallBankPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple YAML-backed treasury balances per player UUID.
 * Exposes deposit / withdraw / getBalance with the signatures used by menus.
 */
public class TreasuryService {

    private final FeatherfallBankPlugin plugin;

    private final Map<UUID, Long> balances = new ConcurrentHashMap<>();

    private File storeFile;
    private YamlConfiguration store;

    public TreasuryService(FeatherfallBankPlugin plugin) {
        this.plugin = plugin;
    }

    /** Load balances from plugins/FeatherfallBank/treasury.yml */
    public void load() {
        try {
            if (!plugin.getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                plugin.getDataFolder().mkdirs();
            }
            storeFile = new File(plugin.getDataFolder(), "treasury.yml");
            if (!storeFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                storeFile.createNewFile();
            }
            store = YamlConfiguration.loadConfiguration(storeFile);

            balances.clear();
            if (store.isConfigurationSection("accounts")) {
                for (String key : store.getConfigurationSection("accounts").getKeys(false)) {
                    try {
                        UUID id = UUID.fromString(key);
                        long v = store.getLong("accounts." + key, 0L);
                        if (v > 0) balances.put(id, v);
                    } catch (IllegalArgumentException ignored) {
                        // skip bad keys
                    }
                }
            }
            plugin.getLogger().info("[FFB] Treasury loaded " + balances.size() + " account(s).");
        } catch (IOException ex) {
            plugin.getLogger().severe("[FFB] Failed to load treasury.yml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /** Persist balances to disk. */
    public synchronized void save() {
        if (store == null) return;
        try {
            store.set("accounts", null); // clear section
            for (Map.Entry<UUID, Long> e : balances.entrySet()) {
                store.set("accounts." + e.getKey(), Math.max(0L, e.getValue()));
            }
            store.save(storeFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("[FFB] Failed to save treasury.yml: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /** Current treasury balance for a player (never negative). */
    public long getBalance(UUID id) {
        return Math.max(0L, balances.getOrDefault(id, 0L));
    }

    /** Add amount to treasury (no-op for non-positive). */
    public void deposit(UUID id, long amount) {
        if (amount <= 0) return;
        balances.merge(id, amount, Long::sum);
        save();
    }

    /**
     * Remove amount from treasury down to zero (no exception if insufficient).
     * If you want strict semantics, check/getBalance before calling.
     */
    public void withdraw(UUID id, long amount) {
        if (amount <= 0) return;
        long cur = getBalance(id);
        long next = Math.max(0L, cur - amount);
        balances.put(id, next);
        save();
    }

    /** Optional: set an explicit balance (clamped >= 0). */
    public void setBalance(UUID id, long newValue) {
        balances.put(id, Math.max(0L, newValue));
        save();
    }
}
