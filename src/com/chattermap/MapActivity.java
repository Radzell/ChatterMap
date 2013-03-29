package com.chattermap;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chattermap.entity.Group;
import com.chattermap.entity.Note;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.orm.androrm.DatabaseAdapter;
import com.orm.androrm.Model;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MapActivity extends Activity implements LocationListener {
	private GoogleMap mMap;
	private Location mCurrentLocation = null;
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_maplayout);
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.setOnMapLongClickListener(new MapScreenLongClickListener(this));
		setupDB();
		createTestGroup();
		createTestNoteInGroup();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setMapTarget( LocationUtils.getCurrentLocation(this,this) );
	}

	private void createTestNoteInGroup() {
		// TODO Auto-generated method stub

	}

	/**
	 * Test for creating public group.
	 * 
	 * @throws ParseException
	 */
	private void createTestGroup() {
		// Create public group is not created
		final ParseQuery query = new ParseQuery("Group");
		query.whereEqualTo("Name", "Public");
		query.getFirstInBackground(new GetCallback() {

			@Override
			public void done(ParseObject object, ParseException e) {
				if (e == null) {

					Log.i("ChatterMap", "Public Exist");
				} else {
					ParseObject publicGroup = new ParseObject("Group");
					publicGroup.put("Name", "Public");
					publicGroup.put("Description", "Group For Public Use");
					publicGroup.saveInBackground();
					Log.i("ChatterMap", "Public Created");
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_maplayout, menu);
		
		// Add a listener to the add button to open a dialog for an action to 
		// perform at current location
		MenuItem shareButton = (MenuItem) menu.findItem(R.id.menu_add);
		shareButton.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// If no current location, show an error message and do nothing
				if( mCurrentLocation == null ) {
					Toast.makeText(getApplicationContext(), "Haven't been able " +
							"to obtain current location yet!", Toast.LENGTH_SHORT).show();
				} else {
					LocationActionDialog lad = new LocationActionDialog(MapActivity.this, 
							mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
					lad.show();
				}
				return true;
			}
		});
		return true;
	}

	/**
	 * Updates the notes for projects in the databases Call in background only
	 * 
	 * @throws ParseException
	 */
	public void update() throws ParseException {
		// Getting the group from the database
		List<Group> groups = Group.getGroups(this).all().toList();

		for (final Group group : groups) {
			// Find all notes by the current group
			final ParseQuery query = new ParseQuery("Note");
			query.whereEqualTo("objectid", group.getID());
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
	 * Save notes to a group
	 * 
	 * @param notes
	 *            list of notes
	 * @param group
	 *            that the notes belong to
	 */
	private void saveNotes(List<ParseObject> notes, Group group) {
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
		models.add(Group.class);
		models.add(Note.class);
		DatabaseAdapter adapter = DatabaseAdapter.getInstance(this);
		adapter.setModels(models);
	}

	@Override
	public void onLocationChanged(Location location) {
		if( location == null ) return;
		if( mCurrentLocation == null ) mCurrentLocation = location;
		
		// If the new update is more accurate, update the current location
		if( location.getAccuracy() < mCurrentLocation.getAccuracy() ) {
			mCurrentLocation = location;
			setMapTarget(location);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Sets the maps camera to focus on the given location.  Also sets the zoom level
	 * to an appropriate zoomed in state if a zoom hasn't yet been set.
	 * @param loc {@link Location} to set the camera's position to
	 */
	private void setMapTarget( Location loc ) {
		if(mMap != null ) {
			float zoom = mMap.getCameraPosition().zoom;
			
			// TODO: Should this be if zoom == GoogleMap.getMaxZoomLevel() ?
			// TODO: Zoom 17 was chosen because that's the first zoom level at
			//        which buildings can be seen, should this be different?
			zoom = zoom < 17.0f ? 17.0f : zoom;
			CameraUpdate npos = CameraUpdateFactory.newLatLngZoom(
					new LatLng(loc.getLatitude(), loc.getLongitude()), zoom);
			mMap.moveCamera(npos);
		}
	}
}
