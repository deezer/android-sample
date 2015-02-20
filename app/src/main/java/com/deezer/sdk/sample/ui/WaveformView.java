package com.deezer.sdk.sample.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class WaveformView extends View {

    private byte[] mWFData;

    private final Paint mPaint = new Paint();
    private final Path mPath = new Path();

    FileOutputStream mOutputStream;

    public WaveformView(final Context context) {
        super(context);
        init();
    }

    public WaveformView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public WaveformView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {

        File file = new File("/sdcard/output.waveform");
        Log.i("Output", file.getAbsolutePath());
        try {
            mOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mOutputStream = null;
        }

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeJoin(Join.ROUND);
        mPaint.setStrokeCap(Cap.ROUND);
        mPaint.setStrokeWidth(5.0f);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        // check edit mode
        if (isInEditMode()) {
            return;
        }

        // safe check
        if (mWFData == null) {
            return;
        }

        int length = mWFData.length;
        float width = ((float) canvas.getWidth()) / length;
        float halfHeight = canvas.getHeight() / 2.0f;

        mPath.reset();


        float x = 0;
        float y = halfHeight;

        for (int i = 0; i < length; ++i) {
            x += width;

            // convert byte to unsigned bytes 
            int unsigned = (mWFData[i] & 0xFF) - 128;

            y = (unsigned / 128.0f);
            y = (y * halfHeight) + halfHeight;

            if (i == 0) {
                mPath.moveTo(x, y);
            } else {
                mPath.lineTo(x, y);
            }
        }

        canvas.drawPath(mPath, mPaint);

    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mOutputStream != null) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setWFData(final byte[] wFData) {

        if (mOutputStream != null) {
            try {
                mOutputStream.write(wFData);
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mWFData = wFData;
        invalidate();

    }
}
