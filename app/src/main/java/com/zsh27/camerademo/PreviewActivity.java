package com.zsh27.camerademo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.zsh27.camerademo.glide.GlideApp;

public class PreviewActivity extends AppCompatActivity {

    private ImageView mIvImage;

    private String mPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        mPath = getIntent().getStringExtra("path");
        initView();

    }

    private void initView() {
        mIvImage = (ImageView) findViewById(R.id.iv_image);
        GlideApp.with(this)
                .load(mPath)
                .into(mIvImage);
    }
}
