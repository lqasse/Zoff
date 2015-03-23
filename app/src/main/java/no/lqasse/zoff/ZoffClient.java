package no.lqasse.zoff;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import no.lqasse.zoff.Datatypes.Zoff;

/**
 * Created by lassedrevland on 23.03.15.
 */
public class ZoffClient {


    private enum GET_TYPE{SKIP,VOTE, REFRESH,SHUFFLE}

    private static class getHolder{
        GET_TYPE type;
        String response;
        String url;
        Zoff zoff;

    }

    public static void refresh(Zoff zoff){
        getHolder holder = new getHolder();
        holder.zoff = zoff;
        holder.type = GET_TYPE.REFRESH;
        holder.url = Zoff.getUrl();

        Get get = new Get();
        get.execute(holder);



    }

    public static void vote(String videoID){

        String voteUrl = Zoff.getUrl() + "vote=pos&id=" + videoID + "&pass="+ Zoff.getRoomPass();

        getHolder holder = new getHolder();
        holder.type = GET_TYPE.VOTE;
        holder.url = voteUrl;

        Get get = new Get();
        get.execute(holder);

    }

    public static void shuffle(){
        getHolder holder = new getHolder();
        holder.type = GET_TYPE.SHUFFLE;
        holder.url = Zoff.getUrl() + "shuffle=true&pass=" + Zoff.getRoomPass();

        Get get = new Get();
        get.execute(holder);

    }

    public static void skip(String videoID){
        getHolder holder = new getHolder();
        holder.type = GET_TYPE.SKIP;
        holder.url = Zoff.getUrl() + "thisUrl=" + videoID + "&act=save";

        Get get = new Get();
        get.execute(holder);
    }



    private static class Get extends AsyncTask<getHolder, Void, getHolder> {
        JSONObject json;
        StringBuilder sb;



        @Override
        protected getHolder doInBackground(getHolder... getHolders) {
            BufferedReader r;
            InputStream inputStream = null;
            String result = "";
            getHolder holder = getHolders[0];
            try {
                String url = holder.url;

                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // make GET request to the given URL
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // convert inputstream to string
                if (inputStream != null) {
                    r = new BufferedReader(new InputStreamReader(inputStream));
                    sb = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        sb.append(line);

                    }

                    holder.response = sb.toString();

                    return holder;


                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;


        }

        @Override
        protected void onPostExecute(getHolder holder) {
            switch (holder.type){

                case REFRESH:
                    holder.zoff.refreshed(true,holder.response);
                    break;
                case VOTE:
                    //DO Nothing
                    break;
                case SHUFFLE:
                    break;
                case SKIP:
                    break;

            }
            super.onPostExecute(holder);
        }
    }

}
