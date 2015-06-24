package no.lqasse.zoff.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.ImageTools.BitmapDownloader;
import no.lqasse.zoff.Models.Channel;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.R;

/**
 * Created by lassedrevland on 18.04.15.
 */
public class ChannelGridAdapter extends ArrayAdapter<Channel> {


    private Context context;
    private  ArrayList<Channel> suggestions;
    public ChannelGridAdapter(Context context, ArrayList<Channel> suggestions){
        super(context, R.layout.channel_gridtile,suggestions);
        this.suggestions = suggestions;
        this.context = context;


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        class Viewholder{
            ImageView headerImage;
            TextView title;
            TextView viewers;
            TextView songs;
        }

        View gridTile;
        Viewholder viewholder;
        if (convertView == null){

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridTile = inflater.inflate(R.layout.channel_gridtile, parent, false);

            viewholder = new Viewholder();
            viewholder.headerImage =     (ImageView) gridTile.findViewById(R.id.headerImage);
            viewholder.title =            (TextView) gridTile.findViewById(R.id.title);
            viewholder.viewers =          (TextView) gridTile.findViewById(R.id.viewersLabel);
            viewholder.songs =            (TextView) gridTile.findViewById(R.id.songsLabel);

            gridTile.setTag(viewholder);

        } else {
            gridTile = convertView;
            viewholder = (Viewholder) convertView.getTag();
            viewholder.headerImage.setImageDrawable(null);
        }





        viewholder.title.setText(suggestions.get(position).getName());
        viewholder.viewers.setText(Integer.toString(suggestions.get(position).getViewers()));
        viewholder.songs.setText(Integer.toString(suggestions.get(position).getSongs()));


        String videoId = suggestions.get(position).getNowPlayingId();

        viewholder.headerImage.setTag(videoId);

        String imageUrl = Video.getThumbMed(videoId);
        String imageUrlAlt = imageUrl;

        if (ImageCache.has(videoId, ImageCache.ImageSize.HUGE)){
            viewholder.headerImage.setImageBitmap(ImageCache.get(videoId, ImageCache.ImageSize.HUGE));
        }else {
            BitmapDownloader.downloadAndSet(imageUrl, imageUrlAlt, videoId, viewholder.headerImage, ImageCache.ImageSize.HUGE, false);

        }




        return gridTile;
    }
}
