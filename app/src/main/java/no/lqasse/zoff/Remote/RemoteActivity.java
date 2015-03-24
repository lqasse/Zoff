package no.lqasse.zoff.Remote;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;


import no.lqasse.zoff.Datatypes.Zoff;
import no.lqasse.zoff.Datatypes.Zoff_Listener;
import no.lqasse.zoff.Helpers.ImageBlur;
import no.lqasse.zoff.Helpers.ImageCache;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.Models.ZoffVideo;
import no.lqasse.zoff.NotificationService;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Search.SearchActivity;
import no.lqasse.zoff.SettingsActivity;

/**
 * Created by lassedrevland on 21.01.15.
 */
public class RemoteActivity extends ActionBarActivity implements Zoff_Listener{
    private final String PREFS_FILE = "no.lqasse.zoff.prefs";
    private String ROOM_NAME;
    private String ROOM_PASS;

    private Zoff zoff;
    private Menu menu;
    private Boolean paused = false;
    private ListView videoList;
    private RemoteListAdapter adapter;
    private SharedPreferences sharedPreferences;

    private Handler h = new Handler();
    private Runnable r;


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
        adapter = new RemoteListAdapter(this, zoff.getVideos(),zoff);
        videoList = (ListView) findViewById(R.id.videoList);
        videoList.setAdapter(adapter);

        videoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {


                if (position != 0){ //Cant vote for current video duh
                    ZoffVideo selectedVideo =  adapter.getItem(position);
                    zoff.vote(selectedVideo);
                }


                return true;
            }
        });

        videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0){ //Currently playing video cant be voted on
                    ToastMaster.showToast(getBaseContext(), ToastMaster.TYPE.HOLD_TO_VOTE);
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

                    ToastMaster.showToast(getBaseContext(), ToastMaster.TYPE.NEEDS_PASS_VOTE);
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
                    ToastMaster.showToast(getBaseContext(), ToastMaster.TYPE.SHUFFLED);
                    zoff.shuffle();
                } else {
                    ToastMaster.showToast(getBaseContext(), ToastMaster.TYPE.NEEDS_PASS_SHUFFLE);
                }
                break;


        }


        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onStop() {


        paused = true;

        super.onStop();
    }

    @Override
    public void zoffRefreshed(Boolean hasInetAccess) {
        adapter.notifyDataSetChanged();

        if (!ImageCache.has(zoff.getNowPlayingID()+"_blur") && ImageCache.has(zoff.getNowPlayingID())){

            ImageBlur.createAndSetBlurBG(ImageCache.get(zoff.getNowPlayingID()),this);

        }
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




    public void setBackgroundImage(Bitmap blurBg){

        LinearLayout l = (LinearLayout) findViewById(R.id.layout);
        l.setBackground(new BitmapDrawable(getBaseContext().getResources(),blurBg));
    }











}


