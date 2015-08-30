package com.junjunguo.pocketmaps.model.dataType;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on August 30, 2015.
 */
public class AnalyticsActivityType {
    private String text;
    private Integer imageId;

    public AnalyticsActivityType(String text, Integer imageId) {
        this.text = text;
        this.imageId = imageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }
}
