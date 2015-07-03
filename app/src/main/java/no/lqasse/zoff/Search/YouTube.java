package no.lqasse.zoff.Search;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import no.lqasse.zoff.Interfaces.YouTubeListener;
import no.lqasse.zoff.Models.SearchResult;
import no.lqasse.zoff.Models.SearchResultDetail;
import no.lqasse.zoff.Models.ZoffSettings;

/**
 * Created by lassedrevland on 26.03.15.
 */
public class YouTube {

    private static ArrayList<SearchResult> searchResults = new ArrayList<>();
    private static HashMap<String,SearchResult> searchResultIDHashMap = new HashMap<>();
    private static String nextPageToken = "";
    private static String query;
    private static ZoffSettings settings;



    public static ArrayList<SearchResult> getSearchResults() {
        return searchResults;
    }

    public static void search(final String query, final ZoffSettings settings, final Callback callback){
        nextPageToken = "";
        YouTube.query = query;
        YouTube.settings = settings;


        YouTubeServer.doSearch(query, "", settings, new YouTubeServer.Callback() {
           @Override
           public void onGotResults(String resultsJSON) {

               nextPageToken = YouTubeJSONTranslator.toNextPageToken(resultsJSON);
               searchResults.clear();
               searchResultIDHashMap.clear();

               searchResults.addAll(YouTubeJSONTranslator.toSearchResults(resultsJSON));

               for (SearchResult r : searchResults){
                   searchResultIDHashMap.put(r.getVideoID(), r);
               }

               callback.onResultsChanged();
           }

           @Override
           public void onGotDetails(String detailsJSON) {

               for (SearchResultDetail detail : YouTubeJSONTranslator.toDetailsArray(detailsJSON)){
                   SearchResult result = searchResultIDHashMap.get(detail.getId());
                   if (result != null ){
                       result.setDuration(detail.getDuration());
                       result.setViews(detail.getViews());
                   }

               }
               callback.onResultsChanged();
           }


       });

    }

    public static void getNextPage(final Callback callback){
        YouTubeServer.doSearch(query, nextPageToken, settings, new YouTubeServer.Callback() {
            @Override
            public void onGotResults(String results) {
                nextPageToken = YouTubeJSONTranslator.toNextPageToken(results);

                searchResults.addAll(YouTubeJSONTranslator.toSearchResults(results));

                for (SearchResult r : searchResults){
                    searchResultIDHashMap.put(r.getVideoID(), r);
                }

                callback.onResultsChanged();
            }

            @Override
            public void onGotDetails(String details) {
                for (SearchResultDetail detail : YouTubeJSONTranslator.toDetailsArray(details)){
                    SearchResult result = searchResultIDHashMap.get(detail.getId());
                    if (result != null ){
                        result.setDuration(detail.getDuration());
                        result.setViews(detail.getViews());
                    }

                }
                callback.onResultsChanged();
            }
        });
    }

    public interface Callback{
        void onResultsChanged();
    }




}
