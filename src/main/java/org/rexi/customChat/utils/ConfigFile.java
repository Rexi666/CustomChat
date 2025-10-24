package org.rexi.customChat.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.rexi.customChat.CustomChat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            plugin.addMessage("color_removed", "&aYou have removed your current chat color.");
            plugin.addMessage("no_permission_color", "&cYou do not have permission to use the color &b{color}");

            plugin.changeMessagetoFormat();

            plugin.reloadAllConfigs();
        }

        if (config.getInt("config_version") < 3) {
            plugin.addConfigINT("config_version", 3);

            plugin.addMessage("inv", "&8[&7My Inventory&8]");
            plugin.addMessage("inv_hover", "&bClick to view player's inventory");
            plugin.addMessage("inv_title", "&7Inventory of {player}");
            plugin.addMessage("inv_not_available", "&cThat inventory is no longer available");
            plugin.addMessage("item", "&8[&b{item}&8]");
            plugin.addMessage("ender", "&8[&7My Ender Chest&8]");
            plugin.addMessage("ender_hover", "&bClick to view player's ender chest");
            plugin.addMessage("ender_title", "&7Ender Chest of {player}");
            plugin.addMessage("ender_not_available", "&cThat ender chest is no longer available");

            plugin.addChatColorConfigBoolean("back.enabled", true);
            plugin.addChatColorConfig("back.material", "ARROW");
            plugin.addChatColorConfig("back.name", "&7Back");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&eClick to go to the previous page");
            plugin.addChatColorConfigList("back.lore", lore);

            plugin.reloadAllConfigs();
        }

        if (config.getInt("config_version") < 4) {
            plugin.addConfigINT("config_version", 4);

            plugin.addMessage("mention_enabled", "&aYou enabled mentions");
            plugin.addMessage("mention_disabled", "&cYou disabled mentions");
            plugin.addMessage("mention_function_disabled", "&cPlayer mentioning is disabled");

            plugin.addConfigBoolean("mentioning.enabled", true);
            plugin.addConfigBoolean("mentioning.requires_@", true);
            plugin.addConfigBoolean("mentioning.toggle_enabled", true);
            plugin.addConfigBoolean("mentioning.enabled_default", true);
            plugin.addConfigBoolean("mentioning.op_disabled_default", true);
            plugin.addConfig("mentioning.sound", "BLOCK_NOTE_BLOCK_PLING");
            plugin.addConfig("mentioning.title.title", "&b{player} &ementioned you");
            plugin.addConfig("mentioning.title.subtitle", "");
            plugin.addConfigINT("mentioning.title.fadeIn", 10);
            plugin.addConfigINT("mentioning.title.stay", 40);
            plugin.addConfigINT("mentioning.title.fadeOut", 10);
            plugin.addConfig("mentioning.message", "&b{player} &ementioned you. You can toggle this notification with &b/customchat mentiontoggle");

            plugin.reloadAllConfigs();
        }

        if (config.getInt("config_version") < 5) {
            plugin.addConfigINT("config_version", 5);

            plugin.addConfigBoolean("discord_hook.enabled", false);
            plugin.addConfig("discord_hook.url", "https://discord.com/api/webhooks/xxxxxxxx/yyyyyyyyyyyy");
            plugin.addConfig("discord_hook.username", "{player}");
            plugin.addConfig("discord_hook.message", "**{player}**: {message}");
            plugin.addConfigBoolean("discord_hook.embed.enabled", true);
            plugin.addConfig("discord_hook.embed.color_rgb", "8,139,168");
            plugin.addConfig("discord_hook.embed.title", "New Message from **{player}**");

            plugin.reloadAllConfigs();
        }
    }
}
