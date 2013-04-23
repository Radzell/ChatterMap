package com.chattermap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chattermap.entity.ChatGroup;
import com.parse.ParseException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ChatGroupDialog extends DialogFragment {
	private EditText mEditText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		int style = DialogFragment.STYLE_NORMAL, theme = 0;
		setStyle(style, theme);
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set listener for a view
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View mainview = inflater.inflate(R.layout.dialog_changegroup, null);
		mEditText = (EditText) mainview.findViewById(R.id.changegroup_edittext);
		ListView lv = (ListView) mainview
				.findViewById(R.id.changegroup_listview);

		// Set the adapter
		final List<ChatGroup> chatgroups = ChatGroup.getGroups(getActivity())
				.all().toList();
		ArrayList<Map<String, String>> chatgroupnames = new ArrayList<Map<String, String>>();
		for (ChatGroup group : chatgroups) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("name", group.getName());
			map.put("one", group.getName());
			chatgroupnames.add(map);
			Log.i("TAG", group.getName());
		}

		String[] from = { "name", "one" };
		int[] to = { android.R.id.text1, android.R.id.text2 };
		lv.setAdapter(new SimpleAdapter(getActivity(), chatgroupnames,
				android.R.layout.simple_list_item_1, from, to));
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MapActivity act = (MapActivity) getActivity();
				act.mCurrentGroup = chatgroups.get(position);
				act.updateMenu();
				act.displayNotes();
				getDialog().dismiss();
			}
		});
		// Set the layout and initialize:
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(mainview);
		// Add action buttons
		builder.setPositiveButton("Create Group",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						onPositiveClick(dialog, id);
					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// onNegativeClick(dialog, id);
					}
				});
		return builder.create();
	}

	protected void onPositiveClick(DialogInterface dialog, int id) {
		if (mEditText != null && mEditText.getText().length() > 0) {
			String user = "";
			MapActivity act = (MapActivity) getActivity();

			if (act.mCurrentUser != null)
				user = act.mCurrentUser.mEmail_hash;
			ChatGroup.createChatGroup(mEditText.getText().toString(), "", user);
			try {
				act.update();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
