package com.deezer.sdk.sample.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class FFTView extends View {
    
    private byte[] mFFTData;
    
    private final Paint mPaint = new Paint();
    private final RectF mRect = new RectF();
    
    public FFTView(final Context context) {
        super(context);
        init();
    }
    
    public FFTView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    
    public FFTView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void init() {
        mPaint.setColor(Color.BLACK);
    }
    
    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        
        // check edit mode
        if (isInEditMode()) {
            return;
        }
        
        // safe check
        if (mFFTData == null) {
            return;
        }
        
        int length = mFFTData.length / 2;
        int width = canvas.getWidth() / length;
        int height = canvas.getHeight();
        
        int pos;
        
        for (int i = 0; i < length; ++i) {
            pos = i * width;
            
            float rFk = ((float) mFFTData[2 * i]) / Byte.MAX_VALUE;
            float iFk = ((float) mFFTData[(2 * i) + 1]) / Byte.MAX_VALUE;
            
            float magnitude = (float) Math.sqrt((rFk * rFk) + (iFk * iFk));
            
            
            mRect.set(pos, height * (1 - magnitude), pos + width, height);
            
            canvas.drawRect(mRect, mPaint);
        }
    }
    
    public void setFFTData(final byte[] fFTData) {
        mFFTData = fFTData;
        invalidate();
    }
}
