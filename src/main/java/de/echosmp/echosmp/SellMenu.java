package de.echosmp.echosmp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** 3x9-Verkaufsmenü mit sicherer, nicht entnehmbarer Bestätigung. */
public final class SellMenu {
    public static final String TITLE = "EchoSMP Verkauf";
    public static final int CONFIRM_SLOT = 26;
    private final BlockPriceManager prices;

    public SellMenu(BlockPriceManager prices) {
        this.prices = prices;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, TITLE);
        ItemStack filler = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int slot = 25; slot < 26; slot++) {
            inventory.setItem(slot, filler);
        }
        inventory.setItem(CONFIRM_SLOT, item(Material.LIME_STAINED_GLASS_PANE,
                ChatColor.GREEN + "Bestätigen"));
        player.openInventory(inventory);
    }

    public long preview(Inventory inventory) {
        long total = 0L;
        for (int slot = 0; slot < 25; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType().isBlock()) {
                total += prices.get(item.getType()) * item.getAmount();
            }
        }
        return total;
    }

    public BlockPriceManager prices() {
        return prices;
    }

    private ItemStack item(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }
}
