LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := tcptests
LOCAL_SRC_FILES := tcptests.cpp

include $(BUILD_SHARED_LIBRARY)
