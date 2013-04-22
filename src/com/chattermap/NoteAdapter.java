package com.chattermap;

import pl.mg6.android.maps.extensions.Marker;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class NoteAdapter extends ArrayAdapter<Marker> {
	Context mContext;
	int mLayoutResourceId;
	Marker mData[] = null;

	/**
	 * ArrayAdapter for Map Markers with notes in them. Currently uses the
	 * simple notelist_row to simply display the body of the note.
	 * 
	 * @param context
	 *            {@link Context} of the {@link Activity} containing the list
	 * @param layoutResourceId
	 *            Id of the layout to use with the list
	 * @param data
	 *            Array of {@link Marker} objects to add to use in the list
	 */
	public NoteAdapter(Context context, int layoutResourceId, Marker[] data) {
		super(context, layoutResourceId, data);
		mLayoutResourceId = layoutResourceId;
		mContext = context;
		mData = data;
	}

	/**
	 * Retrieves the {@link Marker} at the requested position.
	 * 
	 * @param position
	 *            Index of the {@link Marker} to retrieve
	 * @return {@link Marker} at the requested position if the position is not
	 *         out of bounds, null otherwise
	 */
	public Marker getItem(int position) {
		if (position < 0 || position >= mData.length) {
			return null;
		}

		return mData[position];
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		NoteHolder holder = null;

		// Only inflate the row if necessary, otherwise just retrieve it
		if (row == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(mLayoutResourceId, parent, false);

			holder = new NoteHolder();
			holder.txtTitle = (TextView) row.findViewById(R.id.note_text);

			row.setTag(holder);
		} else {
			holder = (NoteHolder) row.getTag();
		}

		// Once we have the row, add the snippet from the marker to it
		Marker marker = mData[position];
		holder.txtTitle.setText(marker.getSnippet());

		return row;
	}

	static class NoteHolder {
		TextView txtTitle;
	}
}
