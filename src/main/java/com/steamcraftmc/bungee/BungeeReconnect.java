package com.steamcraftmc.bungee;

import java.net.InetSocketAddress;

import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeReconnect extends AbstractReconnectHandler {

	private final BungeePlugin bungeePlugin;
	
	public BungeeReconnect(BungeePlugin bungeePlugin) {
		this.bungeePlugin = bungeePlugin;
	}

	@Override
	public void save() {
	}

	@Override
	public void close() {
	}

	@Override
	public void setServer(ProxiedPlayer player) {
        InetSocketAddress host = player.getPendingConnection().getVirtualHost();

        String serverName = null;
        if( player.getReconnectServer() != null ) 
        	serverName = player.getReconnectServer().getName(); 
    	else 
    		serverName = player.getServer().getInfo().getName();
        
        this.bungeePlugin.sql.storeLastServer(
        	player.getUniqueId(), host.getHostString(), host.getPort(), serverName
        	);
	}

	@Override
	protected ServerInfo getStoredServer(ProxiedPlayer player) {
        InetSocketAddress host = player.getPendingConnection().getVirtualHost();
        String serverName = this.bungeePlugin.sql.getLastServer(player.getUniqueId(), host.getHostString(), host.getPort());
        if (serverName != null)
        	return ProxyServer.getInstance().getServerInfo(serverName);
        
        return null;
	}

}
