package com.chattermap;

import com.chattermap.entity.User;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

/**
 * DialogFragment to create or edit a note. Allows the input of note title,
 * contents, and an TODO: an attached picture, and uploads the new note to the
 * database when the user presses the submit button.
 */
public class LoginDialog extends DialogFragment {
	/**
	 * Listener that a calling activity should implement to receive the results
	 * of the edit.
	 */
	public interface EditNoteDialogListener {
		void onFinishEditDialog(int result);
	}

	private double mLat, mLong;
	private EditNoteDialogListener listener;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Try to grab the listener from the calling activity
		listener = getActivity() instanceof EditNoteDialogListener ? (EditNoteDialogListener) getActivity()
				: null;

		// Get the location and id
		AccountManager accountManager = AccountManager.get(getActivity());
		final Account[] accounts = accountManager
				.getAccountsByType("com.google");

		// Set the layout and initialize:
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final int size = accounts.length;
		String[] names = new String[size];
		for (int i = 0; i < size; i++) {
			names[i] = accounts[i].name;
		}
		builder.setItems(names, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Stuff to do when the account is selected by the user
				gotAccount(accounts[which]);
			}
		});
		return builder.create();
	}

	private void gotAccount(Account account) {
		// Make current user
		MapActivity mActivity = (MapActivity) getActivity();
		mActivity.mCurrentUser = new User(account.name);
		mActivity.updateMenu();
	}
}
