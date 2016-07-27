package com.steamcraftmc.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;


public class BungeeCommand extends Command {

    private final BungeePlugin plugin;

    public BungeeCommand(BungeePlugin plugin) {
        super("bplayer", "bplayer.admin");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            cs.sendMessage(new TextComponent(ChatColor.GOLD + "Configuration reloaded!"));
        }
        else {
        	cs.sendMessage(new TextComponent(ChatColor.RED + "Usage: /bplayer reload"));
        }
    }
}
