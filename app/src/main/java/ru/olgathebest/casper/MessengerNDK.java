package ru.olgathebest.casper;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import static android.R.attr.port;



/**
 * Created by Ольга on 28.11.2016.
 */

public class MessengerNDK {
    private byte[] login = {'l','o'};
    private byte[] pass = {'l','o'};
    private String serverUrl = "93.188.161.205";
    private int serverPort = 5222;
    private boolean security = true;
    private MessagingActivity context;
    private static final MessengerNDK messengerNDK = new MessengerNDK();
    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("mymodule");
    }
    public static MessengerNDK getMessengerNDK() {
        return messengerNDK;
    }
//    public MessengerNDK(){}
//    public void reciever(int result){
//        Log.d("msg recieved", ""+result);
//    }
    public void setContext(MessagingActivity context){
        this.context = context;
    }
//    public MessengerNDK(MessagingActivity context){
//        this.context = context;
//    }
public void getMsg(java.lang.String sender_id, java.lang.String identifier, byte[] msg){
    final String mes = UTF8.decode(msg);
    Log.d("msg recieved", ""+mes);
    context.runOnUiThread(new Runnable() {
        @Override
        public void run() {
            //TextView txtView = (TextView) ((Activity)context).findViewById(R.id.msg);
            context.getMessageAdapter().addMessage(mes, MessageAdapter.DIRECTION_INCOMING);
           // txtView.setText(mes);
        }
    });

}
    public native String testJNI();
    public native int nativeLogin(byte[] login, byte[] pwd) throws IOException;
    public native void nativeConnect(String url, int port) throws IOException;
    public native ArrayList<String> nativeUsersList() throws IOException;
    public native void nativeSend(String userId, byte[] text)throws IOException;
}
