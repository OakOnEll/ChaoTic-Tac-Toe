package com.oakonell.chaotictactoe.model;

import java.util.ArrayList;
import java.util.List;

import com.oakonell.chaotictactoe.ui.game.WinOverlayView.WinStyle;

public class State {
	public static class Win {
		private final Cell start;
		private final Cell end;
		private final WinStyle winStyle;

		public Win(Cell start, Cell end, WinStyle winStyle) {
			this.start = start;
			this.end = end;
			this.winStyle = winStyle;
		}

		
		public Cell getStart() {
			return start;
		}

		public Cell getEnd() {
			return end;
		}

		public WinStyle getWinStyle() {
			return winStyle;
		}



	}

	public enum SimpleState {
		WIN, DRAW, OPEN;
	}

	private final SimpleState state;
	private final Player winner;
	private final Cell lastMove; 
	private final Player player; 

	private List<Win> wins = new ArrayList<State.Win>();

	public static State winner(Player playerMoved,List<Win> wins, Player winner, Cell lastMove) {
		return new State(playerMoved, wins, winner, SimpleState.WIN, lastMove);
	}

	public static State draw(Player playerMoved, Cell lastMove) {
		return new State(playerMoved, null, null, SimpleState.DRAW, lastMove);
	}

	public static State open(Player playerMoved, Cell lastMove) {
		return new State(playerMoved,null, null, SimpleState.OPEN, lastMove);
	}
	
	private State(Player playerMoved, List<Win> wins, Player winner, SimpleState state, Cell lastMove) {
		this.lastMove = lastMove;
		this.winner = winner;
		this.state = state;

		this.wins = wins;
		this.player = playerMoved;
	}

	public Player getWinner() {
		if (state == SimpleState.WIN) {
			return winner;
		}
		return null;
	}

	public boolean isDraw() {
		return state == SimpleState.DRAW;
	}

	public List<Win> getWins() {
		return wins;
	}

	public boolean isOver() {
		return state != SimpleState.OPEN;
	}

	public Cell getLastMove() {
		return lastMove;
	}
	public Player getPlayer() {
		return player;
	}

}
