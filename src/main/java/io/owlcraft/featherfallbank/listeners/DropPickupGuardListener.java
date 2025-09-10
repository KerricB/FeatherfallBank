package io.owlcraft.featherfallbank.listeners;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class DropPickupGuardListener implements Listener {
    private final NamespacedKey keyDropper;
    private final NamespacedKey keyBlockUntil;

    public DropPickupGuardListener(JavaPlugin plugin) {
        this.keyDropper = new NamespacedKey(plugin, "ffb_dropper");
        this.keyBlockUntil = new NamespacedKey(plugin, "ffb_block_until");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        Item item = e.getItem();
        PersistentDataContainer pdc = item.getPersistentDataContainer();
        String dropper = pdc.get(keyDropper, PersistentDataType.STRING);
        Long blockUntil = pdc.get(keyBlockUntil, PersistentDataType.LONG);

        if (dropper == null || blockUntil == null) return;

        // Block only the original dropper for a short time window
        if (p.getUniqueId().toString().equals(dropper) && System.currentTimeMillis() < blockUntil) {
            e.setCancelled(true);
        }
    }
}
