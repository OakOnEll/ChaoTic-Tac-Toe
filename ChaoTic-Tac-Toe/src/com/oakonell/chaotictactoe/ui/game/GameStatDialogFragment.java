package com.oakonell.chaotictactoe.ui.game;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.google.android.gms.common.images.ImageManager;
import com.oakonell.chaotictactoe.R;
import com.oakonell.chaotictactoe.model.Game;
import com.oakonell.chaotictactoe.model.ScoreCard;

public class GameStatDialogFragment extends SherlockDialogFragment {
	private GameFragment parent;
	private ScoreCard score;
	private Game game;

	private ImageManager imgManager;

	public void initialize(GameFragment parent, Game game, ScoreCard score) {
		this.parent = parent;
		this.score = score;
		this.game = game;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.game_stats_dialog, container,
				false);
		getDialog().setTitle(R.string.game_stats_title);
		imgManager = ImageManager.create(parent.getMainActivity());

		ImageView playerX = (ImageView) view.findViewById(R.id.player_x);
		game.getXPlayer().updatePlayerImage(imgManager, playerX);

		ImageView playerO = (ImageView) view.findViewById(R.id.player_o);
		game.getOPlayer().updatePlayerImage(imgManager, playerO);

		TextView xNameText = (TextView) view.findViewById(R.id.x_name);
		xNameText.setText(game.getXPlayer().getName());

		TextView oNameText = (TextView) view.findViewById(R.id.o_name);
		oNameText.setText(game.getOPlayer().getName());

		TextView oWinsText = (TextView) view.findViewById(R.id.o_wins);
		TextView oLossesText = (TextView) view.findViewById(R.id.o_losses);
		oWinsText.setText(score.getOWins() + "");
		oLossesText.setText(score.getXWins() + "");

		TextView xWinsText = (TextView) view.findViewById(R.id.x_wins);
		TextView xLossesText = (TextView) view.findViewById(R.id.x_losses);
		xWinsText.setText(score.getXWins() + "");
		xLossesText.setText(score.getOWins() + "");

		TextView numDraws = (TextView) view.findViewById(R.id.num_draws);
		numDraws.setText(parent.getResources().getQuantityString(R.plurals.num_draws_with_label, score.getDraws(), score.getDraws()));

		View ok = view.findViewById(R.id.ok_button);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getDialog().dismiss();
				parent.gameStatsClosed();
			}
		});

		return view;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);

		dialog.getWindow().getAttributes().windowAnimations = R.style.game_stats_dialog_Window;

		return dialog;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		parent.gameStatsClosed();
	}
}
