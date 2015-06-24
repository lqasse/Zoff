package no.lqasse.zoff.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import no.lqasse.zoff.Models.Video;

/**
 * Created by lassedrevland on 21.06.15.
 */


public class VideoListRecyclerAdapter extends RecyclerView.Adapter<VideoListRecyclerAdapter.ViewHolder> {



    public class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
