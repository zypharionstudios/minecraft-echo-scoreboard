package de.echosmp.echosmp;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

/** Sichert die Sidebar, wenn ein anderes Plugin ein Inventar öffnet. */
public final class ScoreboardInventoryListener implements Listener {
    private final EchoSmpPlugin plugin;
    private final ScoreboardManager scoreboardManager;

    public ScoreboardInventoryListener(EchoSmpPlugin plugin, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) {
            plugin.getServer().getScheduler().runTask(plugin, () -> scoreboardManager.ensureVisible(player));
        }
    }
}