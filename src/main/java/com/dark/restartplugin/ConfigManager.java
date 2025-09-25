// ConfigManager.java
package com.dark.restartplugin;
import org.bukkit.entity.Player;
import java.io.File;

public class ConfigManager {
    private final RestartPlugin plugin;
    private LanguageManager languageManager;

    public ConfigManager(RestartPlugin plugin) {
        this.plugin = plugin;
        this.languageManager = new LanguageManager(plugin);
    }

    public void loadConfigs() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                plugin.saveResource("config.yml", false);
                plugin.getLogger().info("config.yml создан");
            }
            plugin.reloadConfig();
            languageManager.loadLanguages();
            plugin.getLogger().info("Конфигурации загружены");
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка загрузки конфигов: " + e.getMessage());
        }
    }

    public void reloadConfigs() {
        try {
            plugin.reloadConfig();
            languageManager.reloadLanguages();
            plugin.getLogger().info("Конфигурации успешно перезагружены");
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при перезагрузке конфигураций: " + e.getMessage());
        }
    }

    public String getMessage(Player player, String path) {
        return languageManager.getMessage(player, path);
    }

    public String getMessage(String path) {
        return languageManager.getMessage(null, path);
    }

    public void saveRestartData(long time, String reason, boolean technical) {
        try {
            plugin.getConfig().set("restart.time", time);
            plugin.getConfig().set("restart.reason", reason);
            plugin.getConfig().set("restart.technical", technical);
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при сохранении данных рестарта: " + e.getMessage());
        }
    }

    public void clearRestartData() {
        try {
            plugin.getConfig().set("restart.time", 0);
            plugin.getConfig().set("restart.reason", null);
            plugin.getConfig().set("restart.technical", false);
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при очистке данных рестарта: " + e.getMessage());
        }
    }

    public RestartData getRestartData() {
        try {
            long time = plugin.getConfig().getLong("restart.time", 0);
            String reason = plugin.getConfig().getString("restart.reason", "");
            boolean technical = plugin.getConfig().getBoolean("restart.technical", false);
            
            return new RestartData(time, reason, technical);
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при получении данных рестарта: " + e.getMessage());
            return new RestartData(0, "", false);
        }
    }
}