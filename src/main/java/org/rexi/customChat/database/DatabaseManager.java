package org.rexi.customChat.database;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        String type = plugin.getConfig().getString("database.type", "sqlite").toLowerCase();
        if (type.equals("mysql")) {
            String host = plugin.getConfig().getString("database.mysql.host");
            int port = plugin.getConfig().getInt("database.mysql.port");
            String db = plugin.getConfig().getString("database.mysql.database");
            String user = plugin.getConfig().getString("database.mysql.username");
            String pass = plugin.getConfig().getString("database.mysql.password");

            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false",
                    user,
                    pass
            );
        } else {
            File dbFile = new File(plugin.getDataFolder(), "data.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        }

        plugin.getLogger().info("Connected to " + type.toUpperCase() + " database!");
        createTables();
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Tabla para colores
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS player_chat_color (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "color TEXT" +
                    ")");

            // Tabla para menciones
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS mention (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "enabled BOOLEAN" +
                    ")");

            // Tabla para ignore
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ignored_players (" +
                    "player_uuid VARCHAR(36) NOT NULL," +
                    "ignored_uuid VARCHAR(36) NOT NULL," +
                    "PRIMARY KEY (player_uuid, ignored_uuid)" +
                    ")");
        }
    }

    public void setPlayerColor(String uuid, String color) throws SQLException {
        String sql = "REPLACE INTO player_chat_color (uuid, color) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setString(2, color);
            ps.executeUpdate();
        }
    }

    public String getPlayerColor(String uuid) throws SQLException {
        String sql = "SELECT color FROM player_chat_color WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("color");
            }
        }
        return "";
    }

    // ---- MÉTODOS MENTION ----
    public void setMentionEnabled(String uuid, boolean enabled) throws SQLException {
        String sql = "REPLACE INTO mention (uuid, enabled) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setBoolean(2, enabled);
            ps.executeUpdate();
        }
    }

    public boolean isMentionEnabled(String uuid) throws SQLException {
        String sql = "SELECT enabled FROM mention WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("enabled");
            }
        }
        boolean byDefault = plugin.getConfig().getBoolean("mentioning.enabled_default", true);
        boolean op = plugin.getConfig().getBoolean("mentioning.op_disabled_default", true);
        if (byDefault) {
            if (op && Bukkit.getPlayer(UUID.fromString(uuid)).isOp()) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public void addIgnoredPlayer(String playerUUID, String ignoredUUID) throws SQLException {
        String sql = "REPLACE INTO ignored_players (player_uuid, ignored_uuid) VALUES (?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID);
            ps.setString(2, ignoredUUID);
            ps.executeUpdate();
        }
    }

    public void removeIgnoredPlayer(String playerUUID, String ignoredUUID) throws SQLException {
        String sql = "DELETE FROM ignored_players WHERE player_uuid = ? AND ignored_uuid = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID);
            ps.setString(2, ignoredUUID);
            ps.executeUpdate();
        }
    }

    public Set<UUID> getIgnoredPlayers(String playerUUID) throws SQLException {
        Set<UUID> ignored = new HashSet<>();

        String sql = "SELECT ignored_uuid FROM ignored_players WHERE player_uuid = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ignored.add(UUID.fromString(rs.getString("ignored_uuid")));
            }
        }

        return ignored;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing database connection: " + e.getMessage());
        }
    }
}
