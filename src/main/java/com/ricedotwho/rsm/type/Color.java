package com.ricedotwho.rsm.type;

import com.ricedotwho.rsm.utils.MathUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("unused")
public final class Color implements Cloneable {
    @Getter
    @Setter
    private double l, c, h, a, b;
    @Getter
    float alpha;
    private float cachedHueFloat, cachedSaturationFloat, cachedValueFloat;
    private byte cachedR, cachedG, cachedB, cachedAlpha;
    private float cachedRFloat, cachedGFloat, cachedBFloat;
    private boolean cached;

    public Color setAlpha(float alpha) {
        this.alpha = alpha;
        cached = false;
        return this;
    }

    private Color(double l, double c, double h, float alpha) {
        this.l = l;
        this.c = c;
        this.h = h;
        this.alpha = alpha;
        this.a = c * Math.cos(h);
        this.b = c * Math.sin(h);
    }

    /**
     * @param alpha [0 - 255]
     */
    public static Color fromHSBA(float hue, float saturation, float brightness, float alpha) {
        return Color.fromHSVA(hue, saturation, brightness, alpha);
    }
    /**
     * @param alpha [0 - 255]
     */
    public static Color fromHSVA(float hue, float saturation, float brightness, float alpha) {
        val rgb = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        return Color.fromRGB(r, g, b).setAlpha(alpha / 255f);
    }

    public static Color fromHex(int rgb, float alpha) {
        return Color.fromRGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha);
    }

    public static Color fromHex(String hex) {
        val color = Color.WHITE.clone();
        val alpha = hex.length() > 7;
        parseHex(hex, alpha).ifPresent(color::setToColor);
        return color;
    }

    public static Color fromHex(int rgb) {
        return Color.fromRGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 1);
    }

    public static Color fromARGB(int argb) {
        return Color.fromRGB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, ((argb >> 24) & 0xFF) / 255f);
    }

    public int getARGB() {
        ensureCached();
        return ((cachedAlpha & 0xFF) << 24) | ((cachedR & 0xFF) << 16) | ((cachedG & 0xFF) << 8) | (cachedB & 0xFF);
    }

    public int getARGBWithAlpha(float alpha) {
        ensureCached();
        return (((int) Math.clamp(alpha * 255f, 0f, 1f)) << 24) | ((cachedR & 0xFF) << 16) | ((cachedG & 0xFF) << 8) | (cachedB & 0xFF);
    }

    @NotNull
    public Color copy() {
        return clone();
    }

    @Override
    @NotNull
    public Color clone() {
        try {
            return (Color) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is to allow myself to change a color while keeping the pointer the same
     */
    public void setToColor(Color other) {
        this.l = other.l;
        this.c = other.c;
        this.h = other.h;
        this.a = other.a;
        this.b = other.b;

        this.alpha = other.alpha;
        this.cached = false;
    }

    public int hsbMax() {
        ensureCached();
        int rgb = java.awt.Color.HSBtoRGB(cachedHueFloat, 1f, 1f);
        return (rgb & 0x00FFFFFF) | ((int) (alpha * 255) << 24);
    }

    public String getHexCode(boolean includeAlpha) {
        ensureCached();
        String red = pad(Integer.toHexString(cachedR & 0xFF)).toUpperCase();
        String green = pad(Integer.toHexString(cachedG & 0xFF)).toUpperCase();
        String blue = pad(Integer.toHexString(cachedB & 0xFF)).toUpperCase();
        String alpha = includeAlpha ? pad(Integer.toHexString(cachedAlpha & 0xFF)).toUpperCase() : "";

        return red + green + blue + alpha;
    }

    private static String pad(String s) {
        return (s.length() == 1) ? "0" + s : s;
    }

    public void setToColor(byte r, byte g, byte b, byte alpha) {
        setFromSrgbBytes(r & 0xFF, g & 0xFF, b & 0xFF, ((float) (alpha & 0xFF)) / 255f);

        cacheHSV();
        cached = true;
    }

    public void setHSV(float hue, float saturation, float value, float alpha) {
        int rgb = java.awt.Color.HSBtoRGB(hue, saturation, value);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        setFromSrgbBytes(r, g, b, alpha);

        cachedValueFloat = value;
        cachedSaturationFloat = saturation;
        cachedHueFloat = hue;
        cached = true;
    }

    public void setToColor(int other) {
        int a = (other >> 24) & 0xFF;
        int r = (other >> 16) & 0xFF;
        int g = (other >> 8) & 0xFF;
        int b = other & 0xFF;

        setFromSrgbBytes(r, g, b, a / 255f);

        cacheHSV();
        cached = true;
    }

    public static Color fromRGB(int r, int g, int b) {
        return fromRGB(r, g, b, 1);
    }

    public static Color fromRGB(int r, int g, int b, float a) {
        if (a > 1 || a < 0) {
            throw new IllegalArgumentException("alpha must be between [0-1]");
        }
        double[] oklab = rgbBytesToOklab(r, g, b);
        Color color = new Color(oklab[0], Math.sqrt(oklab[1] * oklab[1] + oklab[2] * oklab[2]),
                Math.atan2(oklab[2], oklab[1]), a);
        color.ensureCached();
        return color;
    }

    public static Color fromOKLCH(double l, double c, double h, float a) {
        return new Color(l, c, h, a);
    }

    /**
     * Shared implementation for going from 8-bit sRGB + alpha to this color's OkLab/OkLCH
     * fields and cached byte representation. Used by setHSV, setToColor(int) and fromRGB so
     * the sRGB -> linear -> LMS -> OkLab math only lives in one place.
     */
    private void setFromSrgbBytes(int r, int g, int b, float alpha) {
        double lr = srgbToLinear(r / 255.0);
        double lg = srgbToLinear(g / 255.0);
        double lb = srgbToLinear(b / 255.0);

        double lmsL = 0.4122214708 * lr + 0.5363325363 * lg + 0.0514459929 * lb;
        double lmsM = 0.2119034982 * lr + 0.6806995451 * lg + 0.1073969566 * lb;
        double lmsS = 0.0883024619 * lr + 0.2817188376 * lg + 0.6299787005 * lb;

        double l_ = Math.cbrt(lmsL), m_ = Math.cbrt(lmsM), s_ = Math.cbrt(lmsS);

        this.l = 0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_;
        this.a = 1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_;
        this.b = 0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_;
        this.c = Math.sqrt(a * a + b * b);
        this.h = Math.atan2(b, a);
        this.alpha = Math.clamp(alpha, 0f, 1f);

        this.cachedR = (byte) r;
        this.cachedG = (byte) g;
        this.cachedB = (byte) b;
        this.cachedAlpha = (byte) Math.round(this.alpha * 255f);
    }

    /** @return {okL, okA, okB} */
    private static double[] rgbBytesToOklab(int r, int g, int b) {
        double lr = srgbToLinear(r / 255.0);
        double lg = srgbToLinear(g / 255.0);
        double lb = srgbToLinear(b / 255.0);

        double lmsL = 0.4122214708 * lr + 0.5363325363 * lg + 0.0514459929 * lb;
        double lmsM = 0.2119034982 * lr + 0.6806995451 * lg + 0.1073969566 * lb;
        double lmsS = 0.0883024619 * lr + 0.2817188376 * lg + 0.6299787005 * lb;

        double l_ = Math.cbrt(lmsL), m_ = Math.cbrt(lmsM), s_ = Math.cbrt(lmsS);

        double okL = 0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_;
        double okA = 1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_;
        double okB = 0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_;

        return new double[] { okL, okA, okB };
    }

    private void ensureCached() {
        if (cached) return;

        float[] rgb = computeRGBArray(l, a, b);
        cachedRFloat = rgb[0];
        cachedGFloat = rgb[1];
        cachedBFloat = rgb[2];

        var argb = computeARGB(rgb[0], rgb[1], rgb[2], alpha);
        cacheARGB(argb);

        cacheHSV();
        cached = true;
    }

    private void cacheHSV() {
        float[] hsv = java.awt.Color.RGBtoHSB(((int) cachedR) & 0xFF, ((int) cachedG) & 0xFF, ((int) cachedB) & 0xFF, new float[3]);
        cachedHueFloat = hsv[0];
        cachedSaturationFloat = hsv[1];
        cachedValueFloat = hsv[2];
    }

    public float getHueFloat() {
        ensureCached();
        return cachedHueFloat;
    }

    public float getSaturationFloat() {
        ensureCached();
        return cachedSaturationFloat;
    }

    public float getValueFloat() {
        ensureCached();
        return cachedValueFloat;
    }

    public float getRedFloat() {
        ensureCached();
        return cachedRFloat;
    }

    public float getGreenFloat() {
        ensureCached();
        return cachedGFloat;
    }

    public float getBlueFloat() {
        ensureCached();
        return cachedBFloat;
    }

    public byte getRedByte() {
        ensureCached();
        return cachedR;
    }

    public byte getGreenByte() {
        ensureCached();
        return cachedG;
    }

    public byte getBlueByte() {
        ensureCached();
        return cachedB;
    }

    public byte getAlphaByte() {
        ensureCached();
        return cachedAlpha;
    }

    private void cacheARGB(int argb) {
        this.cachedAlpha = (byte) (argb >> 24);
        this.cachedR = (byte) ((argb >> 16) & 0xFF);
        this.cachedG = (byte) ((argb >> 8) & 0xFF);
        this.cachedB = (byte) (argb & 0xFF);
    }

    private static float[] computeRGBArray(double l, double a, double b) {

        double l_ = l + 0.3963377774 * a + 0.2158037573 * b;
        double m_ = l - 0.1055613458 * a - 0.0638541728 * b;
        double s_ = l - 0.0894841775 * a - 1.2914855480 * b;

        double lCube = l_ * l_ * l_, mCube = m_ * m_ * m_, sCube = s_ * s_ * s_;

        double lr = Math.clamp( 4.0767416621 * lCube - 3.3077115913 * mCube + 0.2309699292 * sCube, 0.0, 1.0);
        double lg = Math.clamp(-1.2684380046 * lCube + 2.6097574011 * mCube - 0.3413193965 * sCube, 0.0, 1.0);
        double lb = Math.clamp(-0.0041960863 * lCube - 0.7034186147 * mCube + 1.7076147010 * sCube, 0.0, 1.0);
        return new float[] {
                (float) lr, (float) lg, (float) lb
        };
    }

    public static Optional<Integer> parseHex(String hex, boolean includeAlpha) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        if (hex.length() > 8) return Optional.empty();
        if (hex.length() < 6) return Optional.empty();

        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            int a = includeAlpha && hex.length() == 8 ? Integer.parseInt(hex.substring(6, 8), 16) : 255;
            return Optional.of(((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static int computeARGB(float red, float green, float blue, float alpha) {
        var r = (byte) Math.round(linearToSrgb(red) * 255.0);
        var g = (byte) Math.round(linearToSrgb(green) * 255.0);
        var b = (byte) Math.round(linearToSrgb(blue) * 255.0);
        var a = (byte) Math.round(Math.clamp(alpha, 0.0, 1.0) * 255.0);
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    private static double srgbToLinear(double x) {
        return x >= 0.04045 ? Math.pow((x + 0.055) / 1.055, 2.4) : x / 12.92;
    }

    private static double linearToSrgb(double x) {
        return x >= 0.0031308 ? 1.055 * Math.pow(x, 1.0 / 2.4) - 0.055 : 12.92 * x;
    }

    /**
     * Lerps and mutates the color. The intent is for a situation where you want to lerp
     * between two colors but keep the original pointer.
     */
    public void mutateLerp(Color a, Color b, float t) {
        this.l = MathUtils.lerp(a.l, b.l, t);
        this.a = MathUtils.lerp(a.a, b.a, t);
        this.b = MathUtils.lerp(a.b, b.b, t);
        this.alpha = MathUtils.lerp(a.alpha, b.alpha, t);

        this.c = Math.sqrt(this.a * this.a + this.b * this.b);
        this.h = Math.atan2(this.b, this.a);

        this.cached = false;
    }

    public void mutateLerpNoAlpha(Color a, Color b, float t) {
        this.l = MathUtils.lerp(a.l, b.l, t);
        this.a = MathUtils.lerp(a.a, b.a, t);
        this.b = MathUtils.lerp(a.b, b.b, t);

        this.c = Math.sqrt(this.a * this.a + this.b * this.b);
        this.h = Math.atan2(this.b, this.a);

        this.cached = false;
    }

    /**
     * Lerps from a to b with time t
     * @return argb representation of the lerped color 
     */
    static public int lerp(Color a, Color b, float t) {
        var lerpedL = MathUtils.lerp(a.l, b.l, t);
        var lerpedA = MathUtils.lerp(a.a, b.a, t);
        var lerpedB = MathUtils.lerp(a.b, b.b, t);
        var lerpedAlpha = MathUtils.lerp(a.alpha, b.alpha, t);


        var lerpedRGB = computeRGBArray(lerpedL, lerpedA, lerpedB);

        return computeARGB(lerpedRGB[0], lerpedRGB[1], lerpedRGB[2], lerpedAlpha);
    }

    /**
     * Lerps from a to b with time t while keeping the alpha of color a
     * @return argb representation of the lerped color
     */
    static public int lerpNoAlpha(Color a, Color b, float t) {
        var lerpedL = MathUtils.lerp(a.l, b.l, t);
        var lerpedA = MathUtils.lerp(a.a, b.a, t);
        var lerpedB = MathUtils.lerp(a.b, b.b, t);
        var lerpedRGB = computeRGBArray(lerpedL, lerpedA, lerpedB);

        return computeARGB(lerpedRGB[0], lerpedRGB[1], lerpedRGB[2], a.alpha);
    }

    public static int lerpARGB(int a, int b, float t) {
        int a0 = (a >>> 24) & 0xFF;
        int r0 = (a >>> 16) & 0xFF;
        int g0 = (a >>> 8) & 0xFF;
        int b0 =  a & 0xFF;

        int a1 = (b >>> 24) & 0xFF;
        int r1 = (b >>> 16) & 0xFF;
        int g1 = (b >>> 8) & 0xFF;
        int b1 =  b & 0xFF;

        int r = (int)(r0 + (r1 - r0) * t);
        int g = (int)(g0 + (g1 - g0) * t);
        int bC = (int)(b0 + (b1 - b0) * t);
        int aC = (int)(a0 + (a1 - a0) * t);

        return (aC << 24) | (r << 16) | (g << 8) | bC;
    }

    public static int setArgbAlpha(int argb, float alpha) {
        var a = (int) (Math.clamp(alpha, 0f, 1f) * 255) << 24;
        argb &= 0x00FFFFFF;
        return argb | a;
    }

    public static byte getRedByte(int argb) {
        return (byte) ((argb >> 16) & 0xFF);
    }
    public static byte getGreenByte(int argb) {
        return (byte) ((argb >> 8) & 0xFF);
    }

    public static byte getBlueByte(int argb) {
        return (byte) (argb & 0xFF);
    }

    public static byte getAlphaByte(int argb) {
        return (byte) ((argb >> 24) & 0xFF);
    }

    public int darker() {
        return darker(0.3f);
    }

    public int darker(float factor) {
        return lerpNoAlpha(this, BLACK, factor);
    }

    public int brighter() {
        return brighter(0.3f);
    }

    public int brighter(float factor) {
        return lerpNoAlpha(this, WHITE, factor);
    }

    /**
     * Takes a brightness factor between -1 to 1,
     * negative values decrease brightness
     * positive values increase brightness
     * @return returns argb
     */
    public int adjustBrightness(float factor) {
        if (factor == 0) return getARGB();
        return factor < 0 ? darker(factor * -1) : brighter(factor);
    }

    public static final Color white = Color.fromRGB(255, 255, 255);
    public static final Color WHITE = white;

    public static final Color lightGray = Color.fromRGB(192, 192, 192);
    public static final Color LIGHT_GRAY = lightGray;

    public static final Color gray = Color.fromRGB(128, 128, 128);
    public static final Color GRAY = gray;

    public static final Color darkGray = Color.fromRGB(64, 64, 64);
    public static final Color DARK_GRAY = darkGray;

    public static final Color black = Color.fromRGB(0, 0, 0);
    public static final Color BLACK = black;

    public static final Color red = Color.fromRGB(255, 0, 0);
    public static final Color RED = red;

    public static final Color pink = Color.fromRGB(255, 175, 175);
    public static final Color PINK = pink;

    public static final Color orange = Color.fromRGB(255, 200, 0);
    public static final Color ORANGE = orange;

    public static final Color yellow = Color.fromRGB(255, 255, 0);
    public static final Color YELLOW = yellow;

    public static final Color green = Color.fromRGB(0, 255, 0);
    public static final Color GREEN = green;

    public static final Color magenta   = Color.fromRGB(255, 0, 255);
    public static final Color MAGENTA = magenta;

    public static final Color cyan = Color.fromRGB(0, 255, 255);
    public static final Color CYAN = cyan;

    public static final Color blue = Color.fromRGB(0, 0, 255);
    public static final Color BLUE = blue;

    public static final Color transparent = Color.fromRGB(0, 0, 0, 0);
    public static final Color TRANSPARENT = transparent;

    public static final Color MINECRAFT_DARK_BLUE = Color.fromRGB(0, 0, 170);
    public static final Color MINECRAFT_DARK_GREEN = Color.fromRGB(0, 170, 0);
    public static final Color MINECRAFT_DARK_AQUA = Color.fromRGB(0, 170, 170);
    public static final Color MINECRAFT_DARK_RED = Color.fromRGB(170, 0, 0);
    public static final Color MINECRAFT_DARK_PURPLE = Color.fromRGB(170, 0, 170);
    public static final Color MINECRAFT_GOLD = Color.fromRGB(255, 170, 0);
    public static final Color MINECRAFT_GRAY = Color.fromRGB(170, 170, 170);
    public static final Color MINECRAFT_DARK_GRAY = Color.fromRGB(85, 85, 85);
    public static final Color MINECRAFT_BLUE = Color.fromRGB(85, 85, 255);
    public static final Color MINECRAFT_GREEN = Color.fromRGB(85, 255, 85);
    public static final Color MINECRAFT_AQUA = Color.fromRGB(85, 255, 255);
    public static final Color MINECRAFT_RED = Color.fromRGB(255, 85, 85);
    public static final Color MINECRAFT_LIGHT_PURPLE = Color.fromRGB(255, 85, 255);
    public static final Color MINECRAFT_YELLOW = Color.fromRGB(255, 255, 85);
}
