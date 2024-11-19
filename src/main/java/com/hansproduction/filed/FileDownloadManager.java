package com.hansproduction.filed;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class FileDownloadManager {

    private final FileDownloaderPlugin plugin;
    private final String defaultDownloadPath;
    private final double defaultSpeedLimit;

    public FileDownloadManager(FileDownloaderPlugin plugin, String defaultDownloadPath, double defaultSpeedLimit) {
        this.plugin = plugin;
        this.defaultDownloadPath = defaultDownloadPath;
        this.defaultSpeedLimit = defaultSpeedLimit;
    }

    public void downloadFile(String link, String outputPath, Double speedLimit, CommandSender sender) {
        if (outputPath == null || outputPath.isEmpty()) {
            outputPath = defaultDownloadPath + link.substring(link.lastIndexOf('/') + 1);
        }
        if (speedLimit == null) {
            speedLimit = defaultSpeedLimit;
        }

        final String finalOutputPath = outputPath;
        final double finalSpeedLimit = speedLimit;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (BufferedInputStream in = new BufferedInputStream(new URL(link).openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(finalOutputPath)) {

                HttpURLConnection connection = (HttpURLConnection) new URL(link).openConnection();
                int fileSize = connection.getContentLength();
                String fileName = link.substring(link.lastIndexOf('/') + 1);

                if (fileSize <= 0) {
                    sender.sendMessage("§cFile size could not be determined. Download aborted.");
                    return;
                }

                byte[] buffer = new byte[1024];
                int bytesRead;
                double speedLimitBytes = finalSpeedLimit * 1024 * 1024; // Convert speed limit to bytes per second
                long startTime = System.currentTimeMillis();

                int downloaded = 0;
                DecimalFormat df = new DecimalFormat("#.##");

                sender.sendMessage("§aStarting download: " + fileName);
                while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    downloaded += bytesRead;

                    long elapsedTime = System.currentTimeMillis() - startTime;
                    double currentSpeed = (downloaded / (elapsedTime / 1000.0));

                    // Sleep if download speed exceeds limit
                    if (currentSpeed > speedLimitBytes) {
                        Thread.sleep((long) ((currentSpeed - speedLimitBytes) / speedLimitBytes * 1000));
                    }

                    // Calculate progress and send message
                    int percentage = (int) ((downloaded / (double) fileSize) * 100);
                    String downloadedMB = df.format(downloaded / (1024.0 * 1024.0));
                    String totalMB = df.format(fileSize / (1024.0 * 1024.0));

                    sender.sendMessage("§eFileDownloader: " + fileName +
                            " §7[" + percentage + "%] " +
                            downloadedMB + "MB / " + totalMB + "MB");
                }

                sender.sendMessage("§aDownload Complete: " + finalOutputPath);

            } catch (Exception e) {
                plugin.getLogger().severe("Error downloading file: " + e.getMessage());
                sender.sendMessage("§cError downloading file. Check the console for details.");
            }
        });
    }
}
