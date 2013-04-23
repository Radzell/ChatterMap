package com.chattermap;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import pl.mg6.android.maps.extensions.ClusteringSettings;
import pl.mg6.android.maps.extensions.GoogleMap.OnMarkerClickListener;
import pl.mg6.android.maps.extensions.Marker;
import pl.mg6.android.maps.extensions.SupportMapFragment;
import pl.mg6.android.maps.extensions.GoogleMap;
import pl.mg6.android.maps.extensions.GoogleMap.OnMapLongClickListener;

import com.chattermap.entity.ChatGroup;
import com.chattermap.entity.Note;
import com.chattermap.entity.User;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
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
public class MapActivity extends FragmentActivity implements
		OnMapLongClickListener, LocationListener,
		EditNoteDialog.EditNoteDialogListener,
		MarkerActionDialog.MarkerDialogListener {
	ChatGroup mCurrentGroup;
	private GoogleMap mMap;
	private Location mCurrentLocation = null;
	private Timer mUpdateTimer;

	public User mCurrentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_maplayout);

		// Setup the map instance
		// TODO: Graceful exit if map is null (i.e. user doesn't have
		// PlayServices
		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getExtendedMap();
		mMap.setMyLocationEnabled(true);
		mMap.setOnMapLongClickListener(this);
		mMap.setClustering(new ClusteringSettings().iconDataProvider(
				new NoteIconProvider(getResources())).addMarkersDynamically(
				true));
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				// Start a MarkerActionDialog for the clicked marker
				MarkerActionDialog mad = new MarkerActionDialog(
						MapActivity.this, marker, MapActivity.this);
				mad.show();
				return true;
			}
		});
		setupDB();

		// By default, load the Public group to the map
		loadPublicGroup(true);

		// Attempt to retrieve the current location
		setCurrentLocation(LocationUtils.getCurrentLocation(this, this));
		if (getCurrentLocation() != null) {
			// Set the map to look at it
			setMapTarget(getCurrentLocation());
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		boolean updated = savedInstanceState.getBoolean(
				getString(R.string.bundle_update), false);

		// Restore the current camera location
		if (mMap != null && updated) {
			double lat = savedInstanceState.getDouble(
					getString(R.string.bundle_latitude), 0.0);
			double lon = savedInstanceState.getDouble(
					getString(R.string.bundle_longitude), 0.0);
			CameraPosition pos = new CameraPosition.Builder()
					.target(new LatLng(lat, lon))
					.zoom(savedInstanceState
							.getFloat(getString(R.string.bundle_zoom)))
					.tilt(savedInstanceState
							.getFloat(getString(R.string.bundle_tilt)))
					.bearing(
							savedInstanceState
									.getFloat(getString(R.string.bundle_bearing)))
					.build();
			mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Update the map every five minutes
		mUpdateTimer = new Timer();

		// Schedule a trigger that calls the update method, then update the UI
		// in the UI thread to reflect any changes
		mUpdateTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (mCurrentGroup == null) {
					return;
				}

				try {
					MapActivity.this.update(mCurrentGroup);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				MapActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						MapActivity.this.displayNotes();
					}
				});
			}
		}, 0, 300000);
	}

	@Override
	protected void onPause() {
		super.onPause();

		mUpdateTimer.cancel();
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		// Store the current camera location
		if (mMap != null) {
			Log.i("MAP", "Save camera position");
			CameraPosition pos = mMap.getCameraPosition();
			savedInstanceState.putDouble(getString(R.string.bundle_latitude),
					pos.target.latitude);
			savedInstanceState.putDouble(getString(R.string.bundle_longitude),
					pos.target.longitude);
			savedInstanceState.putFloat(getString(R.string.bundle_zoom),
					pos.zoom);
			savedInstanceState.putFloat(getString(R.string.bundle_tilt),
					pos.tilt);
			savedInstanceState.putFloat(getString(R.string.bundle_bearing),
					pos.bearing);
			savedInstanceState.putBoolean(getString(R.string.bundle_update),
					true);
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
		mMap.addMarker(NoteIconProvider.fromNote(note, this));
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
		case R.id.menu_changeuser:
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
			if (mCurrentLocation == null) {
				// Only focus the map on the current location if we didn't
				// already know it
				setMapTarget(location);
			}
			setCurrentLocation(location);
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
		setMapTarget(new LatLng(loc.getLatitude(), loc.getLongitude()));
	}

	/**
	 * Sets the maps camera to focus on the given location. Also sets the zoom
	 * level to an appropriate zoomed in state if a zoom hasn't yet been set.
	 * 
	 * @param loc
	 *            {@link LatLng} to set the camera's position to
	 */
	private void setMapTarget(LatLng location) {
		if (mMap != null) {
			float zoom = mMap.getCameraPosition().zoom;

			// TODO: Should this be if zoom == GoogleMap.getMaxZoomLevel() ?
			// TODO: Zoom 17 was chosen because that's the first zoom level at
			// which buildings can be seen, should this be different?
			zoom = zoom < 17.0f ? 17.0f : zoom;
			CameraUpdate npos = CameraUpdateFactory.newLatLngZoom(location,
					zoom);
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

	@Override
	public void onFinishMarkerDialog(Marker m) {
		if (m == null) {
			return;
		}

		// If a valid Marker was clicked, center the map on it
		this.setMapTarget(m.getPosition());
	}
}
