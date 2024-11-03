package com.example.downloadplugin;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class DownloadPlugin extends JavaPlugin {

    private final File linkFile = new File(getDataFolder(), "link.txt");
    private File downloadDir;
    private final Set<String> downloadedLinks = new HashSet<>();

    @Override
    public void onEnable() {
        // Save default config if not exist
        saveDefaultConfig();

        // Load config
        FileConfiguration config = getConfig();
        String downloadLocation = config.getString("download-location", "plugin");
        String customPath = config.getString("custom-path", "");
        int checkInterval = config.getInt("check-interval", 60);
        int concurrentDownloads = config.getInt("concurrent-downloads", 4);

        // Determine download directory based on config
        switch (downloadLocation.toLowerCase()) {
            case "server":
                downloadDir = new File(getServer().getWorldContainer(), "downloads");
                break;
            case "custom":
                if (!customPath.isEmpty()) {
                    downloadDir = new File(customPath);
                } else {
                    getLogger().severe("Custom path is empty! Defaulting to plugin directory.");
                    downloadDir = new File(getDataFolder(), "download");
                }
                break;
            case "plugin":
            default:
                downloadDir = new File(getDataFolder(), "download");
                break;
        }

        // Buat direktori plugin jika belum ada
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Buat file link.txt jika belum ada
        if (!linkFile.exists()) {
            try {
                linkFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Could not create link.txt file!");
                e.printStackTrace();
            }
        }

        // Buat direktori download jika belum ada
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                checkForNewLinks();
            }
        }.runTaskTimer(this, 0L, 20L * checkInterval); // Check every interval seconds
    }

    private void checkForNewLinks() {
        try (BufferedReader reader = new BufferedReader(new FileReader(linkFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!downloadedLinks.contains(line)) {
                    if (line.endsWith("/")) {
                        downloadDirectory(line);
                    } else {
                        downloadFile(line);
                    }
                    downloadedLinks.add(line);
                }
            }
        } catch (IOException e) {
            getLogger().severe("Error reading link.txt file!");
            e.printStackTrace();
        }
    }

    private void downloadFile(String link) {
        getLogger().info("Starting download from: " + link);
        long startTime = System.currentTimeMillis();
        try {
            URL url = new URL(link);
            String fileName = Paths.get(url.getPath()).getFileName().toString();
            File outputFile = new File(downloadDir, fileName);

            if (outputFile.exists()) {
                getLogger().info("File already exists: " + outputFile.getAbsolutePath());
                return;
            }

            try (InputStream in = url.openStream()) {
                Files.copy(in, outputFile.toPath());
                long endTime = System.currentTimeMillis();
                getLogger().info("Downloaded file to: " + outputFile.getAbsolutePath() + " in " + (endTime - startTime) + " ms");
            }

        } catch (MalformedURLException e) {
            getLogger().severe("Invalid URL: " + link);
        } catch (IOException e) {
            getLogger().severe("Error downloading file from: " + link);
        } catch (Exception e) {
            getLogger().severe("Unexpected error: " + e.getMessage());
        }
    }

    private void downloadDirectory(String link) {
        getLogger().info("Downloading directory from: " + link);
        try {
            URL url = new URL(link);
            String dirName = Paths.get(url.getPath()).getFileName().toString();
            File outputDir = new File(downloadDir, dirName);

            if (outputDir.exists()) {
                getLogger().info("Directory already exists: " + outputDir.getAbsolutePath());
                return;
            }

            FileUtils.copyURLToFile(url, outputDir);
            getLogger().info("Downloaded directory to: " + outputDir.getAbsolutePath());

        } catch (MalformedURLException e) {
            getLogger().severe("Invalid URL: " + link);
        } catch (IOException e) {
            getLogger().severe("Error downloading directory from: " + link);
        } catch (Exception e) {
            getLogger().severe("Unexpected error: " + e.getMessage());
        }
    }
}
