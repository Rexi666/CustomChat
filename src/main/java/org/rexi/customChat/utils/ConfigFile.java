package org.rexi.customChat.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
            plugin.addConfigINT("config_version", 2);

            // añadidos
            plugin.addConfig("database.type", "sqlite");
            plugin.addConfig("database.mysql.host", "localhost");
            plugin.addConfigINT("database.mysql.port", 3306);
            plugin.addConfig("database.mysql.database", "customchat");
            plugin.addConfig("database.mysql.username", "root");
            plugin.addConfig("database.mysql.password", "");

            plugin.addMessage("noconsole", "&cThis command can only be used by players.");
            plugin.addMessage("color_selected", "&aYou have selected the color &b{color} &afor your chat messages.");
            plugin.addMessage("no_permission_color", "&cYou do not have permission to use the color &b{color}");

            plugin.changeMessagetoFormat();

            plugin.reloadAllConfigs();
        }
    }
}
