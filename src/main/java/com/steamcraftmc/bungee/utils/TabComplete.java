package com.steamcraftmc.bungee.utils;

import java.util.*;

import com.steamcraftmc.bungee.BungeePlugin;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class TabComplete implements Listener {
	private final BungeePlugin plugin;
	private final HashMap<UUID, LastTabRequest> _history;

	private class LastTabRequest {
		long time;
		String text;
	}

	public TabComplete(BungeePlugin plugin) {
		this.plugin = plugin;
		this._history = new HashMap<UUID, LastTabRequest>();
	}

	@EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDisconnect(PlayerDisconnectEvent e) {
		_history.remove(e.getPlayer().getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
    public void onTabCompleteFirst(TabCompleteEvent e) {
		if (e.isCancelled() || !(e.getSender() instanceof ProxiedPlayer)) {
			return;
		}
		
		ProxiedPlayer p = (ProxiedPlayer)e.getSender();
		LastTabRequest preq = _history.get(p.getUniqueId());
		if (preq == null) {
			_history.put(p.getUniqueId(), preq = new LastTabRequest()); 
		}
		
		long now = System.currentTimeMillis();
		String text = e.getCursor();
		if (text.equals(preq.text) && (now - preq.time) < 1000) {
			e.setCancelled(true);
		}
		
		preq.time = now;
		preq.text = text; 
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTabComplete(TabCompleteEvent e) {
		if (e.isCancelled()) {
			return;
		}

		boolean seeHidden = false;
		if (e.getSender() instanceof ProxiedPlayer) {
			seeHidden = ((ProxiedPlayer) e.getSender()).hasPermission("bplayer.show-hidden");
		}

		if (e.getSuggestions().isEmpty()) {
			String fullText = e.getCursor();
			// Chat seems to be doing it all by itself, so we are just handling
			// commands...
			if (fullText.startsWith("/")) {
				String[] args = fullText.substring(1).split("\\s+");
				if (args.length > 1) {
					String last = args[args.length - 1];
					if (last.length() >= 2) {
						for (ProxiedPlayer p : ProxyServer.getInstance().getPlayers()) {
							String pname = p.getName().toLowerCase();
							if ((seeHidden || !plugin.isHidden(pname)) && pname.startsWith(last)) {
								e.getSuggestions().add(p.getName());
							}
						}
					}
				}
			}
		}

		if (!constrainedResponse(seeHidden, e.getSuggestions())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTabCompleteResponse(TabCompleteResponseEvent e) {
		if (e.isCancelled()) {
			return;
		}

		boolean seeHidden = false;
		if (e.getSender() instanceof ProxiedPlayer) {
			seeHidden = ((ProxiedPlayer) e.getSender()).hasPermission("bplayer.show-hidden");
		}

		if (!constrainedResponse(seeHidden, e.getSuggestions())) {
			e.setCancelled(true);
		}	
	}

	private boolean constrainedResponse(boolean seeHidden, List<String> suggestions) {

		if (suggestions == null) {
			return false;
		}

		HashSet<String> seen = new HashSet<String>();

		for (int ix = suggestions.size() - 1; ix >= 0; ix--) {
			String s = suggestions.get(ix);
			if (s == null || s.length() == 0) {
				suggestions.remove(ix);
				continue;
			}

			String slower = s.toLowerCase();
			if (seen.contains(slower)) {
				suggestions.remove(ix);
				continue;
			}
			seen.add(slower);

			if (s.startsWith("/") && s.indexOf(':') > 0) {
				suggestions.remove(ix);
			} else if (!seeHidden && plugin.isHidden(s)) {
				suggestions.remove(ix);
			}
		}
		
		if (suggestions.size() > 1) {
			seen.clear();
			for (int ix = suggestions.size() - 1; ix >= 0; ix--) {
				String s = suggestions.get(ix);
				int offset = s.indexOf(' ');
				if (offset > 0) {
					suggestions.set(ix, s = s.substring(0, offset));
				}
				
				String slower = s.toLowerCase();
				if (seen.contains(slower)) {
					suggestions.remove(ix);
					continue;
				}
				seen.add(slower);
			}
		}

		int size = suggestions.size();
		if (size > 18) {
			return false;
		}

		return true;
	}
}
