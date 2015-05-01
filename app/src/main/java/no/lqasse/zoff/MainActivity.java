package no.lqasse.zoff;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;


import no.lqasse.zoff.Adapters.SuggestionsGridAdapter;
import no.lqasse.zoff.Models.ChanSuggestion;
import no.lqasse.zoff.Server.SocketServer;


public class MainActivity extends ActionBarActivity  {

    private AutoCompleteTextView chanTextView;
    private static final String LOG_IDENTIFIER = "MainActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        chanTextView = (AutoCompleteTextView) findViewById(R.id.acEditText);



        //Handles link clicks
        Intent i = getIntent();
        if (i.getAction().equals(Intent.ACTION_VIEW)){
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
                initialize(url);
            }



        }


        final Toast t = Toast.makeText(this,"Name must be letters or digits ONLY", Toast.LENGTH_SHORT);



        chanTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_GO) {

                    if (isValidRoom()) {

                        initializeNew(chanTextView.getText().toString());


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
                initialize(chanTextView.getText().toString());
            }
        });


    }


    @Override
    protected void onPostResume() {

        super.onPostResume();
    }

    private void initializeNew(String chan){
        Intent i = new Intent(this, RemoteActivity.class);
        i.putExtra("ROOM_NAME", chan);
        i.putExtra("NEW",true);

        startActivity(i);
    }

    private void initialize(String chan){

        Intent i = new Intent(this, RemoteActivity.class);
        i.putExtra("ROOM_NAME", chan);
        startActivity(i);

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




    public void setSuggestions(final ArrayList<ChanSuggestion> suggestions){


        GridView gridView = (GridView) findViewById(R.id.chanGrid);
        SuggestionsGridAdapter suggestionArrayAdapter = new SuggestionsGridAdapter(this,suggestions);

        Collections.sort(suggestions);
        gridView.setAdapter(suggestionArrayAdapter);





        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                initialize(suggestions.get(position).getName());

            }
        });

    }

    private void log(String log){
        Log.i(LOG_IDENTIFIER,log);
    }

    @Override
    protected void onResume() {
        SocketServer.getSuggestions(this);
        log("Getting suggestions");
        super.onResume();
    }
}
