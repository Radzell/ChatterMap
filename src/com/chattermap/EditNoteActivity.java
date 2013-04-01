package com.chattermap;

import com.chattermap.entity.ChatGroup;
import com.chattermap.entity.Note;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class EditNoteActivity extends Activity {
	private double mLat, mLong;
	private String mId = null;
	private String mGroupId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the location and id
		Intent intent = getIntent();
		mLat = intent
				.getDoubleExtra(getString(R.string.editnote_latitude), 0.0);
		mLong = intent.getDoubleExtra(getString(R.string.editnote_longitude),
				0.0);
		mId = intent.getStringExtra(getString(R.string.editnote_id));
		mGroupId = intent.getStringExtra("groupid");

		if (mId == "empty") {
			// TODO: Generate new ID for this note
		}

		// Set the layout and initialize:
		setContentView(R.layout.screen_editnote);

		// TODO: Setup spinner id/share_spinner with sharing options
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate with the edit note action bar items
		getMenuInflater().inflate(R.menu.menu_editnote, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_submit:
			// TODO: Submit the note / queue the note
			EditText et = (EditText) findViewById(R.id.screen_editnote_edittext);
			if (et.length() > 0) {
				ChatGroup group = ChatGroup.findByID(mGroupId, this);
				Note.create("", et.getText().toString(), mLat, mLong, group);
			}
			// Finish the activity!
			// this.finish();
			break;
		case R.id.menu_attach:
			// TODO: Open dialog to attach a picture
			break;
		}

		return true;
	}
}
