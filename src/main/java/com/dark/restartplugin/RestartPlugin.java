// RestartPlugin.java
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
            this.configManager = new ConfigManager(this);
            this.restartManager = new RestartManager(this);
            this.autoRestartManager = new AutoRestartManager(this);
            this.restartMenu = new RestartMenu(this);

            configManager.loadConfigs();

            getCommand("drestart").setExecutor(new RestartCommand(this));
            getCommand("autorestart").setExecutor(new AutoRestartCommand(this));

            restartManager.loadScheduledRestart();
            autoRestartManager.loadAutoRestart();

            getLogger().info("DarkRestart успешно запущен!");
        } catch (Exception e) {
            getLogger().severe("Критическая ошибка при запуске: " + e.getMessage());
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

    public RestartManager getRestartManager() { return restartManager; }
    public AutoRestartManager getAutoRestartManager() { return autoRestartManager; }
    public RestartMenu getRestartMenu() { return restartMenu; }
    public ConfigManager getConfigManager() { return configManager; }
}