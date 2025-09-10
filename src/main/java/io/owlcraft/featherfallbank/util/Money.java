package io.owlcraft.featherfallbank.util;

import io.owlcraft.featherfallbank.FeatherfallBankPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Money utilities for FeatherfallBank.
 * Creates and inspects NBT-tagged "Shillings" items (IRON_NUGGET by default).
 */
public final class Money {
    private Money() {}

    /** Display name used across messages and lore */
    public static final String CURRENCY = "Shillings";

    /** PDC key storing the long amount on the item */
    private static NamespacedKey AMOUNT_KEY;

    private static NamespacedKey key(FeatherfallBankPlugin plugin) {
        if (AMOUNT_KEY == null) {
            AMOUNT_KEY = new NamespacedKey(plugin, "money_amount");
        }
        return AMOUNT_KEY;
    }

    /**
     * Create an NBT-tagged money token representing the given amount.
     * Material: IRON_NUGGET (matches your current nugget-based shillings)
     */
    public static ItemStack create(FeatherfallBankPlugin plugin, long amount) {
        if (amount <= 0) amount = 1;
        ItemStack stack = new ItemStack(Material.IRON_NUGGET);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color("&f" + amount + " &7" + CURRENCY));
            meta.setLore(List.of(
                    Text.color("&7Currency token"),
                    Text.color("&8" + amount + " " + CURRENCY)
            ));
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(key(plugin), PersistentDataType.LONG, amount);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /** True if the item is one of our money tokens (has the PDC key). */
    public static boolean isMoney(FeatherfallBankPlugin plugin, ItemStack stack) {
        if (stack == null) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(key(plugin), PersistentDataType.LONG);
    }

    /** Read the stored amount from a money token (0 if not a money item). */
    public static long getAmount(FeatherfallBankPlugin plugin, ItemStack stack) {
        if (stack == null) return 0L;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return 0L;
        Long v = meta.getPersistentDataContainer().get(key(plugin), PersistentDataType.LONG);
        return v == null ? 0L : v;
    }
}
