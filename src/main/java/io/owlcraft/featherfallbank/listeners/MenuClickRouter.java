package io.owlcraft.featherfallbank.listeners;

import io.owlcraft.featherfallbank.ui.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public final class MenuClickRouter implements Listener {

    private final Function<Player, Long> getPouch;
    private final Function<Player, Long> getTreasury;
    private final BiConsumer<Player, Long> deposit;   // pouch -> treasury
    private final BiConsumer<Player, Long> withdraw;  // treasury -> pouch
    private final Consumer<Player> quickDepositAll;   // optional convenience action

    public MenuClickRouter(Function<Player, Long> getPouch,
                           Function<Player, Long> getTreasury,
                           BiConsumer<Player, Long> deposit,
                           BiConsumer<Player, Long> withdraw,
                           Consumer<Player> quickDepositAll) {
        this.getPouch = getPouch;
        this.getTreasury = getTreasury;
        this.deposit = deposit;
        this.withdraw = withdraw;
        this.quickDepositAll = quickDepositAll;
    }

    private void openMainMenu(Player p) {
        new TreasuryMainMenu(p, getPouch.apply(p), getTreasury.apply(p)).open(p);
    }

    private void openDepositMenu(Player p) {
        new DepositMenu(
                () -> getPouch.apply(p),
                () -> getTreasury.apply(p),
                amount -> deposit.accept(p, amount)
        ).open(p);
    }

    private void openWithdrawMenu(Player p) {
        new ItemBuilder.WithdrawMenu(
                () -> getPouch.apply(p),
                () -> getTreasury.apply(p),
                amount -> withdraw.accept(p, amount)
        ).open(p);
    }

    private void openHelpMenu(Player p) {
        new HelpMenu().open(p);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        // MAIN
        TreasuryMainMenu.handleClick(
                e, p,
                () -> openDepositMenu(p),
                () -> {
                    if (quickDepositAll != null) quickDepositAll.accept(p);
                    openMainMenu(p);
                },
                () -> openWithdrawMenu(p),
                () -> openHelpMenu(p)
        );

        // DEPOSIT
        DepositMenu.handleClick(
                e, p,
                () -> openMainMenu(p),
                () -> getPouch.apply(p),
                amt -> {
                    deposit.accept(p, amt);
                    openDepositMenu(p);
                }
        );

        // WITHDRAW
        ItemBuilder.WithdrawMenu.handleClick(
                e, p,
                () -> openMainMenu(p),
                () -> getTreasury.apply(p),
                amt -> {
                    withdraw.accept(p, amt);
                    openWithdrawMenu(p);
                }
        );

        // HELP
        HelpMenu.handleClick(e, p, () -> openMainMenu(p));
    }
}
