package com.chattermap;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.chattermap.entity.ChatGroup;
import com.chattermap.entity.Note;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Filter;
import com.orm.androrm.Model;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MapActivity extends Activity implements OnMapLongClickListener,
		LocationListener {
	ChatGroup mCurrentGroup;
	private GoogleMap mMap;
	private Location mCurrentLocation = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.screen_maplayout);
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		mMap.setMyLocationEnabled(true);
		mMap.setOnMapLongClickListener(this);
		setupDB();
		loadPublicGroup(true);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Attempt to retrieve the current location
		setCurrentLocation(LocationUtils.getCurrentLocation(this, this));
		if (getCurrentLocation() != null) {
			setMapTarget(getCurrentLocation());
		}
	}

	/**
	 * Adds a give note to the map with associated marker and InfoWindow
	 * contents.
	 * 
	 * @param note
	 *            {@link Note} object to add to the map
	 */
	private void addNoteToMap(Note note) {
		LatLng loc = new LatLng(note.getLocation().getLatitude(), note
				.getLocation().getLongitude());
		Log.i("MAPNOTE",
				"Title: \"" + note.getTitle() + "\" Body:\"" + note.getBody()
						+ "\" at (" + String.valueOf(loc.latitude) + ","
						+ String.valueOf(loc.longitude) + ")");
		mMap.addMarker(new MarkerOptions().position(loc).title(note.getBody()));
	}

	private void saveNote(final String title, final String body,
			final double lat, final double longit) {
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
					update(mCurrentGroup);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				try {
					update(mCurrentGroup);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				displayNotes();
				pd.dismiss();
			};

		}.execute();

	}

	// Called when ready to display notes
	private void displayNotes() {
		List<Note> notes = mCurrentGroup.getNotes(getApplicationContext())
				.toList();
		Log.i("GROUP", "Displaying group: \"" + mCurrentGroup.getName()
				+ "\" with " + String.valueOf(notes.size()) + " notes");
		mMap.clear();
		for (Note note : notes) {
			addNoteToMap(note);
		}
	}

	/**
	 * Loads the initial public group
	 * 
	 * @throws ParseException
	 */
	private void loadPublicGroup() throws ParseException {
		ChatGroup group = ChatGroup.findByName("Public",
				getApplicationContext());
		if (group == null) {
			final ParseQuery query = new ParseQuery("Group");
			query.whereEqualTo("Name", "Public");
			ParseObject ob = query.getFirst();
			Log.i("TAG", ob.getObjectId());
			Log.i("TAG", ob.getString("Name"));
			Log.i("TAG", ob.getString("Description"));

			group = new ChatGroup();
			group.setObjectID(ob.getObjectId());
			group.setName(ob.getString("Name"));
			group.setDescription(ob.getString("Description"));
			group.save(MapActivity.this);
		}
		mCurrentGroup = group;
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
			if (getCurrentLocation() == null) {
				Toast.makeText(
						getApplicationContext(),
						"Haven't been able "
								+ "to obtain current location yet!",
						Toast.LENGTH_SHORT).show();
			} else {
				// open a dialog for an action to perform at current location
				LocationActionDialog lad = new LocationActionDialog(
						MapActivity.this, mCurrentGroup, getCurrentLocation()
								.getLatitude(), getCurrentLocation()
								.getLongitude());
				lad.show();
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
			query.whereEqualTo("parent", group.getObjectID());
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
		query.findInBackground(new FindCallback() {

			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) {
					List<ParseObject> notes = objects;

					saveNotes(notes, group);
					displayNotes();
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
			if (group.getNotes(this)
					.filter(new Filter().is("mObjectID", po.getObjectId()))
					.isEmpty()) {
				Log.i("Test", "empty");
				Note note = new Note();
				note.setID(po.getObjectId());
				note.setGroup(group);
				note.setTitle(po.getString("mTitle"));
				note.setBody(po.getString("mBody"));
				Location lo = new Location("GPS");
				lo.setLatitude(po.getDouble("mLat"));
				lo.setLongitude(po.getDouble("mLongit"));
				note.setLocation(lo);
				note.save(MapActivity.this);
			} else {
				Log.i("Test", "not empty");
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			try {
				update(mCurrentGroup);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Toast.makeText(this, "Ok", Toast.LENGTH_SHORT).show();
			break;
		case RESULT_CANCELED:
			// Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
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

	@Override
	public void onLocationChanged(Location location) {
		if (location == null)
			return;

		// If the new update is more accurate, update the current location
		if (getCurrentLocation() == null
				|| location.getAccuracy() < getCurrentLocation().getAccuracy()) {
			setCurrentLocation(location);
			setMapTarget(location);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/**
	 * Sets the maps camera to focus on the given location. Also sets the zoom
	 * level to an appropriate zoomed in state if a zoom hasn't yet been set.
	 * 
	 * @param loc
	 *            {@link Location} to set the camera's position to
	 */
	private void setMapTarget(Location loc) {
		if (mMap != null) {
			float zoom = mMap.getCameraPosition().zoom;

			// TODO: Should this be if zoom == GoogleMap.getMaxZoomLevel() ?
			// TODO: Zoom 17 was chosen because that's the first zoom level at
			// which buildings can be seen, should this be different?
			zoom = zoom < 17.0f ? 17.0f : zoom;
			CameraUpdate npos = CameraUpdateFactory.newLatLngZoom(new LatLng(
					loc.getLatitude(), loc.getLongitude()), zoom);
			mMap.moveCamera(npos);
		}
	}

	/**
	 * Retrieves the applications idea of the user's current location
	 * 
	 * @return {@link Location} object representing the user's current location
	 */
	public Location getCurrentLocation() {
		return mCurrentLocation;
	}

	/**
	 * Sets the current {@link Location} to a new location and update the map's
	 * current location marker as well.
	 * 
	 * @param loc
	 *            New current {@link Location}
	 */
	public void setCurrentLocation(Location loc) {
		mCurrentLocation = loc;
	}
}
