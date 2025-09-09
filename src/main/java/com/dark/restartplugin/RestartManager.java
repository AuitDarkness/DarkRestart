package com.dark.restartplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RestartManager {
    private final RestartPlugin plugin;
    private BukkitTask restartTask;
    private long restartTime;
    private String restartReason;
    private boolean isTechnical;

    public RestartManager(RestartPlugin plugin) {
        this.plugin = plugin;
    }

    public void scheduleRestart(long delayMillis, String reason, boolean technical) {
        scheduleRestart(delayMillis, reason, technical, 
            plugin.getConfig().getIntegerList("restart.warnings"));
    }

    public void scheduleRestart(long delayMillis, String reason, boolean technical, List<Integer> warnings) {
        cancelRestart();

        this.restartTime = System.currentTimeMillis() + delayMillis;
        this.restartReason = reason;
        this.isTechnical = technical;

        plugin.getConfigManager().saveRestartData(restartTime, restartReason, isTechnical);
        startCountdown(warnings);
        
        plugin.getLogger().info("Рестарт запланирован на " + formatTime(delayMillis) + ". Причина: " + reason);
    }

    public void cancelRestart() {
        if (restartTask != null) {
            restartTask.cancel();
            restartTask = null;
        }

        restartTime = 0;
        restartReason = null;
        isTechnical = false;
        plugin.getConfigManager().clearRestartData();
    }

    public void loadScheduledRestart() {
        RestartData data = plugin.getConfigManager().getRestartData();
        if (data.isValid()) {
            long timeLeft = data.getTime() - System.currentTimeMillis();
            if (timeLeft > 0) {
                scheduleRestart(timeLeft, data.getReason(), data.isTechnical());
            }
        }
    }

    private void startCountdown(List<Integer> warnings) {
        restartTask = new BukkitRunnable() {
            @Override
            public void run() {
                long timeLeft = restartTime - System.currentTimeMillis();
                if (timeLeft <= 0) {
                    performRestart();
                    cancel();
                    return;
                }

                sendNotifications(timeLeft, warnings);
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void sendNotifications(long timeLeft, List<Integer> warnings) {
        long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(timeLeft);

        if (warnings.contains((int) secondsLeft)) {
            String message = plugin.getConfigManager().getMessage("server-restarting")
                    .replace("%time%", formatTime(timeLeft))
                    .replace("%reason%", restartReason);

            Bukkit.broadcastMessage(message);

            if (plugin.getConfig().getBoolean("notifications.sounds", true)) {
                String soundName = plugin.getConfig().getString("restart.countdown-sound", "BLOCK_NOTE_BLOCK_PLING");
                try {
                    Sound sound = Sound.valueOf(soundName.toUpperCase());
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неверное имя звука: " + soundName);
                }
            }
        }

        if (plugin.getConfig().getBoolean("notifications.actionbar", true)) {
            String actionBarMessage = plugin.getConfigManager().getMessage("countdown-actionbar")
                    .replace("%time%", formatTime(timeLeft))
                    .replace("%reason%", restartReason);

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendActionBar(actionBarMessage);
            }
        }
    }

    private void performRestart() {
        Bukkit.broadcastMessage("§c§lСервер перезапускается!");
        Bukkit.broadcastMessage("§cПричина: §e" + restartReason);

        Bukkit.getWorlds().forEach(world -> world.save());

        String kickMessage = ChatColor.translateAlternateColorCodes('&',
            "&cСервер перезагружается!\n&6Причина: &e" + restartReason + "\n&aВозвращайтесь через минуту!");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer(kickMessage);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.shutdown();
            }
        }.runTaskLater(plugin, 40L);
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

    public String getStatus() {
        if (restartTime == 0) {
            return plugin.getConfigManager().getMessage("restart-not-active");
        }

        long timeLeft = restartTime - System.currentTimeMillis();
        String timeString = formatTime(timeLeft);

        return plugin.getConfigManager().getMessage("restart-status")
                .replace("%status%", timeString + " | " + restartReason);
    }

    public boolean isRestartScheduled() { return restartTime > 0; }
    public boolean isTechnical() { return isTechnical; }
    public long getRestartTime() { return restartTime; }
    public String getRestartReason() { return restartReason; }
}