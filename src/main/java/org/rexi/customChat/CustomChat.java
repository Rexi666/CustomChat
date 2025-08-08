package org.rexi.customChat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.rexi.customChat.commands.CustomchatCommand;
import org.rexi.customChat.config.ChatFormat;
import org.rexi.customChat.listeners.LegacyChatListener;
import org.rexi.customChat.listeners.NewChatListener;
import org.rexi.customChat.utils.ConfigFile;
import org.rexi.customChat.utils.UpdateChecker;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class CustomChat extends JavaPlugin {
    private Map<String, ChatFormat> formats;

    private ConfigFile messagesFile;
    private ConfigFile formatsFile;
    private ConfigFile configFile;
    private UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messagesFile = new ConfigFile(this, "messages.yml");
        formatsFile = new ConfigFile(this, "formats.yml");
        loadFormats();
        if (hasClass("io.papermc.paper.event.player.AsyncChatEvent")) {
            getServer().getPluginManager().registerEvents(new NewChatListener(this), this);

            getLogger().info("Paper 1.19+ was detected, using Paper's chat event.");
        } else {
            getServer().getPluginManager().registerEvents(new LegacyChatListener(this), this);

            getLogger().info("Unable to detect Paper 1.19+, using legacy chat event.");
        }

        updateChecker = new UpdateChecker(this, getDescription().getVersion(), "https://raw.githubusercontent.com/Rexi666/CustomChat/main/latest-version.txt");
        updateChecker.checkForUpdates();

        getCommand("customchat").setExecutor(new CustomchatCommand(this, updateChecker));

        int pluginId = 26809; // Reemplaza con el ID real de tu plugin en bStats
        Metrics metrics = new Metrics(this, pluginId);

        configFile.checkConfigVersion();

        getServer().getConsoleSender().sendMessage(Component.text("CustomChat plugin has been enabled!").color(NamedTextColor.GREEN));
        getServer().getConsoleSender().sendMessage(Component.text("Thank you for using Rexi666 plugins :D").color(NamedTextColor.BLUE));
    }

    public void loadFormats() {
        formats = new LinkedHashMap<>();
        FileConfiguration config = formatsFile.getConfig();
        ConfigurationSection section = config.getConfigurationSection("formats");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ChatFormat format = new ChatFormat(key, section.getConfigurationSection(key), this);
                formats.put(key, format);
            }
        }
    }

    public Optional<ChatFormat> getFormatFor(Player player) {
        // Prioriza los formatos con permiso y que el jugador lo tenga
        for (ChatFormat format : formats.values()) {
            if (!format.getPermission().isEmpty() && player.hasPermission(format.getPermission())) {
                return Optional.of(format);
            }
        }

        // Si ninguno ha coincidido, usa el formato por defecto
        ChatFormat defaultFormat = formats.get("default");
        if (defaultFormat != null) {
            return Optional.of(defaultFormat);
        }

        return Optional.empty();
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(Component.text("CustomChat plugin has been disabled!").color(NamedTextColor.RED));
        getServer().getConsoleSender().sendMessage(Component.text("Thank you for using Rexi666 plugins :D").color(NamedTextColor.BLUE));
    }

    public Component deserialize(String input) {
        // Si contiene <...> asumimos que es MiniMessage
        if (input.contains("<") && input.contains(">")) {
            try {
                return MiniMessage.miniMessage().deserialize(input);
            } catch (Exception e) {
                // En caso de error, usa como texto plano
                return Component.text(input);
            }
        }

        // Si no, asumimos que es con códigos &
        return LegacyComponentSerializer.legacyAmpersand().deserialize(input);
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public Component getMessage(String path) {
        return deserialize(messagesFile.getConfig().getString("messages." + path, "&cMessage not found: " + path));
    }

    public Component getMessage(String path, String... placeholders) {
        String message = messagesFile.getConfig().getString("messages." + path, "&cMessage not found: " + path);
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }
        return deserialize(message);
    }

    public void reloadAllConfigs() {
        reloadConfig();
        messagesFile.reload();
        formatsFile.reload();
        loadFormats();
    }
}
