package ru.olgathebest.casper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.id.message;
import static ru.olgathebest.casper.R.id.db;


/**
 * Created by Ольга on 03.12.2016.
 */

public class MessagingActivity extends Activity implements OnMessageSeen, OnMessageReceived, OnMessageStatusChanged {
    public MessengerNDK messengerNDK = MessengerNDK.getMessengerNDK();
    private String opponentUserId;
    private String currentUserId;
    private EditText messageText;
    private ListView messagesList;
    public MessageAdapter messageAdapter;
    private byte[] encodedMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);
        Intent intent = getIntent();
        opponentUserId = intent.getStringExtra(ListUsersActivity.ANOTHER_USER_LOGIN);
        currentUserId = messengerNDK.getCurrentUser();
        // messengerNDK.setContext(this);
        messageText = (EditText) findViewById(R.id.messageBodyField);
        messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);
        loadHistory();
    }

    @Override
    public void onStart() {
        super.onStart();
        messengerNDK.addOnMsgSeen(this);
        messengerNDK.addOnMsgReceived(this);
        messengerNDK.addOnMsgStatusChanged(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messengerNDK.deleteOnMsgSeen(this);
        messengerNDK.deleteOnMsgReceived(this);
        messengerNDK.deleteOnMsgStatusChanged(this);
    }


    public void sendmsg(View view) {
        encodedMsg = UTF8.encode(messageText.getText().toString());
        Message msg = new Message("1", opponentUserId, currentUserId, messageText.getText().toString(), new Date(), StatusMsg.Sending);
        messengerNDK.putMessage(msg);
        messengerNDK.setIdOfSentMsg(messengerNDK.getDao().insertMsg(msg));
        messageAdapter.addMessage(msg, MessageAdapter.DIRECTION_OUTGOING);
        //messageAdapter.notifyDataSetChanged();
        messageText.setText("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    messengerNDK.nativeSend(UTF8.encode(opponentUserId), encodedMsg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void loadHistory() {
        //ArrayList<Message> messages = null;
        new Thread(new Runnable() {
            ArrayList<Message> messages = null;
            @Override
            public void run() {
                messages = (ArrayList<Message>) messengerNDK.getDao().getAllWhere(currentUserId, opponentUserId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (messages != null)
                            for (int i = 0; i < messages.size(); i++) {
                                if (messages.get(i).getFrom().equals(currentUserId) && messages.get(i).getTo().equals(opponentUserId))
                                    messageAdapter.addMessage(messages.get(i), MessageAdapter.DIRECTION_OUTGOING);
                                else if (messages.get(i).getFrom().equals(opponentUserId) && messages.get(i).getTo().equals(currentUserId))
                                    messageAdapter.addMessage(messages.get(i), MessageAdapter.DIRECTION_INCOMING);
                            }
                    }
                });
            }
        }).start();

    }

    public MessageAdapter getMessageAdapter() {
        return messageAdapter;
    }

    @Override
    public void onMessageSeen(Message msg) {
        messengerNDK.nativeMessageSeen(UTF8.encode(msg.getFrom()), UTF8.encode(msg.getId()));
    }

    @Override
    public void onMessageReceived(final Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMessageAdapter().addMessage(message, MessageAdapter.DIRECTION_INCOMING);
            }
        });
    }

    @Override
    public void onMessageStatusChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getMessageAdapter().notifyDataSetChanged();
            }
        });
    }
}
