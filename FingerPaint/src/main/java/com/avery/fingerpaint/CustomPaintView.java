package com.avery.fingerpaint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/**
 * The view holding custom colors chosen by the user.
 */
public class CustomPaintView extends View
{
    private boolean selected;
    private int color;
    private Paint pOutline2;
    private Paint pOutline1;
    private Paint pFill;

    public CustomPaintView(Context context)
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

    public int getColor()
    {
        return color;
    }

    public void setSelected(boolean s)
    {
        this.selected = s;
        invalidate();
    }

    public boolean isSelected()
    {
        return selected;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        int highlightColor = selected ? Color.GREEN : Color.WHITE;
        pOutline1.setColor(highlightColor);

        float padding = 8;
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
}
