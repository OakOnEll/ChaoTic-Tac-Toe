package com.oakonell.chaotictactoe;

import com.oakonell.chaotictactoe.model.Board;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Marker;

public class PlayerStrategy {
	private String name;
	private Marker marker;

	protected PlayerStrategy(String name, Marker marker) {
		this.name = name;
		this.marker = marker;
	}

	public boolean isHuman() {
		return false;
	}

	public String getName() {
		return name;
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
