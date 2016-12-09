package ru.olgathebest.casper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static android.R.id.list;
import static ru.olgathebest.casper.R.id.user;
import static ru.olgathebest.casper.R.id.users;
import static ru.olgathebest.casper.R.id.usersListView;

/**
 * Created by Ольга on 01.12.2016.
 */

public class ListUsersActivity extends Activity implements OnUserListChanged {
    public MessengerNDK messengerNDK = MessengerNDK.getMessengerNDK();
    TextView textView;
    String[] users;
    String senderId;
    private ListView usersListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userslist);
        Intent intent = getIntent();
        senderId = intent.getStringExtra("SENDER_LOGIN");
        messengerNDK.setCurrentUser(senderId);
        usersListView = (ListView) findViewById(R.id.usersListView);
    }

    public void openConversation(String[] names, int i) {
        Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
        intent.putExtra("RECIPIENT_ID", names[i]);
        intent.putExtra("SENDER_LOGIN",senderId);
        startActivity(intent);
    }

    public void disconnect(View view) {
        try {
            messengerNDK.nativeDisconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        messengerNDK.addObserver(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("qwer", "1");
                try {
                    Log.d("qwer", "2");
                    messengerNDK.nativeTestUserList();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messengerNDK.deleteObserver(this);
    }

    public void userlist(View view) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("qwer", "1");
                try {
                    Log.d("qwer", "2");
                    messengerNDK.nativeTestUserList();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }




    @Override
    public void onUserListChanged(final String[] users) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ArrayAdapter<String> namesArrayAdapter =
                        new ArrayAdapter<>(getApplicationContext(),
                                R.layout.users_list_item, users);
                usersListView.setAdapter(namesArrayAdapter);
                namesArrayAdapter.notifyDataSetChanged();
                usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                        openConversation(users, i);
                    }
                });

            }
        });
    }




}


