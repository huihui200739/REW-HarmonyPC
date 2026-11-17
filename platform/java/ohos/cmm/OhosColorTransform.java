package ohos.cmm;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import sun.java2d.cmm.ColorTransform;

/** identity 色彩变换：数据直通。测量数据不经过颜色映射，数值不受影响。 */
public class OhosColorTransform implements ColorTransform {
    private final int in;
    private final int out;

    public OhosColorTransform(int in, int out) { this.in = in; this.out = out; }

    @Override public int getNumInComponents() { return in; }
    @Override public int getNumOutComponents() { return out; }

    @Override public void colorConvert(BufferedImage src, BufferedImage dst) {
        // 直通：复制 src 到 dst（尺寸一致时）
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
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                for (int b = 0; b < dst.getNumBands() && b < src.getNumBands(); b++) {
                    dst.setSample(x, y, b, src.getSampleDouble(x, y, b));
                }
            }
        }
    }

    @Override public short[] colorConvert(short[] src, short[] dest) {
        short[] out = (dest != null && dest.length >= src.length) ? dest : new short[src.length];
        System.arraycopy(src, 0, out, 0, src.length);
        return out;
    }

    @Override public byte[] colorConvert(byte[] src, byte[] dest) {
        byte[] out = (dest != null && dest.length >= src.length) ? dest : new byte[src.length];
        System.arraycopy(src, 0, out, 0, src.length);
        return out;
    }
}
