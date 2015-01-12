package no.lqasse.zoff;

import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewStub;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.util.ArrayList;
import java.util.List;



public class Player extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private static final String API_KEY = "AIzaSyD8DpxXcLaQKrgjfmpVvxy1n-BjlrZh-c4";
    public static final String VIDEO_ID = "dKLftgvYsVU";
    YouTubePlayerFragment youTubePlayerFragment;
    private YouTubePlayer ytPlayer;
    private Zoff zoff;
    private Main main;
    private Handler handler;





    public Player(Main main, String room){
        this.zoff = zoff;
        this.main = main;
        zoff = new Zoff(room, this);
        handler = new Handler();



        main.setContentView(R.layout.yt_player);
        youTubePlayerFragment = (YouTubePlayerFragment) main.getFragmentManager().findFragmentById(R.id.youtube_player);
        youTubePlayerFragment.initialize(API_KEY, this);
    }
    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult result) {
        Toast.makeText(this, "Failured to Initialize!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer ytPlayer, boolean wasRestored) {

        /** add listeners to YouTubePlayer instance **/
        ytPlayer.setPlayerStateChangeListener(playerStateChangeListener);
        ytPlayer.setPlaybackEventListener(playbackEventListener);

        /** Start buffering **/
        if (!wasRestored) {
            //ytPlayer.cueVideo(VIDEO_ID);

        }

        this.ytPlayer = ytPlayer;
        //ytPlayer.loadVideos(zoff.getVideoIDs());


    }

    private PlaybackEventListener playbackEventListener = new PlaybackEventListener() {

        @Override
        public void onBuffering(boolean arg0) {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onPlaying() {

        }

        @Override
        public void onSeekTo(int arg0) {

        }

        @Override
        public void onStopped() {

        }

    };

    private PlayerStateChangeListener playerStateChangeListener = new PlayerStateChangeListener() {

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onError(ErrorReason arg0) {

        }

        @Override
        public void onLoaded(String arg0) {

        }

        @Override
        public void onLoading() {
        }

        @Override
        public void onVideoEnded() {

        }

        @Override
        public void onVideoStarted() {

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void playVideo(String VIDEO_ID){
        ytPlayer.cueVideo(VIDEO_ID);
    }

    public void loadVideos(List<String> ids){
        zoff.L();
        if (zoff.hasVideos()){
            if (ytPlayer != null){
                ytPlayer.loadVideos(ids);
            } else {


                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        loadVideos(zoff.getVideoIDs());
                    }
                };

                handler.postDelayed(r,100);

            }

            populateVideoList();
        }


    }

    public void show(){
        main.setContentView(R.layout.yt_player);
    }

    private void populateVideoList(){
        ListView videoList = (ListView) main.findViewById(R.id.videoList);

        ArrayList<Video> nextVideos = zoff.getVideos();
        nextVideos.remove(0); //Remove currently playing video

        final nowPlayingAdapter adapter = new nowPlayingAdapter(main,nextVideos);
        videoList.setAdapter(adapter);
    }


}

