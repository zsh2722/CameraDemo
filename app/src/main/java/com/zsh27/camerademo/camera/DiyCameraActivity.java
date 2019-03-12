package com.zsh27.camerademo.camera;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import com.zsh27.camerademo.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DiyCameraActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener, View.OnClickListener {

    private String mCameraId;
    private Size mPreviewSize;
    private Size mCaptureSize;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private TextureView mTextureView;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureRequest;
    private CameraCaptureSession mCaptureSession;
    private Integer mSensorOrientation;
    private File mFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diy_camera);

        mTextureView = findViewById(R.id.texture_view);

        findViewById(R.id.iv_capture)
                .setOnClickListener(this);


        mTextureView.setSurfaceTextureListener(this);
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    };

    private float mFloat = 10000f;

    @Override
    public void onClick(View v) {
        System.out.println("DiyCameraActivity:onClick");

        mFloat += 3;

        startPreview();
        Log.d("DiyCameraActivity", "mFloat:" + mFloat);
        //        takePicture();
    }

    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(surfaceTexture);
        try {
            mCaptureRequest = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequest.addTarget(surface);
            mCaptureRequest.set(CaptureRequest.LENS_FOCUS_DISTANCE, mFloat);
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    CaptureRequest build = mCaptureRequest.build();
                    mCaptureSession = session;

                    // 自动对焦
//                    mCaptureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                    // 打开闪光灯
//                    mCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    mCaptureRequest.set(CaptureRequest.LENS_FOCUS_DISTANCE, mFloat);
                    try {
                        session.setRepeatingRequest(build, null, mHandler);

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            mCameraManager.openCamera(mCameraId, mStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    // SurefaceTexture可用的时候
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d("DiyCameraActivity", "onSurfaceTextureAvailable");
        Log.d("DiyCameraActivity", "width:" + width);
        Log.d("DiyCameraActivity", "height:" + height);

        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        setupCamera(width, height);
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.d("DiyCameraActivity", "onSurfaceTextureSizeChanged");
        Log.d("DiyCameraActivity", "width:" + width);
        Log.d("DiyCameraActivity", "height:" + height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void takePicture() {
        lockFocus();
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation));
    }

    private void lockFocus() {
        try {
            mCaptureRequest.addTarget(mImageReader.getSurface());

            mCaptureRequest.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            mCaptureRequest.set(CaptureRequest.JPEG_ORIENTATION, rotation);
            mCaptureSession.capture(mCaptureRequest.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.d("DiyCameraActivity", "onCaptureCompleted");
                    if (mFile != null) {
                        String path = mFile.getAbsolutePath();

                        Log.d("DiyCameraActivity", path);
                        Bitmap bitmap = BitmapFactory.decodeFile(path);

                        if (bitmap != null) {
                            showImage(bitmap);
                        }
                    }
                }
            }, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupCamera(int width, int height) {
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            assert mCameraManager != null;
            String[] cameraIdList = mCameraManager.getCameraIdList();
            for (String s : cameraIdList) {
                Log.d("DiyCameraActivity:", "CameraIdList:" + s);


                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(s);
                // 在这个例子中不使用前置摄像头
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                //获取相机支持的最大拍照尺寸
                mCaptureSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
                    @Override
                    public int compare(Size lhs, Size rhs) {
                        return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth());
                    }
                });

                setupImageReader();
                mCameraId = s;


            }


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //选择sizeMap中大于并且最接近width和height的size
    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }

    private void setupImageReader() {
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(), ImageFormat.JPEG, 1);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {

                Log.d("DiyCameraActivity", "onImageAvailable");

                final Image image = reader.acquireNextImage();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ByteBuffer buffer = image.getPlanes()[0]
                                .getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        try {
                            mFile = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
                            FileOutputStream fileOutputStream = new FileOutputStream(mFile);
                            fileOutputStream.write(bytes);
                            fileOutputStream.flush();
                            fileOutputStream.close();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                });
            }
        }, mHandler);
    }

    private void showImage(Bitmap bitmap) {
        Dialog dialog = new Dialog(DiyCameraActivity.this);
        ImageView imageView = new ImageView(DiyCameraActivity.this);

        dialog.setContentView(imageView);

        imageView.setImageBitmap(bitmap);
        dialog.show();
    }
}
