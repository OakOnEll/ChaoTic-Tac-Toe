package com.oakonell.chaotictactoe;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.google.android.gms.games.multiplayer.Participant;

public class ChatDialogFragment extends SherlockDialogFragment {
	private List<ChatMessage> messages;
	private MessagesAdapter adapter;
	private Participant me;
	private GameFragment parent;

	private static class MessagesAdapter extends ArrayAdapter<ChatMessage> {
		private Context context;

		public MessagesAdapter(Context context, int textViewResourceId,
				List<ChatMessage> objects) {
			super(context, textViewResourceId, objects);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.message_item, parent, false);
			ImageView picView = (ImageView) rowView.findViewById(R.id.player_pic);
			TextView messageView = (TextView) rowView.findViewById(R.id.message);
			ChatMessage item = getItem(position);
			messageView.setText(item.getMessage());
			// TODO renders on UI thread
			picView.setImageURI(item.getParticipant().getIconImageUri());

			return rowView;
		}
	}

	public void initialize(GameFragment parent, List<ChatMessage> messages, Participant me) {
		this.parent = parent;
		this.messages = messages;
		this.me = me;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.chat_dialog, container, false);
		
		ListView messagesView= (ListView)view.findViewById(R.id.messages);
		final TextView messageView = (TextView) view.findViewById(R.id.message);
		
		adapter = new MessagesAdapter(getActivity(), R.id.messages, messages);
		messagesView.setAdapter(adapter);
		
		Button sendButton = (Button) view.findViewById(R.id.send);
		sendButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				sendMessage(messageView.getText().toString());
				adapter.notifyDataSetChanged();
				messageView.setText("");
			}
		});
		
		ImageView closeView = (ImageView) view.findViewById(R.id.close);
		closeView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				dismiss();
				parent.chatClosed();
			}
		});
		
		
		return view;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		parent.chatClosed();
	}
	
	protected void sendMessage(String string) {
		((MainActivity) getActivity()).getRoomListener().sendMessage(string);
		messages.add(new ChatMessage(me, string));
	}

	public void newMessage() {
		adapter.notifyDataSetChanged();		
	}
}
