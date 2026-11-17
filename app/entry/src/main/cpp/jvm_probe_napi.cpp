// N-API 探针：在 HAP 应用进程内 dlopen libjvm.so 并创建 JVM，
// 用于判定应用沙箱是否允许可执行内存（hdc shell 域已确认禁止）。
#include <napi/native_api.h>
#include <hilog/log.h>

#undef LOG_DOMAIN
#undef LOG_TAG
#define LOG_DOMAIN 0x0000
#define LOG_TAG "REWJVMN"
#include <dlfcn.h>
#include <jni.h>
#include <string>
#include <cstring>
#include <cstdio>
#include <sys/mman.h>
#include <dirent.h>
#include <unistd.h>

namespace {

using CreateJavaVmFn = jint (*)(JavaVM **, void **, void *);

std::string ExecMemProbe() {
    std::string out;
    long ps = sysconf(_SC_PAGESIZE);
    static const unsigned char kRet42[] = {0x40, 0x05, 0x80, 0x52, 0xc0, 0x03, 0x5f, 0xd6};
    void *p = mmap(nullptr, ps, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (p == MAP_FAILED) { return "mmap RW: FAIL\n"; }
    memcpy(p, kRet42, sizeof kRet42);
    out += "mmap RW: OK\n";
    if (mprotect(p, ps, PROT_READ | PROT_EXEC) != 0) {
        out += "mprotect RX: FAIL\n";
    } else {
        int (*fn)() = reinterpret_cast<int (*)()>(p);
        char buf[64];
        snprintf(buf, sizeof buf, "mprotect RX: OK (call=%d)\n", fn());
        out += buf;
    }
    munmap(p, ps);
    return out;
}

#define JLOG(...) OH_LOG_INFO(LOG_APP, __VA_ARGS__)

namespace {
void JNICALL EmptyInitIDs(JNIEnv *, jclass) {}

// LCMS 兼容层：OHOS 上暂时不启用 lcms（其 native 依赖未注册的字段 ID，会空指针崩溃）。
// 返回 0/空，让 Java 侧走异常路径而不是崩进程。
jlong JNICALL LcmsLoadProfile(JNIEnv *, jclass, jbyteArray, jobject) { return 0; }
void JNICALL LcmsInit(JNIEnv *, jclass, jclass, jclass, jclass) {}
jbyteArray JNICALL LcmsGetProfileData(JNIEnv *, jclass, jlong) { return nullptr; }
jbyteArray JNICALL LcmsGetTag(JNIEnv *, jclass, jlong, jint) { return nullptr; }
void JNICALL LcmsSetTag(JNIEnv *, jclass, jlong, jint, jbyteArray) {}
jobject JNICALL LcmsGetProfileID(JNIEnv *, jclass, jobject) { return nullptr; }

const char *kInitIdClasses[] = {
    "java/awt/AWTEvent",
    "java/awt/Button",
    "java/awt/Checkbox",
    "java/awt/CheckboxMenuItem",
    "java/awt/Choice",
    "java/awt/Color",
    "java/awt/Component",
    "java/awt/Container",
    "java/awt/Cursor",
    "java/awt/Dialog",
    "java/awt/Dimension",
    "java/awt/Event",
    "java/awt/FileDialog",
    "java/awt/Font",
    "java/awt/FontMetrics",
    "java/awt/Frame",
    "java/awt/Insets",
    "java/awt/KeyboardFocusManager",
    "java/awt/Label",
    "java/awt/Menu",
    "java/awt/MenuBar",
    "java/awt/MenuComponent",
    "java/awt/MenuItem",
    "java/awt/Rectangle",
    "java/awt/ScrollPane",
    "java/awt/ScrollPaneAdjustable",
    "java/awt/Scrollbar",
    "java/awt/TextArea",
    "java/awt/TextField",
    "java/awt/Toolkit",
    "java/awt/TrayIcon",
    "java/awt/Window",
    "java/awt/event/InputEvent",
    "java/awt/event/KeyEvent",
    "java/awt/event/MouseEvent",
    "java/awt/image/BufferedImage",
    "java/awt/image/ColorModel",
    "java/awt/image/IndexColorModel",
    "java/awt/image/Kernel",
    "java/awt/image/Raster",
    "java/awt/image/SampleModel",
    "java/awt/image/SinglePixelPackedSampleModel",
    "sun/awt/FontDescriptor",
    "sun/awt/PlatformFont",
    "sun/awt/X11/GtkFileDialogPeer",
    "sun/awt/X11/XToolkit",
    "sun/awt/X11/XWindow",
    "sun/awt/X11GraphicsConfig",
    "sun/awt/X11InputMethodBase",
    "sun/awt/image/BufImgSurfaceData",
    "sun/awt/image/ByteComponentRaster",
    "sun/awt/image/BytePackedRaster",
    "sun/awt/image/GifImageDecoder",
    "sun/awt/image/ImageRepresentation",
    "sun/awt/image/IntegerComponentRaster",
    "sun/awt/image/JPEGImageDecoder",
    "sun/awt/image/ShortComponentRaster",
    "sun/font/FreetypeFontScaler",
    "sun/font/SunFontManager",
    "sun/java2d/Disposer",
    "sun/java2d/SurfaceData",
    "sun/java2d/loops/GraphicsPrimitiveMgr",
    "sun/java2d/pipe/Region",
    "sun/java2d/pipe/ShapeSpanIterator",
    "sun/java2d/pipe/SpanClipRenderer",
    "sun/java2d/x11/X11SurfaceData",
    "sun/java2d/xr/XRBackendNative",
    "sun/java2d/xr/XRSurfaceData",
};
}  // namespace

std::string BuildClasspath(const std::string &jarDir) {
    std::string cp = ".";
    DIR *d = opendir(jarDir.c_str());
    if (!d) return cp;
    struct dirent *e;
    while ((e = readdir(d)) != nullptr) {
        std::string name = e->d_name;
        if (name.size() > 4 && name.substr(name.size() - 4) == ".jar") {
            cp += ":" + jarDir + "/" + name;
        }
    }
    closedir(d);
    return cp;
}

std::string RunJvm(const std::string &javaHome, const std::string &workDir) {
    std::string out = ExecMemProbe();

    JLOG("stage: dlopen shim");
    void *shim = dlopen("libjdkshim.so", RTLD_NOW | RTLD_GLOBAL);
    JLOG("stage: shim=%{public}s err=%{public}s", shim ? "ok" : "fail", shim ? "-" : dlerror());
    void *symJcos = dlsym(RTLD_DEFAULT, "jcos");
    JLOG("stage: dlsym jcos=%{public}s", symJcos ? "found" : "NOT FOUND");
    JLOG("stage: dlopen");
    void *lib = dlopen("libjvm.so", RTLD_NOW | RTLD_GLOBAL);
    JLOG("stage: dlopen done");
    if (!lib) {
        out += std::string("dlopen libjvm.so: FAIL ") + dlerror() + "\n";
        return out;
    }
    out += "dlopen libjvm.so: OK\n";

    auto create = reinterpret_cast<CreateJavaVmFn>(dlsym(lib, "JNI_CreateJavaVM"));
    if (!create) {
        out += "dlsym JNI_CreateJavaVM: FAIL\n";
        return out;
    }

    JavaVM *vm = nullptr;
    JNIEnv *env = nullptr;
    static char bufA[] = "-Xmx256m";
    static char bufB[] = "-Xms16m";
    static char bufC[] = "-XX:ReservedCodeCacheSize=64m";
    static char bufD[] = "-XX:+UseSerialGC";
    static char bufE[] = "-XX:-UsePerfData";
    static char bufF[] = "-Djava.awt.headless=false";
    static char bufG[] = "-Dawt.toolkit=ohos.awt.OhosToolkit";
    static char bufH[] = "-Djava.awt.graphicsenv=ohos.awt.OhosGraphicsEnvironment";
    static char bufI[] = "--add-opens=java.desktop/java.awt=ALL-UNNAMED";
    static char bufJ[] = "--add-exports=java.desktop/java.awt.peer=ALL-UNNAMED";
    static char bufK[] = "--add-exports=java.desktop/sun.awt=ALL-UNNAMED";
    std::string homeDirOpt = "-Duser.home=" + workDir + "/home";
    JavaVMOption opts[20];
    int n = 0;
    std::string homeOpt = "-Djava.home=" + javaHome;
    std::string tmpOpt = "-Djava.io.tmpdir=" + workDir;
    std::string jarDir = workDir + "/jars";
    std::string cpOpt = "-Djava.class.path=" + BuildClasspath(jarDir);
    opts[n++].optionString = const_cast<char *>(homeOpt.c_str());
    opts[n++].optionString = const_cast<char *>(cpOpt.c_str());
    opts[n++].optionString = const_cast<char *>(tmpOpt.c_str());
    opts[n++].optionString = const_cast<char *>("-Xint");
    opts[n++].optionString = bufA;
    opts[n++].optionString = bufB;
    opts[n++].optionString = bufC;
    opts[n++].optionString = bufD;
    opts[n++].optionString = bufE;
    opts[n++].optionString = bufF;
    opts[n++].optionString = bufG;
    opts[n++].optionString = bufH;
    opts[n++].optionString = bufI;
    opts[n++].optionString = bufJ;
    opts[n++].optionString = bufK;
    opts[n++].optionString = const_cast<char *>(homeDirOpt.c_str());

    JavaVMInitArgs args;
    args.version = JNI_VERSION_1_8;
    args.nOptions = n;
    args.options = opts;
    args.ignoreUnrecognized = JNI_TRUE;

    setenv("ECLIPSE_EMBEDDED_JAVA_HOME", javaHome.c_str(), 1);
    std::string errPath = workDir + "/jvm-stderr.log";
    std::string outPath = workDir + "/jvm-stdout.log";
    if (!freopen(errPath.c_str(), "w", stderr)) {}
    if (!freopen(outPath.c_str(), "w", stdout)) {}
    JLOG("stage: JNI_CreateJavaVM begin (stderr -> %{public}s)", errPath.c_str());
    jint rc = create(&vm, reinterpret_cast<void **>(&env), &args);
    JLOG("stage: JNI_CreateJavaVM rc=%{public}d", static_cast<int>(rc));
    if (rc != JNI_OK) {
        char buf[128];
        snprintf(buf, sizeof buf, "JNI_CreateJavaVM: FAIL rc=%d\n", static_cast<int>(rc));
        out += buf;
        return out;
    }
    out += "JNI_CreateJavaVM: OK\n";
    {
        int ok = 0, fail = 0;
        jclass classClass = env->FindClass("java/lang/Class");
        jmethodID forNameMid = env->GetStaticMethodID(classClass, "forName",
            "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;");
        jclass clCls = env->FindClass("java/lang/ClassLoader");
        jmethodID getSysMid = env->GetStaticMethodID(clCls, "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
        jobject sysLoader = env->CallStaticObjectMethod(clCls, getSysMid);
        for (const char *cn : kInitIdClasses) {
            // 用 Class.forName(name, false, loader) 避免触发静态初始化
            std::string dotted(cn);
            for (auto &ch2 : dotted) { if (ch2 == '/') ch2 = '.'; }
            jstring nm = env->NewStringUTF(dotted.c_str());
            jclass k = (jclass)env->CallStaticObjectMethod(classClass, forNameMid, nm, JNI_FALSE, sysLoader);
            if (!k) { env->ExceptionClear(); JLOG("reg: forName fail %{public}s", cn); fail++; continue; }
            JNINativeMethod m = { const_cast<char *>("initIDs"), const_cast<char *>("()V"), (void *)EmptyInitIDs };
            if (env->RegisterNatives(k, &m, 1) == 0) { ok++; }
            else { env->ExceptionClear(); JLOG("reg: RegisterNatives fail %{public}s", cn); fail++; }
        }
        JLOG("RegisterNatives initIDs ok=%{public}d fail=%{public}d", ok, fail);
    }
    {
        // 手动触发 libawt 的 JNI_OnLoad：JVM 通过 System.loadLibrary 加载时可能已在本进程映射，
        // 不再执行注册函数，导致 Font/Graphics 等 initIDs 未注册。
        void *libawt = dlopen("libawt.so", RTLD_NOW | RTLD_GLOBAL);
        if (!libawt) {
            JLOG("libawt dlopen FAIL: %{public}s", dlerror());
        } else {
            using OnLoadFn = jint (*)(JavaVM *, void *);
            auto onload = reinterpret_cast<OnLoadFn>(dlsym(libawt, "JNI_OnLoad"));
            JLOG("libawt JNI_OnLoad ptr=%{public}s", onload ? "found" : "missing");
            if (onload) {
                jint v = onload(vm, nullptr);
                JLOG("libawt JNI_OnLoad ret=%{public}d", static_cast<int>(v));
            }
        }
        void *liblcms = dlopen("liblcms.so", RTLD_NOW | RTLD_GLOBAL);
        if (liblcms) {
            using OnLoadFn3 = jint (*)(JavaVM *, void *);
            auto onload3 = reinterpret_cast<OnLoadFn3>(dlsym(liblcms, "JNI_OnLoad"));
            if (onload3) {
                JLOG("liblcms JNI_OnLoad ret=%{public}d", static_cast<int>(onload3(vm, nullptr)));
            } else {
                JLOG("liblcms JNI_OnLoad missing");
            }
        }
        void *libheadless = dlopen("libawt_headless.so", RTLD_NOW | RTLD_GLOBAL);
        if (!libheadless) {
            JLOG("libawt_headless dlopen FAIL: %{public}s", dlerror());
        } else {
            using OnLoadFn2 = jint (*)(JavaVM *, void *);
            auto onload2 = reinterpret_cast<OnLoadFn2>(dlsym(libheadless, "JNI_OnLoad"));
            JLOG("libawt_headless JNI_OnLoad ptr=%{public}s", onload2 ? "found" : "missing");
            if (onload2) {
                jint v2 = onload2(vm, nullptr);
                JLOG("libawt_headless JNI_OnLoad ret=%{public}d", static_cast<int>(v2));
            }
        }
    }
    auto propNow = [&](const char *k) -> std::string {
        jclass sc = env->FindClass("java/lang/System");
        jmethodID m = env->GetStaticMethodID(sc, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
        jstring kk = env->NewStringUTF(k);
        auto v = static_cast<jstring>(env->CallStaticObjectMethod(sc, m, kk));
        if (!v) return "<null>";
        const char *s = env->GetStringUTFChars(v, nullptr);
        std::string r = s ? s : "<null>";
        env->ReleaseStringUTFChars(v, s);
        return r;
    };
    JLOG("prop user.home=%{public}s", propNow("user.home").c_str());
    JLOG("prop awt.toolkit=%{public}s", propNow("awt.toolkit").c_str());
    JLOG("prop graphicsenv=%{public}s", propNow("java.awt.graphicsenv").c_str());
    JLOG("prop headless=%{public}s", propNow("java.awt.headless").c_str());
    JLOG("prop library.path=%{public}s", propNow("java.library.path").c_str());
    {
        std::string cpv = propNow("java.class.path");
        JLOG("prop class.path.len=%{public}zu head=%{public}s", cpv.size(), cpv.substr(0, 220).c_str());
        jclass tk = env->FindClass("ohos/awt/OhosToolkit");
        JLOG("FindClass(ohos/awt/OhosToolkit)=%{public}s", tk ? "OK" : "FAIL");
        if (!tk) { env->ExceptionDescribe(); env->ExceptionClear(); }
    }

    auto prop = [&](const char *key) -> std::string {
        jclass sys = env->FindClass("java/lang/System");
        if (!sys) return "<no class>";
        jmethodID mid = env->GetStaticMethodID(sys, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
        if (!mid) return "<no method>";
        jstring k = env->NewStringUTF(key);
        auto v = static_cast<jstring>(env->CallStaticObjectMethod(sys, mid, k));
        if (!v) return "<null>";
        const char *s = env->GetStringUTFChars(v, nullptr);
        std::string r = s ? s : "<null>";
        env->ReleaseStringUTFChars(v, s);
        return r;
    };
    out += "java.version=" + prop("java.version") + "\n";
    out += "java.vm.name=" + prop("java.vm.name") + "\n";
    out += "java.home=" + prop("java.home") + "\n";
    out += "os.arch=" + prop("os.arch") + "\n";

    jclass list = env->FindClass("java/util/ArrayList");
    out += std::string("FindClass(ArrayList)=") + (list ? "OK" : "FAIL") + "\n";

    env->ExceptionClear();
    jclass rew = env->FindClass("roomeqwizard/RoomEQ_Wizard");
    if (rew) {
        out += "FindClass(REW main)=OK\n";
        jmethodID mainMid = env->GetStaticMethodID(rew, "main", "([Ljava/lang/String;)V");
        if (!mainMid) {
            env->ExceptionDescribe();
            env->ExceptionClear();
            out += "GetStaticMethodID(main)=FAIL\n";
        } else {
            jclass strCls = env->FindClass("java/lang/String");
            jobjectArray emptyArgs = env->NewObjectArray(0, strCls, nullptr);
            out += "invoking main()...\n";
            env->CallStaticVoidMethod(rew, mainMid, emptyArgs);
            if (env->ExceptionCheck()) {
                out += "main() threw:\n";
                env->ExceptionDescribe();
                env->ExceptionClear();
            } else {
                out += "main() returned\n";
            }
        }
    } else {
        out += "FindClass(REW main)=FAIL\n";
        jthrowable ex = env->ExceptionOccurred();
        env->ExceptionClear();
        if (ex) {
            jclass throwable = env->FindClass("java/lang/Throwable");
            jmethodID toStr = env->GetMethodID(throwable, "toString", "()Ljava/lang/String;");
            auto s = static_cast<jstring>(env->CallObjectMethod(ex, toStr));
            if (s) {
                const char *cs = env->GetStringUTFChars(s, nullptr);
                out += std::string("  exception: ") + (cs ? cs : "?") + "\n";
                env->ReleaseStringUTFChars(s, cs);
            }
        }
        jclass cnf = env->FindClass("java/lang/NoClassDefFoundError");
        (void)cnf;
    }

    vm->DestroyJavaVM();
    out += "DONE\n";
    return out;
}

napi_value Probe(napi_env env, napi_callback_info info) {
    OH_LOG_INFO(LOG_APP, "probe enter");
    size_t argc = 2;
    napi_value argv[2] = {nullptr, nullptr};
    napi_get_cb_info(env, info, &argc, argv, nullptr, nullptr);

    char home[1024] = {0};
    char work[1024] = {0};
    size_t len = 0;
    if (argc >= 1) napi_get_value_string_utf8(env, argv[0], home, sizeof home, &len);
    if (argc >= 2) napi_get_value_string_utf8(env, argv[1], work, sizeof work, &len);

    OH_LOG_INFO(LOG_APP, "probe args home=%{public}s work=%{public}s", home, work);
    std::string result = RunJvm(home, work);
    OH_LOG_INFO(LOG_APP, "probe done len=%{public}zu", result.size());
    napi_value out;
    napi_create_string_utf8(env, result.c_str(), result.size(), &out);
    return out;
}

napi_value MemProbe(napi_env env, napi_callback_info info) {
    std::string out = ExecMemProbe();
    napi_value res;
    napi_create_string_utf8(env, out.c_str(), out.size(), &res);
    return res;
}

napi_value Init(napi_env env, napi_value exports) {
    napi_property_descriptor desc[] = {
        {"probe", nullptr, Probe, nullptr, nullptr, nullptr, napi_default, nullptr},
        {"memProbe", nullptr, MemProbe, nullptr, nullptr, nullptr, napi_default, nullptr},
    };
    napi_define_properties(env, exports, sizeof desc / sizeof desc[0], desc);
    return exports;
}

}  // namespace

static napi_module g_module = {
    .nm_version = 1,
    .nm_flags = 0,
    .nm_filename = nullptr,
    .nm_register_func = Init,
    .nm_modname = "rewjvm",
    .nm_priv = nullptr,
    .reserved = {0},
};

extern "C" __attribute__((constructor)) void RegisterModule() {
    napi_module_register(&g_module);
}