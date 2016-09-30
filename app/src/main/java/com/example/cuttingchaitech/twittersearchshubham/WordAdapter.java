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

public class WordAdapter extends RealmBaseAdapter {

    private Context mContext;
    private Realm realm;


    private static class itemViewHolder {
        private TextView ivWord;
        private TextView ivCount ;
    }

    public WordAdapter(Context context, int resId, RealmResults<Word> realmResults, boolean automaticUpdate) {
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
            view = inflater.inflate(R.layout.word_item_view, parent, false);
            holder.ivWord = (TextView) view.findViewById(R.id.ivWord);
            holder.ivCount = (TextView) view.findViewById(R.id.ivCount);
            view.setTag(holder);
        }
        Word word = (Word) realmResults.get(position);
        holder.ivWord.setText(word.getText());
        holder.ivCount.setText(String.valueOf(word.getCount()));
        return view;
    }




}
