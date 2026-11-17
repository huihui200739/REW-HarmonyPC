package ohos.awt;

import java.awt.Component;
import java.awt.Window;
import java.awt.peer.KeyboardFocusManagerPeer;

/** 最小焦点管理器 peer：只记录焦点状态，输入事件后端接入后再补真实分发。 */
public class OhosKeyboardFocusManagerPeer implements KeyboardFocusManagerPeer {
    private volatile Window focusedWindow;
    private volatile Component focusOwner;

    @Override public void setCurrentFocusedWindow(Window win) { this.focusedWindow = win; }
    @Override public Window getCurrentFocusedWindow() { return focusedWindow; }
    @Override public void setCurrentFocusOwner(Component comp) { this.focusOwner = comp; }
    @Override public Component getCurrentFocusOwner() { return focusOwner; }
    @Override public void clearGlobalFocusOwner(Window activeWindow) { this.focusOwner = null; }
}
