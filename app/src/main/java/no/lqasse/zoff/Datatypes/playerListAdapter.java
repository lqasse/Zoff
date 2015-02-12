package no.lqasse.zoff.Datatypes;

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
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;

import no.lqasse.zoff.R;

/**
 * Created by lassedrevland on 12.12.14.
 */
public class playerListAdapter extends ArrayAdapter<Zoff.Video> {
    private final Context context;
    //private final String[] values;
    private final ArrayList<Zoff.Video> results;
    private Zoff zoff;

    public playerListAdapter(Context context, ArrayList<Zoff.Video> results) {
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
        ProgressBar progressBar;
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        viewHolder = new ViewHolder();

        View rowView;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        rowView = inflater.inflate(R.layout.now_playing_row, parent, false);
        viewHolder.imageURL = results.get(position).getThumbMed();


        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);

        viewHolder.imageView = imageView;
        viewHolder.position = position;
        viewHolder.progressBar = progressBar;


        if (results.get(position).getImg() == null){
            new downloadImage().execute(viewHolder);
        } else {
            imageView.setImageBitmap(results.get(position).getImg());
            progressBar.setVisibility(View.GONE);
        }


        TextView title = (TextView) rowView.findViewById(R.id.title);
        TextView votes = (TextView) rowView.findViewById(R.id.votesView);


        title.setText(results.get(position).getTitle());
        if (votes != null){
            votes.setText(results.get(position).getVotes());
        }

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



                result.progressBar.setVisibility(View.GONE);

                //Animate fade in <3
                Animation a = new AlphaAnimation(0.00f,1.00f);
                a.setInterpolator(new DecelerateInterpolator());
                a.setDuration(700);
                result.imageView.setImageBitmap(result.bitmap);
                result.imageView.setAnimation(a);
                result.imageView.startAnimation(a);

                results.get(result.position).setImg(result.bitmap);



            }
        }

    }

}