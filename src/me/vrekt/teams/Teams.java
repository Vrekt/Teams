package me.vrekt.teams;

import java.io.File;
import java.io.IOException;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.vrekt.teams.admin.TeamAdminHandler;
import me.vrekt.teams.admin.TeamBanHandler;
import me.vrekt.teams.commands.TeamAdminCommand;
import me.vrekt.teams.commands.TeamCommand;
import me.vrekt.teams.configuration.Configuration;
import me.vrekt.teams.handler.TeamsHandler;
import me.vrekt.teams.invite.InviteActionHandler;
import me.vrekt.teams.listener.PlayerListener;

public class Teams extends JavaPlugin {

	private static Plugin instance;

	private static TeamAdminHandler adminHandler;
	private static InviteActionHandler handler;
	private static TeamBanHandler banHandler;
	private static Configuration config;
	private static TeamsHandler manager;

	private static long beginLoad;
	private long beginSave;

	public void onEnable() {
		instance = this;

		getLogger().info("Teams starting up... Creating files and writing configuration values.");

		File teams = new File(getDataFolder() + "/teams.yml");
		File cfg = new File(getDataFolder() + "/config.yml");

		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}

		if (!cfg.exists()) {
			saveDefaultConfig();
		}

		if (!teams.exists()) {
			try {
				teams.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		config = new Configuration(getConfig());
		manager = new TeamsHandler();
		handler = new InviteActionHandler();
		adminHandler = new TeamAdminHandler();
		banHandler = new TeamBanHandler();

		beginLoad = System.currentTimeMillis();

		getLogger().info("Loading all teams asynchronously, this could take awhile.");

		new BukkitRunnable() {

			@Override
			public void run() {
				manager.writeTeams();
			}
		}.runTaskAsynchronously(this);

		long time = System.currentTimeMillis() - beginLoad;
		beginLoad = time;
		getLogger().info("Finished loading all teams, took (" + time + ")ms");

		getLogger().info("Registering commands and listeners..");

		getCommand("team").setExecutor(new TeamCommand());
		getCommand("ta").setExecutor(new TeamAdminCommand());
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
	}

	public void onDisable() {

		beginSave = System.currentTimeMillis();

		getLogger().info("Attempting to save all teams (SYNC!!), this could take awhile.");

		manager.saveTeams();

		long time = System.currentTimeMillis() - beginSave;
		getLogger().info("Finished saving all teams, took (" + time + ")ms");

		getLogger().info("Destroying existing instances and cleaning up...");

		instance = null;
		config = null;
		manager = null;

	}

	public static Plugin getInstance() {
		return instance;
	}

	public static Configuration getConfiguration() {
		return config;
	}

	public static TeamsHandler getTeamsHandler() {
		return manager;
	}

	public static InviteActionHandler getInviteHandler() {
		return handler;
	}

	public static TeamAdminHandler getAdminHandler() {
		return adminHandler;
	}

	public static TeamBanHandler getBanHandler() {
		return banHandler;
	}

	public static long getLoadTime() {
		return beginLoad;
	}

}
