package com.ennjapps.smsit;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by haider on 30-04-2016.
 */
public class MyPreferences {

    Context _context;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private static final String PREF_NAME="SMSSchedule";
    public static final String IS_FIRSTTIME="IsFirstTime";
    public static final String Username="name";
    int PRIVATE_MODE=0;

    public MyPreferences(Context context) {
        this._context=context;
        pref=_context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor=pref.edit();
    }
    public boolean isFirstTime(){
        return pref.getBoolean(IS_FIRSTTIME,true);

    }
    public void setOld(boolean b){
        if(b){
            editor.putBoolean(IS_FIRSTTIME,false);
            editor.commit();
        }


    }
    public String getUsername(){
        return pref.getString(Username,"");

    }

    public void setUsername(String name){
        editor.putString(Username,name);
        editor.commit();


    }
}
