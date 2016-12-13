package ru.olgathebest.casper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static android.R.id.list;
import static ru.olgathebest.casper.R.id.src_over;
import static ru.olgathebest.casper.R.id.user;
import static ru.olgathebest.casper.R.id.users;
import static ru.olgathebest.casper.R.id.usersListView;

/**
 * Created by Ольга on 01.12.2016.
 */

public class ListUsersActivity extends Activity implements OnUserListChanged {
    final String ATTRIBUTE_NAME_LOGIN = "Login";
    final String ATTRIBUTE_NAME_SECURITY = "Security";
    public static final String ANOTHER_USER_LOGIN = "ANOTHER_USER_LOGIN";
    public MessengerNDK messengerNDK = MessengerNDK.getMessengerNDK();
    private ListView usersListView;
    private String publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userslist);
        publicKey = getIntent().getStringExtra(MainActivity.PUBLIC_KEY);
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
            System.exit(0);
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
                try {
                    messengerNDK.nativeTestUserList();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void onUserListChanged(final String[] users, final String[] keys) {
        Log.d("KEYS", "" + publicKey.equals(keys[0]));
        HashMap<String, String> userlist = new HashMap<>();
        for (int i = 0; i < users.length; i++) {
            userlist.put(users[i], keys[i]);
        }
        messengerNDK.setUsers(userlist);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<HashMap<String, Object>> myArrList = new ArrayList<HashMap<String, Object>>();
                HashMap<String, Object> map;
                for (int i = 0; i < users.length; i++) {
                    map = new HashMap<String, Object>();
                    map.put(ATTRIBUTE_NAME_LOGIN, users[i]);
                    if (keys[i].length() > 0)
                        map.put(ATTRIBUTE_NAME_SECURITY, 0);
                    else
                        map.put(ATTRIBUTE_NAME_SECURITY, 1);
                    myArrList.add(map);
                }
                MySimpleAdapter adapter = new MySimpleAdapter(getApplicationContext(), myArrList, R.layout.users_list_item,
                        new String[]{ATTRIBUTE_NAME_LOGIN, ATTRIBUTE_NAME_SECURITY},
                        new int[]{R.id.userListItem, R.id.secure});
//                final ArrayAdapter<String> namesArrayAdapter =
//                        new ArrayAdapter<>(getApplicationContext(),
//                                R.layout.users_list_item, users);
                usersListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                // namesArrayAdapter.notifyDataSetChanged();
                usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                        openConversation(users, i);
                    }
                });

            }
        });
    }


    class MySimpleAdapter extends SimpleAdapter {

        public MySimpleAdapter(Context context,
                               List<? extends Map<String, ?>> data, int resource,
                               String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public void setViewImage(ImageView v, int value) {
            // метод супер-класса
            super.setViewImage(v, value);
            // разрисовываем ImageView
            if (value == 1) v.setVisibility(View.VISIBLE);
            else if (value == 0) v.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        System.exit(0);
    }
}


