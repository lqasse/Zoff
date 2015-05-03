package no.lqasse.zoff.Models;

/**
 * Created by lassedrevland on 16.04.15.
 */
public class ChanSuggestion implements Comparable<ChanSuggestion>{
    private int viewers;
    private String nowPlayingId;
    private String nowPlayingTitle;
    private String name;
    private int songs;

    public ChanSuggestion(int viewers, String nowPlayingId, String nowPlayingTitle, String name, int songs) {
        this.viewers = viewers;
        this.nowPlayingId = nowPlayingId;
        this.nowPlayingTitle = nowPlayingTitle;
        this.name = name;
        this.songs = songs;
    }

    public int getViewers() {
        return viewers;
    }

    public String getNowPlayingId() {
        return nowPlayingId;
    }

    public String getNowPlayingTitle() {
        return nowPlayingTitle;
    }

    public String getName() {
        return name;
    }

    public int getSongs() {
        return songs;
    }

    @Override
    public int compareTo(ChanSuggestion another) {

        if (this.getViewers() == another.getViewers()){
            if (this.getSongs() < another.getSongs()){return 1;}
            if (this.getSongs() > another.getSongs()){return -1;}

            return 0;
        } else {
            if (this.getViewers() < another.getViewers()){return 1;}
            if (this.getViewers() > another.getViewers()){return -1;}
        }

        return 0;
    }
}
