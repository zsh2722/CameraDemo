package com.zsh27.camerademo.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zsh27.camerademo.PreviewActivity;
import com.zsh27.camerademo.R;
import com.zsh27.camerademo.glide.GlideApp;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther zsh27
 * @date 2019/2/20
 */
public class SampleImageAdapter extends RecyclerView.Adapter<SampleImageAdapter.SampleImageViewHolder> {

    private Context mContext;
    private List<String> mImageList;
    private final LayoutInflater mLayoutInflater;


    public SampleImageAdapter(Context context) {
        this.mContext = context;
        mImageList = new ArrayList<>();
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void setImageList(List<String> imageList) {
        this.mImageList = imageList;
        notifyDataSetChanged();
    }

    public void addImageItem(String image) {
        if (mImageList == null) {
            mImageList = new ArrayList<>();
        }
        mImageList.add(image);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SampleImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.recycler_image_item_layout, parent, false);
        return new SampleImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SampleImageViewHolder holder, final int position) {
        GlideApp.with(mContext)
                .load(mImageList.get(position))
                .placeholder(R.mipmap.ic_picture_place_3)
                .error(R.mipmap.ic_picture_place_3)
                .thumbnail(0.5f)
                .into(holder.mIvImage);
        holder.mIvImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PreviewActivity.class);
                intent.putExtra("path", mImageList.get(position));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImageList.size();
    }

    class SampleImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIvImage;

        SampleImageViewHolder(View itemView) {
            super(itemView);
            mIvImage = itemView.findViewById(R.id.iv_image);

        }
    }
}
