package com.example.h264player;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by Zach on 2021/5/27 18:20
 */
public class H264Player implements Runnable {
    private static final String TAG = "H264Player";

    private Context context;

    private String path;
    private MediaCodec mediaCodec;
    private Surface surface;

    public H264Player(Context context, String path, Surface surface) {

        this.surface = surface;
        this.path = path;
        this.context = context;

        try {
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", 1280, 720);
            mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            mediaCodec.configure(mediaformat, surface, null, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        mediaCodec.start();
        new Thread(this).start();
    }


    @Override
    public void run() {
        byte[] bytes = null;
        try {
            bytes = getBytes(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 得到所有的缓存队列
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        int startIndex = 0;


        int totalSize = bytes.length;
        while (true) {
            if (totalSize == 0 || startIndex >= totalSize) {
                break;
            }
            // startIndex + 2 用于跳过sps/pps, 因为无法解析出图片
            int nextFrameStart = findByFrame(bytes, startIndex + 2, totalSize);

            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            // 查询哪一个bytebuffer能用
            int inIndex = mediaCodec.dequeueInputBuffer(10000);
            if (inIndex >= 0) {
                ByteBuffer byteBuffer = inputBuffers[inIndex];
                byteBuffer.clear();
                byteBuffer.put(bytes, startIndex, nextFrameStart - startIndex);

                mediaCodec.queueInputBuffer(inIndex, 0, nextFrameStart - startIndex, 0, 0);
                startIndex = nextFrameStart;
            } else {
                continue;
            }

            int outIndex = mediaCodec.dequeueOutputBuffer(info, 10000);
            if (outIndex >= 0) {
                try {
                    // 实际一帧耗时=解码时间 + 渲染时间 + 延迟时间。 这里只模拟了延迟时间33ms
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 第二个参数true, 表示渲染到surface
                mediaCodec.releaseOutputBuffer(outIndex, true);
            } else {
            }

        }
    }

    private int findByFrame(byte[] bytes, int start, int totalSize) {
        for (int i = start; i < totalSize - 4; i++) {
            if (bytes[i] == 0x00 && bytes[i + 1] == 0x00 && bytes[i + 2] == 0x00 && bytes[i + 3] == 0x01) {
                return i;
            }
        }
        return -1;
    }

    private byte[] getBytes(String path) throws IOException {
        InputStream is = new DataInputStream(new FileInputStream(new File(path)));
        int len;
        int size = 1024;
        byte[] buf;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        buf = new byte[size];
        while ((len = is.read(buf, 0, size)) != -1) {
            bos.write(buf, 0, len);
        }
        buf = bos.toByteArray();
        return buf;
    }
}
