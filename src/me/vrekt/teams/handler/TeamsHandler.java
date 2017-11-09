package me.vrekt.teams.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.vrekt.teams.Teams;
import me.vrekt.teams.cooldown.HomeCooldown;
import me.vrekt.teams.team.Team;

public class TeamsHandler {

	private HashMap<UUID, Team> teamMapping = new HashMap<UUID, Team>();

	public void createTeam(Team team) {
		teamMapping.put(team.getTeamOwner(), team);
	}

	public void removeTeam(Team team) {
		teamMapping.remove(team.getTeamOwner(), team);
	}

	public void ownerChange(Team team, UUID newOwner) {
		teamMapping.remove(team.getTeamOwner());
		team.setTeamOwner(newOwner);
		teamMapping.put(newOwner, team);
	}

	public boolean hasTeam(UUID check) {

		Collection<Team> teams = teamMapping.values();
		Team team = teams.stream().filter(t -> t.getAllMembers().contains(check)).findAny().orElse(null);
		return teamMapping.containsKey(check) || team != null;

	}

	public boolean isTeam(String name) {

		Collection<Team> teams = teamMapping.values();
		Team t = teams.stream().filter(team -> name.equalsIgnoreCase(team.getTeamName())).findAny().orElse(null);
		return t != null;

	}

	public boolean isTeam(Team team) {
		return teamMapping.values().contains(team);
	}

	public Team getTeam(UUID owner) {

		if (teamMapping.containsKey(owner)) {
			return teamMapping.get(owner);
		} else {
			Collection<Team> teams = teamMapping.values();
			Team team = teams.stream().filter(t -> t.getAllMembers().contains(owner)).findAny().orElse(null);
			return team;
		}

	}

	public Team getTeam(String name) {
		Collection<Team> teams = teamMapping.values();
		return teams.stream().filter(t -> t.getTeamName().equalsIgnoreCase(name)).findAny().get();
	}

	public void handleLogout(Player player) {

		if (HomeCooldown.isScheduledForHome(player)) {
			HomeCooldown.removeFromQueue(player);
		}

		Teams.getInviteHandler().logout(player);

	}

	public int getTotalTeams() {
		return teamMapping.values().size();
	}

	public void writeTeams() {

		File teams = new File(Teams.getInstance().getDataFolder() + "/teams.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(teams);

		HashMap<Team, String> allies = new HashMap<Team, String>();

		if (config.contains("teams")) {
			for (String name : config.getConfigurationSection("teams").getKeys(false)) {

				Team team = new Team(name,
						UUID.fromString(config.getConfigurationSection("teams." + name).getString("owner")));

				for (String member : config.getConfigurationSection("teams." + name).getStringList("members")) {
					UUID uuid = UUID.fromString(member);
					if (uuid.equals(team.getTeamOwner())) {
						continue;
					}
					team.addTeamMember(uuid);
				}

				for (String moderator : config.getConfigurationSection("teams." + name).getStringList("moderators")) {
					UUID uuid = UUID.fromString(moderator);
					team.addTeamModerator(uuid);
				}

				Location home = null;

				if (config.contains("teams." + name + ".home")) {
					String location = config.getConfigurationSection("teams." + name).getString("home");

					World world = Bukkit.getWorld(location.split(" ")[0]);
					int x = Integer.parseInt(location.split(" ")[1]);
					int y = Integer.parseInt(location.split(" ")[2]);
					int z = Integer.parseInt(location.split(" ")[3]);

					home = new Location(world, x, y, z);

				}

				int kills = config.getConfigurationSection("teams." + name).getInt("kills");
				team.setTeamKills(kills);

				team.setTeamHome(home);

				if (config.contains("teams." + name + ".ally")) {
					allies.put(team, config.getConfigurationSection("teams." + name).getString("ally"));
				}

				createTeam(team);

			}

			for (Team team : allies.keySet()) {
				getTeam(team.getTeamOwner()).setAlly(getTeam(allies.get(team)));
			}

			allies.clear();

		}

	}

	public void saveTeams() {

		File teams = new File(Teams.getInstance().getDataFolder() + "/teams.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(teams);

		if (teamMapping.size() > 0) {
			config.set("teams", new ArrayList<>());
		}

		if (config.contains("teams")) {
			for (Team team : teamMapping.values()) {
				if (!config.contains("teams." + team.getTeamName())) {

					String home = null;
					String h = "";
					if (team.hasTeamHome()) {

						h = h.concat(team.getTeamHome().getWorld().getName() + " ");
						h = h.concat(team.getTeamHome().getBlockX() + " ");
						h = h.concat(team.getTeamHome().getBlockY() + " ");
						h = h.concat(team.getTeamHome().getBlockZ() + "");
						home = h;
					}

					List<String> members = new ArrayList<String>();
					List<String> moderators = new ArrayList<String>();
					for (UUID member : team.getAllMembers()) {
						members.add(member.toString());
					}

					for (UUID member : team.getAllModerators()) {
						moderators.add(member.toString());
					}

					config.set("teams." + team.getTeamName() + ".owner", team.getTeamOwner().toString());
					config.set("teams." + team.getTeamName() + ".home", home);
					config.set("teams." + team.getTeamName() + ".ally",
							team.hasAlly() ? team.getAlly().getTeamName() : null);
					config.set("teams." + team.getTeamName() + ".members", members);
					config.set("teams." + team.getTeamName() + ".moderators",
							moderators.size() == 0 ? null : moderators);
					config.set("teams." + team.getTeamName() + ".kills", team.getTeamKills());

				}

			}

			try {
				config.save(teams);
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (Team after : teamMapping.values()) {
				if (!config.contains("teams." + after.getTeamName())) {
					config.set("teams." + after.getTeamName(), null);
				}
			}

			try {
				config.save(teams);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		teamMapping.clear();

	}

}
