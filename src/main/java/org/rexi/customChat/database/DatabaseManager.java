package org.rexi.customChat.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;

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
        createTable();
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS player_chat_color (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "color TEXT" +
                ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
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
