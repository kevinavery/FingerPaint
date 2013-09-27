package com.avery.fingerpaint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * The view that shows the current color and is used to open and close the palette.
 */
public class SelectorView extends View
{
    private int color;

    private Paint pOutline2;
    private Paint pOutline1;
    private Paint pFill;

    public SelectorView(Context context)
    {
        super(context);
        init();
    }

    private void init()
    {
        pOutline2 = new Paint();
        pOutline2.setAntiAlias(true);
        pOutline2.setColor(Color.LTGRAY);

        pOutline1 = new Paint();
        pOutline1.setAntiAlias(true);
        pOutline1.setColor(Color.WHITE);

        pFill = new Paint();
        //pFill.setColor(color);
        pFill.setAntiAlias(true);
    }

    public void setColor(int c)
    {
        this.color = c;
        pFill.setColor(color);
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        float padding = getWidth() / 10f;
        float radius = (getWidth() / 2f) - padding;

        float centerX = radius + padding;
        float centerY = radius + padding;

        RectF r = new RectF();
        r.left = centerX - radius;
        r.right = centerX + radius;
        r.top = centerY - radius;
        r.bottom = centerY + radius;

        canvas.drawCircle(centerX, centerY, radius+8, pOutline2);
        canvas.drawCircle(centerX, centerY, radius+6, pOutline1);
        canvas.drawCircle(centerX, centerY, radius, pFill);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle b = new Bundle();
        b.putParcelable("super", super.onSaveInstanceState());
        b.putInt("color", color);
        return b;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle b = (Bundle) state;
        super.onRestoreInstanceState(b.getParcelable("super"));
        setColor(b.getInt("color"));
    }
}
