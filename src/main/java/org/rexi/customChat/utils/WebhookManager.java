package org.rexi.customChat.utils;

import org.rexi.customChat.CustomChat;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class WebhookManager {

    private final CustomChat plugin;

    public WebhookManager(CustomChat plugin) {
        this.plugin = plugin;
    }

    int[] colorRGB = new int[]{8, 139, 168};

    public void send(String content, String playername, String playeruuid) {
        String webhookUrl = plugin.getConfig().getString("discord_hook.url");
        String username = plugin.getConfig().getString("discord_hook.username", "{player}").replace("{player}", playername);
        String avatarUrl = getPlayerAvatar(playeruuid);

        boolean embedEnabled = plugin.getConfig().getBoolean("discord_hook.embed.enabled", true);
        String title = plugin.getConfig().getString("discord_hook.embed.title", "New Message from **{player}**").replace("{player}", playername);
        setColorRGB(plugin.getConfig().getString("discord_hook.embed.color_rgb","8,139,168"));

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

    public String getPlayerAvatar(String uuid) {
        final String fallback = "https://i.pinimg.com/564x/54/f4/b5/54f4b55a59ff9ddf2a2655c7f35e4356.jpg";
        String avatarUrl = "https://minotar.net/helm/" + uuid + "/64.png";
        try {
            URL url = new URL(avatarUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD"); // solo comprobamos si existe, sin descargar la imagen
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);

            int response = connection.getResponseCode();
            if (response == 404) {
                return fallback;
            }
            return avatarUrl;

        } catch (Exception e) {
            return fallback;
        }
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
