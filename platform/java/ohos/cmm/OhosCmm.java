package ohos.cmm;

import java.awt.color.ICC_Profile;
import sun.java2d.cmm.ColorTransform;
import sun.java2d.cmm.PCMM;
import sun.java2d.cmm.Profile;

/** identity CMM：不依赖 lcms native，profile 数据直通。 */
public class OhosCmm implements PCMM {
    @Override public Profile loadProfile(byte[] data) { return new OhosProfile(data); }
    @Override public byte[] getProfileData(Profile p) {
        System.err.println("[OHOS-CMM] getProfileData called, arg=" + (p == null ? "null" : p.getClass().getName()));
        if (p instanceof OhosProfile) {
            byte[] d = ((OhosProfile) p).getData();
            System.err.println("[OHOS-CMM] returning " + (d == null ? "null" : d.length + " bytes"));
            return d;
        }
        System.err.println("[OHOS-CMM] unknown profile type, returning empty");
        return new byte[0];
    }
    @Override public byte[] getTagData(Profile p, int tagSignature) { return null; }
    @Override public void setTagData(Profile p, int tagSignature, byte[] data) { }
    @Override public ColorTransform createTransform(ICC_Profile profile, int renderType, int inFormatter) {
        return new OhosColorTransform(profile == null ? 3 : profile.getNumComponents(),
                                      profile == null ? 3 : profile.getNumComponents());
    }
    @Override public ColorTransform createTransform(ColorTransform[] transforms) {
        return new OhosColorTransform(3, 3);
    }
}