package no.lqasse.zoff.Adapters;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by lassedrevland on 24.06.15.
 */
public class ItemDecorator extends RecyclerView.ItemDecoration {
    private int topSpace = 10;
    private int sideSpace = 10;
    private int bottomSpace = 25;

    public ItemDecorator() {
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = sideSpace;
        outRect.right = sideSpace;
        outRect.bottom = bottomSpace;

        // Add top margin only for the first item to avoid double space between items
        if(parent.getChildPosition(view) == 0)
            outRect.top = topSpace;
    }
}
