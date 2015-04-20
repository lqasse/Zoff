package no.lqasse.zoff.Player;

import android.os.Handler;
import android.util.Log;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.util.List;

import no.lqasse.zoff.PlayerActivity;
import no.lqasse.zoff.R;

public class YouTube_Player extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {
    private static final String API_KEY = "AIzaSyD8DpxXcLaQKrgjfmpVvxy1n-BjlrZh-c4";
    private YouTubePlayerFragment youTubePlayerFragment;
    private YouTubePlayer player;
    private PlayerActivity playerActivity;
    private boolean initialized = false;


    public YouTube_Player(PlayerActivity p){
        youTubePlayerFragment = (YouTubePlayerFragment) p.getFragmentManager().findFragmentById(R.id.youtube_player);
        youTubePlayerFragment.initialize(API_KEY, this);
        playerActivity = p;
        final Handler handler = new Handler();
        Runnable updatePlaytime = new Runnable() {
            @Override
            public void run() {
                playerActivity.updatePlaytime();
                handler.postDelayed(this,500);
            }
        };

        handler.post(updatePlaytime);//





    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        player = youTubePlayer;
        player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        player.setPlayerStateChangeListener(playerStateChangeListener);
        initialized = true;
       playerActivity.playerInitialized();
        Log.d("YT_INIT", "SUCCESS");



    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Log.d("YT_INIT", "FAIL");
        youTubePlayerFragment.initialize(API_KEY, this);

    }



    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {
            playerActivity.togglePlayIcon();

        }



        @Override
        public void onVideoEnded() {
            playerActivity.videoEnded();



        }



        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            switch (errorReason){
                case NOT_PLAYABLE:
                    playerActivity.videoEnded();
                    playerActivity.videoError(YouTubePlayer.ErrorReason.NOT_PLAYABLE);
                    //player.next();



                    break;
            }

        }
    };



    public boolean isInitialized(){
        return this.initialized;
    }

    public void loadVideos(List<String> ids){
        player.loadVideos(ids);
    }

    public void loadVideo(String id){
        player.loadVideo(id);
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public void play(){
        player.play();

    }

    public void next(){
        if (player.hasNext()){
            player.next();
        }

    }


    public void pause(){
        if (player != null){
            player.pause();
        }

    }

    public int getCurrentMillis(){
        if (player != null){
            return player.getCurrentTimeMillis();
        }

        return 0;
    }

    public String getDuration(){
        if (player != null ){

            return millisToString(player.getDurationMillis());

        }

        return "";
    }

    public String getPlaytime(){
        if (player != null){

            try {
                return millisToString(player.getCurrentTimeMillis()) + " / " + millisToString(player.getDurationMillis());
            } catch (Exception e){
                return "00:00 / 00:00";
            }



        }

        return "";
    }



    private String millisToString(int millis){
        int durationHours = ((millis-millis%3600000)/3600000);
        millis = millis%3600000;

        int durationMins = ((millis-millis%60000)/60000);
        millis = millis%60000;

        int durationSecs = ((millis-millis%1000)/1000);


        String hrs = Integer.toString(durationHours);
        String mins = Integer.toString(durationMins);
        String secs = Integer.toString(durationSecs);


        if (durationMins<10 && durationHours>0){
            mins = "0"+mins;
        }

        if (durationSecs<10 && durationMins>=0){
            secs = "0"+secs;
        }

        if (durationHours == 0){
            return mins + ":" + secs;
        } else {
            return (hrs + ":" + mins + ":" + secs);
        }
    }








}