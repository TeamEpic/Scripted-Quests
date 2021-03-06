package com.playmonumenta.scriptedquests.quests.components.actions;

import java.lang.reflect.Method;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.playmonumenta.scriptedquests.Plugin;
import com.playmonumenta.scriptedquests.quests.components.QuestPrerequisites;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ActionCommand implements ActionBase {
	private String mCommand;

	public ActionCommand(JsonElement element) throws Exception {
		mCommand = element.getAsString();
		if (mCommand == null) {
			throw new Exception("Command value is not a string!");
		}

		//Need to make sure command does not have the / at the start - this breaks the Parser
		if (mCommand.charAt(0) == '/') {
			mCommand = mCommand.substring(1);
		}

		ParseResults<?> pr = null;
		try {
			Object server = Bukkit.getServer().getClass().getDeclaredMethod("getServer").invoke(Bukkit.getServer());
			String packageName = server.getClass().getPackage().getName();

			Class<?> minecraftServerClass = Class.forName(packageName + "." + "MinecraftServer");
			Object minecraftServer = minecraftServerClass.getDeclaredMethod("getServer").invoke(null);
			Object clw = minecraftServerClass.getDeclaredMethod("getServerCommandListener").invoke(minecraftServer);

			Object commandDispatcher = minecraftServerClass.getDeclaredMethod("getCommandDispatcher").invoke(minecraftServer);
			Class<?> commandDispatcherClass = Class.forName(packageName + "." + "CommandDispatcher");
			Object brigadierCmdDispatcher = commandDispatcherClass.getDeclaredMethod("a").invoke(commandDispatcher);

			String testCommandStr = mCommand.replaceAll("@S", "testuser").replaceAll("@N", "testnpc").replaceAll("@U", UUID.randomUUID().toString().toLowerCase());
			Method parse = CommandDispatcher.class.getDeclaredMethod("parse", String.class, Object.class);
			pr = (ParseResults<?>) parse.invoke(brigadierCmdDispatcher, testCommandStr, clw);
		} catch (Exception e) {
			// Failed to test the command - ignore it and print a log message
			e.printStackTrace();

			pr = null;
		}

		if (pr != null && pr.getReader().canRead()) {
			throw new Exception("Invalid command: '" + mCommand + "'");
		}
	}

	@Override
	public void doAction(Plugin plugin, Player player, Entity npcEntity, QuestPrerequisites prereqs) {
		// Because there's no currently good way to run commands we need to run them via the console....janky....I know.
		String commandStr = mCommand;
		if (npcEntity == null) {
			if (commandStr.contains("@N")) {
				String commandErr = ChatColor.RED + "Cannot run ScriptedQuest command without direct NPC interaction: /" + commandStr;
				player.spigot().sendMessage(ChatMessageType.SYSTEM, TextComponent.fromLegacyText(commandErr));
				return;
			}
		} else {
			commandStr = commandStr.replaceAll("@N", npcEntity.getUniqueId().toString());
		}
		commandStr = commandStr.replaceAll("@S", player.getName()).replaceAll("@U", player.getUniqueId().toString().toLowerCase());
		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), commandStr);
	}
}
