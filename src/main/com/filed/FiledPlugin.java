package com.filed;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class FiledPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("FiledPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("FiledPlugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("filed")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /filed download <link> <main|plugins|custom> [customPath] [speed]");
                return true;
            }

            String action = args[0].toLowerCase();
            if (action.equals("download")) {
                String url = args[1];
                String directory = args[2].toLowerCase();
                String customPath = args.length >= 4 ? args[3] : null;
                double speed = args.length >= 5 ? Double.parseDouble(args[4]) : 5.0; // Default 5 MBps

                if (speed > 5.0) {
                    sender.sendMessage("Speed cannot exceed 5 MBps.");
                    return true;
                }

                downloadFile(url, directory, customPath, speed, sender);
                return true;
            }

            sender.sendMessage("Unknown command. Usage: /filed <download|reload|list>");
            return true;
        }
        return false;
    }

    private void downloadFile(String fileURL, String directory, String customPath, double speed, CommandSender sender) {
        new BukkitRunnable() {
            @Override
            public void run() {
                HttpURLConnection httpConn = null;
                try {
                    URL url = new URL(fileURL);
                    httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setRequestMethod("GET");
                    httpConn.connect();

                    if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        sender.sendMessage("Failed to download file. HTTP response code: " + httpConn.getResponseCode());
                        return;
                    }

                    // Get file name from URL or HTTP header
                    String fileName = new File(fileURL).getName();
                    if (fileName.isEmpty()) {
                        String contentDisposition = httpConn.getHeaderField("Content-Disposition");
                        if (contentDisposition != null && contentDisposition.contains("filename=")) {
                            fileName = contentDisposition.split("filename=")[1].replace("\"", "");
                        } else {
                            fileName = "downloaded_file";
                        }
                    }

                    // Determine save directory
                    File outputDir;
                    switch (directory) {
                        case "main":
                            outputDir = new File(".");
                            break;
                        case "plugins":
                            outputDir = getDataFolder().getParentFile();
                            break;
                        case "custom":
                            if (customPath == null || customPath.isEmpty()) {
                                sender.sendMessage("Custom path is required for 'custom' directory option.");
                                return;
                            }
                            outputDir = new File(customPath);
                            if (!outputDir.exists()) {
                                outputDir.mkdirs();
                            }
                            break;
                        default:
                            sender.sendMessage("Invalid directory option. Use <main|plugins|custom>.");
                            return;
                    }

                    // Save file
                    File outputFile = new File(outputDir, fileName);
                    try (InputStream in = new BufferedInputStream(httpConn.getInputStream());
                         FileOutputStream out = new FileOutputStream(outputFile)) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        long totalBytesRead = 0;
                        long startTime = System.currentTimeMillis();
                        double maxBytesPerSecond = speed * 1024 * 1024; // Convert speed to bytes per second

                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;

                            // Enforce speed limit
                            long elapsedTime = System.currentTimeMillis() - startTime;
                            double expectedTime = (totalBytesRead / maxBytesPerSecond) * 1000; // Expected time in ms
                            if (elapsedTime < expectedTime) {
                                Thread.sleep((long) (expectedTime - elapsedTime));
                            }
                        }
                    }

                    sender.sendMessage("File downloaded successfully to: " + outputFile.getAbsolutePath());
                } catch (IOException | InterruptedException e) {
                    sender.sendMessage("Error downloading file: " + e.getMessage());
                } finally {
                    if (httpConn != null) {
                        httpConn.disconnect();
                    }
                }
            }
        }.runTaskAsynchronously(this);
    }
}