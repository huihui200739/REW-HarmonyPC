package ohos.awt;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.MouseEvent;

/** Injects OHOS touch events into the AWT event queue. */
public final class InputBridge {
    private InputBridge() {}

    private static Component target() {
        Frame[] frames = Frame.getFrames();
        for (int i = frames.length - 1; i >= 0; i--) {
            Frame f = frames[i];
            if (f.isShowing() || f.getWidth() > 0) {
                return f;
            }
        }
        return frames.length > 0 ? frames[frames.length - 1] : null;
    }

    /**
     * @param type 0=press 1=release 2=move
     */
    public static void dispatch(int type, int x, int y) {
        Component c = target();
        if (c == null) {
            return;
        }
        int id;
        switch (type) {
            case 0:
                id = MouseEvent.MOUSE_PRESSED;
                break;
            case 1:
                id = MouseEvent.MOUSE_RELEASED;
                break;
            default:
                id = MouseEvent.MOUSE_MOVED;
                break;
        }
        MouseEvent ev = new MouseEvent(c, id, System.currentTimeMillis(),
                0, x, y, 1, false, MouseEvent.BUTTON1);
        EventQueue.invokeLater(() -> c.dispatchEvent(ev));
    }
}
