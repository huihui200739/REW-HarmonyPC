package ohos.cmm;

import sun.java2d.cmm.Profile;

/** identity CMM 的 profile 载体：只保存原始 ICC 数据。 */
public class OhosProfile extends Profile {
    private final byte[] data;

    public OhosProfile(byte[] data) {
        super(0L);
        this.data = data == null ? new byte[0] : data.clone();
    }

    public byte[] getData() { return data.clone(); }
}
