package com.zsh27.camerademo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.zsh27.camerademo.camera.Camera1Activity;
import com.zsh27.camerademo.camera.CameraActivity;
import com.zsh27.camerademo.camera.DiyCameraActivity;
import com.zsh27.camerademo.camera2.Camera2Activity;
import com.zsh27.camerademo.viewpager.ViewPagerActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_camera)
                .setOnClickListener(this);
        findViewById(R.id.btn_camera1)
                .setOnClickListener(this);
        findViewById(R.id.btn_camera2)
                .setOnClickListener(this);
        findViewById(R.id.btn_camera_demo)
                .setOnClickListener(this);
//        startActivity(new Intent(this, ViewPagerActivity.class));
    }

    @Override
    public void onClick(View v) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请摄像头权限
            Toast.makeText(this, "申请权限", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        } else {
            switch (v.getId()) {
                case R.id.btn_camera:
                    startActivity(new Intent(this, DiyCameraActivity.class));
                    break;
                case R.id.btn_camera1:
                    startActivity(new Intent(this, Camera1Activity.class));
                    break;
                case R.id.btn_camera2:
                    startActivity(new Intent(this, Camera2Activity.class));
                    break;
                case R.id.btn_camera_demo:
                    startActivity(new Intent(this, CameraActivity.class));
                    break;
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已申请", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, DiyCameraActivity.class));
            } else {
                Toast.makeText(this, "权限已拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
