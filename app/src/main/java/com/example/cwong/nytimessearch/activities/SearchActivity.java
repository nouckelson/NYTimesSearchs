package com.example.cwong.nytimessearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.cwong.nytimessearch.ArticleArrayAdapter;
import com.example.cwong.nytimessearch.EndlessRecyclerViewScrollListener;
import com.example.cwong.nytimessearch.ItemClickSupport;
import com.example.cwong.nytimessearch.R;
import com.example.cwong.nytimessearch.fragments.SettingsFragment;
import com.example.cwong.nytimessearch.models.Article;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity implements SettingsFragment.SettingsDialogListener {
    @BindView(R.id.rvArticles) RecyclerView rvArticles;
    @BindView(R.id.toolbar) Toolbar toolbar;

    String url = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    int queryPage = 0;

    AsyncHttpClient client;

    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;

    String queryTerm;
    String dateString;
    String sortOrder;
    ArrayList<String> newsArrayValues;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        dateString = "";
        sortOrder = "";
        queryTerm = "";
        newsArrayValues = new ArrayList<>();
        setupViews();

    }

    public void setupViews() {
        articles = new ArrayList<>();
        adapter = new ArticleArrayAdapter(this, articles);
        rvArticles.setAdapter(adapter);
        client = new AsyncHttpClient();
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        gridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        rvArticles.setLayoutManager(gridLayoutManager);

        ItemClickSupport.addTo(rvArticles).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Toast.makeText(SearchActivity.this, "Recherche en cours....", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                        Article article = articles.get(position);
                        i.putExtra("article", Parcels.wrap(article));
                        startActivity(i);
                    }
                }
        );
        rvArticles.addOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (page >= 5) {
                    Toast.makeText(getApplicationContext(), "Reached max articles", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    queryPage = page;
                    articleSearch();
                }
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                queryTerm = query;
                int curSize = adapter.getItemCount();
                articles.clear();
                adapter.notifyItemRangeRemoved(curSize, curSize);
                queryPage = 0;
                articleSearch();
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            FragmentManager fm = getSupportFragmentManager();
            SettingsFragment settingsFragment = SettingsFragment.newInstance(dateString, sortOrder, newsArrayValues);
            settingsFragment.show(fm, "fragment_settings");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void articleSearch() {
        if (!isOnline() || !isNetworkAvailable()) {
            Toast.makeText(this, "Please check internet connection", Toast.LENGTH_LONG).show();
            return;
        }

        RequestParams params = new RequestParams();
        params.put("api-key", "3f79c7cb66c6444cb36e94af7698de79");
        params.put("page", queryPage);
        params.put("q", queryTerm);

        if (dateString.length() > 0) {
            params.put("beginDate", formatDateQuery(dateString));
        }
        if (sortOrder.length() > 0) {
            params.put("sort", sortOrder.toLowerCase());
        }
        if (newsArrayValues.size() > 0) {
            String newsDeskQuery = TextUtils.join(" ", newsArrayValues);
            params.put("fq", "news_desk:(" + newsDeskQuery + ")");
        }
        final int curSize = adapter.getItemCount();
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    articles.addAll(Article.fromJSONArray(articleJsonResults));
                    adapter.notifyItemRangeChanged(curSize, articleJsonResults.length());
                    // rvArticles.scrollToPosition(adapter.getItemCount() - 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }
    public void onFinishSettingsDialog(String date, String sortOrderString, ArrayList<String> newsDeskValues) {
        dateString = date;
        sortOrder = sortOrderString;
        newsArrayValues = newsDeskValues;
        queryPage = 0;
        int curSize = adapter.getItemCount();
        articles.clear();
        adapter.notifyItemRangeRemoved(curSize, curSize);
        articleSearch();
    }

    public String formatDateQuery(String dateString) {
        String format = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        final Calendar c = Calendar.getInstance();

        try {
            c.setTime(sdf.parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return "" + year + month + day;
    }
    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
