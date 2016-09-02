package com.steamcraftmc.bungee;

import com.google.common.io.ByteStreams;
import com.steamcraftmc.bungee.utils.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeePlugin extends Plugin implements Listener {

    private static final ConfigurationProvider configProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
    public Configuration config;
    public MySql sql;

    @Override
    public void onEnable() {
        reloadConfig();
        
        try {
        	sql.initSchema();
        }
        catch(Exception e) {
        	getLogger().log(Level.SEVERE, "Unable to create schema: " + e.toString());
        	return;
        }
        
        PluginManager pm = getProxy().getPluginManager();
        pm.registerCommand(this, new BungeeCommand(this));
        pm.registerCommand(this, new SeenCommand(this));
        pm.registerListener(this, new BungeeEvents(this));
        getProxy().setReconnectHandler(new BungeeReconnect(this));
        pm.registerListener(this, new TabComplete(this));
    }

    public void reloadConfig() {
        try {
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getDataFolder().mkdirs();
                file.createNewFile();
                try (InputStream in = getResourceAsStream("config.yml");
                    FileOutputStream out = new FileOutputStream(file)) {
                    ByteStreams.copy(in, out);
                }
            }
            
            config = configProvider.load(file);
            hiddenPlayers = null;
            if (sql != null) {
            	sql.closeConnection();
            }
        	sql = new MySql(this);
        	
            } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error loading configuration", e);
        }
    }
    
    private Set<String> hiddenPlayers;

	public boolean isHidden(String name) {
		Set<String> hidden = this.hiddenPlayers;
		if (hidden == null) {
			hidden = new HashSet<String>();
			for (String s : config.getStringList("hidden"))
				hidden.add(s.toLowerCase());
			this.hiddenPlayers = hidden;
		}
		
		return name != null && hidden.contains(name.toLowerCase());
	}
}
