package com.oakonell.chaotictactoe.ui.game;

import com.google.android.gms.games.multiplayer.Participant;

public class ChatMessage {
	public ChatMessage(Participant player, String string, boolean isLocal) {
		this.player = player;
		this.message = string;
		this.isLocal = isLocal;
	}

	private Participant player;
	private boolean isLocal;
	private String message;

	public CharSequence getMessage() {
		return message;
	}

	public Participant getParticipant() {
		return player;
	}
	
	public boolean isLocal() {
		return isLocal;
	}
}
