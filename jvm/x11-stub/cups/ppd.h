#ifndef _CUPS_PPD_H_
#define _CUPS_PPD_H_
#include <cups/cups.h>
#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    char *choice;
    char *text;
    char *value;
    int marked;
} ppd_choice_t;

typedef struct {
    char *keyword;
    char *text;
    ppd_choice_t *choices;
    int num_choices;
    char *defchoice;
} ppd_option_t;

typedef struct {
    char *keyword;
    char *text;
    float width;
    float length;
    float left;
    float bottom;
    float right;
    float top;
} ppd_size_t;

typedef struct _ppd_file_s {
    int num_options;
    ppd_option_t *options;
    int num_sizes;
    ppd_size_t *sizes;
    float width;
    float length;
} ppd_file_t;

ppd_file_t *ppdOpenFile(const char *filename);
void ppdClose(ppd_file_t *ppd);
ppd_option_t *ppdFindOption(ppd_file_t *ppd, const char *keyword);
ppd_choice_t *ppdFindChoice(ppd_option_t *option, const char *choice);

#ifdef __cplusplus
}
#endif
#endif