package com.oakonell.chaotictactoe.model;

import com.oakonell.chaotictactoe.ui.game.WinOverlayView.WinStyle;

public class State {
	private enum SimpleState {
		WIN, DRAW, OPEN;
	}

	private final SimpleState state;
	private final Marker winner;

	private final Cell start;
	private final Cell end;

	private final int score;
	private final WinStyle winStyle;

	public static State winner(Cell start, Cell end, Marker winner, int score,
			WinStyle winStyle) {
		return new State(start, end, winner, score, SimpleState.WIN, winStyle);
	}

	public static State draw(Marker player) {
		return new State(null, null, null, 0, SimpleState.DRAW, null);
	}

	public static State open(Cell start, Cell end, Marker player, int score) {
		return new State(start, end, null, score, SimpleState.OPEN, null);
	}

	public State(Cell start, Cell end, Marker winner, int score,
			SimpleState state, WinStyle winStyle) {
		this.winner = winner;
		this.state = state;
		this.start = start;
		this.end = end;
		this.score = score;
		this.winStyle = winStyle;
	}

	public int getScore() {
		return score;
	}

	public Marker getWinner() {
		if (state == SimpleState.WIN) {
			return winner;
		}
		return null;
	}

	public boolean isDraw() {
		return state == SimpleState.DRAW;
	}

	public Cell getStart() {
		return start;
	}

	public Cell getEnd() {
		return end;
	}

	public boolean isOver() {
		return state != SimpleState.OPEN;
	}

	public WinStyle getWinStyle() {
		return winStyle;
	}

}
