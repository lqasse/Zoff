package no.lqasse.zoff.Player;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubePlayer;

import java.net.URL;
import java.util.ArrayList;

import no.lqasse.zoff.Helpers.ImageBlur;
import no.lqasse.zoff.Helpers.ImageCache;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Adapters.PlayerListAdapter;
import no.lqasse.zoff.Zoff;
import no.lqasse.zoff.ZoffActivity;
import no.lqasse.zoff.ZoffListener;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Search.SearchActivity;
import no.lqasse.zoff.SettingsActivity;

public class PlayerActivity extends ZoffActivity implements ZoffListener {
    private String NOW_PLAYING_ID = "";
    private YouTube_Player player;
    private final Handler handler = new Handler();
    private PlayerListAdapter listAdapter;
    private ArrayList<Video> videoList = new ArrayList<>();

    private Menu menu;
    private ListView videoListView;
    private TextView titleLabel;
    private TextView videoDurationLabel;
    private TextView currentTimeLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        ROOM_NAME = b.getString("ROOM_NAME");
        zoff = new Zoff(ROOM_NAME, this);


        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(zoff.getROOM_NAME());
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        titleLabel = (TextView) findViewById(R.id.titleView);
        videoDurationLabel = (TextView) findViewById(R.id.video_length_view);
        currentTimeLabel = (TextView) findViewById(R.id.video_current_time_view);
        videoListView = (ListView) findViewById(R.id.videoList);


        listAdapter = new PlayerListAdapter(this, videoList, zoff);


        videoListView.setAdapter(listAdapter);

        videoListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Video selectedVideo = listAdapter.getItem(position);
                zoff.vote(selectedVideo);


                return true;
            }
        });

        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ToastMaster.showToast(getBaseContext(), ToastMaster.TYPE.HOLD_TO_VOTE);


            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);


        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        MenuItem play = menu.findItem(R.id.action_togglePlay);


        switch (id) {
            case (R.id.action_settings):
                homePressed = false;
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.putExtra("ROOM_NAME", ROOM_NAME);
                startActivity(settingsIntent);
                break;
            case (R.id.action_skip):
                zoff.voteSkip();
                zoff.refreshData();
                break;
            case (R.id.action_togglePlay):
                togglePlay();


                break;
            case (R.id.action_search):
                homePressed = false;
                player.pause();
                Intent searchIntent = new Intent(this, SearchActivity.class);
                searchIntent.putExtra("ROOM_NAME", ROOM_NAME);
                startActivity(searchIntent);


                break;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        zoff.stopRefresh();

    }

    @Override
    protected void onPause() {
        player.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopNotificationService();

        if (player == null) {
            player = new YouTube_Player(this);

        } else {
            player.loadVideos(zoff.getVideoIDs());

        }


    }


    public void zoffRefreshed(Boolean hasInetAccess) {

        videoList.clear();
        videoList.addAll(zoff.getNextVideos());

        listAdapter.notifyDataSetChanged();

        titleLabel.setText(zoff.getNowPlayingTitle());
        currentTimeLabel.setText(zoff.getViewers());

        if (!ImageCache.has(zoff.getNowPlayingID() + "_blur") && ImageCache.has(zoff.getNowPlayingID())){
            downloadBG downloadBG = new downloadBG();
            downloadBG.execute("");
        } else if (ImageCache.has(zoff.getNowPlayingID()+"_blur")){
            setBackgroundImage(ImageCache.getCurrentBlurBG());
        }





        //play next video if current playing != zoff-currentplaying
        if (!NOW_PLAYING_ID.equals(zoff.getNowPlayingID()) && !NOW_PLAYING_ID.equals("")) {
            //videoEnded();
            player.loadVideos(zoff.getVideoIDs());

            player.next();
            NOW_PLAYING_ID = zoff.getNowPlayingID();
        }


    }


    private void loadFirstVideo() {

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                loadFirstVideo();
            }
        };

        if (zoff.hasVideos() && (player.isInitialized())) {
            player.loadVideos(zoff.getVideoIDs());
            NOW_PLAYING_ID = zoff.getNowPlayingID();
            handler.removeCallbacks(r, this);
        } else {

            handler.postDelayed(r, 100);


        }
    }


    public void videoEnded() {

        zoff.voteSkip();
        zoff.refreshData();


    }

    public void videoError(YouTubePlayer.ErrorReason errorReason) {
        switch (errorReason) {
            case NOT_PLAYABLE:

                videoEnded();
                ToastMaster.showToast(getBaseContext(), ToastMaster.TYPE.EMBEDDING_DISABLED,zoff.getNowPlayingTitle());

                break;
        }

    }

    public void playerInitialized() {
        loadFirstVideo();

    }

    public void setBackgroundImage(Bitmap blurBg) {
        LinearLayout l = (LinearLayout) findViewById(R.id.layout);
        l.setBackground(new BitmapDrawable(getBaseContext().getResources(), blurBg));
    }


    private class downloadBG extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap b;
            try {
                URL imageURL = new URL(zoff.getNowPlayingVideo().getThumbMed());
                b = BitmapFactory.decodeStream(imageURL.openStream());
            } catch (Exception e) {
                Log.d("ERROR", e.getLocalizedMessage());
                e.printStackTrace();
                b = null;
            }
            return b;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            ImageBlur.createAndSetBlurBG(bitmap, PlayerActivity.this, zoff.getNowPlayingID());

            super.onPostExecute(bitmap);


        }


    }


    public void updatePlaytime() {
        videoDurationLabel.setText(player.getPlaytime());
    }

    public void togglePlayIcon() {
        MenuItem menuItem = menu.findItem(R.id.action_togglePlay);
        if (player != null) {
            if (player.isPlaying()) {
                menuItem.setIcon(R.drawable.play);
            } else {
                menuItem.setIcon(R.drawable.pause);
            }
        }//777
    }

    public void togglePlay() {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();

            } else {
                player.play();
            }
            togglePlayIcon();

        }
    }






}