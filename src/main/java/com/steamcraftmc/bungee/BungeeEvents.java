package com.steamcraftmc.bungee;

import java.util.HashMap;
import java.util.UUID;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeEvents implements Listener {

	private class PInfo {
		private final long joinTime = System.currentTimeMillis();
		public long elapsed() { return System.currentTimeMillis() - joinTime; }
	}
	
	private final BungeePlugin bungeePlugin;
	private HashMap<UUID, PInfo> onlineTime;
	
	public BungeeEvents(BungeePlugin bungeePlugin) {
		this.bungeePlugin = bungeePlugin;
		this.onlineTime = new HashMap<UUID, PInfo>();
	}

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(PostLoginEvent e) {
    	this.onlineTime.put(e.getPlayer().getUniqueId(), new PInfo());
    	this.bungeePlugin.sql.onPlayerJoin(e.getPlayer().getUniqueId(), e.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerLogOut(PlayerDisconnectEvent e) {
    	PInfo p = onlineTime.remove(e.getPlayer().getUniqueId());
    	this.bungeePlugin.sql.onPlayerQuit(e.getPlayer().getUniqueId(), e.getPlayer().getName(), p != null ? p.elapsed() : 0L);
    }
}
