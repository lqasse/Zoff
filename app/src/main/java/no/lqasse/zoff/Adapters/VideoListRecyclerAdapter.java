package no.lqasse.zoff.Adapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import no.lqasse.zoff.ImageTools.BitmapDownloader;
import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Models.ZoffController;
import no.lqasse.zoff.R;

/**
 * Created by lassedrevland on 21.06.15.
 */


public class VideoListRecyclerAdapter extends RecyclerView.Adapter<VideoListRecyclerAdapter.ViewHolder> {


    private ArrayList<Video> videos;
    private ZoffController controller;

    public VideoListRecyclerAdapter(ZoffController controller) {
        this.videos = controller.getZoff().getVideos();
        this.controller = controller;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        protected  ImageView vThumbnail    ;
        protected  ImageView vDeleteButton ;
        protected  TextView  vTitle        ;
        protected  TextView  vVotes        ;


        public ViewHolder(View v) {
            super(v);
            vThumbnail      = (ImageView)    v.findViewById(R.id.imageView);
            vDeleteButton   = (ImageView)    v.findViewById(R.id.deleteButton);
            vTitle          = (TextView)     v.findViewById(R.id.videoTitleView);
            vVotes          = (TextView)     v.findViewById(R.id.votesView);

        }
    }




    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.now_playing_row, parent, false);

        return new ViewHolder(itemView);

    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Video currentVideo = videos.get(position);

        holder.vDeleteButton.setTag(position);
        holder.vTitle.setText(currentVideo.getTitle());
        holder.vVotes.setText(currentVideo.getVotesString());
        holder.vThumbnail.setTag(currentVideo.getId());





        if (ImageCache.has(currentVideo.getId())) {
            Bitmap videoImage = ImageCache.get(currentVideo.getId());
            holder.vThumbnail.setImageBitmap(videoImage);
        } else {

            BitmapDownloader.downloadAndSet(currentVideo.getThumbMed(), currentVideo.getThumbSmall(), currentVideo.getId(), holder.vThumbnail, ImageCache.ImageSize.REG, true);
        };




    }

    @Override
    public int getItemCount() {
        return videos.size();
    }
}
