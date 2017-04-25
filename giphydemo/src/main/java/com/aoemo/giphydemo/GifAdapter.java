package com.aoemo.giphydemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by liyiwei
 * on 2017/4/13.
 */

public class GifAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<GifBean> mGifBeanList;

    public GifAdapter(Context context, List<GifBean> gifBeanList) {
        this.mContext = context;
        this.mGifBeanList = gifBeanList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        return new GifViewHolder(inflate, mGifBeanList);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GifViewHolder) {
            ((GifViewHolder) holder).onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof GifViewHolder) {
            ((GifViewHolder) holder).onViewRecycled(holder);
        }
    }

    @Override
    public int getItemCount() {
        return mGifBeanList.size();
    }

}