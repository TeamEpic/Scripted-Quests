package com.playmonumenta.scriptedquests.commands;

import java.util.Collection;
import java.util.UUID;
import java.util.regex.Pattern;

import com.playmonumenta.scriptedquests.Plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.EntityTypeArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public class InteractNpc {
	static final Pattern uuidRegex = Pattern.compile("\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}");

	@SuppressWarnings("unchecked")
	public static void register(Plugin plugin) {
		/* First one of these has both required arguments */
		new CommandAPICommand("interactnpc")
			.withPermission(CommandPermission.fromString("scriptedquests.interactnpc"))
			.withArguments(new EntitySelectorArgument("players", EntitySelectorArgument.EntitySelector.MANY_PLAYERS))
			.withArguments(new StringArgument("npcName"))
			.withArguments(new EntityTypeArgument("npcType"))
			.executes((sender, args) -> {
				interact(plugin, sender, (Collection<Player>)args[0],
					(String)args[1], (EntityType)args[2]);
			})
			.register();

		/* Second one accepts a single NPC entity, and goes earlier to take priority over entity names */
		new CommandAPICommand("interactnpc")
			.withPermission(CommandPermission.fromString("scriptedquests.interactnpc"))
			.withArguments(new EntitySelectorArgument("players", EntitySelectorArgument.EntitySelector.MANY_PLAYERS))
			.withArguments(new EntitySelectorArgument("npc", EntitySelectorArgument.EntitySelector.ONE_ENTITY))
			.executes((sender, args) -> {
				interact(plugin, sender, (Collection<Player>)args[0],
					(Entity)args[1]);
			})
			.register();

		/* Third one just has the npc name with VILLAGER as default */
		new CommandAPICommand("interactnpc")
			.withPermission(CommandPermission.fromString("scriptedquests.interactnpc"))
			.withArguments(new EntitySelectorArgument("players", EntitySelectorArgument.EntitySelector.MANY_PLAYERS))
			.withArguments(new StringArgument("npcName"))
			.executes((sender, args) -> {
				interact(plugin, sender, (Collection<Player>)args[0],
					(String)args[1], EntityType.VILLAGER);
			})
			.register();
	}

	private static void interact(Plugin plugin, CommandSender sender, Collection<Player> players,
	                             String npcName, EntityType npcType) {
		if (uuidRegex.matcher(npcName).matches()) {
			UUID npcUuid = UUID.fromString(npcName);
			Entity npc = Bukkit.getEntity(npcUuid);
			if (npc == null) {
				sender.sendMessage(ChatColor.RED + "No NPC with UUID '" + npcName + "'");
			} else {
				interact(plugin, sender, players, npc);
			}
			return;
		}

		if (plugin.mNpcManager != null) {
			for (Player player : players) {
				if (!plugin.mNpcManager.interactEvent(plugin, player, npcName, npcType, null, true)) {
					sender.sendMessage(ChatColor.RED + "No interaction available for player '" + player.getName() +
					                   "' and NPC '" + npcName + "'");
				}
			}
		}
	}

	private static void interact(Plugin plugin, CommandSender sender, Collection<Player> players,
	                             Entity npc) {
		if (plugin.mNpcManager != null) {
			for (Player player : players) {
				if (!plugin.mNpcManager.interactEvent(plugin, player, npc.getCustomName(), npc.getType(), npc, false)) {
					sender.sendMessage(ChatColor.RED + "No interaction available for player '" + player.getName() +
					                   "' and NPC '" + npc.getCustomName() + "'");
				}
			}
		}
	}
}
