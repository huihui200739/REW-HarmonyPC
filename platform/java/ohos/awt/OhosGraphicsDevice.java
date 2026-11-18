package ohos.awt;

import java.awt.*;

public class OhosGraphicsDevice extends GraphicsDevice {
    private final GraphicsConfiguration config;
    private final int index;
    private final DisplayMode mode = new DisplayMode(1920, 1080, 32, DisplayMode.REFRESH_RATE_UNKNOWN);

    public OhosGraphicsDevice(GraphicsEnvironment env) {
        this(env, 0);
    }

    public OhosGraphicsDevice(GraphicsEnvironment env, int index) {
        this.index = index;
        this.config = new OhosGraphicsConfiguration(this);
    }

    public int getIndex() { return index; }

    @Override public int getType() { return TYPE_RASTER_SCREEN; }
    @Override public String getIDstring() { return "OHOS-Display-" + index; }
    @Override public GraphicsConfiguration[] getConfigurations() { return new GraphicsConfiguration[] { config }; }
    @Override public GraphicsConfiguration getDefaultConfiguration() { return config; }
    @Override public DisplayMode getDisplayMode() { return mode; }
    @Override public DisplayMode[] getDisplayModes() { return new DisplayMode[] { mode }; }
    @Override public void setDisplayMode(DisplayMode dm) { }
    @Override public boolean isDisplayChangeSupported() { return false; }
    @Override public boolean isFullScreenSupported() { return true; }
    @Override public void setFullScreenWindow(Window w) { }
    @Override public Window getFullScreenWindow() { return null; }
}
