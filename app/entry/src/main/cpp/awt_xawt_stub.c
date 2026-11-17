/* libawt_xawt.so 兼容壳：OHOS 没有 X11，真正的实现是 ohos.awt 后端。
 * 这个壳只为满足 System.loadLibrary("awt_xawt")，不提供任何 X11 功能。 */
#include <jni.h>

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void)vm; (void)reserved;
    return JNI_VERSION_1_8;
}
