package io.owlcraft.featherfallbank.listeners;

import io.owlcraft.featherfallbank.FeatherfallBankPlugin;
import org.bukkit.event.Listener;

/**
 * Reserved for future teller-block features.
 * Currently a no-op so the project compiles cleanly.
 */
public class TellerBlockListener implements Listener {

    private final FeatherfallBankPlugin plugin;

    public TellerBlockListener(FeatherfallBankPlugin plugin) {
        this.plugin = plugin;
    }
}
