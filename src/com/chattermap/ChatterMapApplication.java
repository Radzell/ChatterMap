package com.chattermap;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

public class ChatterMapApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// Add your initialization code here
		Parse.initialize(this, "FvOIF7bVKcoDUiIoX6dscqrQymIR9GkNlP1tAdNn",
				"aa9Bmep28gPbqjYk2bd9OUaI6pqvSW0W243QOu9E");

		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();

		// If you would like all objects to be private by default, remove this
		// line.
		defaultACL.setPublicReadAccess(true);
	}
}