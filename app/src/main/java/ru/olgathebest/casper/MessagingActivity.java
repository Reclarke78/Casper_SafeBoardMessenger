package ru.olgathebest.casper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static ru.olgathebest.casper.R.id.msg;


/**
 * Created by Ольга on 03.12.2016.
 */

public class MessagingActivity extends Activity {
    public MessengerNDK messengerNDK = MessengerNDK.getMessengerNDK();
    public static final MessagingActivity messagingActivity = new MessagingActivity();
    private String recipientId;
    private byte[] testmsg = {'k','l'};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);
        Intent intent = getIntent();
        recipientId = intent.getStringExtra("RECIPIENT_ID");
        Log.d("I am ok","4");
        messengerNDK.setContext(this);
    }

    public void sendmsg(View view){
        EditText text = (EditText) findViewById(R.id.messageBodyField);
        testmsg = UTF8.encode(text.getText().toString());
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

public static MessagingActivity getMessagingActivity(){
    return messagingActivity;
}
}
