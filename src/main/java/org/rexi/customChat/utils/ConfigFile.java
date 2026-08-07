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

        if (config.getInt("config_version") < 6) {
            plugin.addConfigINT("config_version", 6);

            plugin.addMessage("location", "&8[&7My Location: &b{location}&8]");
            plugin.addMessage("location_placeholder", "World: {world}, X: {x}, Y: {y}, Z: {z}");
            plugin.addMessage("location_hover", "&bClick to copy the location to your clipboard");
            plugin.addMessage("ping", "&8[&7My Ping: {ping_color}{ping}&7ms&8]");
            plugin.addMessage("ping_hover", "{ping_color}{ping}&7ms");

            plugin.reloadAllConfigs();
        }

        if (config.getInt("config_version") < 7) {
            plugin.addConfigINT("config_version", 7);

            plugin.addMessage("msg_usage", "&cUsage: /msg <player> <message>");
            plugin.addMessage("msg", "&7[&b{player} > {target}&7] &e{message}");
            plugin.addMessage("msg_offline", "&c{player} is currently offline.");
            plugin.addMessage("msg_self", "&cYou cannot message yourself.");
            plugin.addMessage("msg_ignoring", "&c{player} is ignoring you.");
            plugin.addMessage("reply_usage", "&cUsage: /reply <message>");
            plugin.addMessage("reply_offline", "&cYou have no one to reply to.");
            plugin.addMessage("ignore_usage", "&cUsage: /ignore <player>");
            plugin.addMessage("ignore_self", "&cYou cannot ignore yourself.");
            plugin.addMessage("ignore_offline", "&c{player} is currently offline.");
            plugin.addMessage("ignore_bypass", "&cYou cannot ignore {player}.");
            plugin.addMessage("ignore_added", "&aYou have ignored {player}.");
            plugin.addMessage("ignore_removed", "&aYou have unignored {player}.");
            plugin.addMessage("spy_self", "&cYou cannot spy on yourself.");
            plugin.addMessage("spy_offline", "&c{player} is currently offline.");
            plugin.addMessage("spy_enabled", "&aYou have enabled spy mode.");
            plugin.addMessage("spy_disabled", "&cYou have disabled spy mode.");
            plugin.addMessage("spy_player_enabled", "&aYou have enabled spy mode for {player}.");
            plugin.addMessage("spy_player_disabled", "&cYou have disabled spy mode for {player}.");
            plugin.addMessage("chatlock_enabled", "&aYou have locked the chat.");
            plugin.addMessage("chatlock_disabled", "&cYou have unlocked the chat.");
            plugin.addMessage("chatlock_active", "&cThe chat is currently locked. You cannot send messages.");
            plugin.addMessage("chatlock_start", "&cThe chat has been locked by {player}.");
            plugin.addMessage("chatlock_end", "&aThe chat has been unlocked by {player}.");
            plugin.addMessage("console", "Console");

            plugin.addConfigBoolean("join_leave_messages.enabled", true);
            plugin.addConfig("join_leave_messages.join_message", "&a+ &r%luckperms_prefix% &e{player}");
            plugin.addConfig("join_leave_messages.leave_message", "&c- &r%luckperms_prefix% &e{player}");

            plugin.addConfigBoolean("death_messages.enabled", true);
            plugin.addConfig("death_messages.placeholder_killer", "by {player}");
            plugin.addConfig("death_messages.default", "&c☠ &e{player} died.");
            plugin.addConfig("death_messages.fall", "&c☠ &e{player} &7fell from a high place.");
            plugin.addConfig("death_messages.lava", "&c☠ &e{player} &7tried to swim in lava.");
            plugin.addConfig("death_messages.fire", "&c☠ &e{player} &7went up in flames.");
            plugin.addConfig("death_messages.fire_tick", "&c☠ &e{player} &7burned to death.");
            plugin.addConfig("death_messages.drowning", "&c☠ &e{player} &7drowned.");
            plugin.addConfig("death_messages.suffocation", "&c☠ &e{player} &7suffocated in a wall.");
            plugin.addConfig("death_messages.void", "&c☠ &e{player} &7fell into the void.");
            plugin.addConfig("death_messages.explosion", "&c☠ &e{player} &7blew up.");
            plugin.addConfig("death_messages.entity_attack", "&c☠ &e{player} &7was slain {killer}.");
            plugin.addConfig("death_messages.projectile", "&c☠ &e{player} &7was shot {killer}.");
            plugin.addConfig("death_messages.magic", "&c☠ &e{player} &7was killed by magic.");
            plugin.addConfig("death_messages.lightning", "&c☠ &e{player} &7was struck by lightning.");
            plugin.addConfig("death_messages.freeze", "&c☠ &e{player} &7froze to death.");
            plugin.addConfig("death_messages.starvation", "&c☠ &e{player} &7starved to death.");

            plugin.reloadAllConfigs();
        }
    }
}
