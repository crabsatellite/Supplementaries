package net.mehvahdjukaar.supplementaries.client.renderers;

import net.minecraft.util.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class HSLColor {

    public static int getRainbowColorPost(float division){
        float scale = 3600f/division;
        float h = (((int)((System.currentTimeMillis())%(int)scale)/scale));
        float[] hsl = postProcess(new float[]{h,0.6f,0.5f});
        return hslToRgb(hsl[0],hsl[1],hsl[2]);
    }

    public static int getRainbowColor(float division){
        float scale = 3600f/division;
        float h = (((int)((System.currentTimeMillis())%(int)scale)/scale));
        return hslToRgb(h,0.6f,0.5f);
    }


    public static float[] postProcess(float[] hsl){
        float h = hsl[0];
        float s = hsl[1];
        float l = hsl[2];
        //map one to one. no effect on its own
        float c = 1-Math.abs((2*l)-1);
        if(s>c)s=c;

        //remove darker colors
        float minLightness = 0.47f;
        l=Math.max(l,minLightness);

        //saturate dark colors
        float j = 1f*(1-l);
        //if(s<j)s=j;


        //desaturate blue
        float scaling = 0.15f;
        float angle = 90;
        float n = (float) (scaling*Math.exp(-angle*Math.pow((h-0.6666f),2)));
        s -=n;


        return new float[]{h,s,l};
    }


    //https://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion

    /**
     * Converts an HSL color value to RGB. Conversion formula
     * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
     * Assumes h, s, and l are contained in the set [0, 1] and
     * returns r, g, and b in the set [0, 255].
     *
     * @param h       The hue
     * @param s       The saturation
     * @param l       The lightness
     * @return int array, the RGB representation
     */
    public static int hslToRgb(float h, float s, float l){
        float r, g, b;

        if (s == 0f) {
            r = g = b = l; // achromatic
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1f/3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f/3f);
        }
        return ColorHelper.PackedColor.color(
                MathHelper.floor(255),MathHelper.floor(r*255),MathHelper.floor(g*255),MathHelper.floor(b*255));
    }
    public static int to255(float v) { return (int)Math.min(255,256*v); }

    /** Helper method that converts hue to rgb */
    public static float hueToRgb(float p, float q, float t) {
        if (t < 0f)
            t += 1f;
        if (t > 1f)
            t -= 1f;
        if (t < 1f/6f)
            return p + (q - p) * 6f * t;
        if (t < 1f/2f)
            return q;
        if (t < 2f/3f)
            return p + (q - p) * (2f/3f - t) * 6f;
        return p;
    }

    public static float[] rgbToHsl(int rgb) {
        int r = ColorHelper.PackedColor.red(rgb);
        int g = ColorHelper.PackedColor.green(rgb);
        int b = ColorHelper.PackedColor.blue(rgb);
        return rgbToHsl(r,g,b);
    }
    /**
     * Converts an RGB color value to HSL. Conversion formula
     * adapted from http://en.wikipedia.org/wiki/HSL_color_space.
     * Assumes pR, pG, and bpBare contained in the set [0, 255] and
     * returns h, s, and l in the set [0, 1].
     *
     * @param pR       The red color value
     * @param pG       The green color value
     * @param pB       The blue color value
     * @return float array, the HSL representation
     */
    public static float[] rgbToHsl(int pR, int pG, int pB) {
        float r = pR / 255f;
        float g = pG / 255f;
        float b = pB / 255f;

        float max = (r > g && r > b) ? r : Math.max(g, b);
        float min = (r < g && r < b) ? r : Math.min(g, b);

        float h, s, l;
        l = (max + min) / 2.0f;

        if (max == min) {
            h = s = 0.0f;
        } else {
            float d = max - min;
            s = (l > 0.5f) ? d / (2.0f - max - min) : d / (max + min);

            if (r > g && r > b)
                h = (g - b) / d + (g < b ? 6.0f : 0.0f);

            else if (g > b)
                h = (b - r) / d + 2.0f;

            else
                h = (r - g) / d + 4.0f;

            h /= 6.0f;
        }
        return new float[]{h, s, l};
    }

    //not my code. I found it here
    //https://tips4java.wordpress.com/2009/07/05/hsl-color/

    /**
     *  Convert a RGB Color to it corresponding HSL values.
     *  H: 0-360, S: 0-100, L: 0-100
     *
     *  @return an array containing the 3 HSL values.
     */
    public static float[] fromRGB(int rgb) {
        float r = ColorHelper.PackedColor.red(rgb)/255f;
        float g = ColorHelper.PackedColor.green(rgb)/255f;
        float b = ColorHelper.PackedColor.blue(rgb)/255f;
        return fromRGB(r,g,b);
    }

    public static float[] fromRGB(float r, float g, float b) {

        //	Minimum and Maximum RGB values are used in the HSL calculations

        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));

        //  Calculate the Hue

        float h = 0;

        if (max == min)
            h = 0;
        else if (max == r)
            h = ((60 * (g - b) / (max - min)) + 360) % 360;
        else if (max == g)
            h = (60 * (b - r) / (max - min)) + 120;
        else if (max == b)
            h = (60 * (r - g) / (max - min)) + 240;

        //  Calculate the Luminance

        float l = (max + min) / 2;

        //  Calculate the Saturation

        float s = 0;

        if (max == min)
            s = 0;
        else if (l <= .5f)
            s = (max - min) / (max + min);
        else
            s = (max - min) / (2 - max - min);

        return new float[] {h, s * 100, l * 100};
    }

    public static int toRGB(float h, float s, float l) {
        return toRGB(h,s,l,1);
    }
    public static int toRGB(float h, float s, float l, float alpha) {
        s = MathHelper.clamp(s,0,100);
        l = MathHelper.clamp(l,0,100);

        //  Formula needs all values between 0 - 1.

        h = h % 360.0f;
        h /= 360f;
        s /= 100f;
        l /= 100f;

        float q = 0;

        if (l < 0.5)
            q = l * (1 + s);
        else
            q = (l + s) - (s * l);

        float p = 2 * l - q;

        float r = Math.max(0, HueToRGB(p, q, h + (1.0f / 3.0f)));
        float g = Math.max(0, HueToRGB(p, q, h));
        float b = Math.max(0, HueToRGB(p, q, h - (1.0f / 3.0f)));

        r = Math.min(r, 1.0f);
        g = Math.min(g, 1.0f);
        b = Math.min(b, 1.0f);

        return ColorHelper.PackedColor.color(
                MathHelper.floor(alpha*255),MathHelper.floor(r*255),MathHelper.floor(g*255),MathHelper.floor(b*255));
    }

    private static float HueToRGB(float p, float q, float h) {
        if (h < 0) h += 1;

        if (h > 1 ) h -= 1;

        if (6 * h < 1)
        {
            return p + ((q - p) * 6 * h);
        }

        if (2 * h < 1 )
        {
            return  q;
        }

        if (3 * h < 2)
        {
            return p + ( (q - p) * 6 * ((2.0f / 3.0f) - h) );
        }

        return p;
    }
}
