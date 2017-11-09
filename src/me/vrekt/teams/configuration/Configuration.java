package me.vrekt.teams.configuration;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.FileConfiguration;

import me.vrekt.teams.team.Team;

public class Configuration {

	private List<String> blacklistedNames;

	private int maxTeamSize;
	private int maxName;
	private int homeCooldown;

	private boolean enableAllies;
	private boolean logTeamChat;
	private boolean logAllyChat;

	public Configuration(FileConfiguration config) {
		blacklistedNames = config.getStringList("blacklisted-names");

		maxTeamSize = config.getInt("max-team-size");
		maxName = config.getInt("max-name-characters");
		homeCooldown = config.getInt("home-cooldown");

		enableAllies = config.getBoolean("enable-allies");
		logTeamChat = config.getBoolean("team-chat-logging");
		logAllyChat = config.getBoolean("team-ally-logging");
	}

	public void reload(FileConfiguration config) {
		blacklistedNames = config.getStringList("blacklisted-names");

		maxTeamSize = config.getInt("max-team-size");
		maxName = config.getInt("max-name-characters");
		homeCooldown = config.getInt("home-cooldown");

		enableAllies = config.getBoolean("enable-allies");
		logTeamChat = config.getBoolean("team-chat-logging");
		logAllyChat = config.getBoolean("team-ally-logging");
	}

	public boolean isBlacklistedCharacter(String c) {
		Pattern pattern = Pattern.compile("[~#@$*\\.+%{}<>\\[\\]|\"\\_^]");
		Matcher matcher = pattern.matcher(c);
		return matcher.find();
	}

	public boolean isBlacklistedName(String name) {
		return blacklistedNames.stream().filter(str -> str.contains(name.toLowerCase()) || name.contains(str)).findAny()
				.isPresent();
	}

	public boolean isNameAllowed(String name) {
		return name.length() < maxName && !isBlacklistedName(name.toLowerCase())
				&& !isBlacklistedCharacter(name.toLowerCase());
	}

	public boolean canAddMember(Team team) {
		return team.getTotalMembers() + 1 <= maxTeamSize;
	}

	public boolean canAlly() {
		return enableAllies;
	}

	public boolean logTeamChat() {
		return logTeamChat;
	}

	public boolean logAllyChat() {
		return logAllyChat;
	}

	public int getHomeCooldown() {
		return homeCooldown;
	}

}
