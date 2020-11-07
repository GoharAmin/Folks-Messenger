package codegradients.com.chatapp.sessions;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessions {
    private static String PREF_NAME = "user_pref";
    private static String KEY_NUMBER = "user_number";
    private static String KEY_USER_NAME = "user_name";
    private static String KEY_USER_ID = "user_id";
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    public UserSessions(Context context) {
        mPreferences = context.getSharedPreferences(PREF_NAME, 0); // 0 - for private mode
        mEditor = mPreferences.edit();
    }

    public void setUser(String userId, String number, String userName) {
    mEditor.putString(KEY_USER_ID,userId);
    mEditor.putString(KEY_NUMBER,number);
    mEditor.putString(KEY_USER_NAME,userName);
    mEditor.commit();
    }

    public String getUSerId() {
        return mPreferences.getString(KEY_USER_ID,"");

    }

    public String getUserName() {
        return mPreferences.getString(KEY_USER_NAME,"");
    }
    public String getUserNumber(){
        return mPreferences.getString(KEY_NUMBER,"");
    }

}
