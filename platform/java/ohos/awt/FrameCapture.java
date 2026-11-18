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

    private static void dumpTree(java.awt.Component c, int depth, StringBuilder sb) {
        if (depth > 3) {
            return;
        }
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        sb.append(c.getClass().getName())
          .append(" ").append(c.getWidth()).append("x").append(c.getHeight())
          .append(" vis=").append(c.isVisible())
          .append(" valid=").append(c.isValid())
          .append("; ");
        if (c instanceof java.awt.Container) {
            java.awt.Component[] kids = ((java.awt.Container) c).getComponents();
            sb.append("kids=").append(kids.length);
            sb.append("\n");
            for (java.awt.Component ch : kids) {
                dumpTree(ch, depth + 1, sb);
            }
        } else {
            sb.append("\n");
        }
    }

    public static String runAndCapture(String mainClass, String outPath, int waitMs, int width, int height) {
        StringBuilder log = new StringBuilder();
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }
            Class<?> cls = Class.forName(mainClass, true, cl);
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
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, width, height);
            g.setColor(java.awt.Color.BLACK);
            final int[] painted = {0};
            final StringBuilder detail = new StringBuilder();
            EventQueue.invokeAndWait(() -> {
                // NOTE: our FramePeer is a stub, so isVisible() is always false and
                // setSize() only changes the Java-side fields - force a real size here.
                for (Frame f : Frame.getFrames()) {
                    f.setSize(width, height);
                    f.doLayout();
                    f.validate();
                    java.awt.Container root = (f instanceof javax.swing.JFrame)
                            ? ((javax.swing.JFrame) f).getContentPane() : f;
                    if (root != null) {
                        root.doLayout();
                    }
                    StringBuilder tree = new StringBuilder();
                    dumpTree(f, 0, tree);
                    detail.append("{").append(tree).append("}");
                    detail.append("[").append(f.getTitle())
                          .append(" ").append(f.getWidth()).append("x").append(f.getHeight())
                          .append(" comps=").append(f.getComponentCount())
                          .append(" valid=").append(f.isValid()).append("] ");
                    f.paintAll(g);
                    painted[0]++;
                }
            });
            g.dispose();
            boolean ok = ImageIO.write(img, "png", new File(outPath));
            try {
                java.nio.file.Files.writeString(java.nio.file.Path.of(outPath + ".tree.txt"),
                        detail.toString(), java.nio.charset.StandardCharsets.UTF_8);
            } catch (Throwable ignored) {
            }
            log.append(detail).append("painted=").append(painted[0]).append(" write=").append(ok);
            return log.toString();
        } catch (Throwable e) {
            return "ERROR: " + e;
        }
    }
}
