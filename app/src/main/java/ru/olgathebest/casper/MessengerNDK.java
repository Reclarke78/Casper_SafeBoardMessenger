package ru.olgathebest.casper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static android.R.attr.key;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.media.CamcorderProfile.get;
import static ru.olgathebest.casper.R.layout.login;
import static ru.olgathebest.casper.R.layout.userslist;


/**
 * Created by Ольга on 28.11.2016.
 */

public class MessengerNDK extends Service {

    private Context context;
    private MainActivity mainActivity = null;

    private String currentUser;
    private String opponentUser;
    private long idOfSentMsg;

    private static DBHelper dbHelper;
    private static Dao dao;

    private HashMap<String, String> users;
    private ArrayList<Message> messages = new ArrayList<>();

    private ArrayList<OnUserListChanged> onUserListChanged = new ArrayList<>();
    private ArrayList<OnLogin> onLogins = new ArrayList<>();
    private ArrayList<OnMessageSeen> onMsgSeen = new ArrayList<>();
    private ArrayList<OnMessageReceived> onMsgReceived = new ArrayList<>();
    private ArrayList<OnMessageStatusChanged> onMsgStatusChanged = new ArrayList<>();

    public static RSA rsa = new RSA(1024);
    private AES aes;
    private static final MessengerNDK messengerNDK = new MessengerNDK();

    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("mymodule");

    }

    public void setContext(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
        dao = new Dao(dbHelper);
    }
///////////////////////////////////////////CALLBACKS///////////////////////////////////////
    public void getMsg(java.lang.String sender_id, java.lang.String identifier, byte[] msg, boolean type) {
        String mes = "";
        Message message = null;
        Log.d("Type", ""+type);
        if (type==true){
            mes = UTF8.bytesToHex(msg);
            message = new Message(identifier, currentUser, sender_id, mes, new Date(), StatusMsg.Delivered);
            message.setType("1");
            putMessage(message);
        } else {

            if (isSecure(currentUser, sender_id)&& type !=true)
                mes = rsa.decrypt(UTF8.decode(msg));
            else
                mes = UTF8.decode(msg);
            Log.d("msg recieved", "from" + sender_id + " " + identifier);
            message = new Message(identifier, currentUser, sender_id, mes, new Date(), StatusMsg.Delivered);
            putMessage(message);
        }

        if (opponentUser == null || !sender_id.equals(opponentUser)) {
            mainActivity.notification(sender_id, mes, true);
        }
        //вот тут вставляем в бд
        dao.insertMsg(message);
        if (onMsgSeen.size() == 0) return;
        for (int i = 0; i < onMsgSeen.size(); i++) {
            onMsgSeen.get(i).onMessageSeen(message);
        }
        for (int i = 0; i < onMsgReceived.size(); i++) {
            onMsgReceived.get(i).onMessageReceived(message);
        }
        Log.d("msg is read", "view changed");
    }

    public void getUserList(byte[] users, int[] len, byte[] keys, int[] keylen) {
        String[] userslist = new String[len.length];
        String[] keylist = new String[len.length];
        int start = 0;
        int startkey = 0;
        for (int i = 0; i < len.length; ++i) {
            byte[] userId = new byte[len[i]];
            byte[] userKey = new byte[keylen[i]];
            for (int j = 0; j < userId.length; ++j) {
                userId[j] = users[start + j];
            }
            for (int j = 0; j < userKey.length; ++j) {
                userKey[j] = keys[startkey + j];
            }
            start += len[i];
            startkey+=keylen[i];
            userslist[i]=UTF8.decode(userId);
            keylist[i] = UTF8.decode(userKey);
        }
        for (int i = 0; i < onUserListChanged.size(); i++) {
            onUserListChanged.get(i).onUserListChanged(userslist, keylist);
        }
    }

    public void onMessageStatusChanged(byte[] msgIdArr, int status) {
        final String msgId = UTF8.decode(msgIdArr);
        int flag = 0;
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId().equals(msgId)) {
                messages.get(i).setStatus(StatusMsg.values()[status]);
                //вот тут обновляем status в БД
                dao.updateMsg(messages.get(i));
                Log.d("Status Changed", "" + msgId + " status:" + status);
                flag++;
                if (flag == 2)
                    break;
            }
        }
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId().equals("1")) {
                messages.get(i).setId(msgId);
                messages.get(i).setStatus(StatusMsg.values()[status]);
                dao.updateMsgById(messages.get(i), idOfSentMsg);
                //вот тут обновляем id и status
                Log.d("Status Changed", "" + msgId + "for 1 status:" + status);
                break;
            }
        }
        for (int i = 0; i < onMsgStatusChanged.size(); i++) {
            onMsgStatusChanged.get(i).onMessageStatusChanged();
        }
    }

    public void onLogin(int result) {
        for (int i = 0; i < onLogins.size(); i++) {
            onLogins.get(i).onLogin(result);
        }
    }

/////////////////////////////ШИФРОВАНИЕ////////////////////////////////////////
    public BigInteger getPublicKey() {
        return rsa.getN();
    }

    public boolean isSecure(String opponentUserId, String currentUserId){
        return (messengerNDK.getUserPublicKey(opponentUserId).length() > 1 && messengerNDK.getUserPublicKey(currentUserId).length() > 1);
    }
    ////////////////////////////NATIVE/////////////////////////////////

    public native String testJNI();

    public native void nativeTestUserList() throws IOException;

    public native void nativeDisconnect() throws IOException;

    public native void nativeLogin(byte[] login, byte[] pwd, byte[] key) throws IOException;

    public native void nativeConnect(String url, int port) throws IOException;

    public native void nativeSend(byte[] userId, byte[] text, int type) throws IOException;

    public native void nativeMessageSeen(byte[] userId, byte[] msgId);


    ///////////////////////////////ШЛАК////////////////////////////////
    public void setMainActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }
    public void setOpponentUser(String user){
        this.opponentUser = user;
    }
    public void setUsers(HashMap<String, String> users) {
        this.users = users;
    }

    public String getUserPublicKey(String login) {
        return users.get(login);
    }

    public void setIdOfSentMsg(long id) {
        idOfSentMsg = id;
    }

    public Dao getDao() {
        return this.dao;
    }

    public static MessengerNDK getMessengerNDK() {
        return messengerNDK;
    }

    public ArrayList<Message> getMessages() {
        return this.messages;
    }

    public void putMessage(Message msg) {
        messages.add(msg);
    }

    public void setCurrentUser(String login) {
        currentUser = login;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void addOnUserListChanged(OnUserListChanged o) {
        onUserListChanged.add(o);
    }

    public void deleteOnUserListChanged(OnUserListChanged o) {
        onUserListChanged.remove(o);
    }

    public void addOnLogin(OnLogin o) {
        onLogins.add(o);
    }

    public void deleteOnLogin(OnLogin o) {
        onLogins.remove(o);
    }

    public void addOnMsgSeen(OnMessageSeen o) {
        onMsgSeen.add(o);
    }

    public void deleteOnMsgSeen(OnMessageSeen o) {
        onMsgSeen.remove(o);
    }

    public void addOnMsgReceived(OnMessageReceived o) {
        onMsgReceived.add(o);
    }

    public void deleteOnMsgReceived(OnMessageReceived o) {
        onMsgReceived.remove(o);
    }

    public void addOnMsgStatusChanged(OnMessageStatusChanged o) {
        onMsgStatusChanged.add(o);
    }

    public void deleteOnMsgStatusChanged(OnMessageStatusChanged o) {
        onMsgStatusChanged.remove(o);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
