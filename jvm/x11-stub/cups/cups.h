/* 最小 CUPS 兼容头：OHOS 无打印子系统，仅为让 libawt_headless 的 CUPSfuncs.c 编过。
 * 全部函数声明为弱符号，运行时若被调用会失败——headless 路径不会走到打印。 */
#ifndef _CUPS_CUPS_H_
#define _CUPS_CUPS_H_
#include <stddef.h>
#ifdef __cplusplus
extern "C" {
#endif

typedef struct _cups_dest_s {
    char *name;
    char *instance;
    int is_default;
    int num_options;
    void *options;
} cups_dest_t;

typedef struct _cups_ptype_s {
    int num_formats;
    int format;
} cups_ptype_t;

typedef struct _ipp_s ipp_t;
typedef struct _http_s http_t;
typedef struct _ipp_attribute_s ipp_attribute_t;

typedef enum { HTTP_ENCRYPTION_IF_REQUESTED } http_encryption_t;
typedef enum { IPP_OP_GET_PRINTER_ATTRIBUTES } ipp_op_t;
typedef enum { IPP_TAG_KEYWORD, IPP_TAG_OPERATION, IPP_TAG_PRINTER } ipp_tag_t;
typedef enum { CUPS_HTTP_DEFAULT = 0 } cups_http_t;

char *cupsGetPPD2(const char *printer, http_t *http);
cups_dest_t *cupsGetDests(void);
void cupsFreeDests(int num_dests, cups_dest_t *dests);
const char *cupsGetOption(const char *name, int num_options, cups_dest_t *options);
void cupsFreeOptions(int num_options, cups_dest_t *options);
http_t *httpConnect2(const char *host, int port, const char *addr, int family, int encryption, int blocking, int msec);
int httpClose(http_t *http);
ipp_t *ippNewRequest(ipp_op_t op);
ipp_attribute_t *ippAddString(ipp_t *ipp, ipp_tag_t group, const char *attr, const char *language, const char *value);
void ippDelete(ipp_t *ipp);
ipp_t *cupsDoRequest(http_t *http, ipp_t *request, const char *resource);

#ifdef __cplusplus
}
#endif
#endif
