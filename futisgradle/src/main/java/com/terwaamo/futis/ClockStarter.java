package com.terwaamo.futis;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ClockStarter {
	static void startAlarmManagerForClock(Context context)
	{
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	    Intent intent = new Intent(context, Widget.class);
	    intent.setAction(context.getPackageName() + ".AIKALEIMA");
	    PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 2000 , pi); // laitetaan kahden sekunnin toistoväli
	    //Log.d("ClockStarter", "startattu");
	}
}
