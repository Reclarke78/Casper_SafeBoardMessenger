package ru.olgathebest.casper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Message;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import static android.R.attr.password;
import static javax.crypto.Cipher.PUBLIC_KEY;

public class MainActivity extends Activity implements OnLogin {
    private EditText loginField;
    private EditText passField;
    private CheckBox isSecureCheckBox;
    private boolean isSecure = true;
    private Intent intent;
    private String login = "patakiph";
    private String pass = "bad girl";
    public static final String PUBLIC_KEY = "Public Key";
    MessengerNDK messengerNDK = MessengerNDK.getMessengerNDK();
    private String serverUrl = "195.123.211.113";//"192.168.1.2";//"172.20.10.5"; //"93.188.161.205"
    private int serverPort = 5222;
    private ProgressDialog pd;
    private EmailValidator emailValidator = new EmailValidator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginField = (EditText) findViewById(R.id.user);
        passField = (EditText) findViewById(R.id.pwd);
        Button button = (Button) findViewById(R.id.db);
        isSecureCheckBox = (CheckBox) findViewById(R.id.security);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent dbmanager = new Intent(getApplicationContext(), AndroidDatabaseManager.class);
                startActivity(dbmanager);
            }
        });
        messengerNDK.setContext(getApplicationContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        messengerNDK.addOnLogin(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messengerNDK.deleteOnLogin(this);
    }

    public void login(View view) {
        login = loginField.getText().toString();

        pass = passField.getText().toString();
        isSecure = isSecureCheckBox.isChecked();
        if (!emailValidator.validate(login)){
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Use your email!", Toast.LENGTH_SHORT);
            toast.show();
        }
        else if (login.equals("") || pass.equals("")) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "All fields should be filled in!", Toast.LENGTH_SHORT);
            toast.show();
        } else if (!isNetworkAvailable()) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No Internet connection!", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            pd = new ProgressDialog(this);
            pd.setMax(10000);
            pd.show(this, "Loading", "Wait while loading...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        messengerNDK.nativeConnect(serverUrl, serverPort);
                        BigInteger keyb = BigInteger.ONE;
                        byte[] key;
                        if (isSecure) {
                            keyb = messengerNDK.getPublicKey();
                        }
                            String str = keyb.toString();
                            key = str.getBytes();

                        messengerNDK.nativeLogin(UTF8.encode(login), UTF8.encode(pass), key);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onLogin(int result) {
        if (result == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    intent = new Intent(getApplicationContext(), ListUsersActivity.class);
                    intent.putExtra(PUBLIC_KEY,messengerNDK.getPublicKey().toString());
                    messengerNDK.setCurrentUser(login);
                    startActivity(intent);

                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Connection error, check server is up!", Toast.LENGTH_SHORT);
                    toast.show();
                    /*костыль, тут нужно как-то останавливать прогресс бар*/
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            });
        }

    }
}
