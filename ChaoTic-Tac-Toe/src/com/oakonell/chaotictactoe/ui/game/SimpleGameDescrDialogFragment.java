package com.oakonell.chaotictactoe.ui.game;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.oakonell.chaotictactoe.R;
import com.oakonell.chaotictactoe.model.Game;

public class SimpleGameDescrDialogFragment extends SherlockDialogFragment {
	private GameFragment parent;
	private Game game;

	public void initialize(GameFragment parent, Game game) {
		this.parent = parent;
		this.game = game;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.simple_game_mode_help_dialog,
				container, false);
		getDialog().setTitle(game.getMarkerChance().getLabel(getActivity()));
		TextView howToPlayText = (TextView) view
				.findViewById(R.id.how_to_play_text);
		if (game.getMarkerChance().isNormal()) {
			howToPlayText
					.setText("Traditional Tic-Tac-Toe, Player X plays Xs, and Player O plays Os");
		} else if (game.getMarkerChance().isReverse()) {
			howToPlayText
					.setText("Reverse Tic-Tac-Toe, Player X plays Os, and Player O plays Xs");
			ImageView marker = (ImageView) view.findViewById(R.id.x_player_marker);
			marker.setImageResource(R.drawable.system_dot);
		}

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
