package com.hansproduction.filed;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FileCommand implements CommandExecutor {

    private final FileDownloaderPlugin plugin;
    private final FileDownloadManager fileDownloadManager;

    public FileCommand(FileDownloaderPlugin plugin, FileDownloadManager fileDownloadManager) {
        this.plugin = plugin;
        this.fileDownloadManager = fileDownloadManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("Usage: /filed down <link> [output_path] [speed_limit_MBps]");
            sender.sendMessage("       /filed reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("down")) {
            if (args.length < 2) {
                sender.sendMessage("§cPlease specify a link.");
                return true;
            }

            String link = args[1];
            String outputPath = args.length > 2 ? args[2] : null;
            Double speedLimit = args.length > 3 ? parseDouble(args[3]) : null;

            fileDownloadManager.downloadFile(link, outputPath, speedLimit, sender);
            sender.sendMessage("§aStarting file download...");
            return true;
        }

        return false;
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
