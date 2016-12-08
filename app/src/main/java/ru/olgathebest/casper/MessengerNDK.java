package ru.olgathebest.casper;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Observer;

import static android.R.attr.port;
import static ru.olgathebest.casper.R.id.usersListView;
import static ru.olgathebest.casper.R.layout.userslist;


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
    private ArrayList<OnUserListChanged> observers = new ArrayList<>();
    private ArrayList<OnLogin> onLogins = new ArrayList<>();
    private static final MessengerNDK messengerNDK = new MessengerNDK();

    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("mymodule");
    }

    public static MessengerNDK getMessengerNDK() {
        return messengerNDK;
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

    public void setContext(MessagingActivity context) {
        this.context = context;
    }

    public void getMsg(java.lang.String sender_id, java.lang.String identifier, byte[] msg, long time) {
        final String mes = UTF8.decode(msg);
        Log.d("msg recieved", identifier);
        Log.d("msg recieved", "" + mes + new Date(time));
        Message message = new Message(identifier,"",sender_id,mes, new Date(time),StatusMsg.Delivered);
       // nativeMessageSeen();
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                context.getMessageAdapter().addMessage(mes, MessageAdapter.DIRECTION_INCOMING);
            }
        });

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
        String msgId = UTF8.decode(msgIdArr);
        Log.d("Status Changed", "" + msgId + " status:" + status);
    }

    public void onLogin() {
        for (int i = 0; i < onLogins.size(); i++) {
            onLogins.get(i).onLogin();
        }
    }

    public native String testJNI();

    public native void nativeTestUserList() throws IOException;

    public native void nativeDisconnect() throws IOException;

    public native int nativeLogin(byte[] login, byte[] pwd) throws IOException;

    public native void nativeConnect(String url, int port) throws IOException;

  //  public native ArrayList<String> nativeUsersList() throws IOException;

    public native void nativeSend(byte[] userId, byte[] text) throws IOException;

    public native void nativeMessageSeen(byte[] userId, byte[] msgId);
}
