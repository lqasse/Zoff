package no.lqasse.zoff.Search;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import no.lqasse.zoff.ImageTools.BitmapDownloader;
import no.lqasse.zoff.ImageTools.ImageCache;
import no.lqasse.zoff.Models.SearchResult;
import no.lqasse.zoff.R;

/**
 * Created by lassedrevland on 09.12.14.
 */
public class SearchResultListAdapter extends ArrayAdapter<SearchResult> {
    private final Context context;
    //private final String[] values;
    private final ArrayList<SearchResult> searchResults;

    public SearchResultListAdapter(Context context, ArrayList<SearchResult> searchResults) {
        super(context, R.layout.search_row, searchResults);
        this.context = context;
        this.searchResults = searchResults;
    }


    private class ViewHolder{
         ImageView imageView    ;
         ProgressBar progressBar;
         TextView title         ;
         TextView channelTitle  ;
         TextView viewCount     ;
         TextView duration      ;

    }
    private
    class downloadViewHolder {
        ImageView imageView;
        String imageURL;
        Bitmap bitmap;
        int position;
        ProgressBar progressBar;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        downloadViewHolder downloadViewHolder;
        ViewHolder holder;


        View rowView = convertView;
        if (rowView == null){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.search_row, parent, false);

            holder = new ViewHolder();

            holder.imageView       = (ImageView) rowView.findViewById(R.id.imageView);
            holder.progressBar      = (ProgressBar) rowView.findViewById(R.id.progressBar);
            holder.title            = (TextView) rowView.findViewById(R.id.videoTitleView);
            holder.channelTitle     = (TextView) rowView.findViewById(R.id.channelTitle);
            holder.viewCount        = (TextView) rowView.findViewById(R.id.viewsTextView);
            holder.duration         = (TextView) rowView.findViewById(R.id.durationView);



            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();



        }


        SearchResult currentVideo = searchResults.get(position);

        holder.imageView.setTag(currentVideo.getVideoID());







        if (ImageCache.has(currentVideo.getVideoID())) {
            Bitmap videoImage = ImageCache.get(currentVideo.getVideoID());
            holder.imageView.setImageBitmap(videoImage);
        } else {

            holder.imageView.setImageBitmap(null);
            BitmapDownloader.downloadAndSet(currentVideo.getThumbSmall(), currentVideo.getThumbSmall(), currentVideo.getVideoID(), holder.imageView, ImageCache.ImageSize.REG, true);
        };



        /*

        if (searchResults.get(position).getImgSmall() == null){
            downloadViewHolder = new downloadViewHolder();
            downloadViewHolder.imageView = holder.imageView;
            downloadViewHolder.imageURL = searchResults.get(position).getThumbSmall();
            downloadViewHolder.position = position;
            downloadViewHolder.progressBar = holder.progressBar;

            holder.imageView.setImageBitmap(null);
            holder.progressBar.setVisibility(View.VISIBLE);
            new downloadImage().execute(downloadViewHolder);
        } else {
            holder.imageView.setImageBitmap(searchResults.get(position).getImgSmall());
            holder.progressBar.setVisibility(View.GONE);
        }

        */


        holder.title.setText(searchResults.get(position).getTitle());
        holder.channelTitle.setText(searchResults.get(position).getChannelTitle());
        holder.viewCount.setText(searchResults.get(position).getViewCountLocalized());
        holder.duration.setText(searchResults.get(position).getDurationLocalized());


        return rowView;


    }
/*


    private class downloadImage extends AsyncTask<downloadViewHolder, Void, downloadViewHolder>{

        @Override
        protected downloadViewHolder doInBackground(downloadViewHolder... params){
            downloadViewHolder downloadViewHolder = params[0];

            try {
                URL imageURL = new URL(downloadViewHolder.imageURL);
                downloadViewHolder.bitmap = BitmapFactory.decodeStream(imageURL.openStream());
            } catch (Exception e){
                Log.d("ERROR", e.getLocalizedMessage());
                e.printStackTrace();
                downloadViewHolder.bitmap = null;
            }
            return downloadViewHolder;
        }

        @Override
        protected void onPostExecute(downloadViewHolder result){
            if (result.bitmap == null){
                Log.d("FAIL", "NO IMAGE");
            } else {
               result.progressBar.setVisibility(View.GONE);

                Animation a = new AlphaAnimation(0.00f,1.00f);
                a.setInterpolator(new DecelerateInterpolator());
                a.setDuration(700);
                result.imageView.setImageBitmap(result.bitmap);
                result.imageView.setAnimation(a);
                result.imageView.startAnimation(a);
               searchResults.get(result.position).setImgSmall(result.bitmap);
            }
        }

    }


*/


    }
