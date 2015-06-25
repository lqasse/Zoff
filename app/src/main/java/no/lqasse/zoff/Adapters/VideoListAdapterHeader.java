package no.lqasse.zoff.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.ImageTools.BitmapDownloader;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Models.ZoffController;
import no.lqasse.zoff.Models.ZoffModel;
import no.lqasse.zoff.R;

/**
 * Created by lassedrevland on 07.04.15.
 */
public class VideoListAdapterHeader extends VideoListAdapter {
    public VideoListAdapterHeader(Context context, ZoffController controller) {
        super(context, controller);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ZoffModel zoff = controller.getZoff();
        if (position == 0){

            Video currentVideo = controller.getZoff().getVideos().get(position);
            ViewHolder viewHolder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.playlist_header, parent, false);



            TextView    title       = (TextView) rowView.findViewById(R.id.playlistHeaderTitle);
            TextView    views       = (TextView) rowView.findViewById(R.id.viewsLabel);
            TextView    skips       = (TextView) rowView.findViewById(R.id.skipsLabel);
            TextView    playtime    = (TextView) rowView.findViewById(R.id.playlistHeaderPlaytime);
            ImageView   imageView   = ((ImageView) rowView.findViewById(R.id.playlistHeaderImage));
            FrameLayout playProgress = ((FrameLayout) rowView.findViewById(R.id.playlistHeaderPlayProgress));




            title.setText(currentVideo.getTitle());
            views.setText(zoff.getCurrentViewers());
            skips.setText(zoff.getCurrentSkips());
            //playtime.setText(zoff.getCurrentPlaytime());

            playProgress.setPivotX(0);
            playProgress.setScaleX(zoff.getPlayProgress());




            viewHolder.imageURL     = videoList.get(position).getThumbMed();
            viewHolder.imageView    = imageView;
            viewHolder.position     = position;
            viewHolder.video        = currentVideo;
            viewHolder.huge         = true;

            viewHolder.imageView.setTag(currentVideo.getId());







            if (ImageCache.has(currentVideo.getId(), ImageCache.ImageSize.HUGE)) {
                imageView.setImageBitmap(ImageCache.get(currentVideo.getId(), ImageCache.ImageSize.HUGE));
            } else {
                BitmapDownloader.downloadAndSet(currentVideo.getThumbHuge(), currentVideo.getThumbMed(), currentVideo.getId(), viewHolder.imageView, ImageCache.ImageSize.HUGE, false);
            }

            return rowView;
        } else {
            return super.getView(position, convertView, parent);
        }

    }
}
