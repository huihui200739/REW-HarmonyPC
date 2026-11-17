/* 最小 fontconfig 兼容头：OHOS 用自有字体发现，先满足 libawt_headless 编译。
 * 运行时会替换为 OHOS 字体后端；headless 路径不调用这些函数。 */
#ifndef _FONTCONFIG_FONTCONFIG_H_
#define _FONTCONFIG_FONTCONFIG_H_
#include <stddef.h>
#ifdef __cplusplus
extern "C" {
#endif

typedef unsigned char FcChar8;
typedef unsigned int FcChar32;
typedef int FcBool;
typedef struct _FcConfig FcConfig;
typedef struct _FcPattern FcPattern;
typedef struct _FcFontSet FcFontSet;
typedef struct _FcObjectSet FcObjectSet;
typedef struct _FcCharSet FcCharSet;
typedef struct _FcStrList FcStrList;

typedef enum { FcResultMatch, FcResultNoMatch, FcResultTypeMismatch, FcResultNoId, FcResultOutOfMemory } FcResult;
typedef enum { FcMatchPattern, FcMatchFont, FcMatchScan } FcMatchKind;
typedef enum { FcSetSystem, FcSetApplication } FcSetName;

struct _FcPattern { int dummy; };
struct _FcFontSet {
    int nfont;
    int sfont;
    FcPattern **fonts;
};
struct _FcObjectSet {
    int nobject;
    int sobject;
    char **objects;
};

FcBool FcInit(void);
FcConfig *FcConfigGetCurrent(void);
FcFontSet *FcConfigGetFonts(FcConfig *config, FcSetName set);
FcPattern *FcPatternCreate(void);
void FcPatternDestroy(FcPattern *p);
FcBool FcPatternAddString(FcPattern *p, const char *object, const FcChar8 *s);
FcBool FcPatternAddBool(FcPattern *p, const char *object, FcBool b);
FcBool FcPatternAddInteger(FcPattern *p, const char *object, int i);
FcResult FcPatternGetString(const FcPattern *p, const char *object, int n, FcChar8 **s);
FcResult FcPatternGetInteger(const FcPattern *p, const char *object, int n, int *i);
FcResult FcPatternGetBool(const FcPattern *p, const char *object, int n, FcBool *b);
FcResult FcPatternGetDouble(const FcPattern *p, const char *object, int n, double *d);
FcResult FcPatternGetCharSet(const FcPattern *p, const char *object, int n, FcCharSet **c);
FcObjectSet *FcObjectSetCreate(void);
FcBool FcObjectSetAdd(FcObjectSet *os, const char *object);
void FcObjectSetDestroy(FcObjectSet *os);
FcFontSet *FcFontList(FcConfig *config, FcPattern *p, FcObjectSet *os);
void FcFontSetDestroy(FcFontSet *s);
int FcStrCmpIgnoreCase(const FcChar8 *s1, const FcChar8 *s2);
FcChar8 *FcStrCopy(const FcChar8 *s);
void FcStrFree(FcChar8 *s);

#define FcTrue 1
#define FcFalse 0

typedef enum {
    FcTypeVoid, FcTypeInteger, FcTypeDouble, FcTypeString, FcTypeBool,
    FcTypeMatrix, FcTypeCharSet, FcTypeFTFace, FcTypeLangSet, FcTypeRange
} FcType;

FcBool FcCharSetIntersect(FcCharSet *a, FcCharSet *b);
FcCharSet *FcCharSetCreate(void);
FcPattern *FcFontMatch(FcConfig *config, FcPattern *p, FcResult *result);
FcBool FcConfigSubstitute(FcConfig *config, FcPattern *p, FcMatchKind kind);
void FcDefaultSubstitute(FcPattern *pattern);
FcBool FcPatternAddDouble(FcPattern *p, const char *object, double d);
FcBool FcPatternAddCharSet(FcPattern *p, const char *object, const FcCharSet *c);
FcBool FcPatternDel(FcPattern *p, const char *object);
FcBool FcPatternRemove(FcPattern *p, const char *object, int n);
FcBool FcPatternEqual(const FcPattern *pa, const FcPattern *pb);
FcPattern *FcPatternDuplicate(const FcPattern *p);
int FcCharSetCount(const FcCharSet *c);
FcStrList *FcConfigGetFontDirs(FcConfig *config);
FcChar8 *FcStrListNext(FcStrList *list);
void FcStrListDone(FcStrList *list);
void FcCharSetDestroy(FcCharSet *fcs);
FcBool FcCharSetUnion(FcCharSet *dst, const FcCharSet *src);
FcResult FcPatternGetFTFace(const FcPattern *p, const char *object, int n, void **f);

#define FC_FILE "file"
#define FC_OUTLINE "outline"
#define FC_LANG "lang"
#define FC_ORDER "order"
#define FC_ANTIALIAS "antialias"
#define FC_HINTING "hinting"
#define FC_HINT_STYLE "hintstyle"
#define FC_RGBA "rgba"
#define FC_RGBA_UNKNOWN 0
#define FC_RGBA_NONE 1
#define FC_RGBA_RGB 2
#define FC_RGBA_BGR 3
#define FC_RGBA_VRGB 4
#define FC_RGBA_VBGR 5
#define FC_EMBOLDEN "embolden"
#define FC_EMBEDDED_BITMAP "embeddedbitmap"
#define FC_MINSPACE "minspace"
#define FC_NAMELANG "namelang"
#define FC_GLOBAL_ADVANCE "globaladvance"
#define FC_FONT_FEATURES "fontfeatures"
#define FC_VARIABLE "variable"
#define FC_FONT_VARIATIONS "fontvariations"
#define FC_INDEX "index"
#define FC_MATRIX "matrix"
#define FC_SCALE "scale"
#define FC_PIXEL_SIZE "pixelsize"
#define FC_DPI "dpi"
#define FC_FAMILY "family"
#define FC_FULLNAME "fullname"
#define FC_FOUNDRY "foundry"
#define FC_STYLE "style"
#define FC_WEIGHT "weight"
#define FC_SLANT "slant"
#define FC_SPACING "spacing"
#define FC_FONTFORMAT "fontformat"
#define FC_SCALABLE "scalable"
#define FC_CHARSET "charset"

#ifdef __cplusplus
}
#endif
#endif