package ohos.cmm;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import sun.java2d.cmm.ColorTransform;

/**
 * Identity color transform.
 *
 * <p>The array based variants MUST return an array sized by the output component count, not by
 * the input length: {@code ICC_ColorSpace.toRGB()} converts a grayscale value (1 component) and
 * then reads {@code tmp[0..2]}, so returning {@code new short[src.length]} makes it throw
 * {@code ArrayIndexOutOfBoundsException: Index 1 out of bounds for length 1}.</p>
 */
public class OhosColorTransform implements ColorTransform {
    private final int in;
    private final int out;

    public OhosColorTransform(int in, int out) { this.in = in; this.out = out; }

    @Override public int getNumInComponents() { return in; }
    @Override public int getNumOutComponents() { return out; }

    @Override public void colorConvert(BufferedImage src, BufferedImage dst) {
        int w = Math.min(src.getWidth(), dst.getWidth());
        int h = Math.min(src.getHeight(), dst.getHeight());
        int[] row = new int[w];
        for (int y = 0; y < h; y++) {
            src.getRGB(0, y, w, 1, row, 0, w);
            dst.setRGB(0, y, w, 1, row, 0, w);
        }
    }

    @Override public void colorConvert(Raster src, WritableRaster dst,
                                       float[] srcMinVal, float[] srcMaxVal,
                                       float[] dstMinVal, float[] dstMaxVal) {
        colorConvert(src, dst);
    }

    @Override public void colorConvert(Raster src, WritableRaster dst) {
        int w = Math.min(src.getWidth(), dst.getWidth());
        int h = Math.min(src.getHeight(), dst.getHeight());
        int[] row = new int[w];
        for (int y = 0; y < h; y++) {
            src.getPixels(src.getMinX(), src.getMinY() + y, w, 1, row);
            dst.setPixels(dst.getMinX(), dst.getMinY() + y, w, 1, row);
        }
    }

    @Override public short[] colorConvert(short[] src, short[] dest) {
        int n = out > 0 ? out : 3;
        short[] r = (dest != null && dest.length >= n) ? dest : new short[n];
        if (src.length == n) {
            System.arraycopy(src, 0, r, 0, n);
        } else if (src.length == 1) {
            r[0] = src[0];
            if (n > 1) {
                r[1] = src[0];
            }
            if (n > 2) {
                r[2] = src[0];
            }
        } else {
            System.arraycopy(src, 0, r, 0, Math.min(src.length, n));
        }
        return r;
    }

    @Override public byte[] colorConvert(byte[] src, byte[] dest) {
        int n = out > 0 ? out : 3;
        byte[] r = (dest != null && dest.length >= n) ? dest : new byte[n];
        if (src.length == n) {
            System.arraycopy(src, 0, r, 0, n);
        } else if (src.length == 1) {
            r[0] = src[0];
            if (n > 1) {
                r[1] = src[0];
            }
            if (n > 2) {
                r[2] = src[0];
            }
        } else {
            System.arraycopy(src, 0, r, 0, Math.min(src.length, n));
        }
        return r;
    }
}
