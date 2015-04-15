package no.lqasse.zoff.Adapters;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;

import no.lqasse.zoff.Helpers.ImageBlur;
import no.lqasse.zoff.Helpers.ImageCache;
import no.lqasse.zoff.Helpers.ImageDownload;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Remote.RemoteActivity;
import no.lqasse.zoff.Server.Server;
import no.lqasse.zoff.Zoff;
/**
 * Created by lassedrevland on 04.04.15.
 */
public class ListAdapter extends ArrayAdapter<Video> {
        //private final String[] values;
        protected final ArrayList<Video> videoList;
        protected Context context;
        protected Zoff zoff;

    public ListAdapter(Context context, ArrayList<Video> videoList, Zoff zoff) {
        super(context, R.layout.now_playing_row, videoList);
        this.context = context;
        this.videoList = videoList;
        this.zoff = zoff;

    }


    public class ViewHolder {

        String imageURL;
        Bitmap bitmap;
        int position;
        Video video;
        ImageView imageView;
        ImageView deleteButton;
        ProgressBar progressBar;
        TextView title;
        TextView votes;
        Boolean huge = false;

    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;



        View rowView = convertView;

        Boolean recycledTopView = false;
        if (rowView!=null){
            recycledTopView = rowView.getId()== R.id.nowPlayingLayout;
        }


        if (rowView == null || recycledTopView){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.now_playing_row, parent, false);

            holder = new ViewHolder();
            holder.imageView      = (ImageView)    rowView.findViewById(R.id.imageView);
            holder.deleteButton   = (ImageView)    rowView.findViewById(R.id.deleteButton);
            holder.progressBar    = (ProgressBar)  rowView.findViewById(R.id.progressBar);
            holder.title          = (TextView)     rowView.findViewById(R.id.videoTitleView);
            holder.votes          = (TextView)     rowView.findViewById(R.id.votesView);

            rowView.setTag(holder);


        } else {
            rowView.setAlpha(1.00f);
            holder =  (ViewHolder) rowView.getTag();
            holder.imageView.setImageBitmap(null);

        }



        Video currentVideo = videoList.get(position);

        holder.title.setText(videoList.get(position).getTitle());
        holder.votes.setText(videoList.get(position).getVotes());
        holder.imageURL = videoList.get(position).getThumbMed();
        holder.position = position;
        holder.video = currentVideo;



            if (Zoff.getRoomPass() == null){
                holder.deleteButton.setVisibility(View.INVISIBLE);
            } else {
                setOnDelete(holder.deleteButton,position);
            }


        if (ImageCache.has(currentVideo.getId())) {
            Bitmap videoImage = ImageCache.get(currentVideo.getId());


            holder.imageView.setImageBitmap(videoImage);
            holder.progressBar.setVisibility(View.GONE);
        } else {
            ImageDownload.downloadAndSet(currentVideo.getThumbMed(),currentVideo.getThumbSmall(),currentVideo.getId(),holder.imageView, ImageCache.ImageType.REG);
        }

        return rowView;
    }


    public void setOnDelete(ImageView view, int position){
        view.setTag(position);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastMaster.showToast(context, ToastMaster.TYPE.HOLD_TO_DELETE);


            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final int index = (int) v.getTag();
                String videoID = videoList.get(index).getId();
                String title = videoList.get(index).getTitle();
                Server.delete(videoID);

                videoList.get(index);
                ToastMaster.showToast(context, ToastMaster.TYPE.VIDEO_DELETED, title);

                RelativeLayout row = (RelativeLayout) v.getParent();
                row.animate()
                        .alpha(0)
                        .setDuration(500)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                videoList.remove(index);
                                notifyDataSetChanged();

                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        })
                        .start();


                return true;
            }
        });
    }







}
