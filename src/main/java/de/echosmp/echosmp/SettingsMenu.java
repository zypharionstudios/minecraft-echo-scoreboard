package de.echosmp.echosmp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Erstellt das 3x9-Menü für die persönlichen Sidebar-Schalter. */
public final class SettingsMenu {
    public static final String TITLE = "EchoSMP Einstellungen";
    private final PlayerSettingsManager settingsManager;

    public SettingsMenu(PlayerSettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, TITLE);
        fill(inventory);
        PlayerSettingsManager.Settings settings = settingsManager.get(player.getUniqueId());
        inventory.setItem(10, item(Material.CLOCK, "Uhr", settings.clock()));
        inventory.setItem(12, item(Material.GOLD_INGOT, "Geld", settings.money()));
        inventory.setItem(14, item(Material.ENDER_EYE, "MS / Ping", settings.ping()));
        inventory.setItem(16, item(Material.PLAYER_HEAD, "Spielerzahl", settings.players()));
        player.openInventory(inventory);
    }

    private void fill(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }
    }

    private ItemStack item(Material material, String name, boolean enabled) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName((enabled ? ChatColor.GREEN + "AN" : ChatColor.RED + "AUS")
                + ChatColor.WHITE + " | " + name);
        item.setItemMeta(meta);
        return item;
    }
}
