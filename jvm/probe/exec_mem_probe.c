/* 判定 OHOS 是否允许可执行匿名内存：RW -> mprotect(RX) -> 调用，以及 RWX 直映射 */
#include <stdio.h>
#include <string.h>
#include <sys/mman.h>
#include <unistd.h>

static const unsigned char kRet42[] = {0x40, 0x05, 0x80, 0x52, 0xc0, 0x03, 0x5f, 0xd6}; /* mov w0,#42; ret */

int main(void) {
    long ps = sysconf(_SC_PAGESIZE);
    void *p = mmap(NULL, ps, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (p == MAP_FAILED) { printf("FAIL mmap RW\n"); return 1; }
    memcpy(p, kRet42, sizeof kRet42);
    printf("OK  mmap RW\n");

    if (mprotect(p, ps, PROT_READ | PROT_EXEC) != 0) {
        printf("FAIL mprotect RX\n");
    } else {
        int (*fn)(void) = (int (*)(void))p;
        printf("OK  mprotect RX, call -> %d\n", fn());
    }

    void *q = mmap(NULL, ps, PROT_READ | PROT_WRITE | PROT_EXEC, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (q == MAP_FAILED) printf("FAIL mmap RWX\n");
    else {
        memcpy(q, kRet42, sizeof kRet42);
        int (*fn2)(void) = (int (*)(void))q;
        printf("OK  mmap RWX, call -> %d\n", fn2());
    }
    return 0;
}
