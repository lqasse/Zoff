package no.lqasse.zoff.Search;

import android.text.Html;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import no.lqasse.zoff.Models.SearchResult;

/**
 * Created by lassedrevland on 24.03.15.
 */
public class YouTubeJSONTranslator {

    public static ArrayList<SearchResult> toSearchResults(String JSONString){
        JSONObject json;
        ArrayList<SearchResult> searchResults = new ArrayList<>();
        String nextPageToken = "";
        try {
            json = new JSONObject(JSONString);


            if (json.has("nextPageToken")){
                nextPageToken = json.getString("nextPageToken");

            }

            JSONArray items = json.getJSONArray("items");

            for (int i = 0; i < items.length() - 1; i++) {

                String title;
                String channelTitle;
                String description;
                String videoID;
                String publishedAt;
                String thumbDefault;
                String thumbMedium;
                String thumbHigh;

                json = items.getJSONObject(i);

                JSONObject idObject = json.getJSONObject("id");

                videoID = idObject.getString("videoId");
                //ids.add(videoID);


                json = json.getJSONObject("snippet");
                publishedAt = json.getString("publishedAt");
                title = json.getString("title");
                channelTitle = json.getString("channelTitle");

                //Decode string from html
                title = Html.fromHtml(title).toString();

                description = json.getString("description");

                JSONObject thumbnailsObject = json.getJSONObject("thumbnails");
                JSONObject thumbDefaultObject = thumbnailsObject.getJSONObject("default");
                JSONObject thumbMedObject = thumbnailsObject.getJSONObject("medium");
                JSONObject thumbHighObject = thumbnailsObject.getJSONObject("high");

                thumbDefault = thumbDefaultObject.getString("url");
                thumbMedium = thumbMedObject.getString("url");
                thumbHigh = thumbHighObject.getString("url");



                searchResults.add(
                        new SearchResult(title, channelTitle,description, publishedAt,videoID, thumbDefault, thumbMedium, thumbHigh)
                );
            }




        }catch (JSONException e){
            e.printStackTrace();
        }

        return searchResults;

    }
}
