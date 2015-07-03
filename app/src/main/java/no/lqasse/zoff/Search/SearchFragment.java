package no.lqasse.zoff.Search;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import no.lqasse.zoff.Helpers.ToastMaster;
import no.lqasse.zoff.Models.ZoffController;
import no.lqasse.zoff.R;

/**
 * Created by lassedrevland on 15.06.15.
 */
public class SearchFragment extends Fragment {
    private static final int AUTOSEARCH_DELAY_MILLIS = 600;

    private Toolbar toolbar;
    private EditText toolBarSearchField;
    private TextView toolBarTitle;

    private Activity activity;
    private ZoffController zoffController;
    private ListView resultsList;
    private SearchResultListAdapter searchAdapter;
    private LinearLayout searchFragment;

    private Boolean isGettingPage = false;

    private MenuItem skip;
    private MenuItem shuffle;
    private MenuItem search;
    private MenuItem close;

    private Handler handler = new Handler();
    private Runnable delaySearch = new Runnable() {
        @Override
        public void run() {

            doSearch(toolBarSearchField.getText().toString());
            //YouTube.search(activity, toolBarSearchField.getText().toString(), zoff.getSettings().isAllvideos(), zoff.getSettings().isLongsongs());
            //loadingProgressbar.setVisibility(View.VISIBLE);
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SearchFragment", "onCreate");


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        toolbar = ((Host) activity).getToolbar();
        zoffController = ((Host) activity).getZoffController();


        this.activity = activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_search, container,false);

        searchFragment = (LinearLayout) v.findViewById(R.id.searchFragment);
        resultsList = (ListView) v.findViewById(R.id.searchFragmentListView);
        toolBarSearchField = (EditText) toolbar.findViewById(R.id.tool_bar_search_edittext);
        toolBarTitle = (TextView) toolbar.findViewById(R.id.toolbarTitle);
        skip = toolbar.getMenu().findItem(R.id.action_skip);
        search = toolbar.getMenu().findItem(R.id.action_search);
        shuffle = toolbar.getMenu().findItem(R.id.action_shuffle);
        close = toolbar.getMenu().findItem(R.id.action_close_searchfield);

        searchAdapter = new SearchResultListAdapter(activity,YouTube.getSearchResults());
        resultsList.setAdapter(searchAdapter);
        searchAdapter.notifyDataSetChanged();

        resultsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {


                if (resultsList.getLastVisiblePosition() == YouTube.getSearchResults().size() - 10 && !isGettingPage) {

                    isGettingPage = true;
                    YouTube.getNextPage(new YouTube.Callback() {
                        @Override
                        public void onResultsChanged() {
                            searchAdapter.notifyDataSetChanged();
                            isGettingPage = false;
                        }
                    });
                    Log.d("Scroll", "Loading next page");

                }



            }
        });

        resultsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ToastMaster.showToast(activity, ToastMaster.TYPE.HOLD_TO_ADD);


            }
        });

        resultsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {



                    String videoTitle = YouTube.getSearchResults().get(position).getTitle();
                    String videoID = (YouTube.getSearchResults().get(position)).getVideoID();
                    String duration = (YouTube.getSearchResults().get(position)).getDuration();

                    zoffController.add(videoID, videoTitle, duration);

                    ToastMaster.showToast(activity, ToastMaster.TYPE.VIDEO_ADDED, videoTitle);





                return true;
            }
        });


       hideNonSearchMenuItems();

        //Open softkeyboard
        toolBarSearchField.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);



        toolBarSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {


                Boolean textFieldEmpty = s.toString().equals("");



                handler.removeCallbacks(delaySearch);
                if (!textFieldEmpty) {

                    handler.postDelayed(delaySearch, AUTOSEARCH_DELAY_MILLIS);

                }


            }
        });

        return v;

    }

    private void doSearch(String query){
        YouTube.search(query, zoffController.getZoff().getSettings(), new YouTube.Callback() {
            @Override
            public void onResultsChanged() {
                searchAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        displayNonSearchMenuItems();


    }



    public interface Host{
        Toolbar getToolbar();
        ZoffController getZoffController();
    }

    private void hideNonSearchMenuItems(){

        skip.setVisible(false);
        shuffle.setVisible(false);
        search.setVisible(false);
        shuffle.setVisible(false);
        close.setVisible(true);

        toolBarTitle.setVisibility(View.GONE);
        toolBarSearchField.setVisibility(View.VISIBLE);




    }

    private void displayNonSearchMenuItems(){

        skip.setVisible(true);
        shuffle.setVisible(true);
        search.setVisible(true);
        shuffle.setVisible(true);
        close.setVisible(false);

        toolBarTitle.setVisibility(View.VISIBLE);
        toolBarSearchField.setVisibility(View.GONE);

    }




}
