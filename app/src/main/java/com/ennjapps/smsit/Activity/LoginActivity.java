package com.ennjapps.smsit.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.ennjapps.smsit.MyPreferences;
import com.ennjapps.smsit.R;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

            MyPreferences pref= new MyPreferences(LoginActivity.this);
            if(!pref.isFirstTime()){
                Intent i=new Intent(getApplicationContext(),AddSmsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                finish();
            }
        }
    public void SaveUserName(View v){
        EditText usrName=(EditText) findViewById(R.id.username);
        MyPreferences pref= new MyPreferences(LoginActivity.this);
        pref.setUsername(usrName.getText().toString().trim());
        Intent i=new Intent(getApplicationContext(),AddSmsActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        finish();
    }
}



