package de.echosmp.echosmp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

/** Öffnet das Auktionshaus mit /ah. */
public final class AuctionHouseCommand implements CommandExecutor {
    private final AuctionHouseMenu menu;
    private final AuctionHouseManager auctions;

    public AuctionHouseCommand(AuctionHouseMenu menu, AuctionHouseManager auctions) {
        this.menu = menu;
        this.auctions = auctions;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && args.length > 0 && args[0].equalsIgnoreCase("sell")) {
            sellFromHand(player, args);
        } else if (sender instanceof Player player && args.length > 0) {
            menu.setSearch(player.getUniqueId(), String.join(" ", args));
            menu.openMain(player);
        } else if (sender instanceof Player player) {
            menu.openMain(player);
        } else {
            sender.sendMessage("Dieser Befehl ist nur im Spiel verfügbar.");
        }
        return true;
    }

    private void sellFromHand(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage(ChatColor.YELLOW + "Nutze /ah sell <preis>, z. B. /ah sell 40k");
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "Halte das Item zum Verkaufen in der Hand.");
            return;
        }
        long price = parsePrice(args[1]);
        if (price < 1) {
            player.sendMessage(ChatColor.RED + "Ungültiger Preis. Beispiele: 40000, 40k, 2.5m");
            return;
        }
        ItemStack listed = item.clone();
        player.getInventory().setItemInMainHand(null);
        auctions.create(player, listed, price);
        player.sendMessage(ChatColor.GREEN + "Angebot für " + MoneyManager.format(price) + " eingestellt.");
    }

    private long parsePrice(String input) {
        String value = input.toLowerCase().replace(",", ".").trim();
        long multiplier = 1L;
        if (value.endsWith("k")) { multiplier = 1_000L; value = value.substring(0, value.length() - 1); }
        else if (value.endsWith("m")) { multiplier = 1_000_000L; value = value.substring(0, value.length() - 1); }
        else if (value.endsWith("b")) { multiplier = 1_000_000_000L; value = value.substring(0, value.length() - 1); }
        try { return Math.round(Double.parseDouble(value) * multiplier); }
        catch (NumberFormatException exception) { return 0L; }
    }
}
