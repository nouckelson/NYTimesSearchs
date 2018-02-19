package com.example.cwong.nytimessearch;

import android.widget.AbsListView;

/**
 * Created by carolinewong on 8/9/16.
 */
public abstract class EndlessScrollListener implements AbsListView.OnScrollListener {
    // min items to have below current scroll position before loading more
    private int visibleThreshold = 5;
    // current offset index of data loaded
    private int currentPage = 0;
    // total num items in data set after last load
    private int previousTotalItemCount = 0;
    // true if waiting for last set of data to load
    private boolean loading = true;
    // start page index
    private int startingPageIndex = 0;

    public EndlessScrollListener() {
    }

    public EndlessScrollListener(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }

    public EndlessScrollListener(int visibleThreshold, int startPage) {
        this.visibleThreshold = visibleThreshold;
        this.startingPageIndex = startPage;
        this.currentPage = startPage;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        // if totalItemCount is 0 and the previous count isn't, assume invalidated
        // list and reset to initial state
        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) { this.loading = true; }
        }
        // if loading, check if dataset count has changed.
        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
            currentPage++;
        }
        // if not loading, check if we need to load more data by checking if we have passed
        // the visible threshold. if yes, execute onLoadMore to fetch data
        if (!loading && (firstVisibleItem + visibleItemCount + visibleThreshold) >= totalItemCount ) {
            loading = onLoadMore(currentPage + 1, totalItemCount);
        }
    }
    // returns true if more data being loaded; false if no more data to load
    public abstract boolean onLoadMore(int page, int totalItemsCount);

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Don't take any action on changed
    }
}
