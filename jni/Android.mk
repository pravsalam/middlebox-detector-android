LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDLIBS := -L/usr/lib -llog
LOCAL_MODULE    := tcptests
LOCAL_SRC_FILES := tcptests.cpp

#include $(BUILD_SHARED_LIBRARY)
include $(BUILD_EXECUTABLE)
