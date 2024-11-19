package com.hansproduction.filed;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class FileDownloaderPlugin extends JavaPlugin {

    private FileDownloadManager fileDownloadManager;
    private String defaultDownloadPath;
    private double defaultSpeedLimit;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        fileDownloadManager = new FileDownloadManager(this, defaultDownloadPath, defaultSpeedLimit);
        getCommand("filed").setExecutor(new FileCommand(this, fileDownloadManager));
        getLogger().info("FileDownloader Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("FileDownloader Plugin Disabled!");
    }

    public void loadConfig() {
        reloadConfig();
        defaultDownloadPath = getConfig().getString("default-download-path", "downloads/");
        defaultSpeedLimit = getConfig().getDouble("default-speed-limit", 1.0);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("filed") && args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            loadConfig();
            sender.sendMessage("Configuration reloaded!");
            return true;
        }
        return false;
    }
}
