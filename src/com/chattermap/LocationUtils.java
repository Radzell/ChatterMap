package com.chattermap;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

/**
 * Utility class for location based operations like getting current location,
 * accessing LocationServices, and prompting the user to enable higher accuracy
 * precision on their device.
 */
public class LocationUtils {

	/**
	 * Retrieves the current last known location and setup location listeners so
	 * that location is constantly monitored.
	 * 
	 * @param activity
	 *            {@link Activity} that is calling this function. Used to get
	 *            location manager and to show dialogs
	 * @param loc_listener
	 *            {@link LocationListener} that will listen for current location
	 *            after this function is called
	 * @return Best last known {@link Location} object, or null if no providers
	 *         were found or there are no last known locations
	 */
	public static Location getCurrentLocation(Activity activity,
			LocationListener loc_listener) {
		// Get the location manager
		LocationManager locationManager = (LocationManager) activity
				.getSystemService(Context.LOCATION_SERVICE);

		// Get the location providers that satisfy certain criteria
		// TODO: require a certain level of accuracy from the providers?
		Criteria criteria = new Criteria();
		List<String> providers = locationManager.getProviders(criteria, true);

		Log.d("LOCATIONSERVICES", String.valueOf(providers.size()));
		if (providers != null && providers.size() > 0) {
			// Get the most recent last known location, and set the map to view
			// it
			Location newestLocation = new Location(providers.get(0));
			newestLocation.setTime(0L);

			for (String provider : providers) {
				Location location = locationManager
						.getLastKnownLocation(provider);
				if (location != null
						&& location.getTime() > newestLocation.getTime()) {
					newestLocation = location;
				}

				// Request an update to the location with this as the listener
				locationManager.requestLocationUpdates(provider, 0, 0,
						loc_listener);
			}
			return newestLocation;
		}

		// TODO: Should this be here, or should it be up to us whether or not
		// to show the dialog.
		showLocationDialog(activity);
		return null;
	}

	/**
	 * Sets up and displays a {@link LocationDialogFragment} to ask the user to
	 * navigate to their location access settings to enable location services so
	 * that the app can access their location.
	 * 
	 * @param activity
	 *            {@link Activity} to launch the Dialog from
	 */
	public static void showLocationDialog(Activity activity) {
		// If no providers were returned, ask user to turn on location services
		LocationDialogFragment dialog = new LocationDialogFragment();
		dialog.show(activity.getFragmentManager(),
				LocationDialogFragment.class.getName());
	}
}
