package com.example.cwong.nytimessearch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.cwong.nytimessearch.models.Article;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cwong on 8/8/16.
 */
public class ArticleArrayAdapter extends RecyclerView.Adapter<ArticleArrayAdapter.ViewHolder> {
    DynamicHeightImageView articleImageView;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivImage) DynamicHeightImageView imageView;
        @BindView(R.id.tvTitle) TextView titleView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    private List<Article> articles;
    private Context context;

    public ArticleArrayAdapter(Context c, List<Article> art) {
        context = c;
        articles = art;
    }
    private Context getContext() {
        return context;
    }

    @Override
    public ArticleArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View articleView = inflater.inflate(R.layout.item_articlegrid, parent, false);
        ViewHolder viewHolder = new ViewHolder(articleView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ArticleArrayAdapter.ViewHolder viewHolder, int position) {
        articleImageView = viewHolder.imageView;
        Article article = articles.get(position);
        if (article.getThumbnail().length() > 0) {
            Glide.with(getContext()).load(article.getThumbnail()).centerCrop().into(articleImageView);
        }
        TextView articleTitleView = viewHolder.titleView;
        if (article.getHeadline().length() > 0) {
            articleTitleView.setText(article.getHeadline());
        }
    }
    @Override
    public int getItemCount() {
        return articles.size();
    }

}
