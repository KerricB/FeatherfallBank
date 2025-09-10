package io.owlcraft.featherfallbank.bank;

import java.io.File;
import java.sql.*;
import java.util.UUID;

/**
 * Very small SQLite DAO for player bank balances.
 * Table: bank_ledger(player TEXT PRIMARY KEY, amount INTEGER NOT NULL DEFAULT 0)
 */
public class BankLedgerDao implements AutoCloseable {
    private final Connection db;

    /* -------- Constructors: accept File or String path for compatibility -------- */
    public BankLedgerDao(File dataFolder) throws SQLException {
        this(dataFolder.getAbsolutePath());
    }
    public BankLedgerDao(String folderPath) throws SQLException {
        try {
            // Ensure folder exists (ok if it already does)
            new File(folderPath).mkdirs();
        } catch (Exception ignored) {}
        String url = "jdbc:sqlite:" + folderPath + File.separator + "bank.db";
        this.db = DriverManager.getConnection(url);
        initSchema();
    }
    /* --------------------------------------------------------------------------- */

    private void initSchema() throws SQLException {
        try (Statement st = db.createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS bank_ledger (" +
                            "  player TEXT PRIMARY KEY," +
                            "  amount INTEGER NOT NULL DEFAULT 0" +
                            ")"
            );
        }
    }

    /* ======================= UUID-native API ======================= */

    public long getBalance(UUID uuid) throws SQLException {
        return getBalance(uuid.toString());
    }

    public void add(UUID uuid, long delta) throws SQLException {
        add(uuid.toString(), delta);
    }

    public void set(UUID uuid, long amount) throws SQLException {
        set(uuid.toString(), amount);
    }

    /**
     * Withdraw if funds available; return true if successful, false if insufficient.
     */
    public boolean withdraw(UUID uuid, long amount) throws SQLException {
        return withdraw(uuid.toString(), amount);
    }

    /* ======================= String API (compat) ======================= */

    public long getBalance(String uuidStr) throws SQLException {
        try (PreparedStatement ps = db.prepareStatement(
                "SELECT amount FROM bank_ledger WHERE player = ?")) {
            ps.setString(1, uuidStr);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        return 0L;
    }

    public void add(String uuidStr, long delta) throws SQLException {
        // upsert: insert new row if missing, then add delta
        db.setAutoCommit(false);
        try {
            ensureRow(uuidStr);
            try (PreparedStatement ps = db.prepareStatement(
                    "UPDATE bank_ledger SET amount = amount + ? WHERE player = ?")) {
                ps.setLong(1, delta);
                ps.setString(2, uuidStr);
                ps.executeUpdate();
            }
            db.commit();
        } catch (SQLException ex) {
            db.rollback();
            throw ex;
        } finally {
            db.setAutoCommit(true);
        }
    }

    public void set(String uuidStr, long amount) throws SQLException {
        try (PreparedStatement ps = db.prepareStatement(
                "INSERT INTO bank_ledger(player, amount) VALUES(?, ?) " +
                        "ON CONFLICT(player) DO UPDATE SET amount = excluded.amount")) {
            ps.setString(1, uuidStr);
            ps.setLong(2, amount);
            ps.executeUpdate();
        }
    }

    public boolean withdraw(String uuidStr, long amount) throws SQLException {
        db.setAutoCommit(false);
        try {
            long current = getBalance(uuidStr);
            if (current < amount) {
                db.rollback();
                db.setAutoCommit(true);
                return false;
            }
            try (PreparedStatement ps = db.prepareStatement(
                    "UPDATE bank_ledger SET amount = amount - ? WHERE player = ?")) {
                ps.setLong(1, amount);
                ps.setString(2, uuidStr);
                ps.executeUpdate();
            }
            db.commit();
            return true;
        } catch (SQLException ex) {
            db.rollback();
            throw ex;
        } finally {
            db.setAutoCommit(true);
        }
    }

    private void ensureRow(String uuidStr) throws SQLException {
        try (PreparedStatement ps = db.prepareStatement(
                "INSERT INTO bank_ledger(player, amount) VALUES(?, 0) " +
                        "ON CONFLICT(player) DO NOTHING")) {
            ps.setString(1, uuidStr);
            ps.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        if (db != null && !db.isClosed()) db.close();
    }
}
