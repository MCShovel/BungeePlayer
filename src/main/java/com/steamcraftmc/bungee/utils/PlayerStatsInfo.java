package com.steamcraftmc.bungee.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerStatsInfo {
	
	static final String SELECT = "SELECT bps_playerUUID, bps_playerName, bps_firstJoined, bps_lastSeen, bps_totalPlayTime ";
	
	public final UUID uniqueId;
	public final String name;
	public final long firstJoin, lastSeen, playTime;

	public PlayerStatsInfo(ResultSet rs) throws SQLException {
		int ix = 0;
		this.uniqueId = UUID.fromString(rs.getString(++ix));
		this.name = rs.getString(++ix);
		this.firstJoin = rs.getLong(++ix);
		this.lastSeen = rs.getLong(++ix);
		this.playTime = rs.getLong(++ix);
	}
}
