package no.lqasse.zoff.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import no.lqasse.zoff.Models.ChanSuggestion;


/**
 * Created by lassedrevland on 16.04.15.
 */
public class SuggestionsAdapter extends ArrayAdapter<ChanSuggestion>{
    private Context context;
    private ArrayList<ChanSuggestion> suggestions;
    public SuggestionsAdapter(Context context,ArrayList<ChanSuggestion> suggestions) {
        super(context, android.R.layout.simple_list_item_1, suggestions);
        this.context = context;
        this.suggestions = suggestions;



    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

        View item = inflater.inflate(android.R.layout.simple_list_item_1,parent,false);

        TextView text = (TextView) item.findViewById(android.R.id.text1);
        text.setText(suggestions.get(position).getName());

        return item;

    }


}
