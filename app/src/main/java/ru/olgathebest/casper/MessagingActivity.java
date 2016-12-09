package ru.olgathebest.casper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.util.Date;


/**
 * Created by Ольга on 03.12.2016.
 */

public class MessagingActivity extends Activity implements OnMessageSeen {
    public MessengerNDK messengerNDK = MessengerNDK.getMessengerNDK();
    private String opponentId;
    private String currentId;
    private EditText text;
    private ListView messagesList;
    public MessageAdapter messageAdapter;
    private byte[] testmsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);
        Intent intent = getIntent();
        opponentId = intent.getStringExtra("RECIPIENT_ID");
        currentId = intent.getStringExtra("SENDER_LOGIN");
        Log.d("I am ok", "4");
        messengerNDK.setContext(this);
        text = (EditText) findViewById(R.id.messageBodyField);
        messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        messengerNDK.addOnMsgSeen(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messengerNDK.deleteOnMsgSeen(this);
    }


    public void sendmsg(View view) {
        testmsg = UTF8.encode(text.getText().toString());
        Message msg = new Message("1", opponentId, currentId, text.getText().toString(), new Date(), StatusMsg.Sending);
       messengerNDK.putMessage(msg);
       messageAdapter.addMessage(msg, MessageAdapter.DIRECTION_OUTGOING);
       messageAdapter.notifyDataSetChanged();
        text.setText("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("About to send", "1");
                    messengerNDK.nativeSend(UTF8.encode(opponentId), testmsg);
                    Log.d("Sent", "2");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public MessageAdapter getMessageAdapter() {
        return messageAdapter;
    }

    @Override
    public void onMessageSeen(Message msg) {

        //msg.setStatus(StatusMsg.Seen);
        Log.d("Msg is about to be seen", "now native method works");
        messengerNDK.nativeMessageSeen(UTF8.encode(msg.getFrom()), UTF8.encode(msg.getId()));
      //  getMessageAdapter().notifyDataSetChanged();
        Log.d("Msg is seen", "native done");

    }

    public void onMessageReceived(Message message) {
        message.setStatus(StatusMsg.Sent);
    }
}
