package me.vrekt.teams.cooldown;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.vrekt.teams.Teams;
import me.vrekt.teams.team.Team;
import net.md_5.bungee.api.ChatColor;

public class HomeCooldown {

	private static List<Player> homeCooldown = new ArrayList<Player>();

	public static void scheduleHome(Team team, Player player) {

		if (homeCooldown.contains(player)) {
			return;
		}

		homeCooldown.add(player);

		player.sendMessage(ChatColor.GREEN + "Teleporting to the team " + ChatColor.GOLD + "HQ" + ChatColor.GREEN
				+ " in " + ChatColor.GOLD + Teams.getConfiguration().getHomeCooldown() + ChatColor.GREEN
				+ " seconds.");

		new BukkitRunnable() {

			@Override
			public void run() {

				if (team == null || !homeCooldown.contains(player)) {
					return;
				}

				player.sendMessage(ChatColor.GREEN + "You were teleported to the team " + ChatColor.GOLD + "HQ"
						+ ChatColor.GREEN + "!");
				player.teleport(team.getTeamHome());
				homeCooldown.remove(player);

			}
		}.runTaskLater(Teams.getInstance(), Teams.getConfiguration().getHomeCooldown() * 20);

	}

	public static boolean isScheduledForHome(Player player) {
		return homeCooldown.contains(player);
	}

	public static void removeFromQueue(Player player) {
		homeCooldown.remove(player);
	}

}
