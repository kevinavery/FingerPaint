package com.avery.fingerpaint;

import android.graphics.Color;

import java.io.Serializable;

/**
 * Represents a value the Cyan Magenta Yellow color space.
 */
public class CMYColor implements Serializable
{
    // c, m, y should be between 0.0f and 1.0f
    private float c, m, y;

    public void setC(float c)
    {
        this.c = c;
    }

    public void setM(float m)
    {
        this.m = m;
    }

    public void setY(float y)
    {
        this.y = y;
    }

    public float getC()
    {
        return c;
    }

    public float getM()
    {
        return m;
    }

    public float getY()
    {
        return y;
    }

    public int getRGB()
    {
        return CMYColor.toRGB(c, m, y);
    }

    /**
     * Convert from CMY to RGB.
     */
    public static int toRGB(float c, float m, float y)
    {
        // Based on ColorMine https://github.com/THEjoezack/ColorMine (MIT License).
        int r = (int) ((1 - c) * 255);
        int g = (int) ((1 - m) * 255);
        int b = (int) ((1 - y) * 255);

        return Color.rgb(r, g, b);
    }

    /**
     * Convert from RGB to CMY.
     */
    public static CMYColor fromRGB(int color)
    {
        CMYColor newColor = new CMYColor();

        // Based on ColorMine https://github.com/THEjoezack/ColorMine (MIT License).
        newColor.setC(1f - Color.red(color) / 255f);
        newColor.setM(1f - Color.green(color) / 255f);
        newColor.setY(1f - Color.blue(color) / 255f);

        return newColor;
    }
}
