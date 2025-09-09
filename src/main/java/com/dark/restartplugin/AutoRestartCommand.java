package com.dark.restartplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import java.util.Arrays;

public class AutoRestartCommand implements CommandExecutor {
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
            sender.sendMessage(plugin.getAutoRestartManager().getStatus());
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
            default:
                sender.sendMessage(plugin.getConfigManager().getMessage("autorestart-usage"));
                return true;
        }
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getMessage("autorestart-usage"));
            return true;
        }

        try {
            int hours = Integer.parseInt(args[1]);
            if (hours <= 0) {
                sender.sendMessage(plugin.getConfigManager().getMessage("greater-than-zero"));
                return true;
            }

            plugin.getAutoRestartManager().setAutoRestart(hours, true);
            sender.sendMessage(plugin.getConfigManager().getMessage("autorestart-set")
                    .replace("%hours%", String.valueOf(hours)));
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("invalid-number"));
            return true;
        }
    }

    private boolean handleCancel(CommandSender sender) {
        plugin.getAutoRestartManager().setAutoRestart(0, false);
        sender.sendMessage(plugin.getConfigManager().getMessage("autorestart-cancelled"));
        return true;
    }
}