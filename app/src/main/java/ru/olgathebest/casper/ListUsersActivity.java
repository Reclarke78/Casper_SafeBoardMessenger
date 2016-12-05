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

public class ListUsersActivity extends Activity {
    public MessengerNDK messengerNDK = MessengerNDK.getMessengerNDK();
    TextView textView;
    String[] users;
    private ListView usersListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userslist);
        usersListView = (ListView) findViewById(R.id.usersListView);
    }

    //@Override
//public void onResume(){
//    super.onResume();
//    Log.d("I am good","1");
//    thread = new Thread(new Runnable() {
//        String[] users;
//        @Override
//        public void run() {
//            Log.d("I am good","2");
//            ArrayList<String> arr = null;
//            try {
//                arr = messengerNDK.nativeUsersList();
//            } catch (Throwable e) {
//                Log.d("I am good",e.toString());
//                e.printStackTrace();
//            }
//
//            Log.d("I am good","3");
//            users = arr.toArray(new String[arr.size()]);
//            Log.d("I am good","4");
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d("I am good","5");
//                    ListView usersListView = (ListView) findViewById(R.id.usersListView);
//                    Log.d("I am good","6");
//                    ArrayAdapter<String> namesArrayAdapter =
//                            new ArrayAdapter<String>(getApplicationContext(),
//                                    R.layout.users_list_item, users);
//                    Log.d("I am good","7");
//                    usersListView.setAdapter(namesArrayAdapter);
//                }
//            });
//
//        }
//    });
//    thread.start();
//    try {
//        thread.join();
//    } catch (InterruptedException e) {
//        e.printStackTrace();
//    }
//}
    public void openConversation(String[] names, int i) {
        Intent intent = new Intent(getApplicationContext(), MessagingActivity.class);
        intent.putExtra("RECIPIENT_ID", names[i]);
        startActivity(intent);
    }

    public void userlist(View view) {
      //  new GetUsersList(this, usersListView, messengerNDK).execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> arr = null;
                try {
                    arr = messengerNDK.nativeUsersList();
                } catch (IOException e) {
                    e.printStackTrace();

                }
                final String[] users = arr.toArray(new String[arr.size()]);
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
        }).start();

    }

    public class GetUsersList extends AsyncTask<Void, Void, String[]> {

        private final Context mContext;
        private final ListView mListView;
        private final MessengerNDK mMessengerNDK;

        public GetUsersList(Context context, ListView usersListView, MessengerNDK messengerNDK) {
            mListView = usersListView;
            mMessengerNDK = messengerNDK;
            mContext = context;

        }

        @Override
        protected String[] doInBackground(Void... voids) {
            ArrayList<String> arr = null;
            try {
                arr = mMessengerNDK.nativeUsersList();
            } catch (IOException e) {
                e.printStackTrace();

            }
            String[] users = arr.toArray(new String[arr.size()]);
            return users;
        }

        @Override
        protected void onPostExecute(final String[] result) {
            super.onPostExecute(result);
            final ArrayAdapter<String> namesArrayAdapter =
                    new ArrayAdapter<>(mContext,
                            R.layout.users_list_item, result);
            mListView.setAdapter(namesArrayAdapter);
            namesArrayAdapter.notifyDataSetChanged();
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                    openConversation(result, i);
                }
            });
        }


    }
}
