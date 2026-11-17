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

std::string RunJvm(const std::string &javaHome, const std::string &workDir) {
    std::string out = ExecMemProbe();

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
    JavaVMOption opts[10];
    int n = 0;
    std::string homeOpt = "-Djava.home=" + javaHome;
    std::string tmpOpt = "-Djava.io.tmpdir=" + workDir;
    opts[n++].optionString = const_cast<char *>(homeOpt.c_str());
    opts[n++].optionString = const_cast<char *>(tmpOpt.c_str());
    opts[n++].optionString = const_cast<char *>("-Xint");
    opts[n++].optionString = bufA;
    opts[n++].optionString = bufB;
    opts[n++].optionString = bufC;
    opts[n++].optionString = bufD;
    opts[n++].optionString = bufE;

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