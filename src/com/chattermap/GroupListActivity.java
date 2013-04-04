package com.chattermap;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class GroupListActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set the layout and initialize:
		setContentView(R.layout.screen_list);
		ListView lv = (ListView) findViewById(R.id.screen_mainlist);

		// TODO: Populate list with groups the user is a member of
		// (I don't know how to do this yet, have fun!)
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate with the edit note action bar items
		getMenuInflater().inflate(R.menu.menu_grouplist, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_addgroup:
			// TODO: Create a group somehow (maybe need a new dialog, I dunno
			// let me know what you need)!
			return true;
		}

		return false;
	}
}
