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
    private ListView videoList;
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
        videoList = (ListView) v.findViewById(R.id.videoPlaylist);
        videoList.setAdapter(listAdapter);





        videoList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (position != 0) { //Cant vote for current video duh
                    Video selectedVideo = listAdapter.getItem(position);
                    zoffController.vote(selectedVideo);
                    ToastMaster.showToast(activity, ToastMaster.TYPE.VIDEO_VOTED, selectedVideo.getTitle());

                }


                return true;
            }
        });

        videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position != 0) { //Currently playing video cant be voted on
                    ToastMaster.showToast(activity, ToastMaster.TYPE.HOLD_TO_VOTE);
                }


            }
        });

        videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });



        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        zoffController = ((Host) activity).getZoffController();

        listAdapter = new VideoListAdapterHeader(activity, zoffController);




        toolbar = ((Host) activity).getToolbar();






    }

    public void invalidateListviewViews(){
        if (videoList != null){
            videoList.invalidateViews();
        }
    }

    public void onZoffRefresh(ZoffModel zoffModel){
        listAdapter.notifyDataSetChanged();

    }

    public interface Host{
        ZoffController getZoffController();
        Toolbar getToolbar();
    }
}
