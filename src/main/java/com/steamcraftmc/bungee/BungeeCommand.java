package com.steamcraftmc.bungee;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import com.steamcraftmc.bungee.utils.PlayerNameInfo;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;


public class BungeeCommand extends Command implements TabExecutor {

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
            return;
        }
        else if (args.length > 1 && (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set"))) {
        	PlayerNameInfo found = getPlayerByName(cs, args[1]);
        	if (found == null) {
        		cs.sendMessage(new TextComponent(ChatColor.RED + "Player not found."));
        		return;
        	}
        	
        	String hostName = null;
        	int port = 25565;
        	if (cs instanceof ProxiedPlayer) {
	            InetSocketAddress host = ((ProxiedPlayer)cs).getPendingConnection().getVirtualHost();
	            hostName = host.getHostString();
	            port = host.getPort();
        	}

        	int ixdomain = args[0].equalsIgnoreCase("get") ? 2 : 3;
        	if (args.length > ixdomain) {
        		hostName = args[ixdomain];
        		if (hostName.indexOf(':') > 0) {
        			port = Integer.parseInt(hostName.substring(1 + hostName.indexOf(':')));
        			hostName = hostName.substring(0, hostName.indexOf(':'));
        		}
        	}
        	if (hostName != null && hostName.length() > 0) {
                String prevName = this.plugin.sql.getLastServer(found.uniqueId, hostName, port);
                if (prevName == null) {
            		cs.sendMessage(new TextComponent(ChatColor.RED + "Player has not connected to " + hostName + "."));
            		return;
                }
                if (args[0].equalsIgnoreCase("get")) {
	            	cs.sendMessage(new TextComponent(
	            			ChatColor.translateAlternateColorCodes('&', 
	            					"&6The player &3" + found.name + "&6 was last seen on &5" + prevName + "&6."
	            					)
	            			));
	            	return;
                }
                else if (args.length > 2) {
                	String newName = args[2];
                	if (!ProxyServer.getInstance().getServers().containsKey(newName)) {
                		cs.sendMessage(new TextComponent(ChatColor.RED + "Server name not found."));
                		return;
                	}
                    this.plugin.sql.storeLastServer(found.uniqueId, hostName, port, newName);
	            	cs.sendMessage(new TextComponent(
	            			ChatColor.translateAlternateColorCodes('&', 
	            					"&6The player &3" + found.name + "&6 has been moved from &5" + 
        							prevName + "&6 to &5" + newName + "&6."
	            					)
	            			));
	            	return;
                }
        	}
        }

    	cs.sendMessage(new TextComponent(ChatColor.RED + "Usage:\n" +
    		ChatColor.GRAY + "/bplayer reload\n" +
    		ChatColor.GRAY + "/bplayer get <player> [domain[:port]]\n" +
    		ChatColor.GRAY + "/bplayer set <player> <server> [domain[:port]]" +
    		""));
    }

	private PlayerNameInfo getPlayerByName(CommandSender cs, String name) {
		PlayerNameInfo[] f = this.plugin.sql.FindPlayersByName(cs.hasPermission("bplayer.show-hidden"), name);
		return f.length == 1 ? f[0] : null;
	}

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
    	ArrayList<String> results = new ArrayList<String>();
    	if (args.length > 1 && (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set"))) {
			PlayerNameInfo[] found = this.plugin.sql.FindPlayersByName(sender.hasPermission("bplayer.show-hidden"), args[0]);
        	
			for (PlayerNameInfo p : found) {
				results.add(p.name);
			}
    	}
    	
    	return results;
    }
}
