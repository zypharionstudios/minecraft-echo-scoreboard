package de.echosmp.echosmp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.NumberFormat;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/** Erstellt und aktualisiert das individuelle Sidebar-Scoreboard. */
public final class ScoreboardManager {
    private static final String OBJECTIVE_NAME = "echo_smp";
    private static final String[] ENTRIES = {"§0", "§1", "§2"};

    private final EchoSmpPlugin plugin;
    private final ConfigManager config;
    private final PlaytimeManager playtimeManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();
    private BukkitTask updateTask;

    public ScoreboardManager(EchoSmpPlugin plugin, ConfigManager config, PlaytimeManager playtimeManager) {
        this.plugin = plugin;
        this.config = config;
        this.playtimeManager = playtimeManager;
    }

    public void start() {
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 0L, config.getUpdateIntervalTicks());
    }

    public void show(Player player) {
        Scoreboard scoreboard = createScoreboard();
        scoreboards.put(player.getUniqueId(), scoreboard);
        player.setScoreboard(scoreboard);
        update(player);
    }

    public void remove(Player player) {
        scoreboards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        scoreboards.clear();
    }

    private Scoreboard createScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(
                OBJECTIVE_NAME,
                Criteria.DUMMY,
                titleComponent(),
                RenderType.INTEGER
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int index = 0; index < ENTRIES.length; index++) {
            Team team = scoreboard.registerNewTeam("echo_line_" + index);
            team.addEntry(ENTRIES[index]);
            Score score = objective.getScore(ENTRIES[index]);
            score.setScore(ENTRIES.length - index);
            score.setNumberFormat(NumberFormat.blank());
        }
        return scoreboard;
    }

    private void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!scoreboards.containsKey(player.getUniqueId())) {
                show(player);
            } else {
                update(player);
            }
        }
    }

    private void update(Player player) {
        Scoreboard scoreboard = scoreboards.get(player.getUniqueId());
        if (scoreboard == null) {
            return;
        }
        setPrefix(scoreboard, 0, Component.text(player.getName()));
        setPrefix(scoreboard, 1, Component.text(config.getPlayerEmoji() + " "
                + Bukkit.getOnlinePlayers().size() + "/" + config.getMaxPlayers()));
        setPrefix(scoreboard, 2, Component.text(config.getClockEmoji() + " " + playtimeManager.format(player)));
    }

    private void setPrefix(Scoreboard scoreboard, int index, Component prefix) {
        Team team = scoreboard.getTeam("echo_line_" + index);
        if (team != null) {
            team.prefix(prefix);
        }
        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective != null) {
            objective.getScore(ENTRIES[index]).setNumberFormat(NumberFormat.blank());
        }
    }

    private Component titleComponent() {
        String gradient = "<gradient:" + config.getGradientStart() + ":"
                + config.getGradientMid() + ":" + config.getGradientEnd() + ">"
                + config.getScoreboardTitle() + "</gradient>";
        return miniMessage.deserialize(gradient);
    }
}
