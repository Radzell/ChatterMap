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
public class ChatGroup extends Model {

	protected CharField mObjectID;
	protected CharField mName;
	protected CharField mDescription;

	protected OneToManyField<ChatGroup, Note> mNotes;

	public String getName() {
		return mName.get();
	}

	public void setName(String mName) {
		this.mName.set(mName);
	}

	public String getObjectID() {
		return mObjectID.get();
	}

	public void setObjectID(String iD) {
		mObjectID.set(iD);
	}

	public ChatGroup() {
		super();

		mObjectID = new CharField();
		mName = new CharField();
		mDescription = new CharField();

		mNotes = new OneToManyField<ChatGroup, Note>(ChatGroup.class,
				Note.class);
	}

	public static QuerySet<ChatGroup> getGroups(Context context) {
		return ChatGroup.objects(context, ChatGroup.class);
	}

	public static ChatGroup findByName(String value, Context context) {

		Filter filter = new Filter();
		filter.is("mName", value);
		List<ChatGroup> mGroups = getGroups(context).filter(filter).toList();
		if (mGroups.size() == 0)
			return null;
		return mGroups.get(0);
	}

	public static ChatGroup findByID(String value, Context context) {

		Filter filter = new Filter();
		filter.is("mObjectID", value);
		List<ChatGroup> mGroups = getGroups(context).filter(filter).toList();
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
