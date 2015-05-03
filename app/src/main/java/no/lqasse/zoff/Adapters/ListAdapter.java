package no.lqasse.zoff.Adapters;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.ImageTools.ImageDownload;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.R;
import no.lqasse.zoff.Models.Zoff;
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
        int position;
        Video video;
        ImageView imageView;
        ImageView deleteButton;
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

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.now_playing_row, parent, false);

            holder = new ViewHolder();
            holder.imageView      = (ImageView)    rowView.findViewById(R.id.imageView);
            holder.deleteButton   = (ImageView)    rowView.findViewById(R.id.deleteButton);
            holder.title          = (TextView)     rowView.findViewById(R.id.videoTitleView);
            holder.votes          = (TextView)     rowView.findViewById(R.id.votesView);

            if (zoff.hasPassword()){
                holder.deleteButton.setVisibility(View.VISIBLE);
            } else {
                holder.deleteButton.setVisibility(View.INVISIBLE);
            }



            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToastMaster.showToast(context, ToastMaster.TYPE.HOLD_TO_DELETE);


                }
            });

            holder.deleteButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final int index = (int) v.getTag();
                    zoff.delete(videoList.get(index));
                    videoList.remove(index);

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

            rowView.setTag(holder);


        } else {
            rowView.setAlpha(1.00f);
            holder =  (ViewHolder) rowView.getTag();
            holder.imageView.setImageBitmap(null);

        }


        Video currentVideo = videoList.get(position);




        holder.title.setText(videoList.get(position).getTitle());
        holder.votes.setText(videoList.get(position).getVotesString());
        holder.imageURL = videoList.get(position).getThumbMed();
        holder.position = position;
        holder.video = currentVideo;
        holder.deleteButton.setTag(position);
        holder.imageView.setTag(currentVideo.getId());







        if (ImageCache.has(currentVideo.getId())) {
            Bitmap videoImage = ImageCache.get(currentVideo.getId());
            holder.imageView.setImageBitmap(videoImage);
        } else {
            ImageDownload.downloadAndSet(currentVideo.getThumbMed(), currentVideo.getThumbSmall(), currentVideo.getId(), holder.imageView, ImageCache.ImageType.REG);
        }

        return rowView;
    }










}