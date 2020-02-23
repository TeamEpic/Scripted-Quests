package com.playmonumenta.scriptedquests;

import java.io.File;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.playmonumenta.scriptedquests.commands.Clickable;
import com.playmonumenta.scriptedquests.commands.Code;
import com.playmonumenta.scriptedquests.commands.DebugZones;
import com.playmonumenta.scriptedquests.commands.GenerateCode;
import com.playmonumenta.scriptedquests.commands.GiveLootTable;
import com.playmonumenta.scriptedquests.commands.HasPermission;
import com.playmonumenta.scriptedquests.commands.InteractNpc;
import com.playmonumenta.scriptedquests.commands.Leaderboard;
import com.playmonumenta.scriptedquests.commands.QuestTrigger;
import com.playmonumenta.scriptedquests.commands.Race;
import com.playmonumenta.scriptedquests.commands.RandomNumber;
import com.playmonumenta.scriptedquests.commands.ReloadQuests;
import com.playmonumenta.scriptedquests.commands.ScheduleFunction;
import com.playmonumenta.scriptedquests.commands.SetVelocity;
import com.playmonumenta.scriptedquests.commands.TimerDebug;
import com.playmonumenta.scriptedquests.listeners.EntityListener;
import com.playmonumenta.scriptedquests.listeners.PlayerListener;
import com.playmonumenta.scriptedquests.managers.ClickableManager;
import com.playmonumenta.scriptedquests.managers.CodeManager;
import com.playmonumenta.scriptedquests.managers.InteractableManager;
import com.playmonumenta.scriptedquests.managers.NpcTradeManager;
import com.playmonumenta.scriptedquests.managers.QuestCompassManager;
import com.playmonumenta.scriptedquests.managers.QuestDeathManager;
import com.playmonumenta.scriptedquests.managers.QuestLoginManager;
import com.playmonumenta.scriptedquests.managers.QuestNpcManager;
import com.playmonumenta.scriptedquests.managers.RaceManager;
import com.playmonumenta.scriptedquests.managers.ZoneManager;
import com.playmonumenta.scriptedquests.managers.ZonePropertyManager;
import com.playmonumenta.scriptedquests.timers.CommandTimerManager;
import com.playmonumenta.scriptedquests.utils.MetadataUtils;

public class Plugin extends JavaPlugin {
	private static Plugin INSTANCE = null;

	private FileConfiguration mConfig;
	private File mConfigFile;
	public Boolean mShowTimerNames = null;
	public boolean mShowZonesDynmap = false;

	public QuestCompassManager mQuestCompassManager;
	public QuestNpcManager mNpcManager;
	public ClickableManager mClickableManager;
	public InteractableManager mInteractableManager;
	public QuestLoginManager mLoginManager;
	public QuestDeathManager mDeathManager;
	public RaceManager mRaceManager;
	public NpcTradeManager mTradeManager;
	public CommandTimerManager mTimerManager;
	public CodeManager mCodeManager;
	public ZoneManager mZoneManager;
	public ZonePropertyManager mZonePropertyManager;

	public World mWorld;
	public Random mRandom = new Random();
	private ScheduleFunction mScheduledFunctionsManager;

	@Override
	public void onLoad() {
		/*
		 * CommandAPI commands which register directly and are usable in functions
		 *
		 * These need to register immediately on load to prevent function loading errors
		 */

		InteractNpc.register(this);
		Clickable.register(this);
		GiveLootTable.register(mRandom);
		Race.register(this);
		Leaderboard.register();
		RandomNumber.register();
		HasPermission.register();
		TimerDebug.register(this);
		GenerateCode.register(this);
		Code.register(this);
		SetVelocity.register();
		DebugZones.register(this);

		mScheduledFunctionsManager = new ScheduleFunction(this);
	}

	@Override
	public void onEnable() {
		INSTANCE = this;

		reloadConfigYaml(null);

		PluginManager manager = getServer().getPluginManager();

		mWorld = Bukkit.getWorlds().get(0);

		mQuestCompassManager = new QuestCompassManager();
		mNpcManager = new QuestNpcManager(this);
		mClickableManager = new ClickableManager();
		mInteractableManager = new InteractableManager();
		mTradeManager = new NpcTradeManager();
		mLoginManager = new QuestLoginManager();
		mDeathManager = new QuestDeathManager();
		mRaceManager = new RaceManager();
		mCodeManager = new CodeManager();
		mZoneManager = new ZoneManager(this);
		mZonePropertyManager = new ZonePropertyManager(this);

		mTimerManager = new CommandTimerManager(this);

		manager.registerEvents(new EntityListener(this), this);
		manager.registerEvents(new PlayerListener(this), this);
		manager.registerEvents(mTimerManager, this);
		manager.registerEvents(mZonePropertyManager, this);

		getCommand("reloadQuests").setExecutor(new ReloadQuests(this));
		getCommand("questTrigger").setExecutor(new QuestTrigger(this));

		/* Load the config 1 tick later to let other plugins load */
		new BukkitRunnable() {
			@Override
			public void run() {
				reloadConfig(null);
			}
		}.runTaskLater(this, 1);
	}

	@Override
	public void onDisable() {
		INSTANCE = null;

		getServer().getScheduler().cancelTasks(this);

		mRaceManager.cancelAllRaces();
		mTimerManager.unloadAll();

		MetadataUtils.removeAllMetadata(this);

		// Run all pending delayed commands
		mScheduledFunctionsManager.cancel();
		mScheduledFunctionsManager = null;
	}

	/* Sender will be sent debugging info if non-null */
	public void reloadConfig(CommandSender sender) {
		reloadConfigYaml(sender);
		mNpcManager.reload(this, sender);
		mClickableManager.reload(this, sender);
		mInteractableManager.reload(this, sender);
		mTradeManager.reload(this, sender);
		mQuestCompassManager.reload(this, sender);
		mLoginManager.reload(this, sender);
		mDeathManager.reload(this, sender);
		mRaceManager.reload(this, sender);
		mCodeManager.reload(this, sender);
		mZonePropertyManager.reload(this, sender);
		mZoneManager.reload(this, sender);
	}

	private void reloadConfigYaml(CommandSender sender) {
		if (mConfigFile == null) {
			mConfigFile = new File(getDataFolder(), "config.yml");
		}

		mConfig = YamlConfiguration.loadConfiguration(mConfigFile);

		if (mConfig.isBoolean("show_timer_names")) {
			mShowTimerNames = mConfig.getBoolean("show_timer_names", false);
			if (sender != null) {
				sender.sendMessage("show_timer_names: " + mShowTimerNames.toString());
			}
		} else {
			mShowTimerNames = null;
			if (sender != null) {
				sender.sendMessage("show_timer_names: null / not automatically changed");
			}
		}

		if (mConfig.isBoolean("show_zones_dynmap")) {
			mShowZonesDynmap = mConfig.getBoolean("show_timer_names", false);
		} else {
			mShowZonesDynmap = false;
		}
		if (sender != null) {
			sender.sendMessage("show_zones_dynmap: " + Boolean.toString(mShowZonesDynmap));
		}
	}

	public static Plugin getInstance() {
		return INSTANCE;
	}
}
