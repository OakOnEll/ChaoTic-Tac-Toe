package com.oakonell.chaotictactoe.model;

public class Game {
	private final MarkerChance markerChance;
	private final Board board;

	private Marker player;
	private Marker toPlay;

	public Game(int size, Marker firstPlayer, MarkerChance chance) {
		board = new Board(size);
		player = firstPlayer;
		markerChance = chance;
		toPlay = pickMarkerToToplay();
	}

	public MarkerChance getMarkerChance() {
		return markerChance;
	}

	public State placeMarker(Cell cell) {
		State outcome;
		if (toPlay == Marker.EMPTY) {
			outcome = board.removeMarker(cell, toPlay);
		} else {
			outcome = board.placeMarker(cell, toPlay);
		}

		if (player == Marker.O) {
			player = Marker.X;
		} else {
			player = Marker.O;
		}

		toPlay = pickMarkerToToplay();
		return outcome;
	}

	private Marker pickMarkerToToplay() {
		return markerChance.pickMove(board, player);
	}

	public Marker getMarkerToPlay() {
		return toPlay;
	}

	public Marker getCurrentPlayer() {
		return player;
	}

	// private PlayerStrategy xPlayerStrategy;
	// private PlayerStrategy oPlayerStrategy;
	// public void setxPlayerStrategy(PlayerStrategy xPlayerStrategy) {
	// this.xPlayerStrategy = xPlayerStrategy;
	// }
	//
	// public void setoPlayerStrategy(PlayerStrategy oPlayerStrategy) {
	// this.oPlayerStrategy = oPlayerStrategy;
	// }

}
