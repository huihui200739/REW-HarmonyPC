package ohos.cmm;

import sun.java2d.cmm.CMMServiceProvider;
import sun.java2d.cmm.PCMM;

/** 通过 -Dsun.java2d.cmm=ohos.cmm.OhosCmmServiceProvider 注入。 */
public class OhosCmmServiceProvider extends CMMServiceProvider {
    private final PCMM cmm = new OhosCmm();
    @Override protected PCMM getModule() { return cmm; }
}
