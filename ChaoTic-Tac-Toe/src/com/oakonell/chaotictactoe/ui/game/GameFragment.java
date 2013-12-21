package com.oakonell.chaotictactoe.ui.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.multiplayer.Participant;
import com.oakonell.chaotictactoe.Achievements;
import com.oakonell.chaotictactoe.ChaoTicTacToe;
import com.oakonell.chaotictactoe.GameMode;
import com.oakonell.chaotictactoe.Leaderboards;
import com.oakonell.chaotictactoe.MainActivity;
import com.oakonell.chaotictactoe.PlayerStrategy;
import com.oakonell.chaotictactoe.R;
import com.oakonell.chaotictactoe.RoomListener;
import com.oakonell.chaotictactoe.Sounds;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Game;
import com.oakonell.chaotictactoe.model.InvalidMoveException;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.MarkerChance;
import com.oakonell.chaotictactoe.model.ScoreCard;
import com.oakonell.chaotictactoe.model.State;
import com.oakonell.utils.StringUtils;
import com.oakonell.utils.Utils;

public class GameFragment extends SherlockFragment {
	private static final int NON_HUMAN_OPPONENT_HIGHLIGHT_MOVE_PAUSE_MS = 300;
	private static final int MARKER_ROLL_VISIBILITY_PAUSE = 150;
	private static final int OPPONENT_MARKER_VISIBILITY_PAUSE_MS = 450;

	private ImageManager imgManager;

	private ImageView markerToPlayView;
	private View xHeaderLayout;
	private View oHeaderLayout;
	private TextView xWins;
	private TextView oWins;
	private TextView draws;

	private TextView numMoves;

	private List<ImageButton> buttons = new ArrayList<ImageButton>();
	private WinOverlayView winOverlayView;

	private Game game;
	private ScoreCard score;

	private PlayerStrategy xStrategy;
	private PlayerStrategy oStrategy;

	private PlayerStrategy currentStrategy;

	private List<ChatMessage> messages = new ArrayList<ChatMessage>();
	private int numNewMessages;

	private ChatDialogFragment chatDialog;
	private MenuItem chatMenuItem;

	private GameMode mode;
	private ProgressBar thinking;

	@Override
	public void onResume() {
		super.onResume();
		// adjust the width or height to make sure the board is a square
		getActivity().findViewById(R.id.grid_container).getViewTreeObserver()
				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						View squareView = getActivity().findViewById(
								R.id.grid_container);
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.game, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		chatMenuItem = menu.findItem(R.id.action_chat);
		handleMenu();
	}

	private void invalidateMenu() {
		if (!ActivityCompat.invalidateOptionsMenu(getActivity())) {
			handleMenu();
		} else {
			getActivity().invalidateOptionsMenu();
		}
	}

	private void handleMenu() {
		chatMenuItem.setVisible(getMainActivity().getRoomListener() != null);
		RelativeLayout actionView = (RelativeLayout) chatMenuItem
				.getActionView();
		actionView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openChatDialog();
			}
		});
		TextView textView = (TextView) actionView
				.findViewById(R.id.actionbar_notifcation_textview);
		ImageView imageView = (ImageView) actionView
				.findViewById(R.id.actionbar_notifcation_imageview);
		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openChatDialog();
			}
		});
		if (numNewMessages > 0) {
			textView.setText("" + numNewMessages);
			imageView.setImageResource(R.drawable.message_available_icon_1332);

			// TODO play a sound
			StringUtils.applyFlashEnlargeAnimation(textView);
		} else {
			imageView.setImageResource(R.drawable.message_icon_27709);
			textView.setText("");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_chat:
			openChatDialog();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void openChatDialog() {
		chatDialog = new ChatDialogFragment();
		// TODO get the opponent name in nice fashion- ie, 'Anonymous'
		// instead of google+'s anonymized string
		chatDialog.initialize(this, messages, getMainActivity()
				.getRoomListener().getMe(), getMainActivity().getRoomListener()
				.getOpponentName());
		chatDialog.show(getChildFragmentManager(), "chat");
	}

	public void startGame(PlayerStrategy xStrategy, PlayerStrategy oStrategy,
			Game game, ScoreCard score) {
		this.xStrategy = xStrategy;
		this.oStrategy = oStrategy;

		currentStrategy = xStrategy;

		if (game.getCurrentPlayer() == Marker.X) {
			currentStrategy = xStrategy;
		} else {
			currentStrategy = oStrategy;
		}

		this.score = score;
		this.game = game;
		if (!currentStrategy.isHuman()) {
			if (thinking != null) {
				// show a thinking/progress icon, suitable for network play and
				// ai
				// thinking..
				thinking.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_game, container, false);
		view.setKeepScreenOn(mode == GameMode.ONLINE);

		setHasOptionsMenu(true);
		thinking = (ProgressBar) view.findViewById(R.id.thinking);
		if (currentStrategy != null && !currentStrategy.isHuman()) {
			if (thinking != null) {
				// show a thinking/progress icon, suitable for network play and
				// ai
				// thinking..
				thinking.setVisibility(View.VISIBLE);
			}
		}

		imgManager = ImageManager.create(getMainActivity());

		TextView xName = (TextView) view.findViewById(R.id.xName);
		xName.setText(getPlayerTitle(Marker.X));
		TextView oName = (TextView) view.findViewById(R.id.oName);
		oName.setText(getPlayerTitle(Marker.O));

		ImageView xImage = (ImageView) view.findViewById(R.id.x_back);
		ImageView oImage = (ImageView) view.findViewById(R.id.o_back);

		updatePlayerImage(xImage, xStrategy.getIconImageUri(),
				R.drawable.system_cross_faded);
		updatePlayerImage(oImage, oStrategy.getIconImageUri(),
				R.drawable.system_dot_faded);

		xHeaderLayout = view.findViewById(R.id.x_name_layout);
		oHeaderLayout = view.findViewById(R.id.o_name_layout);

		markerToPlayView = (ImageView) view.findViewById(R.id.marker_to_play);
		winOverlayView = (WinOverlayView) view.findViewById(R.id.win_overlay);

		ImageButton button = (ImageButton) view.findViewById(R.id.button_r1c1);
		button.setOnClickListener(new ButtonPressListener(new Cell(0, 0)));
		buttons.add(button);

		button = (ImageButton) view.findViewById(R.id.button_r1c2);
		button.setOnClickListener(new ButtonPressListener(new Cell(0, 1)));
		buttons.add(button);

		button = (ImageButton) view.findViewById(R.id.button_r1c3);
		button.setOnClickListener(new ButtonPressListener(new Cell(0, 2)));
		buttons.add(button);

		button = (ImageButton) view.findViewById(R.id.button_r2c1);
		button.setOnClickListener(new ButtonPressListener(new Cell(1, 0)));
		buttons.add(button);

		button = (ImageButton) view.findViewById(R.id.button_r2c2);
		button.setOnClickListener(new ButtonPressListener(new Cell(1, 1)));
		buttons.add(button);

		button = (ImageButton) view.findViewById(R.id.button_r2c3);
		button.setOnClickListener(new ButtonPressListener(new Cell(1, 2)));
		buttons.add(button);

		button = (ImageButton) view.findViewById(R.id.button_r3c1);
		button.setOnClickListener(new ButtonPressListener(new Cell(2, 0)));
		buttons.add(button);

		button = (ImageButton) view.findViewById(R.id.button_r3c2);
		button.setOnClickListener(new ButtonPressListener(new Cell(2, 1)));
		buttons.add(button);

		button = (ImageButton) view.findViewById(R.id.button_r3c3);
		button.setOnClickListener(new ButtonPressListener(new Cell(2, 2)));
		buttons.add(button);

		xWins = (TextView) view.findViewById(R.id.num_x_wins);
		oWins = (TextView) view.findViewById(R.id.num_o_wins);
		draws = (TextView) view.findViewById(R.id.num_draws);

		numMoves = (TextView) view.findViewById(R.id.num_moves);
		if (game.getMarkerChance().isNormal()
				|| game.getMarkerChance().isReverse()) {
			numMoves.setVisibility(View.GONE);
			view.findViewById(R.id.num_moves_lbl).setVisibility(View.GONE);
		}

		updateHeader();
		return view;
	}

	private void updatePlayerImage(ImageView xImage, Uri xUri,
			int defaultResource) {
		if (xUri == null
				|| xUri.getEncodedSchemeSpecificPart().contains("gms.games")) {
			imgManager.loadImage(xImage, xUri, defaultResource);
		} else {
			xImage.setImageURI(xUri);
		}
	}

	private final class ButtonPressListener implements View.OnClickListener {
		private final Cell cell;

		public ButtonPressListener(Cell cell) {
			this.cell = cell;
		}

		@Override
		public void onClick(View view) {
			if (isRolling) {
				return;
			}
			if (!currentStrategy.isHuman()) {
				// ignore button clicks if the current player is not a human
				return;
			}
			Marker marker = game.getMarkerToPlay();
			boolean wasValid = makeMove(marker, cell);
			if (!wasValid)
				return;

			// send move to opponent
			RoomListener appListener = getMainActivity().getRoomListener();
			if (appListener != null) {
				appListener.sendMove(marker, cell);
			}
		}

	}

	private void updateHeader() {
		Marker player = game.getCurrentPlayer();
		numMoves.setText("" + game.getNumberOfMoves());
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

		// TODO make the chaotic mode show some "rolling" of the markers
		// BUT this requires the online/input handling to not accept input until
		// the roll is done
		// on device inputs, can simply ignore button presses.
		// online moves need to be queued while animation is proceeding
		if (game.getMarkerChance().isChaotic()
				|| game.getMarkerChance().isCustom()) {
			displayAnimatedMarkerToPlay();
		} else {
			displayMarkerToPlay();
		}

		xWins.setText("" + score.getXWins());
		oWins.setText("" + score.getOWins());
		draws.setText("" + score.getDraws());
	}

	private Random rollRandom = new Random();
	private volatile boolean isRolling = false;
	private Runnable afterRoll;

	private void displayAnimatedMarkerToPlay() {
		MarkerChance chance = game.getMarkerChance();
		final List<Integer> resourcesList = new ArrayList<Integer>();
		if (chance.getMyMarker() > 0) {
			if (currentStrategy.getMarker() == Marker.X) {
				resourcesList.add(R.drawable.system_cross);
			} else {
				resourcesList.add(R.drawable.system_dot);
			}
		}
		if (chance.getOpponentMarker() > 0) {
			if (currentStrategy.getMarker() == Marker.X) {
				resourcesList.add(R.drawable.system_dot);
			} else {
				resourcesList.add(R.drawable.system_cross);
			}
		}
		if (chance.getRemoveMarker() > 0) {
			resourcesList.add(android.R.drawable.ic_delete);
		}
		final int numPossible = resourcesList.size();
		// number of rolls cycled through is random between min <-> max
		// hold the number in a modifiable int array, as a cheat
		final int[] rolls = new int[] { rollRandom.nextInt(5) + 5 };
		isRolling = true;
		final Drawable originalBackground = markerToPlayView.getBackground();
		markerToPlayView
				.setBackgroundColor(getResources()
						.getColor(
								com.actionbarsherlock.R.color.abs__bright_foreground_disabled_holo_light));
		final Handler handler = new Handler();
		final MainActivity mainActivity = getMainActivity();
		// display a way to say "still rolling"
		final Runnable flip = new Runnable() {
			@Override
			public void run() {
				rolls[0] = rolls[0] - 1;
				if (rolls[0] == 0) {
					diceRollStreamId = 0;
					markerToPlayView.setBackgroundDrawable(originalBackground);
					displayMarkerToPlay();
					// let the marker to play show for a bit before allowing the
					// opponent's (AI or online) move
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							isRolling = false;
							if (afterRoll != null) {
								afterRoll.run();
								afterRoll = null;
							}
						}
					}, OPPONENT_MARKER_VISIBILITY_PAUSE_MS);
					return;
				}

				if (diceRollStreamId >0) {
					mainActivity.stopSound(diceRollStreamId);
				}
				diceRollStreamId = mainActivity.playSound(Sounds.DICE_ROLL);
				markerToPlayView.setImageResource(resourcesList.get(rolls[0]
						% numPossible));
				handler.postDelayed(this, MARKER_ROLL_VISIBILITY_PAUSE);
			}
		};

		flip.run();
	}
	private int diceRollStreamId;

	private void displayMarkerToPlay() {
		Marker toPlay = game.getMarkerToPlay();
		if (toPlay == Marker.EMPTY) {
			markerToPlayView.setImageResource(android.R.drawable.ic_delete);
		} else if (toPlay == Marker.X) {
			markerToPlayView.setImageResource(R.drawable.system_cross);
		} else {
			markerToPlayView.setImageResource(R.drawable.system_dot);
		}
	}

	public boolean makeMove(Marker markerToPlay, Cell cell) {
		Marker marker = game.getMarkerToPlay();
		if (marker != markerToPlay) {
			throw new RuntimeException("Invalid marker played!");
		}
		State outcome = null;
		try {
			outcome = game.placeMarker(cell);
		} catch (InvalidMoveException e) {
			getMainActivity().playSound(Sounds.INVALID_MOVE);
			Toast toast = Toast.makeText(getActivity(), R.string.invalid_move,
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			return false;
		}
		if (markerToPlay == Marker.X) {
			getMainActivity().playSound(Sounds.PLAY_X);
		} else {
			getMainActivity().playSound(Sounds.PLAY_O);
		}
		privateMakeMove(cell, marker, outcome);
		return true;
	}

	private void privateMakeMove(Cell cell, Marker marker, State outcome) {
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
			endGame(outcome);
		} else {
			evaluateInGameAchievements(outcome);
			updateHeader();
			moveIfAI();
		}
	}

	private void endGame(State outcome) {
		numMoves.setText("" + game.getNumberOfMoves());
		evaluateGameEndAchievements(outcome);
		evaluateLeaderboards(outcome);
		OnClickListener cancelListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO leave room,notify opponent of leaving
				dialog.dismiss();
				getMainActivity().getSupportFragmentManager().popBackStack();
				getMainActivity().gameEnded();
			}
		};
		OnClickListener playAgainListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO notify opponent playing again
				game = new Game(3, currentStrategy.getMarker(),
						game.getMarkerChance());
				updateHeader();
				winOverlayView.setWinStyle(null);
				winOverlayView.invalidate();
				for (ImageButton each : buttons) {
					each.setImageDrawable(null);
				}
				moveIfAI();
			}
		};
		Marker winner = outcome.getWinner();
		if (winner != null) {
			score.incrementScore(winner);
			winOverlayView.setWinStyle(outcome.getWinStyle());
			winOverlayView.invalidate();

			if (mode == GameMode.PASS_N_PLAY) {
				getMainActivity().playSound(Sounds.GAME_WON);
			} else {
				// the player either won or lost
				if (xStrategy.isHuman() && winner == Marker.X) {
					getMainActivity().playSound(Sounds.GAME_WON);
				} else if (oStrategy.isHuman() && winner == Marker.O) {
					getMainActivity().playSound(Sounds.GAME_WON);
				} else {
					getMainActivity().playSound(Sounds.GAME_LOST);
				}
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getString(R.string.player_won,
					getPlayerName(winner)));
			builder.setMessage(R.string.play_again);
			builder.setCancelable(false);

			builder.setNegativeButton(R.string.no, cancelListener);
			builder.setPositiveButton(R.string.yes, playAgainListener);

			AlertDialog dialog = builder.create();

			dialog.show();
		} else {
			score.incrementScore(null);
			getMainActivity().playSound(Sounds.GAME_DRAW);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getString(R.string.draw));
			builder.setMessage(R.string.play_again);
			builder.setCancelable(false);

			builder.setNegativeButton(R.string.no, cancelListener);
			builder.setPositiveButton(R.string.yes, playAgainListener);

			AlertDialog dialog = builder.create();

			dialog.show();
		}
	}

	private void moveIfAI() {
		if (currentStrategy.isHuman()) {
			return;
		}
		// show a thinking/progress icon, suitable for network play and ai
		// thinking..
		thinking.setVisibility(View.VISIBLE);
		if (!currentStrategy.isAI()) {
			return;
		}

		AsyncTask<Void, Void, Cell> aiMove = new AsyncTask<Void, Void, Cell>() {
			@Override
			protected Cell doInBackground(Void... params) {
				return currentStrategy.move(game.getBoard(),
						game.getMarkerToPlay());
			}

			@Override
			protected void onPostExecute(final Cell move) {
				if (isRolling) {
					afterRoll = new Runnable() {
						@Override
						public void run() {
							highlightAndMakeMove(game.getMarkerToPlay(), move);
						}
					};
					return;
				}
				highlightAndMakeMove(game.getMarkerToPlay(), move);
			}
		};
		aiMove.execute((Void) null);
	}

	public void onlineMakeMove(final Marker marker, final Cell cell) {
		if (isRolling) {
			afterRoll = new Runnable() {
				@Override
				public void run() {
					highlightAndMakeMove(marker, cell);
				}
			};
			return;
		}
		highlightAndMakeMove(marker, cell);
	}

	public void highlightAndMakeMove(final Marker marker, final Cell move) {
		// hide the progress icon
		thinking.setVisibility(View.GONE);
		// delay and highlight the move so the human player has a
		// chance to see it
		final ImageButton cellButton = findButtonFor(move);
		final Drawable originalBackGround = cellButton.getBackground();
		cellButton.setBackgroundColor(getResources().getColor(
				android.R.color.holo_blue_light));
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO not
				// TODO play opponent sound
				cellButton.setBackgroundDrawable(originalBackGround);
				makeMove(marker, move);
			}
		}, NON_HUMAN_OPPONENT_HIGHLIGHT_MOVE_PAUSE_MS);
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
		return (ImageButton) getActivity().findViewById(id);
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
			playerTurnString = xStrategy.getName();
		} else {
			playerTurnString = oStrategy.getName();
		}
		// playerTurnString = (playerTurnString != null &&
		// playerTurnString.trim()
		// .length() > 0) ? (playerTurnString + " (" + player.name() + ")")
		// : (getString(R.string.player_label) + " " + player.name());
		return playerTurnString;
	}

	private String getPlayerName(Marker player) {
		String playerTurnString;
		if (player == Marker.X) {
			playerTurnString = xStrategy.getName();
		} else {
			playerTurnString = oStrategy.getName();
		}
		playerTurnString = (playerTurnString != null && playerTurnString.trim()
				.length() > 0) ? (playerTurnString + " (" + player.name() + ")")
				: player.name();
		return playerTurnString;
	}

	private void evaluateGameEndAchievements(State outcome) {
		ChaoTicTacToe application = ((ChaoTicTacToe) getActivity()
				.getApplication());

		Achievements achievements = application.getAchievements();
		achievements.testAndSetForGameEndAchievements(getMainActivity()
				.getGameHelper(), getActivity(), game, outcome);
	}

	private void evaluateInGameAchievements(State outcome) {
		ChaoTicTacToe application = ((ChaoTicTacToe) getActivity()
				.getApplication());

		Achievements achievements = application.getAchievements();
		achievements.testAndSetForInGameAchievements(getMainActivity()
				.getGameHelper(), getActivity(), game, outcome);
	}

	private void evaluateLeaderboards(State outcome) {
		ChaoTicTacToe application = ((ChaoTicTacToe) getActivity()
				.getApplication());

		Leaderboards leaderboards = application.getLeaderboards();
		leaderboards.submitGame(getMainActivity().getGameHelper(),
				getActivity(), game, outcome);

	}

	public MainActivity getMainActivity() {
		return (MainActivity) super.getActivity();
	}

	public void messageRecieved(Participant opponentParticipant, String string) {
		messages.add(new ChatMessage(opponentParticipant, string, false));
		getMainActivity().playSound(Sounds.CHAT_RECIEVED);
		if (chatDialog != null) {
			chatDialog.newMessage();
		} else {
			numNewMessages++;
			invalidateMenu();
		}
	}

	public void chatClosed() {
		chatDialog = null;
		numNewMessages = 0;
		invalidateMenu();
	}

	public void setMode(GameMode mode) {
		this.mode = mode;
	}

}
