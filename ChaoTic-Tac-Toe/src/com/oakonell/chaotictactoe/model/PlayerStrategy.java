package com.oakonell.chaotictactoe.model;


public class PlayerStrategy {
	private final Marker marker;

	protected PlayerStrategy(Marker marker) {
		this.marker = marker;
	}

	public boolean isHuman() {
		return false;
	}


	public boolean isAI() {
		return false;
	}

	public Marker getMarker() {
		return marker;
	}

	public Cell move(Board board, Marker toPlay) {
		return null;
	}


}
