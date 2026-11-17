package ohos.awt;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;

public class OhosGraphicsConfiguration extends GraphicsConfiguration {
    private final GraphicsDevice device;

    public OhosGraphicsConfiguration(GraphicsDevice device) { this.device = device; }

    @Override public GraphicsDevice getDevice() { return device; }
    @Override public ColorModel getColorModel() { return ColorModel.getRGBdefault(); }
    @Override public ColorModel getColorModel(int transparency) { return ColorModel.getRGBdefault(); }
    @Override public AffineTransform getDefaultTransform() { return new AffineTransform(); }
    @Override public AffineTransform getNormalizingTransform() { return new AffineTransform(); }
    @Override public Rectangle getBounds() { return new Rectangle(0, 0, 1920, 1080); }
}