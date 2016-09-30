package com.example.cuttingchaitech.twittersearchshubham;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by Cutting Chai Tech on 29-09-2016.
 */

public class Tweet extends RealmObject {

    private String text;

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
