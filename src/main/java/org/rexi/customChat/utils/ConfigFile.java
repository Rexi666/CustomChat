package org.rexi.customChat.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.rexi.customChat.CustomChat;

import java.io.File;
import java.io.IOException;

public class ConfigFile {
    private final CustomChat plugin;
    private final String name;
    private final File file;
    private FileConfiguration config;

    public ConfigFile(CustomChat plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.file = new File(plugin.getDataFolder(), name);

        if (!file.exists()) {
            plugin.saveResource(name, false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    // Config version check
    public void checkConfigVersion() {
        if (!config.contains("config_version") || config.getInt("config_version") < 2) {
            config.addDefault("config_version", 2);

            // añadidos
            config.addDefault("database.type", "sqlite");
            config.addDefault("database.mysql.host", "localhost");
            config.addDefault("database.mysql.port", 3306);
            config.addDefault("database.mysql.database", "customchat");
            config.addDefault("database.mysql.username", "root");
            config.addDefault("database.mysql.password", "");

            config.options().copyDefaults(true);
            plugin.saveConfig();
            plugin.reloadAllConfigs();
        }
    }
}
