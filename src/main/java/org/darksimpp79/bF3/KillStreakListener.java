package org.darksimpp79.bF3;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KillStreakListener implements Listener {

    private final BF3 plugin;
    private Connection connection;
    @Getter
    private final Map<UUID, Integer> playerKills = new HashMap<>();
    private FileConfiguration killConfig;
    private final Map<Integer, String> ranks = new HashMap<>();

    public KillStreakListener(BF3 plugin) {
        this.plugin = plugin;
        setupDatabase();
        loadKillConfig();
        loadRanks();
        startScoreboardUpdater();
    }

    /**
     * Ładuje plik killstreak.yml z folderu pluginu.
     */
    private void loadKillConfig() {
        File configFile = new File(plugin.getDataFolder(), "killstreak.yml");
        if (!configFile.exists()) {
            plugin.saveResource("killstreak.yml", false);
        }
        killConfig = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Wczytuje progi rang z pliku konfiguracyjnego.
     */
    private void loadRanks() {
        if (killConfig.isConfigurationSection("ranks")) {
            for (String key : killConfig.getConfigurationSection("ranks").getKeys(false)) {
                try {
                    int threshold = Integer.parseInt(key);
                    String rankName = killConfig.getString("ranks." + key);
                    ranks.put(threshold, rankName);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Niepoprawny próg rangi w killstreak.yml: " + key);
                }
            }
        } else {
            plugin.getLogger().warning("Brak sekcji 'ranks' w killstreak.yml!");
        }
    }

    /**
     * Ustawia połączenie z bazą SQLite i tworzy tabelę, jeśli nie istnieje.
     */
    private void setupDatabase() {
        try {
            // Plik bazy danych będzie w folderze pluginu
            File dbFile = new File(plugin.getDataFolder(), "killstreak.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS kills (uuid TEXT PRIMARY KEY, kills INT DEFAULT 0)");
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd połączenia z bazą danych: " + e.getMessage());
        }
    }

    /**
     * Listener na zdarzenie śmierci gracza.
     * Jeśli gracz został zabity przez innego gracza, zwiększamy liczbę zabójstw zabójcy.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        UUID uuid = killer.getUniqueId();
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO kills (uuid, kills) VALUES (?, 1) ON CONFLICT(uuid) DO UPDATE SET kills = kills + 1"
            );
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
            stmt.close();

            int kills = getKillCount(uuid);
            playerKills.put(uuid, kills);
            String rank = getRank(kills);
            killer.sendMessage(ChatColor.GREEN + "Masz " + kills + " zabójstw! Ranga: " + ChatColor.AQUA + rank);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pobiera aktualną liczbę zabójstw gracza z bazy danych.
     */
    private int getKillCount(UUID uuid) {
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT kills FROM kills WHERE uuid = ?");
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("kills");
                stmt.close();
                return count;
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Zwraca rangę gracza na podstawie liczby zabójstw.
     */
    private String getRank(int kills) {
        String currentRank = "Rekrut";
        for (Map.Entry<Integer, String> entry : ranks.entrySet()) {
            if (kills >= entry.getKey()) {
                currentRank = entry.getValue();
            }
        }
        return currentRank;
    }

    /**
     * Uruchamia zadanie aktualizujące scoreboard dla wszystkich graczy co sekundę.
     */
    private void startScoreboardUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    /**
     * Aktualizuje scoreboard (pasek po prawej) dla danego gracza.
     */
    private void updateScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("KillStreak", "dummy", ChatColor.YELLOW + "Statystyki");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int kills = playerKills.getOrDefault(player.getUniqueId(), 0);
        String rank = getRank(kills);

        Score nameScore = objective.getScore(ChatColor.GREEN + "Gracz: " + player.getName());
        nameScore.setScore(3);
        Score rankScore = objective.getScore(ChatColor.BLUE + "Ranga: " + rank);
        rankScore.setScore(2);
        Score killScore = objective.getScore(ChatColor.RED + "Zabójstwa: " + kills);
        killScore.setScore(1);

        player.setScoreboard(board);
    }
}
