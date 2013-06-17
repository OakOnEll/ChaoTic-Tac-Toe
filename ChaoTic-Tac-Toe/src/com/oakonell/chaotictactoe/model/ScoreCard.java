package com.oakonell.chaotictactoe.model;

import android.content.Intent;

public class ScoreCard {
	public static final String X_WINS_KEY = "X-wins";
	public static final String O_WINS_KEY = "O-wins";
	public static final String DRAWS_KEY = "draws";

	private int xWins;
	private int oWins;
	private int draws;

	public ScoreCard(int xwins, int owins, int draws) {
		this.xWins = xwins;
		this.oWins = owins;
		this.draws = draws;
	}

	public int getXWins() {
		return xWins;
	}

	public void setXWins(int xWins) {
		this.xWins = xWins;
	}

	public int getOWins() {
		return oWins;
	}

	public void setOWins(int oWins) {
		this.oWins = oWins;
	}

	public int getDraws() {
		return draws;
	}

	public void setDraws(int draws) {
		this.draws = draws;
	}

	public static ScoreCard fromIntentExtras(Intent intent) {
		int xwins = intent.getIntExtra(X_WINS_KEY, 0);
		int owins = intent.getIntExtra(O_WINS_KEY, 0);
		int draws = intent.getIntExtra(DRAWS_KEY, 0);
		return new ScoreCard(xwins, owins, draws);
	}

	public void incrementScore(Marker winner) {
		if (winner == null) {
			draws++;
		} else if (winner == Marker.X) {
			xWins++;
		} else if (winner == Marker.O) {
			oWins++;
		}
	}

}
