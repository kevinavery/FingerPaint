/*
 * A simple finger painting application.
 * Created by Kevin Avery (kevin@avery.io)
 * September 2013
 * CS4962 Project 1
 */

package com.avery.fingerpaint;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity
{
    FrameLayout mainLayout;
    PaintAreaView paintAreaView;
    PaletteView paletteView;
    PrimaryPaintView cyanPaintView;
    PrimaryPaintView magentaPaintView;
    PrimaryPaintView yellowPaintView;
    SelectorView selectorView;
    MixButtonView mixButtonView;
    CMYColor cmy;
    PopupWindow helpWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        cmy = new CMYColor();

        paintAreaView = new PaintAreaView(this);
        paintAreaView.setId(42);

        selectorView = new SelectorView(this);
        selectorView.setId(2);
        selectorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    paletteView.togglePaletteVisible();
                return true;
            }
        });

        cyanPaintView = new PrimaryPaintView(this, Color.CYAN);
        cyanPaintView.setOnColorChangeListener(new PrimaryPaintView.OnColorChangeListener() {
            @Override
            public void OnColorChange(float v) {
                cmy.setC(v);
                paletteView.setCurColor(cmy.getRGB());
                updateNonPrimaries();
            }
        });

        magentaPaintView = new PrimaryPaintView(this, Color.MAGENTA);
        magentaPaintView.setOnColorChangeListener(new PrimaryPaintView.OnColorChangeListener() {
            @Override
            public void OnColorChange(float v) {
                cmy.setM(v);
                paletteView.setCurColor(cmy.getRGB());
                updateNonPrimaries();
            }
        });

        yellowPaintView = new PrimaryPaintView(this, Color.YELLOW);
        yellowPaintView.setOnColorChangeListener(new PrimaryPaintView.OnColorChangeListener() {
            @Override
            public void OnColorChange(float v) {
                cmy.setY(v);
                paletteView.setCurColor(cmy.getRGB());
                updateNonPrimaries();
            }
        });

        mixButtonView = new MixButtonView(this);
        mixButtonView.setId(3);
        mixButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean inMixMode = !paletteView.isInMixMode();
                paletteView.setInMixMode(inMixMode);
                cyanPaintView.setEditable(!inMixMode);
                magentaPaintView.setEditable(!inMixMode);
                yellowPaintView.setEditable(!inMixMode);
            }
        });

        paletteView = new PaletteView(this);
        paintAreaView.setId(1);
        paletteView.setSelectorView(selectorView);
        paletteView.setCyanView(cyanPaintView);
        paletteView.setMagentaView(magentaPaintView);
        paletteView.setYellowView(yellowPaintView);
        paletteView.setMixButtonView(mixButtonView);
        paletteView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        paletteView.setOnSelectedColorChangeListener(new PaletteView.OnSelectedColorChangeListener() {
            @Override
            public void onSelectedColorChange(int newColor) {
                cmy = CMYColor.fromRGB(newColor);
                updateColors();
            }
        });

        // Prepopulate the palette with some random colors.
        for (int i = 0; i < 7; i++)
        {
            final CustomPaintView v = paletteView.addCustomPaintView();
            v.setColor((int)(Math.random() * 0x00FFFFFF) | 0xFF000000);
        }

        cmy = CMYColor.fromRGB(paletteView.getCurColor());
        updateColors();

        mainLayout = new FrameLayout(this);
        mainLayout.addView(paintAreaView);
        mainLayout.addView(paletteView);

        setContentView(mainLayout);
    }

    private void updateColors()
    {
        cyanPaintView.setColorPercent(cmy.getC());
        magentaPaintView.setColorPercent(cmy.getM());
        yellowPaintView.setColorPercent(cmy.getY());

        updateNonPrimaries();
    }

    private void updateNonPrimaries()
    {
        paintAreaView.setPaintColor(cmy.getRGB());
        selectorView.setColor(cmy.getRGB());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_clear:
                paintAreaView.clear();
                return true;
            case R.id.action_help:
                if (helpWindow == null)
                    helpWindow = buildHelpWindow();

                helpWindow.showAtLocation(mainLayout, Gravity.CENTER, 10, 10);
                int width = mainLayout.getWidth() - 20;
                int height = mainLayout.getHeight() - 20;
                helpWindow.update(width, height);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private PopupWindow buildHelpWindow()
    {
        final PopupWindow p = new PopupWindow(this);

        TextView text = new TextView(this);
        text.setPadding(10, 10, 10, 10);
        text.setTextSize(20);
        text.setTextColor(Color.GREEN);
        text.setText(getResources().getString(R.string.help_text));

        Button okButton = new Button(this);
        okButton.setText("OK");
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                p.dismiss();
            }
        });

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(text, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearLayout.addView(okButton, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(linearLayout, ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        p.setContentView(scrollView);
        return p;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable("cmyColor", cmy);
        paletteView.mySaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        cmy = (CMYColor) savedInstanceState.getSerializable("cmyColor");
        paletteView.myRestoreInstanceState(savedInstanceState);
    }

}
