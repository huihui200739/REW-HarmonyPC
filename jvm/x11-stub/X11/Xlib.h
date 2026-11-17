/* 最小 X11 兼容头：只为让 OpenJDK 的 java.desktop 在无 X11 的 OHOS sysroot 下编译通过。
 * 只声明类型，不提供实现；headless 路径不会调用这些函数。
 * 真正的图形后端是 P1-b2/b3 的 OHOS AWT 后端，不使用 X11。 */
#ifndef _X11_XLIB_H_
#define _X11_XLIB_H_

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct _XDisplay Display;
typedef struct _XGC *GC;
typedef struct _XVisual *Visual;
typedef struct _XColormap *Colormap;
typedef struct _XImage XImage;
typedef unsigned long Window;
typedef unsigned long Pixmap;
typedef unsigned long Font;
typedef unsigned long Atom;
typedef unsigned long Cursor;
typedef unsigned long KeySym;
typedef unsigned long Time;
typedef unsigned long XID;
typedef XID Drawable;
typedef int Bool;
typedef int Status;
typedef unsigned char KeyCode;
typedef unsigned long Mask;

typedef struct {
    short x, y;
    unsigned short width, height;
} XRectangle;

typedef struct {
    short x, y;
} XPoint;

typedef struct {
    int depth;
    int bits_per_pixel;
    int scanline_pad;
} XPixmapFormatValues;

typedef struct _XRegion {
    long size;
    long numRects;
} REGION;
typedef REGION *Region;

struct _XImage {
    unsigned int width, height;
    int xoffset, yoffset;
    int format;
    char *data;
    int byte_order;
    int bitmap_unit;
    int bitmap_bit_order;
    int bitmap_pad;
    int depth;
    int bytes_per_line;
    int bits_per_pixel;
    unsigned long red_mask, green_mask, blue_mask;
    void *obdata;
};

typedef unsigned long VisualID;

typedef struct {
    Visual *visual;
    VisualID visualid;
    int screen;
    int depth;
    int class;
    unsigned long red_mask;
    unsigned long green_mask;
    unsigned long blue_mask;
    int colormap_size;
    int bits_per_rgb;
} XVisualInfo;

/* GLX 类型（headless 下不调用，仅满足编译） */
typedef struct __GLXcontextRec *GLXContext;
typedef XID GLXDrawable;
typedef XID GLXPixmap;
typedef XID GLXWindow;
typedef XID GLXPbuffer;
typedef struct __GLXFBConfigRec *GLXFBConfig;
typedef XID GLXFBConfigID;
/* __GLXextFuncPtr 由 JDK 自带的 J2D_GL/glx.h 定义 */
typedef struct __GLXFBConfigRec GLXFBConfigRec;

#define None 0L
#define Success 0
#define True 1
#define False 0
#define AllPlanes ((unsigned long)~0L)

#ifdef __cplusplus
}
#endif
#endif /* _X11_XLIB_H_ */