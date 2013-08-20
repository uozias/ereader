LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

MY_ROOT := ../mupdf

OPENJPEG := openjpeg
JPEG := jpeg
ZLIB := zlib
FREETYPE := freetype
V8 := v8-3.9

LOCAL_CFLAGS += -DARCH_ARM -DARCH_THUMB -DARCH_ARM_CAN_LOAD_UNALIGNED -DAA_BITS=8
ifdef NDK_PROFILER
LOCAL_CFLAGS += -pg -DNDK_PROFILER -O0
NDK_APP_CFLAGS :=
endif

LOCAL_C_INCLUDES := \
	mupdf/thirdparty/jbig2dec \
	mupdf/thirdparty/$(OPENJPEG)/libopenjpeg \
	mupdf/thirdparty/$(JPEG) \
	mupdf/thirdparty/$(ZLIB) \
	mupdf/thirdparty/$(FREETYPE)/include \
	mupdf/draw \
	mupdf/fitz \
	mupdf/pdf \
	mupdf/xps \
	mupdf/cbz \
	mupdf/scripts \
	mupdf
ifdef V8_BUILD
LOCAL_C_INCLUDES += mupdf/thirdparty/$(V8)/include
endif

LOCAL_MODULE    := mupdfcore2
LOCAL_SRC_FILES := \
	$(MY_ROOT)/fitz/res_shade.c

LOCAL_LDLIBS    := -lm -llog -ljnigraphics

include $(BUILD_STATIC_LIBRARY)
