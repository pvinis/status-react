LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := nim_status
LOCAL_EXPORT_C_INCLUDES := $(NIM_STATUS_ANDROID_LIBDIR)/$(TARGET_ARCH_ABI)
LOCAL_SRC_FILES := $(NIM_STATUS_ANDROID_LIBDIR)/$(TARGET_ARCH_ABI)/libnim_status.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := openssl
LOCAL_SRC_FILES := $(NIM_STATUS_ANDROID_LIBDIR)/$(TARGET_ARCH_ABI)/libssl.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := crypto
LOCAL_SRC_FILES := $(NIM_STATUS_ANDROID_LIBDIR)/$(TARGET_ARCH_ABI)/libcrypto.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := pcre
LOCAL_SRC_FILES := $(NIM_STATUS_ANDROID_LIBDIR)/$(TARGET_ARCH_ABI)/libpcre.a
include $(PREBUILT_STATIC_LIBRARY)



include $(CLEAR_VARS)
LOCAL_MODULE := status
LOCAL_EXPORT_C_INCLUDES := $(STATUS_GO_ANDROID_LIBDIR)/$(TARGET_ARCH_ABI)
LOCAL_SRC_FILES := $(STATUS_GO_ANDROID_LIBDIR)/$(TARGET_ARCH_ABI)/libstatus.so
include $(PREBUILT_SHARED_LIBRARY)
 
include $(CLEAR_VARS)
LOCAL_MODULE := status_wrapper
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)
LOCAL_STATIC_LIBRARIES := nim_status openssl crypto pcre
LOCAL_SHARED_LIBRARIES := status
LOCAL_SRC_FILES := status.c 
include $(BUILD_SHARED_LIBRARY)



