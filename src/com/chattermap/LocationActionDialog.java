package com.chattermap;

import com.chattermap.entity.ChatGroup;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

/**
 * Dialog to ask a user what action they would like to perform at a particular
 * location, either share a note or add the location to their favorites.
 */
public class LocationActionDialog extends Dialog {
	private double mLat, mLong;
	private final Activity mContext;

	/**
	 * Creates a LocationActionDialog that asks the user if they want to share a
	 * note at this location or add this location to list of favorites.
	 * 
	 * @param context
	 *            {@link Context} of the {@link Activity} that created this
	 *            dialog
	 * @param group
	 * @param latitude
	 *            Latitude of the location of the action
	 * @param longitude
	 *            Longitude of the location of the action
	 */
	public LocationActionDialog(Activity context, final ChatGroup group,
			double latitude, double longitude) {
		super(context);

		// Grab the latitude, longitude, and context
		mLat = latitude;
		mLong = longitude;
		mContext = context;

		// Load the location action layout
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.dialog_locationaction);

		setOwnerActivity((Activity) context);
		// TODO: Change text if current location or "other" location
		// Add listeners to the buttons
		Button shareButton = (Button) this.findViewById(R.id.dialog_sharenote);
		Button favButton = (Button) this.findViewById(R.id.dialog_addfavorite);
		shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Start the edit note dialog
				EditNoteDialog editdialog = new EditNoteDialog();
				
				// Pass along latitude and longitude of the click
				Bundle args = new Bundle();
				args.putDouble(mContext.getString(R.string.bundle_latitude),
						mLat);
				args.putDouble(mContext.getString(R.string.bundle_longitude),
						mLong);
				editdialog.setArguments(args);

				editdialog.show(getOwnerActivity().getFragmentManager(),
						"editnote");
				LocationActionDialog.this.dismiss();
			}
		});
		favButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Add this location to favorites
				Toast.makeText(
						mContext,
						mContext.getString(R.string.locationaction_toast_add_location),
						Toast.LENGTH_SHORT).show();
				LocationActionDialog.this.dismiss();
			}
		});
	}
}
