package no.lqasse.zoff.Models;

import java.util.concurrent.TimeUnit;

/**
 * Created by lassedrevland on 02.05.15.
 */
public class ZoffSettings {

    public static final String KEY_ADD_SONGS = "addsongs";
    public static final String KEY_ALL_VIDEOS = "allvideos";
    public static final String KEY_FRONTPAGE  = "frontpage";
    public static final String KEY_LONG_SONGS = "longsongs";
    public static final String KEY_SHUFFLE    = "shuffle";
    public static final String KEY_SKIP       = "skip";
    public static final String KEY_VOTE       = "vote";
    public static final String KEY_REMOVE_PLAY= "removeplay";

    public static final String[] KEYS =
            {KEY_ADD_SONGS,
                    KEY_ALL_VIDEOS,
                    KEY_FRONTPAGE,
                    KEY_LONG_SONGS,
                    KEY_SHUFFLE,
                    KEY_SKIP,
                    KEY_VOTE,
                    KEY_REMOVE_PLAY
            };


    private String _id;
    private int skips;
    private int viewers;
    private long startTimeMillis;
    private boolean addsongs;
    private boolean allvideos;
    private boolean frontpage;
    private boolean longsongs;
    private boolean removeplay;
    private boolean shuffle;
    private boolean skip;
    private boolean vote;


    public ZoffSettings(String _id, int skips, int viewers, int startTimeSecs, boolean addsongs, boolean allvideos, boolean longsongs, boolean frontpage, boolean removeplay, boolean shuffle, boolean skip, boolean vote) {
        this._id = _id;
        this.skips = skips;
        this.viewers = viewers;
        this.startTimeMillis = TimeUnit.SECONDS.toMillis(startTimeSecs);
        this.addsongs = addsongs;
        this.allvideos = allvideos;
        this.longsongs = longsongs;
        this.frontpage = frontpage;
        this.removeplay = removeplay;
        this.shuffle = shuffle;
        this.skip = skip;
        this.vote = vote;
    }

    public ZoffSettings(boolean addsongs, boolean allvideos, boolean frontpage, boolean longsongs, boolean removeplay, boolean shuffle, boolean skip, boolean vote) {
        this.addsongs = addsongs;
        this.allvideos = allvideos;
        this.frontpage = frontpage;
        this.longsongs = longsongs;
        this.removeplay = removeplay;
        this.shuffle = shuffle;
        this.skip = skip;
        this.vote = vote;
    }

    public String get_id() {
        return _id;
    }

    public int getSkips() {
        return skips;
    }

    public int getViewers() {
        return viewers;
    }

    public long getNowPlayingStartTimeMillis() {
        return startTimeMillis;
    }

    public boolean isAddsongs() {
        return addsongs;
    }

    public boolean isAllvideos() {
        return allvideos;
    }

    public boolean isFrontpage() {
        return frontpage;
    }

    public boolean isLongsongs() {
        return longsongs;
    }

    public boolean isRemoveplay() {
        return removeplay;
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public boolean isSkip() {
        return skip;
    }

    public boolean isVote() {
        return vote;
    }
}
