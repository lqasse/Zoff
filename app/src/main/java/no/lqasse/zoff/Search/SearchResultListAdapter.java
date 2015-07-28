package no.lqasse.zoff.Search;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.ArrayList;

import no.lqasse.zoff.ImageTools.BitmapDownloader;
import no.lqasse.zoff.ImageTools.BitmapCache;
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

            holder.imageView       = (ImageView) rowView.findViewById(R.id.playlistHeaderImage);
            holder.progressBar      = (ProgressBar) rowView.findViewById(R.id.progressBar);
            holder.title            = (TextView) rowView.findViewById(R.id.playlistHeaderTitle);
            holder.channelTitle     = (TextView) rowView.findViewById(R.id.channelTitle);
            holder.viewCount        = (TextView) rowView.findViewById(R.id.viewsTextView);
            holder.duration         = (TextView) rowView.findViewById(R.id.durationView);



            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();



        }


        SearchResult currentVideo = searchResults.get(position);

        holder.imageView.setTag(currentVideo.getVideoID());







        if (BitmapCache.has(currentVideo.getVideoID())) {
            Bitmap videoImage = BitmapCache.get(currentVideo.getVideoID());
            holder.imageView.setImageBitmap(videoImage);
        } else {

            holder.imageView.setImageBitmap(null);
            BitmapDownloader.downloadAndSet(currentVideo.getThumbMed(), currentVideo.getThumbSmall(), currentVideo.getVideoID(), holder.imageView, BitmapCache.ImageSize.REG, true);
        };



        holder.title.setText(searchResults.get(position).getTitle());
        holder.channelTitle.setText(searchResults.get(position).getChannelTitle());
        holder.viewCount.setText(searchResults.get(position).getViewCountLocalized());
        holder.duration.setText(searchResults.get(position).getDurationLocalized());


        return rowView;


    }



    }
