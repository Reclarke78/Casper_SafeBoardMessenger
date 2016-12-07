package ru.olgathebest.casper;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import static android.R.attr.password;

public class MainActivity extends Activity {
    private Button loginButton;
    private EditText loginField;
    private EditText passField;
    private Intent intent;
    private String login = "patakiph";
    private String pass = "bad girl";
    MessengerNDK messengerNDK = MessengerNDK.getMessengerNDK();
    private String serverUrl = "192.168.1.2";//"192.168.1.2";//"172.20.10.5"; //"93.188.161.205"
    private int serverPort = 5222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginField = (EditText) findViewById(R.id.user);
        passField = (EditText) findViewById(R.id.pwd);
    }

    public void login(View view) {
        login = loginField.getText().toString();
        pass = passField.getText().toString();
        Thread connect = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    messengerNDK.nativeConnect(serverUrl, serverPort);
                    Log.d("I am ok", "0");
                    if (messengerNDK.nativeLogin(UTF8.encode(login), UTF8.encode(pass)) == 0) {
                        Log.d("I am ok", "1");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                intent = new Intent(getApplicationContext(), ListUsersActivity.class);
                                Log.d("I am ok", "2");
                                startActivity(intent);

                            }
                        });
                    }

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        connect.start();/*
        try {
            messengerNDK.nativeConnect(serverUrl, serverPort);
            Log.d("I am ok","0");
            if (messengerNDK.nativeLogin(UTF8.encode(login), UTF8.encode(pass)) == 0) {
                Log.d("I am ok","1");
                intent = new Intent(getApplicationContext(), ListUsersActivity.class);
                Log.d("I am ok","2");
                startActivity(intent);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        */
    }

}
