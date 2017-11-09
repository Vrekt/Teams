package me.vrekt.teams.team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class Team {

	private Location teamHome;
	private UUID teamOwner;

	private List<UUID> teamMembers;
	private List<UUID> teamModerators;
	private List<Player> teamChat;
	private List<Player> teamAllyChat;

	private String teamName;

	private Team teamAlly;
	
	private int teamKills;

	public Team(String teamName, UUID teamOwner) {

		this.teamName = teamName;
		this.teamOwner = teamOwner;

		teamMembers = new ArrayList<UUID>();
		teamModerators = new ArrayList<UUID>();
		teamChat = new ArrayList<Player>();
		teamAllyChat = new ArrayList<Player>();

		teamMembers.add(teamOwner);

	}

	public Location getTeamHome() {
		return teamHome;
	}

	public boolean hasTeamHome() {
		return teamHome != null;
	}

	public void setTeamHome(Location home) {
		teamHome = home;
	}

	public UUID getTeamOwner() {
		return teamOwner;
	}

	public void setTeamOwner(UUID owner) {
		teamOwner = owner;
	}

	public boolean isOwner(UUID check) {
		return teamOwner.equals(check);
	}

	public boolean isAdmin(UUID check) {
		return teamOwner.equals(check) || isModerator(check);
	}

	public void addTeamMember(UUID member) {
		teamMembers.add(member);
	}

	public void removeTeamMember(UUID member) {
		teamMembers.remove(member);
	}

	public boolean isMember(UUID check) {
		return teamMembers.contains(check);
	}

	public int getTotalMembers() {
		return teamMembers.size();
	}

	public List<UUID> getAllMembers() {
		return teamMembers;
	}

	public List<UUID> getAllModerators() {
		return teamModerators;
	}

	public List<String> getAllMembersAsName() {
		List<String> names = new ArrayList<String>();

		for (UUID uuid : teamMembers) {
			String name = Bukkit.getOfflinePlayer(uuid).getName().toLowerCase();
			names.add(name);
		}

		return names;

	}

	public UUID getUUIDFromName(String name) {

		for (UUID uuid : teamMembers) {
			String player = Bukkit.getOfflinePlayer(uuid).getName().toLowerCase();
			if (player.equalsIgnoreCase(name)) {
				return uuid;
			}

		}
		return null;

	}
	
	public int getTeamKills() {
		return teamKills;
	}

	public void setTeamKills(int kills) {
		teamKills = kills;
	}
	
	public void addTeamModerator(UUID member) {
		teamModerators.add(member);
	}

	public void removeTeamModerator(UUID member) {
		teamModerators.remove(member);
	}

	public boolean isModerator(UUID check) {
		return teamModerators.contains(check);
	}

	public void setInTeamChat(Player add) {
		teamChat.add(add);
	}

	public void removeFromTeamChat(Player remove) {
		teamChat.remove(remove);
	}

	public boolean isInTeamChat(Player check) {
		return teamChat.contains(check);
	}

	public void setInAllyChat(Player add) {
		teamAllyChat.add(add);
	}

	public void removeFromAllyChat(Player remove) {
		teamAllyChat.remove(remove);
	}

	public boolean isInAllyChat(Player check) {
		return teamAllyChat.contains(check);
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String name) {
		teamName = name;
	}

	public Team getAlly() {
		return teamAlly;
	}

	public boolean hasAlly() {
		return teamAlly != null;
	}

	public void setAlly(Team ally) {
		teamAlly = ally;
	}

	public void disband() {
		teamMembers.clear();
		teamModerators.clear();
		teamChat.clear();
		teamAllyChat.clear();

		if (hasAlly()) {
			teamAlly.setAlly(null);
		}

		teamAlly = null;
	}

	public void sendTeamMessage(String message) {

		for (UUID member : teamMembers) {
			Player player = Bukkit.getPlayer(member);
			if (player != null) {
				player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + getTeamName() + ChatColor.DARK_GRAY
						+ "] " + message);
			}
		}

	}

	public void sendTeamMessagePlayer(String playerName, String message) {
		for (UUID member : teamMembers) {
			Player player = Bukkit.getPlayer(member);
			if (player != null) {
				player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + getTeamName() + ChatColor.DARK_GRAY
						+ "] " + ChatColor.RED + playerName + ChatColor.AQUA + ": " + message);
			}
		}

	}

	public void sendAllyMessagePlayer(String playerName, String message) {
		for (UUID member : getAlly().getAllMembers()) {
			Player player = Bukkit.getPlayer(member);
			if (player != null) {
				player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + ChatColor.LIGHT_PURPLE + "Ally"
						+ ChatColor.DARK_GRAY + "] " + ChatColor.RED + playerName + ChatColor.AQUA + ": "
						+ ChatColor.LIGHT_PURPLE + message);
			}
		}

		for (UUID member : getAllMembers()) {
			Player player = Bukkit.getPlayer(member);
			if (player != null) {
				player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + ChatColor.LIGHT_PURPLE + "Ally"
						+ ChatColor.DARK_GRAY + "] " + ChatColor.RED + playerName + ChatColor.AQUA + ": "
						+ ChatColor.LIGHT_PURPLE + message);
			}
		}

	}

	
}
