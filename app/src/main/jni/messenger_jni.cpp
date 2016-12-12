#include <jni.h>
#include <future>
#include <memory>
#include <iostream>
#include <vector>
#include <string>
#include "messenger/messenger.h"
#include "../../../../../../Android/sdk/ndk-bundle/platforms/android-16/arch-x86/usr/include/android/log.h"


#define JNI_CALL(__ret, __f) extern "C" JNIEXPORT __ret JNICALL Java_ru_olgathebest_casper_MessengerNDK_##__f
using namespace std;
using namespace messenger;


shared_ptr<IMessenger> i_mes;

vector<unsigned char> getPublicKey();


promise<UserList> user_list;

string text_mes = "no";
int code_result = 0;

class Client : public ILoginCallback, public IMessagesObserver, public IRequestUsersCallback {
public:
    JavaVM *mJvm;
    JNIEnv *mEnv;
    jobject mObj;
public :
    Client(string url, int port, JNIEnv *env, jobject obj) {
        MessengerSettings settings;
        settings.serverUrl = url;//"93.188.161.205";
        settings.serverPort = port;// 5222;
        i_mes = GetMessengerInstance(settings);
        env->GetJavaVM(&mJvm);
        mEnv = env;
        mObj = env->NewGlobalRef(obj);
    }

    void OnOperationResult(operation_result::Type result) override {
        __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "result %d", result);
        code_result = result;
        mJvm->AttachCurrentThread(&mEnv, NULL);
        jclass thisClass = mEnv->GetObjectClass(mObj);
        jmethodID method = mEnv->GetMethodID(thisClass, "onLogin", "(I)V");
        mEnv->CallVoidMethod(mObj, method,result);
        mJvm->DetachCurrentThread();
    }

//    void OnOperationResult(operation_result::Type result, const UserList &users) override {
//        user_list.set_value(users);
//    }
    void OnOperationResult(operation_result::Type result, const UserList &users) override {
        mJvm->AttachCurrentThread(&mEnv, NULL);
        jclass thisClass = mEnv->GetObjectClass(mObj);
        jmethodID method = mEnv->GetMethodID(thisClass, "getUserList", "([B[I[B[I)V");
        unsigned int len = 0;
        unsigned int keyLen = 0;
        for (int i = 0; i < users.size(); i++) {
            len += users[i].identifier.length();
            keyLen+=users[i].securityPolicy.encryptionPubKey.size();
        }
       // __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "len = %d; keyLen = %d",len,keyLen);
        jbyteArray keylist = mEnv->NewByteArray(keyLen);
        jbyteArray userslist = mEnv->NewByteArray(len);
        jintArray userslen = mEnv->NewIntArray(users.size());
        jintArray keyslen = mEnv->NewIntArray(users.size());
        unsigned int start = 0;
        unsigned int startkey = 0;
        for (int i = 0; i < users.size(); i++) {
            unsigned int length = users[i].identifier.length();
            mEnv->SetByteArrayRegion(userslist, start, users[i].identifier.length(),
                                     reinterpret_cast<const jbyte *>(&users[i].identifier.c_str()[0]));
            __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "login: %s",users[i].identifier.c_str());
            __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "length: %d",users[i].identifier.length());
            start += users[i].identifier.length();
            __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "start = %d",start);
            mEnv->SetIntArrayRegion(userslen, i, 1, reinterpret_cast<const jint *>(&length));

            int size = users[i].securityPolicy.encryptionPubKey.size();
            string str = string(begin(users[i].securityPolicy.encryptionPubKey), end(users[i].securityPolicy.encryptionPubKey));
            __android_log_print(ANDROID_LOG_DEBUG, "[C++]","%s", str.c_str());
            __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "size[%d] = %d",i,size);
            if (size > 0) {
                __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "I am inside");
                mEnv->SetByteArrayRegion(keylist, startkey, size,
                                         reinterpret_cast<const jbyte *>(&users[i].securityPolicy.encryptionPubKey[0]));
            }
            __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "I am outside");
                mEnv->SetIntArrayRegion(keyslen, i, 1, reinterpret_cast<const jint *>(&size));
            __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "I am dead");
            startkey += size;
        }
        mEnv->CallVoidMethod(mObj, method, userslist, userslen, keylist,keyslen);
        mJvm->DetachCurrentThread();

    }

    ~Client() {
        mEnv->DeleteGlobalRef(mObj);
    }

    void OnMessageStatusChanged(const MessageId &msg_id, message_status::Type status) override {
        __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "Status START: %d", status);
        JNIEnv *env;
        bool attached = false;
        jint envRes = mJvm->GetEnv((void **) &env, JNI_VERSION_1_6);
        if (envRes == JNI_EDETACHED) {
            mJvm->AttachCurrentThread(&env, NULL);
            attached = true;
            __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "ATTACHED");
        }

        jclass cls = env->GetObjectClass(mObj);
        jmethodID method = env->GetMethodID(cls, "onMessageStatusChanged", "([BI)V");
        unsigned int msgIdLen = msg_id.size();
        const jbyte *msgIdBytes = reinterpret_cast<const jbyte *>(&msg_id.c_str()[0]);
        jbyteArray msgIdArr = env->NewByteArray(msgIdLen);

        env->SetByteArrayRegion(msgIdArr, 0, msgIdLen, msgIdBytes);
        env->CallVoidMethod(mObj, method, msgIdArr, status);

        if (attached) {
            mJvm->DetachCurrentThread();

            __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "DETACHED");
        }
        __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "Status END: %d", status);
    }

    void OnMessageReceived(const UserId &sender_id, const Message &msg) override {

        mJvm->AttachCurrentThread(&mEnv, NULL);
        jclass thisClass = mEnv->GetObjectClass(mObj);
        long time = msg.time;
        unsigned int len = msg.content.data.size();
        // unsigned int idLen = msg.identifier.size();
        const jbyte *text = reinterpret_cast<const jbyte *>(&msg.content.data[0]);
        // const jbyte *id = reinterpret_cast<const jbyte *>(&msg.identifier[0]);
        jbyteArray byte_mes = mEnv->NewByteArray(len);
        // jbyteArray byte_id = mEnv->NewByteArray(idLen);
        mEnv->SetByteArrayRegion(byte_mes, 0, len, text);
        // mEnv->SetByteArrayRegion(byte_id, 0, idLen, id);
        jmethodID midCallBack = mEnv->GetMethodID(thisClass, "getMsg",
                                                  "(Ljava/lang/String;Ljava/lang/String;[BJ)V");
        mEnv->CallVoidMethod(mObj, midCallBack, mEnv->NewStringUTF(sender_id.c_str()),
                             mEnv->NewStringUTF(msg.identifier.c_str()), byte_mes, time);
        mJvm->DetachCurrentThread();
    }
};

Client *client;
JNI_CALL(void, nativeConnect)(JNIEnv *env, jclass caller, jstring url, jint port) {
    const char *login_chars = env->GetStringUTFChars(url, 0);
    client = new Client(login_chars, port, env, caller);
}
JNI_CALL(void, nativeLogin)(JNIEnv *env, jobject obj, jbyteArray userId, jbyteArray password, jbyteArray publicKey) {

    jsize userIdSize = env->GetArrayLength(userId);
    jsize passwordSize = env->GetArrayLength(password);
    jsize keySize = env->GetArrayLength(publicKey);
    __android_log_print(ANDROID_LOG_DEBUG, "[C++]", "keSize = %d", keySize);
    messenger::SecurityPolicy policy;
    if (keySize > 1){
        policy.encryptionAlgo = encryption_algorithm::Type::RSA_1024;
    } else {
        policy.encryptionAlgo = encryption_algorithm::Type::None;
    }
    policy.encryptionPubKey.resize(keySize);
    env->GetByteArrayRegion(publicKey, 0, keySize, reinterpret_cast<jbyte *>(&policy.encryptionPubKey[0]));
    messenger::UserId userIdNew(userIdSize, ' ');
    std::string passwordNew(passwordSize, ' ');
    for (jsize i = 0; i < userIdSize; ++i)
        env->GetByteArrayRegion(userId, i, 1, reinterpret_cast<jbyte *>(&userIdNew[i]));

    for (jsize i = 0; i < passwordSize; ++i)
        env->GetByteArrayRegion(password, i, 1, reinterpret_cast<jbyte *>(&passwordNew[i]));
    i_mes->RegisterObserver(client);
    i_mes->Login(userIdNew, passwordNew, policy, client);
}

JNI_CALL(void, nativeSend)(JNIEnv *env, jclass caller, jbyteArray recpt,
                           jbyteArray text) { //тут тоже должен быть массив байт
    jsize userIdSize = env->GetArrayLength(recpt);
    messenger::UserId recptId(userIdSize, ' ');
    for (jsize i = 0; i < userIdSize; ++i)
        env->GetByteArrayRegion(recpt, i, 1, reinterpret_cast<jbyte *>(&recptId[i]));
    MessageContent msg;
    msg.type = message_content_type::Text;
    jsize size_msg = env->GetArrayLength(text);
    msg.data.resize(size_msg);
    env->GetByteArrayRegion(text, 0, size_msg, reinterpret_cast<jbyte *>(&msg.data[0]));
    i_mes->SendMessage(recptId, msg);

}

JNI_CALL(void, nativeMessageSeen)(JNIEnv *env, jclass caller, jbyteArray userId,
                                  jbyteArray msg_id) {

    jclass cls = env->GetObjectClass(caller);
    jmethodID method = env->GetMethodID(cls, "onMessageStatusChanged", "([BI)V");
    jsize userIdSize = env->GetArrayLength(userId);
    jsize msgIdSize = env->GetArrayLength(msg_id);
    messenger::UserId usrId(userIdSize, ' ');
    messenger::MessageId msgId(msgIdSize, ' ');
    for (jsize i = 0; i < userIdSize; ++i)
        env->GetByteArrayRegion(userId, i, 1, reinterpret_cast<jbyte *>(&usrId[i]));
    for (jsize i = 0; i < msgIdSize; ++i)
        env->GetByteArrayRegion(msg_id, i, 1, reinterpret_cast<jbyte *>(&msgId[i]));
    i_mes->SendMessageSeen(usrId, msgId);
    env->CallVoidMethod(caller, method, msg_id, 4);
}
JNI_CALL(void, nativeTestUserList)(JNIEnv *env, jclass caller) {
    i_mes->RequestActiveUsers(client);
}
JNI_CALL(void, nativeDisconnect)(JNIEnv *env, jclass caller) {
    i_mes->UnregisterObserver(client);
    i_mes->Disconnect();
}
JNI_CALL(jstring, testJNI)(JNIEnv *env, jobject obj) {
    return env->NewStringUTF("Hello from native code!");
}