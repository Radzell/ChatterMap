package com.chattermap;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.model.LatLng;

public class MapScreenLongClickListener implements OnMapLongClickListener {
	private Activity mActivity;
	
	/**
	 * Constructs a MapScreenLongClickListener for the {@link Activity} passed in. 
	 * @param activity {@link activity} that this listener is created from
	 */
	public MapScreenLongClickListener( Activity activity ) {
		mActivity = activity;
	}
	
	/**
	 * Launches a choose location action from the stored activity using the 
	 * location passed into the listener. 
	 */
	@Override
	public void onMapLongClick(LatLng loc) {
		// TODO: Use the stored activity to launch the "choose action" dialog
		Log.d("LONG PRESS", "Long press at: (" + loc.latitude + ", " + 
				loc.longitude + ")");
		LocationActionDialog lad = new LocationActionDialog(mActivity, loc.latitude, loc.longitude);
		lad.show();
	}
}
