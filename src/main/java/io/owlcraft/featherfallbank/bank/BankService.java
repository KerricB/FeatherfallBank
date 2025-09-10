package io.owlcraft.featherfallbank.bank;

import io.owlcraft.featherfallbank.economy.VaultEconomy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Service layer for Treasury (server bank) operations.
 * Wraps BankLedgerDao (SQLite) + VaultEconomy (player pouch) and provides
 * Player, UUID, and String convenience overloads.
 */
public class BankService {
    private final BankLedgerDao dao;   // SQLite DAO
    private final VaultEconomy vault;  // pouch bridge (Vault)
    private final Plugin plugin;       // for logging

    public BankService(BankLedgerDao dao, VaultEconomy vault, Plugin plugin) {
        this.dao = dao;
        this.vault = vault;
        this.plugin = plugin;
    }

    /* ===================== Reads ===================== */

    /** Read a player's Treasury balance (UUID-native). */
    public long getBank(UUID uuid) {
        try {
            return dao.getBalance(uuid);
        } catch (SQLException e) {
            plugin.getLogger().warning("[Bank] get failed: " + e.getMessage());
            return 0L;
        }
    }

    /** Convenience overloads. */
    public long getBank(String uuidStr) { return getBank(UUID.fromString(uuidStr)); }
    public long getBank(Player player)   { return getBank(player.getUniqueId()); }

    /* ===================== Mutations (low level) ===================== */

    public void add(UUID uuid, long delta) throws SQLException { dao.add(uuid, delta); }
    public void set(UUID uuid, long amount) throws SQLException { dao.set(uuid, amount); }

    /**
     * Withdraw from Treasury if funds available.
     * @return true if withdrawn in the ledger (no pouch side-effects here)
     */
    public boolean withdraw(UUID uuid, long amount) throws SQLException {
        return dao.withdraw(uuid, amount);
    }

    /* ===================== Mutations (Player-facing) ===================== */
    /**
     * Deposit Shillings from pouch to Treasury.
     * - withdraw from the player's Vault pouch
     * - add to the bank ledger
     */
    public boolean deposit(Player player, long amount) {
        if (amount <= 0) return false;

        // 1) take from pouch (Vault)
        if (!vault.withdraw(player, amount)) {
            return false; // insufficient pouch funds or provider error
        }

        // 2) add to Treasury (SQLite)
        try {
            dao.add(player.getUniqueId(), amount);
            return true;
        } catch (SQLException e) {
            // refund pouch on failure
            vault.deposit(player, amount);
            plugin.getLogger().warning("[Bank] deposit fail, refunding: " + e.getMessage());
            return false;
        }
    }

    /**
     * Withdraw Shillings from Treasury to pouch.
     * - withdraw from the bank ledger (if funds)
     * - deposit into the player's Vault pouch
     */
    public boolean withdraw(Player player, long amount) {
        if (amount <= 0) return false;

        try {
            // 1) take from Treasury if available
            if (!dao.withdraw(player.getUniqueId(), amount)) {
                return false; // insufficient bank funds
            }

            // 2) credit the pouch; if that somehow fails, best effort: put money back
            if (!vault.deposit(player, amount)) {
                // try to revert the ledger
                try { dao.add(player.getUniqueId(), amount); } catch (SQLException ignored) {}
                return false;
            }

            return true;
        } catch (SQLException e) {
            plugin.getLogger().warning("[Bank] withdraw fail: " + e.getMessage());
            return false;
        }
    }
}
