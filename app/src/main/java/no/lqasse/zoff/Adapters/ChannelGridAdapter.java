package no.lqasse.zoff.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
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
            TextView newChannel;
        }

        View gridTile;
        Viewholder viewholder;
        if (convertView == null){

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            gridTile = inflater.inflate(R.layout.channel_gridtile, parent, false);

            viewholder = new Viewholder();
            viewholder.headerImage =     (ImageView) gridTile.findViewById(R.id.channelTileHeader);
            viewholder.title =            (TextView) gridTile.findViewById(R.id.channelTileTitle);
            viewholder.viewers =          (TextView) gridTile.findViewById(R.id.channelTileViewers);
            viewholder.songs =            (TextView) gridTile.findViewById(R.id.channelTileSongs);
            viewholder.newChannel = (TextView) gridTile.findViewById(R.id.channelTileNewChannel);


            gridTile.setTag(viewholder);

        } else {
            gridTile = convertView;
            viewholder = (Viewholder) convertView.getTag();
            viewholder.headerImage.setImageDrawable(null);
        }





        viewholder.title.setText(suggestions.get(position).getNameRaisedFirstLetter());
        viewholder.viewers.setText(suggestions.get(position).getViewersText());
        viewholder.songs.setText(suggestions.get(position).getSongsText());


        String videoId = suggestions.get(position).getNowPlayingId();

        viewholder.headerImage.setTag(videoId);

        String imageUrl = Video.getThumbMed(videoId);
        String imageUrlAlt = imageUrl;


        if (suggestions.get(position).isNewChannel()) {
            viewholder.viewers.setText("");
            viewholder.songs.setText("");
            viewholder.newChannel.setText("Create new channel");
        } else {
            viewholder.newChannel.setText("");
        }


        if (ImageCache.has(videoId, ImageCache.ImageSize.REG)){
            viewholder.headerImage.setImageBitmap(ImageCache.get(videoId, ImageCache.ImageSize.REG));
        }else {
            final ImageView targetView = viewholder.headerImage;
            targetView.setTag(videoId);

            BitmapDownloader.downloadTo(videoId, targetView,false, ImageCache.ImageSize.REG, new BitmapDownloader.SetImageCallback() {
                @Override
                public void onImageDownloaded(Bitmap image, String videoId) {
                    if ((targetView.getTag()).equals(videoId)){
                        Animation a = new AlphaAnimation(0.00f, 1.00f);
                        a.setInterpolator(new DecelerateInterpolator());
                        a.setDuration(700);
                        targetView.startAnimation(a);
                        a.start();
                        targetView.setImageBitmap(image);
                    }
                }
            });


        }




        return gridTile;
    }
}
