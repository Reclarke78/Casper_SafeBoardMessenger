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
    public static final String ANOTHER_USER_LOGIN="ANOTHER_USER_LOGIN";
    public MessengerNDK messengerNDK = MessengerNDK.getMessengerNDK();
    private ListView usersListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userslist);
        usersListView = (ListView) findViewById(R.id.usersListView);
    }

    public void openConversation(String[] names, int i) {
        Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
        intent.putExtra(ANOTHER_USER_LOGIN, names[i]);
        startActivity(intent);
    }

    public void disconnect(View view) {
        try {
            messengerNDK.nativeDisconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        messengerNDK.addOnUserListChanged(this);
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
        messengerNDK.deleteOnUserListChanged(this);
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


