package com.steamcraftmc.bungee.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import com.steamcraftmc.bungee.*;

public class MySql {

	private final BungeePlugin plugin;

    private String host;
    private int port;
    private String username;
    private String password;
    private String database;
    private String tablePrefix;

    private Connection conn;

    public MySql(BungeePlugin plugin) {
    	
    	this.plugin = plugin;
    	loadConfig();
    }

    private void loadConfig() {
        this.host = plugin.config.getString("mysql.host", "localhost");
        this.port = plugin.config.getInt("mysql.port", 3306);
        this.username = plugin.config.getString("mysql.username", "root");
        this.password = plugin.config.getString("mysql.password", "password");
        this.database = plugin.config.getString("mysql.database", "bungeeplayer");
        this.tablePrefix = plugin.config.getString("mysql.tablePrefix", "bplayer_");
        closeConnection();
	}

    public Connection getConn() {
        if (!isConnected()) {
            try {
                this.conn = java.sql.DriverManager.getConnection(
                        "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?autoReconnect=true",
                        this.username, this.password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.conn;
    }

	public boolean isConnected() {
        try { 
        	return this.conn != null && this.conn.isClosed() == false; 
    	}
        catch(SQLException e) { 
        	this.conn = null; 
        	return false; 
    	}
    }

    public void closeConnection() {
        if (isConnected()) {
            try {
                this.conn.close();
                this.conn = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void exec(final String query) throws SQLException {
        PreparedStatement pst = getConn().prepareStatement(query);
        pst.executeUpdate();
        pst.close();
    }

    private ResultSet getResult(String query) {
        try {
            PreparedStatement pst = getConn().prepareStatement(query);
            return pst.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

	public void initSchema() throws SQLException {
		exec("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "connections` ( \n" +
	        "  `bpc_playerUUID` VARCHAR(40) NOT NULL, \n" +
	        "  `bpc_serverHost` VARCHAR(255) NOT NULL, \n" +
	        "  `bpc_serverPort` INT NOT NULL, \n" +
	        "  `bpc_lastServer` VARCHAR(255), \n" +
	        "  `bpc_firstSeen` BIGINT NOT NULL, \n" +
	        "  `bpc_lastSeen` BIGINT NOT NULL, \n" +
	        "  PRIMARY KEY (`bpc_playerUUID`, `bpc_serverHost`, `bpc_serverPort`)); \n" +
			"");

		exec("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "playerNames` ( \n" +
	        "  `bpn_playerUUID` VARCHAR(40) NOT NULL, \n" +
	        "  `bpn_playerName` VARCHAR(45), \n" +
	        "  `bpn_firstJoined` BIGINT NOT NULL, \n" +
	        "  `bpn_lastJoined` BIGINT NOT NULL, \n" +
	        "  PRIMARY KEY (`bpn_playerUUID`, `bpn_playerName`), " +
	        "  INDEX `bpn_playerByName` (`bpn_playerName`) " +
	        "  ); \n" +
			"");
		
		exec("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "playerStats` ( \n" +
		        "  `bps_playerUUID` VARCHAR(40) NOT NULL, \n" +
		        "  `bps_playerName` VARCHAR(45), \n" +
		        "  `bps_firstJoined` BIGINT NOT NULL, \n" +
		        "  `bps_lastSeen` BIGINT NOT NULL, \n" +
		        "  `bps_totalPlayTime` BIGINT NOT NULL, \n" +
		        "  PRIMARY KEY (`bps_playerUUID`)); \n" +
				"");
	}

	public void storeLastServer(UUID uniqueId, String hostString, int port, String serverName) {

		hostString = String.valueOf(hostString).replaceAll("[^\\w\\.\\-]", "_");
        try {
        	String now = String.valueOf(System.currentTimeMillis());
        	exec("INSERT INTO " + tablePrefix + "connections " +
        			"(`bpc_playerUUID`, `bpc_serverHost`, `bpc_serverPort`, `bpc_lastServer`, `bpc_firstSeen`, `bpc_lastSeen`) \n" +
        			"VALUES ( \n" +
        			"'" + uniqueId + "', \n" +
        			"'" + String.valueOf(hostString) + "', \n" +
        			"" + String.valueOf(port) + ", \n" +
        			"'" + serverName + "', \n" +
        			"" + now + ", \n" +
        			"" + now + " ) \n" +
        			"ON DUPLICATE KEY UPDATE \n" +
        			"bpc_lastServer = '" + serverName + "', \n" +
        			"bpc_lastSeen = " + now + "; \n"
        			);
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
	}

	public String getLastServer(UUID uniqueId, String hostString, int port) {

		hostString = String.valueOf(hostString).replaceAll("[^\\w\\.\\-]", "_");
        try {
        	ResultSet rs = getResult("SELECT `bpc_lastServer` FROM " + tablePrefix + "connections " +
        		"WHERE `bpc_playerUUID` = '" + uniqueId.toString() + "' " +
        		"AND `bpc_serverHost` = '" + String.valueOf(hostString) + "' " + 
        		"AND `bpc_serverPort` = " + String.valueOf(port) +  " " +
        		";");
            
        	if (rs.next()) {
                return rs.getString(1);
            }
            rs.close();
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

	public void onPlayerJoin(UUID uniqueId, String playerName) {

		playerName = String.valueOf(playerName).replaceAll("[^\\w]", "_");
		
        try {
        	String now = String.valueOf(System.currentTimeMillis());
        	exec("INSERT INTO " + tablePrefix + "playerNames " +
        			"(`bpn_playerUUID`, `bpn_playerName`, `bpn_firstJoined`, `bpn_lastJoined`) \n" +
        			"VALUES ( \n" +
        			"'" + uniqueId + "', \n" +
        			"'" + playerName + "', \n" +
        			"" + now + ", \n" +
        			"" + now + " ) \n" +
        			"ON DUPLICATE KEY UPDATE \n" +
        			"bpn_lastJoined = " + now + "; \n"
        			);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        
        try {
        	String now = String.valueOf(System.currentTimeMillis());
        	exec("INSERT INTO " + tablePrefix + "playerStats " +
        			"(`bps_playerUUID`, `bps_playerName`, `bps_firstJoined`, `bps_lastSeen`, `bps_totalPlayTime`) \n" +
        			"VALUES ( \n" +
        			"'" + uniqueId + "', \n" +
        			"'" + playerName + "', \n" +
        			"" + now + ", \n" +
        			"" + now + "," +
					"0 ) \n" +
        			"ON DUPLICATE KEY UPDATE \n" +
        			"bps_playerName = '" + playerName + "', " +
        			"bps_lastSeen = " + now + "; \n"
        			);
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
	}

	public void onPlayerQuit(UUID uniqueId, String playerName, long time) {

		playerName = String.valueOf(playerName).replaceAll("[^\\w]", "_");
        try {
        	String now = String.valueOf(System.currentTimeMillis());
        	exec("UPDATE " + tablePrefix + "playerStats SET " +
        			"bps_lastSeen = " + now + ", " +
        			"bps_totalPlayTime = bps_totalPlayTime + " + String.valueOf(time) + " " +
					"WHERE `bps_playerUUID` = '" + uniqueId + "'; \n"
        			);
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
	}

	public PlayerNameInfo[] FindPlayersByName(boolean seeHidden, String name) {
		name = String.valueOf(name).replaceAll("[^\\w]", "_");
		ArrayList<PlayerNameInfo> found = new ArrayList<>();
		ArrayList<PlayerNameInfo> exact = null;

        try {
        	ResultSet rs = getResult(PlayerNameInfo.SELECT + 
        			"FROM `" + tablePrefix + "playerNames` " +
        			"WHERE `bpn_playerName` LIKE '" + name +  "%' " +
					"ORDER BY `bpn_playerName` LIMIT 25;");
            
        	while (rs.next()) {
        		PlayerNameInfo pni = new PlayerNameInfo(rs);

        		if (seeHidden || !plugin.isHidden(pni.name)) {
	        		found.add(pni);
	        		if (pni.name.equalsIgnoreCase(name)) {
	        			if (exact == null) {
	        				exact = new ArrayList<PlayerNameInfo>();
	        			}
	        			exact.add(pni);
	        		}
        		}
            }
            rs.close();
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }

        if (exact != null && exact.size() > 0) {
        	return exact.toArray(new PlayerNameInfo[exact.size()]);
        }
        
        return found.toArray(new PlayerNameInfo[found.size()]);
	}

	public PlayerNameInfo[] PlayersNamesByUUID(UUID uniqueId) {
		ArrayList<PlayerNameInfo> found = new ArrayList<>();

        try {
        	ResultSet rs = getResult(PlayerNameInfo.SELECT + 
        			"FROM `" + tablePrefix + "playerNames` " +
        			"WHERE `bpn_playerUUID` = '" + uniqueId + "' " +
					"ORDER BY bpn_lastJoined DESC;");
            
        	while (rs.next()) {
        		PlayerNameInfo pni = new PlayerNameInfo(rs);
        		found.add(pni);
            }
            rs.close();
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        
        return found.toArray(new PlayerNameInfo[found.size()]);
	}

	public PlayerStatsInfo PlayerStats(UUID uniqueId) {
        try {
        	ResultSet rs = getResult(PlayerStatsInfo.SELECT + 
        			"FROM `" + tablePrefix + "playerStats` " +
        			"WHERE `bps_playerUUID` = '" + uniqueId + "';");
            
        	if (rs.next()) {
        		return new PlayerStatsInfo(rs);
            }
            rs.close();
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
	}
	
}