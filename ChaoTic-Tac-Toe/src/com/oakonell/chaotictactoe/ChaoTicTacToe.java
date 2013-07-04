package com.oakonell.chaotictactoe;

import android.app.Application;

public class ChaoTicTacToe extends Application {
	private Achievements achievements = new Achievements();

	public Achievements getAchievements() {
		return achievements;
	}

	public void setAchievements(Achievements achievements) {
		this.achievements = achievements;
	}

}