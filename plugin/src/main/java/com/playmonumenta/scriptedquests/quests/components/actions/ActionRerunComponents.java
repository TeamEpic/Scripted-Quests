package com.playmonumenta.scriptedquests.quests.components.actions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.playmonumenta.scriptedquests.Plugin;
import com.playmonumenta.scriptedquests.quests.components.QuestPrerequisites;

public class ActionRerunComponents implements ActionBase {
	private final String mNpcName;
	private final EntityType mEntityType;

	// Array list should be faster than hashmap for such small set
	private final List<Player> mLocked = new ArrayList<Player>(10);

	public ActionRerunComponents(String npcName, EntityType entityType) {
		mNpcName = npcName;
		mEntityType = entityType;
	}

	@Override
	public void doAction(Plugin plugin, Player player, Entity npcEntity, QuestPrerequisites prereqs) {
		/*
		 * Prevent infinite loops by preventing this specific action
		 * from running itself again
		 */
		if (!mLocked.contains(player)) {
			mLocked.add(player);
			System.out.println("rerun");
			plugin.mNpcManager.interactEvent(plugin, player, mNpcName, mEntityType, npcEntity, true);
			mLocked.remove(player);
		} else {
			plugin.getLogger().severe("Stopped infinite loop for NPC '" + mNpcName + "'");
		}
	}
}
