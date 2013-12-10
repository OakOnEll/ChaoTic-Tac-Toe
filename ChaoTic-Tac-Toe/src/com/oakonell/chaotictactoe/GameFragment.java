package com.oakonell.chaotictactoe;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.games.multiplayer.Participant;
import com.oakonell.chaotictactoe.model.Cell;
import com.oakonell.chaotictactoe.model.Game;
import com.oakonell.chaotictactoe.model.InvalidMoveException;
import com.oakonell.chaotictactoe.model.Marker;
import com.oakonell.chaotictactoe.model.ScoreCard;
import com.oakonell.chaotictactoe.model.State;
import com.oakonell.utils.Utils;

public class GameFragment extends SherlockFragment {
	private ImageView markerToPlayView;
	private View xHeaderLayout;
	private View oHeaderLayout;
	private TextView xWins;
	private TextView oWins;
	private TextView draws;

	private List<ImageButton> buttons = new ArrayList<ImageButton>();
	private WinOverlayView winOverlayView;

	private Game game;
	private ScoreCard score;

	private PlayerStrategy xStrategy;
	private PlayerStrategy oStrategy;

	private PlayerStrategy currentStrategy;

	private List<ChatMessage> messages = new ArrayList<ChatMessage>();
	private boolean hasNewMessage;

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
		if (hasNewMessage) {
			chatMenuItem.setIcon(android.R.drawable.ic_dialog_email);
		} else {
			chatMenuItem.setIcon(android.R.drawable.ic_menu_call);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_chat:
			chatDialog = new ChatDialogFragment();
			chatDialog.initialize(this, messages, getMainActivity()
					.getRoomListener().getMe());
			chatDialog.show(getChildFragmentManager(), "chat");
			hasNewMessage = false;
			invalidateMenu();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void startGame(PlayerStrategy xStrategy, PlayerStrategy oStrategy,
			Game game, ScoreCard score) {
		this.xStrategy = xStrategy;
		this.oStrategy = oStrategy;

		currentStrategy = xStrategy;

		if (game.getCurrentPlayer()== Marker.X) {
			currentStrategy = xStrategy;
		} else {
			currentStrategy = oStrategy;
		}

		this.score = score;
		this.game = game;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_game, container, false);
		setHasOptionsMenu(true);

		TextView xName = (TextView) view.findViewById(R.id.xName);
		xName.setText(getPlayerTitle(Marker.X));
		TextView oName = (TextView) view.findViewById(R.id.oName);
		oName.setText(getPlayerTitle(Marker.O));
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

		updateHeader();
		return view;
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
			RoomListener appListener = getMainActivity().getRoomListener();
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

		xWins.setText("" + score.getXWins());
		oWins.setText("" + score.getOWins());
		draws.setText("" + score.getDraws());
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
			Toast toast = Toast.makeText(getActivity(), R.string.invalid_move,
					Toast.LENGTH_SHORT);
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
			evaluateLeaderboards(outcome);
			OnClickListener cancelListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO leave room,notify opponent of leaving
					dialog.dismiss();
					getMainActivity().getSupportFragmentManager()
							.popBackStack();
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
			if (outcome.getWinner() != null) {
				score.incrementScore(outcome.getWinner());
				winOverlayView.setWinStyle(outcome.getWinStyle());
				winOverlayView.invalidate();

				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
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
						getActivity());
				builder.setTitle(getString(R.string.draw));
				builder.setMessage(R.string.play_again);
				builder.setCancelable(false);

				builder.setNegativeButton(R.string.no, cancelListener);
				builder.setPositiveButton(R.string.yes, playAgainListener);

				AlertDialog dialog = builder.create();

				dialog.show();
			}

		} else {
			evaluateInGameAchievements(outcome);
			updateHeader();
			moveIfAI();
		}
	}

	private void moveIfAI() {
		if (currentStrategy.isAI()) {
			final Cell move = currentStrategy.move(game.getBoard(),
					game.getMarkerToPlay());
			// delay and highlight the move so the human player has a
			// chance to see it

			final ImageButton cellButton = findButtonFor(move);
			final Drawable originalBackGround = cellButton.getBackground();
			cellButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					cellButton.setBackground(originalBackGround);
					makeMove(game.getMarkerToPlay(), move);
				}
			}, 200);
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
		playerTurnString = (playerTurnString != null && playerTurnString.trim()
				.length() > 0) ? (playerTurnString + " (" + player.name() + ")")
				: (getString(R.string.player_label) + " " + player.name());
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
		// TODO Auto-generated method stub
		ChaoTicTacToe application = ((ChaoTicTacToe) getActivity()
				.getApplication());

		Leaderboards leaderboards = application.getLeaderboards();
		leaderboards.submitGame(getMainActivity().getGameHelper(),
				getActivity(), game, outcome);

	}

	public MainActivity getMainActivity() {
		return (MainActivity) super.getActivity();
	}

	private ChatDialogFragment chatDialog;
	private MenuItem chatMenuItem;

	public void messageRecieved(Participant opponentParticipant, String string) {
		messages.add(new ChatMessage(opponentParticipant, string));
		if (chatDialog != null) {
			chatDialog.newMessage();
		} else {
			hasNewMessage = true;
			invalidateMenu();
		}
	}

	public void chatClosed() {
		chatDialog = null;
	}

}
