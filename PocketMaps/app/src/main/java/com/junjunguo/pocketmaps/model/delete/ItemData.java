package com.junjunguo.pocketmaps.model.delete;

/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on June 25, 2015.
 */
public class ItemData {
    private int iconResId;
    private String description, text;

    public ItemData() {
    }

    public ItemData(int iconResId, String description, String text) {
        this.iconResId = iconResId;
        this.description = description;
        this.text = text;
    }

    public ItemData(int iconResId, String text) {
        this.iconResId = iconResId;
        this.text = text;
    }

    public ItemData(String description, String text) {
        this.description = description;
        this.text = text;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override public String toString() {
        return "ItemData{" +
                "iconResId=" + iconResId +
                ", description='" + description + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
