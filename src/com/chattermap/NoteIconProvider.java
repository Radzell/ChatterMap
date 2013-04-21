package com.chattermap;

import pl.mg6.android.maps.extensions.ClusteringSettings.IconDataProvider;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;

import com.chattermap.entity.Note;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class NoteIconProvider implements IconDataProvider {
	private static final int[] res = { R.drawable.l1, R.drawable.l2,
			R.drawable.l3, R.drawable.l4, R.drawable.l5 };

	private static final int[] forCounts = { 10, 100, 1000, 10000,
			Integer.MAX_VALUE };

	private Bitmap[] baseBitmaps;

	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Rect bounds = new Rect();

	// Anchor point is at (x,y) = (0.5,1.0), at the bottom middle
	private MarkerOptions markerOptions = new MarkerOptions()
			.anchor(0.5f, 1.0f);

	public NoteIconProvider(Resources resources) {
		// Retrieve the bitmaps for use with each level of clustering
		baseBitmaps = new Bitmap[res.length];
		for (int i = 0; i < res.length; i++) {
			baseBitmaps[i] = BitmapFactory.decodeResource(resources, res[i]);
		}

		// Initialize the paint for drawing the text on the callouts
		paint.setColor(Color.BLACK);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(50);
	}

	@Override
	public MarkerOptions getIconData(int markersCount) {
		// First, figure out what level of clustering this marker is currently
		// at
		Bitmap base;
		int i = 0;
		do {
			base = baseBitmaps[i];
		} while (markersCount >= forCounts[i++]);

		// Construct the bitmap from the level of clustering, and draw in the
		// number
		Bitmap bitmap = base.copy(Config.ARGB_8888, true);

		String text = String.valueOf(markersCount);
		paint.getTextBounds(text, 0, text.length(), bounds);
		float x = bitmap.getWidth() / 2.0f;
		float y = 3.0f * bitmap.getHeight() / 5.0f;

		Canvas canvas = new Canvas(bitmap);
		canvas.drawText(text, x, y, paint);

		// Construct the marker options from the bitmap and return
		BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
		return markerOptions.icon(icon);
	}

	/**
	 * Constructs and returns the base {@link MarkerOptions} for an unclustered
	 * note.
	 * 
	 * @param note
	 *            {@link Note} to construct the marker from
	 * @return {@link MarkerOptions} with the note's content, location, and icon
	 */
	public static MarkerOptions fromNote(Note note, Context context) {
		LatLng loc = new LatLng(note.getLocation().getLatitude(), note
				.getLocation().getLongitude());

		// Construct the base marker options, adding in location and contents
		MarkerOptions options = new MarkerOptions();
		options.position(loc).title(note.getTitle()).snippet(note.getBody());

		// Now construct the base icon
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;

		// Create a bitmap with the given size to draw into
		Bitmap baseIcon = BitmapFactory.decodeResource(context.getResources(),
				(R.drawable.l1));
		int size = baseIcon.getWidth();
		Bitmap bmp = Bitmap.createBitmap(size, size, conf);

		// Create a canvas to draw into the Bitmap with
		Canvas c = new Canvas(bmp);

		// Draw the callout and note count
		Paint color = new Paint();
		color.setTextSize(((float) size) / 5.0f);
		color.setTextAlign(Align.CENTER);
		color.setColor(Color.BLACK);
		c.drawBitmap(BitmapFactory.decodeResource(context.getResources(),
				(R.drawable.l1)), 0, 0, color);
		c.drawText(String.valueOf(1), size / 2, 3 * size / 5, color);

		// Add the bitmap to the MarkerOptions, set the anchor point, and return
		options.icon(BitmapDescriptorFactory.fromBitmap(bmp));
		options.anchor(0.5f, 1.0f).snippet(note.getBody());

		return options;
	}
}
