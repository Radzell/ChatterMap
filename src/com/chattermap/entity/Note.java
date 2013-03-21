package com.chattermap.entity;

import android.location.Location;

import com.orm.androrm.Model;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.LocationField;

/**
 * Notes class that will be used to save to local disk TODO write update method
 * that will update the local database based on changing server
 * 
 * @author radzell
 * 
 */
public class Note extends Model {

	protected CharField ID;
	protected CharField mTitle;
	protected CharField mBody;

	protected LocationField mLocation;

	// Link the Group model to the Note model.
	protected ForeignKeyField<Group> mGroup;

	public String getID() {
		return ID.get();
	}

	public void setID(String mID) {
		this.ID.set(mID);
	}

	public String getTitle() {
		return mTitle.get();
	}

	public void setTitle(String mTitle) {
		this.mTitle.set(mTitle);
	}

	public Location getLocation() {
		return mLocation.get();
	}

	public void setLocation(Location mLocation) {
		this.mLocation.set(mLocation);
	}

	public Group getGroup() {
		return mGroup.get();
	}

	public void setGroup(Group mGroup) {
		this.mGroup.set(mGroup);
	}

	public Note() {
		super();

		ID = new CharField();
		mTitle = new CharField();
		mBody = new CharField();

		mGroup = new ForeignKeyField<Group>(Group.class);
	}

	public void setBody(String body) {
		mBody.set(body);
	}

}
