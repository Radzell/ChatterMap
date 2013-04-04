package com.chattermap;

import com.chattermap.entity.ChatGroup;
import com.chattermap.entity.Note;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class EditNoteDialog extends DialogFragment {
	public interface EditNoteDialogListener {
		void onFinishEditDialog(int result);
	}

	private double mLat, mLong;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the location and id
		Bundle args = getArguments();
		mLat = args.getDouble(getString(R.string.editnote_latitude), 0.0);
		mLong = args.getDouble(getString(R.string.editnote_longitude), 0.0);

		// Set the layout and initialize:
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.dialog_editnote, null))
				// Add action buttons
				.setPositiveButton(R.string.editnote_submit,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								onPositiveClick(dialog, id);
							}
						})
				.setNegativeButton(R.string.editnote_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								onNegativeClick(dialog, id);
							}
						});
		return builder.create();
	}
	
	/**
	 * Handle a click of the positive "submit" button of the dialog.
	 * @param dialog DialogInterface passed into the onClickListener
	 * @param id id passed into the onClickListener
	 */
	private void onPositiveClick(DialogInterface dialog, int id ) {
		ChatGroup group = ((MapActivity) getActivity())
				.getCurrentGroup();
		EditText et = (EditText) EditNoteDialog.this
				.getDialog().findViewById(
						R.id.dialog_editnote_editbody);
		if (et.length() > 0) {
			Note.create("", et.getText().toString(),
					mLat, mLong, group);
			Log.d("EDITNOTE", "Note created : "
					+ et.getText().toString());
		}
		EditNoteDialogListener listener = (EditNoteDialogListener) getActivity();
		listener.onFinishEditDialog(Activity.RESULT_OK);
	}
	
	/**
	 * Handle a click of the positive "submit" button of the dialog.
	 * @param dialog
	 * @param id
	 */
	private void onNegativeClick(DialogInterface dialog, int id ) {
		EditNoteDialogListener listener = (EditNoteDialogListener) getActivity();
		listener.onFinishEditDialog(Activity.RESULT_CANCELED);
		EditNoteDialog.this.getDialog().cancel();
	}
}
