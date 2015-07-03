package no.lqasse.zoff.Adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by lassedrevland on 24.06.15.
 */
public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {

    private int offset = 0;
    private int maxOffset = -180;

    public HidingScrollListener(int toolbarHeight){
        //this.toolbarHeight = toolbarHeight;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);


        if (dy > 0 && offset > maxOffset){
            offset -= dy;

        } else if (dy < 0 && offset < 0){

            if ((offset - dy) > 0){
                offset = 0;
            } else {
                offset -= dy;
            }

        }


        toolbarOffset(offset);






    }


    public abstract void toolbarOffset(int calculatedOffset);


}
