package com.hku.tripals.model;

import com.yalantis.filter.model.FilterModel;

public class Tag implements FilterModel {
    private String text;

    public Tag(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
