package no.lqasse.zoff;

import org.json.JSONArray;
import org.json.JSONException;

import no.lqasse.zoff.Server.Server;

/**
 * Created by lassedrevland on 08.07.15.
 */
public class MockServer extends Server {


    Listener listener;
    public MockServer(String channel, Listener listener) {
        super(channel, listener);
        this.listener = listener;
    }


    public void fireRefresh(String testData){

        listener.onListRefreshed(getJSONArray(testData));
    }

    private JSONArray getJSONArray(String data){
        try {
            return new JSONArray(data);
        }catch (JSONException e){
            e.printStackTrace();
        }
    return new JSONArray();

    }







}
