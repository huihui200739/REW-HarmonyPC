package ohos.awt;

/** Bridges Java2D rendering output to the OHOS XComponent surface. */
public final class SurfaceBridge {
    private SurfaceBridge() {}

    private static native void blit(int[] argb, int width, int height);

    private static volatile boolean warned = false;

    /** Sends an ARGB pixel buffer to the native surface. Silently no-ops if unavailable. */
    public static void push(int[] argb, int width, int height) {
        try {
            blit(argb, width, height);
        } catch (Throwable t) {
            if (!warned) {
                warned = true;
                System.err.println("[OHOS-SD] SurfaceBridge.push failed: " + t);
            }
        }
    }
}
