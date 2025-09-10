package io.owlcraft.featherfallbank.drops;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * CashPouchFactory
 * ----------------
 * Creates a "Feather Pouch" item that securely holds a Shillings amount in NBT (PDC),
 * and reads it back on pickup. Display name/lore are cosmetic; only NBT is trusted.
 */
public class CashPouchFactory {
    private final Plugin plugin;
    private final NamespacedKey AMOUNT_KEY;

    public CashPouchFactory(Plugin plugin) {
        this.plugin = plugin;
        this.AMOUNT_KEY = new NamespacedKey(plugin, "pouchAmount"); // unique key for NBT
    }

    /** Create a Feather Pouch item representing {amount} Shillings. */
    public ItemStack create(long amount) {
        FileConfiguration cfg = plugin.getConfig();

        // Configurable material (defaults to PAPER)
        Material mat = Material.getMaterial(cfg.getString("ux.coin_item.material", "PAPER"));
        if (mat == null) mat = Material.IRON_NUGGET;

        ItemStack is = new ItemStack(mat);
        ItemMeta meta = is.getItemMeta();

        // Cosmetic name + lore (color codes with &)
        String name = cfg.getString("ux.coin_item.name", "&6Feather Pouch").replace('&', '§');
        meta.setDisplayName(name);

        List<String> lore = cfg.getStringList("ux.coin_item.lore_template").stream()
                .map(s -> s.replace('&', '§').replace("{amount}", String.valueOf(amount)))
                .toList();
        meta.setLore(lore);

        // 🔒 Store the true value in NBT (server-trusted)
        meta.getPersistentDataContainer().set(AMOUNT_KEY, PersistentDataType.LONG, amount);
        is.setItemMeta(meta);
        return is;
    }

    /** Read the Shillings amount from a Feather Pouch. Returns null if not a valid pouch. */
    public Long readAmount(ItemStack is) {
        if (is == null || !is.hasItemMeta()) return null;
        ItemMeta meta = is.getItemMeta();
        Long amt = meta.getPersistentDataContainer().get(AMOUNT_KEY, PersistentDataType.LONG);
        return (amt == null || amt <= 0) ? null : amt;
    }
}
