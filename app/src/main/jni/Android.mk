LOCAL_PATH := $(call my-dir)

######################################

include $(CLEAR_VARS)
LOCAL_MODULE := dobby
LOCAL_SRC_FILES := lib/$(TARGET_ARCH_ABI)/libdobby.a
include $(PREBUILT_STATIC_LIBRARY)

######################################

include $(CLEAR_VARS)
LOCAL_MODULE := Lewis

LOCAL_CFLAGS := -w -s -Wno-error=format-security -fvisibility=hidden -fpermissive -fexceptions
LOCAL_CPPFLAGS := -w -s -Wno-error=format-security -fvisibility=hidden -Werror -std=c++11 -std=c++17
LOCAL_CPPFLAGS += -Wno-error=c++11-narrowing -fpermissive -Wall -fexceptions
LOCAL_LDFLAGS += -Wl,--gc-sections,--strip-all, -llog
LOCAL_LDLIBS := -llog -landroid -lEGL -lGLESv3
LOCAL_ARM_MODE := arm

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/xdl
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/ImGui
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/ImGui/backends
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/ImGui/font
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/Hook/Dobby
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/Hook/KittyMemory
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/Utils
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/Utils/Unity
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/Utils/Unity/il2cpp

FILE_LIST := $(wildcard $(LOCAL_PATH)/*.cpp*)
FILE_LIST += $(wildcard $(LOCAL_PATH)/src/*.cpp*)
FILE_LIST += $(wildcard $(LOCAL_PATH)/src/xdl/*.c*)
FILE_LIST += $(wildcard $(LOCAL_PATH)/src/ImGui/*.cpp*)
FILE_LIST += $(wildcard $(LOCAL_PATH)/src/ImGui/backends/*.cpp*)
FILE_LIST += $(wildcard $(LOCAL_PATH)/src/Hook/KittyMemory/*.cpp*)
FILE_LIST += $(wildcard $(LOCAL_PATH)/src/Utils/*.cpp*)

LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_STATIC_LIBRARIES := dobby

include $(BUILD_SHARED_LIBRARY)

######################################