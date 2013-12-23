package com.oakonell.chaotictactoe.model;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.google.android.gms.games.multiplayer.Participant;
import com.oakonell.chaotictactoe.GameMode;

public class Game {
	private int moves;
	private final MarkerChance markerChance;
	private final Board board;

	private Player firstPlayer;
	private Player secondPlayer;
	private GameMode mode;

	private Player player;
	private Marker toPlay;
	private Map<Long, Integer> numVisitsPerState = new HashMap<Long, Integer>();

	public Game(int size, GameMode mode, Player firstPlayer,
			Player secondPlayer, MarkerChance chance) {
		board = new Board(size);
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
		firstPlayer.setOpponent(secondPlayer);
		secondPlayer.setOpponent(firstPlayer);

		player = firstPlayer;
		markerChance = chance;
		toPlay = pickMarkerToToplay();
		this.mode = mode;
	}

	public MarkerChance getMarkerChance() {
		return markerChance;
	}

	public State placeMarker(Cell cell) {
		moves++;
		State outcome;
		if (toPlay == Marker.EMPTY) {
			outcome = board.removeMarker(cell, player);
		} else {
			outcome = board.placeMarker(cell, player, toPlay);
		}

		// switch to next player
		player = player.opponent();
		// if (player == Marker.O) {
		// player = Marker.X;
		// } else {
		// player = Marker.O;
		// }

		// pick the next marker to play
		toPlay = pickMarkerToToplay();

		recordVisitToState();

		return outcome;
	}

	private void recordVisitToState() {
		long state = board.getBoardStateAsLong();
		Integer number = numVisitsPerState.get(state);
		if (number == null) {
			number = 0;
		}
		numVisitsPerState.put(state, number + 1);
		Log.i("Game", "Board state " + state);
	}

	private Marker pickMarkerToToplay() {
		return markerChance.pickMove(board, player);
	}

	public Marker getMarkerToPlay() {
		return toPlay;
	}

	public Player getCurrentPlayer() {
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
		if (integer == null)
			return 0;
		return integer;
	}

	public Board getBoard() {
		return board;
	}

	public GameMode getMode() {
		return mode;
	}

	public Player getLocalPlayer() {
		if (getMode() == GameMode.PASS_N_PLAY) {
			return null;
		}
		if (firstPlayer.getStrategy().isHuman()) {
			return firstPlayer;
		}
		return secondPlayer;
	}

	public Player getNonLocalPlayer() {
		if (getMode() == GameMode.PASS_N_PLAY) {
			return null;
		}
		if (firstPlayer.getStrategy().isHuman()) {
			return secondPlayer;
		}
		return firstPlayer;
	}

	public Player getXPlayer() {
		if (firstPlayer.getMarker() == Marker.X) return firstPlayer;
		return secondPlayer;
	}
	public Player getOPlayer() {
		if (firstPlayer.getMarker() == Marker.O) return firstPlayer;
		return secondPlayer;
	}

}
