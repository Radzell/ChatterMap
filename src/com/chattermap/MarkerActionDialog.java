package com.chattermap;

import java.util.ArrayList;

import pl.mg6.android.maps.extensions.Marker;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MarkerActionDialog extends Dialog {

	/**
	 * Creates a LocationActionDialog that asks the user if they want to share a
	 * note at this location or add this location to list of favorites.
	 * 
	 * @param context
	 *            {@link Context} of the {@link Activity} that created this
	 *            dialog
	 * @param marker
	 *            {@link Marker} that was clicked
	 */
	public MarkerActionDialog(Activity context, final Marker marker) {
		super(context);

		// Load the location action layout
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.dialog_list);

		// If the marker is a clustered marker, populate the list, otherwise
		// just display the note
		ArrayList<String> notes = new ArrayList<String>();
		if (marker.isCluster()) {
			for (Marker m : marker.getMarkers()) {
				notes.add(m.getSnippet());
			}
		} else {
			notes.add(marker.getSnippet());
			System.out.println(marker.getSnippet());
		}

		// Add these snippets to the list
		ListView lv = (ListView) this.findViewById(R.id.listdialog_list);
		String[] notes_array = new String[notes.size()];
		notes.toArray(notes_array);
		for( String s : notes_array )
			Log.d("CONTENTS", s);
		lv.setAdapter(new ArrayAdapter<String>(context,
				android.R.layout.simple_list_item_1, notes_array));

		setOwnerActivity((Activity) context);
	}
}