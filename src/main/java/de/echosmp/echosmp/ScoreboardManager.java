package de.echosmp.echosmp;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/** Erstellt und aktualisiert das individuelle Sidebar-Scoreboard. */
public final class ScoreboardManager {
    private static final String OBJECTIVE_NAME = "echo_smp";
    private static final String[] ENTRIES = {"§0", "§1", "§2", "§3", "§4", "§5", "§6"};

    private final EchoSmpPlugin plugin;
    private final ConfigManager config;
    private final PlaytimeManager playtimeManager;
    private final PlayerSettingsManager settingsManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();
    private BukkitTask updateTask;

    public ScoreboardManager(EchoSmpPlugin plugin, ConfigManager config, PlaytimeManager playtimeManager,
                             PlayerSettingsManager settingsManager) {
        this.plugin = plugin;
        this.config = config;
        this.playtimeManager = playtimeManager;
        this.settingsManager = settingsManager;
    }

    public void start() {
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 0L, config.getUpdateIntervalTicks());
    }

    public void show(Player player) {
        Scoreboard scoreboard = createScoreboard(player);
        scoreboards.put(player.getUniqueId(), scoreboard);
        player.setScoreboard(scoreboard);
        update(player);
    }

    public void refresh(Player player) {
        if (scoreboards.containsKey(player.getUniqueId())) {
            show(player);
        }
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

    private Scoreboard createScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective(
                OBJECTIVE_NAME,
                Criteria.DUMMY,
                titleComponent(),
                RenderType.INTEGER
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<Component> lines = activeLines(player);
        for (int index = 0; index < lines.size(); index++) {
            Team team = scoreboard.registerNewTeam("echo_line_" + index);
            team.addEntry(ENTRIES[index]);
            Score score = objective.getScore(ENTRIES[index]);
            score.setScore(lines.size() - index);
            score.numberFormat(NumberFormat.blank());
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
        List<Component> lines = activeLines(player);
        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective == null || scoreboard.getEntries().size() != lines.size()) {
            refresh(player);
            return;
        }
        for (int index = 0; index < lines.size(); index++) {
            setPrefix(scoreboard, index, lines.get(index));
        }
    }

    private List<Component> activeLines(Player player) {
        PlayerSettingsManager.Settings settings = settingsManager.get(player.getUniqueId());
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text(player.getName()));
        if (settings.players()) {
            lines.add(Component.text(config.getPlayerEmoji() + " "
                    + Bukkit.getOnlinePlayers().size() + "/" + config.getMaxPlayers())
                    .color(net.kyori.adventure.text.format.NamedTextColor.BLUE));
        }
        if (settings.clock()) {
            lines.add(Component.text(config.getClockEmoji() + " " + playtimeManager.format(player))
                    .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW));
        }
        if (settings.money()) {
            lines.add(Component.text(config.getMoneyEmoji() + " Coming soon")
                    .color(net.kyori.adventure.text.format.NamedTextColor.GOLD));
        }
        if (settings.ping()) {
            lines.add(pingComponent(player));
        }
        lines.add(separatorComponent(player, settings));
        lines.add(Component.text(config.getDiscordText()).color(TextColor.color(0x4B0082)));
        return lines;
    }

    private Component separatorComponent(Player player, PlayerSettingsManager.Settings settings) {
        String players = config.getPlayerEmoji() + " "
                + Bukkit.getOnlinePlayers().size() + "/" + config.getMaxPlayers();
        String playtime = config.getClockEmoji() + " " + playtimeManager.format(player);
        String ping = config.getMsEmoji() + " " + Math.max(0, player.getPing()) + "ms";
        int width = Math.max(player.getName().length(), config.getDiscordText().length());
        if (settings.players()) width = Math.max(width, players.length());
        if (settings.clock()) width = Math.max(width, playtime.length());
        if (settings.money()) width = Math.max(width, (config.getMoneyEmoji() + " Coming soon").length());
        if (settings.ping()) width = Math.max(width, ping.length());
        return Component.text("-".repeat(Math.max(1, width)))
                .color(net.kyori.adventure.text.format.NamedTextColor.BLACK);
    }

    private Component pingComponent(Player player) {
        int ping = Math.max(0, player.getPing());
        TextColor color;
        if (ping <= 100) {
            color = TextColor.color(0x55FF55);
        } else if (ping < 215) {
            color = TextColor.color(0xFFA500);
        } else {
            color = TextColor.color(0xFF5555);
        }
        return Component.text(config.getMsEmoji() + " " + ping + "ms").color(color);
    }

    private void setPrefix(Scoreboard scoreboard, int index, Component prefix) {
        Team team = scoreboard.getTeam("echo_line_" + index);
        if (team != null) {
            team.prefix(prefix);
        }
        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective != null) {
            objective.getScore(ENTRIES[index]).numberFormat(NumberFormat.blank());
        }
    }

    private Component titleComponent() {
        String gradient = "<gradient:" + config.getGradientStart() + ":"
                + config.getGradientMid() + ":" + config.getGradientEnd() + ">"
                + config.getScoreboardTitle() + "</gradient>";
        return miniMessage.deserialize(gradient);
    }
}
