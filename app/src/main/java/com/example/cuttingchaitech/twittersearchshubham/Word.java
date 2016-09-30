package com.example.cuttingchaitech.twittersearchshubham;

import io.realm.RealmObject;

/**
 * Created by Cutting Chai Tech on 29-09-2016.
 */

public class Word extends RealmObject {

    private String text;
    private int count;

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
