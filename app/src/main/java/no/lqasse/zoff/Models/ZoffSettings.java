package no.lqasse.zoff.Models;

import java.util.concurrent.TimeUnit;

/**
 * Created by lassedrevland on 02.05.15.
 */
public class ZoffSettings {

    public static final String KEY_ADD_SONGS  = "addsongs";
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

    public ZoffSettings() {

        this._id            = "";
        this.skips          = 0;
        this.viewers        = 0;
        this.startTimeMillis = 0;
        this.addsongs       = false;
        this.allvideos      = false;
        this.longsongs      = false;
        this.frontpage      = false;
        this.removeplay     = false;
        this.shuffle        = false;
        this.skip           = false;
        this.vote           = false;
    }

    public ZoffSettings(ZoffSettings.Builder builder){
        this._id                = builder.nested_id;
        this.skips              = builder.nestedSkips;
        this.viewers            = builder.nestedViewers;
        this.startTimeMillis    = builder.nestedStartTimeMillis;
        this.addsongs           = builder.nestedAddsongs;
        this.allvideos          = builder.nesttedAllvideos;
        this.longsongs          = builder.nestedFrontpage;
        this.frontpage          = builder.nestedLongsongs;
        this.removeplay         = builder.nestedRemoveplay;
        this.shuffle            = builder.nestedShuffle;
        this.skip               = builder.nestedSkip;
        this.vote               = builder.nestedVote;

        invertValuesToMatchSwitches();
    }

    private void invertValuesToMatchSwitches(){
        addsongs = !addsongs;
        vote = !vote;
        shuffle = !shuffle;
        skip = !skip;
        allvideos = !allvideos;
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

    public void setNowPlayingStartTimeMillis(int startTimeMillis){
        this.startTimeMillis = startTimeMillis;
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

    public static class Builder{

        String  nested_id;
        int     nestedSkips;
        int     nestedViewers;
        long    nestedStartTimeMillis;
        boolean nestedAddsongs;
        boolean nesttedAllvideos;
        boolean nestedFrontpage;
        boolean nestedLongsongs;
        boolean nestedRemoveplay;
        boolean nestedShuffle;
        boolean nestedSkip;
        boolean nestedVote;






        public ZoffSettings.Builder _id(String _id){
            this.nested_id = _id;
            return this;
        }

        public ZoffSettings.Builder numberOfSkips(int skips){
            this.nestedSkips = skips;
            return this;
        }
        public ZoffSettings.Builder numberOfViewers(int viewers){
            this.nestedViewers = viewers;
            return this;
        }
        public ZoffSettings.Builder startTimeSeconds(int secs){
            this.nestedStartTimeMillis = TimeUnit.SECONDS.toMillis(secs);
            return this;
        }

        public ZoffSettings.Builder allowsAddsongs(Boolean addsongs){
            this.nestedAddsongs = addsongs;
            return this;
        }
        public ZoffSettings.Builder allvideos(Boolean allvideos){
            this.nesttedAllvideos = allvideos;
            return this;
        }
        public ZoffSettings.Builder frontpage(Boolean frontpage){
            this.nestedFrontpage = frontpage;
            return this;
        }
        public ZoffSettings.Builder longsongs(Boolean longsongs){
            this.nestedLongsongs = longsongs;
            return this;
        }
        public ZoffSettings.Builder removeplay(Boolean removeplay){
            this.nestedRemoveplay = removeplay;
            return this;
        }
        public ZoffSettings.Builder shuffle(Boolean shuffle){
            this.nestedShuffle = shuffle;
            return this;
        }
        public ZoffSettings.Builder skip(Boolean skip){
            this.nestedSkip = skip;
            return this;
        }
        public ZoffSettings.Builder vote(Boolean vote){
            this.nestedVote = vote;
            return this;
        }










        public ZoffSettings build() {

            return new ZoffSettings(this) {


            };

        }



    }
}
