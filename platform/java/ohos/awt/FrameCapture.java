package ohos.awt;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import javax.imageio.ImageIO;

/**
 * Starts a Swing application's main() on a worker thread, waits for the UI to settle,
 * then paints all visible frames into one offscreen image and writes it as PNG.
 */
public final class FrameCapture {
    private FrameCapture() {}

    public static String runAndCapture(String mainClass, String outPath, int waitMs, int width, int height) {
        StringBuilder log = new StringBuilder();
        try {
            Class<?> cls = Class.forName(mainClass);
            Method main = cls.getMethod("main", String[].class);
            Thread t = new Thread(() -> {
                try {
                    main.invoke(null, (Object) new String[0]);
                } catch (Throwable ignored) {
                }
            }, "REW-main");
            t.setDaemon(true);
            t.start();
            log.append("main started; ");
            Thread.sleep(waitMs);

            Frame[] frames = Frame.getFrames();
            int visible = 0;
            for (Frame f : frames) {
                if (f.isVisible()) {
                    visible++;
                }
            }
            log.append("frames=").append(frames.length).append(" visible=").append(visible).append("; ");

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            final int[] painted = {0};
            EventQueue.invokeAndWait(() -> {
                for (Frame f : Frame.getFrames()) {
                    if (f.isVisible()) {
                        g.translate(f.getX(), f.getY());
                        f.paint(g);
                        g.translate(-f.getX(), -f.getY());
                        painted[0]++;
                    }
                }
            });
            g.dispose();
            boolean ok = ImageIO.write(img, "png", new File(outPath));
            log.append("painted=").append(painted[0]).append(" write=").append(ok);
            return log.toString();
        } catch (Throwable e) {
            return "ERROR: " + e;
        }
    }
}
