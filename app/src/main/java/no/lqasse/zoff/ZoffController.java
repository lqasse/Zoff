package no.lqasse.zoff;

import android.util.Log;

import org.json.JSONArray;

import no.lqasse.zoff.Helpers.Sha256;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.ImageTools.BitmapCache;
import no.lqasse.zoff.Models.Playlist;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Models.Zoff;
import no.lqasse.zoff.Models.Settings;
import no.lqasse.zoff.Server.JSONTranslator;
import no.lqasse.zoff.Server.Server;

/**
 * Created by lassedrevland on 11.01.15.
 */
public class ZoffController implements Server.Listener{
    public static final String BUNDLEKEY_CHANNEL = "channel";
    public static final String BUNDLEKEY_IS_NEW_CHANNEL = "NEW";
    private static final String LOG_IDENTIFIER = "ZoffController";
    private static ZoffController controller;

    private Zoff zoff;
    protected Server server;
    private RefreshCallback refreshCallback;
    private CorrectPasswordCallback correctPasswordCallback;
    private ToastMessageCallback toastMessageCallback;
    private PlaylistCallback playlistCallback;

    public static ZoffController getInstance(String channel) {
        if (controller != null) {
            if (controller.getZoff().getChannel().equals(channel)) {
                controller.removeCallbacks();
                controller.onGotInstance();
                Log.d("Controller", " got instance " + controller.toString());

                return controller;
            }
        }

        BitmapCache.empty();
        controller = new ZoffController(channel);
        Log.i(LOG_IDENTIFIER,  " got NEW instance " + controller.toString());
        return controller;

    }

    public static void StopInstance() {
        controller = null;
        Log.i(LOG_IDENTIFIER, "Instance stopped");
    }

    public ZoffController(String channel) {
        zoff = new Zoff(channel);
        log(channel);
        server = new Server(channel,this);
    }

    public Playlist getPlaylist(){
        return zoff.getPlaylist();
    }

    public Video getCurrentlyPlayingVideo(){
        return zoff.getPlaylist().getNowPlaying();
    }

    public void onGotInstance(){
        server.ping();
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

    public void setPlaylistCallback(PlaylistCallback playlistCallback){
        this.playlistCallback = playlistCallback;
    }

    public void removeCallbacks() {
        this.refreshCallback = null;
        this.correctPasswordCallback = null;
        this.toastMessageCallback = null;
    }

    public void vote(Video video) {
        server.vote(video, zoff.getAdminpass());
        toastMessageCallback.showToast(ToastMaster.TYPE.VIDEO_VOTED, video.getTitle());
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
        toastMessageCallback.showToast(ToastMaster.TYPE.VIDEO_DELETED, video.getTitle());
    }

    public void savePassword(String password) {
        server.savePassword(Sha256.getHash(password));
    }

    public void saveSettings(Settings settings) {
        server.saveSettings(zoff.getAdminpass(), settings);
    }

    public void refreshPlaylist() {
        server.getPlaylist();
    }

    public void disconnect() {
        StopInstance();
        try {
            finalize();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void onToast(String toastKeyword) {
        if (toastMessageCallback != null) {
            toastMessageCallback.showToast(toastKeyword);
        }
    }

    public void onConfigurationChanged(JSONArray data) {
        zoff.setSettings(JSONTranslator.createSettingsFromJSON(data));
    }

    public void onListRefreshed(JSONArray data) {
        zoff.setVideos(JSONTranslator.createVideoListFromJSON(data));
        if (playlistCallback != null){
            playlistCallback.onGotPlaylist();
        }

        if (refreshCallback != null) {
            refreshCallback.onZoffRefreshed(zoff);
        }
    }

    public void onVideoGotVote(JSONArray data) {
        zoff.addVote(JSONTranslator.getVoteMessage(data));
        if (refreshCallback != null) {
            refreshCallback.onZoffRefreshed(zoff);
        }
    }

    public void onVideoDeleted(JSONArray data) {
        zoff.deleteVideo(JSONTranslator.getContentID(data));
        if (refreshCallback != null) {
            refreshCallback.onZoffRefreshed(zoff);
        }
    }

    public void onVideoAdded(JSONArray data) {
        zoff.addVideo(JSONTranslator.createVideoFromJSON(data));
        if (refreshCallback != null) {
            refreshCallback.onZoffRefreshed(zoff);
        }
    }

    public void onVideoChanged(JSONArray data) {
        zoff.getPlaylist().setNowPlaying(JSONTranslator.getVideoChangeMessage(data));

        if (refreshCallback != null) {
            refreshCallback.onZoffRefreshed(zoff);
        }
    }

    public void onViewersChanged(int viewers) {
        zoff.setCurrentViewers(viewers);
        if (refreshCallback != null) {
            refreshCallback.onZoffRefreshed(zoff);
        }
    }

    public void onCorrectPassword(String password) {
        zoff.setAdminpass(password);
        if (correctPasswordCallback != null) {
            correctPasswordCallback.onCorrectPassword(password);
        }
    }

    public Zoff getZoff() {
        return zoff;
    }

    public interface PlaylistCallback {
        void onGotPlaylist();
    }

    public interface RefreshCallback {
        void onZoffRefreshed(Zoff zoff);
    }

    public interface CorrectPasswordCallback {
        void onCorrectPassword(String password);
    }

    public interface ToastMessageCallback {
        void showToast(String toastkeyword);
        void showToast(ToastMaster.TYPE type, String contextual);
    }

    @Override
    public String toString() {
        return zoff.getChannel() + ": " + zoff.getPlaylist().size();
    }

    @Override
    protected void finalize() throws Throwable {
        log("Finalized controller");
        server.disconnect();
        super.finalize();
    }

    private void log(String data) {
        Log.i(LOG_IDENTIFIER, data);
    }
}




