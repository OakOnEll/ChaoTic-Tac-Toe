package com.oakonell.chaotictactoe.model;


public class ScoreCard {
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

	public void incrementScore(Marker winner) {
		if (winner == null) {
			draws++;
		} else if (winner == Marker.X) {
			xWins++;
		} else if (winner == Marker.O) {
			oWins++;
		}
	}

	public int getTotalGames() {
		return xWins + oWins + draws;
	}
}
