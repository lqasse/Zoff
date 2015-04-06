package no.lqasse.zoff.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import no.lqasse.zoff.Helpers.ImageCache;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Zoff;

/**
 * Created by lassedrevland on 04.04.15.
 */
public class PlayerListAdapter extends ZoffListAdapter {
    public PlayerListAdapter(Context context, ArrayList<Video> videoList, Zoff zoff) {
        super(context, videoList, zoff);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        ViewHolder viewHolder = new ViewHolder();
        Video currentVideo = videoList.get(position);

        View rowView;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate(R.layout.now_playing_row, parent, false);


        viewHolder.imageURL = videoList.get(position).getThumbMed();


        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        ImageView deleteButton = (ImageView) rowView.findViewById(R.id.deleteButton);

        setOnDelete(deleteButton,position);
        ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);

        viewHolder.imageView = imageView;
        viewHolder.position = position;
        viewHolder.progressBar = progressBar;
        viewHolder.video = currentVideo;


        if (ImageCache.has(currentVideo.getId())) {
            imageView.setImageBitmap(ImageCache.get(currentVideo.getId()));
            progressBar.setVisibility(View.GONE);
        } else {
            new downloadImage().execute(viewHolder);
        }




        TextView title = (TextView) rowView.findViewById(R.id.titleView);
        TextView votes = (TextView) rowView.findViewById(R.id.votesView);
        TextView views = (TextView) rowView.findViewById(R.id.viewsLabel);
        TextView skips = (TextView) rowView.findViewById(R.id.skipsLabel);


        title.setText(videoList.get(position).getTitle());

        if (votes != null) {

            votes.setText(videoList.get(position).getVotes());

        } else {
            views.setText(zoff.getViewers());
            skips.setText(zoff.getSkips());
        }


        return rowView;
    }
}
