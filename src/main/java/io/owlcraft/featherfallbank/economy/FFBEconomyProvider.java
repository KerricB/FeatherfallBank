package io.owlcraft.featherfallbank.economy;

import io.owlcraft.featherfallbank.FeatherfallBankPlugin;
import io.owlcraft.featherfallbank.pouch.PouchService;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FFBEconomyProvider implements Economy {

    private final FeatherfallBankPlugin plugin;
    private final PouchService pouch;

    public FFBEconomyProvider(FeatherfallBankPlugin plugin, PouchService pouch) {
        this.plugin = plugin;
        this.pouch = pouch;
    }

    @Override public boolean isEnabled() { return plugin.isEnabled(); }
    @Override public String getName() { return "FeatherfallBank"; }
    @Override public boolean hasBankSupport() { return false; }
    @Override public int fractionalDigits() { return 0; } // whole shillings only

    @Override public String currencyNamePlural() { return plugin.getConfig().getString("currency.name_plural", "Shillings"); }
    @Override public String currencyNameSingular() { return plugin.getConfig().getString("currency.name_singular", "Shilling"); }
    @Override public String format(double amount) {
        long v = floorNonNegative(amount);
        return v + " " + (v == 1 ? currencyNameSingular() : currencyNamePlural());
    }

    @Override public boolean hasAccount(OfflinePlayer player) { return true; }
    @Override public boolean hasAccount(String player) { return true; }
    @Override public boolean hasAccount(OfflinePlayer p, String w) { return true; }
    @Override public boolean hasAccount(String p, String w) { return true; }
    @Override public boolean createPlayerAccount(OfflinePlayer p) { return true; }
    @Override public boolean createPlayerAccount(String p) { return true; }
    @Override public boolean createPlayerAccount(OfflinePlayer p, String w) { return true; }
    @Override public boolean createPlayerAccount(String p, String w) { return true; }

    private static boolean bad(double d) {
        return Double.isNaN(d) || Double.isInfinite(d);
    }
    private static long floorNonNegative(double d) {
        if (bad(d)) return 0L;
        return Math.max(0L, (long) Math.floor(d));
    }
    private static long ceilNonNegative(double d) {
        if (bad(d)) return 0L;
        return Math.max(0L, (long) Math.ceil(d));
    }

    private UUID id(OfflinePlayer p) { return p.getUniqueId(); }

    @Override public double getBalance(OfflinePlayer p) { return pouch.get(id(p)); }
    @Override public double getBalance(String name) { return getBalance(Bukkit.getOfflinePlayer(name)); }
    @Override public double getBalance(OfflinePlayer p, String w) { return getBalance(p); }
    @Override public double getBalance(String name, String w) { return getBalance(name); }

    // Use CEIL so requesting 1.1 requires at least 2 when there are no fractional digits.
    @Override public boolean has(OfflinePlayer p, double amt) {
        long need = ceilNonNegative(amt);
        return pouch.get(id(p)) >= need;
    }
    @Override public boolean has(String name, double amt) { return has(Bukkit.getOfflinePlayer(name), amt); }
    @Override public boolean has(OfflinePlayer p, String w, double amt) { return has(p, amt); }
    @Override public boolean has(String name, String w, double amt) { return has(name, amt); }

    @Override public EconomyResponse depositPlayer(OfflinePlayer p, double amt) {
        long a = floorNonNegative(amt);
        if (a <= 0) {
            // treat zero/negative/NaN/∞ as failure to be strict
            return new EconomyResponse(amt, getBalance(p), EconomyResponse.ResponseType.FAILURE, "amount must be > 0");
        }
        pouch.add(id(p), a);
        return new EconomyResponse(a, getBalance(p), EconomyResponse.ResponseType.SUCCESS, "");
    }
    @Override public EconomyResponse depositPlayer(String name, double amt) { return depositPlayer(Bukkit.getOfflinePlayer(name), amt); }
    @Override public EconomyResponse depositPlayer(OfflinePlayer p, String w, double amt) { return depositPlayer(p, amt); }
    @Override public EconomyResponse depositPlayer(String name, String w, double amt) { return depositPlayer(name, amt); }

    // ✅ Atomic withdraw: fail if balance < amount (no partial deduction, no overdraft).
    @Override public EconomyResponse withdrawPlayer(OfflinePlayer p, double amt) {
        long a = floorNonNegative(amt);
        if (a <= 0) {
            return new EconomyResponse(amt, getBalance(p), EconomyResponse.ResponseType.FAILURE, "amount must be > 0");
        }

        long bal = pouch.get(id(p));
        if (bal < a) {
            return new EconomyResponse(0, bal, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }

        boolean ok = pouch.take(id(p), a);
        if (!ok) {
            // race/edge case fallback: re-read and report accurate state
            bal = pouch.get(id(p));
            return new EconomyResponse(0, bal, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
        }
        return new EconomyResponse(a, getBalance(p), EconomyResponse.ResponseType.SUCCESS, "");
    }
    @Override public EconomyResponse withdrawPlayer(String name, double amt) { return withdrawPlayer(Bukkit.getOfflinePlayer(name), amt); }
    @Override public EconomyResponse withdrawPlayer(OfflinePlayer p, String w, double amt) { return withdrawPlayer(p, amt); }
    @Override public EconomyResponse withdrawPlayer(String name, String w, double amt) { return withdrawPlayer(name, amt); }

    // Banks not used
    @Override public EconomyResponse createBank(String n, String p) { return notImpl(); }
    @Override public EconomyResponse createBank(String n, OfflinePlayer p) { return notImpl(); }
    @Override public EconomyResponse deleteBank(String n) { return notImpl(); }
    @Override public EconomyResponse bankBalance(String n) { return notImpl(); }
    @Override public EconomyResponse bankHas(String n, double a) { return notImpl(); }
    @Override public EconomyResponse bankWithdraw(String n, double a) { return notImpl(); }
    @Override public EconomyResponse bankDeposit(String n, double a) { return notImpl(); }
    @Override public EconomyResponse isBankOwner(String n, String p) { return notImpl(); }
    @Override public EconomyResponse isBankOwner(String n, OfflinePlayer p) { return notImpl(); }
    @Override public EconomyResponse isBankMember(String n, String p) { return notImpl(); }
    @Override public EconomyResponse isBankMember(String n, OfflinePlayer p) { return notImpl(); }
    @Override public List<String> getBanks() { return Collections.emptyList(); }

    private EconomyResponse notImpl() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not used");
    }
}
