//SwiftChat v1.04
//Written By Ethan Rowan
//June 2017
package me.rowan.ethan.swiftchat;

import android.content.*;
import android.os.Bundle;
import android.support.design.widget.*;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static android.R.drawable.ic_media_pause;
import static android.R.drawable.ic_media_play;

public class Main extends AppCompatActivity
{
    String username = "";
    static String thisusername = "", thispassword = "";
    static String ip = "", port = "";
    static boolean connected = false;
    boolean debug = false;

    String[] data;
    String ds = "~";

    ArrayList<String> users;
    StringBuilder chatbuilder;

    Socket socket;
    PrintWriter writer;
    BufferedReader reader;

    FloatingActionButton fab;
    TextView chatArea;
    Button btnSend;
    EditText txtMessage;
    ScrollView scrollview;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        users = new ArrayList<String>();
        chatbuilder = new StringBuilder();

        Intent intent = getIntent();
        thisusername = intent.getStringExtra("username");
        thispassword = intent.getStringExtra("password");
        ip = intent.getStringExtra("ip");
        port = intent.getStringExtra("port");
        scrollview = (ScrollView) findViewById(R.id.scrollview);

        initUI();
    }

    class Connect implements Runnable
    {
        public void run()
        {
            if (thisusername.length() > 0 && thispassword.length() > 0 &&
                    ip.length() > 0 && port.length() > 0)
            {
                try
                {
                    socket = new Socket(ip, Integer.parseInt(port));
                    username = thisusername;
                    writer = new PrintWriter(socket.getOutputStream());
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    connected = true;

                    writer.println("connect" + ds + username + ds + thispassword);
                    writer.flush();

                    Thread listener = new Thread(new Listener());
                    listener.start();

                    System.out.println("Connected");
                }
                catch (Exception e)
                {
                    appendChat("Error connecting to the server. <br>", "red");
                    e.printStackTrace();
                }
            }
        }
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
            writer.println(message + "");
            writer.flush();
        }
    }

    class Listener implements Runnable
    {
        public void run()
        {
            try
            {
                if (connected)
                {
                    while (true)
                    {
                        data = reader.readLine().split(ds);

                        if (debug)
                        {
                            for (int i = 0; i < data.length; i++) appendChat("DEBUG: Data received: " +
                                    data[i] + "<br>", "green");
                        }

                        if (data[0].equals("error"))
                        {
                            appendChat("<b>SERVER: </b>" + "</font>" + data[2] + "</font>" + " <br>", "red");
                            disconnect(false);
                        }
                        if (data[0].equals("connect"))
                        {
                            users.add(data[1]);
                            if (data[1].equals(username))
                            {
                                appendChat("<b>You</b>" + " have connected. <br>", "blue");
                            }
                            else if (!data[1].equals("null"))
                                appendChat("<b>" + data[1] + "</b>" + " has connected. <br>", "blue");
                        }
                        if (data[0].equals("disconnect"))
                        {
                            users.remove(data[1]);
                            if (data[1].equals(username))
                            {
                                appendChat("<b>You</b>" + " have disconnected. <br>", "blue");
                                disconnect(false);
                                fab.setImageResource(ic_media_play);
                            }
                            else if (!data[1].equals("null"))
                                appendChat("<b>" + data[1] + "</b>" + " has disconnected. <br>", "blue");
                        }
                        if (data[0].equals("chat"))
                        {
                            appendChat("<b>" + "</font>" + data[1] + "</b>" + ": " + "</font>" + data[2] + "</font>" + " <br>", "black");
                        }
                    }
                }
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
        }
    }

    private void connect()
    {
        if (thisusername.length() > 0 && thispassword.length() > 0)
        {
            Thread connect = new Thread(new Connect());
            connect.start();

            btnSend.setEnabled(true);
            txtMessage.setEnabled(true);
            fab.setImageResource(ic_media_pause);
        }
    }

    public void println(String message)
    {
        Thread writerWrite = new Thread(new Writer(message));
        writerWrite.start();
    }

    private void disconnect(boolean sendDisconnect)
    {
        try
        {
            if (sendDisconnect)
            {
                println("disconnect" + ds + username);
            }
            users.clear();
            connected = false;
        }
        catch(Exception e)
        {
            appendChat("Failed to disconnect. <br>", "red");
        }

        if (connected == false)
        {
            btnSend.setEnabled(false);
            txtMessage.setEnabled(false);
            fab.setImageResource(ic_media_play);
        }
    }

    private void appendChat(String message, String color)
    {
        chatbuilder.append("<font size = 5><font face = \"Tahoma\">" + "<font color = " + color + ">" +
                message + "</font>");
        chatArea.post(new Runnable()
        {
            public void run()
            {
                chatArea.setText(Html.fromHtml(chatbuilder.toString()));
            }
        });
    }

    private boolean checkClientCommand(String message)
    {
        message = message.toLowerCase();
        if (message.startsWith("/debug"))
        {
            if (debug)
            {
                appendChat("Debug mode disabled. <br>", "blue");
                debug = false;
            }
            else
            {
                appendChat("Debug mode enabled. <br>", "blue");
                debug = true;
            }
            return true;
        }
        if (message.startsWith("/disconnect"))
        {
            disconnect(true);
        }

        return false;
    }

    private void sendMessage(String message)
    {
        if (message.length() > 0)
        {
            if (checkClientCommand(message) == false)
            {
                println("chat" + ds + username + ds + message);
            }
        }
        txtMessage.setText("");
    }

    public void initUI()
    {
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.writeMessage);

        chatArea = (TextView)findViewById(R.id.chatArea);
        btnSend = (Button)findViewById(R.id.btnSend);
        txtMessage = (EditText)findViewById(R.id.txtMessage);

        createListeners();
    }

    private void createListeners()
    {
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (connected == false)
                {
                    Snackbar.make(view, "Connecting to the server...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    connect();
                }
                else
                {
                    disconnect(true);
                    Snackbar.make(view, "Disconnecting from the server...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sendMessage(txtMessage.getText().toString());
            }
        });

        txtMessage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                txtMessage.setText("");
            }
        });

        txtMessage.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (event==null)
                {
                    if (actionId==EditorInfo.IME_ACTION_DONE);
                    else if (actionId==EditorInfo.IME_ACTION_NEXT);
                    else return false;
                }
                else if (actionId==EditorInfo.IME_NULL)
                {
                    if (event.getAction()==KeyEvent.ACTION_DOWN);
                    else  return true;
                }
                else  return false;

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                sendMessage(txtMessage.getText().toString());
                txtMessage.setText("");
                return true;
            }
        });
    }
}
