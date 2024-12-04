package com.hanz.filed;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class filedmain extends JavaPlugin {
    private Config config;

    @Override
    public void onEnable() {
        this.config = new Config(this); // Inisialisasi file config.yml
        getLogger().info("FILED Plugin has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("FILED Plugin has been disabled.");
    }

    public Config getConfigHandler() {
        return config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /filed <down|reload>");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "down":
                return new DownloadCommand(this).onCommand(sender, command, label, args);
            case "reload":
                config = new Config(this); // Reload config
                sender.sendMessage("Configuration reloaded successfully.");
                getLogger().info("Configuration reloaded.");
                return true;
            default:
                sender.sendMessage("Unknown subcommand. Use /filed <down|reload>.");
                return true;
        }
    }
}