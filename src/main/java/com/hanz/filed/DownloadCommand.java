package com.hanz.filed;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class DownloadCommand implements CommandExecutor {
    private final filedmain plugin;

    public DownloadCommand(filedmain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /filed down <link> <output path> [file name]");
            return false;
        }

        String link = args[1];
        String outputPath = args[2];
        String fileName = args.length > 3 ? args[3] : null;

        // Membaca konfigurasi
        int threads = plugin.getConfigHandler().getDownloadThreads();
        int maxSpeed = plugin.getConfigHandler().getMaxDownloadSpeed();

        try {
            plugin.getLogger().log(Level.INFO, "Starting download with " + threads + " threads and max speed " + maxSpeed + " KB/s...");
            // Tambahkan kode untuk memulai proses download
            sender.sendMessage("Download started for: " + link);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Download error", e);
            sender.sendMessage("Failed to download: " + link);
        }
        return true;
    }
}