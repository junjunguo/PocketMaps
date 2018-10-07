package com.junjunguo.pocketmaps.model;

import com.junjunguo.pocketmaps.util.Calorie;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on August 30, 2015.
 */
public class SportCategory {
    private String text;
    private Integer imageId;
    private Calorie.Type sportMET;

    public SportCategory(String text, Integer imageId, Calorie.Type activityMET) {
        this.text = text;
        this.imageId = imageId;
        this.sportMET = activityMET;
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

    public Calorie.Type getSportMET() {
        return sportMET;
    }
}
