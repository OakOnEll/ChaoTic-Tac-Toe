package com.oakonell.chaotictactoe.model;

public class Move {
	private final Player player;
	private final Cell cell;
	private final Marker previousMarker;
	private final Marker playedMarker;

	public Move(Player player, Marker playedMarker, Cell cell,
			Marker previousMarker) {
		this.player = player;
		this.cell = cell;
		this.previousMarker = previousMarker;
		this.playedMarker = playedMarker;
	}

	public Player getPlayer() {
		return player;
	}

	public Cell getCell() {
		return cell;
	}

	public Marker getPreviousMarker() {
		return previousMarker;
	}

	public Marker getPlayedMarker() {
		return playedMarker;
	}

}
