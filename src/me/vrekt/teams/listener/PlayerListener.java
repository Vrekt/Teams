package me.vrekt.teams.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.vrekt.teams.Teams;
import me.vrekt.teams.cooldown.HomeCooldown;
import me.vrekt.teams.team.Team;
import net.md_5.bungee.api.ChatColor;

public class PlayerListener implements Listener {

	@EventHandler
	public void onMove(PlayerMoveEvent event) {

		Location from = event.getFrom();
		Location to = event.getTo();

		Player player = event.getPlayer();

		if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY()
				|| from.getBlockZ() != to.getBlockZ()) {

			if (HomeCooldown.isScheduledForHome(player)) {
				HomeCooldown.removeFromQueue(player);
				player.sendMessage(ChatColor.RED + "Teleportation to the team HQ has been cancelled.");
				return;
			}

		}

	}

	@EventHandler
	public void onAttack(EntityDamageByEntityEvent event) {

		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {

			Player player = (Player) event.getDamager();
			if (HomeCooldown.isScheduledForHome(player)) {
				HomeCooldown.removeFromQueue(player);
				player.sendMessage(ChatColor.RED + "Teleportation to the team HQ has been cancelled.");
				return;
			}

			if (Teams.getTeamsHandler().hasTeam(player.getUniqueId())) {
				Team team = Teams.getTeamsHandler().getTeam(player.getUniqueId());

				if (Teams.getTeamsHandler().hasTeam(event.getEntity().getUniqueId())) {
					Team ally = Teams.getTeamsHandler().getTeam(event.getEntity().getUniqueId());
					if (ally.hasAlly()) {
						if (ally.getAlly().equals(team)) {
							player.sendMessage(ChatColor.LIGHT_PURPLE + "You cannot attack your allies!");
							event.setCancelled(true);
							return;
						}
					}

				}

				if (team.isMember(event.getEntity().getUniqueId())) {
					player.sendMessage(ChatColor.LIGHT_PURPLE + "You cannot attack your teammates!");
					event.setCancelled(true);
				}
			}

		}

	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {

		Player player = event.getPlayer();
		if (Teams.getTeamsHandler().hasTeam(player.getUniqueId())) {
			Team team = Teams.getTeamsHandler().getTeam(player.getUniqueId());
			if (team.isInTeamChat(player)) {
				for (Player staff : Teams.getAdminHandler().getStaffForAlerts()) {
					staff.sendMessage(ChatColor.RED + "Spy: " + ChatColor.GREEN + "[Team] " + ChatColor.GOLD
							+ player.getName() + ChatColor.AQUA + ": " + event.getMessage());
				}
				team.sendTeamMessagePlayer(player.getName(), event.getMessage());
				event.setCancelled(true);
			}

			if (team.isInAllyChat(player)) {
				for (Player staff : Teams.getAdminHandler().getStaffForAlerts()) {
					staff.sendMessage(ChatColor.RED + "Spy: " + ChatColor.LIGHT_PURPLE + "[Ally] " + ChatColor.GOLD
							+ player.getName() + ChatColor.AQUA + ": " + event.getMessage());
				}
				team.sendAllyMessagePlayer(player.getName(), event.getMessage());
				event.setCancelled(true);
			}

		}

	}

	@EventHandler
	public void onLogout(PlayerQuitEvent event) {
		Teams.getTeamsHandler().handleLogout(event.getPlayer());

		if (Teams.getAdminHandler().showAlerts(event.getPlayer())) {
			Teams.getAdminHandler().removeAlerts(event.getPlayer());
		}

	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {

		if (event.getPlayer().hasPermission("team.admin")) {
			Teams.getAdminHandler().addAlerts(event.getPlayer());
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {

		Player player = event.getEntity().getKiller();
		if (player != null) {
			if (Teams.getTeamsHandler().hasTeam(player.getUniqueId())) {
				Team team = Teams.getTeamsHandler().getTeam(player.getUniqueId());
				team.setTeamKills(team.getTeamKills() + 1);
			}
		}

	}

}
