package com.oakonell.chaotictactoe;

import com.oakonell.chaotictactoe.utils.DevelopmentUtil.Info;

import android.app.Application;

public class ChaoTicTacToe extends Application {
	private Achievements achievements = new Achievements();
	private Leaderboards leaderboards = new Leaderboards();

	public Achievements getAchievements() {
		return achievements;
	}

	// public void setAchievements(Achievements achievements) {
	// this.achievements = achievements;
	// }

	public Leaderboards getLeaderboards() {
		return leaderboards;
	}

	Info info;

	public void setDevelopInfo(Info info) {
		this.info = info;
	}

	public Info getDevelopInfo() {
		return info;
	}
}