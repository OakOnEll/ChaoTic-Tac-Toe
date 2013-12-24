package com.oakonell.chaotictactoe.ui.menu;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.oakonell.chaotictactoe.R;
import com.oakonell.chaotictactoe.model.MarkerChance;

public class OnlineGameModeDialog extends SherlockDialogFragment {
	public static final String SELECT_PLAYER_INTENT_KEY = "select_player";

	public interface OnlineGameModeListener {
		void chosenMode(MarkerChance chance);
	}

	private OnlineGameModeListener listener;
	private boolean isQuick;

	public void initialize(boolean isQuick, OnlineGameModeListener listener) {
		this.listener = listener;
		this.isQuick = isQuick;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_online_type, container,
				false);

		if (isQuick) {
			getDialog().setTitle(R.string.choose_quick_game_mode_title);
		} else {
			getDialog().setTitle(R.string.choose_online_game_mode_title);
		}

		final MarkerChanceFragment frag = new MarkerChanceFragment();
		frag.allowCustomType(!isQuick);
		FragmentManager fragmentManager = getChildFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.fragmentContainer, frag);

		transaction.commit();

		Button start = (Button) view.findViewById(R.id.start);
		if (isQuick) {
			start.setText(R.string.choose_online_opponent);
		}
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!validate(frag)) {
					return;
				}

				MarkerChance chance = frag.getChance();
				dismiss();
				listener.chosenMode(chance);
			}
		});
		return view;

	}

	protected boolean validate(MarkerChanceFragment frag) {
		boolean isValid = true;

		isValid &= frag.validate();
		return isValid;
	}

}
