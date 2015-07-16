package no.lqasse.zoff;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;


import no.lqasse.zoff.Adapters.ChannelGridAdapter;
import no.lqasse.zoff.ImageTools.BitmapCache;
import no.lqasse.zoff.Models.Channel;
import no.lqasse.zoff.Remote.RemoteActivity;
import no.lqasse.zoff.Server.Server;


public class ChannelChooserActivity extends ActionBarActivity  {

    private AutoCompleteTextView channelTextView;
    private static final String LOG_IDENTIFIER = "MainActivity";
    private ArrayList<Channel> displayedSuggestions = new ArrayList<>();
    private ArrayList<Channel> allSuggestions = new ArrayList<>();
    private ChannelGridAdapter suggestionArrayAdapter;
    private LoadingAnimation loadingAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BitmapCache.empty();
        setContentView(R.layout.activity_channelchooser);
        channelTextView = (AutoCompleteTextView) findViewById(R.id.acEditText);
        GridView gridView = (GridView) findViewById(R.id.chanGrid);
        loadingAnimation = (LoadingAnimation) findViewById(R.id.channel_chooser_loading_icon);
        suggestionArrayAdapter = new ChannelGridAdapter(this,displayedSuggestions);

        gridView.setAdapter(suggestionArrayAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                initializeRemote(displayedSuggestions.get(position).getName());
            }
        });

        channelTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!allSuggestions.isEmpty()){
                    filterSuggestions(s.toString());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        Server.getChannelSuggestions(this, new Server.SuggestionsCallback() {
            @Override
            public void onResponse(final ArrayList<Channel> suggestions) {
                setChannelSuggestions(suggestions);
                loadingAnimation.setVisibility(View.INVISIBLE);
            }
        });

        super.onResume();
    }

    private void initializeRemote(String chan){

        Intent i = new Intent(this, RemoteActivity.class);
        i.putExtra(ZoffController.BUNDLEKEY_CHANNEL, chan);
        startActivity(i);

    }

    private boolean isValidChannel(String channel){
        channel = channel.replace(" ","");

        for (char c:channel.toCharArray()){
            if (!Character.isLetterOrDigit(c)){
                Toast.makeText(this,Character.toString(c) + " is not a valid character",Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
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

    private void log(String log){
        Log.i(LOG_IDENTIFIER,log);
    }
}
