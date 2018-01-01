//This class was not originally written by me.
package me.rowan.ethan.swiftchat;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class PrefManager
{
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "welcome";

    private static final String SLIDESHOW_FIRST_TIME_LAUNCH = "SlideshowFirstTimeLaunch";
    private static final String TAPTARGET_FIRST_TIME_LAUNCH = "TaptargetFirstTimeLaunch";

    public PrefManager(Context context)
    {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setSlideshowFirstTimeLaunch(boolean isFirstTime)
    {
        editor.putBoolean(SLIDESHOW_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public void setTaptargetFirstTimeLaunch(boolean isFirstTime)
    {
        editor.putBoolean(TAPTARGET_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isSlideshowFirstTimeLaunch()
    {
        return pref.getBoolean(SLIDESHOW_FIRST_TIME_LAUNCH, true);
    }

    public boolean isTaptargetFirstTimeLaunch()
    {
        return pref.getBoolean(TAPTARGET_FIRST_TIME_LAUNCH, true);
    }
}
