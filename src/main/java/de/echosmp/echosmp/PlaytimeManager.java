package de.echosmp.echosmp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/** Lädt, berechnet und speichert die dauerhafte Spielzeit aller Spieler. */
public final class PlaytimeManager {
    private final EchoSmpPlugin plugin;
    private final File dataFile;
    private final FileConfiguration data;
    private final Map<UUID, Long> storedSeconds = new HashMap<>();
    private final Map<UUID, Long> sessionStartedAt = new HashMap<>();

    public PlaytimeManager(EchoSmpPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        loadData();
    }

    private void loadData() {
        for (String key : data.getKeys(false)) {
            try {
                storedSeconds.put(UUID.fromString(key), Math.max(0L, data.getLong(key)));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Ungültige UUID in playerdata.yml: " + key);
            }
        }
    }

    public void playerJoined(Player player) {
        UUID uuid = player.getUniqueId();
        storedSeconds.putIfAbsent(uuid, 0L);
        sessionStartedAt.put(uuid, System.currentTimeMillis());
    }

    public long getSeconds(Player player) {
        UUID uuid = player.getUniqueId();
        long total = storedSeconds.getOrDefault(uuid, 0L);
        Long startedAt = sessionStartedAt.get(uuid);
        if (startedAt != null) {
            total += Math.max(0L, (System.currentTimeMillis() - startedAt) / 1000L);
        }
        return total;
    }

    public String format(Player player) {
        long seconds = getSeconds(player);
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "m";
        }
        return (minutes / 60) + "h " + (minutes % 60) + "m";
    }

    public void playerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        storeCurrent(uuid);
        sessionStartedAt.remove(uuid);
        saveFile();
    }

    /** Schreibt alle aktiven Sessions als Sicherheits-Backup auf die Festplatte. */
    public void saveAll() {
        for (UUID uuid : sessionStartedAt.keySet().toArray(UUID[]::new)) {
            storeCurrent(uuid);
            sessionStartedAt.put(uuid, System.currentTimeMillis());
        }
        saveFile();
    }

    private void storeCurrent(UUID uuid) {
        long total = storedSeconds.getOrDefault(uuid, 0L);
        Long startedAt = sessionStartedAt.get(uuid);
        if (startedAt != null) {
            total += Math.max(0L, (System.currentTimeMillis() - startedAt) / 1000L);
        }
        storedSeconds.put(uuid, total);
        data.set(uuid.toString(), total);
    }

    private void saveFile() {
        for (Map.Entry<UUID, Long> entry : storedSeconds.entrySet()) {
            data.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            if (!dataFile.exists() && !dataFile.createNewFile()) {
                plugin.getLogger().warning("playerdata.yml konnte nicht erstellt werden.");
            }
            data.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Spielzeiten konnten nicht gespeichert werden.", exception);
        }
    }
}
