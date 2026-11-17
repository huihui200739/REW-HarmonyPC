/* OHOS JVM 加载探针：不依赖 java launcher，直接 JNI_CreateJavaVM 启动 java.base，
 * 读取 System.getProperty("java.version") 与 java.home，用于设备侧 P1-a4 门禁。 */
#include <jni.h>
#include <stdio.h>
#include <string.h>

static int get_prop(JNIEnv *env, const char *key, char *out, size_t n) {
    jclass sys = (*env)->FindClass(env, "java/lang/System");
    if (!sys) return -1;
    jmethodID mid = (*env)->GetStaticMethodID(env, sys, "getProperty",
                                              "(Ljava/lang/String;)Ljava/lang/String;");
    if (!mid) return -2;
    jstring k = (*env)->NewStringUTF(env, key);
    jstring v = (jstring)(*env)->CallStaticObjectMethod(env, sys, mid, k);
    if (!v) return -3;
    const char *s = (*env)->GetStringUTFChars(env, v, NULL);
    if (!s) return -4;
    snprintf(out, n, "%s", s);
    (*env)->ReleaseStringUTFChars(env, v, s);
    return 0;
}

int main(void) {
    JavaVM *vm = NULL;
    JNIEnv *env = NULL;
    JavaVMInitArgs args;
    JavaVMOption opts[3];
    int n = 0;

    opts[n++].optionString = "-Xint";
    opts[n++].optionString = "-Djava.class.path=.";

    args.version = JNI_VERSION_1_8;
    args.nOptions = n;
    args.options = opts;
    args.ignoreUnrecognized = JNI_TRUE;

    jint rc = JNI_CreateJavaVM(&vm, (void **)&env, &args);
    if (rc != JNI_OK) {
        printf("FAIL JNI_CreateJavaVM rc=%d\n", (int)rc);
        return 1;
    }
    printf("OK  JVM created\n");

    char buf[256];
    if (get_prop(env, "java.version", buf, sizeof buf) == 0) printf("java.version=%s\n", buf);
    if (get_prop(env, "java.vm.name", buf, sizeof buf) == 0) printf("java.vm.name=%s\n", buf);
    if (get_prop(env, "java.home", buf, sizeof buf) == 0) printf("java.home=%s\n", buf);
    if (get_prop(env, "os.arch", buf, sizeof buf) == 0) printf("os.arch=%s\n", buf);
    if (get_prop(env, "java.awt.headless", buf, sizeof buf) == 0) printf("java.awt.headless=%s\n", buf);

    /* 类加载冒烟：加载一个真实类并取名字 */
    jclass c = (*env)->FindClass(env, "java/util/ArrayList");
    printf("FindClass(ArrayList)=%s\n", c ? "OK" : "FAIL");
    if (c) {
        jclass objc = (*env)->GetObjectClass(env, NULL);
        (void)objc;
    }

    (*vm)->DestroyJavaVM(vm);
    printf("DONE\n");
    return 0;
}
