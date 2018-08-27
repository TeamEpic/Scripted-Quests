package com.playmonumenta.scriptedquests.quests;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.entity.Player;

import com.google.gson.JsonElement;

import com.playmonumenta.scriptedquests.Plugin;
import com.playmonumenta.scriptedquests.utils.MessagingUtils;

class DialogText implements DialogBase {
	private String mDisplayName;
	private ArrayList<String> mText = new ArrayList<String>();

	DialogText(String displayName, JsonElement element) throws Exception {
		mDisplayName = displayName;

		if (element.isJsonPrimitive()) {
			mText.add(element.getAsString());
		} else if (element.isJsonArray()) {
			Iterator<JsonElement> iter = element.getAsJsonArray().iterator();
			while (iter.hasNext()) {
				mText.add(iter.next().getAsString());
			}
		} else {
			throw new Exception("text value is neither an array nor a string!");
		}
	}

	@Override
	public void sendDialog(Plugin plugin, Player player, QuestPrerequisites prereqs) {
		for (String text : mText) {
			MessagingUtils.sendNPCMessage(player, mDisplayName, text);
		}
	}
}
