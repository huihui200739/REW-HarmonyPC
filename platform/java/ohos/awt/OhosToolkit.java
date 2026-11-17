package ohos.awt;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/** 最小 Toolkit：先让 AWT 在无 X11 的 OHOS 上完成初始化，绘制能力后续接入。 */
public class OhosToolkit extends Toolkit {
    private static final Dimension SCREEN = new Dimension(1920, 1080);
    private static final int DPI = 160;
    private static final String[] FONTS = {"Dialog", "SansSerif", "Serif", "Monospaced"};

    @Override public Dimension getScreenSize() { return SCREEN; }
    @Override public int getScreenResolution() { return DPI; }
    @Override public ColorModel getColorModel() { return ColorModel.getRGBdefault(); }
    @Override public String[] getFontList() { return FONTS.clone(); }
    @Override public FontMetrics getFontMetrics(Font font) { return new BasicMetrics(font); }
    @Override public void sync() { }
    @Override public void beep() { }

    @Override public Image getImage(String filename) { return createImage(filename); }
    @Override public Image getImage(URL url) { return createImage(url); }

    @Override public Image createImage(String filename) {
        try { return ImageIO.read(new File(filename)); } catch (IOException e) { return blank(); }
    }
    @Override public Image createImage(URL url) {
        try { return ImageIO.read(url); } catch (IOException e) { return blank(); }
    }
    @Override public boolean prepareImage(Image image, int width, int height, ImageObserver observer) { return true; }
    @Override public int checkImage(Image image, int width, int height, ImageObserver observer) { return ImageObserver.ALLBITS; }
    @Override public Image createImage(ImageProducer producer) {
        return blank();
    }
    @Override public Image createImage(byte[] imagedata, int offset, int length) {
        try {
            return ImageIO.read(new java.io.ByteArrayInputStream(imagedata, offset, length));
        } catch (IOException e) { return blank(); }
    }
    private static Image blank() { return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); }

    @Override public PrintJob getPrintJob(Frame frame, String jobtitle, JobAttributes jobAttributes, PageAttributes pageAttributes) {
        return null;
    }
    @Override public PrintJob getPrintJob(Frame frame, String jobtitle, java.util.Properties props) {
        return null;
    }

    @Override public Clipboard getSystemClipboard() { return new EmptyClipboard(); }
    @Override public Clipboard getSystemSelection() { return null; }

    @Override public boolean isModalityTypeSupported(Dialog.ModalityType type) { return true; }
    @Override public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType type) { return true; }
    @Override public Map<TextAttribute, ?> mapInputMethodHighlight(java.awt.im.InputMethodHighlight highlight) { return null; }

    @Override public boolean areExtraMouseButtonsEnabled() { return false; }
    @Override public int getMaximumCursorColors() { return 256; }

    private EventQueue eventQueue;

    @Override protected EventQueue getSystemEventQueueImpl() {
        synchronized (this) {
            if (eventQueue == null) {
                try {
                    java.lang.reflect.Constructor<EventQueue> c = EventQueue.class.getDeclaredConstructor();
                    c.setAccessible(true);
                    eventQueue = c.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("cannot create EventQueue", e);
                }
            }
            return eventQueue;
        }
    }

    /** 简单度量：按字号估算。后续接入 Freetype 的 FontDesignMetrics。 */
    static final class BasicMetrics extends FontMetrics {
        BasicMetrics(Font font) { super(font); }
        @Override public int getLeading() { return 1; }
        @Override public int getAscent() { return (int) (getFont().getSize2D() * 0.8f); }
        @Override public int getDescent() { return (int) (getFont().getSize2D() * 0.2f); }
        @Override public int getMaxAdvance() { return (int) (getFont().getSize2D() * 0.6f); }
        @Override public int charWidth(int codePoint) { return (int) (getFont().getSize2D() * 0.5f); }
        @Override public int charWidth(char ch) { return (int) (getFont().getSize2D() * 0.5f); }
        @Override public int stringWidth(String s) { return s == null ? 0 : (int) (s.length() * getFont().getSize2D() * 0.5f); }
        @Override public int getHeight() { return getAscent() + getDescent() + getLeading(); }
    }

    static final class EmptyClipboard extends Clipboard {
        EmptyClipboard() { super("OHOS"); }
        @Override public synchronized Transferable getContents(Object requestor) { return null; }
        @Override public synchronized void setContents(Transferable contents, ClipboardOwner owner) { }
    }
}