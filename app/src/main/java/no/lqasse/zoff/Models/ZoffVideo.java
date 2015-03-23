package no.lqasse.zoff.Models;

import android.graphics.Bitmap;

import no.lqasse.zoff.Datatypes.Zoff;

/**
 * Created by lassedrevland on 23.03.15.
 */
public class ZoffVideo implements Comparable<ZoffVideo>{
        private String title;
        private String id;
        private String votes;
        private String added;


        public ZoffVideo(String title, String id, String votes, String added){
            this.title = title;
            this.id = id;
            this.votes = votes;
            this.added = added;


        }

        public ZoffVideo(String title, String id, String votes, String added, Zoff zoff){
        //TODO REMOVE
        this.title = title;
        this.id = id;
        this.votes = votes;
        this.added = added;


    }

        @Override
        public int compareTo(ZoffVideo another) {

            if (this.getVotesInt() != another.getVotesInt()){
                return another.getVotesInt() - this.getVotesInt(); //Descending on votes 1, 2 ,3 etc

            } else {
                return (int) (this.getAddedLong()- another.getAddedLong()); //Ascending on time, ie added earliger gives higher position
            }

        }


        public String getTitle() {
            if (title.equals("null")){
                return "There are no videos here yet!";
            }
            return title;
        }

        public String getId() {
            return id;
        }

        public String getAdded(){
            return this.added;
        }

        public String getVotes() {
            return votes;
        }

        public String getThumbMed() {

            return "https://i.ytimg.com/vi/" + id +"/mqdefault.jpg";
        }

        public String getThumbSmall() {
            return "https://i.ytimg.com/vi/" + id +"/default.jpg";
        }

        public String getImageBig() {
            return "https://i.ytimg.com/vi/" + id +"/hqdefault.jpg";
        }

        public String getThumbHuge() {
            return "https://i.ytimg.com/vi/" + id +"/maxresdefault.jpg";
        }

        public int getVotesInt(){
            return Integer.valueOf(votes);
        }

        public long getAddedLong(){
            return Long.valueOf(added);
        }





}
