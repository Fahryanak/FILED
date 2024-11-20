package com.hansproduction.filed;

import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;

public class FileDownloaderPlugin extends JavaPlugin {
    private FileDownloadManager downloadManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.downloadManager = new FileDownloadManager(this);

        getCommand("filed").setExecutor(new FileCommand(this));
        getLogger().info("FileDownloader plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("FileDownloader plugin disabled!");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        downloadManager.updateConfig(
            getConfig().getLong("default-download-speed", 1),
            Paths.get(getConfig().getString("default-download-path", "downloads"))
        );
    }

    public FileDownloadManager getDownloadManager() {
        return downloadManager;
    }
}