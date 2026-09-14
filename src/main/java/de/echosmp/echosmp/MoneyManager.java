package de.echosmp.echosmp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/** Verwaltet das persistente Spielerguthaben. */
public final class MoneyManager {
    private final EchoSmpPlugin plugin;
    private final File file;
    private final FileConfiguration data;
    private final Map<UUID, Long> balances = new HashMap<>();

    public MoneyManager(EchoSmpPlugin plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "money.yml");
        data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(false)) {
            try {
                balances.put(UUID.fromString(key), Math.max(0L, data.getLong(key)));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Ungültige UUID in money.yml: " + key);
            }
        }
    }

    public long get(UUID uuid) {
        return balances.getOrDefault(uuid, 0L);
    }

    public void add(UUID uuid, long amount) {
        if (amount <= 0) {
            return;
        }
        balances.put(uuid, Math.addExact(get(uuid), amount));
        save();
    }

    public void remove(UUID uuid, long amount) {
        if (amount <= 0) return;
        balances.put(uuid, Math.max(0L, get(uuid) - amount));
        save();
    }

    public void save() {
        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {
            data.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Geld konnte nicht gespeichert werden.", exception);
        }
    }

    public static String format(long amount) {
        if (amount >= 1_000_000_000L) {
            return compact(amount, 1_000_000_000L, "b");
        }
        if (amount >= 1_000_000L) {
            return compact(amount, 1_000_000L, "m");
        }
        if (amount >= 1_000L) {
            return compact(amount, 1_000L, "k");
        }
        return Long.toString(amount);
    }

    private static String compact(long amount, long divisor, String suffix) {
        long whole = amount / divisor;
        long remainder = (amount % divisor) * 10 / divisor;
        return remainder == 0 ? whole + suffix : whole + "." + remainder + suffix;
    }
}
