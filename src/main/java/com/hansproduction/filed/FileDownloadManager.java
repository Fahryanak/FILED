package com.hansproduction.filed;

import org.bukkit.Bukkit;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileDownloadManager {
    private final FileDownloaderPlugin plugin;
    private long defaultSpeedLimit;
    private Path defaultSavePath;

    public FileDownloadManager(FileDownloaderPlugin plugin) {
        this.plugin = plugin;
        this.defaultSpeedLimit = plugin.getConfig().getLong("default-download-speed", 1) * 1024 * 1024;
        this.defaultSavePath = Path.of(plugin.getConfig().getString("default-download-path", "downloads"));
    }

    public void updateConfig(long speedLimit, Path savePath) {
        this.defaultSpeedLimit = speedLimit * 1024 * 1024;
        this.defaultSavePath = savePath;
    }

    public void downloadFile(String fileURL, Path savePath, long speedLimit) {
        try {
            if (!Files.exists(savePath.getParent())) {
                Files.createDirectories(savePath.getParent());
            }

            HttpURLConnection connection = (HttpURLConnection) new URL(fileURL).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                plugin.getLogger().severe("Failed to connect to URL: " + fileURL);
                return;
            }

            long fileSize = connection.getContentLengthLong();
            if (fileSize <= 0) {
                plugin.getLogger().warning("File size could not be determined. Proceeding with unknown file size...");
            }

            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(savePath.toFile())) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesDownloaded = 0;
                long startTime = System.nanoTime();

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesDownloaded += bytesRead;

                    // Display progress
                    double progress = (fileSize > 0)
                            ? (totalBytesDownloaded * 100.0 / fileSize)
                            : -1;
                    String progressText = (progress >= 0)
                            ? String.format("%.2f%%", progress)
                            : "Unknown";

                    Bukkit.getScheduler().runTask(plugin, () -> plugin.getLogger().info(
                            String.format("Downloading: %s (Progress: %s, Downloaded: %.2f MB)",
                                    savePath.getFileName(),
                                    progressText,
                                    totalBytesDownloaded / (1024.0 * 1024.0))
                    ));

                    // Throttle speed
                    long elapsedTime = System.nanoTime() - startTime;
                    double downloadSpeed = (totalBytesDownloaded / 1024.0 / 1024.0) / (elapsedTime / 1e9);
                    if (downloadSpeed > speedLimit) {
                        Thread.sleep((long) ((downloadSpeed - speedLimit) * 1000));
                    }
                }

                plugin.getLogger().info("Download completed: " + savePath.getFileName());
            }
        } catch (IOException | InterruptedException e) {
            plugin.getLogger().severe("Error downloading file: " + e.getMessage());
        }
    }

    public Path getDefaultSavePath() {
        return defaultSavePath;
    }

    public long getDefaultSpeedLimit() {
        return defaultSpeedLimit;
    }
}