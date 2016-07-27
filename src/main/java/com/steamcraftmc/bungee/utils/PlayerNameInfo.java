package com.steamcraftmc.bungee.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerNameInfo {
	
	static final String SELECT = "SELECT bpn_playerUUID, bpn_playerName, bpn_firstJoined, bpn_lastJoined ";
	
	public PlayerNameInfo(ResultSet rs) throws SQLException {
		int ix = 0;
		this.uniqueId = UUID.fromString(rs.getString(++ix));
		this.name = rs.getString(++ix);
		this.firstJoin = rs.getLong(++ix);
		this.lastJoin = rs.getLong(++ix);
	}
	
	public final UUID uniqueId;
	public final String name;
	public final long firstJoin, lastJoin;
}
