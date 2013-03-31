package com.chattermap.entity;

import java.util.List;

import android.content.Context;

import com.orm.androrm.Filter;
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

	protected CharField mObjectID;
	protected CharField mName;
	protected CharField mDescription;

	protected OneToManyField<Group, Note> mNotes;

	public String getName() {
		return mName.get();
	}

	public void setName(String mTitle) {
		this.mName.set(mTitle);
	}

	public String getID() {
		return mObjectID.get();
	}

	public void setID(String iD) {
		mObjectID.set(iD);
	}

	public Group() {
		super();

		mObjectID = new CharField();
		mName = new CharField();
		mDescription = new CharField();

		mNotes = new OneToManyField<Group, Note>(Group.class, Note.class);
	}

	public static QuerySet<Group> getGroups(Context context) {
		return Group.objects(context, Group.class);
	}

	public static Group find(String value, Context context) {

		Filter filter = new Filter();
		filter.is("mName", "Public");
		List<Group> mGroups = getGroups(context).filter(filter).toList();
		if (mGroups.size() == 0)
			return null;
		return mGroups.get(0);
	}

	public void setDescription(String string) {
		mDescription.set(string);
	}

	public String getDescription() {
		return mDescription.get();
	}
}
