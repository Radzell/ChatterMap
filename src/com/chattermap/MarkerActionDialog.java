package com.chattermap;

import pl.mg6.android.maps.extensions.Marker;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MarkerActionDialog extends Dialog {
	/**
	 * Listener that a calling activity should implement to receive the results
	 * of the marker finish.
	 */
	public interface MarkerDialogListener {
		void onFinishMarkerDialog(Marker m);
	}
	
	private MarkerDialogListener mListener;
	
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
	public MarkerActionDialog(Activity context, final Marker marker, MarkerDialogListener mdl) {
		super(context);
		mListener = mdl;
		
		// Load the location action layout
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.dialog_list);

		// If the marker is a clustered marker, populate the list, otherwise
		// just display the note
		Marker[] markerList;
		if (marker.isCluster()) {
			markerList = new Marker[marker.getMarkers().size()];
			for (int i = 0; i < marker.getMarkers().size(); ++i) {
				markerList[i] = marker.getMarkers().get(i);
			}
		} else {
			markerList = new Marker[1];
			markerList[0] = marker;
		}

		// Add these snippets to the list
		final ListView lv = (ListView) this.findViewById(R.id.listdialog_list);
		lv.setAdapter(new NoteAdapter(context,
				R.layout.notelist_row, markerList));
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// Update the listener with the clicked item, then exit
				mListener.onFinishMarkerDialog(((NoteAdapter) lv.getAdapter()).getItem(position));
				MarkerActionDialog.this.dismiss();
			}
		});

		setOwnerActivity((Activity) context);
	}
}