package no.lqasse.zoff;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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


import no.lqasse.zoff.Adapters.ChannelGridAdapter;
import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.Models.Channel;
import no.lqasse.zoff.Models.ZoffController;
import no.lqasse.zoff.Remote.RemoteActivity;
import no.lqasse.zoff.Server.Server;


public class ChannelChooserActivity extends ActionBarActivity  {

    private AutoCompleteTextView channelTextView;
    private static final String LOG_IDENTIFIER = "MainActivity";
    private ArrayList<Channel> displayedSuggestions = new ArrayList<>();
    private ArrayList<Channel> allSuggestions = new ArrayList<>();
    private ChannelGridAdapter suggestionArrayAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageCache.empty();


        setContentView(R.layout.activity_channelchooser);
        channelTextView = (AutoCompleteTextView) findViewById(R.id.acEditText);
        GridView gridView = (GridView) findViewById(R.id.chanGrid);
        suggestionArrayAdapter = new ChannelGridAdapter(this,displayedSuggestions);
        gridView.setAdapter(suggestionArrayAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                initializeRemote(displayedSuggestions.get(position).getName());

            }
        });



        Intent i = getIntent();
        if (i.getAction().equals(Intent.ACTION_VIEW)){

            handleLinkClickIntent(i.getData().toString());
        }


        final Toast t = Toast.makeText(this,"Name must be letters or digits ONLY", Toast.LENGTH_SHORT);



        channelTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filterSuggestions(s.toString());
            }
        });


    }


    private void initializeNew(String chan){
        Intent i = new Intent(this, RemoteActivity.class);
        i.putExtra(ZoffController.BUNDLEKEY_CHANNEL, chan);
        i.putExtra(ZoffController.BUNDLEKEY_IS_NEW_CHANNEL, true);

        startActivity(i);
    }

    private void initializeRemote(String chan){

        Intent i = new Intent(this, RemoteActivity.class);
        i.putExtra(ZoffController.BUNDLEKEY_CHANNEL, chan);
        startActivity(i);

    }



    private boolean isValidRoom(){

        String room = channelTextView.getText().toString();
        String r = room.replace(" ","");

        channelTextView.setText(r);
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






    private void handleLinkClickIntent(String url){

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

            channelTextView.setText(url);
            initializeRemote(url);
        }

    }

    private void log(String log){
        Log.i(LOG_IDENTIFIER,log);
    }

    @Override
    protected void onResume() {

        Server.getChannelSuggestions(this, new Server.SuggestionsCallback() {
            @Override
            public void onResponse(final ArrayList<Channel> suggestions) {
                setChannelSuggestions(suggestions);
            }
        });


        log("Getting suggestions");
        super.onResume();
    }

    public void setChannelSuggestions(final ArrayList<Channel> suggestions){
        allSuggestions.clear();
        allSuggestions.addAll(suggestions);
        Collections.sort(allSuggestions);

        displayedSuggestions.clear();

        for (int i = 0;i<20;i++){
            displayedSuggestions.add(allSuggestions.get(i));
        }


        suggestionArrayAdapter.notifyDataSetChanged();



    }

    private void filterSuggestions(String predicate){
        predicate = predicate.toLowerCase();
        ArrayList<Channel> filteredResults = new ArrayList<>();


        for (Channel c:allSuggestions){
            if (c.getName().contains(predicate)){
                filteredResults.add(c);
            }
        }

        if (predicate.equals("")){
            filteredResults.addAll(allSuggestions);
        }

        int lengthLimit = 20;

        if (filteredResults.size() < 20){
            lengthLimit = filteredResults.size();
        }

        displayedSuggestions.clear();
        for (int i = 0;i<lengthLimit;i++){
            displayedSuggestions.add(filteredResults.get(i));
        }

        ArrayList<String> titles = new ArrayList<>();
        for (Channel c:displayedSuggestions){
            titles.add(c.getName());
        }

        if (!titles.contains(predicate)){
            displayedSuggestions.add(getNewChannelPlaceholder(predicate));

        }

        suggestionArrayAdapter.notifyDataSetChanged();


    }

    private Channel getNewChannelPlaceholder(String title){

        Channel channel = Channel.createNewChannelPlaceholder(title);

        return channel;

    }
}
