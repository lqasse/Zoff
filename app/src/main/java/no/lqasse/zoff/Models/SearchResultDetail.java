package no.lqasse.zoff.Models;

/**
 * Created by lassedrevland on 16.06.15.
 */
public class SearchResultDetail {
    private String id;
    private String duration;
    private String views;

    public SearchResultDetail(String id, String views, String duration) {
        this.id = id;
        this.views = views;
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public String getDuration() {
        return duration;
    }

    public String getViews() {
        return views;
    }
}
