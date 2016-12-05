LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libexpat
LOCAL_CFLAGS := -DHAVE_MEMMOVE
LOCAL_SRC_FILES := libs/$(TARGET_ARCH_ABI)/libexpat.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libstrophe
LOCAL_SRC_FILES := libs/$(TARGET_ARCH_ABI)/libstrophe.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := mymodule
LOCAL_STATIC_LIBRARIES := libstrophe libexpat
LOCAL_LDLIBS += -latomic -llog
LOCAL_SRC_FILES := messenger/src/messenger_impl.cpp \
	messenger/src/detail/base64.cpp \
	messenger/src/detail/operation_queue.cpp \
	messenger/src/detail/uuid.cpp \
	messenger/src/xmpp/xmpp_connection.cpp \
	messenger/src/xmpp/xmpp_context.cpp \
	messenger/src/xmpp/xmpp_env.cpp \
	messenger/src/xmpp/xmpp_logger_android.cpp \
	messenger/src/xmpp/xmpp_stanza.cpp \
	messenger/src/xmpp/xmpp_stanza_builder.cpp \
	messenger/src/xmpp/xmpp_stanza_id.cpp \
	messenger/src/xmpp/xmpp_stanza_parser.cpp \
	messenger_jni.cpp
include $(BUILD_SHARED_LIBRARY)
