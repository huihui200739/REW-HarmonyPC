/* OHOS JVM 兼容 shim：补齐 glibc 有、OHOS musl 没有的少量符号。
 * 数学函数转调平台 libm（StrictMath 位精确性会略降，对测量软件可接受）。
 * 这些符号通过 RTLD_GLOBAL 预加载，供 libjvm/libjava 解析。 */
#include <math.h>
#include <string.h>
#include <errno.h>
#include <stddef.h>

double jcos(double x) { return cos(x); }
double jsin(double x) { return sin(x); }
double jtan(double x) { return tan(x); }
double jacos(double x) { return acos(x); }
double jasin(double x) { return asin(x); }
double jatan(double x) { return atan(x); }
double jatan2(double y, double x) { return atan2(y, x); }
double jcosh(double x) { return cosh(x); }
double jsinh(double x) { return sinh(x); }
double jtanh(double x) { return tanh(x); }
double jexpm1(double x) { return expm1(x); }
double jlog(double x) { return log(x); }
double jlog10(double x) { return log10(x); }
double jlog1p(double x) { return log1p(x); }
double jremainder(double x, double y) { return remainder(x, y); }
double jsqrt(double x) { return sqrt(x); }

int __xpg_strerror_r(int errnum, char *buf, size_t buflen) {
    const char *msg = strerror(errnum);
    if (msg == NULL) return EINVAL;
    if (buflen == 0) return ERANGE;
    size_t n = strlen(msg);
    if (n >= buflen) n = buflen - 1;
    memcpy(buf, msg, n);
    buf[n] = '\0';
    return 0;
}

void __register_frame_info(void *begin, void *ob) { (void)begin; (void)ob; }
void __deregister_frame_info(void *begin) { (void)begin; }
void __at_fini(void) {}
void collector_func_load(void) {}
