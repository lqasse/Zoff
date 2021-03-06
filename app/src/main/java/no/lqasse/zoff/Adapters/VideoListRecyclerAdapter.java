package no.lqasse.zoff.Adapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import no.lqasse.zoff.ImageTools.BitmapDownloader;
import no.lqasse.zoff.ImageTools.BitmapCache;
import no.lqasse.zoff.Models.Playlist;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.ZoffController;
import no.lqasse.zoff.R;

/**
 * Created by lassedrevland on 21.06.15.
 */


public class VideoListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_HEADER = 0;
    private final static int TYPE_ROW = 1;


    private Playlist playlist;
    private ZoffController controller;

    public VideoListRecyclerAdapter(ZoffController controller) {
        this.playlist = controller.getPlaylist();
        this.controller = controller;
    }


    public class ViewHolderItem extends RecyclerView.ViewHolder{
        protected  ImageView vThumbnail    ;
        protected  ImageView vDeleteButton ;
        protected  TextView  vTitle        ;
        protected  TextView  vVotes        ;


        public ViewHolderItem(View v) {
            super(v);
            vThumbnail      = (ImageView)    v.findViewById(R.id.playlistItemImage);
            vDeleteButton   = (ImageView)    v.findViewById(R.id.playlistItemDeleteButton);
            vTitle          = (TextView)     v.findViewById(R.id.playlistItemTitle);
            vVotes          = (TextView)     v.findViewById(R.id.playlistItemVotes);

        }
    }

    public class ViewHolderHeader extends RecyclerView.ViewHolder{
        protected  ImageView vThumbnail    ;
        protected  TextView  vTitle        ;
        protected  TextView  vSkips;
        protected  TextView  vViews;
        protected FrameLayout vProgress;


        public ViewHolderHeader(View v) {
            super(v);
            vThumbnail      = (ImageView)    v.findViewById(R.id.playlistHeaderImage);
            vTitle          = (TextView)     v.findViewById(R.id.playlistHeaderTitle);
            vSkips          = (TextView)     v.findViewById(R.id.playlistHeaderSkips);
            vViews          = (TextView)     v.findViewById(R.id.playlistHeaderViews);
            vProgress  = ((FrameLayout) v.findViewById(R.id.playlistHeaderPlayProgress));

        }
    }




    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView;

        if (viewType == TYPE_HEADER){
            itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.playlist_header, parent, false);

            return new ViewHolderHeader(itemView);

        } else {
            itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.playlist_item, parent, false);

            return new ViewHolderItem(itemView);

        }




    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Video currentVideo;
        BitmapCache.ImageSize imageSize = BitmapCache.ImageSize.REG;
        ImageView currentImageView = null;
        currentVideo = playlist.get(position);

        if (holder instanceof ViewHolderHeader){

            ((ViewHolderHeader) holder).vTitle.setText(currentVideo.getTitle());
            ((ViewHolderHeader) holder).vViews.setText(controller.getZoff().getCurrentViewers());
            ((ViewHolderHeader) holder).vSkips.setText(controller.getZoff().getCurrentSkips());
            ((ViewHolderHeader) holder).vThumbnail.setTag(currentVideo.getId());
            ((ViewHolderHeader) holder).vProgress.setPivotX(0);
            ((ViewHolderHeader) holder).vProgress.setScaleX(controller.getZoff().getPlayProgress());

            currentImageView = ((ViewHolderHeader) holder).vThumbnail;

            imageSize = BitmapCache.ImageSize.HUGE;


        } else if (holder instanceof ViewHolderItem){

            ViewHolderItem holderItem = (ViewHolderItem) holder;

            holderItem.vDeleteButton.setTag(position);
            holderItem.vTitle.setText(currentVideo.getTitle());
            holderItem.vVotes.setText(currentVideo.getVotesString());
            holderItem.vThumbnail.setTag(currentVideo.getId());

            currentImageView = holderItem.vThumbnail;
            imageSize = BitmapCache.ImageSize.REG;

            if (controller.getZoff().isUnlocked()){
                holderItem.vDeleteButton.setVisibility(View.VISIBLE);
            }

            holderItem.itemView.setTag(position);
            holderItem.vDeleteButton.setTag(position);
            holderItem.vDeleteButton.setOnClickListener(onClickDelete);
            holderItem.itemView.setOnLongClickListener(onLongClickVote);
        }

        if (BitmapCache.has(currentVideo.getId())) {
            Bitmap videoImage = BitmapCache.get(currentVideo.getId(), imageSize);
            currentImageView.setImageBitmap(videoImage);
        } else {
            currentImageView.setImageBitmap(null);
            final ImageView targetView = currentImageView;
            targetView.setTag(currentVideo.getId());
            BitmapDownloader.downloadTo(currentVideo.getId(), targetView, true, imageSize, new BitmapDownloader.SetImageCallback() {
                @Override
                public void onImageDownloaded(Bitmap image, String videoId) {
                    if (targetView.getTag().equals(videoId)) {
                        Animation a = new AlphaAnimation(0.00f, 1.00f);
                        a.setInterpolator(new DecelerateInterpolator());
                        a.setDuration(700);
                        targetView.startAnimation(a);
                        a.start();
                        targetView.setImageBitmap(image);
                    }
                }
            });
        };

    }

    @Override
    public int getItemCount() {
        return playlist.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)){
            return TYPE_HEADER;
        } else
            return TYPE_ROW;
    }


    private boolean isPositionHeader(int position){
        return position == 0;
    }

    View.OnClickListener onClickDelete = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int index = (int) v.getTag();
            controller.delete(playlist.get(index));

        }
    };



    View.OnLongClickListener onLongClickVote = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            int index = (int) v.getTag();
            controller.vote(playlist.get(index));
            return true;
        }
    };
}
