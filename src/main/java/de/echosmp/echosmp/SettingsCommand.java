package de.echosmp.echosmp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Öffnet das persönliche 3x9-Einstellungsmenü. */
public final class SettingsCommand implements CommandExecutor {
    private final SettingsMenu menu;

    public SettingsCommand(SettingsMenu menu) {
        this.menu = menu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl ist nur im Spiel verfügbar.");
            return true;
        }
        boolean directAlias = label.equalsIgnoreCase("scoreboardsettings") && args.length == 0;
        boolean scoreboardSettings = args.length == 1 && args[0].equalsIgnoreCase("settings");
        if (directAlias || scoreboardSettings) {
            menu.open(player);
            return true;
        }
        player.sendMessage("Nutze /scoreboard settings oder /scoreboardsettings");
        return true;
    }
}
