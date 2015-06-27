package no.lqasse.zoff.Models;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;

import no.lqasse.zoff.Helpers.Sha256;
import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.Server.JSONTranslator;
import no.lqasse.zoff.Server.Server;

/**
 * Created by lassedrevland on 11.01.15.
 */
public class ZoffController {

    public static final String BUNDLEKEY_CHANNEL = "channel";
    public static final String BUNDLEKEY_IS_NEW_CHANNEL = "NEW";
    private static final String LOG_IDENTIFIER = "Zoff_LOG";
    private ZoffModel zoff;
    private Server server;

    private RefreshCallback refreshCallback;
    private CorrectPasswordCallback correctPasswordCallback;
    private ToastMessageCallback toastMessageCallback;

    private static ZoffController controller;

    public static ZoffController getInstance(String channel, Context context){
        if (controller != null){
            if (controller.getZoff().getChannel().equals(channel)){
                controller.removeCallbacks();
                return controller;
            }



        }

        ImageCache.empty();
        controller = new ZoffController(channel,context);
        return controller;

    }

    public ZoffController(String channel, Context context) {
        zoff = new ZoffModel(channel);
        zoff.setAndroid_id(Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID));

        log(channel);
        server = new Server(channel, this, zoff.getAndroid_id());


    }


    public void setOnRefreshListener(RefreshCallback callback) {
        this.refreshCallback = callback;
    }

    public void setCorrectPasswordCallback(CorrectPasswordCallback callback) {
        this.correctPasswordCallback = callback;
    }

    public void setToastMessageCallback(ToastMessageCallback callback) {
        this.toastMessageCallback = callback;
    }

    public void removeCallbacks() {
        this.refreshCallback = null;
        this.correctPasswordCallback = null;
        this.toastMessageCallback = null;
    }

    public ZoffModel getZoff() {
        return zoff;
    }


    public void showToast(String toastKeyword) {
        if (toastMessageCallback != null) {
            toastMessageCallback.onToastReceived(toastKeyword);
        }

    }

    public void onListRefreshed(JSONArray data) {

        zoff.setVideos(JSONTranslator.createVideoListFromJSON(data));
        zoff.setSettings(JSONTranslator.createSettingsFromJSON(data));


        if (refreshCallback != null) {
            refreshCallback.onZoffRefreshed(zoff);
        }

    }


    public void onVoteVideo(JSONArray data){

        zoff.addVote(JSONTranslator.getVoteMessage(data));


        if (refreshCallback != null) {
            refreshCallback.onZoffRefreshed(zoff);
        }

    }

    public void onDeleteVideo(JSONArray data){

        zoff.deleteVideo(JSONTranslator.getContentID(data));

        if (refreshCallback != null) {
            refreshCallback.onZoffRefreshed(zoff);
        }

    }

    public void onVideoAdded(JSONArray data){

        zoff.addVideo(JSONTranslator.createVideoFromJSON(data));

        if (refreshCallback != null) {
            refreshCallback.onZoffRefreshed(zoff);
        }
    }

    public void onSongChange(JSONArray data){
        zoff.getPlayingVideo().setAdded(JSONTranslator.getTimeAdded(data));
        zoff.getPlayingVideo().setNullVotes();
        zoff.setNextNowPlaying();

        zoff.getSettings().setNowPlayingStartTimeMillis(JSONTranslator.getTimeAdded(data));

        if (refreshCallback != null) {
            refreshCallback.onZoffRefreshed(zoff);
        }

    }

    public void viewersChanged(int viewers) {

        zoff.setCurrentViewers(viewers);

        if (refreshCallback != null) {
            refreshCallback.onZoffRefreshed(zoff);
        }


    }

    public void onCorrectPassword(String password) {
        if (correctPasswordCallback != null) {
            correctPasswordCallback.onCorrectPassword(password);
        }

        zoff.setAdminpass(password);

    }




    public void vote(Video video) {
        server.vote(video, zoff.getAdminpass());
    }

    public void shuffle() {
        server.shuffle(zoff.getAdminpass());
    }

    public void add(String id, String title, String duration) {
        server.add(id, title, zoff.getAdminpass(), duration);
    }

    public void skip() {
        server.skip(zoff.getAdminpass());

    }

    public void delete(Video video) {
        server.delete(video, zoff.getAdminpass());
    }

    public void savePassword(String password) {
        server.savePassword(Sha256.getHash(password));
    }

    public void saveSettings(ZoffSettings settings) {
        server.saveSettings(zoff.getAdminpass(), settings);

    }


    @Override
    public String toString() {
        return zoff.getChannel() + ": " + zoff.getVideos().size();
    }

    public void disconnect() {
        server.off();

    }

    private void log(String data) {
        Log.i(LOG_IDENTIFIER, data);
    }


    public interface RefreshCallback {
        void onZoffRefreshed(ZoffModel zoff);
    }

    public interface CorrectPasswordCallback {
        void onCorrectPassword(String password);
    }

    public interface ToastMessageCallback {
        void onToastReceived(String toastkeyword);
    }


}




