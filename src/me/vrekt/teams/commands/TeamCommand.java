package me.vrekt.teams.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.vrekt.teams.Teams;
import me.vrekt.teams.cooldown.HomeCooldown;
import me.vrekt.teams.team.Team;
import net.md_5.bungee.api.ChatColor;

public class TeamCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

		if (command.getName().equalsIgnoreCase("team")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You cannot do this via console!");
				return true;
			}

			Player player = (Player) sender;

			if (Teams.getBanHandler().isBanned(player.getUniqueId())) {
				player.sendMessage(ChatColor.RED + "You are not allowed to execute this command since you're banned!");
				return true;
			}

			if (args.length == 0) {
				helpOne(player);
				return true;
			}

			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("help")) {

					int operation = 0;

					try {
						operation = Integer.parseInt(args[1]);
					} catch (IllegalArgumentException ex) {
						player.sendMessage(ChatColor.RED + "Invalid page! (1-2)");
						return true;
					}

					if (operation == 1) {
						helpOne(player);
						return true;
					} else if (operation == 2) {
						helpTwo(player);
						return true;
					} else {
						player.sendMessage(ChatColor.RED + "Invalid page! (1-2)");
						return true;
					}

				}
			}

			if (args.length == 1) {

				boolean hasTeam = Teams.getTeamsHandler().hasTeam(player.getUniqueId());

				Team team = null;
				if (hasTeam) {
					team = Teams.getTeamsHandler().getTeam(player.getUniqueId());
				}

				String action = args[0];
				switch (action.toLowerCase()) {
				case "disband":
				case "remove":
				case "delete":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {
						if (team.isAdmin(player.getUniqueId())) {

							Bukkit.broadcastMessage(
									ChatColor.AQUA + player.getName() + ChatColor.RED + " has disbanded the team "
											+ ChatColor.GOLD + team.getTeamName() + ChatColor.RED + "!");

							Teams.getTeamsHandler().removeTeam(team);
							team.disband();
							return true;

						} else {
							player.sendMessage(ChatColor.RED + "You cannot do this!");
							return true;
						}
					}

				case "who":
				case "info":
				case "show":

					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {
						info(team, player);
						return true;
					}
				case "leave":
				case "quit":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {
						if (team.isOwner(player.getUniqueId())) {
							player.sendMessage(
									ChatColor.RED + "You cannot do this! Use /team owner <player> before leaving.");
							return true;
						}

						team.removeTeamMember(player.getUniqueId());
						team.sendTeamMessage(
								ChatColor.GOLD + player.getName() + ChatColor.RED + " has left your team!");
						player.sendMessage(ChatColor.RED + "You have left!");

						if (team.getAllMembers().size() == 0) {
							Bukkit.broadcastMessage(
									ChatColor.AQUA + player.getName() + ChatColor.RED + " has disbanded the team "
											+ ChatColor.GOLD + team.getTeamName() + ChatColor.RED + "!");

							Teams.getTeamsHandler().removeTeam(team);
							team.disband();
							return true;
						}
						return true;

					}

				case "home":
				case "hq":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {

						if (team.hasTeamHome()) {
							if (HomeCooldown.isScheduledForHome(player)) {
								player.sendMessage(ChatColor.RED + "You are already scheduled to be teleported!");
								return true;
							}

							HomeCooldown.scheduleHome(team, player);
							return true;

						} else {
							player.sendMessage(ChatColor.RED + "No team HQ has been set!");
							return true;
						}

					}

				case "sethome":
				case "sethq":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {
						if (team.isAdmin(player.getUniqueId())) {

							if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
								team.setTeamHome(player.getLocation());
								team.sendTeamMessage(ChatColor.GOLD + player.getName() + ChatColor.GREEN
										+ " has set the team home!");
								return true;
							} else {
								player.sendMessage(
										ChatColor.RED + "Please move to solid ground before setting the team home.");
								return true;
							}

						} else {
							player.sendMessage(ChatColor.RED + "You cannot set the team home.");
							return true;
						}
					}

				case "chat":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {
						if (team.isInTeamChat(player)) {
							team.removeFromTeamChat(player);
							player.sendMessage(ChatColor.GREEN + "You are no longer chatting in the team chat.");
							return true;
						} else {
							team.setInTeamChat(player);
							if (team.isInAllyChat(player)) {
								team.removeFromAllyChat(player);
							}
							player.sendMessage(ChatColor.GREEN + "You are now chatting in the team chat.");
							return true;
						}
					}
				case "ally":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (!team.hasAlly()) {
						player.sendMessage(ChatColor.RED + "No ally found!");
						return true;
					}

					if (team != null) {
						if (team.isInAllyChat(player)) {
							team.removeFromAllyChat(player);
							player.sendMessage(ChatColor.GREEN + "You are no longer chatting in the ally chat.");
							return true;
						} else {
							team.setInAllyChat(player);
							if (team.isInTeamChat(player)) {
								team.removeFromTeamChat(player);
							}
							player.sendMessage(ChatColor.GREEN + "You are now chatting in the ally chat.");
							return true;
						}
					}

				}

			}

			if (args.length == 2) {

				String action = args[0];
				String name = args[1];

				boolean hasTeam = Teams.getTeamsHandler().hasTeam(player.getUniqueId());
				boolean isTeam = Teams.getTeamsHandler().isTeam(name);

				boolean isTeamName = Bukkit.getPlayer(name) != null
						&& Teams.getTeamsHandler().hasTeam(Bukkit.getPlayer(name).getUniqueId());

				Player who = Bukkit.getPlayer(name);

				Team team = null;
				if (Teams.getTeamsHandler().hasTeam(player.getUniqueId())) {
					team = Teams.getTeamsHandler().getTeam(player.getUniqueId());
				}

				switch (action.toLowerCase()) {
				case "create":
				case "new":
				case "make":
					if (hasTeam) {
						player.sendMessage(ChatColor.RED + "You cannot create another team while you're in one!");
						return true;
					}

					if (isTeam || isTeamName) {
						player.sendMessage(ChatColor.RED + "This team already exists, try another name.");
						return true;
					}

					if (!Teams.getConfiguration().isNameAllowed(name)) {
						player.sendMessage(
								ChatColor.RED + "This team-name contains invalid or blacklisted characters!");
						return true;
					}

					Bukkit.broadcastMessage(ChatColor.AQUA + player.getName() + ChatColor.GRAY
							+ " has created the team " + ChatColor.GOLD + name + ChatColor.GRAY + "!");

					Teams.getTeamsHandler().createTeam(new Team(name, player.getUniqueId()));
					return true;
				case "who":
				case "info":
				case "show":

					if (!isTeam && !isTeamName) {
						player.sendMessage(ChatColor.RED + "Team not found!");
						return true;
					}

					if (Teams.getTeamsHandler().isTeam(name)) {
						info(Teams.getTeamsHandler().getTeam(name), player);
						return true;
					}

					if (who != null && Teams.getTeamsHandler().hasTeam(who.getUniqueId())) {
						info(Teams.getTeamsHandler().getTeam(who.getUniqueId()), player);
						return true;
					}
				case "invite":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {

						if (team.isAdmin(player.getUniqueId())) {

							if (who == null) {
								player.sendMessage(ChatColor.RED + "Invalid player.");
								return true;
							}

							if (team.isMember(who.getUniqueId())) {
								player.sendMessage(ChatColor.RED + "This player is already in your team!");
								return true;
							}

							if (!Teams.getInviteHandler().canInvite(who, team)) {
								player.sendMessage(ChatColor.RED + "You cannot invite this player yet!");
								return true;
							}

							team.sendTeamMessage(ChatColor.GOLD + player.getName() + ChatColor.GREEN + " has invited "
									+ ChatColor.GOLD + who.getName() + ChatColor.GREEN + " to your team.");
							Teams.getInviteHandler().dispatchInvite(team, who);
							return true;

						} else {
							player.sendMessage(ChatColor.RED + "You cannot invite other members!");
							return true;
						}

					}
				case "revoke":
				case "fuckoff":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {

						if (team.isAdmin(player.getUniqueId())) {

							if (who == null) {
								player.sendMessage(ChatColor.RED + "Invalid player.");
								return true;
							}

							if (team.isMember(who.getUniqueId())) {
								player.sendMessage(ChatColor.RED + "This player is already in your team!");
								return true;
							}

							if (Teams.getInviteHandler().canAccept(who, team)) {
								Teams.getInviteHandler().removeFromInvite(who, team);
								team.sendTeamMessage(ChatColor.GOLD + player.getName() + ChatColor.RED
										+ " has revoked the invite for player " + ChatColor.GOLD + who.getName()
										+ ChatColor.RED + "!");
								who.sendMessage(ChatColor.RED + "Your invitation to the team " + ChatColor.GOLD
										+ team.getTeamName() + ChatColor.RED + " was revoked!");
								return true;
							} else {
								player.sendMessage(ChatColor.RED + "This player was not invited!");
								return true;
							}

						} else {
							player.sendMessage(ChatColor.RED + "You cannot do this!");
							return true;
						}
					}
				case "accept":
				case "join":

					if (hasTeam) {
						player.sendMessage(ChatColor.RED + "You must leave your current team first.");
						return true;
					}

					if (!isTeam && !isTeamName) {
						player.sendMessage(ChatColor.RED + "Team not found!");
						return true;
					}

					Team invite = isTeamName ? Teams.getTeamsHandler().getTeam(who.getUniqueId())
							: Teams.getTeamsHandler().getTeam(name);

					if (Teams.getInviteHandler().canAccept(player, invite)) {
						invite.addTeamMember(player.getUniqueId());
						Teams.getInviteHandler().removeFromInvite(player, team);
						invite.sendTeamMessage(
								ChatColor.GOLD + player.getName() + ChatColor.AQUA + " has joined your team!");
						return true;
					}

				case "kick":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {
						if (team.isAdmin(player.getUniqueId())) {

							if (team.getAllMembersAsName().contains(name.toLowerCase())) {

								if (team.isOwner(team.getUUIDFromName(name))) {
									player.sendMessage(ChatColor.RED + "You cannot kick this person.");
									return true;
								}

								if (name.equalsIgnoreCase(player.getName())) {
									player.sendMessage(ChatColor.RED + "You cannot kick yourself.");
									return true;
								}

								team.removeTeamMember(team.getUUIDFromName(name));
								team.sendTeamMessage(ChatColor.GOLD + name + ChatColor.RED + " has been kicked!");
								if (who != null) {
									who.sendMessage(ChatColor.RED + "You have been kicked.");
								}
								return true;
							} else {
								player.sendMessage(ChatColor.RED + "This player is not in your team!");
								return true;
							}

						} else {
							player.sendMessage(ChatColor.RED + "You cannot do this!");
							return true;
						}
					}
				case "promote":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {
						if (team.isAdmin(player.getUniqueId())) {
							UUID id = team.getUUIDFromName(name);
							if (team.isAdmin(id)) {
								player.sendMessage(ChatColor.RED + "This player is already promoted!");
								return true;
							}

							if (!team.isMember(id)) {
								player.sendMessage(ChatColor.RED + "This player is not in your team!");
								return true;
							}

							team.sendTeamMessage(
									ChatColor.GOLD + name + ChatColor.GREEN + " was promoted to Moderator.");
							team.addTeamModerator(id);
							return true;

						} else {
							player.sendMessage(ChatColor.RED + "You cannot do this!");
							return true;
						}
					}

				case "demote":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {
						if (team.isAdmin(player.getUniqueId())) {
							UUID id = team.getUUIDFromName(name);

							if (!team.isMember(id)) {
								player.sendMessage(ChatColor.RED + "This player is not in your team!");
								return true;
							}

							if (!team.isAdmin(id)) {
								player.sendMessage(ChatColor.RED + "This player is already demoted!");
								return true;
							}

							team.sendTeamMessage(ChatColor.GOLD + name + ChatColor.RED + " was demoted.");
							team.removeTeamModerator(id);
							return true;
						} else {
							player.sendMessage(ChatColor.RED + "You cannot do this!");
							return true;
						}
					}
				case "owner":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {
						if (team.isOwner(player.getUniqueId())) {
							UUID id = team.getUUIDFromName(name);
							if (team.isMember(id)) {
								if (team.isOwner(id)) {
									player.sendMessage(ChatColor.RED + "You cant owner the owner!");
									return true;
								} else {
									Teams.getTeamsHandler().ownerChange(team, id);
									team.sendTeamMessage(
											ChatColor.GOLD + name + ChatColor.GREEN + " is now the owner.");
									team.setTeamOwner(id);
									return true;
								}
							} else {

								player.sendMessage(ChatColor.RED + "This player is not in your team!");
								return true;

							}

						} else {
							player.sendMessage(ChatColor.RED + "You cannot do this!");
							return true;
						}

					}
				case "tag":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {
						if (team.isAdmin(player.getUniqueId())) {
							if (!Teams.getConfiguration().isNameAllowed(name)) {
								player.sendMessage(ChatColor.RED + "Invalid name!");
								return true;
							}

							team.sendTeamMessage(ChatColor.GOLD + player.getName() + ChatColor.GREEN
									+ " has changed the team name to " + ChatColor.AQUA + name + ChatColor.GREEN + "!");
							Bukkit.broadcastMessage(ChatColor.GOLD + team.getTeamName() + ChatColor.AQUA
									+ " has changed their name to " + ChatColor.GOLD + name);
							team.setTeamName(name);
							return true;

						} else {
							player.sendMessage(ChatColor.RED + "You cannot do this!");
							return true;
						}

					}

				case "ally":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (!Teams.getConfiguration().canAlly()) {
						player.sendMessage(ChatColor.RED + "This feature has been disabled.");
						return true;
					}

					if (team != null) {
						if (!isTeam && !isTeamName) {
							player.sendMessage(ChatColor.RED + "Team not found!");
							return true;
						}

						Team ally = isTeamName ? Teams.getTeamsHandler().getTeam(who.getUniqueId())
								: Teams.getTeamsHandler().getTeam(name);

						if (ally.equals(team)) {
							player.sendMessage(ChatColor.RED + "You cannot ally your own team!");
							return true;
						}

						if (ally.hasAlly() || team.hasAlly()) {
							player.sendMessage(ChatColor.RED + "You cannot ally this team!");
							return true;
						}

						if (Teams.getInviteHandler().canAlly(ally, team)) {
							ally.setAlly(team);
							team.setAlly(ally);
							Teams.getInviteHandler().removeFromAlly(ally, team);
							team.sendTeamMessage(ChatColor.GOLD + "You are now allied with " + ChatColor.AQUA
									+ ally.getTeamName() + ChatColor.GOLD + "!");
							ally.sendTeamMessage(ChatColor.GOLD + "You are now allied with " + ChatColor.AQUA
									+ team.getTeamName() + ChatColor.GOLD + "!");
							return true;
						} else {
							if (Teams.getInviteHandler().canSendAllyInvite(team, ally)) {
								Teams.getInviteHandler().dispatchAllyInvite(team, ally);
								team.sendTeamMessage(ChatColor.GOLD + player.getName() + ChatColor.GREEN
										+ " has sent an ally request to " + ChatColor.GOLD + ally.getTeamName()
										+ ChatColor.GREEN + "!");
								return true;
							} else {
								player.sendMessage(ChatColor.RED + "You cannot ally this team yet.");
								return true;
							}
						}

					}

				case "neutral":
					if (!hasTeam) {
						player.sendMessage(ChatColor.RED + "You do not have a team!");
						return true;
					}

					if (team != null) {
						if (!isTeam && !isTeamName) {
							player.sendMessage(ChatColor.RED + "Team not found!");
							return true;
						}

						Team ally = isTeamName ? Teams.getTeamsHandler().getTeam(who.getUniqueId())
								: Teams.getTeamsHandler().getTeam(name);

						if (ally.equals(team)) {
							player.sendMessage(ChatColor.RED + "You cannot neutral your own team!");
							return true;
						}

						if (team.getAlly().equals(ally)) {
							ally.setAlly(null);
							team.setAlly(null);

							ally.sendTeamMessage(ChatColor.GOLD + team.getTeamName() + ChatColor.RED
									+ " is no longer allied with you.");
							team.sendTeamMessage(ChatColor.RED + "No longer allied with " + ChatColor.GOLD
									+ ally.getTeamName() + ChatColor.RED + ".");
							return true;

						} else {
							player.sendMessage(ChatColor.RED + "This team is not your ally!");
							return true;
						}

					}

				default:
					helpOne(player);
					return true;
				}

			}

		}

		return true;

	}

	public void helpOne(Player player) {
		player.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "------------------[ " + ChatColor.RED
				+ " Teams Help " + ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + " ]------------------\n");
		player.sendMessage(ChatColor.GOLD + "    /team create <name> - " + ChatColor.AQUA + "Create a new Team.");
		player.sendMessage(ChatColor.GOLD + "    /team who <name/player> - " + ChatColor.AQUA
				+ "View information on certain team.");
		player.sendMessage(ChatColor.GOLD + "    /team leave - " + ChatColor.AQUA + "Leave your team.");
		player.sendMessage(ChatColor.GOLD + "    /team disband - " + ChatColor.AQUA + "Disband your team.");
		player.sendMessage(ChatColor.GOLD + "    /team home - " + ChatColor.AQUA + "Teleport to the team home.");
		player.sendMessage(ChatColor.GOLD + "    /team sethome - " + ChatColor.AQUA + "Set the home for your team.");
		player.sendMessage(ChatColor.GOLD + "    /team tag <name> - " + ChatColor.AQUA + "Change your team name.");
		player.sendMessage(ChatColor.GOLD + "    /team promote <name> - " + ChatColor.AQUA + "Promote a player.");
		player.sendMessage(ChatColor.GOLD + "    /team demote <name> - " + ChatColor.AQUA + "Demote a team moderator.");
		player.sendMessage(ChatColor.GOLD + "    /team owner <name> - " + ChatColor.AQUA + "Step down as leader.");
		player.sendMessage(
				ChatColor.GOLD + "    /team invite <name> - " + ChatColor.AQUA + "Invite a new member to your team.");
		player.sendMessage(ChatColor.GOLD + "    /team help <page> - " + ChatColor.AQUA + "View even more commands!");

	}

	public void helpTwo(Player player) {
		player.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "------------------[ " + ChatColor.RED
				+ " Teams Help " + ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + " ]------------------\n");
		player.sendMessage(ChatColor.GOLD + "    /team revoke <name> - " + ChatColor.AQUA + "Revoke the invite.");
		player.sendMessage(ChatColor.GOLD + "    /team kick <name> - " + ChatColor.AQUA + "Kick member of your team.");
		player.sendMessage(ChatColor.GOLD + "    /team ally <team> - " + ChatColor.AQUA + "Ally another team.");
		player.sendMessage(ChatColor.GOLD + "    /team neutral <team> - " + ChatColor.AQUA + "Neutral your ally.");
		player.sendMessage(ChatColor.GOLD + "    /team chat - " + ChatColor.AQUA + "Toggle team chat.");
		player.sendMessage(ChatColor.GOLD + "    /team ally - " + ChatColor.AQUA + "Toggle ally chat.");
	}

	public void info(Team team, Player player) {
		player.sendMessage(ChatColor.GOLD + "          " + ChatColor.STRIKETHROUGH + "-----------[ " + ChatColor.RED
				+ " " + team.getTeamName() + " " + ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + " ]-----------\n");

		String owner = Teams.getInstance().getServer().getOfflinePlayer(team.getTeamOwner()).getName();
		player.sendMessage(ChatColor.AQUA + "          Leader: " + ChatColor.GRAY + owner);

		String members = "";
		String moderators = "";

		for (UUID member : team.getAllMembers()) {
			String name = Teams.getInstance().getServer().getOfflinePlayer(member).getName();
			members = members.concat(name + ", ");
		}

		for (UUID moderator : team.getAllModerators()) {
			String name = Teams.getInstance().getServer().getOfflinePlayer(moderator).getName();
			moderators = moderators.concat(name + ", ");
		}

		String ally = team.hasAlly() ? team.getAlly().getTeamName() : ChatColor.RED + "None";
		String mod = moderators.length() == 0 ? ChatColor.RED + "None" : moderators;
		player.sendMessage(ChatColor.AQUA + "          Members: " + ChatColor.GOLD + members);
		player.sendMessage(ChatColor.AQUA + "          Moderators: " + ChatColor.GOLD + mod);
		player.sendMessage(ChatColor.AQUA + "          Ally: " + ChatColor.GOLD + ally);
		player.sendMessage(ChatColor.AQUA + "          Kills: " + ChatColor.GOLD + team.getTeamKills() + "\n");
		player.sendMessage(ChatColor.GOLD + "          " + ChatColor.STRIKETHROUGH + "-----------[ " + ChatColor.RED
				+ " " + team.getTeamName() + " " + ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + " ]-----------");
	}

}
