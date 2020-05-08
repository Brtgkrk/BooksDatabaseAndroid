package com.example.booksdatabasev1;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.TreeMap;

public class SessionManagement {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String SHARED_PREF_NAME = "session";
    String ID_KEY = "user_id";
    String USERNAME_KEY = "user_name";
    String PASSWORD_KEY = "user_password";

    public SessionManagement(Context context){
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveSession(User user){
        //save session of user whenever user is logged in
        int id = user.getId();
        String username = user.getName();
        String password = user.getPassword();

        editor.putInt(ID_KEY,id).commit();
        editor.putString(USERNAME_KEY,username).commit();
        editor.putString(PASSWORD_KEY,password).commit();
    }

    public TreeMap getSession(){
        //return user whose session is saved
        Map<String,String>userPref = new TreeMap<>();

        userPref.put("id", String.valueOf(sharedPreferences.getInt(ID_KEY, -1)));
        userPref.put("username", sharedPreferences.getString(USERNAME_KEY, "null"));
        userPref.put("password", sharedPreferences.getString(PASSWORD_KEY, "null"));

        return (TreeMap) userPref;
    }

    public void removeSession(){
        editor.putInt(ID_KEY, -1).commit();
        editor.putString(USERNAME_KEY, "null").commit();
        editor.putString(PASSWORD_KEY, "null").commit();
    }
}
