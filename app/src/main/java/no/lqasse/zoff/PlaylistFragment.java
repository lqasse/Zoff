package no.lqasse.zoff;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import no.lqasse.zoff.Adapters.HidingScrollListener;
import no.lqasse.zoff.Adapters.ItemDecorator;
import no.lqasse.zoff.Adapters.VideoListAdapter;
import no.lqasse.zoff.Adapters.VideoListAdapterHeader;
import no.lqasse.zoff.Adapters.VideoListRecyclerAdapter;
import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.Models.Video;
import no.lqasse.zoff.Models.ZoffController;
import no.lqasse.zoff.Models.ZoffModel;
import no.lqasse.zoff.Remote.RemoteActivity;
import no.lqasse.zoff.Search.YouTube;

/**
 * Created by lassedrevland on 16.06.15.
 */
public class PlaylistFragment extends Fragment {
    private RecyclerView videoList;
    private Toolbar toolbar;
    private View header;
    private Activity activity;
    private RelativeLayout layout;
    private ZoffController zoffController;
    private VideoListAdapter listAdapter;
    private VideoListRecyclerAdapter recyclerAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.playlist_fragment,container,false);
        videoList = (RecyclerView) v.findViewById(R.id.videoPlaylist);
        //videoList.setAdapter(listAdapter);

        recyclerAdapter = new VideoListRecyclerAdapter(zoffController);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        videoList.setLayoutManager(layoutManager);
        videoList.addItemDecoration(new ItemDecorator(10));
        videoList.setAdapter(recyclerAdapter);

        videoList.setOnScrollListener(new HidingScrollListener((int)toolbar.getHeight()) {
            @Override
            public void toolbarOffset(int calculatedOffset) {
                toolbar.setTranslationY(calculatedOffset);
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        zoffController = ((Host) activity).getZoffController();




        toolbar = ((Host) activity).getToolbar();






    }

    public void invalidateListviewViews(){
        if (videoList != null){
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    public void onZoffRefresh(ZoffModel zoffModel){
        recyclerAdapter.notifyDataSetChanged();

    }

    public interface Host{
        ZoffController getZoffController();
        Toolbar getToolbar();
    }
}
