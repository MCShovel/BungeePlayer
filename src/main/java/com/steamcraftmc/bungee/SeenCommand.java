package com.steamcraftmc.bungee;

import java.text.SimpleDateFormat;
import java.util.*;

import com.steamcraftmc.bungee.utils.PlayerNameInfo;
import com.steamcraftmc.bungee.utils.PlayerStatsInfo;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.api.ProxyServer;

public class SeenCommand extends Command implements TabExecutor {

    private final BungeePlugin plugin;

    public SeenCommand(BungeePlugin plugin) {
        super("seen", "bplayer.seen", "playtime");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
    	if (args.length == 0 && cs instanceof ProxiedPlayer) {
    		args = new String[] { ((ProxiedPlayer)cs).getName() };
    	}
    	
        if (args.length == 1 && args[0].length() >= 2) {
			PlayerNameInfo[] found = this.plugin.sql.FindPlayersByName(cs.hasPermission("bplayer.show-hidden"), args[0]);
        	
        	if (found.length == 0) {
                cs.sendMessage(new TextComponent(ChatColor.RED + "Unable to find a player by that name."));
        	}
        	else if (found.length > 1) {
        		StringBuilder sb = new StringBuilder();
        		if (found.length > 10) {
        			sb.append("(too many matches)");
        		} 
        		else {
	        		for (PlayerNameInfo p : found) {
	        			if (sb.length() > 0) {
	        				sb.append(", ");
	        			}
	        			
	        			sb.append(p.name);
	        		}
        		}
        	    cs.sendMessage(new TextComponent(ChatColor.RED + "Multiple players found: " + ChatColor.GRAY + sb.toString()));
        	}
        	else {
        		PlayerNameInfo pn = found[0];
        		PlayerStatsInfo ps = this.plugin.sql.PlayerStats(pn.uniqueId);
        		PlayerNameInfo[] alias = this.plugin.sql.PlayersNamesByUUID(pn.uniqueId);
        		StringBuilder sb = new StringBuilder();
        		sb.append(ChatColor.GOLD);
        		sb.append("Player ");
        		sb.append(ChatColor.DARK_AQUA);
        		sb.append(pn.name);
        		sb.append(ChatColor.GOLD);
        		sb.append(" has been ");
        		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pn.uniqueId);
        		long duration = 0;
        		
        		if (player != null) {
        			duration = (System.currentTimeMillis() - pn.lastJoin) / 1000 / 60;
        			sb.append(ChatColor.DARK_GREEN);
        			sb.append("online ");
        		}
        		else {
        			duration = (System.currentTimeMillis() - (ps != null ? ps.lastSeen : pn.lastJoin)) / 1000 / 60;
        			sb.append(ChatColor.RED);
        			sb.append("offline ");
        		}

        		sb.append(ChatColor.GOLD);
    			sb.append("for ");
    			if (duration > 1440) {
        			sb.append(duration / 1440);
        			sb.append(" days.");
    			}
    			else if (duration > 120) {
        			sb.append(duration / 60);
        			sb.append(" hours.");
    			} else {
    				if (duration > 60) {
            			sb.append(" an hour and ");
            			duration -= 60;
    				}
        			sb.append(duration);
        			sb.append(" minutes.");
    			}
    			sb.append('\n');
        		sb.append(ChatColor.GOLD);
        		sb.append("Joined on: ");
        		sb.append(ChatColor.GRAY);
        		sb.append(new SimpleDateFormat("MMM d, yyyy").format(new Date(pn.firstJoin)));
    			sb.append('\n');
        		sb.append(ChatColor.GOLD);
        		sb.append("Last seen: ");
        		sb.append(ChatColor.GRAY);
        		sb.append(player != null ? "now" :
        				new SimpleDateFormat("MMM d, yyyy hh:mm aaa").format(new Date(ps != null ? ps.lastSeen : pn.lastJoin)));

        		if (alias.length > 1) {
            		sb.append(ChatColor.GOLD);
            		sb.append("Previous name(s): ");
            		sb.append(ChatColor.GRAY);
            		for (int ix = 1; ix < alias.length; ix++) {
            			if (ix > 1)
            				sb.append(", ");
            			sb.append(alias[ix].name);
            		}
        		}
        		
        		if (ps != null) {
	        		sb.append('\n');
            		sb.append(ChatColor.GOLD);
	        		sb.append("Playtime: ");
            		sb.append(ChatColor.GRAY);
	        		duration = ps.playTime / 1000 / 60;
	        		if (player != null)
	        			duration += (System.currentTimeMillis() - pn.lastJoin) / 1000 / 60;

	    			if (duration > 1440) {
	        			sb.append(duration / 1440);
	        			sb.append(" days ");
	        			duration = duration % 1440;
	    			}
	    			if (duration > 120) {
	        			sb.append(duration / 60);
	        			sb.append(" hours ");
	        			duration = duration % 60;
	    			}
        			sb.append(duration);
        			sb.append(" minutes");
        		}
        		
        		cs.sendMessage(new TextComponent(sb.toString()));
        	}
        } else {
            cs.sendMessage(new TextComponent("Usage: /seen [player]"));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
    	ArrayList<String> results = new ArrayList<String>();
    	
    	if ((args.length == 0 || (args.length == 1 && args[0].length() == 0)) 
    			&& sender instanceof ProxiedPlayer) {
    		results.add(((ProxiedPlayer)sender).getName());
    	}
    	else if (args.length == 1 && args[0].length() >= 2) {
			PlayerNameInfo[] found = this.plugin.sql.FindPlayersByName(sender.hasPermission("bplayer.show-hidden"), args[0]);
        	
			for (PlayerNameInfo p : found) {
				results.add(p.name);
			}
    	}
    	
    	return results;
    }
}
