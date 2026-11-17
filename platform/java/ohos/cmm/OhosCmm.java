package ohos.cmm;

import java.awt.color.ICC_Profile;
import sun.java2d.cmm.ColorTransform;
import sun.java2d.cmm.PCMM;
import sun.java2d.cmm.Profile;

/** identity CMM：不依赖 lcms native，profile 数据直通。 */
public class OhosCmm implements PCMM {
    @Override public Profile loadProfile(byte[] data) { return new OhosProfile(data); }
    @Override public byte[] getProfileData(Profile p) {
        return (p instanceof OhosProfile) ? ((OhosProfile) p).getData() : new byte[0];
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
