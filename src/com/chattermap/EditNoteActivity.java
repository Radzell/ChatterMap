package com.chattermap;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class EditNoteActivity extends Activity {
	private double mLat, mLong;
	private long mId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the location and id
		mLat = savedInstanceState.getDouble(getString(R.string.editnote_latitude));
		mLong = savedInstanceState.getDouble(getString(R.string.editnote_longitude));
		mId = savedInstanceState.getLong(getString(R.string.editnote_id, -1));
		if( mId == -1 ) {
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
	public boolean onOptionsItemSelected (MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_submit:
			// TODO: Submit the note / queue the note
			
			// Finish the activity!
			this.finish();
			break;
		case R.id.menu_attach:
			// TODO: Open dialog to attach a picture
			break;
		}
				
		return true;
	}
}
