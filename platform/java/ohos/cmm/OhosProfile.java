package ohos.cmm;

import sun.java2d.cmm.Profile;

/** identity CMM 的 profile 载体：保存原始 ICC 数据；无数据时给一个最小合法头。 */
public class OhosProfile extends Profile {
    private static final int HEADER_SIZE = 128;
    private final byte[] data;

    public OhosProfile(byte[] data) {
        super(0L);
        this.data = (data == null || data.length < HEADER_SIZE) ? defaultHeader() : data.clone();
    }

    public byte[] getData() { return data.clone(); }

    /** 128 字节最小 ICC 头：RGB / XYZ / monitor。 */
    public static byte[] defaultHeader() {
        byte[] h = new byte[HEADER_SIZE];
        putInt(h, 0, HEADER_SIZE);
        putInt(h, 8, 0x02100000);
        putSig(h, 12, "mntr");
        putSig(h, 16, "RGB ");
        putSig(h, 20, "XYZ ");
        putInt(h, 64, 0);
        putInt(h, 68, 0x0000F6D6);
        putInt(h, 72, 0x00010000);
        putInt(h, 76, 0x0000D32D);
        putSig(h, 36, "acsp");
        return h;
    }

    private static void putInt(byte[] b, int off, int v) {
        b[off] = (byte) (v >>> 24);
        b[off + 1] = (byte) (v >>> 16);
        b[off + 2] = (byte) (v >>> 8);
        b[off + 3] = (byte) v;
    }

    private static void putSig(byte[] b, int off, String sig) {
        for (int i = 0; i < 4; i++) {
            b[off + i] = (byte) sig.charAt(i);
        }
    }
}
