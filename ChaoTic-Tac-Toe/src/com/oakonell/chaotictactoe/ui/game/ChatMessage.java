package com.oakonell.chaotictactoe.ui.game;

import com.google.android.gms.games.multiplayer.Participant;

public class ChatMessage {
	public ChatMessage(Participant player, String string) {
		this.player = player;
		this.message = string;
	}

	private Participant player;
	private String message;

	public CharSequence getMessage() {
		return message;
	}

	public Participant getParticipant() {
		return player;
	}
}
