package com.dark.restartplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class RestartPlugin extends JavaPlugin {
    private RestartManager restartManager;
    private AutoRestartManager autoRestartManager;
    private RestartMenu restartMenu;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        try {
            // Сначала инициализируем ConfigManager
            this.configManager = new ConfigManager(this);
            
            // Загружаем конфигурации ДО создания других менеджеров
            configManager.loadConfigs();

            // Теперь инициализируем остальные менеджеры
            this.restartManager = new RestartManager(this);
            this.autoRestartManager = new AutoRestartManager(this);
            this.restartMenu = new RestartMenu(this);

            // Регистрация команд
            getCommand("drestart").setExecutor(new RestartCommand(this));
            getCommand("autorestart").setExecutor(new AutoRestartCommand(this));

            // Восстановление запланированных рестартов
            restartManager.loadScheduledRestart();
            autoRestartManager.loadAutoRestart();

            getLogger().info("§aDarkRestartPlugin успешно включен!");
            
        } catch (Exception e) {
            getLogger().severe("§cОшибка при включении плагина: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (restartManager != null) {
            restartManager.cancelRestart();
        }
        if (autoRestartManager != null) {
            autoRestartManager.cancelAutoRestart();
        }
        getLogger().info("§cDarkRestartPlugin выключен!");
    }

    // Геттеры
    public RestartManager getRestartManager() { return restartManager; }
    public AutoRestartManager getAutoRestartManager() { return autoRestartManager; }
    public RestartMenu getRestartMenu() { return restartMenu; }
    public ConfigManager getConfigManager() { return configManager; }
}