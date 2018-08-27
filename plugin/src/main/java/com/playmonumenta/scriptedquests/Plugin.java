package com.playmonumenta.scriptedquests;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.World;

import com.playmonumenta.scriptedquests.commands.GiveLootTable;
import com.playmonumenta.scriptedquests.commands.InteractNpc;
import com.playmonumenta.scriptedquests.commands.QuestTrigger;
import com.playmonumenta.scriptedquests.commands.ReloadQuests;
import com.playmonumenta.scriptedquests.listeners.EntityListener;
import com.playmonumenta.scriptedquests.listeners.PlayerListener;
import com.playmonumenta.scriptedquests.managers.QuestCompassManager;
import com.playmonumenta.scriptedquests.managers.QuestDeathManager;
import com.playmonumenta.scriptedquests.managers.QuestNpcManager;
import com.playmonumenta.scriptedquests.utils.MetadataUtils;

public class Plugin extends JavaPlugin {
	public QuestCompassManager mQuestCompassManager;
	public QuestNpcManager mNpcManager;
	public QuestDeathManager mDeathManager;

	public World mWorld;

	//	Logic that is performed upon enabling the plugin.
	@Override
	public void onEnable() {
		PluginManager manager = getServer().getPluginManager();

		mWorld = Bukkit.getWorlds().get(0);

		manager.registerEvents(new EntityListener(this), this);
		manager.registerEvents(new PlayerListener(this), this);

		getCommand("reloadQuests").setExecutor(new ReloadQuests(this));
		getCommand("questTrigger").setExecutor(new QuestTrigger(this));
		getCommand("interactNpc").setExecutor(new InteractNpc(this));
		getCommand("giveLootTable").setExecutor(new GiveLootTable(this));

		mQuestCompassManager = new QuestCompassManager(this);
		mNpcManager = new QuestNpcManager(this);
		mDeathManager = new QuestDeathManager(this);
	}

	//	Logic that is performed upon disabling the plugin.
	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);

		MetadataUtils.removeAllMetadata(this);
	}
}
