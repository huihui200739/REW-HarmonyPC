/* 无 X11 的 AWT native 兼容层：为 java.desktop 中声明 native initIDs 的类
 * 提供空实现，使组件类可以完成静态初始化。真正的绘制/事件由后续后端实现。 */
#include <jni.h>

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void)vm; (void)reserved;
    return JNI_VERSION_1_8;
}

#define EMPTY_INITIDS(name)     JNIEXPORT void JNICALL Java_java_awt_##name##_initIDs(JNIEnv *env, jclass cls) { (void)env; (void)cls; }

EMPTY_INITIDS(AWTEvent)
EMPTY_INITIDS(Button)
EMPTY_INITIDS(Checkbox)
EMPTY_INITIDS(Component)
EMPTY_INITIDS(Container)
EMPTY_INITIDS(Cursor)
EMPTY_INITIDS(Dialog)
EMPTY_INITIDS(Event)
EMPTY_INITIDS(FileDialog)
EMPTY_INITIDS(Frame)
EMPTY_INITIDS(Insets)
EMPTY_INITIDS(KeyboardFocusManager)
EMPTY_INITIDS(Menu)
EMPTY_INITIDS(MenuComponent)
EMPTY_INITIDS(MenuItem)
EMPTY_INITIDS(ScrollPane)
EMPTY_INITIDS(Scrollbar)
EMPTY_INITIDS(TextArea)
EMPTY_INITIDS(TextField)
EMPTY_INITIDS(TrayIcon)
EMPTY_INITIDS(Window)

JNIEXPORT void JNICALL Java_sun_awt_X11_XToolkit_initIDs(JNIEnv *env, jclass cls) { (void)env; (void)cls; }
JNIEXPORT jlong JNICALL Java_sun_awt_X11_XToolkit_getTrayIconDisplayTimeout(JNIEnv *env, jclass cls) { (void)env; (void)cls; return 0; }
JNIEXPORT jlong JNICALL Java_sun_awt_X11_XToolkit_getDefaultXColormap(JNIEnv *env, jclass cls) { (void)env; (void)cls; return 0; }
JNIEXPORT void JNICALL Java_sun_awt_X11_XToolkit_nativeLoadSystemColors(JNIEnv *env, jclass cls, jintArray arr) { (void)env; (void)cls; (void)arr; }
JNIEXPORT jboolean JNICALL Java_sun_awt_X11GraphicsEnvironment_initDisplay(JNIEnv *env, jclass cls, jboolean headlessCheck) {
    (void)env; (void)cls; (void)headlessCheck;
    return JNI_TRUE;
}
