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
import org.rexi.customChat.commands.ChatColorCommand;
import org.rexi.customChat.commands.CustomchatCommand;
import org.rexi.customChat.config.ChatFormat;
import org.rexi.customChat.database.DatabaseManager;
import org.rexi.customChat.listeners.ColorChatListener;
import org.rexi.customChat.listeners.LegacyChatListener;
import org.rexi.customChat.listeners.MenuListener;
import org.rexi.customChat.listeners.NewChatListener;
import org.rexi.customChat.utils.ConfigFile;
import org.rexi.customChat.utils.UpdateChecker;

import java.sql.SQLException;
import java.util.*;

public final class CustomChat extends JavaPlugin {
    private Map<String, ChatFormat> formats;
    private final Map<UUID, String> playerChatColor = new HashMap<>();

    public final Map<Integer, String> colorItems = new HashMap<>();
    public final Map<Integer, String> gradientItems = new HashMap<>();

    private ConfigFile messagesFile;
    private ConfigFile formatsFile;
    private ConfigFile chatcolorFile;
    private ConfigFile configFile;
    private UpdateChecker updateChecker;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messagesFile = new ConfigFile(this, "messages.yml");
        formatsFile = new ConfigFile(this, "formats.yml");
        chatcolorFile = new ConfigFile(this, "chatcolor.yml");
        configFile = new ConfigFile(this, "config.yml");

        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.connect();
        } catch (SQLException e) {
            getLogger().severe("Could not connect to database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(new ColorChatListener(this), this);

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

        setMenuItems();
        getCommand("chatcolor").setExecutor(new ChatColorCommand(this));
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

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
        if (databaseManager != null) {
            databaseManager.close();
        }

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
        chatcolorFile.reload();
        loadFormats();
    }

    public void setPlayerChatColor(Player player, String colorCode) {
        playerChatColor.put(player.getUniqueId(), colorCode);
        try {
            databaseManager.setPlayerColor(player.getUniqueId().toString(), colorCode);
        } catch (SQLException e) {
            getLogger().severe("Failed to save player chat color on the database: " + e.getMessage());
        }
    }

    public String getPlayerChatColor(Player player) {
        return playerChatColor.get(player.getUniqueId());
    }
    public void getPlayerChatColorFromDB(Player player) {
        try {
            String color = databaseManager.getPlayerColor(player.getUniqueId().toString());
            playerChatColor.put(player.getUniqueId(), color);
        } catch (SQLException e) {
            getLogger().severe("Failed to load player chat color on the database: " + e.getMessage());
        }
    }

    public String getChatColorString(String path) {
        return chatcolorFile.getConfig().getString(path);
    }
    public List<String> getChatColorList(String path) {
        return chatcolorFile.getConfig().getStringList(path);
    }
    public FileConfiguration getChatColorConfig() {
        return chatcolorFile.getConfig();
    }

    public void addConfig(String path, String key) {
        configFile.getConfig().set(path, key);
        configFile.save();
    }

    public void addConfigINT(String path, int key) {
        configFile.getConfig().set(path, key);
        configFile.save();
    }

    public void addMessage(String path, String message) {
        messagesFile.getConfig().set("messages." + path, message);
        messagesFile.save();
    }

    public void changeMessagetoFormat() {
        FileConfiguration config = formatsFile.getConfig();
        ConfigurationSection section = config.getConfigurationSection("formats");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String message = section.getString(key + ".message", "&f");
                // quitar el {message}
                message = message.replace("{message}", "");

                // guardar en format
                config.set("formats."+key + ".format", message);

                // borrar la clave antigua
                config.set("formats."+key + ".message", null);
            }
            // guardar el archivo
            formatsFile.save();
        }
    }

    public void setMenuItems() {

        // colores
        ConfigurationSection colorsSection = getChatColorConfig().getConfigurationSection("chatcolor.colors.colors");
        if (colorsSection == null) return;

        int custommodeldata_colors = 10001;
        for (String key : colorsSection.getKeys(false)) {

            colorItems.put(custommodeldata_colors, key);

            custommodeldata_colors++;
        }

        // gradientes
        ConfigurationSection gradientsSection = getChatColorConfig().getConfigurationSection("chatcolor.gradients.colors");
        if (gradientsSection == null) return;

        int custommodeldata_gradients = 10001;
        for (String key : gradientsSection.getKeys(false)) {

            gradientItems.put(custommodeldata_gradients, key);

            custommodeldata_gradients++;
        }
    }
}
