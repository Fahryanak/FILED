package com.hanz.filed;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

public class DownloadCommand implements CommandExecutor {
    private final DownloadPlugin plugin;

    public DownloadCommand(DownloadPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /download <link> <download_path> [file_name]");
            return true;
        }

        String link = args[0];
        String downloadPath = args[1];
        String fileName = args.length > 2 ? args[2] : null;

        // Asynchronous download to avoid blocking the server
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (link.contains("youtube.com") || link.contains("youtu.be")) {
                    downloadYouTube(link, downloadPath, fileName);
                } else {
                    downloadDirectFile(link, downloadPath, fileName);
                }
                sender.sendMessage(ChatColor.GREEN + "Download completed successfully!");
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "Error during download: " + e.getMessage());
                plugin.getLogger().log(Level.SEVERE, "Download error", e);
            }
        });

        sender.sendMessage(ChatColor.YELLOW + "Download started...");
        return true;
    }

    private void downloadDirectFile(String link, String downloadPath, String fileName) throws IOException {
        URL url = new URL(link);
        String outputFileName = fileName != null ? fileName : Paths.get(url.getPath()).getFileName().toString();
        File outputFile = new File(downloadPath, outputFileName);

        plugin.getLogger().info("Downloading file: " + outputFile.getAbsolutePath());
        try (var in = url.openStream();
             var out = new java.io.FileOutputStream(outputFile)) {
            byte[] buffer = new byte[plugin.getPluginConfig().getMaxDownloadSpeed() * 1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        plugin.getLogger().info("Download complete: " + outputFile.getAbsolutePath());
    }

    private void downloadYouTube(String link, String downloadPath, String fileName) throws IOException, InterruptedException {
        String outputTemplate = fileName != null ? downloadPath + File.separator + fileName : downloadPath + File.separator + "%(title)s.%(ext)s";
        ProcessBuilder processBuilder = new ProcessBuilder(
                "yt-dlp",
                "--output", outputTemplate,
                "--limit-rate", plugin.getPluginConfig().getMaxDownloadSpeed() + "K",
                link
        );

        processBuilder.inheritIO();
        Process process = processBuilder.start();
        process.waitFor();
        plugin.getLogger().info("YouTube download complete.");
    }
}