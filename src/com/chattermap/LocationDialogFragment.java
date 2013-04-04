package com.chattermap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

/**
 * DialogFragment to politely ask the user to turn on their location services or
 * enable a higher accuracy provider.
 */
public class LocationDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build an AlertDialog with positive/negative buttons
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Set the title and initialize the buttons
		builder.setMessage(R.string.location_dialog_message)
				.setPositiveButton(R.string.location_dialog_positive_button,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// On a positive click, go to location settings
								Intent settingsIntent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(settingsIntent);
							}
						})
				.setNegativeButton(R.string.location_dialog_negative_button,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// On a negative click, cancel with a helpful
								// message
								Toast.makeText(getActivity(),
										R.string.no_location_message,
										Toast.LENGTH_LONG).show();
							}
						});

		return builder.create();
	}
}