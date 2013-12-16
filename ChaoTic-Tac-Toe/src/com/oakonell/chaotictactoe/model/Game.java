package com.oakonell.chaotictactoe.model;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class Game {
	private int moves;
	private final MarkerChance markerChance;
	private final Board board;

	private Marker player;
	private Marker toPlay;
	private Map<Long, Integer> numVisitsPerState = new HashMap<Long, Integer>(); 

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
		moves++;
		State outcome;
		if (toPlay == Marker.EMPTY) {
			outcome = board.removeMarker(cell, toPlay);
		} else {
			outcome = board.placeMarker(cell, toPlay);
		}

		// switch to next player
		if (player == Marker.O) {
			player = Marker.X;
		} else {
			player = Marker.O;
		}

		// pick the next marker to play
		toPlay = pickMarkerToToplay();
		
		recordVisitToState();
		
		return outcome;
	}

	private void recordVisitToState() {
		long state = board.getBoardStateAsLong();
		Integer number = numVisitsPerState.get(state);
		if (number == null) {
			number =0;
		}
		numVisitsPerState.put(state, number+1);
		Log.i("Game", "Board state " + state);
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

	public int getNumberOfMoves() {
		return moves;
	}
	
	public int getNumberOfTimesInThisState() {
		long state = board.getBoardStateAsLong();
		Integer integer = numVisitsPerState.get(state);
		if (integer == null) return 0;
		return integer;
	}
	
	public Board getBoard() {
		return board;
	}
}
