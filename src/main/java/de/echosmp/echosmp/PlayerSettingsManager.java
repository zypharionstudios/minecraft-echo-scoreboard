package de.echosmp.echosmp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/** Speichert die persönlichen Sidebar-Einstellungen der Spieler. */
public final class PlayerSettingsManager {
    private final EchoSmpPlugin plugin;
    private final File file;
    private final FileConfiguration data;
    private final Map<UUID, Settings> settings = new HashMap<>();

    public PlayerSettingsManager(EchoSmpPlugin plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "settings.yml");
        data = YamlConfiguration.loadConfiguration(file);
    }

    public Settings get(UUID uuid) {
        return settings.computeIfAbsent(uuid, key -> new Settings(
                data.getBoolean(key + ".clock", true),
                data.getBoolean(key + ".money", true),
                data.getBoolean(key + ".ping", true),
                data.getBoolean(key + ".players", true)));
    }

    public void toggle(UUID uuid, Setting setting) {
        Settings current = get(uuid);
        switch (setting) {
            case CLOCK -> current.clock = !current.clock;
            case MONEY -> current.money = !current.money;
            case PING -> current.ping = !current.ping;
            case PLAYERS -> current.players = !current.players;
        }
        save();
    }

    public void save() {
        for (Map.Entry<UUID, Settings> entry : settings.entrySet()) {
            String path = entry.getKey().toString();
            Settings value = entry.getValue();
            data.set(path + ".clock", value.clock);
            data.set(path + ".money", value.money);
            data.set(path + ".ping", value.ping);
            data.set(path + ".players", value.players);
        }
        try {
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Settings konnten nicht gespeichert werden.", exception);
        }
    }

    public enum Setting {
        CLOCK, MONEY, PING, PLAYERS
    }

    public static final class Settings {
        private boolean clock;
        private boolean money;
        private boolean ping;
        private boolean players;

        private Settings(boolean clock, boolean money, boolean ping, boolean players) {
            this.clock = clock;
            this.money = money;
            this.ping = ping;
            this.players = players;
        }

        public boolean clock() {
            return clock;
        }

        public boolean money() {
            return money;
        }

        public boolean ping() {
            return ping;
        }

        public boolean players() {
            return players;
        }
    }
}
