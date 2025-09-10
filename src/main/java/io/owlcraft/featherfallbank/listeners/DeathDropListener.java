package io.owlcraft.featherfallbank.listeners;

import io.owlcraft.featherfallbank.FeatherfallBankPlugin;
import io.owlcraft.featherfallbank.economy.VaultEconomy;
import io.owlcraft.featherfallbank.util.Money;
import io.owlcraft.featherfallbank.util.RegionGuard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Drops 50% of a player's Pouch (Vault) as a single NBT-tagged "Shillings" nugget on death,
 * unless RegionGuard says the location is safe (stub currently returns false = allow).
 */
public class DeathDropListener implements Listener {

    private final FeatherfallBankPlugin plugin;
    private final VaultEconomy vault;
    private final RegionGuard regionguard;

    public DeathDropListener(FeatherfallBankPlugin plugin,
                             VaultEconomy vault,
                             RegionGuard regionguard) {
        this.plugin = plugin;
        this.vault = vault;
        this.regionguard = regionguard;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        // Skip if in a "safe" region (RegionGuard is a stub for now, returns false)
        if (regionguard != null && regionguard.isInSafeRegion(p.getLocation())) {
            plugin.getLogger().fine("[FFB] DeathDrop skipped (safe region).");
            return;
        }

        long wallet = vault.getBalanceAsLong(p);
        long toDrop = wallet / 2L; // 50%
        if (toDrop <= 0) return;

        // Remove from pouch first
        vault.withdraw(p, toDrop);

        // Create money nugget and drop it
        ItemStack coin = Money.create(plugin, toDrop);
        Item dropped = p.getWorld().dropItemNaturally(p.getLocation(), coin);
        dropped.setPickupDelay(20);

        // Debug snapshot of what landed
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            long nbt = Money.getAmount(plugin, dropped.getItemStack());
            plugin.getLogger().info("[FFB] DeathDrop -> dropped token amount=" + nbt);
        }, 1L);
    }
}
