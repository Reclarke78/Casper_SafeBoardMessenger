#include <jni.h>
#include <future>
#include <memory>
#include <iostream>
#include <vector>
#include <string>
#include "messenger/messenger.h"


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
       code_result = result;
    }

//    void OnOperationResult(operation_result::Type result, const UserList &users) override {
//        user_list.set_value(users);
//    }
    void OnOperationResult(operation_result::Type result, const UserList &users) override {
            mJvm->AttachCurrentThread(&mEnv, NULL);
            jclass thisClass = mEnv->GetObjectClass(mObj);
            jmethodID method = mEnv->GetMethodID(thisClass, "getUserList", "([B[I)V");
            unsigned int len = 0;
            for (int i = 0; i < users.size(); i++) {
                len += users[i].identifier.length();
            }
            jbyteArray userslist = mEnv->NewByteArray(len);
            jintArray userslen = mEnv->NewIntArray(users.size());
            unsigned int start = 0;
            for (int i = 0; i < users.size(); i++) {
                unsigned int length = users[i].identifier.length();
                mEnv->SetByteArrayRegion(userslist, start, users[i].identifier.length(),
                                         reinterpret_cast<const jbyte *>(&users[i].identifier.c_str()[0]));
                start += users[i].identifier.length();
                mEnv->SetIntArrayRegion(userslen,i,1,reinterpret_cast<const jint *>(&length));
            }
            mEnv->CallVoidMethod(mObj, method, userslist,userslen);
            mJvm->DetachCurrentThread();

    }

    ~Client() {
        mEnv->DeleteGlobalRef(mObj);
    }

    void OnMessageStatusChanged(const MessageId &msg_id, message_status::Type status) override {

    }

    void OnMessageReceived(const UserId &sender_id, const Message &msg) override {

        mJvm->AttachCurrentThread(&mEnv, NULL);
        jclass thisClass = mEnv->GetObjectClass(mObj);

        unsigned int len = msg.content.data.size();
        const jbyte *text = reinterpret_cast<const jbyte *>(&msg.content.data[0]);
        jbyteArray byte_mes = mEnv->NewByteArray(len);
        mEnv->SetByteArrayRegion(byte_mes, 0, len, text);

        jmethodID midCallBack = mEnv->GetMethodID(thisClass, "getMsg","(Ljava/lang/String;Ljava/lang/String;[B)V");
        mEnv->CallVoidMethod(mObj, midCallBack, mEnv->NewStringUTF(sender_id.c_str()),
                             mEnv->NewStringUTF(msg.identifier.c_str()), byte_mes);
        mJvm->DetachCurrentThread();
    }
};

Client *client;
JNI_CALL(void, nativeConnect)(JNIEnv *env, jclass caller, jstring url, jint port) {
    const char *login_chars = env->GetStringUTFChars(url, 0);
    client = new Client(login_chars, port, env, caller);
}
JNI_CALL(jint, nativeLogin)(JNIEnv *env, jobject obj, jbyteArray userId, jbyteArray password) {

    jsize userIdSize = env->GetArrayLength(userId);
    jsize passwordSize = env->GetArrayLength(password);
    messenger::SecurityPolicy policy;
    messenger::UserId userIdNew(userIdSize, ' ');
    std::string passwordNew(passwordSize, ' ');
    for (jsize i = 0; i < userIdSize; ++i)
        env->GetByteArrayRegion(userId, i, 1, reinterpret_cast<jbyte *>(&userIdNew[i]));

    for (jsize i = 0; i < passwordSize; ++i)
        env->GetByteArrayRegion(password, i, 1, reinterpret_cast<jbyte *>(&passwordNew[i]));
    i_mes->RegisterObserver(client);
    i_mes->Login(userIdNew, passwordNew, policy, client);
    return code_result;
}

JNI_CALL(void, nativeSend)(JNIEnv *env, jclass caller, jstring recpt, jbyteArray text) {
    const char *recpt_chars = env->GetStringUTFChars(recpt, 0);
    std::string recpt_str(recpt_chars, 50);
    MessageContent msg;
    msg.type = message_content_type::Text;
    jsize size_msg = env->GetArrayLength(text);
    msg.data.resize(size_msg);
    env->GetByteArrayRegion(text, 0, size_msg, reinterpret_cast<jbyte *>(&msg.data[0]));
    // copy(recpt_str.begin(), recpt_str.end(), back_inserter(msg.data));
    i_mes->SendMessage(recpt_str, msg);
    //  env->ReleaseStringUTFChars(recpt, recpt_chars);
}
JNI_CALL(jobject, nativeUsersList)(JNIEnv *env, jclass caller) {
    UserList list;
    i_mes->RequestActiveUsers(client);
    list = user_list.get_future().get();
    jclass ArrayList_class = env->FindClass("java/util/ArrayList");
    jmethodID ArrayList_init_id = env->GetMethodID(ArrayList_class, "<init>", "()V");
    jmethodID ArrayList_add_id = env->GetMethodID(ArrayList_class, "add", "(Ljava/lang/Object;)Z");
    jobject List_obj = env->NewObject(ArrayList_class, ArrayList_init_id);
    for (User item: list) {
        env->CallBooleanMethod(List_obj, ArrayList_add_id,
                               env->NewStringUTF(item.identifier.c_str()));
    }
    return List_obj;
}
JNI_CALL(void, nativeTestUserList)(JNIEnv *env, jclass caller){
    i_mes->RequestActiveUsers(client);
}
JNI_CALL(void, nativeDisconnect)(JNIEnv *env, jclass caller){
    i_mes->Disconnect();
}
JNI_CALL(jstring, testJNI)(JNIEnv *env, jobject obj) {
    return env->NewStringUTF("Hello from native code!");
}