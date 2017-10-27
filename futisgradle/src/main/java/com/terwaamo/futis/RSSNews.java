package com.terwaamo.futis;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;

import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;



public class RSSNews extends Activity {

    private ArrayList<RssItem> mRssItems;

    private class CustomAdapter extends ArrayAdapter<RssItem> {

        private final Context context;
        private ArrayList<RssItem> rssItems;

        public CustomAdapter(Context context, int textViewResourceId, ArrayList<RssItem> rssItems) {
            super(context, textViewResourceId, rssItems);
            this.context = context;
            this.rssItems = rssItems;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //View v = convertView;

            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.rss_news_item, parent, false);

            /*if (v == null) {

                LayoutInflater vi;
                vi = LayoutInflater.from(this.context);
                v = vi.inflate(R.layout.rss_news_item, parent, false);

            }*/
            TextView title = (TextView) v.findViewById(R.id.newsItemTitle);
            title.setText(rssItems.get(position).getTitle());
            title.setTextColor(Color.parseColor("#FFFFFF"));

            TextView time = (TextView) v.findViewById(R.id.newsPubTime);
            time.setText(rssItems.get(position).getPubDate().toString());
            time.setTextColor(Color.parseColor("#AAAAAA"));

            TextView description = (TextView) v.findViewById(R.id.newsItemDescription);
            description.setText(rssItems.get(position).getDescription());
            description.setTextColor(Color.parseColor("#BBBBFF"));

            return v;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_news);
        // Show the Up button in the action bar.
        setupActionBar();

        Networker nw = new Networker();
        nw.setActivity(this);
        nw.execute();

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void errorDialog(String message)
    {
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

    public void processData(ArrayList<RssItem> rssItems)
    {
        /*for(RssItem rssItem : rssItems) {
            Log.i("RSS Reader", rssItem.getTitle());
            Log.i("RSS Reader", rssItem.getLink());
            Log.i("RSS Reader", rssItem.getDescription());
            Log.i("RSS Reader", rssItem.getPubDate().toString());
        }*/
        Log.w("RSS Reader", "count " + rssItems.size());
        mRssItems = rssItems;
        ListView listview = (ListView) findViewById(R.id.RssNewsListView);
        CustomAdapter adapter = new CustomAdapter(this, R.layout.rss_news_item, rssItems);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                Uri uriUrl = Uri.parse(mRssItems.get(position).getLink());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });


    }
}


class Networker extends AsyncTask<String, Void, ArrayList<RssItem>>
{
    private RSSNews mActivity;

    public void setActivity(RSSNews handler)
    {
        mActivity = handler;
    }

    @Override
    protected ArrayList<RssItem> doInBackground(String... params) {
        RssFeed feed = null;
        try {
            URL url = new URL("http://www.veikkausliiga.com/Rss.aspx");
            feed = RssReader.read(url);
        }
        catch (Exception e) {
            //errorDialog("Odottamaton virhe uutisten haussa. Yritä myöhemmin uudestaan.");
            e.printStackTrace();
        }

        ArrayList<RssItem> rssItems = feed.getRssItems();

        return rssItems;
    }

    @Override
    protected void onPostExecute(ArrayList<RssItem> data) {
        mActivity.processData(data);
        return;
    }
}
