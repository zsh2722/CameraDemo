package com.zsh27.camerademo.camera;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.zsh27.camerademo.PreviewActivity;
import com.zsh27.camerademo.R;
import com.zsh27.camerademo.glide.GlideApp;

public class Camera1Activity extends AppCompatActivity implements View.OnClickListener, CameraHelper.OnCameraHelperStateListener {

    private AutoFitTextureView mTextureView;
    private ImageView mIvTakePhoto;
    private ImageView mIvCover;
    private int mWidthPixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera1);
        initView();
        CameraHelper.getInstance().init(this, mTextureView);
        CameraHelper.getInstance().setListener(this);
    }

    private void initView() {
        mTextureView = findViewById(R.id.texture_view);
        mIvTakePhoto = findViewById(R.id.iv_take_photo);
        mIvCover = findViewById(R.id.iv_cover);
        mIvTakePhoto.setOnClickListener(this);

        mWidthPixels = getResources().getDisplayMetrics().widthPixels;
        Log.d("Camera1Activity", "widthPixels:" + mWidthPixels);

        float width = mWidthPixels;
        Log.d("Camera1Activity", "width:" + width);
        float height = width * 9 / 16;
        Log.d("Camera1Activity", "height:" + height);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mIvCover.getLayoutParams();
        params.width = (int) width;
        params.height = (int) height;
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        int dp15 = dp2px(15);
        Log.d("Camera1Activity", "dp15:" + dp15);
        int dp80 = dp2px(80);
        Log.d("Camera1Activity", "dp80:" + dp80);
        //        params.setMargins(dp15, dp80, dp15, 0);
        mIvCover.setLayoutParams(params);
    }

    private int dp2px(int dpValue) {
        float dimension = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
        return (int) dimension;
    }

    @Override
    protected void onResume() {
        super.onResume();
        CameraHelper.getInstance().startCameraPreview();
    }

    @Override
    protected void onPause() {
        CameraHelper.getInstance().releaseCamera();
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.iv_take_photo:
                CameraHelper.getInstance().takePicture("");
                break;
        }
    }

    @Override
    public void onCameraError(String msg) {

    }

    @Override
    public void onTakePictureSuccess(String fileName) {
        Log.d("Camera1Activity", "fileName:" + fileName);

        Bitmap bitmap = BitmapFactory.decodeFile(fileName);

        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();


        Log.d("Camera1Activity", "bitmap.getWidth():" + bitmapWidth);
        Log.d("Camera1Activity", "bitmap.getHeight():" + bitmapHeight);

        int textureViewWidth = mTextureView.getWidth();
        int textureViewHeight = mTextureView.getHeight();
        Log.d("Camera1Activity", "textureViewWidth:" + textureViewWidth);
        Log.d("Camera1Activity", "textureViewHeight:" + textureViewHeight);
        Matrix matrix = new Matrix();
        float scaleX = (float) textureViewWidth / bitmapWidth;
        Log.d("Camera1Activity", "scaleX:" + scaleX);
        float scaleY = (float)textureViewHeight  /bitmapHeight ;
        Log.d("Camera1Activity", "scaleY:" + scaleY);
        matrix.postScale(scaleX, scaleY);
        Bitmap newBM = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);

        Log.d("Camera1Activity", "newBM.getWidth():" + newBM.getWidth());
        Log.d("Camera1Activity", "newBM.getHeight():" + newBM.getHeight());
        int coverWidth = mIvCover.getWidth();
        Log.d("Camera1Activity", "coverWidth:" + coverWidth);

        int leftPoint = (mWidthPixels - coverWidth) / 2;

        Log.d("Camera1Activity", "leftPoint:" + leftPoint);

        int coverHeight = mIvCover.getHeight();
        Log.d("Camera1Activity", "coverHeight:" + coverHeight);


        //        Bitmap.createBitmap()
        int left = mIvCover.getLeft();
        int top = mIvCover.getTop();
        int right = mIvCover.getRight();
        int bottom = mIvCover.getBottom();

        float scaleLeft = (float) left / textureViewWidth;
        Log.d("Camera1Activity", "scaleLeft:" + scaleLeft);
        float scaleTop = (float) top / textureViewHeight;
        Log.d("Camera1Activity", "scaleTop:" + scaleTop);

        float scaleWidth = (float) coverWidth / textureViewWidth;
        Log.d("Camera1Activity", "scaleWidth:" + scaleWidth);
        float scaleHeight = (float) coverHeight / textureViewHeight;
        Log.d("Camera1Activity", "scaleHeight:" + scaleHeight);

        Log.d("Camera1Activity", "left:" + left);
        Log.d("Camera1Activity", "top:" + top);
        Log.d("Camera1Activity", "right:" + right);
        Log.d("Camera1Activity", "bottom:" + bottom);

//        Bitmap bitmap1 = Bitmap.createBitmap(bitmap, (int) (scaleLeft * bitmapWidth), (int) (scaleTop * bitmapHeight), (int) (scaleWidth * bitmapWidth), (int) (scaleHeight * bitmapHeight));

        Bitmap bitmap1 = Bitmap.createBitmap(newBM, left, top, coverWidth, coverHeight);
        int bitmap1Width = bitmap1.getWidth();
        int bitmap1Height = bitmap1.getHeight();
        Log.d("Camera1Activity", "bitmap1Width:" + bitmap1Width);
        Log.d("Camera1Activity", "bitmap1Height:" + bitmap1Height);

        showImage(bitmap1);
        //        View view = LayoutInflater.from(this)
        //                .inflate(R.layout.dialog_image_item_layout, null);
        //        ImageView imageView = view.findViewById(R.id.iv_image);
        //        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //        builder.setView(view);
        //        builder.setCancelable(true);
        //        AlertDialog alertDialog = builder.create();
        //        alertDialog.show();
        //
        //
        //        GlideApp.with(this)
        //                .load(fileName)
        //                .into(imageView);

        //        showImage(fileName);
        //        Intent intent = new Intent(this, PreviewActivity.class);
        //        intent.putExtra("path", fileName);
        //        startActivity(intent);
    }

    private void showImage(Bitmap bitmap) {
        Dialog dialog = new Dialog(Camera1Activity.this);
        ImageView imageView = new ImageView(Camera1Activity.this);

        dialog.setContentView(imageView);

        imageView.setImageBitmap(bitmap);


        dialog.show();
    }

    private void showImage(String fileName) {
        Dialog dialog = new Dialog(Camera1Activity.this);
        ImageView imageView = new ImageView(Camera1Activity.this);

        dialog.setContentView(imageView);

        GlideApp.with(this)
                .load(fileName)
                .into(imageView);

        dialog.show();
    }

    @Override
    public void onTakePictureFail(String msg) {

    }


}
