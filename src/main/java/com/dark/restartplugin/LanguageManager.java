// LanguageManager.java
package com.dark.restartplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LanguageManager {
    private final RestartPlugin plugin;
    private final Map<UUID, String> playerLanguages = new HashMap<>();
    private FileConfiguration enConfig;
    private FileConfiguration ruConfig;

    public LanguageManager(RestartPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadLanguages() {
        try {
            File ruFile = new File(plugin.getDataFolder(), "messages.yml");
            if (!ruFile.exists()) {
                try (InputStream in = plugin.getResource("messages.yml")) {
                    if (in != null) {
                        java.nio.file.Files.copy(in, ruFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        plugin.getLogger().info("messages.yml создан из ресурсов");
                    } else {
                        plugin.getLogger().warning("Не удалось найти messages.yml в ресурсах");
                        ruFile.createNewFile();
                    }
                }
            }
            ruConfig = YamlConfiguration.loadConfiguration(ruFile);

            File enFile = new File(plugin.getDataFolder(), "messages_en.yml");
            if (!enFile.exists()) {
                try (InputStream in = plugin.getResource("messages_en.yml")) {
                    if (in != null) {
                        java.nio.file.Files.copy(in, enFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        plugin.getLogger().info("messages_en.yml создан из ресурсов");
                    } else {
                        plugin.getLogger().warning("Не удалось найти messages_en.yml в ресурсах");
                        enFile.createNewFile();
                    }
                }
            }
            enConfig = YamlConfiguration.loadConfiguration(enFile);

            plugin.getLogger().info("Языковые файлы загружены");
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при загрузке языковых файлов: " + e.getMessage());
        }
    }

    public String getMessage(Player player, String path) {
        return getMessage(player, path, "§cСообщение не найдено: " + path);
    }

    public String getMessage(Player player, String path, String defaultValue) {
        String lang = "ru"; 
        if (player != null) {
            lang = playerLanguages.getOrDefault(player.getUniqueId(), "ru");
        }
        
        FileConfiguration config = lang.equals("en") ? enConfig : ruConfig;
        if (config == null) {
            return defaultValue;
        }
        
        String message = config.getString("messages." + path);
        return message != null ? message : defaultValue;
    }

    public void setPlayerLanguage(Player player, String language) {
        if (language.equals("en") || language.equals("ru")) {
            playerLanguages.put(player.getUniqueId(), language);
        }
    }

    public String getPlayerLanguage(Player player) {
        return playerLanguages.getOrDefault(player.getUniqueId(), "ru");
    }

    public void reloadLanguages() {
        try {
            File ruFile = new File(plugin.getDataFolder(), "messages.yml");
            File enFile = new File(plugin.getDataFolder(), "messages_en.yml");
            
            ruConfig = YamlConfiguration.loadConfiguration(ruFile);
            enConfig = YamlConfiguration.loadConfiguration(enFile);
            
            plugin.getLogger().info("Языковые файлы перезагружены");
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при перезагрузке языковых файлов: " + e.getMessage());
        }
    }
}