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

    private static void collectText(java.awt.Component c, StringBuilder sb, int depth) {
        if (depth > 12) {
            return;
        }
        String txt = null;
        if (c instanceof javax.swing.JLabel) {
            txt = ((javax.swing.JLabel) c).getText();
        } else if (c instanceof javax.swing.AbstractButton) {
            txt = ((javax.swing.AbstractButton) c).getText();
        } else if (c instanceof javax.swing.text.JTextComponent) {
            txt = ((javax.swing.text.JTextComponent) c).getText();
        }
        if (txt != null && !txt.isEmpty()) {
            sb.append(txt).append(" | ");
        }
        if (c instanceof java.awt.Container) {
            for (java.awt.Component ch : ((java.awt.Container) c).getComponents()) {
                collectText(ch, sb, depth + 1);
            }
        }
    }

    private static int clickClose(java.awt.Component c, int depth) {
        if (depth > 12) {
            return 0;
        }
        int n = 0;
        if (c instanceof javax.swing.AbstractButton) {
            String t = ((javax.swing.AbstractButton) c).getText();
            if (t != null && (t.contains("Close") || t.equals("OK") || t.contains("Dismiss"))) {
                ((javax.swing.AbstractButton) c).doClick(10);
                n++;
            }
        }
        if (c instanceof java.awt.Container) {
            for (java.awt.Component ch : ((java.awt.Container) c).getComponents()) {
                n += clickClose(ch, depth + 1);
            }
        }
        return n;
    }

    private static void makeTreeVisible(java.awt.Component c) {
        c.setVisible(true);
        if (c instanceof java.awt.Container) {
            for (java.awt.Component ch : ((java.awt.Container) c).getComponents()) {
                makeTreeVisible(ch);
            }
        }
    }

    private static void dumpTree(java.awt.Component c, int depth, StringBuilder sb) {
        if (depth > 10) {
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
            final String[] mainError = {null};
            Thread t = new Thread(() -> {
                try {
                    main.invoke(null, (Object) new String[0]);
                } catch (Throwable e) {
                    java.io.StringWriter sw = new java.io.StringWriter();
                    e.printStackTrace(new java.io.PrintWriter(sw));
                    mainError[0] = sw.toString();
                }
            }, "REW-main");
            t.setDaemon(true);
            t.start();
            log.append("main started; ");
            Thread.sleep(waitMs);

            // Do not click buttons on a modal dialog: doClick() sleeps and may re-enter
            // invokeAndWait on the EDT, deadlocking the probe. Hiding is enough here.
            final int[] closed = {0};
            EventQueue.invokeLater(() -> {
                for (java.awt.Window win : java.awt.Window.getWindows()) {
                    if (win instanceof java.awt.Dialog) {
                        try {
                            win.setVisible(false);
                            ((java.awt.Dialog) win).dispose();
                            closed[0]++;
                        } catch (Throwable ignored) {
                        }
                    }
                }
            });
            Thread.sleep(4000);
            log.append("dialogsHidden=").append(closed[0]).append("; ");
            Thread.sleep(3000);
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
                    makeTreeVisible(f);
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
                    f.printAll(g);
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
            if (mainError[0] != null) {
                log.append("MAIN-ERROR: ").append(mainError[0]).append("\n");
            } else {
                log.append("MAIN-OK(no exception)\n");
            }
            for (java.util.Map.Entry<Thread, StackTraceElement[]> en
                    : Thread.getAllStackTraces().entrySet()) {
                String nm = en.getKey().getName();
                if (nm.contains("REW-main") || nm.startsWith("AWT-EventQueue")) {
                    log.append("-- ").append(nm).append(" ").append(en.getKey().getState()).append("\n");
                    for (StackTraceElement el : en.getValue()) {
                        log.append("   at ").append(el).append("\n");
                    }
                }
            }
            try (java.io.BufferedWriter w = java.nio.file.Files.newBufferedWriter(
                    java.nio.file.Path.of(outPath + ".stack.txt"),
                    java.nio.charset.StandardCharsets.UTF_8)) {
                if (mainError[0] != null) {
                    w.write("MAIN-ERROR:");
                    w.newLine();
                    w.write(mainError[0]);
                } else {
                    w.write("MAIN-OK (no exception thrown yet)");
                w.newLine();
                StringBuilder dlgText = new StringBuilder();
                for (java.awt.Window win : java.awt.Window.getWindows()) {
                    dlgText.append("[WINDOW ").append(win.getClass().getName())
                           .append(" title=").append(win instanceof java.awt.Dialog
                                   ? ((java.awt.Dialog) win).getTitle() : "")
                           .append("] ");
                    collectText(win, dlgText, 0);
                    dlgText.append("\n");
                }
                w.write(dlgText.toString());
                }
                w.newLine();
                for (java.util.Map.Entry<Thread, StackTraceElement[]> en
                        : Thread.getAllStackTraces().entrySet()) {
                    String nm = en.getKey().getName();
                    if (nm.contains("REW-main") || nm.startsWith("AWT-EventQueue")
                            || nm.startsWith("AWT-")) {
                        w.write("-- " + nm + " " + en.getKey().getState());
                        w.newLine();
                        for (StackTraceElement el : en.getValue()) {
                            w.write("   at " + el);
                            w.newLine();
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
            return log.toString();
        } catch (Throwable e) {
            return "ERROR: " + e;
        }
    }
}
