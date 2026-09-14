package de.echosmp.echosmp;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/** Verarbeitet Klicks im 3x9-Einstellungsmenü. */
public final class SettingsListener implements Listener {
    private final PlayerSettingsManager settingsManager;
    private final SettingsMenu menu;
    private final ScoreboardManager scoreboardManager;

    public SettingsListener(PlayerSettingsManager settingsManager, SettingsMenu menu,
                            ScoreboardManager scoreboardManager) {
        this.settingsManager = settingsManager;
        this.menu = menu;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!SettingsMenu.TITLE.equals(event.getView().getTitle())) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getRawSlot() >= 27) {
            return;
        }
        PlayerSettingsManager.Setting setting = switch (event.getRawSlot()) {
            case 10 -> PlayerSettingsManager.Setting.CLOCK;
            case 12 -> PlayerSettingsManager.Setting.MONEY;
            case 14 -> PlayerSettingsManager.Setting.PING;
            case 16 -> PlayerSettingsManager.Setting.PLAYERS;
            default -> null;
        };
        if (setting != null) {
            settingsManager.toggle(player.getUniqueId(), setting);
            scoreboardManager.refresh(player);
            menu.open(player);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (SettingsMenu.TITLE.equals(event.getView().getTitle())) {
            event.setCancelled(true);
        }
    }
}
