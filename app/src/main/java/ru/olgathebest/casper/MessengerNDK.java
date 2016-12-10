package ru.olgathebest.casper;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import static android.media.CamcorderProfile.get;


/**
 * Created by Ольга on 28.11.2016.
 */

public class MessengerNDK {
    private Context context;
    private String currentUser;
    private static DBHelper dbHelper;
    private static Dao dao;
    private ArrayList<Message> messages = new ArrayList<>();
    private ArrayList<OnUserListChanged> onUserListChanged = new ArrayList<>();
    private ArrayList<OnLogin> onLogins = new ArrayList<>();
    private ArrayList<OnMessageSeen> onMsgSeen = new ArrayList<>();
    private ArrayList<OnMessageReceived> onMsgReceived = new ArrayList<>();
    private ArrayList<OnMessageStatusChanged> onMsgStatusChanged = new ArrayList<>();
    private long idOfSentMsg;
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

    public void getMsg(java.lang.String sender_id, java.lang.String identifier, byte[] msg, long time) {
        final String mes = UTF8.decode(msg);
        Log.d("msg recieved", "from" + sender_id + " " + identifier);
        final Message message = new Message(identifier, currentUser, sender_id, mes, new Date(), StatusMsg.Delivered);
        putMessage(message);
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

    public void getUserList(byte[] users, int[] len) {
        StringBuffer usersdecoded = new StringBuffer(UTF8.decode(users));
        String[] userslist = new String[len.length];
        int start = 0;
        for (int i = 0; i < len.length; i++) {
            userslist[i] = usersdecoded.substring(start, start + len[i]);
            start += len[i];
        }
        for (int i = 0; i < onUserListChanged.size(); i++) {
            onUserListChanged.get(i).onUserListChanged(userslist);
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
                dao.updateMsgById(messages.get(i),idOfSentMsg);
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

    ////////////////////////////NATIVE/////////////////////////////////

    public native String testJNI();

    public native void nativeTestUserList() throws IOException;

    public native void nativeDisconnect() throws IOException;

    public native void nativeLogin(byte[] login, byte[] pwd) throws IOException;

    public native void nativeConnect(String url, int port) throws IOException;

    public native void nativeSend(byte[] userId, byte[] text) throws IOException;

    public native void nativeMessageSeen(byte[] userId, byte[] msgId);


    ///////////////////////////////ШЛАК////////////////////////////////

    public  void setIdOfSentMsg(long id){
        idOfSentMsg = id;
    }
    public Dao getDao(){return this.dao;}
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
}
