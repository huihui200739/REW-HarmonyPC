package ohos.cmm;

import java.awt.color.ICC_Profile;
import sun.java2d.cmm.ColorTransform;
import sun.java2d.cmm.PCMM;
import sun.java2d.cmm.Profile;

public class OhosCmm implements PCMM {
    @Override public Profile loadProfile(byte[] data) { return new OhosProfile(data); }
    @Override public byte[] getProfileData(Profile p) {
        System.err.println("[OHOS-CMM] getProfileData called, arg=" + (p == null ? "null" : p.getClass().getName()));
        if (p instanceof OhosProfile) {
            byte[] d = ((OhosProfile) p).getData();
            System.err.println("[OHOS-CMM] returning " + (d == null ? "null" : d.length + " bytes"));
            return d;
        }
        System.err.println("[OHOS-CMM] unknown profile type, returning sRGB profile");
        return new OhosProfile().getData();
    }
    private static final int IC_SIG_HEAD = 0x68656164;

    @Override public byte[] getTagData(Profile p, int tagSignature) {
        if (tagSignature == IC_SIG_HEAD) {
            byte[] all = getProfileData(p);
            if (all != null && all.length >= 128) {
                byte[] head = new byte[128];
                System.arraycopy(all, 0, head, 0, 128);
                return head;
            }
            return new OhosProfile().getData();
        }
        return null;
    }
    @Override public void setTagData(Profile p, int tagSignature, byte[] data) { }
    @Override public ColorTransform createTransform(ICC_Profile profile, int renderType, int inFormatter) {
        return new OhosColorTransform(profile == null ? 3 : profile.getNumComponents(),
                                      profile == null ? 3 : profile.getNumComponents());
    }
    @Override public ColorTransform createTransform(ColorTransform[] transforms) {
        return new OhosColorTransform(3, 3);
    }
}