package com.chattermap.entity;

import android.content.Context;

import com.orm.androrm.Model;
import com.orm.androrm.QuerySet;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.OneToManyField;

/**
 * Group class that will be used to save to local disk TODO write update method
 * that will update the local database based on changing server
 * 
 * @author radzell
 * 
 */
public class Group extends Model {

	protected CharField ID;
	protected CharField mTitle;
	protected OneToManyField<Group, Note> mNotes;

	public CharField getmTitle() {
		return mTitle;
	}

	public void setmTitle(String mTitle) {
		this.mTitle.set(mTitle);
	}

	public String getID() {
		return ID.get();
	}

	public void setID(String iD) {
		ID.set(iD);
	}

	public Group() {
		super();

		ID = new CharField();
		mTitle = new CharField();

		mNotes = new OneToManyField<Group, Note>(Group.class, Note.class);
	}

	public static QuerySet<Group> getGroups(Context context) {
		return Group.objects(context, Group.class);
	}
}
