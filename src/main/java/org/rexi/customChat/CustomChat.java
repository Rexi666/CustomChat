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
import org.rexi.customChat.commands.*;
import org.rexi.customChat.config.ChatFormat;
import org.rexi.customChat.database.DatabaseManager;
import org.rexi.customChat.listeners.*;
import org.rexi.customChat.utils.*;

import java.sql.SQLException;
import java.util.*;

public final class CustomChat extends JavaPlugin {
    private Map<String, ChatFormat> formats;
    private final Map<UUID, String> playerChatColor = new HashMap<>();

    public final Map<Integer, String> colorItems = new HashMap<>();
    public final Map<Integer, String> gradientItems = new HashMap<>();

    public Map<UUID, UUID> messageReplies = new HashMap<>();

    public boolean chatlockActive = false;

    public ConfigFile messagesFile;
    public ConfigFile formatsFile;
    public ConfigFile chatcolorFile;
    public ConfigFile configFile;
    private UpdateChecker updateChecker;
    private DatabaseManager databaseManager;
    private ChatManager chatManager;

    private static CustomChat instance;

    @Override
    public void onEnable() {
        instance = this;
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

        chatManager = new ChatManager(this, new WebhookManager(this));

        loadFormats();
        if (hasClass("io.papermc.paper.event.player.AsyncChatEvent")) {
            getServer().getPluginManager().registerEvents(new NewChatListener(chatManager), this);

            getLogger().info("Paper 1.19+ was detected, using Paper's chat event.");
        } else {
            getServer().getPluginManager().registerEvents(new LegacyChatListener(chatManager), this);

            getLogger().info("Unable to detect Paper 1.19+, using legacy chat event.");
        }

        updateChecker = new UpdateChecker(this, getDescription().getVersion());
        updateChecker.checkForUpdatesConsole();

        CustomchatCommand customchatCommand = new CustomchatCommand(this, updateChecker);
        getCommand("customchat").setExecutor(customchatCommand);
        getCommand("customchat").setTabCompleter(customchatCommand);

        MsgCommand msgCommand = new MsgCommand(this);
        getCommand("msg").setExecutor(msgCommand);
        getCommand("msg").setTabCompleter(msgCommand);
        getCommand("message").setExecutor(msgCommand);
        getCommand("message").setTabCompleter(msgCommand);
        getCommand("tell").setExecutor(msgCommand);
        getCommand("tell").setTabCompleter(msgCommand);

        ReplyCommand replyCommand = new ReplyCommand(this);
        getCommand("reply").setExecutor(replyCommand);
        getCommand("r").setExecutor(replyCommand);

        IgnoreCommand ignoreCommand = new IgnoreCommand(this);
        getCommand("ignore").setExecutor(ignoreCommand);
        getCommand("ignore").setTabCompleter(ignoreCommand);
        getCommand("block").setExecutor(ignoreCommand);
        getCommand("block").setTabCompleter(ignoreCommand);

        SpyCommand spyCommand = new SpyCommand(this);
        getCommand("spy").setExecutor(spyCommand);
        getCommand("spy").setTabCompleter(spyCommand);

        ChatlockCommand chatlockCommand = new ChatlockCommand(this);
        getCommand("chatlock").setExecutor(chatlockCommand);
        getCommand("lockchat").setExecutor(chatlockCommand);

        setMenuItems();
        getCommand("chatcolor").setExecutor(new ChatColorCommand(this));
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        getServer().getPluginManager().registerEvents(new InventoryManager(), this);

        getServer().getPluginManager().registerEvents(new JoinLeaveMessagesListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);

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

    private static final LegacyComponentSerializer LEGACY_HEX_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors() // Habilita el soporte de hex
            .useUnusualXRepeatedCharacterHexFormat() // Soporta &x&r&r&g&g&b&b
            .build();

    public Component deserialize(String input) {
        if (input.contains("&")) {
            return LEGACY_HEX_SERIALIZER.deserialize(input.replace("§", "&"));
        } else if (input.contains("<") && input.contains(">")) {
            try {
                return MiniMessage.miniMessage().deserialize(input);
            } catch (Exception e) {
                return Component.text(input);
            }
        }
        return LEGACY_HEX_SERIALIZER.deserialize(input.replace("§", "&"));
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
    public void cacheRemoveChatColor(Player player) {
        playerChatColor.remove(player.getUniqueId());
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
    public void addConfigBoolean(String path, boolean key) {
        configFile.getConfig().set(path, key);
        configFile.save();
    }

    public void addMessage(String path, String message) {
        messagesFile.getConfig().set("messages." + path, message);
        messagesFile.save();
    }

    public void addChatColorConfig(String path, String key) {
        chatcolorFile.getConfig().set(path, key);
        chatcolorFile.save();
    }
    public void addChatColorConfigBoolean(String path, Boolean key) {
        chatcolorFile.getConfig().set(path, key);
        chatcolorFile.save();
    }
    public void addChatColorConfigList(String path, List<String> key) {
        chatcolorFile.getConfig().set(path, key);
        chatcolorFile.save();
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

    public static CustomChat getInstance() {
        return instance;
    }

    Map<UUID, Boolean> playerMention = new HashMap<>();
    public boolean getMentionEnabled(Player player) {
        return playerMention.getOrDefault(player.getUniqueId(), false);
    }
    public void setMentionEnabled(Player player, boolean active) {
        playerMention.put(player.getUniqueId(), active);
        try {
            databaseManager.setMentionEnabled(player.getUniqueId().toString(), active);
        } catch (SQLException e) {
            getLogger().severe("Failed to save player mention status on the database: " + e.getMessage());
        }
    }
    public void cacheRemoveMentionEnabled(Player player) {
        playerMention.remove(player.getUniqueId());
    }
    public void getMentionEnabledFromDB(Player player) {
        try {
            boolean active = databaseManager.isMentionEnabled(player.getUniqueId().toString());
            playerMention.put(player.getUniqueId(), active);
        } catch (SQLException e) {
            getLogger().severe("Failed to load player mention status on the database: " + e.getMessage());
        }
    }

    Map<UUID, Set<UUID>> ignoredPlayers = new HashMap<>();
    public void getIgnoredPlayersFromDB(Player player) {
        try {
            ignoredPlayers.put(
                    player.getUniqueId(),
                    databaseManager.getIgnoredPlayers(player.getUniqueId().toString())
            );
        } catch (SQLException e) {
            getLogger().severe("Failed to load player ignored list on the database: " + e.getMessage());
        }
    }
    public void setIgnoredPlayers(UUID player, UUID ignored, boolean active) {
        if (active) {
            ignoredPlayers.computeIfAbsent(player, k -> new HashSet<>()).add(ignored);
            try {
                databaseManager.addIgnoredPlayer(player.toString(), ignored.toString());
            } catch (SQLException e) {
                getLogger().severe("Failed to save player ignored status on the database: " + e.getMessage());
            }
        } else {
            Set<UUID> ignoredSet = ignoredPlayers.get(player);
            if (ignoredSet != null) {
                ignoredSet.remove(ignored);
            }
            try {
                databaseManager.removeIgnoredPlayer(player.toString(), ignored.toString());
            } catch (SQLException e) {
                getLogger().severe("Failed to save player ignored status on the database: " + e.getMessage());
            }
        }
    }
    public boolean checkIgnoredPlayers(UUID player, UUID ignored) {
        return ignoredPlayers.getOrDefault(player, Collections.emptySet()).contains(ignored);
    }
    public void cacheRemoveIgnoredPlayers(Player player) {
        ignoredPlayers.remove(player.getUniqueId());
    }

    public Map<UUID, List<UUID>> spyPlayers = new HashMap<>(); // Target - List<UUID> of players spying on them
    public List<UUID> spyGlobalPlayers = new ArrayList<>();

    public void removeSpy(UUID spyUUID) {
        Iterator<Map.Entry<UUID, List<UUID>>> iterator = spyPlayers.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, List<UUID>> entry = iterator.next();

            List<UUID> spies = entry.getValue();
            spies.remove(spyUUID);

            if (spies.isEmpty()) {
                iterator.remove();
            }
        }
    }
}
