package com.example.h264player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private H264Player2 h264Player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();
        initSurface();
    }

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);

        }
        return false;
    }

    private void initSurface() {
        SurfaceView surface = findViewById(R.id.preview);
        final SurfaceHolder surfaceHolder = surface.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                h264Player = new H264Player2(MainActivity.this,
                        new File(Environment.getExternalStorageDirectory(), "mame.h264").getAbsolutePath(),
                        surfaceHolder.getSurface());
                h264Player.play();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });

    }
}