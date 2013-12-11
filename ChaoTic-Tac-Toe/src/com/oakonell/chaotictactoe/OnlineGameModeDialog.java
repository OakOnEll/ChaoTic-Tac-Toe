package com.oakonell.chaotictactoe;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockDialogFragment;
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

		// TODO utilize isQuick to remove custom option from chance fragment
		if (isQuick) {
			getDialog().setTitle("Choose Quick Game Mode");
		} else {
			getDialog().setTitle("Choose Game Mode");
		}

		final MarkerChanceFragment frag = new MarkerChanceFragment();
		frag.allowCustomType(!isQuick);
		FragmentManager fragmentManager = getChildFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.fragmentContainer, frag);

		transaction.commit();

		Button start = (Button) view.findViewById(R.id.start);
		if (isQuick) {
			start.setText("Find Opponent");
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
