package com.playmonumenta.scriptedquests.trades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.scriptedquests.Plugin;
import com.playmonumenta.scriptedquests.quests.QuestNpc;

/*
 * An NpcTrader object holds prerequisites for each trade slot in the NPC's inventory
 * Only one NpcTrader object exists per NPC name
 */
public class NpcTrader {
	private final ArrayList<NpcTrade> mTrades = new ArrayList<NpcTrade>();
	private final String mNpcName;

	public NpcTrader(JsonObject object) throws Exception {
		// Read the npc's name first
		JsonElement npc = object.get("npc");
		if (npc == null) {
			throw new Exception("'npc' entry is required");
		}
		if (npc.getAsString() == null || QuestNpc.squashNpcName(npc.getAsString()).isEmpty()) {
			throw new Exception("Failed to parse 'npc' name as string");
		}
		mNpcName = QuestNpc.squashNpcName(npc.getAsString());

		// Read the npc's trades
		JsonArray array = object.getAsJsonArray("trades");
		if (array == null) {
			throw new Exception("Failed to parse 'trades' as JSON array");
		}
		Iterator<JsonElement> iter = array.iterator();
		while (iter.hasNext()) {
			JsonElement entry = iter.next();

			mTrades.add(new NpcTrade(entry));
		}

		// Iterate through the remaining keys and throw an error if any are found
		Set<Entry<String, JsonElement>> entries = object.entrySet();
		for (Entry<String, JsonElement> ent : entries) {
			String key = ent.getKey();

			if (!key.equals("npc") && !key.equals("trades")) {
				throw new Exception("Unknown NpcTrader key: " + key);
			}
		}

		// Sort the list of trades in reverse / descending order
		Collections.sort(mTrades, Collections.reverseOrder());

		// Step through the list - if you ever find the same index twice in a row, throw an error
		int lastIndex = -1;
		for (NpcTrade trade : mTrades) {
			if (trade.getIndex() == lastIndex) {
				throw new Exception("Trader '" + mNpcName + "' specifies index " + Integer.toString(lastIndex) + " more than once");
			}
			lastIndex = trade.getIndex();
		}
	}

	/*
	 * NOTE: This is always the squashed/stripped version of the name!
	 */
	public String getNpcName() {
		return mNpcName;
	}

	public List<MerchantRecipe> getPlayerTrades(Plugin plugin, Villager villager, Player player) {
		// Copy the current trades
		List<MerchantRecipe> modifiedRecipes = new ArrayList<MerchantRecipe>(villager.getRecipes());

		// Remove unmatched prereq trades
		boolean modified = false;
		String modifiedSlots = null;
		for (NpcTrade trade : mTrades) {
			if (!trade.prerequisiteMet(player, villager)) {
				if (modifiedRecipes.size() <= trade.getIndex()) {
					player.sendMessage(ChatColor.RED + "BUG! This NPC has too few trades for some reason. Please report this!");
				} else {
					modifiedRecipes.remove(trade.getIndex());
					if (modifiedSlots == null) {
						modifiedSlots = Integer.toString(trade.getIndex());
					} else {
						modifiedSlots += ", " + Integer.toString(trade.getIndex());
					}

					modified = true;
				}
			}
		}

		if (modified && player.getGameMode() == GameMode.CREATIVE && player.isOp()) {
			player.sendMessage(ChatColor.GOLD + "These trader slots were not shown to you: " + modifiedSlots);
			player.sendMessage(ChatColor.GOLD + "This message only appears to operators in creative mode");
		}

		return modifiedRecipes;
	}
}
