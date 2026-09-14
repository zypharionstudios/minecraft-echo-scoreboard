package de.echosmp.echosmp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Öffnet das automatische Verkaufsmenü. */
public final class SellCommand implements CommandExecutor {
    private final SellMenu menu;

    public SellCommand(SellMenu menu) {
        this.menu = menu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl ist nur im Spiel verfügbar.");
            return true;
        }
        menu.open(player);
        return true;
    }
}
