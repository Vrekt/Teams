package me.vrekt.teams.admin;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import me.vrekt.teams.Teams;

public class TeamBanHandler {
	
	private HashMap<UUID, Integer> ticksBanned = new HashMap<UUID, Integer>();
	
	public void scheduleBan(UUID uuid, int ticks) {
		
		ticksBanned.put(uuid, ticks);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if(!ticksBanned.containsKey(uuid)) {
					return;
				}
				
				ticksBanned.remove(uuid);
				
			}
		}.runTaskLater(Teams.getInstance(), ticks * 60 * 20);
		
	}
	
	public boolean isBanned(UUID uuid) {
		return ticksBanned.containsKey(uuid);
	}
	
	public void removeBan(UUID uuid) {
		ticksBanned.remove(uuid);
	}
	
}