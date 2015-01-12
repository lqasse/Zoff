package no.lqasse.zoff;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by lassedrevland on 12.12.14.
 */
public class nowPlayingAdapter extends ArrayAdapter<Video> {
    private final Context context;
    //private final String[] values;
    private final ArrayList<Video> results;

    public nowPlayingAdapter(Context context, ArrayList<Video> results) {
        super(context, R.layout.now_playing_row, results);
        this.context = context;
        this.results = results;

    }

    private static
    class ViewHolder{
        ImageView imageView;
        String imageURL;
        Bitmap bitmap;
        int position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;


        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.now_playing_row, parent, false);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);

        viewHolder = new ViewHolder();
        viewHolder.imageView = imageView;
        viewHolder.imageURL = results.get(position).getThumbMed();
        viewHolder.position = position;

        if (results.get(position).getImgSmall() == null){
            new downloadImage().execute(viewHolder);
        } else {
            imageView.setImageBitmap(results.get(position).getImgSmall());
        }


        TextView title = (TextView) rowView.findViewById(R.id.title);
        TextView votes = (TextView) rowView.findViewById(R.id.votesView);


        title.setText(results.get(position).getTitle());
        votes.setText(results.get(position).getVotes());
        //textView.setText(values[position]);
        // change the icon for Windows and iPhone
        //String s = values[position];


        return rowView;
    }

    private class downloadImage extends AsyncTask<ViewHolder, Void, ViewHolder> {



        @Override
        protected ViewHolder doInBackground(ViewHolder... params){
            ViewHolder viewHolder = params[0];

            try {
                URL imageURL = new URL(viewHolder.imageURL);
                viewHolder.bitmap = BitmapFactory.decodeStream(imageURL.openStream());
            } catch (Exception e){
                Log.d("ERROR", e.getLocalizedMessage());
                e.printStackTrace();
                viewHolder.bitmap = null;
            }
            return viewHolder;
        }

        @Override
        protected void onPostExecute(ViewHolder result){
            if (result.bitmap == null){
                Log.d("FAIL", "NO IMAGE");
            } else {
                result.imageView.setImageBitmap(result.bitmap);
                results.get(result.position).setImgSmall(result.bitmap);
            }
        }

    }

}