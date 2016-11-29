package com.jiepier.floatmusic.base;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panruijiesx on 2016/11/14.
 */

public abstract class BaseAdapter<T , K extends BaseViewHolder> extends RecyclerView.Adapter<K> {

    private List<T> mData;
    private int mLayoutResId;

    public BaseAdapter(int layoutResId, List<T> data) {
        this.mData = data == null ? new ArrayList<T>() : data;
        if (layoutResId != 0) {
            this.mLayoutResId = layoutResId;
        }
    }

    public BaseAdapter(List<T> data) {
        this(0, data);
    }


    @Override
    public K onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(mLayoutResId,parent,false);
        return (K) new BaseViewHolder(item);
    }

    @Override
    public void onBindViewHolder(K holder, int position) {
        convert(holder,mData.get(position));
    }

    protected abstract void convert(BaseViewHolder holder,T item);

    @Override
    public int getItemCount() {
        return mData == null? 0:mData.size();
    }

    public List<T> getData() {
        return mData;
    }

    public T getItem(int position) {
        return mData.get(position);
    }

    public void remove(int position) {
        mData.remove(position);
        notifyItemRemoved(position);

    }

    public void add(int position, T item) {
        mData.add(position, item);
        notifyItemInserted(position);
    }

}
