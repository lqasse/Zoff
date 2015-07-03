package no.lqasse.zoff.Models;

/**
 * Created by lassedrevland on 16.04.15.
 */
public class Channel implements Comparable<Channel>{
    public static String isNewChannelFlag = "NEW";
    private int viewers;
    private String nowPlayingId;
    private String nowPlayingTitle;
    private String name;
    private int songs;
    private boolean isNewChannel;

    public static Channel createNewChannelPlaceholder(String name){
        return new Channel(0,isNewChannelFlag,"",name,0);
    }

    public Boolean isNewChannel(){
        return isNewChannel;
    }



    public Channel(int viewers, String nowPlayingId, String nowPlayingTitle, String name, int songs) {
        this.viewers = viewers;
        this.nowPlayingId = nowPlayingId;
        this.nowPlayingTitle = nowPlayingTitle;
        this.name = name;
        this.songs = songs;

        if (nowPlayingId.equals(isNewChannelFlag)){
            isNewChannel = true;
        }

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

    public String getNameRaisedFirstLetter(){
        char first = this.name.charAt(0);
        first = Character.toUpperCase(first);

        String nameRaisedFirstLetter = first + this.name.substring(1);

        return nameRaisedFirstLetter;


    }

    public String getViewersText(){
        return "Viewers: " + viewers;
    }

    public String getSongsText(){
        return "Songs: " + songs;
    }


    public int getSongs() {
        return songs;
    }

    @Override
    public int compareTo(Channel another) {

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
