package com.playmonumenta.scriptedquests.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.playmonumenta.scriptedquests.Constants;
import com.playmonumenta.scriptedquests.Plugin;
import com.playmonumenta.scriptedquests.point.Point;
import com.playmonumenta.scriptedquests.quests.components.QuestActions;
import com.playmonumenta.scriptedquests.quests.components.QuestPrerequisites;

/*
 * A QuestLogin object holds all the quest components bound together with a particular
 * set of login rules (respawn location, etc.)
 */
public class QuestLogin {
	public class LoginActions {
		private final QuestActions mActions;
		private final QuestPrerequisites mPrerequisites;

		public LoginActions(QuestActions actions, QuestPrerequisites prerequisites) {
			mActions = actions;
			mPrerequisites = prerequisites;
		}

		public void doActions(Plugin plugin, Player player) {
			mActions.doActions(plugin, player, null, mPrerequisites);
		}
	}

	private QuestPrerequisites mPrerequisites = null;
	private QuestActions mActions = null;

	public QuestLogin(JsonObject object) throws Exception {
		Set<Entry<String, JsonElement>> entries = object.entrySet();
		for (Entry<String, JsonElement> ent : entries) {
			String key = ent.getKey();
			JsonElement value = ent.getValue();

			switch (key) {
			case "prerequisites":
				mPrerequisites = new QuestPrerequisites(value);
				break;
			case "actions":
				mActions = new QuestActions("", "", EntityType.VILLAGER, 0, value);
				break;
			default:
				throw new Exception("Unknown login quest key: '" + key + "'");
			}
		}
	}

	/* Returns true if prerequisites match and actions were taken, false otherwise */
	@SuppressWarnings("unchecked")
	public boolean loginEvent(Plugin plugin, PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (mPrerequisites == null || mPrerequisites.prerequisiteMet(player, null)) {
			mActions.doActions(plugin, player, null, mPrerequisites);
			return true;
		}
		return false;
	}
}
