package no.lqasse.zoff.Player;

import android.app.Activity;
import android.util.Log;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.util.List;

import no.lqasse.zoff.R;

public class YouTube_Player extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {
    private static final String API_KEY = "AIzaSyD8DpxXcLaQKrgjfmpVvxy1n-BjlrZh-c4";
    public static final String VIDEO_ID = "dKLftgvYsVU";
    private YouTubePlayerFragment youTubePlayerFragment;
    private YouTubePlayer player;
    private PlayerActivity playerActivity;
    private boolean initialized = false;


    public YouTube_Player(PlayerActivity p){
        youTubePlayerFragment = (YouTubePlayerFragment) p.getFragmentManager().findFragmentById(R.id.youtube_player);
        youTubePlayerFragment.initialize(API_KEY, this);
        playerActivity = p;


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

        }

        @Override
        public void onVideoEnded() {
            playerActivity.videoEnded();



        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {

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

    public void pause(){
        player.pause();
    }





}