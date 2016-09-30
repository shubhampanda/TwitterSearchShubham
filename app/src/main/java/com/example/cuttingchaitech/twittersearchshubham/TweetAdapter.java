package com.example.cuttingchaitech.twittersearchshubham;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by Cutting Chai Tech on 29-09-2016.
 */

public class TweetAdapter extends RealmBaseAdapter {


    private Context mContext;
    private Realm realm;


    private static class itemViewHolder {
        private TextView ivTweet;
    }

    public TweetAdapter(Context context, int resId, RealmResults<Tweet> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
        mContext = context;
        realm = Realm.getInstance(mContext);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        itemViewHolder holder;
        if (view != null) {
            holder = (itemViewHolder) view.getTag();
        } else {
            holder = new itemViewHolder();
            view = inflater.inflate(R.layout.tweet_item_view, parent, false);
            holder.ivTweet = (TextView) view.findViewById(R.id.ivTweet);
            view.setTag(holder);
        }
        Tweet tweet = (Tweet) realmResults.get(position);
        holder.ivTweet.setText(tweet.getText());
        return view;
    }


}
