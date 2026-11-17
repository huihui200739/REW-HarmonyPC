package ohos.awt;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Locale;

/** 最小图形环境：单屏、固定 DPI。绘制后端后续接入 OHOS Surface。 */
public class OhosGraphicsEnvironment extends GraphicsEnvironment {
    private final OhosGraphicsDevice device = new OhosGraphicsDevice(this);

    @Override public GraphicsDevice[] getScreenDevices() { return new GraphicsDevice[] { device }; }
    @Override public GraphicsDevice getDefaultScreenDevice() { return device; }
    @Override public Graphics2D createGraphics(BufferedImage img) { return img.createGraphics(); }
    @Override public Font[] getAllFonts() { return new Font[0]; }
    @Override public String[] getAvailableFontFamilyNames() { return new String[] { "Dialog", "SansSerif", "Serif", "Monospaced" }; }
    @Override public String[] getAvailableFontFamilyNames(Locale locale) { return getAvailableFontFamilyNames(); }
}
