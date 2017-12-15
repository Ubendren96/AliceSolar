package io.ubendren.imageprocessing;

/**
 * Created by ajith on 12/5/17.
 */

public class FeedItem {
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    private String title;
    private String thumbnail;
}
