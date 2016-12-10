package ru.olgathebest.casper;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static android.media.CamcorderProfile.get;


/**
 * Created by Ольга on 28.11.2016.
 */

public class MessengerNDK {
    private byte[] login = {'l', 'o'};
    private byte[] pass = {'l', 'o'};
    private String serverUrl = "93.188.161.205";
    private int serverPort = 5222;
    private boolean security = true;
    private MessagingActivity context;
    private String currentUser;
    private ArrayList<Message> messages = new ArrayList<>();
    private ArrayList<OnUserListChanged> observers = new ArrayList<>();
    private ArrayList<OnLogin> onLogins = new ArrayList<>();
    private ArrayList<OnMessageSeen> onMsgSeen = new ArrayList<>();
    private static final MessengerNDK messengerNDK = new MessengerNDK();

    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("mymodule");
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

    public void addObserver(OnUserListChanged o) {
        observers.add(o);
    }

    public void deleteObserver(OnUserListChanged o) {
        observers.remove(o);
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

    public void setContext(MessagingActivity context) {
        this.context = context;
    }

    public void getMsg(java.lang.String sender_id, java.lang.String identifier, byte[] msg, long time) {
        final String mes = UTF8.decode(msg);
        Log.d("msg recieved", identifier);
        Log.d("msg recieved", "from" + sender_id + " text:" + mes + new Date(time));
        final Message message = new Message(identifier, currentUser, sender_id, mes, new Date(), StatusMsg.Delivered);
        putMessage(message);
        //вот тут вставляем в бд
        context.insertMsg(message);
        if (onMsgSeen.size() == 0) return;
        for (int i = 0; i < onMsgSeen.size(); i++) {
            onMsgSeen.get(i).onMessageSeen(message);
        }
        Log.d("msg is read", "setting view");
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                context.getMessageAdapter().addMessage(message, MessageAdapter.DIRECTION_INCOMING);
            }
        });
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
        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).onUserListChanged(userslist);
        }

    }

    public void onMessageStatusChanged(byte[] msgIdArr, int status) {
        final String msgId = UTF8.decode(msgIdArr);
        int flag = 0;
        for (int i = 0; i < messages.size(); i++) {
            Log.d("msg id",""+messages.get(i).getId());
            Log.d("status id",""+msgId);
            if (messages.get(i).getId().equals(msgId)) {
                messages.get(i).setStatus(StatusMsg.values()[status]);
                //вот тут обновляем id
                //context.insertMsg(messages.get(i));
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
                //вот тут обновляем id
                //context.insertMsg(messages.get(i));
                Log.d("Status Changed", "" + msgId + "for 1 status:" + status);
                break;
            }
        }
        //   final int i = k;
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("Drowing sent msg", "1");
                context.getMessageAdapter().notifyDataSetChanged();
            }
        });

    }

    public void onLogin(int result) {

        for (int i = 0; i < onLogins.size(); i++) {
            onLogins.get(i).onLogin(result);
        }
    }

    public native String testJNI();

    public native void nativeTestUserList() throws IOException;

    public native void nativeDisconnect() throws IOException;

    public native void nativeLogin(byte[] login, byte[] pwd) throws IOException;

    public native void nativeConnect(String url, int port) throws IOException;

    //  public native ArrayList<String> nativeUsersList() throws IOException;

    public native void nativeSend(byte[] userId, byte[] text) throws IOException;

    public native void nativeMessageSeen(byte[] userId, byte[] msgId);
}
