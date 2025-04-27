package com.dark.restartplugin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AutoRestartManager {
    private final RestartPlugin plugin;
    private BukkitTask autoRestartTask;
    private long interval;
    private long nextRestart;
    private File autoRestartFile;
    private FileConfiguration autoRestartConfig;

    public AutoRestartManager(RestartPlugin plugin) {
        this.plugin = plugin;
        setupAutoRestartConfig();
        loadFromConfig();
    }

    private void setupAutoRestartConfig() {
        try {
            // Создаем папку плагина, если она не существует
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
                plugin.getLogger().info("Создана папка плагина: " + plugin.getDataFolder().getAbsolutePath());
            }

            autoRestartFile = new File(plugin.getDataFolder(), "autorestart.yml");
            plugin.getLogger().info("Путь к файлу авторестарта: " + autoRestartFile.getAbsolutePath());

            if (!autoRestartFile.exists()) {
                plugin.getLogger().info("Файл autorestart.yml не найден, создаем новый...");
                autoRestartFile.createNewFile();
                autoRestartConfig = new YamlConfiguration();
                autoRestartConfig.set("interval", 6);
                autoRestartConfig.set("next-restart", 0);
                saveAutoRestartConfig();
                plugin.getLogger().info("Файл autorestart.yml создан успешно");
            } else {
                plugin.getLogger().info("Файл autorestart.yml найден, загружаем...");
                autoRestartConfig = YamlConfiguration.loadConfiguration(autoRestartFile);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Ошибка при создании/загрузке файла autorestart.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveAutoRestartConfig() {
        try {
            autoRestartConfig.save(autoRestartFile);
            plugin.getLogger().info("Настройки авторестарта сохранены в файл");
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить настройки авторестарта: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFromConfig() {
        try {
            // Всегда используем 6 часов как интервал по умолчанию
            interval = TimeUnit.HOURS.toMillis(6);
            nextRestart = autoRestartConfig.getLong("next-restart", 0);

            plugin.getLogger().info("Загружены настройки авторестарта:");
            plugin.getLogger().info("Интервал: " + TimeUnit.MILLISECONDS.toHours(interval) + " часов");
            plugin.getLogger().info("Следующий рестарт: " + (nextRestart > 0 ? 
                "через " + formatTime(nextRestart - System.currentTimeMillis()) : "не запланирован"));

            // Если nextRestart не установлен или уже прошел, устанавливаем новый
            if (nextRestart <= System.currentTimeMillis()) {
                nextRestart = System.currentTimeMillis() + interval;
                autoRestartConfig.set("next-restart", nextRestart);
                saveAutoRestartConfig();
                plugin.getLogger().info("Установлено новое время рестарта: " + formatTime(interval));
            }

            // Запускаем авторестарт
            startAutoRestart();
            plugin.getLogger().info("Авторестарт запущен с интервалом " + TimeUnit.MILLISECONDS.toHours(interval) + " часов");
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при загрузке настроек авторестарта: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setInterval(long hours) {
        try {
            interval = TimeUnit.HOURS.toMillis(hours);
            nextRestart = System.currentTimeMillis() + interval;

            // Сохраняем в конфиг
            autoRestartConfig.set("interval", hours);
            autoRestartConfig.set("next-restart", nextRestart);
            saveAutoRestartConfig();

            plugin.getLogger().info("Установлен новый интервал авторестарта: " + hours + " часов");
            startAutoRestart();
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при установке интервала авторестарта: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cancelAutoRestart() {
        try {
            if (autoRestartTask != null) {
                autoRestartTask.cancel();
                autoRestartTask = null;
            }

            interval = 0;
            nextRestart = 0;

            // Очищаем конфиг
            autoRestartConfig.set("interval", 0);
            autoRestartConfig.set("next-restart", 0);
            saveAutoRestartConfig();

            plugin.getLogger().info("Авторестарт отменен");
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при отмене авторестарта: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startAutoRestart() {
        try {
            if (autoRestartTask != null) {
                autoRestartTask.cancel();
            }

            autoRestartTask = new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        if (System.currentTimeMillis() >= nextRestart) {
                            plugin.getLogger().info("Запуск авторестарта...");
                            
                            // Запускаем рестарт
                            plugin.getRestartManager().scheduleRestart(
                                TimeUnit.MINUTES.toMillis(5),
                                "Автоматический рестарт (каждые " + TimeUnit.MILLISECONDS.toHours(interval) + " часов)",
                                false
                            );

                            // Устанавливаем следующий рестарт
                            nextRestart = System.currentTimeMillis() + interval;
                            autoRestartConfig.set("next-restart", nextRestart);
                            saveAutoRestartConfig();
                            
                            plugin.getLogger().info("Следующий рестарт запланирован через " + formatTime(interval));
                        }
                    } catch (Exception e) {
                        plugin.getLogger().severe("Ошибка в задаче авторестарта: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
            
            plugin.getLogger().info("Задача авторестарта запущена");
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при запуске задачи авторестарта: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getStatus() {
        if (interval == 0) {
            return plugin.getMessage("autorestart-not-active");
        }

        long timeLeft = nextRestart - System.currentTimeMillis();
        String timeString = formatTime(timeLeft);
        String hours = String.valueOf(TimeUnit.MILLISECONDS.toHours(interval));

        return plugin.getMessage("autorestart-status")
                .replace("%status%", timeString + " | Каждые " + hours + " часов");
    }

    private String formatTime(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long hours = TimeUnit.MINUTES.toHours(minutes);

        if (hours > 0) {
            return String.format("%dч %dм", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dм %dс", minutes, seconds % 60);
        } else {
            return String.format("%dс", seconds);
        }
    }

    public boolean isAutoRestartEnabled() {
        return interval > 0;
    }
} 