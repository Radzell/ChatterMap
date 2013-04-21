package com.chattermap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;

public class NoteMarkerFactory {

	/**
	 * Create a {@link Bitmap} icon for the groups of notes on a map.
	 * 
	 * @param noteCount
	 *            Number of notes in this group
	 * @param width
	 *            Width of the {@link Bitmap} to create
	 * @param height
	 *            Height of the {@link Bitmap} to create
	 * @return {@link Bitmap} containing the icon for the map
	 */
	public static Bitmap createNoteMarker(int noteCount, int size,
			Context context) {
		Bitmap.Config conf = Bitmap.Config.ARGB_8888;

		// Create a bitmap with the given size to draw into
		Bitmap bmp = Bitmap.createBitmap(size, size, conf);

		// Create a canvas to draw into the Bitmap with
		Canvas c = new Canvas(bmp);

		// Draw the callout and note count
		Paint color = new Paint();
		color.setTextSize(((float) size) / 2.0f);
		color.setTextAlign(Align.CENTER);
		color.setColor(Color.BLACK);
		c.drawBitmap(BitmapFactory.decodeResource(context.getResources(),
				(R.drawable.markercallout)), 0, 0, color);
		c.drawText(String.valueOf(noteCount), size / 2, 3 * size / 5, color);

		return bmp;
	}
}
