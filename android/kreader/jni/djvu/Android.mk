LOCAL_PATH := $(call my-dir)
TOP_LOCAL_PATH := $(LOCAL_PATH)

include $(TOP_LOCAL_PATH)/Core.mk

include $(CLEAR_VARS)
LOCAL_ARM_MODE := arm

LOCAL_C_INCLUDES := \
    $(TOP_LOCAL_PATH) \
    $(TOP_LOCAL_PATH)/libdjvu  \
    $(TOP_LOCAL_PATH)/../common/utils

LOCAL_MODULE    := djvu
LOCAL_SRC_FILES := djvu.cpp \
            orion_bitmap.c

LOCAL_STATIC_LIBRARIES := djvucore libutils

LOCAL_LDLIBS    := -lm -llog -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
