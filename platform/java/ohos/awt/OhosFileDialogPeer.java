package ohos.awt;

import java.awt.FileDialog;
import java.awt.peer.FileDialogPeer;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * FileDialogPeer backed by {@link JFileChooser}.
 *
 * <p>REW's open/save workflow runs through {@code FileDialog}; a Proxy stub that returns null
 * from {@code getDirectory()} / {@code getFile()} makes those dialogs silently do nothing.
 * This peer shows a real Swing chooser on the EDT and writes the selection back to the target.</p>
 */
public final class OhosFileDialogPeer implements InvocationHandler {

    private final FileDialog target;

    private OhosFileDialogPeer(FileDialog target) {
        this.target = target;
    }

    public static FileDialogPeer create(FileDialog target) {
        return (FileDialogPeer) Proxy.newProxyInstance(
                OhosFileDialogPeer.class.getClassLoader(),
                new Class<?>[] { FileDialogPeer.class },
                new OhosFileDialogPeer(target));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String name = method.getName();
        Class<?> rt = method.getReturnType();
        try {
            if ("setVisible".equals(name)) {
                boolean visible = args != null && args.length > 0 && Boolean.TRUE.equals(args[0]);
                if (visible) {
                    showChooser();
                }
                return null;
            }
            if ("show".equals(name)) {
                showChooser();
                return null;
            }
            if ("dispose".equals(name)) {
                return null;
            }
            if ("setTitle".equals(name) || "setDirectory".equals(name) || "setFile".equals(name)
                    || "setFilenameFilter".equals(name) || "setBounds".equals(name)
                    || "setResizable".equals(name) || "blockWindows".equals(name)
                    || "toFront".equals(name) || "toBack".equals(name)
                    || "updateAlwaysOnTopState".equals(name)) {
                return null;
            }
            if ("getLocationOnScreen".equals(name)) {
                return new java.awt.Point(0, 0);
            }
        } catch (Throwable ignored) {
            // fall through to default values
        }
        if (rt == void.class) {
            return null;
        }
        if (rt == boolean.class) {
            return Boolean.FALSE;
        }
        if (rt == int.class) {
            return Integer.valueOf(0);
        }
        if (rt == long.class) {
            return Long.valueOf(0L);
        }
        if (rt == java.awt.Insets.class) {
            return new java.awt.Insets(0, 0, 0, 0);
        }
        if (rt == java.awt.Dimension.class) {
            return new java.awt.Dimension(0, 0);
        }
        if (rt == java.awt.Rectangle.class) {
            return new java.awt.Rectangle(0, 0, 0, 0);
        }
        return null;
    }

    private void showChooser() {
        Runnable r = () -> {
            try {
                JFileChooser chooser = new JFileChooser();
                String dir = target.getDirectory();
                String file0 = target.getFile();
                if (dir != null && !dir.isEmpty()) {
                    File d = new File(dir);
                    if (d.isDirectory()) {
                        chooser.setCurrentDirectory(d);
                    }
                }
                if (file0 != null && !file0.isEmpty()) {
                    chooser.setSelectedFile(new File(file0));
                }
                String title = target.getTitle();
                if (title != null && !title.isEmpty()) {
                    chooser.setDialogTitle(title);
                }
                if (target.getMode() == FileDialog.SAVE) {
                    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
                } else {
                    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                }
                chooser.setFileFilter(new FileNameExtensionFilter(
                        "REW measurement (*.mdat)", "mdat"));
                chooser.setAcceptAllFileFilterUsed(true);
                int result = chooser.showDialog(null,
                        target.getMode() == FileDialog.SAVE ? "Save" : "Open");
                if (result == JFileChooser.APPROVE_OPTION) {
                    File sel = chooser.getSelectedFile();
                    if (sel != null) {
                        target.setDirectory(sel.getParent());
                        target.setFile(sel.getName());
                    }
                } else {
                    target.setFile(null);
                }
            } catch (Throwable t) {
                target.setFile(null);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (Throwable ignored) {
                // ignore
            }
        }
    }
}
