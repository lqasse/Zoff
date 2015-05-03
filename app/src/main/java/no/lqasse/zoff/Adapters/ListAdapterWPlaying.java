package no.lqasse.zoff.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;


import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.ImageTools.ImageDownload;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Models.Zoff;

/**
 * Created by lassedrevland on 07.04.15.
 */
public class ListAdapterWPlaying extends ListAdapter {
    public ListAdapterWPlaying(Context context, ArrayList<Video> videoList, Zoff zoff) {
        super(context, videoList, zoff);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0){

            Video currentVideo = videoList.get(position);
            ViewHolder viewHolder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.now_playing_row_top, parent, false);



            TextView    title       = (TextView) rowView.findViewById(R.id.videoTitleView);
            TextView    views       = (TextView) rowView.findViewById(R.id.viewsLabel);
            TextView    skips       = (TextView) rowView.findViewById(R.id.skipsLabel);
            TextView    playtime    = (TextView) rowView.findViewById(R.id.playtimeCurrent);
            ImageView   imageView   = ((ImageView) rowView.findViewById(R.id.imageView));
            FrameLayout playProgress = ((FrameLayout) rowView.findViewById(R.id.playProgress));




            title.setText(currentVideo.getTitle());
            views.setText(zoff.getViewersCount());
            skips.setText(zoff.getSkips());
            //playtime.setText(zoff.getCurrentPlaytime());

            playProgress.setPivotX(0);
            playProgress.setScaleX(zoff.getPlayProgress());




            viewHolder.imageURL     = videoList.get(position).getThumbMed();
            viewHolder.imageView    = imageView;
            viewHolder.position     = position;
            viewHolder.video        = currentVideo;
            viewHolder.huge         = true;







            if (ImageCache.has(currentVideo.getId(), ImageCache.ImageType.HUGE)) {
                imageView.setImageBitmap(ImageCache.get(currentVideo.getId(), ImageCache.ImageType.HUGE));
            } else {
                ImageDownload.downloadAndSet(currentVideo.getThumbHuge(), currentVideo.getThumbMed(), currentVideo.getId(), viewHolder.imageView, ImageCache.ImageType.HUGE);

            }

            return rowView;
        } else {
            return super.getView(position, convertView, parent);
        }

    }
}
