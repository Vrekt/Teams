package me.vrekt.teams.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.vrekt.teams.Teams;
import me.vrekt.teams.team.Team;
import net.md_5.bungee.api.ChatColor;

public class TeamAdminCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

		if (command.getName().equalsIgnoreCase("ta")) {
			if (sender.hasPermission("team.admin")) {

				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.RED + "The console cannot execute these actions.");
					return true;
				}

				Player player = (Player) sender;

				if (args.length == 0 || args.length == 1 && args[0].equalsIgnoreCase("help")) {
					help(sender);
					return true;
				}

				if (args.length == 1) {
					String action = args[0];
					switch (action.toLowerCase()) {
					case "togglespy":
						if (Teams.getAdminHandler().showAlerts(player)) {
							Teams.getAdminHandler().removeAlerts(player);
							player.sendMessage(ChatColor.RED + "Spy disabled.");
							return true;
						} else {
							Teams.getAdminHandler().addAlerts(player);
							player.sendMessage(ChatColor.GREEN + "Spy enabled.");
							return true;
						}
					case "stats":
						player.sendMessage(
								ChatColor.GOLD + "Teams: " + ChatColor.AQUA + Teams.getTeamsHandler().getTotalTeams());
						player.sendMessage(ChatColor.GOLD + "Load Time: " + ChatColor.AQUA + Teams.getLoadTime()
								+ " milliseconds.");
						return true;
					case "reload":

						Teams.getInstance().saveConfig();
						Teams.getInstance().reloadConfig();
						Teams.getConfiguration().reload(Teams.getInstance().getConfig());
						player.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
						return true;

					default:
						help(sender);
					}
				}

				if (args.length == 2) {
					String action = args[0];
					String name = args[1];
					boolean isTeam = Teams.getTeamsHandler().isTeam(name);
					boolean isTeamName = Bukkit.getPlayer(name) != null
							&& Teams.getTeamsHandler().hasTeam(Bukkit.getPlayer(name).getUniqueId());

					Player who = Bukkit.getPlayer(name);

					switch (action.toLowerCase()) {
					case "home":

						if (!isTeam && !isTeamName) {
							player.sendMessage(ChatColor.RED + "Team not found!");
							return true;
						}
						Team home = isTeam ? Teams.getTeamsHandler().getTeam(name)
								: Teams.getTeamsHandler().getTeam(who.getUniqueId());

						if (home.hasTeamHome()) {
							player.teleport(home.getTeamHome());
							player.sendMessage(ChatColor.GREEN + "Teleported.");
							return true;
						} else {
							player.sendMessage(ChatColor.RED + "No team-home found.");
							return true;
						}
					case "disband":
						if (!isTeam && !isTeamName) {
							player.sendMessage(ChatColor.RED + "Team not found!");
							return true;
						}
						Team disband = isTeam ? Teams.getTeamsHandler().getTeam(name)
								: Teams.getTeamsHandler().getTeam(who.getUniqueId());

						Teams.getTeamsHandler().removeTeam(disband);
						disband.disband();
						player.sendMessage(ChatColor.RED + "Team disbanded.");
						return true;

					case "rmban":

						if (who == null) {
							player.sendMessage(ChatColor.RED + "Invalid player.");
							return true;
						}

						if (Teams.getBanHandler().isBanned(who.getUniqueId())) {
							Teams.getBanHandler().removeBan(who.getUniqueId());
							player.sendMessage(ChatColor.GREEN + "Ban removed.");
							return true;
						} else {
							player.sendMessage(ChatColor.RED + "This player is not banned.");
							return true;
						}

					default:
						help(sender);
					}
				}

				if (args.length == 3) {
					String action = args[0];
					String name = args[1];

					Player who = Bukkit.getPlayer(name);
					switch (action.toLowerCase()) {
					case "ban":

						if (who == null) {
							player.sendMessage(ChatColor.RED + "Invalid player.");
							return true;
						}

						if (Teams.getBanHandler().isBanned(who.getUniqueId())) {
							player.sendMessage(ChatColor.RED + "This player is already banned.");
							return true;
						} else {

							int ticks = 0;

							try {
								ticks = Integer.parseInt(args[2]);

								if (ticks > 0) {
									Teams.getBanHandler().scheduleBan(who.getUniqueId(), ticks);
									player.sendMessage(ChatColor.RED + "Player banned.");
									return true;
								} else {
									player.sendMessage(ChatColor.RED + "0 minutes? What?");
									return true;
								}

							} catch (IllegalArgumentException ex) {
								player.sendMessage(ChatColor.RED + "Invalid integer.");
								return true;
							}

						}

					}
				}

			} else {
				sender.sendMessage(ChatColor.RED + "No permission.");
				return true;
			}
		}

		return false;
	}

	public void help(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "     " + ChatColor.STRIKETHROUGH + "------------[ " + ChatColor.RED
				+ " Teams Administrative Help " + ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + " ]----------\n");

		sender.sendMessage(ChatColor.GOLD + "    /ta ban <player> <minutes> - " + ChatColor.AQUA
				+ "Ban the player from accessing /team");
		sender.sendMessage(ChatColor.GOLD + "    /ta rmban <player> - " + ChatColor.AQUA + "remove the /team ban.");
		sender.sendMessage(ChatColor.GOLD + "    /ta togglespy - " + ChatColor.AQUA
				+ "Disables the spy feature for ally chat and team chat.");
		sender.sendMessage(ChatColor.GOLD + "   /ta disband <team> - " + ChatColor.AQUA + "Disband the team.");
		sender.sendMessage(
				ChatColor.GOLD + "   /ta home <team> - " + ChatColor.AQUA + "Teleport to the home of the team.");
		sender.sendMessage(ChatColor.GOLD + "   /ta stats - " + ChatColor.AQUA + "Displays stats.");
		sender.sendMessage(ChatColor.GOLD + "   /ta reload - " + ChatColor.AQUA + "Reloads the configuration.");
	}

}
