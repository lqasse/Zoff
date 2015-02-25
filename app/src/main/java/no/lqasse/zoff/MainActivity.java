package no.lqasse.zoff;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import no.lqasse.zoff.Player.PlayerActivity;
import no.lqasse.zoff.Remote.RemoteActivity;


public class MainActivity extends ActionBarActivity  {


    private String ZOFF_ACTIVECHANNELS_URL = "http://zoff.no/Proggis/php/activechannels.php";
    private AutoCompleteTextView acEditText;
    private Runnable checkInetAccess;
    private Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        acEditText = (AutoCompleteTextView) findViewById(R.id.acEditText);
        h = new Handler();
        checkInetAccess = new Runnable() {
            @Override
            public void run() {

                Boolean hasInetAccess = isOnline();
                if (hasInetAccess && !acEditText.isEnabled()){
                    String[] input = {ZOFF_ACTIVECHANNELS_URL};
                    getSuggestions suggestions = new getSuggestions();
                    suggestions.execute(input);
                    acEditText.setEnabled(true);
                    acEditText.setText("");
                } else if(!hasInetAccess){
                    acEditText.setEnabled(false);
                    acEditText.setText("No Internet access");
                    h.removeCallbacks(this);
                    h.postDelayed(checkInetAccess, 1000);
                }

            }
        };

        h.post(checkInetAccess);


        final Toast t = Toast.makeText(this,"Name must be letters or digits ONLY", Toast.LENGTH_SHORT);



        acEditText.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {

                    if (isValidRoom()){

                        initialize();
                    } else {
                        t.show();

                    }

                }
                return handled;
            }


        });

        acEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                initialize();
            }
        });







    }

    @Override
    protected void onStop() {
        h.removeCallbacks(checkInetAccess);
        super.onStop();
    }

    private void initialize(){

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
        Intent i;
        if (checkBox.isChecked()){
            //Starter PLayerActivity
                i = new Intent(this, PlayerActivity.class);
        } else {
            //Starter RemoteActivity

            i = new Intent(this, RemoteActivity.class);
        }



        i.putExtra("ROOM_NAME", acEditText.getText().toString());

        if (isOnline()){
            startActivity(i);
            h.removeCallbacks(checkInetAccess);
        } else {
            h.post(checkInetAccess);

        }






    }


    private void initializeDebug(){

        Intent i = new Intent(this, RemoteActivity.class);
        i.putExtra("ROOM_NAME", "lqasse");

        startActivity(i);



    }



    private boolean isValidRoom(){


        String room = acEditText.getText().toString();
        String r = room.replace(" ","");

        acEditText.setText(r);
        if (r.equals("")) {
            Toast.makeText(this,"Enter name",Toast.LENGTH_SHORT).show();
            return false;
        }

        for (char c:r.toCharArray()){
            if (!Character.isLetterOrDigit(c)){
                Toast.makeText(this,Character.toString(c) + " is not a valid character",Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }



    private class getSuggestions extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();

            try {
                InputStream inputStream = null;
                BufferedReader r;

                String url = params[0];
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
                inputStream = httpResponse.getEntity().getContent();
                if(inputStream != null){
                    r = new BufferedReader(new InputStreamReader(inputStream));
                    sb = new StringBuilder();
                    String line;
                    while((line = r.readLine()) !=null){
                        sb.append(line);

                    }



                }
            } catch (Exception e){
                throw new IllegalStateException();

            }



            return sb.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ArrayAdapter<String> adapter;
            String[] activeRooms = new String[0];
            try{
                JSONArray array = new JSONArray(s);
                activeRooms = new String[array.length()];
                for (int i = 0;i<array.length();i++){
                    activeRooms[i] = array.get(i).toString();
                    adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, activeRooms);
                    acEditText.setAdapter(adapter);
                }


            } catch (Exception e){
                e.printStackTrace();
            }


        }
    }

    public boolean isOnline() {

        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }




}
