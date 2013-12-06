package com.oakonell.chaotictactoe;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.OnScoreSubmittedListener;
import com.google.android.gms.games.leaderboard.SubmitScoreResult;
import com.google.android.gms.games.leaderboard.SubmitScoreResult.Result;
import com.oakonell.chaotictactoe.googleapi.GameHelper;
import com.oakonell.chaotictactoe.model.Game;
import com.oakonell.chaotictactoe.model.State;

public class Leaderboards {

	public  List<String> getLeaderboardIds(Context context) {
		List<String> result = new ArrayList<String>();
		result.add(context
				.getString(R.string.leaderboard_shortest_choatic_game));
		result.add(context.getString(R.string.leaderboard_longest_choatic_game));
		return result;
	}

	public  void submitGame(GameHelper gameHelper, final Context context,
			Game game, State outcome) {

		if (game.getMarkerChance().isChaotic()) {
			// TODO only for online? only for winner?
			GamesClient gamesClient = gameHelper.getGamesClient();
			int submittedScore = game.getNumberOfMoves();

			submitLongestGame(context, gamesClient, submittedScore);
			submitShortestGame(context, gamesClient, submittedScore);
		}
	}

	private  void submitShortestGame(final Context context,
			GamesClient gamesClient, int submittedScore) {
		final String id = context
				.getString(R.string.leaderboard_shortest_choatic_game);
		gamesClient.submitScoreImmediate(new OnScoreSubmittedListener() {

			@Override
			public void onScoreSubmitted(int status, SubmitScoreResult result) {
				if (status != GamesClient.STATUS_OK) {
					// report an error?
					Log.e("Leaderboard", "Error submitting leaderboard score- "
							+ status);
					Toast.makeText(
							context,
							"Error submitting leaderboard score- status="
									+ status, Toast.LENGTH_SHORT).show();
					return;
				}
				if (!result.getLeaderboardId().equals(id))
					return;

				Result allTime = result
						.getScoreResult(LeaderboardVariant.TIME_SPAN_ALL_TIME);
				if (allTime != null && allTime.newBest) {
					Toast.makeText(
							context,
							"You beat your all time minimum moves in a chaotic game - "
									+ allTime.rawScore, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				Result weekly = result
						.getScoreResult(LeaderboardVariant.TIME_SPAN_WEEKLY);
				if (weekly != null && weekly.newBest) {
					Toast.makeText(
							context,
							"You beat your weekly minimum moves in a chaotic game - "
									+ weekly.rawScore, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				Result daily = result
						.getScoreResult(LeaderboardVariant.TIME_SPAN_DAILY);
				if (daily != null && daily.newBest) {
					Toast.makeText(
							context,
							"You beat your daily minimum moves in a chaotic game - "
									+ daily.rawScore, Toast.LENGTH_SHORT)
							.show();
					return;
				}
			}
		}, id, submittedScore);
	}

	private  void submitLongestGame(final Context context,
			GamesClient gamesClient, int submittedScore) {
		final String id = context
				.getString(R.string.leaderboard_longest_choatic_game);
		gamesClient.submitScoreImmediate(new OnScoreSubmittedListener() {

			@Override
			public void onScoreSubmitted(int status, SubmitScoreResult result) {
				if (status != GamesClient.STATUS_OK) {
					// report an error?
					Log.e("Leaderboard", "Error submitting leaderboard score- "
							+ status);
					Toast.makeText(
							context,
							"Error submitting leaderboard score- status="
									+ status, Toast.LENGTH_SHORT).show();
					return;
				}
				if (!result.getLeaderboardId().equals(id))
					return;

				Result allTime = result
						.getScoreResult(LeaderboardVariant.TIME_SPAN_ALL_TIME);
				if (allTime != null && allTime.newBest) {
					Toast.makeText(
							context,
							"You beat your all time maximum moves in a chaotic game - "
									+ allTime.rawScore, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				Result weekly = result
						.getScoreResult(LeaderboardVariant.TIME_SPAN_WEEKLY);
				if (weekly != null && weekly.newBest) {
					Toast.makeText(
							context,
							"You beat your weekly maximum moves in a chaotic game - "
									+ weekly.rawScore, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				Result daily = result
						.getScoreResult(LeaderboardVariant.TIME_SPAN_DAILY);
				if (daily != null && daily.newBest) {
					Toast.makeText(
							context,
							"You beat your daily maximum moves in a chaotic game - "
									+ daily.rawScore, Toast.LENGTH_SHORT)
							.show();
					return;
				}
			}
		}, id, submittedScore);
	}

}
