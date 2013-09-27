package com.avery.fingerpaint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

/**
 * The view for entering and leaving "mix mode."
 */
public class MixButtonView extends View
{
    private boolean active;
    private Paint pOutline;
    private Paint pText;
    private Paint pFill;

    public MixButtonView(Context context)
    {
        super(context);
        init();
    }

    private void init()
    {
        pOutline = new Paint();
        pOutline.setAntiAlias(true);
        pOutline.setColor(Color.LTGRAY);

        pText = new Paint();
        pText.setColor(Color.DKGRAY);
        pText.setStyle(Paint.Style.FILL);
        pText.setTextSize(26);

        pFill = new Paint();
        pFill.setColor(Color.WHITE);
        pFill.setAntiAlias(true);
    }

    public void setActive(boolean a)
    {
        this.active = a;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        int highlightColor = active ? Color.RED : Color.WHITE;
        pFill.setColor(highlightColor);

        float padding = 3;
        float radius = (getWidth() / 2f) - padding;

        float centerX = radius + padding;
        float centerY = radius + padding;

        RectF r = new RectF();
        r.left = centerX - radius;
        r.right = centerX + radius;
        r.top = centerY - radius;
        r.bottom = centerY + radius;

        canvas.drawCircle(centerX, centerY, radius+2, pOutline);
        canvas.drawCircle(centerX, centerY, radius, pFill);
        // Note: Drawing this text is a real hack - since we don't know the exact size
        // it will draw, we can't center it perfectly. Oh well. Use LabelView?
        canvas.drawText("Mix", centerX - 20, centerY + 10, pText);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle b = new Bundle();
        b.putParcelable("super", super.onSaveInstanceState());
        b.putBoolean("active", active);
        return b;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle b = (Bundle) state;
        super.onRestoreInstanceState(b.getParcelable("super"));
        setActive(b.getBoolean("active"));
        invalidate();
    }
}
