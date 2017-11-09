package me.vrekt.teams.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class TeamAdminHandler {

	private List<Player> showAlerts = new ArrayList<Player>();
	
	public List<Player> getStaffForAlerts() {
		return showAlerts;
	}
	
	public boolean showAlerts(Player player) {
		return showAlerts.contains(player);
	}
	
	public void removeAlerts(Player player) {
		showAlerts.remove(player);
	}
	
	public void addAlerts(Player player) {
		showAlerts.add(player);
	}
	
}
