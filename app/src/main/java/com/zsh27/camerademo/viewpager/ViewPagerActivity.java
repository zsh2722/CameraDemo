package com.zsh27.camerademo.viewpager;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zsh27.camerademo.R;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private TestViewPagerAdapter mTestViewPagerAdapter;
    private List<String> mList = new ArrayList<>();
    private int mIndex = 0;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
        ViewPager viewPager = findViewById(R.id.view_pager);
        mTextView = findViewById(R.id.text);
        mTestViewPagerAdapter = new TestViewPagerAdapter(this);

        viewPager.setAdapter(mTestViewPagerAdapter);
        viewPager.addOnPageChangeListener(this);

        findViewById(R.id.btn_add)
                .setOnClickListener(this);
        findViewById(R.id.btn_delete)
                .setOnClickListener(this);


        mList.add("https://avatar.csdn.net/F/F/5/3_lmj623565791.jpg");
        mList.add("http://pic22.nipic.com/20120714/9622064_105642209176_2.jpg");
        mList.add("http://www.pptbz.com/pptpic/UploadFiles_6909/201203/2012031220134655.jpg");
        mList.add("http://pic3.nipic.com/20090702/918855_174429094_2.jpg");
        //        mList.add("http://pic3.nipic.com/20090527/1242397_102231006_2.jpg");
        //        mList.add("http://pic.qingting.fm/2018/0421/20180421072635816190.jpg%21800");
        //        mList.add("");

        mTestViewPagerAdapter.setList(mList);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Log.d("ViewPagerActivity", "onPageScrolled:"+position);
    }

    @Override
    public void onPageSelected(int position) {
        Log.d("ViewPagerActivity", "onPageSelected:" + position);

        mIndex = position;
        mTextView.setText((mIndex + 1) + "/" + mTestViewPagerAdapter.getCount());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                mList.add("http://pic3.nipic.com/20090527/1242397_102231006_2.jpg");
                mList.add("http://pic.qingting.fm/2018/0421/20180421072635816190.jpg%21800");
                mList.add("http://pic6.photophoto.cn/20080108/0021033823208441_b.jpg");
                mTestViewPagerAdapter.setList(mList);

                mTextView.setText((mIndex + 1) + "/" + mTestViewPagerAdapter.getCount());
                break;
            case R.id.btn_delete:
                mTestViewPagerAdapter.remove(mIndex);
                mTextView.setText((mIndex + 1) + "/" + mTestViewPagerAdapter.getCount());
                break;
        }
    }
}
