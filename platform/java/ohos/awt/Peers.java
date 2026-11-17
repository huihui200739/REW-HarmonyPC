package ohos.awt;

import java.lang.reflect.Proxy;

/** Minimal peer factory: default values, with sensible objects for common return types. */
public final class Peers {
    private Peers() {}

    @SuppressWarnings("unchecked")
    public static <T> T make(Class<T> itf) {
        return (T) Proxy.newProxyInstance(
            Peers.class.getClassLoader(),
            new Class<?>[] { itf },
            (proxy, method, args) -> {
                Class<?> rt = method.getReturnType();
                if (rt == void.class) return null;
                if (rt == boolean.class) return false;
                if (rt == int.class) return 0;
                if (rt == long.class) return 0L;
                if (rt == float.class) return 0f;
                if (rt == double.class) return 0d;
                if (rt == char.class) return (char) 0;
                if (rt == byte.class) return (byte) 0;
                if (rt == short.class) return (short) 0;
                if (rt == java.awt.Insets.class) return new java.awt.Insets(0, 0, 0, 0);
                if (rt == java.awt.Dimension.class) return new java.awt.Dimension(0, 0);
                if (rt == java.awt.Rectangle.class) return new java.awt.Rectangle(0, 0, 0, 0);
                if (rt == java.awt.Point.class) return new java.awt.Point(0, 0);
                if (rt == java.awt.Font.class) return new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12);
                if (rt == java.awt.Color.class) return java.awt.Color.BLACK;
                if (rt == java.awt.Cursor.class) return java.awt.Cursor.getDefaultCursor();
                if (rt == java.awt.Image.class) return null;
                return null;
            });
    }
}
