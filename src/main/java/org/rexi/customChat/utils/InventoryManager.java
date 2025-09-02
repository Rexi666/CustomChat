package org.rexi.customChat.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.rexi.customChat.CustomChat;

import java.util.*;

public class InventoryManager implements Listener {

    private static final Map<String, Inventory> sharedInventories = new HashMap<>();

    public static String registerInventory(Player player) {
        try {
            String title = CustomChat.getInstance().messagesFile.getConfig()
                    .getString("messages.inv_title", "&7Inventory of {player}")
                    .replace("{player}", player.getName());
            Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', title));

            // --- Fila 1: Armadura + offhand ---
            ItemStack[] armor = player.getEquipment().getArmorContents(); // orden: boots, leggings, chestplate, helmet
            // En el inventario del juego, visualmente es Helmet, Chestplate, Leggings, Boots.
            inv.setItem(0, armor[3]); // helmet
            inv.setItem(1, armor[2]); // chestplate
            inv.setItem(2, armor[1]); // leggings
            inv.setItem(3, armor[0]); // boots
            inv.setItem(8, player.getInventory().getItemInOffHand()); // mano secundaria

            // --- Fila 2: Cristales grises ---
            ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = filler.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§7");
                filler.setItemMeta(meta);
            }
            for (int i = 9; i < 18; i++) {
                inv.setItem(i, filler);
            }

            // --- Filas 3–6: Inventario del jugador ---
            // Bukkit: slots 0–8 = hotbar, 9–35 = inventario, 36–39 armor, 40 offhand
            // Queremos mostrar: primero inventario 9–35 (27 slots), luego hotbar 0–8 (9 slots)
            int targetSlot = 18;
            for (int i = 9; i < 36; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null) inv.setItem(targetSlot, item.clone());
                targetSlot++;
            }
            for (int i = 0; i < 9; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null) inv.setItem(targetSlot, item.clone());
                targetSlot++;
            }

            // --- Guardar en el mapa ---
            String sha1 = HashUtils.createSha1(ChatColor.translateAlternateColorCodes('&', title), inv);
            sharedInventories.put(sha1, inv);
            return sha1;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Inventory getInventory(String sha1) {
        return sharedInventories.get(sha1);
    }

    public static String registerEnderChest(Player player) {
        try {
            String title = CustomChat.getInstance().messagesFile.getConfig()
                    .getString("messages.ender_title", "&7Ender Chest of {player}")
                    .replace("{player}", player.getName());

            Inventory enderChestInv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', title));
            // Copiar contenido del EnderChest del jugador
            Inventory playerEC = player.getEnderChest();
            for (int i = 0; i < playerEC.getSize(); i++) {
                ItemStack item = playerEC.getItem(i);
                if (item != null) {
                    enderChestInv.setItem(i, item.clone());
                }
            }

            // Guardar en el mapa
            String sha1 = HashUtils.createSha1(ChatColor.translateAlternateColorCodes('&', title), enderChestInv);
            sharedInventories.put(sha1, enderChestInv);
            return sha1;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Map<UUID, Boolean> opened = new HashMap<>();
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Entity entity = event.getWhoClicked();

        if (!(entity instanceof Player player)) return;

        if (opened.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Entity entity = event.getPlayer();

        if (!(entity instanceof Player player)) return;

        opened.put(player.getUniqueId(), false);
    }
}
