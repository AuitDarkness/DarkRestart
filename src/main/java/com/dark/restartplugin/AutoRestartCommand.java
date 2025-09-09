package com.dark.restartplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoRestartCommand implements TabExecutor {
    private final RestartPlugin plugin;

    public AutoRestartCommand(RestartPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("darkrestart.autorestart")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set":
                return handleSet(sender, args);
            case "cancel":
                return handleCancel(sender);
            case "status":
                sender.sendMessage(plugin.getAutoRestartManager().getStatus());
                return true;
            case "help":
                sendHelp(sender);
                return true;
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /autorestart set <часы>");
            sender.sendMessage("§7Пример: §e/autorestart set 6");
            sender.sendMessage("§7Доступные интервалы: §e1, 2, 4, 6, 8, 12, 24");
            return true;
        }

        try {
            int hours = Integer.parseInt(args[1]);
            
            if (hours <= 0) {
                sender.sendMessage(plugin.getConfigManager().getMessage("greater-than-zero"));
                return true;
            }

            // Проверка допустимых интервалов
            int[] validIntervals = {1, 2, 4, 6, 8, 12, 24};
            boolean isValid = false;
            for (int interval : validIntervals) {
                if (hours == interval) {
                    isValid = true;
                    break;
                }
            }

            if (!isValid) {
                sender.sendMessage("§cНеверный интервал! Допустимые значения: 1, 2, 4, 6, 8, 12, 24");
                return true;
            }

            plugin.getAutoRestartManager().setAutoRestart(hours, true);
            
            String message = plugin.getConfigManager().getMessage("autorestart-set")
                    .replace("%hours%", String.valueOf(hours));
            sender.sendMessage(message);
            
            // Дополнительная информация
            sender.sendMessage("§7Предупреждения будут отправляться за: §e5 мин, 1 мин, 30 сек, 10 сек, 5 сек");
            sender.sendMessage("§7Используйте §e/autorestart cancel §7для отмены");
            
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("invalid-number"));
            sender.sendMessage("§7Используйте целые числа: §e1, 2, 4, 6, 8, 12, 24");
            return true;
        }
    }

    private boolean handleCancel(CommandSender sender) {
        if (!plugin.getConfig().getBoolean("auto-restart.enabled", false)) {
            sender.sendMessage("§cАвторестарт не активен!");
            return true;
        }

        plugin.getAutoRestartManager().setAutoRestart(0, false);
        sender.sendMessage(plugin.getConfigManager().getMessage("autorestart-cancelled"));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l=== Помощь по AutoRestart ===");
        sender.sendMessage("§e/autorestart set <часы> §7- Установить авторестарт");
        sender.sendMessage("§e/autorestart cancel §7- Отменить авторестарт");
        sender.sendMessage("§e/autorestart status §7- Показать статус");
        sender.sendMessage("§e/autorestart help §7- Показать эту помощь");
        sender.sendMessage("");
        sender.sendMessage("§6Доступные интервалы:");
        sender.sendMessage("§7- §e1 час §7(каждый час)");
        sender.sendMessage("§7- §e2 часа §7(каждые 2 часа)");
        sender.sendMessage("§7- §e4 часа §7(каждые 4 часа)");
        sender.sendMessage("§7- §e6 часов §7(каждые 6 часов)");
        sender.sendMessage("§7- §e8 часов §7(каждые 8 часов)");
        sender.sendMessage("§7- §e12 часов §7(каждые 12 часов)");
        sender.sendMessage("§7- §e24 часа §7(каждый день)");
        sender.sendMessage("");
        sender.sendMessage("§6Предупреждения:");
        sender.sendMessage("§7Игроки получат уведомления за: §e5 мин, 1 мин, 30 сек, 10 сек, 5 сек");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("darkrestart.autorestart")) {
            return completions;
        }

        if (args.length == 1) {
            completions.addAll(Arrays.asList("set", "cancel", "status", "help"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            completions.addAll(Arrays.asList("1", "2", "4", "6", "8", "12", "24"));
        }

        // Фильтрация по вводу
        String input = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(input));

        return completions;
    }
}