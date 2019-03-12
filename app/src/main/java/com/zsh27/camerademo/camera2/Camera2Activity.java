package com.zsh27.camerademo.camera2;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zsh27.camerademo.MainActivity;
import com.zsh27.camerademo.R;
import com.zsh27.camerademo.adapter.SampleImageAdapter;
import com.zsh27.camerademo.camera.AutoFitTextureView;

import java.io.File;

public class Camera2Activity extends AppCompatActivity implements View.OnClickListener, Camera2Helper.AfterDoListener {

    private SampleImageAdapter mSampleImageAdapter;
    private ImageButton mIbTips;
    private ImageButton mIbFlash;
    private AutoFitTextureView mTextureView;
    /**
     * 侧前方
     */
    private TextView mTvTopTip;
    /**
     * 请您在保证安全下拍摄
     */
    private TextView mTvBottomTip;
    private LinearLayout mLlTipRoot;
    private RecyclerView mRvPhotos;
    private ImageView mIvClose;
    private ImageView mIvTakePhoto;
    private ImageView mIvConfirm;
    public static final String PHOTO_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String PHOTO_NAME = "camera2";
    private File mFile;
    private Camera2Helper mCamera2Helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        initView();
    }

    private void initView() {
        mIbTips = findViewById(R.id.ib_tips);
        mIbTips.setOnClickListener(this);
        mIbFlash = findViewById(R.id.ib_flash);
        mIbFlash.setOnClickListener(this);
        mTextureView = findViewById(R.id.texture_view);
        mTvTopTip = findViewById(R.id.tv_top_tip);
        mTvBottomTip = findViewById(R.id.tv_bottom_tip);
        mLlTipRoot = findViewById(R.id.ll_tip_root);
        mRvPhotos = findViewById(R.id.rv_photos);
        mIvClose = findViewById(R.id.iv_close);
        mIvClose.setOnClickListener(this);
        mIvTakePhoto = findViewById(R.id.iv_take_photo);
        mIvTakePhoto.setOnClickListener(this);
        mIvConfirm = findViewById(R.id.iv_confirm);
        mIvConfirm.setOnClickListener(this);
        mSampleImageAdapter = new SampleImageAdapter(this);
        mRvPhotos.setAdapter(mSampleImageAdapter);
        mCamera2Helper = Camera2Helper.getInstance(Camera2Activity.this, mTextureView, mFile);
        mCamera2Helper.setAfterDoListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mCamera2Helper.startCameraPreView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera2Helper.onDestroyHelper();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.ib_tips:
                break;
            case R.id.ib_flash:
                break;
            case R.id.iv_close:
                finish();
                break;
            case R.id.iv_take_photo:
                mFile = new File(PHOTO_PATH + "/"+System.currentTimeMillis() + ".jpg");
                mCamera2Helper.takePicture(mFile.getAbsolutePath());

                break;
            case R.id.iv_confirm:
                break;
        }
    }

    @Override
    public void onAfterPreviewBack() {

    }

    @Override
    public void onAfterTakePicture(final String path) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSampleImageAdapter.addImageItem(path);

            }
        });

    }
}
