package com.dark.restartplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class ConfigManager {
    private final RestartPlugin plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public ConfigManager(RestartPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        // Загрузка основного конфига
        plugin.saveDefaultConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();

        // Загрузка messages.yml
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadConfigs() {
        plugin.reloadConfig();
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path) {
        return messagesConfig.getString("messages." + path, "§cСообщение не найдено: " + path);
    }

    public void saveRestartData(long time, String reason, boolean technical) {
        plugin.getConfig().set("restart.time", time);
        plugin.getConfig().set("restart.reason", reason);
        plugin.getConfig().set("restart.technical", technical);
        plugin.saveConfig();
    }

    public void clearRestartData() {
        plugin.getConfig().set("restart.time", 0);
        plugin.getConfig().set("restart.reason", null);
        plugin.getConfig().set("restart.technical", false);
        plugin.saveConfig();
    }

    public RestartData getRestartData() {
        long time = plugin.getConfig().getLong("restart.time", 0);
        String reason = plugin.getConfig().getString("restart.reason", "");
        boolean technical = plugin.getConfig().getBoolean("restart.technical", false);
        
        return new RestartData(time, reason, technical);
    }
}

class RestartData {
    private final long time;
    private final String reason;
    private final boolean technical;

    public RestartData(long time, String reason, boolean technical) {
        this.time = time;
        this.reason = reason;
        this.technical = technical;
    }

    public long getTime() { return time; }
    public String getReason() { return reason; }
    public boolean isTechnical() { return technical; }
    public boolean isValid() { return time > System.currentTimeMillis(); }
}