package com.avery.fingerpaint;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * The palette view, which controls displaying the Selector, the Mix Button,
 * the three primary colors, and the custom paints.
 */
public class PaletteView extends ViewGroup
{
    /**
     * Interface definition for a callback to be invoked when the selected color changes.
     */
    public interface OnSelectedColorChangeListener
    {
        public void onSelectedColorChange(int newColor);
    }

    private boolean paletteVisible;
    private float axisX;
    private float axisY;
    private float rotationAngle;
    private float offsetY;
    private float translateY;
    private RectF rectangle; // reusable rectangle

    private PrimaryPaintView cyanView;
    private PrimaryPaintView magentaView;
    private PrimaryPaintView yellowView;
    private SelectorView selectorView;
    private MixButtonView mixButtonView;

    private boolean inMixMode;
    private OnSelectedColorChangeListener listener;
    private List<CustomPaintView> customPaintViews;
    private List<CustomPaintView> selectedCustomPaintViews;
    private CustomPaintView currentCustomPaintView;

    private static final int MIN_CUSTOM_COLORS = 2;
    private static final int MAX_CUSTOM_COLORS = 11;


    public PaletteView(Context context)
    {
        super(context);
        //this.setBackgroundColor(Color.BLUE);
        paletteVisible = true;
        customPaintViews = new ArrayList<CustomPaintView>();
        selectedCustomPaintViews = new ArrayList<CustomPaintView>();
        inMixMode = false;
        rotationAngle = 0;
        rectangle = new RectF();
    }

    @Override
    public boolean shouldDelayChildPressedState()
    {
        return false;
    }

    public void setCyanView(PrimaryPaintView v)
    {
        this.cyanView = v;
        addView(v);
    }

    public void setMagentaView(PrimaryPaintView v)
    {
        this.magentaView = v;
        addView(v);
    }

    public void setYellowView(PrimaryPaintView v)
    {
        this.yellowView = v;
        addView(v);
    }

    public void setSelectorView(SelectorView v)
    {
        this.selectorView = v;
        addView(v);
    }

    public void setMixButtonView(MixButtonView v)
    {
        this.mixButtonView = v;
        addView(v);
    }

    public boolean removeCustomPaintView(CustomPaintView v)
    {
        if (!inMixMode && v != currentCustomPaintView && customPaintViews.size() > MIN_CUSTOM_COLORS)
        {
            customPaintViews.remove(v);
            removeView(v);
            return true;
        }
        return false;
    }

    public CustomPaintView addCustomPaintView()
    {
        if (customPaintViews.size() == MAX_CUSTOM_COLORS)
            return null;

        CustomPaintView v = new CustomPaintView(getContext());

        for (CustomPaintView c : customPaintViews)
        {
            c.setSelected(false);
            selectedCustomPaintViews.remove(c);
        }

        v.setSelected(true);
        selectedCustomPaintViews.add(v);

        customPaintViews.add(v);
        currentCustomPaintView = v;

        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                CustomPaintView cview = (CustomPaintView) view;
                cview.setSelected(!cview.isSelected());

                if (inMixMode)
                {
                    if (cview == currentCustomPaintView)
                    {
                        // Disallow deselecting current view
                        cview.setSelected(true);
                        return;
                    }

                    if (cview.isSelected())
                    {
                        cview.setSelected(true);
                        selectedCustomPaintViews.add(cview);
                        mixSelectedColors();
                    }
                    else
                    {
                        cview.setSelected(false);
                        selectedCustomPaintViews.remove(cview);
                        mixSelectedColors();
                    }
                }
                else
                {
                    currentCustomPaintView = cview;

                    for (CustomPaintView c : customPaintViews)
                    {
                        if (c != cview)
                        {
                            c.setSelected(false);
                            selectedCustomPaintViews.remove(c);
                        }
                    }
                    cview.setSelected(true);
                    selectedCustomPaintViews.add(cview);

                    if (listener != null)
                    {
                        listener.onSelectedColorChange(getCurColor());
                    }
                }
            }
        });

        v.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return removeCustomPaintView((CustomPaintView)view);
            }
        });

        addView(v);
        return v;
    }

    public void setInMixMode(boolean m)
    {
        this.inMixMode = m;
        mixButtonView.setActive(inMixMode);

        if (!m && selectedCustomPaintViews.size() > 1)
        {
            // Leaving mix mode, and user has actually selected multiple paints to mix...
            // (Don't bother mixing and adding a paint if they didn't select multiple.)

            int mixedColor = mixColors(selectedCustomPaintViews);

            // Try to create a new CustomPaintView
            CustomPaintView v = this.addCustomPaintView();
            if (v != null)
            {
                // Successfully added new CustomPaintView
                currentCustomPaintView = v;
            }
            currentCustomPaintView.setColor(mixedColor);

            for (CustomPaintView c : customPaintViews)
            {
                if (c != currentCustomPaintView)
                {
                    c.setSelected(false);
                    selectedCustomPaintViews.remove(c);
                }
            }
        }
    }

    public boolean isInMixMode()
    {
        return this.inMixMode;
    }

    public void setOnSelectedColorChangeListener(OnSelectedColorChangeListener l)
    {
        this.listener = l;
    }

    public void setCurColor(int color)
    {
        currentCustomPaintView.setColor(color);
    }

    public int getCurColor()
    {
        return currentCustomPaintView.getColor();
    }

    /**
     * Shows or hides the Palette.
     */
    public void togglePaletteVisible()
    {
        if (paletteVisible)
            hidePalette();
        else
            showPalette();
    }

    private void showPalette()
    {
        paletteVisible = true;
        mixButtonView.setClickable(true);
        animatePalette(translateY, 0, 180, 360, 0f, 1f);
    }

    private void hidePalette()
    {
        paletteVisible = false;
        mixButtonView.setClickable(false);
        animatePalette(0, translateY, 0, 180, 1f, 0f);
    }

    /**
     * Called reflectively by Animator
     */
    private void setRotationAngle(float angle)
    {
        rotationAngle = angle;
        requestLayout();
    }

    /**
     * Called reflectively by Animator
     */
    private void setOffsetY(float y)
    {
        offsetY = y;
    }

    private void animatePalette(float fromY, float toY, float fromAngle, float toAngle, float fromAlpha, float toAlpha)
    {
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(this, "rotationAngle", fromAngle, toAngle);
        rotationAnimator.setDuration(500);

        ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(this, "offsetY", fromY, toY);
        translateAnimator.setDuration(500);

        ObjectAnimator mixButtonAlpha = ObjectAnimator.ofFloat(mixButtonView, "alpha", fromAlpha, toAlpha);
        mixButtonAlpha.setDuration(100);

        AnimatorSet animation = new AnimatorSet();
        animation.playTogether(rotationAnimator, translateAnimator, mixButtonAlpha);
        animation.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        for (int i = 0; i < getChildCount(); i++)
        {
            // Kick off the measure for each child, but ignore what they say :)
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        float width = getWidth();
        float height = getHeight();
        float shortSide = width < height ? width : height;

        float radius = shortSide / 4f;
        this.axisX = width / 2f;
        this.axisY = height - (radius / 2) + offsetY;

        layoutCenter(this.selectorView, radius);
        layoutRight(this.mixButtonView, radius);
        layoutCircle(this.cyanView, 2, radius);
        layoutCircle(this.magentaView, 1, radius);
        layoutCircle(this.yellowView, 0, radius);
        layoutCustomPaints(this.customPaintViews, radius + 120f);
    }

    private void layoutCenter(View child, float radius)
    {
        float childRadius = radius / 2;
        this.translateY = childRadius * 0.8f;

        rectangle.left = axisX - childRadius;
        rectangle.right = axisX + childRadius;
        rectangle.top = axisY - childRadius;
        rectangle.bottom = axisY + childRadius;

        child.layout((int)rectangle.left, (int)rectangle.top, (int)rectangle.right, (int)rectangle.bottom);
    }

    private void layoutRight(View child, float radius)
    {
        float offsetX = radius / 2;
        float childRadius = radius / 5f;

        rectangle.left = axisX + offsetX;
        rectangle.right = axisX + offsetX + childRadius * 2;
        rectangle.top = axisY - childRadius;
        rectangle.bottom = axisY + childRadius;

        child.layout((int)rectangle.left, (int)rectangle.top, (int)rectangle.right, (int)rectangle.bottom);
    }

    private void layoutCircle(View child, int index, float radius)
    {
        float angle = 2.0f * (float)Math.PI * (index / 8.0f + 1f/8f - rotationAngle/360f);

        float childRadius = radius / 3;
        float childCenterX = radius * FloatMath.cos(angle) + axisX;
        float childCenterY = -radius * FloatMath.sin(angle) + axisY;

        rectangle.left = childCenterX - childRadius;
        rectangle.right = childCenterX + childRadius;
        rectangle.top = childCenterY - childRadius;
        rectangle.bottom = childCenterY + childRadius;

        child.layout((int)rectangle.left, (int)rectangle.top, (int)rectangle.right, (int)rectangle.bottom);
    }

    private void layoutCustomPaints(List<CustomPaintView> views, float radius)
    {
        for (int i = 0; i < views.size(); i++)
        {
            View child = views.get(i);
            float angleOffset = 0.25f - (0.5f * (views.size() - 1)) / 24f;
            float angle = 2.0f * (float)Math.PI * (i / 24.0f + angleOffset - rotationAngle/360f);

            float childRadius = radius / 10;
            float childCenterX = radius * FloatMath.cos(angle) + axisX;
            float childCenterY = -radius * FloatMath.sin(angle) + axisY;

            rectangle.left = childCenterX - childRadius;
            rectangle.right = childCenterX + childRadius;
            rectangle.top = childCenterY - childRadius;
            rectangle.bottom = childCenterY + childRadius;

            child.layout((int)rectangle.left, (int)rectangle.top, (int)rectangle.right, (int)rectangle.bottom);
        }
    }

    // For some reason the provided on{Save,Restore}InstanceState methods were not being called.
    protected void mySaveInstanceState(Bundle state)
    {
        ArrayList<Integer> customColors = new ArrayList<Integer>();
        ArrayList<Integer> selectedIndexes = new ArrayList<Integer>();
        int currentIndex = 0;
        for (int i = 0; i < customPaintViews.size(); i++)
        {
            CustomPaintView v = customPaintViews.get(i);
            customColors.add(v.getColor());
            if (selectedCustomPaintViews.contains(v))
                selectedIndexes.add(i);
            if (currentCustomPaintView == v)
                currentIndex = i;
        }

        Bundle b = new Bundle();
        b.putParcelable("super", super.onSaveInstanceState());
        b.putBoolean("paletteVisible", paletteVisible);
        b.putFloat("rotationAngle", rotationAngle);
        b.putFloat("offsetY", offsetY);
        b.putBoolean("inMixMode", inMixMode);
        b.putSerializable("customColors", customColors);
        b.putSerializable("selectedIndexes", selectedIndexes);
        b.putInt("currentIndex", currentIndex);

        state.putBundle("paletteView", b);
    }

    protected void myRestoreInstanceState(Bundle state)
    {
        Bundle b = state.getBundle("paletteView");
        paletteVisible = b.getBoolean("paletteVisible");
        rotationAngle = b.getFloat("rotationAngle");
        offsetY = b.getFloat("offsetY");
        inMixMode = b.getBoolean("inMixMode");
        ArrayList<Integer> customColors = (ArrayList<Integer>) b.getSerializable("customColors");
        ArrayList<Integer> selectedIndexes = (ArrayList<Integer>) b.getSerializable("selectedIndexes");
        int currentIndex = b.getInt("currentIndex");

        if (!paletteVisible)
        {
            mixButtonView.setClickable(false);
            mixButtonView.setAlpha(0);
        }

        // It may be that the user deleted a prepopulated paint...
        while (customPaintViews.size() > customColors.size())
        {
            CustomPaintView v = customPaintViews.get(0);
            customPaintViews.remove(v);
            removeView(v);
        }
        selectedCustomPaintViews.clear();

        for (int i = 0; i < customColors.size(); i++)
        {
            CustomPaintView v;
            if (i < this.customPaintViews.size())
            {
                v = this.customPaintViews.get(i);
            }
            else
            {
                v = this.addCustomPaintView();
            }
            v.setColor(customColors.get(i));

            if (currentIndex == i)
                this.currentCustomPaintView = v;
        }

        // Once all views are restored, set the selections.
        // NOTE: This must happen AFTER all views are restored.
        for (int i = 0; i < customColors.size(); i++)
        {
            CustomPaintView v = this.customPaintViews.get(i);
            if (selectedIndexes.contains(i))
            {
                this.selectedCustomPaintViews.add(v);
                v.setSelected(true);
            }
            else
            {
                this.selectedCustomPaintViews.remove(v);
                v.setSelected(false);
            }
        }

        // Let the app update other things that depend on the palette color.
        if (listener != null)
            listener.onSelectedColorChange(mixColors(selectedCustomPaintViews));
    }

    private void mixSelectedColors()
    {
        if (listener != null)
            listener.onSelectedColorChange(mixColors(selectedCustomPaintViews));
    }

    private static int mixColors(List<CustomPaintView> views)
    {
        // Simple algorithm for mixing N colors in the subtractive color space...
        // I made it up because I wasn't satisfied with what I found online.
        float c = 0;
        float m = 0;
        float y = 0;

        for (CustomPaintView v : views)
        {
            CMYColor color = CMYColor.fromRGB(v.getColor());
            c += color.getC();
            m += color.getM();
            y += color.getY();
        }
        return CMYColor.toRGB(clamp(c), clamp(m), clamp(y));
    }

    private static float clamp(float f)
    {
        return Math.min(1f, f);
    }
}
