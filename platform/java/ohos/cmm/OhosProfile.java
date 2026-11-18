package ohos.cmm;

import java.util.HashMap;
import java.util.Map;

/**
 * A complete minimal sRGB ICC v2 profile implemented in pure Java.
 *
 * <p>{@code sun.java2d.cmm.Profile} is a concrete class that only wraps a native pointer, so this
 * subclass passes 0 and keeps the profile bytes on the Java side. {@link OhosCmm} serves those
 * bytes to the JDK, which lets {@code ICC_ColorSpace.toRGB()} find the rXYZ/gXYZ/bXYZ and
 * rTRC/gTRC/bTRC tags it needs.</p>
 */
public class OhosProfile extends sun.java2d.cmm.Profile {

    private static final int TAG_desc = 0x64657363;
    private static final int TAG_cprt = 0x63707274;
    private static final int TAG_wtpt = 0x77747074;
    private static final int TAG_rXYZ = 0x7258595a;
    private static final int TAG_gXYZ = 0x6758595a;
    private static final int TAG_bXYZ = 0x6258595a;
    private static final int TAG_rTRC = 0x72545243;
    private static final int TAG_gTRC = 0x67545243;
    private static final int TAG_bTRC = 0x62545243;

    private static final double D50_X = 0.9642;
    private static final double D50_Y = 1.0000;
    private static final double D50_Z = 0.8249;

    private static final double[] R_XYZ = { 0.4360, 0.2225, 0.0139 };
    private static final double[] G_XYZ = { 0.3851, 0.7169, 0.0971 };
    private static final double[] B_XYZ = { 0.1431, 0.0606, 0.7141 };

    private static final Map<Integer, byte[]> SHARED = buildTags();
    private static final byte[] SHARED_DATA = assemble(SHARED);

    private final byte[] data;

    public OhosProfile() {
        super(0L);
        this.data = SHARED_DATA;
    }

    public OhosProfile(byte[] raw) {
        super(0L);
        this.data = raw == null ? SHARED_DATA : raw;
    }

    public byte[] getData() {
        return data.clone();
    }

    public byte[] getTag(int sig) {
        byte[] v = SHARED.get(sig);
        return v == null ? null : v.clone();
    }

    private static Map<Integer, byte[]> buildTags() {
        Map<Integer, byte[]> m = new HashMap<>();
        m.put(TAG_wtpt, xyz(D50_X, D50_Y, D50_Z));
        m.put(TAG_rXYZ, xyz(R_XYZ[0], R_XYZ[1], R_XYZ[2]));
        m.put(TAG_gXYZ, xyz(G_XYZ[0], G_XYZ[1], G_XYZ[2]));
        m.put(TAG_bXYZ, xyz(B_XYZ[0], B_XYZ[1], B_XYZ[2]));
        byte[] trc = gammaCurve(2.2);
        m.put(TAG_rTRC, trc);
        m.put(TAG_gTRC, trc);
        m.put(TAG_bTRC, trc);
        m.put(TAG_desc, text("sRGB"));
        m.put(TAG_cprt, text("No copyright, use freely"));
        return m;
    }

    private static byte[] xyz(double x, double y, double z) {
        byte[] b = new byte[20];
        b[0] = 'X'; b[1] = 'Y'; b[2] = 'Z'; b[3] = ' ';
        putFixed(b, 8, x);
        putFixed(b, 12, y);
        putFixed(b, 16, z);
        return b;
    }

    private static void putFixed(byte[] b, int off, double v) {
        int raw = (int) Math.round(v * 65536.0);
        b[off] = (byte) (raw >>> 24);
        b[off + 1] = (byte) (raw >>> 16);
        b[off + 2] = (byte) (raw >>> 8);
        b[off + 3] = (byte) raw;
    }

    private static byte[] gammaCurve(double gamma) {
        byte[] b = new byte[14];
        b[0] = 'c'; b[1] = 'u'; b[2] = 'r'; b[3] = 'v';
        int v = (int) Math.round(gamma * 256.0);
        b[10] = (byte) (v >>> 8);
        b[11] = (byte) v;
        return b;
    }

    private static byte[] text(String s) {
        byte[] body = s.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] b = new byte[12 + body.length + 1 + 79];
        b[0] = 'd'; b[1] = 'e'; b[2] = 's'; b[3] = 'c';
        b[8] = (byte) (body.length >>> 24);
        b[9] = (byte) (body.length >>> 16);
        b[10] = (byte) (body.length >>> 8);
        b[11] = (byte) body.length;
        System.arraycopy(body, 0, b, 12, body.length);
        return b;
    }

    private static byte[] assemble(Map<Integer, byte[]> tags) {
        int tagCount = tags.size();
        int tableSize = 4 + tagCount * 12;
        int total = 128 + tableSize;
        for (byte[] v : tags.values()) {
            total += (v.length + 3) & ~3;
        }
        byte[] out = new byte[total];
        out[0] = (byte) (total >>> 24);
        out[1] = (byte) (total >>> 16);
        out[2] = (byte) (total >>> 8);
        out[3] = (byte) total;
        out[8] = 0x02;
        out[9] = 0x10;
        out[12] = 'm'; out[13] = 'n'; out[14] = 't'; out[15] = 'r';
        out[16] = 'R'; out[17] = 'G'; out[18] = 'B'; out[19] = ' ';
        out[20] = 'X'; out[21] = 'Y'; out[22] = 'Z'; out[23] = ' ';
        out[36] = 'a'; out[37] = 'c'; out[38] = 's'; out[39] = 'p';
        putFixed(out, 68, D50_X);
        putFixed(out, 72, D50_Y);
        putFixed(out, 76, D50_Z);
        out[128] = (byte) (tagCount >>> 24);
        out[129] = (byte) (tagCount >>> 16);
        out[130] = (byte) (tagCount >>> 8);
        out[131] = (byte) tagCount;
        int entry = 132;
        int offset = 128 + tableSize;
        for (Map.Entry<Integer, byte[]> e : tags.entrySet()) {
            int sig = e.getKey();
            byte[] val = e.getValue();
            out[entry] = (byte) (sig >>> 24);
            out[entry + 1] = (byte) (sig >>> 16);
            out[entry + 2] = (byte) (sig >>> 8);
            out[entry + 3] = (byte) sig;
            out[entry + 4] = (byte) (offset >>> 24);
            out[entry + 5] = (byte) (offset >>> 16);
            out[entry + 6] = (byte) (offset >>> 8);
            out[entry + 7] = (byte) offset;
            int size = val.length;
            out[entry + 8] = (byte) (size >>> 24);
            out[entry + 9] = (byte) (size >>> 16);
            out[entry + 10] = (byte) (size >>> 8);
            out[entry + 11] = (byte) size;
            System.arraycopy(val, 0, out, offset, size);
            offset += (size + 3) & ~3;
            entry += 12;
        }
        return out;
    }
}
