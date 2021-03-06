package com.chattermap.entity;

import android.location.Location;

import com.orm.androrm.Model;
import com.orm.androrm.field.CharField;
import com.orm.androrm.field.ForeignKeyField;
import com.orm.androrm.field.LocationField;
import com.parse.ParseObject;

/**
 * Notes class that will be used to save to local disk TODO write update method
 * that will update the local database based on changing server
 * 
 * @author radzell
 * 
 */
public class Note extends Model {

	protected CharField mObjectID;
	protected CharField mTitle;
	protected CharField mBody;

	public String getBody() {
		return mBody.get();
	}

	public void setBody(String mBody) {
		this.mBody.set(mBody);
	}

	protected LocationField mLocation;

	// Link the Group model to the Note model.
	protected ForeignKeyField<ChatGroup> mGroup;

	public String getID() {
		return mObjectID.get();
	}

	public void setID(String mID) {
		this.mObjectID.set(mID);
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

	public ChatGroup getGroup() {
		return mGroup.get();
	}

	public void setGroup(ChatGroup mGroup) {
		this.mGroup.set(mGroup);
	}

	public Note() {
		super();

		mObjectID = new CharField();
		mTitle = new CharField();
		mBody = new CharField();
		mLocation = new LocationField();
		mGroup = new ForeignKeyField<ChatGroup>(ChatGroup.class);
	}

	/**
	 * Static way of creating a note on the server
	 * 
	 * @param title
	 * @param body
	 * @param mLat
	 * @param mLong
	 */
	public static void create(String title, String body, double mLat,
			double mLong, ChatGroup group) {
		ParseObject poGroup = new ParseObject("Group");

		poGroup.setObjectId(group.getObjectID());
		poGroup.put("Name", group.getName());
		poGroup.put("Description", group.getDescription());

		ParseObject po = new ParseObject("Note");
		po.put("mTitle", title);
		po.put("mBody", body);
		po.put("mLat", mLat);
		po.put("mLongit", mLong);
		po.put("parent", poGroup);
		po.saveEventually();
	}

}
