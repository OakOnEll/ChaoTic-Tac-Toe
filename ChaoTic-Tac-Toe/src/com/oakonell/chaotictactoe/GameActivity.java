package com.oakonell.chaotictactoe;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.google.analytics.tracking.android.EasyTracker;
import com.oakonell.chaotictactoe.googleapi.BaseGameActivity;
import com.oakonell.chaotictactoe.googleapi.GameHelper;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Game;
import com.oakonell.chaotictactoe.model.InvalidMoveException;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.MarkerChance;
import com.oakonell.chaotictactoe.model.ScoreCard;
import com.oakonell.chaotictactoe.model.State;
import com.oakonell.chaotictactoe.utils.Utils;

public class GameActivity extends BaseGameActivity {
	public static final String X_NAME_KEY = "X-name";
	public static final String O_NAME_KEY = "O-name";
	public static final String X_STRATEGY_KEY = "X-strat";
	public static final String O_STRATEGY_KEY = "O-strat";
	public static final String X_FIRST_KEY = "xFirst";

	public static final int HUMAN_STRATEGY_KEY = 1;
	public static final int ONLINE_OPPONENT_STRATEGY_KEY = 2;
	public static final int AI_STRATEGY_KEY = 3;

	private String playerXName = null;
	private String playerOName = null;
	private PlayerStrategy xStrategy;
	private PlayerStrategy oStrategy;

	private ImageView markerToPlayView;
	private View xHeaderLayout;
	private View oHeaderLayout;

	private List<ImageButton> buttons = new ArrayList<ImageButton>();
	private WinOverlayView winOverlayView;

	private Game game;
	private Marker firstPlayer;
	private ScoreCard score;

	PlayerStrategy currentStrategy;
	private RoomListener appListener;

	@Override
	public void onResume() {
		super.onResume();
		// adjust the width or height to make sure the board is a square
		findViewById(R.id.grid_container).getViewTreeObserver()
				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						View squareView = findViewById(R.id.grid_container);
						LayoutParams layout = squareView.getLayoutParams();
						int min = Math.min(squareView.getWidth(),
								squareView.getHeight());
						layout.height = min;
						layout.width = min;
						squareView.setLayoutParams(layout);
						squareView.getViewTreeObserver()
								.removeGlobalOnLayoutListener(this);

						LayoutParams params = winOverlayView.getLayoutParams();
						params.height = layout.height;
						params.width = layout.width;
						winOverlayView.setLayoutParams(params);
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	@Override
	public void onSignInSucceeded() {
		// ignore this... we should be connected for online, and it is a bonus
		// if connected for on-device play
		Toast.makeText(GameActivity.this, "sign in succeeded!?",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSignInFailed() {
		// TODO raise an error for an online game, otherwise silently fail
		Toast.makeText(GameActivity.this, "sign in failure!?",
				Toast.LENGTH_SHORT).show();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.enableStrictMode();
		setContentView(R.layout.activity_game);

		appListener = ((ChaoTicTacToe) getApplication()).getRoomListener();
		if (appListener != null) {
			appListener.setActivity(this);
			appListener.setMoveListener(new MoveListener() {
				@Override
				public void makeMove(Marker marker, Cell cell) {
						GameActivity.this.makeMove(marker, cell)			;
				}
			});
		}

		final ActionBar ab = getSupportActionBar();
		ab.setDisplayUseLogoEnabled(true);
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setTitle(R.string.title_game);

		String stringExtra = getIntent().getStringExtra(X_NAME_KEY);
		if (stringExtra != null) {
			playerXName = stringExtra;
		}
		stringExtra = getIntent().getStringExtra(O_NAME_KEY);
		if (stringExtra != null) {
			playerOName = stringExtra;
		}
		int xPlayerStrategyKey = getIntent().getIntExtra(X_STRATEGY_KEY,
				HUMAN_STRATEGY_KEY);
		int oPlayerStrategyKey = getIntent().getIntExtra(O_STRATEGY_KEY,
				HUMAN_STRATEGY_KEY);
		boolean multiplayer = xPlayerStrategyKey == ONLINE_OPPONENT_STRATEGY_KEY
				|| oPlayerStrategyKey == ONLINE_OPPONENT_STRATEGY_KEY;
		xStrategy = getPlayerStrategy(xPlayerStrategyKey, playerXName,
				multiplayer);
		oStrategy = getPlayerStrategy(oPlayerStrategyKey, playerOName,
				multiplayer);

		TextView xName = (TextView) findViewById(R.id.xName);
		xName.setText(getPlayerTitle(Marker.X));
		TextView oName = (TextView) findViewById(R.id.oName);
		oName.setText(getPlayerTitle(Marker.O));
		xHeaderLayout = findViewById(R.id.x_name_layout);
		oHeaderLayout = findViewById(R.id.o_name_layout);

		firstPlayer = Marker.X;
		currentStrategy = xStrategy;
		if (!getIntent().getBooleanExtra(X_FIRST_KEY, true)) {
			firstPlayer = Marker.O;
			currentStrategy = oStrategy;
		}

		MarkerChance chance = MarkerChance.fromIntentExtras(getIntent());
		score = ScoreCard.fromIntentExtras(getIntent());

		game = new Game(3, firstPlayer, chance);

		markerToPlayView = (ImageView) findViewById(R.id.marker_to_play);
		winOverlayView = (WinOverlayView) findViewById(R.id.win_overlay);

		ImageButton button = (ImageButton) findViewById(R.id.button_r1c1);
		button.setOnClickListener(new ButtonPressListener(new Cell(0, 0)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r1c2);
		button.setOnClickListener(new ButtonPressListener(new Cell(0, 1)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r1c3);
		button.setOnClickListener(new ButtonPressListener(new Cell(0, 2)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r2c1);
		button.setOnClickListener(new ButtonPressListener(new Cell(1, 0)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r2c2);
		button.setOnClickListener(new ButtonPressListener(new Cell(1, 1)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r2c3);
		button.setOnClickListener(new ButtonPressListener(new Cell(1, 2)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r3c1);
		button.setOnClickListener(new ButtonPressListener(new Cell(2, 0)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r3c2);
		button.setOnClickListener(new ButtonPressListener(new Cell(2, 1)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r3c3);
		button.setOnClickListener(new ButtonPressListener(new Cell(2, 2)));
		buttons.add(button);

		updateHeader();
	}

	private PlayerStrategy getPlayerStrategy(int playerStrategyKey,
			String playerName, boolean multiplayer) {
		if (playerStrategyKey == HUMAN_STRATEGY_KEY) {
			return new HumanStrategy(playerName, multiplayer);
		} else if (playerStrategyKey == ONLINE_OPPONENT_STRATEGY_KEY) {
			return new OnlineStrategy(playerName, multiplayer);
		} else if (playerStrategyKey == AI_STRATEGY_KEY) {
			// return new AiStrategy(playerName);
		}
		throw new RuntimeException("Unhandled player strategy!");
	}

	private final class ButtonPressListener implements View.OnClickListener {
		private final Cell cell;

		public ButtonPressListener(Cell cell) {
			this.cell = cell;
		}

		@Override
		public void onClick(View view) {
			if (!currentStrategy.isHuman()) {
				// ignore button clicks if the current player is not a human
				return;
			}
			Marker marker = game.getMarkerToPlay();
			makeMove(marker, cell);
			// send move to opponent
			if (appListener != null) {
				appListener.sendMove(marker, cell);
			}
		}

	}

	private void updateHeader() {
		Marker player = game.getCurrentPlayer();
		float notTurnAlpha = 0.25f;
		if (player == Marker.X) {
			xHeaderLayout.setBackgroundResource(R.drawable.current_player);
			oHeaderLayout.setBackgroundResource(R.drawable.inactive_player);

			if (Utils.hasHoneycomb()) {
				xHeaderLayout.setAlpha(1f);
				oHeaderLayout.setAlpha(notTurnAlpha);
			}

		} else {
			oHeaderLayout.setBackgroundResource(R.drawable.current_player);
			xHeaderLayout.setBackgroundResource(R.drawable.inactive_player);

			if (Utils.hasHoneycomb()) {
				oHeaderLayout.setAlpha(1f);
				xHeaderLayout.setAlpha(notTurnAlpha);
			}
		}

		Marker toPlay = game.getMarkerToPlay();
		if (toPlay == Marker.EMPTY) {
			markerToPlayView.setImageResource(android.R.drawable.ic_delete);
		} else if (toPlay == Marker.X) {
			markerToPlayView.setImageResource(R.drawable.system_cross);
		} else {
			markerToPlayView.setImageResource(R.drawable.system_dot);
		}

		((TextView) findViewById(R.id.num_x_wins)).setText(""
				+ score.getXWins());
		((TextView) findViewById(R.id.num_o_wins)).setText(""
				+ score.getOWins());
		((TextView) findViewById(R.id.num_draws))
				.setText("" + score.getDraws());
	}

	public void makeMove(Marker markerToPlay, Cell cell) {
		Marker marker = game.getMarkerToPlay();
		if (marker != markerToPlay) {
			throw new RuntimeException("Invalid marker played!");
		}
		State outcome = null;
		try {
			outcome = game.placeMarker(cell);
		} catch (InvalidMoveException e) {
			Toast toast = Toast.makeText(GameActivity.this,
					R.string.invalid_move, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return;
		}
		switchPlayerStrategy();

		ImageButton cellButton = findButtonFor(cell);
		if (marker == Marker.EMPTY) {
			cellButton.setImageDrawable(null);
		} else {
			int resId = marker == Marker.X ? R.drawable.system_cross
					: R.drawable.system_dot;
			cellButton.setImageResource(resId);
		}
		if (outcome.isOver()) {
			evaluateGameEndAchievements(outcome);
			OnClickListener cancelListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO leave room,notify opponent of leaving
					finish();
					dialog.dismiss();
				}
			};
			OnClickListener playAgainListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO notify opponent playing again
					game = new Game(3, firstPlayer.opponent(),
							game.getMarkerChance());
					updateHeader();
					winOverlayView.setWinStyle(null);
					winOverlayView.invalidate();
					for (ImageButton each : buttons) {
						each.setImageDrawable(null);
					}
				}
			};
			if (outcome.getWinner() != null) {
				score.incrementScore(outcome.getWinner());
				winOverlayView.setWinStyle(outcome.getWinStyle());
				winOverlayView.invalidate();

				AlertDialog.Builder builder = new AlertDialog.Builder(
						GameActivity.this);
				builder.setTitle(getString(R.string.player_won,
						getPlayerName(outcome.getWinner())));
				builder.setMessage(R.string.play_again);
				builder.setCancelable(false);

				builder.setNegativeButton(R.string.no, cancelListener);
				builder.setPositiveButton(R.string.yes, playAgainListener);

				AlertDialog dialog = builder.create();

				dialog.show();
			} else {
				score.incrementScore(null);
				AlertDialog.Builder builder = new AlertDialog.Builder(
						GameActivity.this);
				builder.setTitle(getString(R.string.draw));
				builder.setMessage(R.string.play_again);
				builder.setCancelable(false);

				builder.setNegativeButton(R.string.no, cancelListener);
				builder.setPositiveButton(R.string.yes, playAgainListener);

				AlertDialog dialog = builder.create();

				dialog.show();
			}

		} else {
			updateHeader();
		}
	}

	private ImageButton findButtonFor(Cell cell) {
		int id;
		int x = cell.getX();
		int y = cell.getY();
		if (x == 0) {
			if (y == 0) {
				id = R.id.button_r1c1;
			} else if (y == 1) {
				id = R.id.button_r1c2;
			} else if (y == 2) {
				id = R.id.button_r1c3;
			} else {
				throw new RuntimeException("Invalid cell");
			}
		} else if (x == 1) {
			if (y == 0) {
				id = R.id.button_r2c1;
			} else if (y == 1) {
				id = R.id.button_r2c2;
			} else if (y == 2) {
				id = R.id.button_r2c3;
			} else {
				throw new RuntimeException("Invalid cell");
			}
		} else if (x == 2) {
			if (y == 0) {
				id = R.id.button_r3c1;
			} else if (y == 1) {
				id = R.id.button_r3c2;
			} else if (y == 2) {
				id = R.id.button_r3c3;
			} else {
				throw new RuntimeException("Invalid cell");
			}
		} else {
			throw new RuntimeException("Invalid cell");
		}
		return (ImageButton) findViewById(id);
	}

	public void switchPlayerStrategy() {
		if (currentStrategy == xStrategy) {
			currentStrategy = oStrategy;
		} else {
			currentStrategy = xStrategy;
		}
	}

	private String getPlayerTitle(Marker player) {
		String playerTurnString;
		if (player == Marker.X) {
			playerTurnString = playerXName;
		} else {
			playerTurnString = playerOName;
		}
		playerTurnString = (playerTurnString != null && playerTurnString.trim()
				.length() > 0) ? (playerTurnString + " (" + player.name() + ")")
				: (getString(R.string.player_label) + " " + player.name());
		return playerTurnString;
	}

	private String getPlayerName(Marker player) {
		String playerTurnString;
		if (player == Marker.X) {
			playerTurnString = playerXName;
		} else {
			playerTurnString = playerOName;
		}
		playerTurnString = (playerTurnString != null && playerTurnString.trim()
				.length() > 0) ? (playerTurnString + " (" + player.name() + ")")
				: player.name();
		return playerTurnString;
	}

	@Override
	public void onStart() {
		super.onStart();
		// ((ChaoTicTacToe) getApplication()).getGameHelper().onStart(this);
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		// ((ChaoTicTacToe) getApplication()).getGameHelper().onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	private void evaluateGameEndAchievements(State outcome) {
		ChaoTicTacToe application = ((ChaoTicTacToe) getApplication());
		GameHelper gameHelper = getGameHelper();

		Achievements achievements = application.getAchievements();
		achievements.testAndSetForGameEndAchievements(gameHelper, this, game,
				outcome);
	}
}
