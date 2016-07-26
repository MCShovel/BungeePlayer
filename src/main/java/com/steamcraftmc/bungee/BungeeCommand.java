package com.steamcraftmc.bungeetime;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;


public class BungeeCommand extends Command {

    private final BungeeHostNameWhiteList plugin;

    public BungeeCommand(BungeeHostNameWhiteList plugin) {
        super("hostnamewhitelist", "hostnamewhitelist.admin", "hnw");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            cs.sendMessage(new TextComponent("Configuration reloaded"));
        }
    }
}
