package io.owlcraft.featherfallbank.util;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public final class DropUtil {
    private DropUtil() {}

    public static final double FORWARD_BLOCKS = 1.25;
    public static final double LIFT_BLOCKS = 0.20;
    public static final double PUSH_SPEED = 0.25;
    public static final long   BLOCK_SELF_MS = 1500L;

    public static Item dropInFront(JavaPlugin plugin, Player p, ItemStack stack) {
        Location base = p.getLocation();
        Vector dir = base.getDirection().normalize();
        Location spawn = base.clone().add(dir.multiply(FORWARD_BLOCKS)).add(0, LIFT_BLOCKS, 0);

        Item item = p.getWorld().dropItem(spawn, stack);
        item.setVelocity(dir.multiply(PUSH_SPEED).setY(0.05)); // gentle forward toss
        item.setPickupDelay(2); // small universal delay to avoid jitter

        // Tag for the pickup-guard listener
        PersistentDataContainer pdc = item.getPersistentDataContainer();
        pdc.set(new NamespacedKey(plugin, "ffb_dropper"), PersistentDataType.STRING, p.getUniqueId().toString());
        pdc.set(new NamespacedKey(plugin, "ffb_block_until"), PersistentDataType.LONG, System.currentTimeMillis() + BLOCK_SELF_MS);
        return item;
    }
}
