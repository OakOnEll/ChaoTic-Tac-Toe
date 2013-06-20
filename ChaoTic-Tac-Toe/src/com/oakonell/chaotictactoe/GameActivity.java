package com.oakonell.chaotictactoe;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
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
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.google.analytics.tracking.android.EasyTracker;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Game;
import com.oakonell.chaotictactoe.model.InvalidMoveException;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.MarkerChance;
import com.oakonell.chaotictactoe.model.ScoreCard;
import com.oakonell.chaotictactoe.model.State;
import com.oakonell.chaotictactoe.utils.Utils;

public class GameActivity extends SherlockFragmentActivity {
	public static final String X_NAME_KEY = "X-name";
	public static final String O_NAME_KEY = "O-name";
	public static final String X_FIRST_KEY = "xFirst";

	private Game game;
	private String playerXName = null;
	private String playerOName = null;

	private ImageView markerToPlay;
	private WinOverlayView winOverlay;
	private Marker firstPlayer;

	private View xLayout;
	private View oLayout;

	private List<ImageButton> buttons = new ArrayList<ImageButton>();
	private ScoreCard score;

	@Override
	public void onResume() {
		super.onResume();
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

						LayoutParams params = winOverlay.getLayoutParams();
						params.height = layout.height;
						params.width = layout.width;
						winOverlay.setLayoutParams(params);
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.enableStrictMode();
		setContentView(R.layout.activity_game);

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

		TextView xName = (TextView) findViewById(R.id.xName);
		xName.setText(playerXName);
		TextView oName = (TextView) findViewById(R.id.oName);
		oName.setText(playerOName);
		xLayout = findViewById(R.id.x_name_layout);
		oLayout = findViewById(R.id.o_name_layout);

		firstPlayer = Marker.X;
		if (!getIntent().getBooleanExtra(X_FIRST_KEY, true)) {
			firstPlayer = Marker.O;
		}

		MarkerChance chance = MarkerChance.fromIntentExtras(getIntent());
		score = ScoreCard.fromIntentExtras(getIntent());

		game = new Game(3, firstPlayer, chance);

		markerToPlay = (ImageView) findViewById(R.id.marker_to_play);
		winOverlay = (WinOverlayView) findViewById(R.id.win_overlay);

		ImageButton button = (ImageButton) findViewById(R.id.button_r1c1);
		button.setOnClickListener(new MoveListener(new Cell(0, 0)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r1c2);
		button.setOnClickListener(new MoveListener(new Cell(0, 1)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r1c3);
		button.setOnClickListener(new MoveListener(new Cell(0, 2)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r2c1);
		button.setOnClickListener(new MoveListener(new Cell(1, 0)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r2c2);
		button.setOnClickListener(new MoveListener(new Cell(1, 1)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r2c3);
		button.setOnClickListener(new MoveListener(new Cell(1, 2)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r3c1);
		button.setOnClickListener(new MoveListener(new Cell(2, 0)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r3c2);
		button.setOnClickListener(new MoveListener(new Cell(2, 1)));
		buttons.add(button);

		button = (ImageButton) findViewById(R.id.button_r3c3);
		button.setOnClickListener(new MoveListener(new Cell(2, 2)));
		buttons.add(button);

		updateHeader();
	}

	private final class MoveListener implements View.OnClickListener {
		private final Cell cell;

		public MoveListener(Cell cell) {
			this.cell = cell;
		}

		@Override
		public void onClick(View view) {
			Marker marker = game.getMarkerToPlay();
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
			if (marker == Marker.EMPTY) {
				((ImageButton) view).setImageDrawable(null);
			} else {
				int resId = marker == Marker.X ? R.drawable.system_cross
						: R.drawable.system_dot;
				((ImageButton) view).setImageResource(resId);
			}
			if (outcome.isOver()) {
				OnClickListener cancelListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
						dialog.dismiss();
					}
				};
				OnClickListener playAgainListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						game = new Game(3, firstPlayer.opponent(),
								game.getMarkerChance());
						updateHeader();
						winOverlay.setWinStyle(null);
						winOverlay.invalidate();
						for (ImageButton each : buttons) {
							each.setImageDrawable(null);
						}
					}
				};
				if (outcome.getWinner() != null) {
					score.incrementScore(outcome.getWinner());
					winOverlay.setWinStyle(outcome.getWinStyle());
					winOverlay.invalidate();

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
	}

	private void updateHeader() {
		Marker player = game.getCurrentPlayer();
		float notTurnAlpha = 0.25f;
		if (player == Marker.X) {
			xLayout.setBackgroundResource(R.drawable.current_player);
			oLayout.setBackgroundResource(R.drawable.inactive_player);

			if (Utils.hasHoneycomb()) {
				xLayout.setAlpha(1f);
				oLayout.setAlpha(notTurnAlpha);
			}

		} else {
			oLayout.setBackgroundResource(R.drawable.current_player);
			xLayout.setBackgroundResource(R.drawable.inactive_player);

			if (Utils.hasHoneycomb()) {
				oLayout.setAlpha(1f);
				xLayout.setAlpha(notTurnAlpha);
			}
		}

		Marker toPlay = game.getMarkerToPlay();
		if (toPlay == Marker.EMPTY) {
			markerToPlay.setImageResource(android.R.drawable.ic_delete);
		} else if (toPlay == Marker.X) {
			markerToPlay.setImageResource(R.drawable.system_cross);
		} else {
			markerToPlay.setImageResource(R.drawable.system_dot);
		}

		((TextView) findViewById(R.id.num_x_wins)).setText(""
				+ score.getXWins());
		((TextView) findViewById(R.id.num_o_wins)).setText(""
				+ score.getOWins());
		((TextView) findViewById(R.id.num_draws))
				.setText("" + score.getDraws());
	}

	private String getPlayerName(Marker player) {
		String playerTurnString;
		if (player == Marker.X) {
			playerTurnString = playerXName;
		} else {
			playerTurnString = playerOName;
		}
		playerTurnString = playerTurnString != null ? (playerTurnString + " ("
				+ player.name() + ")") : player.name();
		return playerTurnString;
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

}
