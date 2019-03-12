package com.zsh27.camerademo.viewpager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zsh27.camerademo.R;

import java.util.List;

/**
 * @auther zsh27
 * @date 2019/3/4
 */
public class TestViewPagerAdapter extends PagerAdapter {
    private Context mContext;
    private List<String> mList;
    private final LayoutInflater mInflater;

    public TestViewPagerAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setList(List<String> list) {
        mList = list;
        Log.d("TestViewPagerAdapter", "mList.size():" + mList.size());
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (mList != null) {
            Log.d("TestViewPagerAdapter", "mList.size():" + mList.size());
            for (int i = 0; i < mList.size(); i++) {
                if (i == position) {
                    mList.remove(i);
                    break;
                }
            }
            Log.d("TestViewPagerAdapter", "mList.size():" + mList.size());
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = mInflater.inflate(R.layout.view_pager_item_layout, container, false);
        ImageView imageView = view.findViewById(R.id.iv_image);
        Glide.with(mContext)
                .load(mList.get(position))
                .into(imageView);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return PagerAdapter.POSITION_NONE;
    }
}
