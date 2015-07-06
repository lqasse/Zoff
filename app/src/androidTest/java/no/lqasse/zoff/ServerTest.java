package no.lqasse.zoff;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;

import no.lqasse.zoff.Server.Server;

/**
 * Created by lassedrevland on 05.07.15.
 */
public class ServerTest extends TestCase{
    Server server;

     class ServerListener implements Server.Listener{

         public String jsonResponseHeader = null;
         @Override
         public void onCorrectPassword(String data) {

         }

         @Override
         public void onViewersChanged(int viewers) {

         }

         @Override
         public void onVideoAdded(JSONArray video) {

         }

         @Override
         public void onVideoChanged(JSONArray newSong) {

         }

         @Override
         public void onVideoDeleted(JSONArray deletedVideo) {

         }

         @Override
         public void onListRefreshed(JSONArray list) {
             try {
                 jsonResponseHeader = list.getString(0);
             }catch (JSONException e) {
                 e.printStackTrace();
             }


             synchronized (this){
                 notifyAll();

             }


         }

         @Override
         public void onConfigurationChanged(JSONArray configuration) {

         }

         @Override
         public void onVideoGotVote(JSONArray video) {

         }

         @Override
         public void onToast(String toast) {

         }
     }



    public void testServerConnects() throws Exception{

        ServerListener serverListener = new ServerListener();
        server = new Server("lqasse",serverListener);


        synchronized (serverListener){
            serverListener.wait(60000);
        }





        assertEquals("list",serverListener.jsonResponseHeader);








    }




}
