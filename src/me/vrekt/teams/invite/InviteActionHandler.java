package me.vrekt.teams.invite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.vrekt.teams.Teams;
import me.vrekt.teams.team.Team;
import net.md_5.bungee.api.ChatColor;

public class InviteActionHandler {

	private HashMap<Player, List<Team>> invites = new HashMap<Player, List<Team>>();
	private HashMap<Team, List<Team>> allyInvites = new HashMap<Team, List<Team>>();

	public void dispatchInvite(Team team, Player player) {

		invites.putIfAbsent(player, new ArrayList<Team>());

		List<Team> list = invites.get(player);
		list.add(team);
		invites.put(player, list);

		player.sendMessage(ChatColor.GOLD + team.getTeamName() + ChatColor.GREEN + " has invited you to join! \n"
				+ ChatColor.GRAY + "Type (/team accept " + team.getTeamName() + ") to accept.");

		new BukkitRunnable() {

			@Override
			public void run() {
				if (!Teams.getTeamsHandler().isTeam(team) || !invites.containsKey(player)
						|| team.isMember(player.getUniqueId())) {
					this.cancel();
					return;
				}
				invites.get(player).remove(team);
				player.sendMessage(ChatColor.RED + "Time has ran out to accept the invite for team " + ChatColor.GOLD
						+ team.getTeamName() + ChatColor.RED + "!");

			}
		}.runTaskLater(Teams.getInstance(), 1200);

	}

	public void dispatchAllyInvite(Team sender, Team ally) {

		allyInvites.putIfAbsent(sender, new ArrayList<Team>());
		allyInvites.get(sender).add(ally);

		ally.sendTeamMessage(ChatColor.GOLD + sender.getTeamName() + ChatColor.GREEN + " wishes to be an ally! \n"
				+ ChatColor.GRAY + "Type: (/team ally " + sender.getTeamName() + ") to accept.");

		new BukkitRunnable() {

			@Override
			public void run() {

				if (!Teams.getTeamsHandler().isTeam(sender) || !Teams.getTeamsHandler().isTeam(ally)
						|| sender.getAlly() != null && sender.getAlly().equals(ally)
						|| !allyInvites.get(sender).contains(ally)) {
					this.cancel();
					return;
				}

				allyInvites.get(sender).remove(ally);
				ally.sendTeamMessage(ChatColor.RED + "Time has ran out to accept the ally request for team "
						+ ChatColor.GOLD + sender.getTeamName() + ChatColor.RED + "!");
				sender.sendTeamMessage(ChatColor.RED + "The team " + ChatColor.GOLD + ally.getTeamName() + ChatColor.RED
						+ " did not accept your ally request!");

			}
		}.runTaskLater(Teams.getInstance(), 600);

	}

	public boolean canInvite(Player player, Team team) {
		return invites.containsKey(player) ? invites.get(player).contains(team) ? false : true : true;
	}

	public boolean canAccept(Player player, Team team) {
		return invites.containsKey(player) && invites.get(player).contains(team);
	}

	public void removeFromInvite(Player player, Team team) {
		if (invites.containsKey(player)) {
			invites.remove(player);
		}
	}

	public boolean canSendAllyInvite(Team sender, Team ally) {
		return allyInvites.containsKey(sender) ? allyInvites.get(sender).contains(ally) ? false : true : true;
	}

	public boolean canAlly(Team sender, Team ally) {
		return allyInvites.containsKey(sender) && allyInvites.get(sender).contains(ally);
	}

	public void removeFromAlly(Team team, Team ally) {
		allyInvites.get(team).remove(ally);
	}

	public boolean invitedForAlly(Team team, Team ally) {
		return allyInvites.containsKey(team) ? allyInvites.get(team).contains(ally) ? true : false : false;
	}

	public void logout(Player player) {
		if (invites.containsKey(player)) {
			invites.remove(player);
		}

	}

}
