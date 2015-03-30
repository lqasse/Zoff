package no.lqasse.zoff;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import no.lqasse.zoff.Player.PlayerActivity;
import no.lqasse.zoff.Remote.RemoteActivity;
import no.lqasse.zoff.Server.JSONTranslator;
import no.lqasse.zoff.Server.Server;


public class MainActivity extends ActionBarActivity  {

    private AutoCompleteTextView chanTextView;
    private Runnable retryConnectRunnable;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Server.getChanSuggestions(this);

        setContentView(R.layout.activity_main);
        chanTextView = (AutoCompleteTextView) findViewById(R.id.acEditText);

        handler = new Handler();
        retryConnectRunnable = new Runnable() {
            @Override
            public void run() {

                Server.getChanSuggestions(MainActivity.this);

            }
        };



        //Handles link clicks
        Intent i = getIntent();
        if (i.getAction() == Intent.ACTION_VIEW){
            String url = i.getData().toString();
            url = url.replace("http://www.zoff.no/","");
            url = url.replace("/","");

            char urlChars[] = url.toCharArray();
            Boolean containsIllegalChar = false;

            for (char c: urlChars){
                if (!Character.isLetterOrDigit(c)){
                    containsIllegalChar = true;

                }

            }
            if (!containsIllegalChar){

                chanTextView.setText(url);
                initialize();
            }



        }


        final Toast t = Toast.makeText(this,"Name must be letters or digits ONLY", Toast.LENGTH_SHORT);



        chanTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_GO) {

                    if (isValidRoom()) {

                        initialize();
                    } else {
                        t.show();

                    }

                }
                return true;
            }


        });

        chanTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                initialize();
            }
        });







    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(retryConnectRunnable);
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



        i.putExtra("ROOM_NAME", chanTextView.getText().toString());

        startActivity(i);
        handler.removeCallbacks(retryConnectRunnable);







    }



    private boolean isValidRoom(){

        String room = chanTextView.getText().toString();
        String r = room.replace(" ","");

        chanTextView.setText(r);
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



    public void receivedChanSuggestions(String data){


        if (data.equals("404")){
            //There was an error connecting
            chanTextView.setEnabled(false);
            chanTextView.setText("Error connecting to server...");
            handler.postDelayed(retryConnectRunnable, 1000);
        } else {
            chanTextView.setEnabled(true);
            chanTextView.setText("");

            ArrayList<String> activeRooms = new ArrayList<>();
            activeRooms.addAll(JSONTranslator.toRoomSuggestions(data));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, activeRooms);
            chanTextView.setAdapter(adapter);
        }




    }




}
