package com.playmonumenta.scriptedquests.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.playmonumenta.scriptedquests.utils.InventoryUtils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;

public class GiveItemWithLore {
	@SuppressWarnings("unchecked")
	public static void register() {
		CommandPermission perms = CommandPermission.fromString("scriptedquests.giveitemwithlore");
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

		arguments.put("players", new EntitySelectorArgument(EntitySelectorArgument.EntitySelector.MANY_PLAYERS));
		arguments.put("item", new ItemStackArgument());
		arguments.put("count", new IntegerArgument(1, 64));
		arguments.put("lore", new TextArgument());

		new CommandAPICommand("giveitemwithlore")
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				giveLoot((Collection<Player>)args[0], (ItemStack)args[1], (Integer)args[2], (String)args[3]);
			})
			.register();
	}

	private static void giveLoot(Collection<Player> players, ItemStack item, int count, String lore) throws WrapperCommandSyntaxException {
		item.setAmount(count);
		final List<String> itemLore;
		ItemMeta meta = item.getItemMeta();

		if (meta == null) {
			CommandAPI.fail("Item missing meta, can not add lore");
		}
		if (meta != null && meta.hasLore()) {
			itemLore = meta.getLore();
		} else {
			itemLore = new ArrayList<String>();
		}

		for (String addLore : lore.split("\\\\n")) {
			itemLore.add(ChatColor.translateAlternateColorCodes('&', addLore));
		}

		meta.setLore(itemLore);
		item.setItemMeta(meta);

		List<ItemStack> items = new ArrayList<ItemStack>(1);
		items.add(item);

		for (Player player : players) {
			InventoryUtils.giveItems(player, items, false);
		}
	}
}
