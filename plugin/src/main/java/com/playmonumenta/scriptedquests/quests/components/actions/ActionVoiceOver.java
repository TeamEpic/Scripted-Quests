package com.playmonumenta.scriptedquests.quests;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import org.bukkit.Location;
import org.bukkit.SoundCategory;

import com.google.gson.JsonElement;

import com.playmonumenta.scriptedquests.Plugin;

class ActionVoiceOver implements ActionBase {
	private String mSound;

	ActionVoiceOver(JsonElement element) throws Exception {
		mSound = element.getAsString();
		if (mSound == null) {
			throw new Exception("Voice Over value is not a string!");
		}
	}

	@Override
	public void doAction(Plugin plugin, Player player, QuestPrerequisites prereqs) {
		Entity entity = (Entity)player; // TODO Update this to the NPC
		Location location = entity.getLocation().add(0,entity.getHeight(),0);
		float volume = 1.0f;
		float pitch = 1.0f;
		String lastSound = "";
		player.stopSound(lastSound,SoundCategory.VOICE);
		player.playSound(location, mSound, SoundCategory.VOICE, volume, pitch);
	}
}
