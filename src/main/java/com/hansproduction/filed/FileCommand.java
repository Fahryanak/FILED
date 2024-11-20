package com.hansproduction.filed;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.nio.file.Path;

public class FileCommand implements CommandExecutor {
    private final FileDownloaderPlugin plugin;

    public FileCommand(FileDownloaderPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /filed <down/reload> [params]");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPluginConfig();
            sender.sendMessage("Configuration reloaded!");
            return true;
        }

        if (args[0].equalsIgnoreCase("down")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /filed down <link> [path] [speed]");
                return true;
            }

            String url = args[1];
            Path savePath = args.length > 2
                    ? Path.of(args[2])
                    : plugin.getDownloadManager().getDefaultSavePath().resolve("downloaded_file");
            long speedLimit = args.length > 3
                    ? Long.parseLong(args[3]) * 1024 * 1024
                    : plugin.getDownloadManager().getDefaultSpeedLimit();

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                    plugin.getDownloadManager().downloadFile(url, savePath, speedLimit)
            );

            sender.sendMessage("Downloading file...");
            return true;
        }

        sender.sendMessage("Unknown subcommand. Use /filed <down/reload>");
        return true;
    }
}