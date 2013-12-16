package com.oakonell.chaotictactoe;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.games.GamesClient;
import com.oakonell.chaotictactoe.R;
import com.oakonell.chaotictactoe.googleapi.GameHelper;
import com.oakonell.chaotictactoe.model.Board;
import com.oakonell.chaotictactoe.model.Game;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.State;

public class Achievements {
	private static final int NUM_MOVES_LONG_HAUL = 20;
	private static final int NUM_MOVES_CLEAN_SLATE = 5;
	protected static final int NUM_MOVES_DEJA_VU = 3;

	private BooleanAchievement onlyXsOrOs = new BooleanAchievement(
			R.string.achievement_only_xs_or_os,
			R.string.offline_achievement_only_xs_or_os) {
		@Override
		public void testAndSet(GameHelper gameHelper, Context context,
				Game game, State outcome) {
			Board board = game.getBoard();
			int size = board.getSize();
			int numX = 0;
			int numO = 0;
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					Marker marker = board.getCell(x, y);
					if (marker == Marker.X) {
						numX++;
					} else if (marker == Marker.O) {
						numO++;
					}
					if (numO > 0 && numX > 0)
						break;
				}
			}
			if (numX == 0 || numO == 0) {
				unlock(gameHelper, context);
			}
		}
	};

	private BooleanAchievement chaoticDraw = new BooleanAchievement(
			R.string.achievement_chaotic_draw,
			R.string.offline_achievement_chaotic_draw) {

		@Override
		public void testAndSet(GameHelper gameHelper, Context context,
				Game game, State outcome) {
			if (game.getMarkerChance().isChaotic()) {
				if (outcome.isDraw()) {
					unlock(gameHelper, context);
				}
			}
		}

	};
	private BooleanAchievement dejaVu = new BooleanAchievement(
			R.string.achievement_deja_vu, R.string.offline_achievement_deja_vu) {

		@Override
		public void testAndSet(GameHelper gameHelper, Context context,
				Game game, State outcome) {
			if (game.getBoard().isEmpty()) return;
			if (game.getNumberOfTimesInThisState() > NUM_MOVES_DEJA_VU) {
				unlock(gameHelper, context);
			}
		}
	};

	private BooleanAchievement longHaul = new BooleanAchievement(
			R.string.achievement_long_haul,
			R.string.offline_achievement_long_haul) {

		@Override
		public void testAndSet(GameHelper gameHelper, Context context,
				Game game, State outcome) {
			if (game.getMarkerChance().isChaotic()
					&& game.getNumberOfMoves() > NUM_MOVES_LONG_HAUL) {
				unlock(gameHelper, context);
			}
		}
	};

	private BooleanAchievement withALittleHelp = new BooleanAchievement(
			R.string.achievement_with_a_little_help,
			R.string.offline_achievement_with_a_little_help) {

		@Override
		public void testAndSet(GameHelper gameHelper, Context context,
				Game game, State outcome) {
			if (game.getNumberOfMoves() == game.getBoard().getSize()) {
				// TODO SHould this only apply to the winner?
				unlock(gameHelper, context);
			}
		}

	};
	private BooleanAchievement cleanSlate = new BooleanAchievement(
			R.string.achievement_clean_slate,
			R.string.offline_achievement_chaotic_draw) {

		@Override
		public void testAndSet(GameHelper gameHelper, Context context,
				Game game, State outcome) {
			if (game.getNumberOfMoves() >= NUM_MOVES_CLEAN_SLATE && game.getBoard().isEmpty()) {
				unlock(gameHelper, context);
			}
		}
	};

	private IncrementalAchievement plainJaneCount = new IncrementalAchievement(
			R.string.achievement_plain_jane) {
		@Override
		public void testAndSet(GameHelper gameHelper, Context context,
				Game game, State outcome) {
			if (game.getMarkerChance().isNormal()) {
				increment(gameHelper, context);
			}
		}
	};

	private IncrementalAchievement reversiCount = new IncrementalAchievement(
			R.string.achievement_reversi) {
		@Override
		public void testAndSet(GameHelper gameHelper, Context context,
				Game game, State outcome) {
			if (game.getMarkerChance().isReverse()) {
				increment(gameHelper, context);
			}
		}
	};

	private IncrementalAchievement customCount = new IncrementalAchievement(
			R.string.achievement_customer) {
		@Override
		public void testAndSet(GameHelper gameHelper, Context context,
				Game game, State outcome) {
			if (game.getMarkerChance().isCustom()) {
				increment(gameHelper, context);
			}
		}
	};

	private List<Achievement> endGameAchievements = new ArrayList<Achievements.Achievement>();
	private List<Achievement> inGameAchievements = new ArrayList<Achievements.Achievement>();

	public Achievements() {
		inGameAchievements.add(dejaVu);
		inGameAchievements.add(cleanSlate);

		endGameAchievements.add(onlyXsOrOs);
		endGameAchievements.add(chaoticDraw);
		endGameAchievements.add(longHaul);
		endGameAchievements.add(withALittleHelp);

		endGameAchievements.add(plainJaneCount);
		endGameAchievements.add(reversiCount);
		endGameAchievements.add(customCount);
	}

	private interface Achievement {
		void push(GamesClient client, Context context);

		void testAndSet(GameHelper gameHelper, Context context, Game game,
				State outcome);

		boolean isPending();
	}

	private abstract static class BooleanAchievement implements Achievement {
		private boolean value = false;
		private final int achievementId;
		private final int stringId;

		BooleanAchievement(int achievementId, int stringId) {
			this.achievementId = achievementId;
			this.stringId = stringId;
		}

		@Override
		public boolean isPending() {
			return value;
		}

		public void push(GamesClient client, Context context) {
			if (value) {
				client.unlockAchievement(context.getString(achievementId));
				value = false;
			}
		}

		public void unlock(GameHelper helper, Context context) {
			if (helper.isSignedIn()) {
				helper.getGamesClient().unlockAchievement(
						context.getString(achievementId));
			} else {
				if (!value) {
					Toast.makeText(
							context,
							context.getString(R.string.offline_achievement_label)
									+ ": " + context.getString(stringId),
							Toast.LENGTH_LONG).show();
				}
				value = true;
			}
		}
	}

	private static abstract class IncrementalAchievement implements Achievement {
		private int count = 0;
		private final int achievementId;

		IncrementalAchievement(int achievementId) {
			this.achievementId = achievementId;
		}

		@Override
		public boolean isPending() {
			return count > 0;
		}

		public void push(GamesClient client, Context context) {
			if (count > 0) {
				client.incrementAchievement(context.getString(achievementId),
						count);
				count = 0;
			}
		}

		public void increment(GameHelper helper, Context context) {
			if (helper.isSignedIn()) {
				helper.getGamesClient().incrementAchievement(
						context.getString(achievementId), 1);
			} else {
				count++;
			}
		}
	}

	public boolean hasPending() {
		for (Achievement each : endGameAchievements) {
			if (each.isPending())
				return true;
		}
		for (Achievement each : inGameAchievements) {
			if (each.isPending())
				return true;
		}
		return false;
	}

	public void pushToGoogle(GameHelper helper, Context context) {
		if (!helper.isSignedIn())
			return;

		GamesClient client = helper.getGamesClient();
		for (Achievement each : endGameAchievements) {
			each.push(client, context);
		}
		for (Achievement each : inGameAchievements) {
			each.push(client, context);
		}
	}

	public void testAndSetForInGameAchievements(GameHelper gameHelper,
			Context context, Game game, State outcome) {
		for (Achievement each : inGameAchievements) {
			each.testAndSet(gameHelper, context, game, outcome);
		}
	}

	public void testAndSetForGameEndAchievements(GameHelper gameHelper,
			Context context, Game game, State outcome) {
		for (Achievement each : endGameAchievements) {
			each.testAndSet(gameHelper, context, game, outcome);
		}
	}
}
