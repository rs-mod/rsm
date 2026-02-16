package com.ricedotwho.rsm.data;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.Serializable;

// Yes this is stolen from Polyfrost, colour > color

/**
 * Colour is a class for storing colors in HSBA format. This format is used to allow the color selectors to work correctly.
 * <p>
 * <code>
 * short[0] = hue (0-360)
 * short[1] = saturation (0-100)
 * short[2] = brightness (0-100)
 * short[3] = alpha (0-255)
 * </code>
 */
@SuppressWarnings("unused")
public final class Colour implements Serializable, Cloneable, Comparable<Colour> {
    transient private Integer argb = null;
    private short[] hsba;
    private int dataBit = -1;

    @Getter
    @Setter
    private static double FACTOR = 0.7;

    // hex constructor

    /** Create a Colour from the given hex.
     */
    public Colour(String hex) {
        hsba = new short[]{0, 0, 0, 0};
        if(hex.length() > 7) {
            hsba[3] = (short) Integer.parseInt(hex.substring(6, 8), 16);
        }
        setColorFromHex(hex);
    }

    // rgb constructors

    /**
     * Create a new Colour, converting the RGBA color to HSBA.
     */
    public Colour(int argb) {
        this.argb = argb;
        this.hsba = ARGBtoHSBA(this.argb);
    }

    /**
     * Create a new Colour from the given RGBA values.
     */
    public Colour(int r, int g, int b, int a) {
        this.argb = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
        this.hsba = ARGBtoHSBA(this.argb);
    }

    /**
     * Create a new Colour, converting the RGB color to HSBA.
     */
    public Colour(int r, int g, int b) {
        this(r, g, b, 255);
    }

    /**
     * Convert the java.awt.Color to an Colour (HSBA format).
     */
    public Colour(@NotNull Color c) {
        this(c.getRGB());
    }

    // hsb constructors

    /**
     * Create a new Colour from the given HSBA values.
     */
    public Colour(float hue, float saturation, float brightness, float alpha) {
        this.hsba = new short[]{(short) hue, (short) saturation, (short) brightness, (short) alpha};
        this.argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);

    }

    /**
     * Create a new Colour from the given HSB values. (alpha is set to max)
     */
    public Colour(float hue, float saturation, float brightness) {
        this(hue, saturation, brightness, 1.0f);
    }

    // chroma constructors

    /**
     * Create a new Chroma Colour. The speed should be a max of 30s and a min of 1s.
     */
    public Colour(int saturation, int brightness, int alpha, float chromaSpeed) {
        this(System.currentTimeMillis() % (int) (chromaSpeed * 1000) / (chromaSpeed * 1000) * 360, saturation, brightness, alpha);
        if (chromaSpeed < 1) chromaSpeed = 1;
        if (chromaSpeed > 30) chromaSpeed = 30;
        this.dataBit = (int) chromaSpeed * 1000;
    }

    // internal constructor
    public Colour(int hue, int saturation, int brightness, int alpha, int chromaSpeed) {
        if (chromaSpeed == -1) {
            this.hsba = new short[]{(short) hue, (short) saturation, (short) brightness, (short) alpha};
            this.argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
        } else {
            this.dataBit = chromaSpeed;
            this.hsba = new short[]{(short) hue, (short) saturation, (short) brightness, (short) alpha};
        }
    }


    // accessors

    /**
     * Get the RGBA color from the HSB color, and apply the alpha.
     */
    public static int HSBAtoARGB(float hue, float saturation, float brightness, int alpha) {
        int temp = Color.HSBtoRGB(hue / 360f, saturation / 100f, brightness / 100f);
        return ((temp & 0x00ffffff) | (alpha << 24));
    }

    /**
     * Get the HSBA color from the RGBA color.
     */
    public static short[] ARGBtoHSBA(int rgba) {
        short[] hsb = new short[4];
        float[] hsbArray = Color.RGBtoHSB((rgba >> 16 & 255), (rgba >> 8 & 255), (rgba & 255), null);
        hsb[0] = (short) (hsbArray[0] * 360);
        hsb[1] = (short) (hsbArray[1] * 100);
        hsb[2] = (short) (hsbArray[2] * 100);
        hsb[3] = (short) (rgba >> 24 & 255);
        return hsb;
    }

    /**
     * Get the red value of the color (0-255).
     */
    public int getRed() {
        return getRGB() >> 16 & 255;
    }

    public static int getRed(int rgb) {
        return rgb >> 16 & 255;
    }

    public byte getRedByte() {
        return (byte) this.getRed();
    }

    public float getRedFloat() {
        return this.getRed() / 255f;
    }

    /**
     * Get the green value of the color (0-255).
     */
    public int getGreen() {
        return getRGB() >> 8 & 255;
    }

    public static int getGreen(int rgb) {
        return rgb >> 8 & 255;
    }

    public byte getGreenByte() {
        return (byte) this.getGreen();
    }

    public float getGreenFloat() {
        return this.getGreen() / 255f;
    }

    /**
     * Get the blue value of the color (0-255).
     */
    public int getBlue() {
        return getRGB() & 255;
    }

    public static int getBlue(int rgb) {
        return rgb & 255;
    }

    public byte getBlueByte() {
        return (byte) this.getBlue();
    }

    public float getBlueFloat() {
        return this.getBlue() / 255f;
    }

    /**
     * Get the hue value of the color (0-360).
     */
    public int getHue() {
        return hsba[0];
    }

    public byte geHueByte() {
        return (byte) this.getHue();
    }

    /**
     * Get the saturation value of the color (0-100).
     */
    public int getSaturation() {
        return hsba[1];
    }

    public byte geSaturationByte() {
        return (byte) this.getSaturation();
    }

    /**
     * Get the brightness value of the color (0-100).
     */
    public int getBrightness() {
        return hsba[2];
    }

    public byte getBrightnessByte() {
        return (byte) this.getBrightness();
    }

    /**
     * Get the alpha value of the color (0-255).
     */
    public int getAlpha() {
        return hsba[3];
    }

    public byte getAlphaByte() {
        return (byte) this.getAlpha();
    }

    public float getAlphaFloat() {
        return this.getAlpha() / 255f;
    }

    public void setAlpha(int alpha) {
        this.hsba[3] = (short) alpha;
        argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
    }

    /**
     * Get the chroma speed of the color (1s-30s).
     */
    public int getDataBit() {
        return dataBit == -1 ? -1 : dataBit / 1000;
    }
    public int getDataBitRaw() {
        return dataBit;
    }

    /**
     * Set the current chroma speed of the color. -1 to disable.
     */
    public void setChromaSpeed(int speed) {
        if (speed == -1) {
            this.dataBit = -1;
            return;
        }
        if (speed < 1) speed = 1;
        if (speed > 30) speed = 30;
        this.dataBit = speed * 1000;
    }

    /**
     * Set the HSBA values of the color.
     */
    public void setHSBA(int hue, int saturation, int brightness, int alpha) {
        this.hsba[0] = (short) hue;
        this.hsba[1] = (short) saturation;
        this.hsba[2] = (short) brightness;
        this.hsba[3] = (short) alpha;
        this.argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
    }

    /** Set a part of this color based on the index in the array,<br> for example where hue is index 0, saturation is index 1... */
    public void setHSBA(int index, final int val) {
        this.hsba[index] = (short) val;
        argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
    }

    /** get the HSBA values for this color. */
    public short[] getHSBA() {
        return hsba;
    }

    public void setFromColour(Colour color) {
        setHSBA(color.hsba[0], color.hsba[1], color.hsba[2], color.hsba[3]);
    }

    /**
     * Return the current color in ARGB format. This is the format used by LWJGL and Minecraft.
     * This method WILL return the color as a chroma, at the specified speed, if it is set.
     * Otherwise, it will just return the current color.
     *
     * @return the current color in RGBA format (equivalent to getRGB of java.awt.Color)
     */
    public int getRGB() {
        if (dataBit == 0) dataBit = -1;
        if (dataBit == -1) {
            // fix for when rgba is not set because of deserializing not calling constructor
            if (argb == null) argb = HSBAtoARGB(this.hsba[0], this.hsba[1], this.hsba[2], this.hsba[3]);
            return argb;
        } else {
            int temp = Color.HSBtoRGB(System.currentTimeMillis() % dataBit / (float) dataBit, hsba[1] / 100f, hsba[2] / 100f);
            hsba[0] = (short) ((System.currentTimeMillis() % dataBit / (float) dataBit) * 360);
            return ((temp & 0x00ffffff) | (hsba[3] << 24));
        }
    }

    /**
     * return the current color without its alpha. Internal method.
     */
    public int getRGBNoAlpha() {
        return 0xff000000 | getRGB();
    }

    /**
     * Return the color as if it had maximum saturation and brightness. Internal method.
     */
    public int getRGBMax(boolean maxBrightness) {
        if (dataBit == 0) dataBit = -1;
        if (dataBit == -1) {
            return HSBAtoARGB(this.hsba[0], 100, maxBrightness ? 100 : 0, this.hsba[3]);
        } else {
            int temp = Color.HSBtoRGB(System.currentTimeMillis() % dataBit / (float) dataBit, 1, maxBrightness ? 1 : 0);
            hsba[0] = (short) ((System.currentTimeMillis() % dataBit / (float) dataBit) * 360);
            return ((temp & 0x00ffffff) | (hsba[3] << 24));
        }
    }

    public String getHex() {
        return Integer.toHexString(0xff000000 | getRGB()).toUpperCase().substring(2);
    }

    public void setColorFromHex(String hex) {
        hex = hex.replace("#", "");
        if(hex.length() == 8) {
            hsba[3] = (short) (Integer.parseInt(hex.substring(6, 8), 16));
        }
        if (hex.length() > 6) {
            hex = hex.substring(0, 6);
        }
        if (hex.length() == 3) {
            hex = charsToString(hex.charAt(0), hex.charAt(0), hex.charAt(1), hex.charAt(1), hex.charAt(2), hex.charAt(2));
        }
        if (hex.length() == 1) {
            hex = charsToString(hex.charAt(0), hex.charAt(0), hex.charAt(0), hex.charAt(0), hex.charAt(0), hex.charAt(0));
        }
        if (hex.length() == 2 && hex.charAt(1) == hex.charAt(0)) {
            hex = charsToString(hex.charAt(0), hex.charAt(0), hex.charAt(0), hex.charAt(0), hex.charAt(0), hex.charAt(0));
        }
        StringBuilder hexBuilder = new StringBuilder(hex);
        while (hexBuilder.length() < 6) {
            hexBuilder.append("0");
        }
        hex = hexBuilder.toString();
        int r = Integer.valueOf(hex.substring(0, 2), 16);
        int g = Integer.valueOf(hex.substring(2, 4), 16);
        int b = Integer.valueOf(hex.substring(4, 6), 16);
        this.argb = ((getAlpha() & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
        hsba = ARGBtoHSBA(argb);
    }

    private String charsToString(char... chars) {
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }

    public Color toJavaColor() {
        return new Color(getRGB(), true);
    }

    @Override
    public String toString() {
        return "Colour{rgba=[r=" + getRed() + ", g=" + getGreen() + ", b=" + getBlue() + ", a=" + getAlpha() + "], " +
                "hsba=[h=" + getHue() + ", s=" + getSaturation() + ", b=" + getBrightness() + ", a=" + getAlpha() + "], hex=" + getHex() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Colour color) {
            // can't just check the RGB because of chroma, so we just check the HSBA + the data bit
            return getHue() == color.getHue() && getSaturation() == color.getSaturation() &&
                    getBrightness() == color.getBrightness() && getAlpha() == color.getAlpha() && getDataBit() == color.getDataBit();
        } else return false;
    }

    @Override
    public int compareTo(@NotNull Colour o) {
        return getHue() - o.getHue() + getSaturation() - o.getSaturation() + getBrightness() - o.getBrightness() + getAlpha() - o.getAlpha();
    }

    public Colour brighter() {
        int r = getRed();
        int g = getGreen();
        int b = getBlue();
        int alpha = getAlpha();

        int i = (int) (1.0 /(1.0 - FACTOR));
        if ( r == 0 && g == 0 && b == 0) {
            return new Colour(i, i, i, alpha);
        }
        if ( r > 0 && r < i ) r = i;
        if ( g > 0 && g < i ) g = i;
        if ( b > 0 && b < i ) b = i;

        return new Colour(Math.min((int) (r / FACTOR), 255),
                Math.min((int)(g / FACTOR), 255),
                Math.min((int)(b / FACTOR), 255),
                alpha);
    }

    public Colour darker() {
        return new Colour(Math.max((int) (getRed() * FACTOR), 0),
                Math.max((int) (getGreen() * FACTOR), 0),
                Math.max((int) (getBlue() * FACTOR), 0),
                getAlpha());
    }

    public Colour alpha(int alpha) {
        Colour copy = this.copy();
        copy.setAlpha(alpha);
        return copy;
    }

    /**
     * Return a "safe" copy of this Colour. The precise meaning of this is that the returned Colour will not be affected by any changes made to this Colour.
     */
    @NotNull
    @Override
    public Colour clone() {
        return new Colour(getRed(), getBlue(), getGreen(), getAlpha());
    }

    /**
     * Return a "safe" copy of this Colour. The precise meaning of this is that the returned Colour will not be affected by any changes made to this Colour.
     * <br> same as {@link #clone()}
     * @see #clone()
     */
    @NotNull
    public Colour copy() {
        return clone();
    }

    public static final Colour white     = new Colour(255, 255, 255);

    public static final Colour WHITE = white;

    public static final Colour lightGray = new Colour(192, 192, 192);

    public static final Colour LIGHT_GRAY = lightGray;

    public static final Colour gray      = new Colour(128, 128, 128);

    public static final Colour GRAY = gray;

    public static final Colour darkGray  = new Colour(64, 64, 64);

    public static final Colour DARK_GRAY = darkGray;

    public static final Colour black     = new Colour(0, 0, 0);

    public static final Colour BLACK = black;

    public static final Colour red       = new Colour(255, 0, 0);

    public static final Colour RED = red;

    public static final Colour pink      = new Colour(255, 175, 175);

    public static final Colour PINK = pink;

    public static final Colour orange    = new Colour(255, 200, 0);

    public static final Colour ORANGE = orange;

    public static final Colour yellow    = new Colour(255, 255, 0);

    public static final Colour YELLOW = yellow;

    public static final Colour green     = new Colour(0, 255, 0);

    public static final Colour GREEN = green;

    public static final Colour magenta   = new Colour(255, 0, 255);

    public static final Colour MAGENTA = magenta;

    public static final Colour cyan      = new Colour(0, 255, 255);

    public static final Colour CYAN = cyan;

    public static final Colour blue      = new Colour(0, 0, 255);

    public static final Colour BLUE = blue;
}

