package org.rexi.customChat.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Formatter;

public class HashUtils {

    public static byte[] createSha1(File file) throws Exception {
        return createSha1(new FileInputStream(file));
    }

    public static byte[] createSha1(InputStream fis) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        int n = 0;
        byte[] buffer = new byte[8192];
        while (n != -1) {
            n = fis.read(buffer);
            if (n > 0) {
                digest.update(buffer, 0, n);
            }
        }
        fis.close();
        return digest.digest();
    }

    public static String createSha1String(File file) throws Exception {
        byte[] b = createSha1(file);
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static String createSha1String(InputStream fis) throws Exception {
        byte[] b = createSha1(fis);
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static String createSha1(boolean rightHanded, int selectedSlot, int level, String title, Inventory inventory) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        dataOutput.writeBoolean(rightHanded);
        dataOutput.writeByte(selectedSlot);
        dataOutput.writeInt(level);
        if (title == null) {
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            dataOutput.write(title.getBytes(StandardCharsets.UTF_8));
        }
        dataOutput.writeInt(inventory.getSize());
        for (int i = 0; i < inventory.getSize(); i++) {
            ByteArrayDataOutput itemByte = ByteStreams.newDataOutput();
            writeItemStack(itemByte, 0, inventory.getItem(i), StandardCharsets.UTF_8);
            dataOutput.write(itemByte.toByteArray());
        }
        dataOutput.close();
        byte[] bytes = outputStream.toByteArray();
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        Formatter formatter = new Formatter();
        for (byte b : md.digest(bytes)) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    public static String createSha1(String title, Inventory inventory) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        if (title == null) {
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            dataOutput.write(title.getBytes(StandardCharsets.UTF_8));
        }
        dataOutput.writeInt(inventory.getSize());
        for (int i = 0; i < inventory.getSize(); i++) {
            ByteArrayDataOutput itemByte = ByteStreams.newDataOutput();
            writeItemStack(itemByte, 0, inventory.getItem(i), StandardCharsets.UTF_8);
            dataOutput.write(itemByte.toByteArray());
        }
        dataOutput.close();
        byte[] bytes = outputStream.toByteArray();
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        Formatter formatter = new Formatter();
        for (byte b : md.digest(bytes)) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    public static String createSha1(String title, ItemStack item) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        if (title == null) {
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            dataOutput.write(title.getBytes(StandardCharsets.UTF_8));
        }
        ByteArrayDataOutput itemByte = ByteStreams.newDataOutput();
        writeItemStack(itemByte, 0, item, StandardCharsets.UTF_8);
        dataOutput.write(itemByte.toByteArray());
        dataOutput.close();
        byte[] bytes = outputStream.toByteArray();
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        Formatter formatter = new Formatter();
        for (byte b : md.digest(bytes)) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    @SuppressWarnings("deprecation")
    public static void writeItemStack(ByteArrayDataOutput out, int defaultEncodingScheme, ItemStack itemStack, Charset charset) throws IOException {
        int encodingScheme = defaultEncodingScheme;
        ByteArrayDataOutput itemByte = ByteStreams.newDataOutput();

        switch (encodingScheme) {
            case 0:
                try {
                    // Serialización con Yaml
                    YamlConfiguration config = new YamlConfiguration();
                    config.set("i", itemStack);
                    writeString(itemByte, config.saveToString(), charset);
                    break;
                } catch (Throwable e) {
                    // Si falla, fallback al esquema 1
                    encodingScheme = 1;
                    itemByte = ByteStreams.newDataOutput();
                }

            case 1:
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    itemByte.writeBoolean(false);
                } else {
                    itemByte.writeBoolean(true);

                    // Nombre del material
                    writeString(itemByte, itemStack.getType().name(), charset);

                    // Cantidad
                    itemByte.writeInt(itemStack.getAmount());

                    // Durabilidad (si aplica)
                    boolean isDamageable = itemStack.getType().getMaxDurability() > 0;
                    if (isDamageable) {
                        itemByte.writeBoolean(true);
                        int damage = itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof Damageable
                                ? ((Damageable) itemStack.getItemMeta()).getDamage()
                                : itemStack.getDurability();
                        itemByte.writeInt(damage);
                    } else {
                        itemByte.writeBoolean(false);
                    }

                    // NBT serializado como string (aquí podrías implementar tu propia utilidad si no tienes ItemNBTUtils)
                    String nbt = "{}"; // De momento vacío, o usa una lib como Adventure-NBT
                    writeString(itemByte, nbt, charset);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown encodingScheme version " + encodingScheme);
        }

        // Escribir esquema y bytes resultantes
        out.writeByte(encodingScheme);
        out.write(itemByte.toByteArray());
    }

    public static void writeString(ByteArrayDataOutput out, String string, Charset charset) throws IOException {
        byte[] bytes = string.getBytes(charset);
        out.writeInt(bytes.length);
        out.write(bytes);
    }
}
