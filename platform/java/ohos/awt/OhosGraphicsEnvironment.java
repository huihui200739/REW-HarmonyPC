package ohos.awt;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.Locale;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;

public class OhosGraphicsEnvironment extends GraphicsEnvironment {
    private final OhosGraphicsDevice device = new OhosGraphicsDevice(this);
    private static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);

    @Override public GraphicsDevice[] getScreenDevices() { return new GraphicsDevice[] { device }; }
    @Override public GraphicsDevice getDefaultScreenDevice() { return device; }
    @Override public Graphics2D createGraphics(BufferedImage img) {
        SurfaceData sd = SurfaceData.getPrimarySurfaceData(img);
        return new SunGraphics2D(sd, Color.white, Color.black, DEFAULT_FONT);
    }
    @Override public Font[] getAllFonts() { return new Font[0]; }
    @Override public String[] getAvailableFontFamilyNames() {
        return new String[] { "Dialog", "SansSerif", "Serif", "Monospaced" };
    }
    @Override public String[] getAvailableFontFamilyNames(Locale locale) { return getAvailableFontFamilyNames(); }
}
