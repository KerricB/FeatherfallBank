package io.owlcraft.featherfallbank.listeners;

import io.owlcraft.featherfallbank.FeatherfallBankPlugin;
import io.owlcraft.featherfallbank.ui.TreasuryMainMenu;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public final class BellInteractListener implements Listener {
    private final FeatherfallBankPlugin plugin;

    public BellInteractListener(FeatherfallBankPlugin plugin) {
        this.plugin = plugin;
    }

    private static String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    private boolean isRegistered(Location loc) {
        List<String> list = plugin.getConfig().getStringList("tellers");
        return list.contains(key(loc));
    }

    private void toggleRegister(Player p, Location loc) {
        List<String> list = plugin.getConfig().getStringList("tellers");
        String k = key(loc);
        boolean added;
        if (list.contains(k)) {
            list.remove(k);
            added = false;
        } else {
            list.add(k);
            added = true;
        }
        plugin.getConfig().set("tellers", list);
        plugin.saveConfig();

        if (added) {
            p.sendMessage("§a[FFB] Teller registered at §b" + k);
            p.playSound(loc, Sound.BLOCK_BELL_RESONATE, 0.8f, 1.8f);
        } else {
            p.sendMessage("§c[FFB] Teller unregistered at §b" + k);
            p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.7f);
        }
    }

    @EventHandler
    public void onBellUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block b = e.getClickedBlock();
        if (b == null || b.getType() != Material.BELL) return;

        Player p = e.getPlayer();
        Location loc = b.getLocation();

        // Admin quick (un)register: shift + right-click
        if (p.isSneaking() && p.hasPermission("ffb.teller.admin")) {
            e.setCancelled(true);
            toggleRegister(p, loc);
            return;
        }

        // Regular use: open GUI only for registered bells
        if (isRegistered(loc)) {
            e.setCancelled(true);
            Economy econ = plugin.economy();
            long pouch = (econ != null) ? (long) Math.floor(econ.getBalance(p)) : 0L;
            long tre = plugin.treasury().getBalance(p.getUniqueId());
            new TreasuryMainMenu(p, pouch, tre).open(p);
        }
    }
}
