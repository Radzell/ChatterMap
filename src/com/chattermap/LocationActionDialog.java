package com.chattermap;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class LocationActionDialog extends Dialog {
	private double mLat, mLong;
	private final Context mContext;
	
	/**
	 * Creates a LocationActionDialog that asks the user if they want to share 
	 * a note at this location or add this location to list of favorites.
	 * @param context {@link Context} of the {@link Activity} that created this dialog
	 * @param latitude Latitude of the location of the action
	 * @param longitude Longitude of the location of the action
	 */
	public LocationActionDialog(Context context, double latitude, double longitude) {
		super(context);
		
		// Grab the latitude, longitude, and context
		mLat = latitude;
		mLong = longitude;
		mContext = context;
		
		// Load the location action layout
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.dialog_locationaction);
		
		// TODO: Change text if current location or "other" location
		// Add listeners to the buttons
		Button shareButton = (Button) this.findViewById(R.id.dialog_sharenote);
		Button favButton   = (Button) this.findViewById(R.id.dialog_addfavorite);
		shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Start the edit note activity
				Intent editIntent = new Intent(mContext, EditNoteActivity.class);
				editIntent.putExtra(mContext.getString(R.string.editnote_latitude), mLat);
				editIntent.putExtra(mContext.getString(R.string.editnote_longitude), mLong);
				editIntent.putExtra(mContext.getString(R.string.editnote_id), -1L);
				mContext.startActivity(editIntent);
				
				LocationActionDialog.this.dismiss();
			}
		});
		favButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Add this location to favorites 
				Toast.makeText(mContext, mContext.getString(R.string.locationaction_toast_add_location), 
						Toast.LENGTH_SHORT).show();
				LocationActionDialog.this.dismiss();
			}
		});
	}
	
	

}
