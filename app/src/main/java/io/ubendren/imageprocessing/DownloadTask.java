package io.ubendren.imageprocessing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ajith on 12/15/17.
 */

public class DownloadTask extends AsyncTask<String, Void, Integer> {

    private static final String TAG="DownloadTask";
    private List<FeedItem> feedsList;
    private Activity activity;
    private RecyclerView mRecyclerView;
    private MyRecyclerViewAdapter adapter;
    private ProgressBar progressBar;
    private boolean actmain=false;
    Intent intent;

    DownloadTask(Activity activity,RecyclerView recyclerView,ProgressBar progressBar,boolean main){
        this.activity=activity;
        this.mRecyclerView = recyclerView;
        this.progressBar=progressBar;
        this.actmain=main;
    }

    @Override
    protected void onPreExecute() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Integer doInBackground(String... params) {
        Integer result = 0;
        HttpURLConnection urlConnection;
        try {
            URL url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            int statusCode = urlConnection.getResponseCode();

            // 200 represents HTTP OK
            if (statusCode == 200) {
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    response.append(line);
                }
                parseResult(response.toString());
                result = 1; // Successful
            } else {
                result = 0; //"Failed to fetch data!";
            }
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return result; //"Failed to fetch data!";
    }

    @Override
    protected void onPostExecute(Integer result) {
        progressBar.setVisibility(View.GONE);

        if (result == 1) {
            adapter = new MyRecyclerViewAdapter(activity, feedsList);
            mRecyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(FeedItem item) {
                    /*if (actmain){
                        switch (item.getTitle()){
                            case "Inverter":
                                MainActivity.TYPE=0;
                                break;
                            case "Battery":
                                MainActivity.TYPE=1;
                                break;
                            case "Panel":
                                MainActivity.TYPE=2;
                                break;
                            default:
                                MainActivity.TYPE=0;
                                break;
                        }
                        
                    } else {
                        Toast.makeText(activity, item.getTitle(), Toast.LENGTH_LONG).show();
                    }*/
                    
                }
            });

        } else {
            //Toast.makeText(MainActivity.class, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
        }
    }

    private void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray posts = response.optJSONArray("posts");
            feedsList = new ArrayList<>();

            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);
                FeedItem item = new FeedItem();
                item.setTitle(post.optString("title"));
                item.setThumbnail(post.optString("thumbnail"));
                feedsList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}