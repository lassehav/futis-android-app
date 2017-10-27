package com.terwaamo.futis;


import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.facebook.model.OpenGraphAction;
import com.facebook.model.OpenGraphObject;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.flurry.android.FlurryAgent;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;
import com.facebook.UiLifecycleHelper;


public class BestPlayerVote extends ActionBarActivity {
	
	private int mGameId;
	private Player[] players = null;
	private boolean mUserHasVoted = false;
	private Context mContext;
	private ProgressDialog mProgress;
	private boolean mAppTeam = false;
	private int mHomeTeamId;
	private int mAwayTeamId;
	private boolean mHomeTeamIsActive = true;
	private int mHasVotedForPlayer = 0;
	private Menu mActionBarMenu = null;
	private UiLifecycleHelper uiHelper;
	private String mGameTeams;
	private String mGameDate;
	private int mHomeGoals;
	private int mAwayGoals;
	private String mGameTime;
	private boolean mStatePlayerEventsOngoing = false;
	private boolean mStateCheckIfUserHasVotedOngoing = false;
	private boolean mStatePlayersOngoing = false;
	private boolean mStateGameStatsOngoing = false;
	private boolean mGameHasEnded = false;
	
	private String mState = "Start";
	
	private class PlayerEvent
	{
		public int scoresHome;
		public int scoresAway;
		public int time;
		public String type;
		
			
	}
	
	private class Player
	{
		public int playerNumber;
		public String playerName;
		public int playerVotes;
		public int playerId;
		public int teamId;
		public ArrayList<PlayerEvent> events = new ArrayList<PlayerEvent>();
		
		Player(int num, String name, int id, int team)
		{
			playerVotes = 0;
			playerNumber = num;
			playerName = name;
			playerId = id;
			teamId = team;
		}		
		
		Player()
		{}
	}
	
	
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
	
	public void votePlayer(int playerId)
	{
			
		final Player playerData = findPlayerOnId(playerId);
		
		new AlertDialog.Builder(this)
	    .setTitle("Äänestys")
	    .setMessage("Haluatko varmasti äänestää pelaajaa nro " + playerData.playerNumber + ", " + playerData.playerName + " ottelun parhaaksi pelaajaksi?")
	    .setPositiveButton("Kyllä", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 	        		        
	        	
	        	Resources res = getResources();
	    		int teamId = res.getInteger(R.integer.teamId);		
	    		String voterId = DeviceIdProvider.getDeviceId(mContext);
	    		
	    		String urldata = "{\"command\":\"vote\",\"teamId\":" + teamId + ",\"playerId\":" + playerData.playerId + ",\"voterId\":\"" + voterId + "\",\"gameId\":" + mGameId + "}";
	    					    					   	            
	            
	            NetworkResultHandler voteHandler = new NetworkResultHandler(){
	    			@Override
	    			public void processStringResult(String result) {
	    				//Log.d("VotePlayer", result);
	    					
	    				if(result == null)
	    				{
	    					errorDialog("Odottamaton virhe äänestyksessä. Yritä myöhemmin uudestaan.");
	    					mHasVotedForPlayer = 0;
	    					return;
	    				}
	    				
	    				String status = getJSONStatus(result);
	    				mProgress.dismiss();
	    				if(status.equals("OK"))
	    				{	    						    					
	    					JSONObject object;
							try {
								object = (JSONObject) new JSONTokener(result).nextValue();
								JSONArray data = object.getJSONArray("data");
		    					updatePlayerVotes(data);
							} catch (JSONException e) {
								errorDialog("Odottamaton virhe äänestyksessä. Yritä myöhemmin uudestaan.");
								e.printStackTrace();
							}
								
							mHasVotedForPlayer = playerData.playerId;
							voteSuccessModifyList();
							updateActionMenuBar();
	    				}	 
	    				else
	    				{
	    					// vote failed
	    					errorDialog("Odottamaton virhe äänestyksessä. Yritä myöhemmin uudestaan.");
	    				}
	    			}
	            };
	    		
	            //Log.d("BestPlayerVote", "Voting player with URL " + URL.toString());
	            
	            HttpWorker httpWorker = new HttpWorker();
	            httpWorker.setContext(mContext);
	            httpWorker.setResultHandler(voteHandler);
	            httpWorker.setKahakkaSecure();
	            httpWorker.execute(urldata);
	            
	            mProgress = new ProgressDialog(mContext);
				mProgress.setIndeterminate(true);		
				mProgress.setMessage("Äänestys käynnissä...");
				mProgress.show();
	        }	    
	    })
	    .setNegativeButton("Ei", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        })
        .show();
		
		
		
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if((mProgress != null) && (mProgress.isShowing()))
		{
			mProgress.dismiss();
		}
		uiHelper.onDestroy();
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_best_player_vote);
		mContext = this;
		
		// enable back button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		TextView voteInstruction = (TextView) findViewById(R.id.yourTeamVoteInstruction);
		voteInstruction.setVisibility(View.GONE);
		
		Bundle extras = getIntent().getExtras();
		if(extras != null)
		{							
			mGameId = extras.getInt("gameId");
			mHomeTeamId = extras.getInt("homeId");
			mAwayTeamId = extras.getInt("awayId");
			String homeName = extras.getString("homeTeamShortName");
			String awayName =  extras.getString("awayTeamShortName");
			mGameDate = extras.getString("gameDate");
			mGameTime = extras.getString("gameTime");
			mHomeGoals = extras.getInt("homeGoals");
			mAwayGoals = extras.getInt("awayGoals");
			
			TextView teamNamesText = (TextView) findViewById(R.id.TeamNames);
			mGameTeams = homeName + " - " + awayName;
			teamNamesText.setText(mGameTeams);
			
			
			ImageView h = (ImageView) findViewById(R.id.BestPlayerVoteHomeTeamLogo);
			h.setBackgroundColor(Color.argb(80, 255, 255, 255));
			

			// Get team id
			Resources res = getResources();
    		int teamId = res.getInteger(R.integer.teamId);
    		if((teamId == mHomeTeamId) || (teamId == mAwayTeamId))
    		{
    			mAppTeam = true;
    		}
			
            
			
			TeamData td = new TeamData();
			
			TextView gameDateTime = (TextView) findViewById(R.id.BestPlayerVoteGameTime);
			gameDateTime.setText(mGameDate + ", " + mGameTime);			
			
			TextView goals = (TextView) findViewById(R.id.GameGoals);
			goals.setText(mHomeGoals + " - " + mAwayGoals);
			
			ImageView homeTeamLogo = (ImageView) findViewById(R.id.BestPlayerVoteHomeTeamLogo);
			int homeLogoResId = getResources().getIdentifier(td.getTeamLogoFileName(mHomeTeamId), "drawable", getPackageName());
			homeTeamLogo.setImageResource(homeLogoResId);			
			
			ImageView awayTeamLogo = (ImageView) findViewById(R.id.BestPlayerVoteAwayTeamLogo);
			int awayLogoResId = getResources().getIdentifier(td.getTeamLogoFileName(mAwayTeamId), "drawable", getPackageName());
			awayTeamLogo.setImageResource(awayLogoResId);
			
			mProgress = new ProgressDialog(this);
			mProgress.setIndeterminate(true);		
			mProgress.setMessage("Lataa pelaajatietoja...");
			mProgress.show();
			
			mStatePlayerEventsOngoing = true;
			mStateCheckIfUserHasVotedOngoing = true;
			mStatePlayersOngoing = true;
						
			updatePlayers();                       
            updatePlayerEvents();
            //checkIfUserHasVoted();
		}
		else
		{
			// TODO virheilmo	
			errorDialog("Odottamaton virhe. Yritä myöhemmin uudestaan.");
		}
		
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    Session.getActiveSession().onActivityResult(this, requestCode,
                resultCode, data);
        

	    uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
	        @Override
	        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
	           // Log.e("Activity", String.format("Error: %s", error.toString()));
	        }

	        @Override
	        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	           // Log.i("Activity", "Success!");
	        }
	    });
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mActionBarMenu = menu;
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.bestplayervote_actionbarmenu, menu);
	    
	    MenuItem item = menu.findItem(R.id.action_share);
	    item.setVisible(false);
	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_share:
	        	fbShare();
	            return true;	 
	        case R.id.action_bestplayervote_refresh:
	        	if(mState.equals("Idle"))
	        	{
		        	mProgress = new ProgressDialog(mContext);
					mProgress.setIndeterminate(true);		
					mProgress.setMessage("Päivitetään...");
					mProgress.show();
					
					mState = "Refresh";
					mStatePlayerEventsOngoing = true;
					mStateGameStatsOngoing = true;
					
		        	updateGameStats();
		        	updatePlayerEvents();
	        	}
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private Player findPlayerOnId(int id)
	{
		for(int i = 0; i < players.length; i++)
		{
			if(players[i].playerId == id)
			{
				return players[i];
			}
		}
		
		return null;
	}
	
	private void updatePlayerVotes(JSONArray data)
	{
		int dataLen = data.length();
		for(int x = 0; x < dataLen; x++)
		{
			JSONObject d;
			try {
				d = (JSONObject)data.get(x);
				int playerId = d.getInt("playerId");
				int playerVoteCount = d.getInt("lkm");
				for(int i = 0; i < players.length; i++)
				{
					if(players[i].playerId == playerId)
					{
						players[i].playerVotes = playerVoteCount;
						break;
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}				
		}			
	}
	
	private void updateActionMenuBar()
	{
		if(mActionBarMenu != null)
		{
			MenuItem item = mActionBarMenu.findItem(R.id.action_share);
			if(item != null)
			{
				if(mHasVotedForPlayer != 0)
				{
					item.setVisible(true);
				}
				else
				{
					item.setVisible(false);
				}
			}
		}
		
	
	}
	
	private void voteSuccessModifyList()
	{
		mUserHasVoted = true;
		TextView voteHeader = (TextView) findViewById(R.id.bestPlayerVoteListHeaderCount);
		voteHeader.setVisibility(View.VISIBLE);
		TextView voteInstruction = (TextView) findViewById(R.id.yourTeamVoteInstruction);
		voteInstruction.setText("Olet jo äänestänyt tämän ottelun parasta pelaajaa. Näet tulokset alla.");
		initPlayerList();
		
	}
	
	private String getJSONStatus(String rawJSON)
	{
		JSONObject object = null;
		String status = "";
		try {
			object = (JSONObject) new JSONTokener(rawJSON).nextValue();
			status = object.getString("status");			
			return status;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}        			
	}
	
	private void errorDialog(String message)
	{
		if(mProgress != null)
		{
			mProgress.dismiss();
		}
		
		new AlertDialog.Builder(this)
	    .setTitle("Virhe")
	    .setMessage(message)				    
	    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            finish();
	        }
	     })				    
	     .show();		
	}
	
	private void initPlayerList()
	{
		if(players.length == 0)
		{
			return;
		}
		
		LinearLayout linearList = (LinearLayout) findViewById(R.id.linearPlayerList);
		linearList.removeAllViews();
		int selectedTeamId;
		if(mHomeTeamIsActive)
		{
			selectedTeamId = mHomeTeamId;
		}
		else			
		{
			selectedTeamId = mAwayTeamId;
		}
		
		Resources res = getResources();
		int applicationTeamId = res.getInteger(R.integer.teamId);
				
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int playerCount = players.length;
		for(int i = 0; i < playerCount; i++)
		{
			if(players[i].teamId == selectedTeamId)
			{
				View v = inflater.inflate(R.layout.best_player_list_item, linearList, false);
	
		        Button voteButton = (Button) v.findViewById(R.id.BestPlayerVoteButton);
		        if((mUserHasVoted == false) && (mAppTeam == true) && (players[i].teamId == applicationTeamId) && (mGameHasEnded == false))		        
		        {	        	
			        voteButton.setTag(players[i].playerId);
			        voteButton.setOnClickListener(
			        		new OnClickListener()
			        		{
			        		    @Override
			        		   public void onClick(View view) {
			        		    	Object id = view.getTag();
			        		    	int plrId = Integer.parseInt(id.toString());		        		    			        		       
			        		        votePlayer(plrId);
			        		   }
			        		}
			        );
		        	Drawable d = voteButton.getBackground();  
		            d.setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0x00067500));
		        }
		        else
		        {	     
		        	if(players[i].playerId == mHasVotedForPlayer)
		        	{
		        		RelativeLayout row = (RelativeLayout) v.findViewById(R.id.PlayerListRow);
		        		row.setBackgroundColor(0x22FFFFFF);
		        	}
		        	voteButton.setVisibility(View.GONE);
		        	TextView voteCount = (TextView) v.findViewById(R.id.PlayerVotes);
					voteCount.setVisibility(View.VISIBLE);
					voteCount.setText(Integer.toString(players[i].playerVotes) + "%");											
		        }
		        
		        
		           
			    TextView playerNumText = (TextView) v.findViewById(R.id.PlayerNumber);
			    playerNumText.setText(Integer.toString(players[i].playerNumber));
			    
			    TextView playerNameText = (TextView) v.findViewById(R.id.PlayerName);
			    playerNameText.setText(players[i].playerName);
			    
			    LinearLayout stats = (LinearLayout) v.findViewById(R.id.PlayerStats);
			    for (PlayerEvent e : players[i].events)
			    {
			     //   if (s.equals(value))
			    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			    	TextView tv = new TextView(mContext);
			    	tv.setLayoutParams(params);
			    	tv.setTextSize(14);
			    	tv.setText(e.scoresHome + "-" + e.scoresAway + " " + e.type + " " + e.time +"min");
			    	tv.setTextColor(Color.parseColor("#BBBBFF"));
			    	stats.addView(tv);
			    }
			    
			    linearList.addView(v);
			}
		}	
		
		// set action bar share icon
		
	}
	
	public void homeTeamClick(View view)
	{
		FlurryAgent.logEvent("BestPlayerVote HomeTeam Click");
		mHomeTeamIsActive = true;
		ImageView h = (ImageView) findViewById(R.id.BestPlayerVoteHomeTeamLogo);
		//h.setBackgroundColor(Color.argb(190, 14, 122, 0));
		h.setBackgroundColor(Color.argb(80, 255, 255, 255));
		
	
		ImageView a = (ImageView) findViewById(R.id.BestPlayerVoteAwayTeamLogo);
		a.setBackgroundColor(Color.TRANSPARENT);		
		
		initPlayerList();
	}
	
	public void awayTeamClick(View view)
	{
		FlurryAgent.logEvent("BestPlayerVote AwayTeam Click");
		mHomeTeamIsActive = false;
		ImageView h = (ImageView) findViewById(R.id.BestPlayerVoteHomeTeamLogo);
		h.setBackgroundColor(Color.TRANSPARENT);
				
		ImageView a = (ImageView) findViewById(R.id.BestPlayerVoteAwayTeamLogo);
		a.setBackgroundColor(Color.argb(80, 255, 255, 255));
		
		
		initPlayerList();
	}
	
	public void fbShare()
	{
		/*
		//create the send intent  
		Intent shareIntent =   
		 new Intent(android.content.Intent.ACTION_SEND);  
		  
		//set the type  
		shareIntent.setType("text/plain");  
		  
		//add a subject  
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,   
		 "Insert Subject Here");  
		  
		//build the body of the message to be shared  
		String shareMessage = "Insert message body here.";  
		  
		//add the message  
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,   
		 shareMessage);  
		  
		//start the chooser for sharing  
		startActivity(Intent.createChooser(shareIntent,   
		 "Insert share chooser title here"));  */
		
		// PERUS DIALOG SHARE
		
		Player bestPlayer = findPlayerOnId(mHasVotedForPlayer);
		String pkgName = getApplicationContext().getPackageName();
		String fbLink = "http://play.google.com/store/apps/details?id=" + pkgName;
		//Log.d("BestPlayerVote", "Link to share in FB " + fbLink);
		String description = mGameDate + " " + mGameTeams + " ottelun paras pelaaja";
		String imageLink = mContext.getResources().getText(R.string.serverAddress) + "img/" + mContext.getResources().getText(R.string.fb_logo_image) + ".png";
		//Log.d("BestPlayerVote", "FB Image link " + imageLink);
		
		OpenGraphObject plr = OpenGraphObject.Factory.createForPost(getString(R.string.facebookapp_namespace) + ":Player");
		plr.setProperty("title", bestPlayer.playerName);
		plr.setProperty("name", bestPlayer.playerName);
		plr.setProperty("image", imageLink);
		plr.setProperty("url", fbLink);
		plr.setProperty("description", description);

		OpenGraphAction action = GraphObject.Factory.create(OpenGraphAction.class);
		action.setProperty("player", plr);
		
		
		
		
		if (FacebookDialog.canPresentShareDialog(getApplicationContext(), 
		        FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
		        
			/*int fbLogoImageId = mContext.getResources().getIdentifier(getResources().getString(R.string.fb_logo_image), "drawable", mContext.getPackageName());
			Bitmap image = BitmapFactory.decodeResource(getResources(), fbLogoImageId);
			//Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.hjk_144);
			List<Bitmap> images = new ArrayList<Bitmap>();
			images.add(image);*/
	
			FacebookDialog shareDialog = new FacebookDialog.OpenGraphActionDialogBuilder(this, action, "futishjk:Vote", "player")
					//.setImageAttachmentsForObject("player", images, false)
					//.setImageAttachmentsForAction(images, false)
			        .build();
			uiHelper.trackPendingDialogCall(shareDialog.present());


		}
		else
		{
			Bundle params = new Bundle();
		    params.putString("name", mContext.getResources().getText(R.string.app_name).toString());
		    params.putString("description", bestPlayer.playerName + " " + description);
		    params.putString("link", fbLink);
		    params.putString("picture", imageLink);
		    

		    String appId = null;
		    Session.openActiveSession(this, false, null);
		    WebDialog feedDialog = 
		        new WebDialog.FeedDialogBuilder(mContext, appId, params)
		        .setOnCompleteListener(new WebDialog.OnCompleteListener() {

		            @Override
		            public void onComplete(Bundle values,
		                FacebookException error) {
		                if (error == null) {
		                    // When the story is posted, echo the success
		                    // and the post Id.
		                    final String postId = values.getString("post_id");
		                    if (postId != null) {
		                        Toast.makeText(BestPlayerVote.this,
		                            "Posted story, id: "+postId,
		                            Toast.LENGTH_SHORT).show();
		                    } else {
		                        // User clicked the Cancel button
		                        Toast.makeText(mContext, 
		                            "Publish cancelled", 
		                            Toast.LENGTH_SHORT).show();
		                    }
		                } else if (error instanceof FacebookOperationCanceledException) {
		                    // User clicked the "x" button
		                    Toast.makeText(mContext, 
		                        "Publish cancelled", 
		                        Toast.LENGTH_SHORT).show();
		                } else {
		                    // Generic, ex: network error
		                    Toast.makeText(mContext, 
		                        "Error posting story", 
		                        Toast.LENGTH_SHORT).show();
		                }
		            }

		        })
		        .build();
		    feedDialog.show();
			
		}			
	}
	
	private void processTeamPlayers(NodeList nodes, int teamId, ArrayList<Player> list)
	{
							
		if(nodes != null && nodes.getLength() > 0) 
		{
	           	            
			for(int i = 0; i < nodes.getLength(); ++i) {
				Player plr = new Player();
 
	            Node node = nodes.item(i);              
	            Element e = (Element)node;
	            plr.playerId = Integer.parseInt(e.getAttribute("id"));
                plr.playerNumber = Integer.parseInt(e.getAttribute("uniform"));
                plr.playerName = e.getAttribute("lastname") + " " + e.getAttribute("firstname"); 	    
                plr.teamId = teamId;
                	               
                list.add(plr);	                	               	               
            }
        }
			

	}
	
	public void processPlayersData(String data)
	{

		ArrayList<Player> playerList = new ArrayList<Player>();
		
		InputSource inputSrcHome = new InputSource(new StringReader( data ));
		InputSource inputSrcAway = new InputSource(new StringReader( data ));
		XPath xpath = XPathFactory.newInstance().newXPath();
		String homeExpression = "//game_lineups/players_home/player";
		String awayExpression = "//game_lineups/players_away/player";
		try {
			NodeList nodes = (NodeList)xpath.evaluate(homeExpression, inputSrcHome, XPathConstants.NODESET);
					
			processTeamPlayers(nodes, mHomeTeamId, playerList);
			xpath.reset();
			nodes = (NodeList)xpath.evaluate(awayExpression, inputSrcAway, XPathConstants.NODESET);
			processTeamPlayers(nodes, mAwayTeamId, playerList);							
			
		} catch (XPathExpressionException e) {
			//networkError();
			e.printStackTrace();
			return;
		}
		
		players = playerList.toArray(new Player[playerList.size()]);
		if(players.length == 0)
		{
			LinearLayout target = (LinearLayout) findViewById(R.id.linearPlayerList);
			target.removeAllViews();
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80);
			params.setMargins(20, 20, 20, 0); // llp.setMargins(left, top, right, bottom);
	    	TextView tv = new TextView(mContext);
	    	tv.setLayoutParams(params);
	    	tv.setTextSize(18);
	    	tv.setGravity(Gravity.CENTER);
	    	tv.setTextColor(Color.parseColor("#FFFFFF"));
	    	
	    	
	    	tv.setText("Aloituskokoonpanoja ei ole vielä julkaistu");
	    	target.addView(tv);
	    	
	    	
		}
		
	}
	
	public void processGameEventData(String data)
	{				
		InputSource inputSrc = new InputSource(new StringReader( data ));
		
		XPath xpath = XPathFactory.newInstance().newXPath();
		String expression = "//goal";
		
		try {
			NodeList nodes = (NodeList)xpath.evaluate(expression, inputSrc, XPathConstants.NODESET);
				
			if(nodes != null && nodes.getLength() > 0) 
			{		  
				
				for(int i = 0; i < players.length; i++)
				{
					players[i].events.clear();
				}
				
				for(int i = 0; i < nodes.getLength(); ++i) {
						 
		            Node node = nodes.item(i);              
		            Element e = (Element)node;		            
		            		           		            
		            NodeList childNodes = node.getChildNodes();
	                int childLen= childNodes.getLength();	                
		            for(int c = 0; c < childLen; c++) {
		            	Node childNode = childNodes.item(c);
		            	/*short type = childNode.getNodeType();
		            	String text = childNode.getTextContent();
		            	String value = childNode.getNodeValue();*/
		            	String name = childNode.getNodeName();
		            	NamedNodeMap attr = childNode.getAttributes();
		            			            			            
		            	if(name.equals("scorer"))
				    	{
		            		PlayerEvent evt = new PlayerEvent();
		            		evt.scoresHome = Integer.parseInt(e.getAttribute("scores_home"));
			            	evt.scoresAway = Integer.parseInt(e.getAttribute("scores_away"));
			            	evt.time = Integer.parseInt(e.getAttribute("time"));
		            		evt.type = "maali";
		            		Player plr = findPlayerOnId(Integer.parseInt(attr.getNamedItem("id").getTextContent()));
		            		plr.events.add(evt);
				    	}		            	
				    	else if(name.equals("assist"))
				    	{
				    		PlayerEvent evt = new PlayerEvent();
				    		evt.scoresHome = Integer.parseInt(e.getAttribute("scores_home"));
			            	evt.scoresAway = Integer.parseInt(e.getAttribute("scores_away"));
			            	evt.time = Integer.parseInt(e.getAttribute("time"));
				    		evt.type = "syöttö";
				    		String x = attr.getNamedItem("id").getTextContent();
				    		if(x.length()>0)
				    		{
				    			Player plr = findPlayerOnId(Integer.parseInt(x));
				    			plr.events.add(evt);
				    		}
				    	}		            			            			            					    
		            }	                	               	                                	               	             
	            }
	        }
			
			
		} catch (XPathExpressionException e) {
			//networkError();
			e.printStackTrace();
			return;
		}
		
		
	}
	
	public void updateGameStats()
	{			
		NetworkResultHandler dataHandler = new NetworkResultHandler(){
			@Override
			public void processStringResult(String result) {
				boolean error = false;
				mStateGameStatsOngoing = false;
											
				if(result == null)
				{
					//Log.d("BestPlayerVote", "updateGameStats on null");
					error = true;
				}
									
				if((error == false) && (result != null))
				{								
					InputSource inputSrc = new InputSource(new StringReader( result ));
					XPath xpath = XPathFactory.newInstance().newXPath();
					String expression = "//game_schedule/game[@id='" + mGameId + "']";
					try {
						NodeList nodes = (NodeList)xpath.evaluate(expression, inputSrc, XPathConstants.NODESET);						
						
						if(nodes != null && nodes.getLength() > 0) {
				            
				            int len = nodes.getLength();
				            for(int i = 0; i < len; ++i) {
				            	
				                Node node = nodes.item(i);				               				                				               
				                
				                NodeList childNodes = node.getChildNodes();
				                int childLen= childNodes.getLength();	                
					            for(int c = 0; c < childLen; c++) {
					            	Node childNode = childNodes.item(c);
					            	String name = childNode.getNodeName();
					            	NamedNodeMap attr = childNode.getAttributes();
					            						            						            
							    	if(name.equals("home_team"))
							    	{				    								    	
							    		mHomeGoals = Integer.parseInt(attr.getNamedItem("goals").getTextContent());
							    		TextView goals = (TextView) findViewById(R.id.GameGoals);
										goals.setText(mHomeGoals + " - " + mAwayGoals);
							    	}
							    	else if(name.equals("away_team"))
							    	{								    									    		
							    		mAwayGoals = Integer.parseInt(attr.getNamedItem("goals").getTextContent());
							    		TextView goals = (TextView) findViewById(R.id.GameGoals);
										goals.setText(mHomeGoals + " - " + mAwayGoals);
							    	}		            	
					            }		            					                   	              
				            }
				            checkStateAndUpdate();
				        }
						
					} catch (XPathExpressionException e) {
						//networkError();
						e.printStackTrace();
						return;
					}
				}
				else
				{
					
				}
				
			}
        };
		
        HttpWorker httpWorker = new HttpWorker();
        httpWorker.setResultHandler(dataHandler);
        httpWorker.execute("http://www.veikkausliiga.com/VeikkausliigaWS/otteluohjelma.asmx/Get?");
	
	}
	
	public void updatePlayerEvents()
	{			
		// Haetaan pelitapahtumat
        NetworkResultHandler gameEventsHandler = new NetworkResultHandler(){
        	private void updateGameStatus(String data)
        	{        	
        		InputSource inputSrc = new InputSource(new StringReader( data ));        		
        		
        		XPath xpath = XPathFactory.newInstance().newXPath();
        		String expression = "//game";
        		
        		try {
        			NodeList nodes = (NodeList)xpath.evaluate(expression, inputSrc, XPathConstants.NODESET);
        				
        			if(nodes != null && nodes.getLength() > 0) 
        			{		           	                   				        					        
    		            Node node = nodes.item(0);              
    		            Element e = (Element)node;		            
    		            String status = e.getAttribute("status");
    		            
    		            if(status.equals("ottelu p��ttynyt"))
    		            //if(1 == 2)
    		            {
    		            	mGameHasEnded = true;
    		            	if(mAppTeam == true)
    		            	{
    		            		TextView voteInstruction = (TextView) findViewById(R.id.yourTeamVoteInstruction);
    		            		voteInstruction.setText("Ottelu ja parhaan pelaajan äänestys on päättynyt.");
    		            	}
    		            }
        	        } 
        		} catch (XPathExpressionException e) {
        			e.printStackTrace();
        			return;
        		}
        	}
        	
			@Override
			public void processStringResult(String result) {
				boolean error = false;
				mStatePlayerEventsOngoing = false;
								
				if(result == null)
				{
					error = true;
				}
									
				if((error == false) && (result != null))
				{										
					processGameEventData(result);
					updateGameStatus(result);	
					checkStateAndUpdate();
				}
				else
				{
					
				}
				
			}
        };
		
        String gameEventsLink = "http://www.veikkausliiga.com/VeikkausliigaWS/OtteluRaportti.asmx/Get?OtteluID=" + mGameId;
        HttpWorker httpWorkerEvents = new HttpWorker();
        httpWorkerEvents.setResultHandler(gameEventsHandler);
        httpWorkerEvents.execute(gameEventsLink);
	
	}
	
	public void updatePlayers()
	{
		NetworkResultHandler teamPlayers = new NetworkResultHandler(){
			@Override
			public void processStringResult(String result) {
				boolean error = false;
				mStatePlayersOngoing = false;
								
				if(result == null)
				{
					//Log.d("BestPlayerVote", "Player get failed");
					error = true;
				}
									
				if((error == false) && (result != null))
				{										
					processPlayersData(result);							
					checkStateAndUpdate();
					if(mState.equals("Start"))
					{
						checkIfUserHasVoted();
					}
					
				}
				else
				{    								
					errorDialog("Odottamaton virhe. Yritä myöhemmin uudestaan.");
				}
				
			}
        };
		
                
        String teamPlayersLink = "http://www.veikkausliiga.com/VeikkausliigaWS/aloituskokoonpanot.asmx/Get?OtteluID=" + mGameId;
        HttpWorker httpWorker = new HttpWorker();
        httpWorker.setResultHandler(teamPlayers);        
        httpWorker.execute(teamPlayersLink);
		
	}
	
	public void checkStateAndUpdate()
	{
		if(mState.equals("Start"))
		{			
			// player list available, user vote check, player events
			if((mStatePlayersOngoing == false) && (mStateCheckIfUserHasVotedOngoing == false) && (mStatePlayerEventsOngoing == false))
			{
				initPlayerList();
				mProgress.dismiss();
				mState = "Idle";
			}						 		
		}
		else if(mState.equals("Refresh"))
		{
			if((mStateGameStatsOngoing == false) && (mStatePlayerEventsOngoing == false))
			{
				initPlayerList();
				mProgress.dismiss();
				mState = "Idle";
			}
		}
	}
	
	public void checkIfUserHasVoted()
	{
		// Get the id of this device/user
		String voterId = DeviceIdProvider.getDeviceId(mContext);
		
		// Get team id
		Resources res = getResources();
		int teamId = res.getInteger(R.integer.teamId);
		String urldata;
		if((teamId == mHomeTeamId) || (teamId == mAwayTeamId))
		{		    		
			TextView voteInstruction = (TextView) findViewById(R.id.yourTeamVoteInstruction);
			voteInstruction.setVisibility(View.VISIBLE);
			    			    			    			    	
			// Check if user has already voted							
			urldata = "{\"command\":\"hasVoted\",\"voterId\":\"" + voterId + "\",\"gameId\":" + mGameId + ",\"teamId\":" + teamId + "}";
		}
		else
		{
			TextView voteHeader = (TextView) findViewById(R.id.bestPlayerVoteListHeaderCount);
			voteHeader.setVisibility(View.VISIBLE);	
			
			urldata = "{\"command\":\"getGameVotes\",\"gameId\":" + mGameId + "}";
		}
        try
        {
        	NetworkResultHandler hasVotedHandler = new NetworkResultHandler(){
    			@Override
    			public void processStringResult(String result) {
    				//Log.d("BestPlayerVote", "Server response " + result);
    				mStateCheckIfUserHasVotedOngoing = false;
    				
    				if(result == null)
    				{
    					return;
    				}
    				String status = getJSONStatus(result);
    				
					//Log.d("BestPlayerVote", "status value " + status);
    				if(status.equals("OK"))
    				{
    					
    					JSONObject object = null;    					
        				try {
        					object = (JSONObject) new JSONTokener(result).nextValue();
        					int teamId = getResources().getInteger(R.integer.teamId);
        					if((teamId == mHomeTeamId) || (teamId == mAwayTeamId))
        					{
        						boolean hasVoted = object.getBoolean("hasVoted");
            					if(!hasVoted)
            					{
            						//Log.d("BestPlayerVote", "User has not yet voted!"); 
            						mUserHasVoted = false;		        						
            					}
            					else
            					{
            						//Log.d("BestPlayerVote", "User has voted!");
            						mUserHasVoted = true;
            						TextView voteHeader = (TextView) findViewById(R.id.bestPlayerVoteListHeaderCount);
            						voteHeader.setVisibility(View.VISIBLE);
            						TextView voteInstruction = (TextView) findViewById(R.id.yourTeamVoteInstruction);
            						voteInstruction.setText("Olet jo äänestänyt tämän ottelun parasta pelaajaa. Näet tulokset alla.");
            						
            						mHasVotedForPlayer = object.getInt("votedForPlayer");
            					}
            					object = (JSONObject) new JSONTokener(result).nextValue();
        					}
        					
        					
        					JSONArray data = object.getJSONArray("data");
        					updatePlayerVotes(data);
        							        				
        					updateActionMenuBar();
        					checkStateAndUpdate();
        				}
        				catch(Exception e)
        				{
        					errorDialog("Yhteysvirhe. Yritä myöhemmin uudestaan.");
        					e.printStackTrace();
        				}
    				}
    				else
    				{
    					errorDialog("Yhteysvirhe. Yritä myöhemmin uudestaan.");
    				}
    							   		
    			}
            };
    		
            HttpWorker httpWorker = new HttpWorker();
            httpWorker.setContext(mContext);
            httpWorker.setResultHandler(hasVotedHandler);
            httpWorker.setKahakkaSecure();
            httpWorker.execute(urldata);
        }
        catch(Exception ex)
        {
            // Fail
        	ex.printStackTrace();
        	return;
        }
	}
	
}



