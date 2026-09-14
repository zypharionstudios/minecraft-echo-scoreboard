package de.echosmp.echosmp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Öffnet das Auktionshaus mit /ah. */
public final class AuctionHouseCommand implements CommandExecutor {
    private final AuctionHouseMenu menu;

    public AuctionHouseCommand(AuctionHouseMenu menu) { this.menu = menu; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            menu.openMain(player);
        } else {
            sender.sendMessage("Dieser Befehl ist nur im Spiel verfügbar.");
        }
        return true;
    }
}
