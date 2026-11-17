package ohos.awt;

import java.lang.reflect.Proxy;

/** Minimal peer factory: all methods return default values so the component tree can be built. */
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
                return null;
            });
    }
}
