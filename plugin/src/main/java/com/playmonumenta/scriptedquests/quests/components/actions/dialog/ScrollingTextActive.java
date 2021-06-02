package com.playmonumenta.scriptedquests.quests.components.actions.dialog;

import com.playmonumenta.scriptedquests.Plugin;
import com.playmonumenta.scriptedquests.point.AreaBounds;
import com.playmonumenta.scriptedquests.quests.components.QuestActions;
import com.playmonumenta.scriptedquests.quests.components.QuestPrerequisites;
import com.playmonumenta.scriptedquests.utils.MessagingUtils;
import me.Novalescent.Constants;
import me.Novalescent.mobs.npcs.RPGNPC;
import me.Novalescent.utils.FormattedMessage;
import me.Novalescent.utils.MessageFormat;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class ScrollingTextActive {

	private Plugin mPlugin;
	private int mIndex = -1;
	private List<String> mText;
	private Player mPlayer;
	private Entity mEntity;
	private QuestActions mActions;
	private QuestPrerequisites mPrerequisites;
	private final AreaBounds mValidArea;
	private boolean mRaw;

	public ScrollingTextActive(Plugin plugin, Player player, Entity npcEntity,
							   List<String> text, QuestActions actions, QuestPrerequisites prerequisites, AreaBounds validArea) {
		this(plugin, player, npcEntity, text, actions, prerequisites, validArea, false);
	}

	public ScrollingTextActive(Plugin plugin, Player player, Entity npcEntity,
							   List<String> text, QuestActions actions, QuestPrerequisites prerequisites, AreaBounds validArea, boolean raw) {
		mPlugin = plugin;
		mPlayer = player;
		mEntity = npcEntity;
		mText = text;
		mActions = actions;
		mPrerequisites = prerequisites;
		mValidArea = validArea;
		mRaw = raw;
	}

	public void next() {

		if (!mValidArea.within(mPlayer.getLocation())) {
			mPlayer.playSound(mPlayer.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 0.3f);
			FormattedMessage.sendMessage(mPlayer, MessageFormat.NOTICE, ChatColor.RED + "You moved too far away to hear the dialogue...");
			mPlayer.removeMetadata(com.playmonumenta.scriptedquests.Constants.PLAYER_SCROLLING_DIALOG_METAKEY, mPlugin);
			return;
		} else if (mPrerequisites != null && !mPrerequisites.prerequisiteMet(mPlayer, mEntity)) {
			mPlayer.playSound(mPlayer.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 0.3f);
			FormattedMessage.sendMessage(mPlayer, MessageFormat.NOTICE, ChatColor.RED + "You no longer meet the requirements to listen to this dialogue...");
			mPlayer.removeMetadata(com.playmonumenta.scriptedquests.Constants.PLAYER_SCROLLING_DIALOG_METAKEY, mPlugin);
			return;
		}

		String name = "";
		if (mEntity != null && mEntity.hasMetadata(Constants.NPC_METAKEY)) {
			RPGNPC npc = (RPGNPC) mEntity.getMetadata(Constants.NPC_METAKEY).get(0).value();
			name = ChatColor.stripColor(npc.mNameStand.getCustomName());
		}
		mIndex++;
		if (mIndex < mText.size()) {
			String text = mText.get(mIndex);
			if (!text.trim().isEmpty()) {
				if (!mRaw) {
					MessagingUtils.sendScrollableRawMessage(mPlayer, text);
				} else {
					MessagingUtils.sendScrollableNPCMessage(mPlayer, name, text);
				}
			} else {
				next();
				return;
			}
		} else {
			mActions.doActions(mPlugin, mPlayer, mEntity, mPrerequisites);
			mPlayer.removeMetadata(com.playmonumenta.scriptedquests.Constants.PLAYER_SCROLLING_DIALOG_METAKEY, mPlugin);
			return;
		}

	}

}

