package no.lqasse.zoff.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import no.lqasse.zoff.Helpers.ImageCache;
import no.lqasse.zoff.Helpers.ImageDownload;
import no.lqasse.zoff.Models.ChanSuggestion;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.R;

/**
 * Created by lassedrevland on 18.04.15.
 */
public class SuggestionsGridAdapter extends ArrayAdapter<ChanSuggestion> {


    private Context context;
    private  ArrayList<ChanSuggestion> suggestions;
    public SuggestionsGridAdapter(Context context,ArrayList<ChanSuggestion> suggestions){
        super(context, R.layout.channel_gridtile,suggestions);
        this.suggestions = suggestions;
        this.context = context;


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View gridView = inflater.inflate(R.layout.channel_gridtile, parent, false);

        ImageView headerImage = (ImageView)gridView.findViewById(R.id.headerImage);
        TextView title = (TextView) gridView.findViewById(R.id.title);
        TextView viewers = (TextView) gridView.findViewById(R.id.viewersLabel);
        TextView songs = (TextView) gridView.findViewById(R.id.songsLabel);

        title.setText(suggestions.get(position).getName());
        viewers.setText(Integer.toString(suggestions.get(position).getViewers()));
        songs.setText(Integer.toString(suggestions.get(position).getSongs()));


        String videoId = suggestions.get(position).getNowPlayingId();

        String imageUrl = Video.getThumbMed(videoId);
        String imageUrlAlt = imageUrl;

        if (ImageCache.has(videoId)){
            headerImage.setImageBitmap(ImageCache.get(videoId));
        }else {
            ImageDownload.downloadAndSet(imageUrl,imageUrlAlt,videoId,headerImage, ImageCache.ImageType.REG);

        }







        return gridView;
    }
}
