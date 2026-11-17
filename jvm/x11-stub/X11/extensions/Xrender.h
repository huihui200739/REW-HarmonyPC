#ifndef _X11_XRENDER_H_
#define _X11_XRENDER_H_
#include <X11/Xlib.h>
typedef struct { unsigned short red, green, blue, alpha; } XRenderColor;
typedef struct { short red, green, blue, alpha; } XRenderDirectFormat;
typedef struct { XRenderDirectFormat direct; Colormap colormap; unsigned int type; unsigned int depth; } XRenderPictFormat;
typedef unsigned long PictFormat;
typedef XID GlyphSet;
typedef XID Picture;
typedef struct { int n; short x, y; } XRenderPictFormatList;
typedef struct _XRenderPictureAttributes { int repeat; } XRenderPictureAttributes;
#endif
