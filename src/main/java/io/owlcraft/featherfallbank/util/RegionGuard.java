package io.owlcraft.featherfallbank.util;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Placeholder RegionGuard until we wire real WorldGuard logic.
 * Uses JavaPlugin instead of the concrete FeatherfallBankPlugin to avoid hard coupling.
 */
public class RegionGuard {

    private final JavaPlugin plugin;

    public RegionGuard(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Return true if the given location is in a "safe" region
     * where pouch loss / drops should be prevented.
     *
     * Current implementation: returns false (no blocking),
     * so gameplay continues unchanged until WG support is added.
     */
    public boolean isInSafeRegion(Location loc) {
        if (loc == null) return false;

        // If you want a quick toggle before full WG, you can uncomment:
        // boolean enabled = plugin.getConfig().getBoolean("treasury.region.enabled", false);
        // if (!enabled) return false;
        // String name = plugin.getConfig().getString("treasury.region.name", "");
        // TODO: integrate with WorldGuard here later.

        return false;
    }
}
