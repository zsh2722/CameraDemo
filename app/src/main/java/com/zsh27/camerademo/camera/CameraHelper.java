package com.zsh27.camerademo.camera;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import com.zsh27.camerademo.camera2.Camera2Helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @auther zsh27
 * @date 2019/3/6
 */
public class CameraHelper implements TextureView.SurfaceTextureListener {

    static final SparseIntArray DISPLAY_ORIENTATIONS = new SparseIntArray();

    static {
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_0, 0);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_90, 90);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_180, 180);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_270, 270);
    }

    private static int CAMERA_BACK = 0;
    private static int CAMERA_FRONT = 0;
    private static final String TAG = CameraHelper.class.getSimpleName();
    private volatile static CameraHelper sCameraHelper;
    private Camera mCamera;
    private Activity mActivity;
    private AutoFitTextureView mFitTextureView;
    private int mFlash = 1;
    private Camera.Parameters mCameraParameters;
    private File mFile;
    private OnCameraHelperStateListener mListener;
    private final Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();

    private int mCameraId;
    private static final int INVALID_CAMERA_ID = -1;

    private CameraHelper() {

    }

    public static CameraHelper getInstance() {
        if (sCameraHelper == null) {
            synchronized (Camera2Helper.class) {
                sCameraHelper = new CameraHelper();
            }
        }
        return sCameraHelper;
    }

    public void init(Activity activity, AutoFitTextureView fitTextureView) {
        mActivity = activity;
        mFitTextureView = fitTextureView;
    }

    public void startCameraPreview() {
        if (mFitTextureView != null) {
            if (mFitTextureView.isAvailable()) {
                chooseCamera();
                openCamera(mFitTextureView.getWidth(), mFitTextureView.getHeight());
                setUpPreview();
            } else {
                mFitTextureView.setSurfaceTextureListener(this);
            }
        }
    }

    private void chooseCamera() {
        for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
            Camera.getCameraInfo(i, mCameraInfo);
            Log.d(TAG, "chooseCamera:mCameraInfo.facing:" + mCameraInfo.facing);
            if (mCameraInfo.facing == CAMERA_BACK) {
                mCameraId = i;
                return;
            }
        }
        mCameraId = INVALID_CAMERA_ID;
    }

    /**
     * 打开摄像头
     *
     * @param width  预览容器的宽度
     * @param height 预览容器的高度
     */
    private void openCamera(int width, int height) {
        if (mCamera != null) {
            releaseCamera();
        }
        mCamera = Camera.open(mCameraId);
        initCameraParameters(width, height);
    }

    /**
     * 设置摄像头预览
     * 开始预览
     */
    private void setUpPreview() {
        try {
            mCamera.setPreviewTexture(mFitTextureView.getSurfaceTexture());
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onCameraError("摄像头预览开启失败");
            }
        }
    }

    /**
     * 设置摄像头参数
     *
     * @param width
     * @param height
     */
    private void initCameraParameters(int width, int height) {
        mCameraParameters = mCamera.getParameters();
        //设置拍照后存储的图片格式
        mCameraParameters.setPictureFormat(PixelFormat.JPEG);
        // 获取支持的预览大小
        List<Camera.Size> previewSizes = mCameraParameters.getSupportedPreviewSizes();
        // 获取支持的图片大小
        List<Camera.Size> pictureSizes = mCameraParameters.getSupportedPictureSizes();
        // 获取最大支持的图片大小
        Camera.Size maxPictureSize = Collections.max(pictureSizes, new CompareSizesByArea());
        Log.d(TAG, "maxPictureSize.width:" + maxPictureSize.width);
        Log.d(TAG, "maxPictureSize.height:" + maxPictureSize.height);

        Point displaySize = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        int maxPreviewWidth = displaySize.x;
        Log.d("CameraHelper", "maxPreviewWidth:" + maxPreviewWidth);
        int maxPreviewHeight = displaySize.y;
        Log.d("CameraHelper", "maxPreviewHeight:" + maxPreviewHeight);
        // 获取适应当前屏幕的预览大小
        Camera.Size previewSize = chooseOptimalSize(previewSizes, width, height, maxPreviewWidth, maxPreviewHeight, maxPictureSize);

        // 设置预览大小
        mCameraParameters.setPreviewSize(previewSize.width, previewSize.height);
        // 设置图片大小
        mCameraParameters.setPictureSize(maxPictureSize.width, maxPictureSize.height);
        // 获取当前屏幕方向
        int orientation = mActivity.getResources().getConfiguration().orientation;
        Log.d(TAG, "orientation:" + orientation);
        // 设置预览框TextureView的大小
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mFitTextureView.setAspectRatio(
                    previewSize.width, previewSize.height);
        } else {
            mFitTextureView.setAspectRatio(previewSize.height, previewSize.width);
        }
        int displayRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        Log.d(TAG, "displayRotation:" + displayRotation);

        mCameraParameters.setRotation(calcCameraRotation(displayRotation));

        mCamera.setDisplayOrientation(calcDisplayOrientation(displayRotation));

        // 开启自动对焦
        setAutoFocus();
        // 设置闪光灯模式
        setFlash(mFlash);
        // 设置摄像头参数
        mCamera.setParameters(mCameraParameters);
    }

    /**
     * 设置自动对焦
     *
     * @return boolean
     */
    private boolean setAutoFocus() {
        if (isCameraOpened()) {
            final List<String> modes = mCameraParameters.getSupportedFocusModes();
            if (modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            } else {
                mCameraParameters.setFocusMode(modes.get(0));
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置闪光灯模式
     *
     * @param flash 1 关闭 2 自动 3 开启
     * @return 下一个模式
     */
    public int setFlash(int flash) {
        switch (flash) {
            case 1:
                mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mFlash = 2;
                break;
            case 2:
                mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                mFlash = 3;
                break;
            case 3:
                mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                mFlash = 1;
                break;
            default:
                mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mFlash = 1;
                break;
        }
        return mFlash;
    }

    /**
     * 计算显示方向
     * 确定预览的方向
     *
     * @param screenOrientationDegrees 屏幕方向度
     * @return 旋转预览所需的度数
     */
    private int calcDisplayOrientation(int screenOrientationDegrees) {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 - (mCameraInfo.orientation + screenOrientationDegrees) % 360) % 360;
        } else {  // back-facing
            return (mCameraInfo.orientation - screenOrientationDegrees + 360) % 360;
        }
    }

    /**
     * 计算摄像机旋转
     *
     * @param screenOrientationDegrees 屏幕方向度
     * @return 旋转图像的度数
     */
    private int calcCameraRotation(int screenOrientationDegrees) {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (mCameraInfo.orientation + screenOrientationDegrees) % 360;
        } else {  // back-facing
            final int landscapeFlip = isLandscape(screenOrientationDegrees) ? 180 : 0;
            return (mCameraInfo.orientation + screenOrientationDegrees + landscapeFlip) % 360;
        }
    }

    private boolean isLandscape(int orientationDegrees) {
        return (orientationDegrees == 90 ||
                orientationDegrees == 270);
    }

    /**
     * 摄像头是否已经打开
     *
     * @return boolean
     */
    private boolean isCameraOpened() {
        return mCamera != null;
    }

    private boolean getAutoFocus() {
        if (mCameraParameters != null) {
            String focusMode = mCameraParameters.getFocusMode();
            return focusMode != null && focusMode.contains("continuous");
        } else {
            return false;
        }
    }

    public void takePicture(String localPath) {
        if (TextUtils.isEmpty(localPath)) {
            mFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        } else {
            mFile = new File(localPath);
        }
        if (isCameraOpened()) {
            if (getAutoFocus()) {
                mCamera.cancelAutoFocus();
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        takePicture();
                    }
                });
            } else {
                takePicture();
            }
        } else {
            if (mListener != null) {
                mListener.onCameraError("摄像头未开启");
            }
        }
    }

    /**
     * 拍摄照片
     */
    private void takePicture() {
        mCamera.takePicture(null, null, mRectJpegPictureCallback);
    }

    public void setListener(OnCameraHelperStateListener listener) {
        mListener = listener;
    }

    private Camera.PictureCallback mRectJpegPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.cancelAutoFocus();
            camera.startPreview();

            if (data != null) {
                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, arrayOutputStream);

                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(mFile);
                    outputStream.write(arrayOutputStream.toByteArray());
                    outputStream.flush();
                    outputStream.close();
                    if (mListener != null) {
                        mListener.onTakePictureSuccess(mFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (mListener != null) {
                        mListener.onTakePictureFail("拍照失败");
                    }
                }
            } else {
                if (mListener != null) {
                    mListener.onTakePictureFail("拍照失败");
                }
            }
        }
    };


    /**
     * 释放摄像头资源
     */
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 停止预览,并释放摄像头资源
     */
    public void stopCameraPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        releaseCamera();
    }

    private Camera.Size chooseOptimalSize(List<Camera.Size> choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Camera.Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Camera.Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Camera.Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.width;
        int h = aspectRatio.height;
        for (Camera.Size option : choices) {
            if (option.width <= maxWidth && option.height <= maxHeight &&
                    option.height == option.width * h / w) {
                if (option.width >= textureViewWidth &&
                        option.height >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices.get(0);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable:width:" + width + ":height:" + height);
        //3.在TextureView可用的时候尝试打开摄像头
        chooseCamera();
        openCamera(width, height);
        setUpPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureSizeChanged:width:" + width + ":height:" + height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed");
        stopCameraPreview();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Camera.Size> {

        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.width * lhs.height -
                    (long) rhs.width * rhs.height);
        }
    }


    public interface OnCameraHelperStateListener {
        void onCameraError(String msg);

        void onTakePictureSuccess(String fileName);

        void onTakePictureFail(String msg);
    }
}
