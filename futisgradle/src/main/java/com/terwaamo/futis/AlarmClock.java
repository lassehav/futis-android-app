package com.terwaamo.futis;

import java.util.Calendar;

import com.flurry.android.FlurryAgent;
import com.terwaamo.futis.R;

import android.os.Bundle;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;


public class AlarmClock extends ActionBarActivity {

	@Override
	protected void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, "5PNF6S6J3D43KSBWCCYP");
	}
	 
	@Override
	protected void onStop()
	{
		super.onStop();		
		FlurryAgent.onEndSession(this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_clock);
		// enable back button
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		TimePicker timePicker = (TimePicker)findViewById(R.id.timePickerForAlarm);
		timePicker.setIs24HourView(true);
		timePicker.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
		
		initAlarmUIStatus();				
	}

	
	
	@Override
	public void onResume()
	{		
		initAlarmUIStatus();
		super.onResume();
	}
	
	public void alarmSetToggleClick(View view)
	{
		ToggleButton alarmSetToggle = (ToggleButton)findViewById(R.id.alarmSetToggle);
		final Context context = AlarmClock.this;
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, Alarm.class);	    
	    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		if(alarmSetToggle.isChecked())
		{
			TimePicker timePicker = (TimePicker)findViewById(R.id.timePickerForAlarm);
			Calendar calNow = Calendar.getInstance();
            Calendar calSet = (Calendar) calNow.clone();

            calSet.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
            calSet.set(Calendar.MINUTE, timePicker.getCurrentMinute());
            calSet.set(Calendar.SECOND, 0);
            calSet.set(Calendar.MILLISECOND, 0);
           
            if(calSet.compareTo(calNow) <= 0){
                //Today Set time passed, count to tomorrow
                calSet.add(Calendar.DATE, 1);
            }
            
			//c.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
	        //c.set(Calendar.MINUTE, timePicker.getCurrentMinute());
	        
	        FlurryAgent.logEvent("Alarm Set Click");

			//-- alarm manager	    
	       // Log.d("AlarmClock", "set pending intent alarm");
	       // Log.d("AlarmClock", "current time is " + System.currentTimeMillis());
	       // Log.d("AlarmClock", "alarm time is " + c.getTimeInMillis());
		    
		    
		    am.set(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(), pendingIntent);
		    
		    SharedPreferences settings = getSharedPreferences("AlarmSettings", 0);
		    SharedPreferences.Editor editor = settings.edit();
		    editor.putBoolean("AlarmSet", true);
		    editor.putInt("AlarmHour", timePicker.getCurrentHour());
		    editor.putInt("AlarmMin",  timePicker.getCurrentMinute());

            // Commit the edits!
		    editor.commit();
		    
		    // -- alarm manager
		}
		else
		{
			// Poistetaan alarm managerin asetus
			FlurryAgent.logEvent("Alarm Cancel Click");	               
	        am.cancel(pendingIntent);
	        pendingIntent.cancel();
	        
	        SharedPreferences settings = getSharedPreferences("AlarmSettings", 0);
		    SharedPreferences.Editor editor = settings.edit();
		    editor.putBoolean("AlarmSet", false);
		    editor.commit();
		}

		
		
		
		updateAlarmStatus();
	}
	
	private void initAlarmUIStatus()
	{
		ToggleButton alarmSetToggle = (ToggleButton)findViewById(R.id.alarmSetToggle);
		SharedPreferences settings = getSharedPreferences("AlarmSettings", 0);
	    boolean alarmSet = settings.getBoolean("AlarmSet", false);
	    
	    if(alarmSet)
	    {
	    	alarmSetToggle.setChecked(true);
	    	TextView alarmTimeLabel = (TextView)findViewById(R.id.alarmTimeLabel);
			TextView alarmTimeValue = (TextView)findViewById(R.id.alarmTimeValue);			
			
			alarmTimeLabel.setEnabled(true);
			alarmTimeValue.setEnabled(true);
			int alarmHourTime = settings.getInt("AlarmHour", 0);
			int alarmMinTime = settings.getInt("AlarmMin", 0);
			alarmTimeValue.setText(String.format("%02d", alarmHourTime) + ":" + String.format("%02d", alarmMinTime));
		//	Log.d("initAlarmUISTatus", "hourtime? " + alarmHourTime + ", mintime " + alarmMinTime);
	    }
	    else
	    {
	    	alarmSetToggle.setChecked(false);
	    	TextView alarmTimeLabel = (TextView)findViewById(R.id.alarmTimeLabel);
			TextView alarmTimeValue = (TextView)findViewById(R.id.alarmTimeValue);			
			
			alarmTimeLabel.setEnabled(false);
			alarmTimeValue.setEnabled(false);
	    }
	}
	
	private void updateAlarmStatus()
	{
		ToggleButton alarmSetToggle = (ToggleButton)findViewById(R.id.alarmSetToggle);
				  	    	   	
		if(alarmSetToggle.isChecked())
		{			
			TextView alarmTimeLabel = (TextView)findViewById(R.id.alarmTimeLabel);
			TextView alarmTimeValue = (TextView)findViewById(R.id.alarmTimeValue);
			TimePicker timePicker = (TimePicker)findViewById(R.id.timePickerForAlarm);
			
			alarmTimeLabel.setEnabled(true);
			alarmTimeValue.setEnabled(true);
			alarmTimeValue.setText(String.format("%02d", timePicker.getCurrentHour()) + ":" + String.format("%02d", timePicker.getCurrentMinute()));			
		}
		else
		{			
			TextView alarmTimeLabel = (TextView)findViewById(R.id.alarmTimeLabel);
			TextView alarmTimeValue = (TextView)findViewById(R.id.alarmTimeValue);			
			
			alarmTimeLabel.setEnabled(false);
			alarmTimeValue.setEnabled(false);
		}
	}
	

}
