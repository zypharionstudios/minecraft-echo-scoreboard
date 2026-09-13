package de.echosmp.echosmp;

import org.bukkit.configuration.file.FileConfiguration;

/** Kapselt alle Standardwerte und Einstellungen des Plugins. */
public final class ConfigManager {
    private final EchoSmpPlugin plugin;
    private final FileConfiguration config;

    public ConfigManager(EchoSmpPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        plugin.saveConfig();
        this.config = plugin.getConfig();
    }

    public long getUpdateIntervalTicks() {
        return Math.max(1L, config.getLong("update-interval-ticks", 20L));
    }

    public int getMaxPlayers() {
        int configured = config.getInt("max-players", -1);
        return configured > 0 ? configured : plugin.getServer().getMaxPlayers();
    }

    public String getGradientStart() {
        return config.getString("gradient-start", "#FF8A8A");
    }

    public String getGradientMid() {
        return config.getString("gradient-mid", "#E63946");
    }

    public String getGradientEnd() {
        return config.getString("gradient-end", "#FFA500");
    }

    public String getPlayerEmoji() {
        return config.getString("emoji-player", "👤");
    }

    public String getClockEmoji() {
        return config.getString("emoji-clock", "⏰");
    }

    public String getScoreboardTitle() {
        return config.getString("scoreboard-title", "echo smp");
    }
}
