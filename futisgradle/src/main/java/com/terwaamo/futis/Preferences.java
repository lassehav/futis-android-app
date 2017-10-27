package com.terwaamo.futis;


import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
 
public class Preferences extends PreferenceActivity {
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
        
        addPreferencesFromResource(R.layout.preferences);
 
    }
    
    @Override
    public void onStop()
    {
    	//Log.d("Asetukset", "onstop");
    	/*final Context context = Preferences.this;
	    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	    ComponentName thisAppWidget = new ComponentName(context.getPackageName(),
	    		  											AppWidgetConfigure.class.getName());
	    
	    //N.B.: we want to launch this intent to our AppWidgetProvider!
	    Intent firstUpdate = new Intent(context, Widget.class);
	    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
	    firstUpdate.setAction("android.appwidget.action.APPWIDGET_UPDATE");
	    firstUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
	    
	    String packageName = context.getPackageName();
	    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String clockModePreference = sharedPref.getString("prefClockStyle", "Digitaalikello");
        firstUpdate.putExtra(packageName + ".CLOCK_MODE", clockModePreference);
	    
	    context.sendBroadcast(firstUpdate);			   
    	*/
    	
    	super.onStop();
    }
    

}