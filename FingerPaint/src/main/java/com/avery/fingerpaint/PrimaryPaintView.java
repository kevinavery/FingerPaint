package com.avery.fingerpaint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * The view for holding one of the primary colors.
 */
public class PrimaryPaintView extends View
{
    public interface OnColorChangeListener
    {
        public void OnColorChange(float v);
    }

    private int color;
    private OnColorChangeListener listener;
    private boolean editable;

    private Paint pOutline;
    private Paint pIndicatorBg;
    private Paint pIndicator;
    private Paint pFill;

    private float lastY;
    private float colorPercent;

    public PrimaryPaintView(Context context, int color)
    {
        super(context);
        this.color = color;
        init();
    }

    private void init()
    {
        editable = true;

        pOutline = new Paint();
        pOutline.setAntiAlias(true);
        pOutline.setColor(Color.LTGRAY);

        pIndicatorBg = new Paint();
        pIndicatorBg.setAntiAlias(true);
        pIndicatorBg.setColor(Color.WHITE);

        pIndicator = new Paint();
        pIndicator.setAntiAlias(true);
        pIndicator.setColor(Color.GREEN);

        pFill = new Paint();
        pFill.setColor(color);
        pFill.setAntiAlias(true);
    }

    public void setColorPercent(float percent)
    {
        colorPercent = percent;
        invalidate();
    }

    public void setOnColorChangeListener(OnColorChangeListener l)
    {
        this.listener = l;
    }

    public void setEditable(boolean editable)
    {
        this.editable = editable;

        if (editable)
            this.setAlpha(1f);
        else
            this.setAlpha(0.2f);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (!editable)
        {
            return true;
        }

        float y = event.getY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                this.lastY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                Log.i("PrimaryPaintView", "onTouchEvent:move y = " + y);
                onTouchMove(y);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void onTouchMove(float y)
    {
        float height = this.getHeight();
        float dy = lastY - y;
        float factor = dy / (2 * height);
        colorPercent = clip(colorPercent + factor);
        lastY = y;

        if (listener != null)
            listener.OnColorChange(colorPercent);

        Log.i("PrimaryPaintView", "onTouchMove factor = " + factor + " colorPercent = " + colorPercent);

        invalidate();
    }

    private float clip(float f)
    {
        if (f > 1f) return 1f;
        if (f < 0f) return 0f;
        return f;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        float padding = getWidth() / 10f;
        float radius = (getWidth() / 2f) - padding;

        float centerX = radius + padding;
        float centerY = radius + padding;

        float indicatorAngle = 360 * colorPercent;
        float indicatorRadius = radius + 6;

        RectF r = new RectF();
        r.left = centerX - indicatorRadius;
        r.right = centerX + indicatorRadius;
        r.top = centerY - indicatorRadius;
        r.bottom = centerY + indicatorRadius;

        canvas.drawCircle(centerX, centerY, radius+8, pOutline);
        canvas.drawArc(r, 0, 360, true, pIndicatorBg);
        canvas.drawArc(r, 90f, indicatorAngle, true, pIndicator);
        canvas.drawCircle(centerX, centerY, radius, pFill);
    }
}
