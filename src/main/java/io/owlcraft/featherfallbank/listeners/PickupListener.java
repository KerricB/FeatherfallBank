package io.owlcraft.featherfallbank.listeners;

import io.owlcraft.featherfallbank.FeatherfallBankPlugin;
import io.owlcraft.featherfallbank.util.Money; // adjust if your Money util lives elsewhere

import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PickupListener implements Listener {

    private final FeatherfallBankPlugin plugin;
    private final NamespacedKey keyCurrency; // FFB item meta tag

    public PickupListener(FeatherfallBankPlugin plugin) {
        this.plugin = plugin;
        this.keyCurrency = new NamespacedKey(plugin, "ffb_currency");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        ItemStack stack = e.getItem().getItemStack();

        // Path A: legacy/custom Money items your util recognizes
        if (Money.isMoney(plugin, stack)) {
            long per = Money.getAmount(plugin, stack);
            if (per <= 0) return;
            long total = per * Math.max(1, stack.getAmount());
            Economy econ = plugin.economy();
            if (econ == null) return;

            e.setCancelled(true);
            e.getItem().remove();
            econ.depositPlayer(p, total);
            try {
                p.sendActionBar(Component.text("+" + total + " Shillings to your Pouch"));
            } catch (Throwable ignored) {
                p.sendMessage(ChatColor.GREEN + "+" + total + " Shillings to your Pouch");
            }
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.0f);
            return;
        }

        // Path B: FFB-tagged shilling nuggets (safe; normal nuggets ignored)
        if (isFFBShilling(stack)) {
            long per = 1L; // 1 shilling per nugget
            long total = per * Math.max(1, stack.getAmount());
            Economy econ = plugin.economy();
            if (econ == null) return;

            e.setCancelled(true);
            e.getItem().remove();
            econ.depositPlayer(p, total);
            try {
                p.sendActionBar(Component.text("+" + total + " Shillings to your Pouch"));
            } catch (Throwable ignored) {
                p.sendMessage(ChatColor.GREEN + "+" + total + " Shillings to your Pouch");
            }
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.0f);
        }
        // else: normal gold or other items flow through to inventory
    }

    private boolean isFFBShilling(ItemStack stack) {
        if (stack == null || stack.getType() != Material.GOLD_NUGGET) return false;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;

        // Strong signal: PDC tag
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String tag = pdc.getOrDefault(keyCurrency, PersistentDataType.STRING, "");
        if ("shilling".equalsIgnoreCase(tag)) return true;

        // Soft back-compat: visible name "Shilling"
        String dn = ChatColor.stripColor(meta.getDisplayName() == null ? "" : meta.getDisplayName()).trim();
        return dn.equalsIgnoreCase("Shilling");
    }
}
