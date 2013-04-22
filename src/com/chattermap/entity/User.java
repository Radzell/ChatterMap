package com.chattermap.entity;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.util.Base64;

public class User {
	public String mEmail;
	public String mEmail_hash;

	public User(String pEmail) {
		mEmail = pEmail;
		mEmail_hash = hash(pEmail);

	}

	private String hash(String pEmail) {
		return Base64.encodeToString(pEmail.getBytes(), Base64.DEFAULT);
	}

	private String unhash(String pEmail) {
		return new String(Base64.decode(pEmail.getBytes(), Base64.DEFAULT));
	}
}
