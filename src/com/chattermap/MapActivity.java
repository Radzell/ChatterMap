package com.chattermap;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.chattermap.entity.User;
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

/**
 * Primary {@link Activity} of the application. Displays a Google Maps instance
 * with notes for the users viewing pleasure. Also allows the addition of new
 * notes through a long press or menu action.
 */
public class MapActivity extends Activity implements OnMapLongClickListener,
		LocationListener, EditNoteDialog.EditNoteDialogListener {
	ChatGroup mCurrentGroup;
	private GoogleMap mMap;
	private Location mCurrentLocation = null;

	public User mCurrentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_maplayout);

		// Setup the map instance
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		mMap.setMyLocationEnabled(true);
		mMap.setOnMapLongClickListener(this);
		setupDB();

		// By default, load the Public group to the map
		loadPublicGroup(true);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Attempt to retrieve the current location
		setCurrentLocation(LocationUtils.getCurrentLocation(this, this));
		if (getCurrentLocation() != null) {
			// Set the map to look at it
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

		// If the note doesn't have a title, use the body as the info window
		// title
		if (note.getTitle().length() == 0) {
			mMap.addMarker(new MarkerOptions().position(loc).title(
					note.getBody()));
		} else {
			mMap.addMarker(new MarkerOptions().position(loc)
					.title(note.getTitle()).snippet(note.getBody()));
		}
	}

	/**
	 * Retrieves the current group being viewed by the user.
	 * 
	 * @return {@link ChatGroup} currently being viewed
	 */
	public ChatGroup getCurrentGroup() {
		return mCurrentGroup;
	}

	/**
	 * Connects to the Parse Cloud to retrieve notes from the Public group and
	 * add them to the users local database. Starts a new AsyncTask to do
	 * network I/O asynchronously.
	 */
	private void loadPublicGroup(boolean b) {
		final ProgressDialog pd = ProgressDialog.show(MapActivity.this,
				"Loading...", "Working");
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					loadPublicGroup();
					update();

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				try {
					update();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				displayNotes();
				pd.dismiss();
			};

		}.execute();

	}

	/**
	 * Retrieves all locally saved notes for the current group and populates the
	 * map with them.
	 */
	public void displayNotes() {
		List<Note> notes = mCurrentGroup.getNotes(getApplicationContext())
				.toList();
		Log.i("GROUP", "Displaying group: \"" + mCurrentGroup.getName()
				+ "\" with " + String.valueOf(notes.size()) + " notes");

		// Clear the map so we don't get duplicates or have leftover notes
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

		String user = "";
		String group = "";
		if (mCurrentGroup != null)
			group = mCurrentGroup.getName();
		if (mCurrentUser != null) {
			user = mCurrentUser.mEmail;
		} else {
			user = "Guest";
		}
		menu.add("User: " + user);
		menu.add("Group: " + group);

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
		case R.id.menu_refresh:
			// Attempt to refresh map
			if (mCurrentGroup != null) {
				try {
					update();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			return true;
		case R.id.menu_settings:
			LoginDialog log = new LoginDialog();
			log.show(getFragmentManager(), "Test");
			return true;
		case R.id.menu_changegroup:
			ChatGroupDialog chatdialog = new ChatGroupDialog();
			chatdialog.show(getFragmentManager(), "Test");
			Toast.makeText(this, "Change Group", Toast.LENGTH_SHORT).show();
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
		final ParseQuery groupquery = new ParseQuery("Group");
		groupquery.findInBackground(new FindCallback() {

			@Override
			public void done(List<ParseObject> objects, ParseException e) {

				if (e == null) {
					List<ParseObject> groups = objects;
					for (ParseObject po : groups)
						saveGroup(po);
				}

			}
		});
	}

	private void saveGroup(ParseObject group) {
		if (ChatGroup.getGroups(this)
				.filter(new Filter().is("mObjectID", group.getObjectId()))
				.isEmpty()) {
			ChatGroup g = new ChatGroup();
			g.setName(group.getString("Name"));
			g.setDescription(group.getString("Description"));
			g.setUser(group.getString("User"));
			g.setObjectID(group.getObjectId());
			g.save(this);
			try {
				update(g);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	 * Saves notes from the Parse Cloud to the local database.
	 * 
	 * @param notes
	 *            List of notes received from the Parse Cloud
	 * @param group
	 *            {@link ChatGroup} that the notes belong to
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

	/**
	 * Sets up the local background database for Groups and Notes
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
		// Launch a LocationActionDialog to do for this location
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

	// Required extra unused methods from LocationListener
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

	@Override
	public void onFinishEditDialog(int result) {
		// Called when the EditNoteDialog is finished
		switch (result) {
		case RESULT_OK:
			try {
				// On an "OK", a note was added, update the map
				update(mCurrentGroup);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	public void updateMenu() {
		invalidateOptionsMenu();
		
	}
}
