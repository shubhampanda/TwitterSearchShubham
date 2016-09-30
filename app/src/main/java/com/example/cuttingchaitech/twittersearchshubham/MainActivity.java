package com.example.cuttingchaitech.twittersearchshubham;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    Realm realm;
    ListView listView;
    EditText mSearchText;
    Button mSearchTweetsButton;
    Button mSearchWordsButton;
    String oauthToken;
    SharedPreferences sharedPreferences;
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweets_display);
        realm = Realm.getInstance(this);
        getOauthToken();
        initViews();
        RealmResults<Tweet> tweets = realm.where(Tweet.class).findAll();
        setupTweetAdapter(tweets);
        sharedPreferences = getSharedPreferences("myPref", Context.MODE_PRIVATE);

    }

    private void initViews() {
        listView = (ListView) findViewById(R.id.listView);
        mSearchText = (EditText) findViewById(R.id.toSearch);
        mSearchTweetsButton = (Button) findViewById(R.id.searchTweets);
        mSearchWordsButton = (Button) findViewById(R.id.searchWords);
        mSearchTweetsButton.setOnClickListener(this);
        mSearchWordsButton.setOnClickListener(this);
    }

    private void setupTweetAdapter(RealmResults<Tweet> tweets) {
        TweetAdapter tweetAdapter = new TweetAdapter(this, R.layout.activity_tweets_display, tweets, false);
        listView.setAdapter(tweetAdapter);
    }


    private void getOauthToken() {
        final RequestQueue queue = Volley.newRequestQueue(this);
        final String requestBody = "grant_type=client_credentials";
        String urlKey = null;
        try {
            urlKey = URLEncoder.encode(Constants.TWITTER_CONSUMER_API_KEY, "UTF-8");
            String urlSecret = URLEncoder.encode(Constants.TWITTER_CONSUMER_SECRET, "UTF-8");
            // Concatenate the encoded consumer key
            String combined = urlKey + ":" + urlSecret;
            // Base64 encode the string
            final String base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.TWITTER_OAUTH_URL, requestBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("onResponse", response.toString());
                    try {
                        JSONObject object = new JSONObject(response.toString());
                        oauthToken = object.getString("access_token");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("onErrorResponse", error.toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    // Basic Authentication
                    //String auth = "Basic " + Base64.encodeToString(CONSUMER_KEY_AND_SECRET.getBytes(), Base64.NO_WRAP);
                    String auth = "Basic " + base64Encoded;
                    headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    headers.put("Authorization", auth);
                    return headers;
                }
            };
            queue.add(jsonObjectRequest);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void searchTweets(final String toSearch, final String accessToken) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                String url = Constants.TWITTER_SEARCH_URL;
                String searchParameter = "?q=" + toSearch + "&count=100";
                String finalUrl = url + searchParameter;
                StringRequest postRequest = new StringRequest(Request.Method.GET, finalUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "The response is " + response);
                        try {
                            JSONObject object = new JSONObject(response);
                            final JSONArray searchResults = object.getJSONArray("statuses");
                            Log.d(TAG, "The searchResults size is " + searchResults.length());
                            for (int i = 0; i < searchResults.length(); i++) {
                                JSONObject search = searchResults.getJSONObject(i);
                                String searchText = search.getString("text");
                                addTweet(searchText);
                                Log.d(TAG, "The searchText is " + searchText);
                                StringTokenizer tokenizer = new StringTokenizer(searchText);
                                while (tokenizer.hasMoreTokens()) {
                                    try {
                                        String token = tokenizer.nextToken();
                                        addWord(token);
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            messageHandler.sendEmptyMessage(0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ERROR", "error => " + error.toString());
                        messageHandler.sendEmptyMessage(0);
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", "Bearer " + accessToken);
                        return headers;

                    }
                };

                queue.add(postRequest);
            }
        });
        thread.start();


    }

    private void addTweet(final String searchText) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Tweet tweet = realm.createObject(Tweet.class);
                tweet.setText(searchText);
            }
        });
    }

    public void addWord(final String text) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (!text.contains("#") && !text.contains("@") && !Patterns.WEB_URL.matcher(text).matches()) {
                    Word word = realm.where(Word.class).equalTo("text", text, Case.INSENSITIVE).findFirst();
                    if (word != null) {
                        // If the word already exists
                        int count = word.getCount();
                        word.setCount(count + 1);
                    } else {
                        // create a new word
                        word = realm.createObject(Word.class);
                        word.setText(text);
                        word.setCount(1);
                    }
                    Log.d("MainActivity", "The word with text " + word.getText() + "has count " + word.getCount());
                }
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchTweets:
                RealmResults<Tweet> tweets = realm.where(Tweet.class).findAll();
                setupTweetAdapter(tweets);
                String toSearch = mSearchText.getText().toString().trim();
                if (mSearchText.getText().toString().length() > 0) {
                    if (!isSameSearch(toSearch)) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
                        dialog = new ProgressDialog(this);
                        dialog.setMessage("Loading the tweets");
                        dialog.setCancelable(true);
                        dialog.setIndeterminate(false);
                        dialog.show();
                        initializeRequest();
                    }
                } else {
                    Toast.makeText(this, "This is not a valid input", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.searchWords:
                RealmResults<Word> words = realm.where(Word.class).findAllSorted("count", Sort.DESCENDING);
                setupWordAdapter(words);
                break;
        }
    }

    private void setupWordAdapter(RealmResults<Word> words) {
        WordAdapter wordAdapter = new WordAdapter(this, R.layout.activity_tweets_display, words, false);
        listView.setAdapter(wordAdapter);
    }


    private boolean isSameSearch(String toSearch) {
        boolean retVal = true;
        if (!sharedPreferences.getString("searchText", Constants.SEARCH_DEFAULT_VALUE).equals(toSearch)) {
            retVal = false;
        }
        return retVal;
    }

    private void initializeRequest() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Tweet> tweets = realm.where(Tweet.class).findAll();
                tweets.clear();
                RealmResults<Word> words = realm.where(Word.class).findAll();
                words.clear();
            }
        }, new Realm.Transaction.Callback() {
            @Override
            public void onSuccess() {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("searchText", mSearchText.getText().toString());
                editor.commit();
                ;
                searchTweets(mSearchText.getText().toString(), oauthToken);
            }
        });
    }


    private Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            RealmResults<Tweet> tweets = realm.where(Tweet.class).findAll();
            if (tweets.size() == 0) {
                Toast.makeText(MainActivity.this, "Sorry no available tweets", Toast.LENGTH_SHORT).show();
            }
            setupTweetAdapter(tweets);
            dialog.cancel();
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        if (!sharedPreferences.getString("searchText", Constants.SEARCH_DEFAULT_VALUE).equals(Constants.SEARCH_DEFAULT_VALUE)) {
            mSearchText.setText(sharedPreferences.getString("searchText", Constants.SEARCH_DEFAULT_VALUE));
        }
    }
}
