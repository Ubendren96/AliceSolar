package io.ubendren.imageprocessing;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;
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

import io.ubendren.imageprocessing.AccManager.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "RecyclerViewExample";

    private static final int REQ_SIGNUP = 1;

    private AccountManager mAccountManager;
    private AuthPreferences mAuthPreferences;
    private String authToken;

    Handler handler;
    Runnable runnable;


    private List<FeedItem> feedsList;
    private List<OfferItem> offerItems;
    private RecyclerView mRecyclerView;
    private RecyclerView mOfferView;
    private MyRecyclerViewAdapter adapter;
    private OfferRecyclerViewAdapter offerAdapter;

    private ProgressBar progressBar;
    SwipeRefreshLayout mSwipeRefreshLayout;
    static String[] url;
    Intent intent;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        authToken = null;
        mAuthPreferences = new AuthPreferences(this);
        mAccountManager = AccountManager.get(this);

        // Ask for an auth token
        mAccountManager.getAuthTokenByFeatures(AccountUtils.ACCOUNT_TYPE, AccountUtils.AUTH_TOKEN_TYPE, null, this, null, null, new MainActivity.GetAuthTokenCallback(), null);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mOfferView = (RecyclerView)findViewById(R.id.offers_recycler_view);
        mOfferView.setLayoutManager(new ScrollingLinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false,5000));

        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);

        url = new String[]{"https://stoneware-hickories.000webhostapp.com/Mainmenu.json", "https://stoneware-hickories.000webhostapp.com/Mainmenu.json"};
//        String url="http://stacktips.com/?json=get_category_posts&slug=news&count=30";
        new DownloadTask().execute(url);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        initiateRefresh();
                    }
                }
        );
    }


    public class DownloadTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            HttpURLConnection urlConnection;
            for (String p : params) {
                try {
                    URL url = new URL(p);
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
                        result = 0; //"Fai
                        // led to fetch data!";
                    }
                } catch (Exception e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
            }

            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            progressBar.setVisibility(View.GONE);

            if (result == 1) {
                adapter = new MyRecyclerViewAdapter(MainActivity.this, feedsList);
                offerAdapter = new OfferRecyclerViewAdapter(MainActivity.this,offerItems);
                mOfferView.setAdapter(adapter);
                mRecyclerView.setAdapter(adapter);
                adapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(FeedItem item) {
//                        Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_LONG).show();
                        switch (item.getCategory()){
                            case "Inverter":
                                Products.category="Inverter";
                                break;
                            case "Battery":
                                Products.category="Battery";
                                break;
                            case "Panel":
                                Products.category="Panel";
                                break;
                            default:
                                Products.category="Inverter";
                                break;
                        }
                        intent=new Intent(MainActivity.this,Products.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onItemClick(OfferItem item) {
                    }
                });

                offerAdapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(FeedItem item) {

                    }

                    @Override
                    public void onItemClick(OfferItem item) {
                        Toast.makeText(MainActivity.this,item.getOffer_title(),Toast.LENGTH_SHORT).show();
                    }
                });

                myUpdateOperation();



            } else {
                Toast.makeText(MainActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void autoScroll(){
        final int speedScroll =2000;
        handler = new Handler();
        runnable = new Runnable() {
            int count=-1;
            @Override
            public void run() {
                if (count<mOfferView.getAdapter().getItemCount()) {
                    mOfferView.smoothScrollToPosition(++count);
                    handler.postDelayed(this,speedScroll);
                }
                if (count ==mOfferView.getAdapter().getItemCount()){
                    mOfferView.smoothScrollToPosition(--count);
                    handler.postDelayed(this,speedScroll);
                }
            }
        };
    }

    private void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONArray posts = response.optJSONArray("posts");
            feedsList = new ArrayList<>();
            offerItems = new ArrayList<>();

            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);
                FeedItem item = new FeedItem();
                OfferItem offerItem = new OfferItem();
                offerItem.setOffer_title(post.optString("title"));
                offerItem.setOffer_thumbnail(post.optString("thumbnail"));
                item.setTitle(post.optString("title"));
                item.setThumbnail(post.optString("thumbnail"));
                item.setCategory(post.optString("category"));
                feedsList.add(item);
                offerItems.add(offerItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main3, menu);

        /*MenuItem item = menu.findItem(R.id.menu_item_share);

        mShareActionProvider = (ShareActionProvider) item.getActionProvider();*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.menu_refresh :

                mSwipeRefreshLayout.setRefreshing(true);
                initiateRefresh();
                return true;

            case R.id.action_close_session:
                // Clear session and ask for new auth token
                mAccountManager.invalidateAuthToken(AccountUtils.ACCOUNT_TYPE, authToken);
                mAuthPreferences.setAuthToken(null);
                mAuthPreferences.setUsername(null);
                mAccountManager.getAuthTokenByFeatures(AccountUtils.ACCOUNT_TYPE, AccountUtils.AUTH_TOKEN_TYPE, null, this, null, null, new MainActivity.GetAuthTokenCallback(), null);
                return true;
            case R.id.action_settings :
                //noinspection SimplifiableIfStatement
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initiateRefresh() {
        new DownloadTask().execute(url);
    }


    private void myUpdateOperation(){
        mSwipeRefreshLayout.setRefreshing(false);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_ineverter) {
            // Handle the camera action
            Products.category="Inverter";
            Intent intent = new Intent(this,Products.class);
            startActivity(intent);
        } else if (id == R.id.nav_batery) {
            Products.category="Battery";
            Intent intent = new Intent(this,Products.class);
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            //setShareIntent(this.intent);
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            Bundle bundle;

            try {
                bundle = result.getResult();

                final Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (null != intent) {
                    startActivityForResult(intent, REQ_SIGNUP);
                } else {
                    authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    final String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);

                    // Save session username & auth token
                    mAuthPreferences.setAuthToken(authToken);
                    mAuthPreferences.setUsername(accountName);

                    /*text1.setText("Retrieved auth token: " + authToken);
                    text2.setText("Saved account name: " + mAuthPreferences.getAccountName());
                    text3.setText("Saved auth token: " + mAuthPreferences.getAuthToken());
*/
                    // If the logged account didn't exist, we need to create it on the device
                    Account account = AccountUtils.getAccount(MainActivity.this, accountName);
                    if (null == account) {
                        account = new Account(accountName, AccountUtils.ACCOUNT_TYPE);
                        mAccountManager.addAccountExplicitly(account, bundle.getString(io.ubendren.imageprocessing.AccManager.LoginActivity.PARAM_USER_PASSWORD), null);
                        mAccountManager.setAuthToken(account, AccountUtils.AUTH_TOKEN_TYPE, authToken);
                    }
                }
            } catch(OperationCanceledException e) {
                // If signup was cancelled, force activity termination
                finish();
            } catch(Exception e) {
                e.printStackTrace();
            }

        }

    }

    /*mOfferView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        switch (newState){
                            case RecyclerView.SCROLL_STATE_IDLE:
                                float targerBottomPosition1=mOfferView.getX();
                                float targerBottomPosition2=mOfferView.getX()+mOfferView.getWidth();

                                View view1 = mOfferView.findChildViewUnder(targerBottomPosition1,0);
                                View view2 = mOfferView.findChildViewUnder(targerBottomPosition2,0);

                                float x1=targerBottomPosition1;
                                if (view1!=null) {
                                    x1 =view1.getX();
                                }

                                float x2 = targerBottomPosition2;
                                if (view2!=null) {
                                    x2=view2.getX();
                                }

                                float dx1 =Math.abs(mOfferView.getX() -x1);
                                float dx2 =Math.abs(mOfferView.getX() + mOfferView.getWidth() -x2);

                                float visiblePortionOfItem1 =0;
                                float visiblePortionOfItem2 =0;

                                if (x1<0&&view1!=null){
                                    visiblePortionOfItem1=view1.getWidth() -dx1;
                                }

                                if (view2!=null){
                                    visiblePortionOfItem2=view2.getWidth() -dx2;
                                }

                                int position = 0;
                                if (visiblePortionOfItem1 >= visiblePortionOfItem2) {
                                    position = mOfferView.getChildAdapterPosition(mOfferView.findChildViewUnder(targerBottomPosition1,0));
                                } else {
                                    position = mOfferView.getChildAdapterPosition(mOfferView.findChildViewUnder(targerBottomPosition2,0));
                                }
                                mOfferView.scrollToPosition(position);
                                break;

                            case RecyclerView.SCROLL_STATE_DRAGGING:
                                break;
                            case RecyclerView.SCROLL_STATE_SETTLING:
                                break;
                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    }
                });
*/

    public class ScrollingLinearLayoutManager extends LinearLayoutManager {

        private final int duration;

        public ScrollingLinearLayoutManager(Context context, int orientation, boolean reverseLayout, int duration) {
            super(context, orientation, reverseLayout);
            this.duration = duration;
        }

        @Override
        public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
            View firstVisibleChild = recyclerView.getChildAt(0);
            int itemWidth = firstVisibleChild.getWidth();
            int currentPosition = recyclerView.getChildAdapterPosition(firstVisibleChild);
            int distanceInPixel = Math.abs((currentPosition - position) * itemWidth);
            if (distanceInPixel == 0) {
                distanceInPixel = (int) Math.abs(firstVisibleChild.getX());
            }
            SmoothScroller smoothScroller = new SmoothScroller(recyclerView.getContext(), distanceInPixel, duration);
            smoothScroller.setTargetPosition(position);
            startSmoothScroll(smoothScroller);
        }

        private class SmoothScroller extends LinearSmoothScroller {

            private final float distanceInPixels;
            private final float duration;

            public SmoothScroller(Context context, float distanceInPixels, float duration) {
                super(context);
                this.distanceInPixels = distanceInPixels;
                this.duration = duration;
            }

            @Nullable
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return ScrollingLinearLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected int calculateTimeForScrolling(int dx) {
                float proportion = (float) dx /distanceInPixels;
                return (int)(duration*proportion);
            }
        }
    }
}