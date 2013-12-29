package com.oakonell.chaotictactoe.ui.game;

import java.text.DecimalFormat;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.oakonell.chaotictactoe.R;
import com.oakonell.chaotictactoe.model.Game;
import com.oakonell.chaotictactoe.model.MarkerChance;

public class ComplexGameDescrDialogFragment extends SherlockDialogFragment {
	private GameFragment parent;
	private Game game;
	private DecimalFormat format = new DecimalFormat("###.##");

	public void initialize(GameFragment parent, Game game) {
		this.parent = parent;
		this.game = game;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.complex_game_mode_help_dialog,
				container, false);
		MarkerChance markerChance = game.getMarkerChance();
		getDialog().setTitle(markerChance.getLabel(getActivity()));
		TextView howToPlayText = (TextView) view
				.findViewById(R.id.how_to_play_text);

		TextView playerPercent = (TextView) view
				.findViewById(R.id.player_marker_percentage);
		TextView opponentPercent = (TextView) view
				.findViewById(R.id.opponent_marker_percentage);
		TextView removePercent = (TextView) view
				.findViewById(R.id.remove_marker_percentage);

		playerPercent.setText(format.format(100*markerChance
				.getMyMarkerPercentage()) + "%");
		opponentPercent.setText(format.format(100*markerChance
				.getOpponentMarkerPercentage()) + "%");
		removePercent.setText(format.format(100*markerChance
				.getRemoveMarkerPercentage()) + "%");

		howToPlayText
				.setText("On each player's turn, there is a chance they can play their own marker, their opponent's marker, or can use a bomb to explode an existing marker on the board.");
		View ok = view.findViewById(R.id.ok_button);

		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				getDialog().dismiss();
				parent.gameHelpClosed();				
			}
		});
		return view;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		parent.gameHelpClosed();
	}
}
