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
	private static final int[] res = { R.drawable.l1, R.drawable.l1b,
			R.drawable.l2, R.drawable.l2b, R.drawable.l3, R.drawable.l3b,
			R.drawable.l4, R.drawable.l4b, R.drawable.l5, R.drawable.l5b };

	private static final int[] forCounts = { 2, 5, 10, 25, 50, 75, 100, 250,
			1000, Integer.MAX_VALUE };

	private Bitmap[] baseBitmaps;

	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Rect bounds = new Rect();

	// Anchor point is at (x,y) = (0.5,1.0), at the bottom middle
	private MarkerOptions markerOptions = new MarkerOptions()
			.anchor(0.5f, 1.0f);

	/**
	 * Constructor for the NoteIconProvider that initializes the basic paints
	 * and bitmaps to use for the icons.
	 * 
	 * @param resources
	 *            {@link Resources} for the provider to use to construct bitmaps
	 */
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
				(R.drawable.notecallout));
		int width = baseIcon.getWidth();
		int height = baseIcon.getHeight();
		Bitmap bmp = Bitmap.createBitmap(width, height, conf);

		// Create a canvas to draw into the Bitmap with
		Canvas c = new Canvas(bmp);

		// Draw the callout and note count
		Paint textPaint = new Paint();
		int fontSize = context.getResources().getDimensionPixelSize(
				R.dimen.markerFontSize);
		textPaint.setTextSize(fontSize);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setColor(Color.BLACK);
		c.drawBitmap(baseIcon, 0, 0, textPaint);

		// Draw the text on the canvas
		int lines = 0, index = 0;

		// Split the body of the note into words
		String[] words = note.getBody().split(" ");

		// Make 3 lines of text
		while (lines < 3) {

			// Add as many words as will fit onto each line
			String line = "";
			while (index < words.length
					&& textPaint.measureText(line + " " + words[index]) < width) {
				line += " " + words[index];
				++index;
			}

			// If content didn't fit on the last line, add an ellipsis
			++lines;
			if (lines == 3 && index < words.length) {
				if (line.length() > 3) {
					line = line.substring(0, line.length() - 3) + "...";
				} else {
					line = "...";
				}
			}
			c.drawText(line, width / 2, lines * fontSize, textPaint);
		}

		// Add the bitmap to the MarkerOptions, set the anchor point, and return
		options.icon(BitmapDescriptorFactory.fromBitmap(bmp));
		options.anchor(0.5f, 1.0f).snippet(note.getBody());

		return options;
	}
}
