package com.chattermap;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.chattermap.entity.ChatGroup;
import com.chattermap.entity.Note;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Model;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MapActivity extends Activity implements OnMapLongClickListener {
	ChatGroup mCurrentGroup;
	private GoogleMap mMap;
	private Location mCurrentLocation = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.screen_maplayout);
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		mMap.setOnMapLongClickListener(this);
		setupDB();
		loadPublicGroup(true);
	}

	private void saveNote(final String title, final String body, final int lat,
			final int longit) {
		Note.create(title, body, lat, longit, mCurrentGroup);
	}

	private void loadPublicGroup(boolean b) {
		final ProgressDialog pd = ProgressDialog.show(MapActivity.this,
				"Loading...", "Working");
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					loadPublicGroup();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				saveNote("Second Note title", "This is our second Note", 0, 0);
				pd.dismiss();
			};

		}.execute();

	}

	/**
	 * Loads the initial public group
	 * 
	 * @throws ParseException
	 */
	private void loadPublicGroup() throws ParseException {
		ChatGroup mGroup = ChatGroup.findByName("Public", getApplicationContext());
		if (mGroup == null) {
			final ParseQuery query = new ParseQuery("Group");
			query.whereEqualTo("Name", "Public");
			ParseObject ob = query.getFirst();
			Log.i("TAG", ob.getObjectId());
			Log.i("TAG", ob.getString("Name"));
			Log.i("TAG", ob.getString("Description"));

			ChatGroup group = new ChatGroup();
			group.setObjectID(ob.getObjectId());
			group.setName(ob.getString("Name"));
			group.setDescription(ob.getString("Description"));
			group.save(MapActivity.this);
			mCurrentGroup = group;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_maplayout, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureid, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			// If no current location, show an error message and do nothing
			if (mCurrentLocation == null) {
				Toast.makeText(
						getApplicationContext(),
						"Haven't been able "
								+ "to obtain current location yet!",
						Toast.LENGTH_SHORT).show();
			} else {
				// open a dialog for an action to perform at current location
				/*
				 * LocationActionDialog lad = new LocationActionDialog(
				 * MapActivity.this, mCurrentLocation.getLatitude(),
				 * mCurrentLocation.getLongitude()); lad.show();
				 */
			}
			return true;
		}
		return false;
	}

	/**
	 * Updates the notes for projects in the databases Call in background only
	 * 
	 * @throws ParseException
	 */
	public void update() throws ParseException {
		// Getting the group from the database
		List<ChatGroup> groups = ChatGroup.getGroups(this).all().toList();

		for (final ChatGroup group : groups) {
			// Find all notes by the current group
			final ParseQuery query = new ParseQuery("Note");
			query.whereEqualTo("objectid", group.getObjectID());
			query.findInBackground(new FindCallback() {

				@Override
				public void done(List<ParseObject> objects, ParseException e) {
					if (e == null) {
						List<ParseObject> notes = objects;
						saveNotes(notes, group);
					}
				}
			});

		}
	}

	/**
	 * Updates the notes for projects in the databases Call in background only
	 * 
	 * @throws ParseException
	 */
	public void update(final ChatGroup group) throws ParseException {
		// Getting the group from the database

		// Find all notes by the current group
		final ParseQuery query = new ParseQuery("Note");
		query.whereEqualTo("objectid", group.getObjectID());
		query.findInBackground(new FindCallback() {

			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) {
					List<ParseObject> notes = objects;
					saveNotes(notes, group);
				}
			}
		});

	}

	/**
	 * Save notes to a group
	 * 
	 * @param notes
	 *            list of notes
	 * @param group
	 *            that the notes belong to
	 */
	private void saveNotes(List<ParseObject> notes, ChatGroup group) {
		for (ParseObject po : notes) {
			Note note = new Note();
			note.setID(po.getObjectId());
			note.setGroup(group);
			note.setTitle(po.getString("Title"));
			note.setBody(po.getString("Body"));
		}
	}

	/**
	 * Sets up the background database for Groups and Notes
	 */
	private void setupDB() {
		DatabaseAdapter.setDatabaseName("chattermapdb");

		List<Class<? extends Model>> models = new ArrayList<Class<? extends Model>>();
		models.add(ChatGroup.class);
		models.add(Note.class);
		DatabaseAdapter adapter = DatabaseAdapter.getInstance(this);
		adapter.setModels(models);
	}

	@Override
	public void onMapLongClick(LatLng loc) {
		// TODO: Use the stored activity to launch the "choose action" dialog
		Log.d("LONG PRESS", "Long press at: (" + loc.latitude + ", "
				+ loc.longitude + ")");
		LocationActionDialog lad = new LocationActionDialog(this,
				mCurrentGroup, loc.latitude, loc.longitude);
		lad.show();
	}
}
