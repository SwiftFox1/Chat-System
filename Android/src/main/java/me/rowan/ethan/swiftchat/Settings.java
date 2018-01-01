//SwiftChat v1.14
//Written By Ethan Rowan
//June 2017
package me.rowan.ethan.swiftchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends AppCompatActivity
{
    String username, password;
    String ds = "~";

    EditText txtnewusername, txtnewpassword;
    Button btnupdateinfo;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtnewusername = (EditText) findViewById(R.id.txtNewUsername);
        txtnewpassword = (EditText) findViewById(R.id.txtNewPassword);
        fab = (FloatingActionButton) findViewById(R.id.closesettingsfab);
        btnupdateinfo = (Button) findViewById(R.id.btnUpdate);

        Intent settings = getIntent();
        username = settings.getStringExtra("username");
        password = settings.getStringExtra("password");

        NestedScrollView sv = (NestedScrollView) findViewById(R.id.settings_scrollview);
        sv.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent e)
            {
                return true;
            }
        });

        createListeners();
    }

    @Override
    protected void onStop()
    {
        Main.settingsactive = false;
        Thread writerWrite = new Thread(new Writer("disconnect" + ds + username));
        writerWrite.start();
        super.onStop();
    }

    class Writer implements Runnable
    {
        String message;

        public Writer(String message)
        {
            this.message = message;
        }

        public void run()
        {
            Main.writer.println(message + "");
            Main.writer.flush();
        }
    }

    private void createListeners()
    {
        btnupdateinfo.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String newusername = txtnewusername.getText().toString();
                String newpassword = txtnewpassword.getText().toString();
                if (newpassword.length() > 0 && newpassword != password)
                {
                    Thread writerWrite = new Thread(new Writer("update" + ds +
                            "password" + ds + username + ds + password + ds + newpassword));
                    writerWrite.start();
                    Main.thispassword = newpassword;
                    Toast.makeText(getApplicationContext(), "Chnages saved!", Toast.LENGTH_LONG)
                            .show();

                }
                if (newusername.length() > 0 && newpassword != password)
                {
                    Thread writerWrite = new Thread(new Writer("update" + ds +
                            "username" + ds + username + ds + newusername));
                    writerWrite.start();
                    Main.thisusername = newusername;
                    Toast.makeText(getApplicationContext(), "Chnages saved!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        fab.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }
}
