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

	public NoteAdapter(Context context, int layoutResourceId, Marker[] data) {
		super(context, layoutResourceId, data);
		mLayoutResourceId = layoutResourceId;
		mContext = context;
		mData = data;
	}

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

		if (row == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(mLayoutResourceId, parent, false);

			holder = new NoteHolder();
			holder.txtTitle = (TextView) row.findViewById(R.id.note_text);

			row.setTag(holder);
		} else {
			holder = (NoteHolder) row.getTag();
		}

		Marker marker = mData[position];
		holder.txtTitle.setText(marker.getSnippet());

		return row;
	}

	static class NoteHolder {
		TextView txtTitle;
	}
}
