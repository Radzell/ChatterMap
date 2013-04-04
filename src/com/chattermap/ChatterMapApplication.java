package com.chattermap;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

public class ChatterMapApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// Initialize the Parse Database
		Parse.initialize(this, "FvOIF7bVKcoDUiIoX6dscqrQymIR9GkNlP1tAdNn",
				"aa9Bmep28gPbqjYk2bd9OUaI6pqvSW0W243QOu9E");

		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();

		// Allow public viewing of the database, so all users have access
		defaultACL.setPublicReadAccess(true);
	}
}