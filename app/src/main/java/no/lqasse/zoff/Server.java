package no.lqasse.zoff;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import no.lqasse.zoff.Datatypes.Zoff;

/**
 * Created by lassedrevland on 23.03.15.
 */
public class Server {


    private enum GET_TYPE{SKIP,VOTE, REFRESH,SHUFFLE,ADD}
    private enum POST_TYPE{SETTINGS}

    private static class getHolder{
        GET_TYPE type;
        String response;
        String url;
        Zoff zoff;

    }

    private static class postHolder{
        POST_TYPE type;
        String response;
        String url;
        List<NameValuePair> nameValuePairs = new ArrayList<>(8);
        Zoff zoff;
        Activity activity;

    }

    public static void add(String videoID,String title){


        getHolder holder = new getHolder();
        holder.url = Zoff.getUrl() + "v=" + videoID + "&n=" + title + "&pass=" + Zoff.getRoomPass();
        holder.type = GET_TYPE.ADD;

        Get get = new Get();
        get.execute(holder);


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

    public static void postSettings(Activity activity, String password, Boolean... settings){


        String vote = settings[0].toString();
        String addsongs = settings[1].toString();
        String longsongs = settings[2].toString();
        String frontpage = settings[3].toString();
        String allvideos = settings[4].toString();
        String removeplay = settings[5].toString();

        postHolder holder = new postHolder();
        holder.type = POST_TYPE.SETTINGS;
        holder.url = Zoff.getPOST_URL();
        holder.activity = activity;

        holder.nameValuePairs.clear();
        holder.nameValuePairs.add(new BasicNameValuePair("conf", "start"));
        holder.nameValuePairs.add(new BasicNameValuePair("vote", vote));
        holder.nameValuePairs.add(new BasicNameValuePair("addsongs", addsongs));
        holder.nameValuePairs.add(new BasicNameValuePair("longsongs", longsongs));
        holder.nameValuePairs.add(new BasicNameValuePair("frontpage", frontpage));
        holder.nameValuePairs.add(new BasicNameValuePair("allvideos", allvideos));
        holder.nameValuePairs.add(new BasicNameValuePair("removeplay", removeplay));
        holder.nameValuePairs.add(new BasicNameValuePair("pass", password));

        Post post = new Post();
        post.execute(holder);



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

    private static class Post extends AsyncTask<postHolder, Void, postHolder> {


        @Override
        protected postHolder doInBackground(postHolder... params) {
            postHolder holder = params[0];

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(holder.url);

            try {



                httppost.setEntity((new UrlEncodedFormEntity(holder.nameValuePairs, "UTF-8")));
                httppost.setHeader("Content-type", "application/x-www-form-urlencoded");

                //Execute!
                HttpResponse response = httpClient.execute(httppost);
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    holder.response =  EntityUtils.toString(resEntity);

                }
                return holder;


            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException();
            }


        }

        @Override
        protected void onPostExecute(postHolder holder) {
            super.onPostExecute(holder);

            switch (holder.type){
                case SETTINGS:
                    //Log.d("Response", holder.response);

                    ((SettingsActivity) holder.activity).settingsPostResponse(holder.response);


                    break;

            }


        }
    }

}
