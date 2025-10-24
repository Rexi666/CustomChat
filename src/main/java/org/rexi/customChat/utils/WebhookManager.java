package org.rexi.customChat.utils;

import org.rexi.customChat.CustomChat;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class WebhookManager {

    private final CustomChat plugin;

    public WebhookManager(CustomChat plugin) {
        this.plugin = plugin;
    }

    int[] colorRGB = new int[]{8, 139, 168};

    public void send(String content, String playername) {
        String webhookUrl = plugin.getConfig().getString("discord_hook.url");
        String username = plugin.getConfig().getString("discord_hook.username", "{player}").replace("{player}", playername);
        String avatarUrl = getPlayerAvatar(playername);

        boolean embedEnabled = plugin.getConfig().getBoolean("discord_hook.embed.enabled", true);
        String title = plugin.getConfig().getString("discord_hook.embed.title", "New Message from **{player}**").replace("{player}", playername);
        setColorRGB(plugin.getConfig().getString("8,139,168"));

        String message = plugin.getConfig().getString("discord_hook.message", "**{player}**: {message}").replace("{player}", playername).replace("{message}", content);

        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            int color = (colorRGB[0] << 16) + (colorRGB[1] << 8) + colorRGB[2];

            String payload;
            if (embedEnabled) {
                payload = """
                {
                  "username": "%s",
                  "avatar_url": "%s",
                  "embeds": [{
                    "title": "%s",
                    "description": "%s",
                    "color": %d
                  }]
                }
                """.formatted(
                        escape(username),
                        escape(avatarUrl),
                        escape(title),
                        escape(message),
                        color
                );
            } else {
                payload = """
                {
                  "username": "%s",
                  "avatar_url": "%s",
                  "content": "%s"
                }
                """.formatted(
                        escape(username),
                        escape(avatarUrl),
                        escape(message)
                );
            }

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            connection.getResponseCode(); // fuerza la ejecución
        } catch (Exception e) {
            System.err.println(plugin.getMessage("webhook_error"));
            e.printStackTrace();
        }
    }

    private String escape(String s) {
        return s == null ? "" : s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t");
    }

    public static String getUuidFromName(String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return null;
            }

            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                String responseBody = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

                int idKeyIndex = responseBody.indexOf("\"id\"");
                if (idKeyIndex == -1) return null;

                int colonIndex = responseBody.indexOf(":", idKeyIndex);
                if (colonIndex == -1) return null;

                int quoteStart = responseBody.indexOf("\"", colonIndex);
                if (quoteStart == -1) return null;

                int quoteEnd = responseBody.indexOf("\"", quoteStart + 1);
                if (quoteEnd == -1) return null;

                return responseBody.substring(quoteStart + 1, quoteEnd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPlayerAvatar(String playerName) {
        String uuid = getUuidFromName(playerName);
        String avatar = (uuid != null)
                ? "https://minotar.net/helm/" + uuid + "/64.png"
                : "https://i.pinimg.com/564x/54/f4/b5/54f4b55a59ff9ddf2a2655c7f35e4356.jpg";
        return avatar;
    }

    public void setColorRGB(String color) {
        try {
            String[] parts = color.split(",");
            if (parts.length == 3) {
                colorRGB = new int[]{
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()),
                        Integer.parseInt(parts[2].trim())
                };
            }
        } catch (Exception ignored) {}
    }
}
