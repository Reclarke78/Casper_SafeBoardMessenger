package ru.olgathebest.casper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;


/**
 * Created by Ольга on 03.12.2016.
 */

public class MessagingActivity extends Activity {
    public MessengerNDK messengerNDK = MessengerNDK.getMessengerNDK();
    private String recipientId;
    private ListView messagesList;
    public MessageAdapter messageAdapter;
    private byte[] testmsg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);
        Intent intent = getIntent();
        recipientId = intent.getStringExtra("RECIPIENT_ID");
        Log.d("I am ok","4");
        messengerNDK.setContext(this);
        messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);
    }

    public void sendmsg(View view){
        EditText text = (EditText) findViewById(R.id.messageBodyField);
        testmsg = UTF8.encode(text.getText().toString());
        messageAdapter.addMessage(text.getText().toString(), MessageAdapter.DIRECTION_OUTGOING);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("I am ok","1");
                    messengerNDK.nativeSend(recipientId,testmsg);
                    Log.d("I am ok","2");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
public MessageAdapter getMessageAdapter(){
    return messageAdapter;
}
}
