package no.lqasse.zoff.Remote;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;


import no.lqasse.zoff.Datatypes.TOAST_TYPES;
import no.lqasse.zoff.Datatypes.Zoff;
import no.lqasse.zoff.NotificationService;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Search.SearchActivity;
import no.lqasse.zoff.SettingsActivity;

/**
 * Created by lassedrevland on 21.01.15.
 */
public class RemoteActivity extends ActionBarActivity {
    private final String PREFS_FILE = "no.lqasse.zoff.prefs";
    private String ROOM_NAME;
    private String ROOM_PASS;

    private Zoff zoff;
    private Menu menu;
    private Boolean paused = false;
    private ListView videoList;
    private remoteListAdapter adapter;
    private SharedPreferences sharedPreferences;

    private Handler h = new Handler();
    private Runnable r;



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        if (b != null) {
            ROOM_NAME = b.getString("ROOM_NAME");
        }




        ROOM_PASS = getPASS();


        zoff = new Zoff(ROOM_NAME, this);
        zoff.setROOM_PASS(getPASS());
        setContentView(R.layout.activity_remote);
        getSupportActionBar().setIcon(R.drawable.logo);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(zoff.getROOM_NAME());
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        //Handle everything listview related
        adapter = new remoteListAdapter(this, zoff.getVideos(),zoff);
        videoList = (ListView) findViewById(R.id.videoList);
        videoList.setAdapter(adapter);

        videoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    zoff.vote(position);

                return true;
            }
        });

        videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0){ //Currently playing video cant be voted on
                    zoff.showToast(TOAST_TYPES.HOLD_TO_VOTE);
                }



            }
        });

        r = new Runnable() {
            @Override
            public void run() {
             Intent i = new Intent(getBaseContext(),NotificationService.class);

            }
        };



    }

    @Override
    protected void onResume() {
        zoff.resumeRefresh();
        super.onResume();
    }

    @Override
    protected void onPause() {
        zoff.pauseRefresh();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zoff.pauseRefresh();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_remote, menu);
        this.menu = menu;
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case (R.id.action_settings):
                break;
            case (R.id.action_next):
                zoff.voteSkip();
                //player.loadVideo(zoff.getNextId());
                zoff.refreshData();

                break;
            case (R.id.action_search):
                //Display youtub

                if (zoff.ANYONE_CAN_ADD()) {
                    Intent i = new Intent(this, SearchActivity.class);
                    i.putExtra("ROOM_NAME", ROOM_NAME);
                    i.putExtra("ROOM_PASS", ROOM_PASS);
                    i.putExtra("ALL_VIDEOS_ALLOWED", zoff.ALL_VIDEOS_ALLOWED());
                    i.putExtra("LONG_SONGS",zoff.LONG_SONGS());
                    startActivity(i);
                }else if (!zoff.ANYONE_CAN_ADD() && zoff.hasROOM_PASS()){
                    Intent i = new Intent(this, SearchActivity.class);
                    i.putExtra("ROOM_NAME", ROOM_NAME);
                    i.putExtra("ROOM_PASS", ROOM_PASS);
                    i.putExtra("ALL_VIDEOS_ALLOWED", zoff.ALL_VIDEOS_ALLOWED());
                    i.putExtra("LONG_SONGS",zoff.LONG_SONGS());

                    startActivity(i);
                } else {



                    zoff.showToast(TOAST_TYPES.NEEDS_PASS_VOTE);
                }


                break;

            case (R.id.action_zoff_settings):
                //Display ZOff settings

                Intent i = new Intent(this, SettingsActivity.class);
                i.putExtras(zoff.getSettingsBundle());


                startActivity(i);
                break;
            case (R.id.action_shuffle):
                if (zoff.hasROOM_PASS()){
                    zoff.showToast(TOAST_TYPES.SHUFFLED);
                    zoff.shuffle();
                } else {

                    zoff.showToast(TOAST_TYPES.NEEDS_PASS_SHUFFLE);
                }
                break;


        }


        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onStop() {

        /* TODO Fix ongoing Notification
        Intent service = new Intent(this,NotificationService.class);
        service.putExtra("ROOM_NAME",ROOM_NAME);
        startService(service);
        */
        paused = true;
        //showNotification();

        super.onStop();
    }

    @Override
    protected void onStart() {
        stopService(new Intent(NotificationService.class.getName()));

        super.onStart();
    }

    public void zoffRefreshed(boolean hasInetAccess) {
          adapter.notifyDataSetChanged();
    }

    private String getPASS(){
        String PASS;
        sharedPreferences = getSharedPreferences(PREFS_FILE,0);
        PASS = sharedPreferences.getString(ROOM_NAME,null);

        return PASS;

    }



    public Zoff getZoff(){
        return this.zoff;
    }


    /**
     * Created by lassedrevland on 12.12.14.
     */
    public static class remoteListAdapter extends ArrayAdapter<Zoff.Video> {
        private final Context context;
        //private final String[] values;
        private final ArrayList<Zoff.Video> results;
        private Zoff zoff;



        public remoteListAdapter(Context context, ArrayList<Zoff.Video> results, Zoff zoff) {
            super(context, R.layout.now_playing_row, results);
            this.context = context;
            this.results = results;
            this.zoff = zoff;


        }



        private static
        class ViewHolder{
            ImageView imageView;
            String imageURL;
            Bitmap bitmap;
            int position;
            ProgressBar progressBar;
            Boolean hqImage = false;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            viewHolder = new ViewHolder();

            View rowView;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            if (position == 0){ //Top of list, Now playing!
                rowView = inflater.inflate(R.layout.now_playing_row_top, parent, false);
                viewHolder.imageURL = results.get(position).getThumbBig();




            } else {
                rowView = inflater.inflate(R.layout.now_playing_row, parent, false);
                viewHolder.imageURL = results.get(position).getThumbMed();
            }

            ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
            ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);

            viewHolder.imageView = imageView;
            viewHolder.position = position;
            viewHolder.progressBar = progressBar;


            if (position == 0 && results.get(position).getImgBig() == null){ //TOp image is not in hq
                viewHolder.hqImage = true;
                new downloadImage().execute(viewHolder);
            } else if (position == 0 && results.get(position).getImgBig() != null){ //TOp image is in hq
                imageView.setImageBitmap(results.get(position).getImgBig());
                progressBar.setVisibility(View.GONE);
            } else if (results.get(position).getImg() == null){ //Not top, not downloaded
                new downloadImage().execute(viewHolder);
            } else {
                imageView.setImageBitmap(results.get(position).getImg());//Not top,  downloaded
                progressBar.setVisibility(View.GONE);
            }


            TextView title = (TextView) rowView.findViewById(R.id.titleView);
            TextView votes = (TextView) rowView.findViewById(R.id.votesView);
            TextView views = (TextView) rowView.findViewById(R.id.viewsLabel);
            TextView playtime = (TextView) rowView.findViewById(R.id.playtimeLabel);






            title.setText(results.get(position).getTitle());

            if (votes != null){

                votes.setText(results.get(position).getVotes());

            } else {
                views.setText(zoff.getVIEWERS_STRING());
                playtime.setText(""); //Implement later?

            }




            return rowView;
        }



        private class downloadImage extends AsyncTask<ViewHolder, Void, ViewHolder> {





            @Override
            protected ViewHolder doInBackground(ViewHolder... params){
                ViewHolder viewHolder = params[0];

                try {
                    URL imageURL = new URL(viewHolder.imageURL);
                    viewHolder.bitmap = BitmapFactory.decodeStream(imageURL.openStream());
                } catch (Exception e){
                    Log.d("ERROR", e.getLocalizedMessage());
                    e.printStackTrace();
                    viewHolder.bitmap = null;
                }
                return viewHolder;
            }

            @Override
            protected void onPostExecute(ViewHolder result){
                if (result.bitmap == null){
                    Log.d("FAIL", "NO IMAGE");
                } else {
                    result.progressBar.setVisibility(View.GONE);

                    //Animate fade in <3
                    Animation a = new AlphaAnimation(0.00f,1.00f);
                    a.setInterpolator(new DecelerateInterpolator());
                    a.setDuration(700);
                    result.imageView.setImageBitmap(result.bitmap);
                    result.imageView.setAnimation(a);
                    result.imageView.startAnimation(a);

                    result.imageView.setImageBitmap(result.bitmap);
                    if (result.hqImage){
                        results.get(result.position).setImgBig(result.bitmap);
                    } else {
                        results.get(result.position).setImg(result.bitmap);
                    }




                }
            }



        }

    }


    public void setBlurBg(Bitmap blurBg){


        LinearLayout l = (LinearLayout) findViewById(R.id.layout);
        l.setBackground(new BitmapDrawable(getBaseContext().getResources(),blurBg));
    }











}


