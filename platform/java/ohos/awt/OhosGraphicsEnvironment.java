package ohos.awt;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;

/**
 * Minimal graphics environment: single screen, software rendering via SunGraphics2D.
 * SurfaceData is cached per raster: creating it twice for the same raster makes the
 * native layer complain ("Attempting to set SurfaceData ops twice").
 */
public class OhosGraphicsEnvironment extends GraphicsEnvironment {
    private final OhosGraphicsDevice device = new OhosGraphicsDevice(this, 0);
    // REW indexes screen devices with [1]; expose a second virtual device so it does not
    // fail with ArrayIndexOutOfBoundsException on a single-screen port.
    private final OhosGraphicsDevice device1 = new OhosGraphicsDevice(this, 1);
    private static volatile Font defaultFont;
    private static final Map<WritableRaster, SurfaceData> SURFACE_CACHE = new WeakHashMap<>();

    private static synchronized Font font() {
        if (defaultFont == null) {
            defaultFont = new Font(Font.DIALOG, Font.PLAIN, 12);
        }
        return defaultFont;
    }

    private static SurfaceData surfaceFor(BufferedImage img) {
        WritableRaster raster = img.getRaster();
        synchronized (SURFACE_CACHE) {
            SurfaceData sd = SURFACE_CACHE.get(raster);
            if (sd == null) {
                sd = SurfaceData.getPrimarySurfaceData(img);
                SURFACE_CACHE.put(raster, sd);
            }
            return sd;
        }
    }

    @Override public GraphicsDevice[] getScreenDevices() { return new GraphicsDevice[] { device, device1 }; }
    @Override public GraphicsDevice getDefaultScreenDevice() { return device; }

    @Override public Graphics2D createGraphics(BufferedImage img) {
        if (img == null) {
            throw new NullPointerException("BufferedImage cannot be null");
        }
        return new SunGraphics2D(surfaceFor(img), Color.white, Color.black, font());
    }

    @Override public Font[] getAllFonts() { return new Font[0]; }
    @Override public String[] getAvailableFontFamilyNames() {
        return new String[] { "Dialog", "SansSerif", "Serif", "Monospaced" };
    }
    @Override public String[] getAvailableFontFamilyNames(Locale locale) { return getAvailableFontFamilyNames(); }
}
