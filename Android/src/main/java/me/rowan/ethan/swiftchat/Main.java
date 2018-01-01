//SwiftChat v1.14
//Written By Ethan Rowan
//June 2017
package me.rowan.ethan.swiftchat;

import android.content.*;
import android.os.Bundle;
import android.support.design.widget.*;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.*;
import android.widget.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class Main extends AppCompatActivity
{
    String username = "";
    static String thisusername = "", thispassword = "";
    static String ip = "", port = "";
    static boolean connected = false, settingsactive = false;
    boolean debug = false, isFabOpen = false;

    String[] data;
    String ds = "~";

    ArrayList<String> users;
    StringBuilder chatbuilder;

    Socket socket;
    static PrintWriter writer;
    BufferedReader reader;

    FloatingActionButton fab1, fab2, fab3, sendmessagefab;
    Animation fab_open,fab_close,rotate_forward,rotate_backward;
    TextView chatArea;
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

    @Override
    protected void onStop()
    {
        if (connected && settingsactive == false)
            disconnect(true);
        connected = false;
        super.onStop();
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

                    txtMessage.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            txtMessage.setEnabled(true);
                        }
                    });
                    fab2.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            fab2.setImageResource(R.drawable.pause);
                        }
                    });

                    findViewById(R.id.scrollview).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_SHORT)
                                .show();
                        }
                    });
                }
                catch (Exception e)
                {
                    appendChat("Error connecting to the server. <br>", "red");
                    e.printStackTrace();
                    findViewById(R.id.scrollview).post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(getApplicationContext(), "Failed to connect.", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
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
            if (connected)
            {
                while (true)
                {
                    try
                    {
                        data = reader.readLine().split(ds);

                        if (debug)
                        {
                            for (int i = 0; i < data.length; i++)
                                appendChat("DEBUG: Data received: " +
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
                            } else if (!data[1].equals("null"))
                                appendChat("<b>" + data[1] + "</b>" + " has connected. <br>", "blue");
                        }
                        if (data[0].equals("disconnect"))
                        {
                            users.remove(data[1]);
                            if (data[1].equals(username))
                            {
                                appendChat("<b>You</b>" + " have disconnected. <br>", "blue");
                                disconnect(false);
                                fab2.post(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        fab2.setImageResource(R.drawable.play);
                                    }
                                });
                            } else if (!data[1].equals("null"))
                                appendChat("<b>" + data[1] + "</b>" + " has disconnected. <br>", "blue");
                        }
                        if (data[0].equals("chat"))
                        {
                            appendChat("<b>" + "</font>" + data[1] + "</b>" + ": " + "</font>" + data[2] + "</font>" + " <br>", "black");
                        }

                        NestedScrollView sv = (NestedScrollView) findViewById(R.id.scrollview);
                        sv.scrollTo(0, sv.getBottom());
                    }
                    catch (Exception e)
                    {

                    }
                }
            }
        }
    }

    private void connect()
    {
        if (thisusername.length() > 0 && thispassword.length() > 0)
        {
            Thread connect = new Thread(new Connect());
            connect.start();
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
                println("disconnect" + ds + username);
            users.clear();
            connected = false;
        }
        catch(Exception e)
        {
            appendChat("Failed to disconnect. <br>", "red");
        }

        if (connected == false)
        {
            txtMessage.setEnabled(false);
            fab2.post(new Runnable()
            {
                @Override
                public void run()
                {
                    fab2.setImageResource(R.drawable.play);
                }
            });

            findViewById(R.id.scrollview).post(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(getApplicationContext(), "Disconnected.", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
        else
        {
            findViewById(R.id.scrollview).post(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(getApplicationContext(), "Failed to disconnect.", Toast.LENGTH_SHORT)
                            .show();
                }
            });
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
        else if (message.startsWith("/disconnect"))
        {
            disconnect(true);
        }
        else if (message.startsWith("/clear"))
        {
            chatbuilder = new StringBuilder();
            appendChat("", "blue");
            return true;
        }

        return false;
    }

    private void sendMessage(String message)
    {
        if (message.length() > 0)
            if (checkClientCommand(message) == false)
                println("chat" + ds + username + ds + message);
        txtMessage.setText("");
    }

    public void animateFAB()
    {
        if(isFabOpen)
        {
            fab1.startAnimation(rotate_backward);
            fab2.startAnimation(fab_close);
            fab3.startAnimation(fab_close);
            fab2.setClickable(false);
            fab3.setClickable(false);
            isFabOpen = false;
        }
        else
        {
            fab1.startAnimation(rotate_forward);
            fab2.startAnimation(fab_open);
            fab3.startAnimation(fab_open);
            fab2.setClickable(true);
            fab3.setClickable(true);
            isFabOpen = true;
        }
    }

    public void initUI()
    {
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab1 = (FloatingActionButton) findViewById(R.id.menufab);
        fab2 = (FloatingActionButton) findViewById(R.id.connectanddisconnectfab);
        fab3 = (FloatingActionButton) findViewById(R.id.settingsfab);

        fab2.setVisibility(View.INVISIBLE);
        fab2.setClickable(false);
        fab3.setVisibility(View.INVISIBLE);
        fab3.setClickable(false);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forwards);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backwards);

        sendmessagefab = (FloatingActionButton) findViewById(R.id.sendmessagefab);

        chatArea = (TextView)findViewById(R.id.chatArea);
        txtMessage = (EditText)findViewById(R.id.txtMessage);
        txtMessage.setVisibility(View.INVISIBLE);

        createListeners();

        if (new PrefManager(this).isTaptargetFirstTimeLaunch())
        {
            new MaterialTapTargetPrompt.Builder(Main.this)
                    .setTarget(findViewById(R.id.sendmessagefab))
                    .setPrimaryText("Send a Message")
                    .setSecondaryText("Tap here to open up the keyboard (once you are connected to the server).")
                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                    {
                        @Override
                        public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                        {
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED ||
                                    state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED)
                            {
                                new MaterialTapTargetPrompt.Builder(Main.this)
                                        .setTarget(findViewById(R.id.menufab))
                                        .setPrimaryText("Menu Button")
                                        .setSecondaryText("Tap here to reveal hidden options.")
                                        .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                                        {
                                            @Override
                                            public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                                            {
                                                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED ||
                                                        state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED)
                                                {
                                                    new MaterialTapTargetPrompt.Builder(Main.this)
                                                            .setTarget(findViewById(R.id.connectanddisconnectfab))
                                                            .setPrimaryText("Connect & Disconnect")
                                                            .setSecondaryText("Tap here to connect and disconnect from the server.")
                                                            .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                                                            {
                                                                @Override
                                                                public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                                                                {
                                                                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED ||
                                                                            state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED)
                                                                    {
                                                                        new MaterialTapTargetPrompt.Builder(Main.this)
                                                                                .setTarget(findViewById(R.id.settingsfab))
                                                                                .setPrimaryText("Settings")
                                                                                .setSecondaryText("Tap here to manage your account settings specific to this server.")
                                                                                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                                                                                {
                                                                                    @Override
                                                                                    public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                                                                                    {
                                                                                        new PrefManager(Main.this).setTaptargetFirstTimeLaunch(false);
                                                                                    }
                                                                                })
                                                                                .show();
                                                                    }
                                                                }
                                                            })
                                                            .show();
                                                }
                                            }
                                        })
                                        .show();
                            }
                        }
                    })
                    .show();
        }
    }

    private void createListeners()
    {
        fab1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                animateFAB();
            }
        });

        fab2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (connected == false)
                    connect();
                else
                    disconnect(true);
            }
        });

        fab3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (connected && writer != null)
                {
                    Intent settings = new Intent(Main.this, Settings.class);
                    settings.putExtra("username", thisusername);
                    settings.putExtra("password", thispassword);
                    Main.this.startActivity(settings);
                    settingsactive = true;
                }
                else
                    Toast.makeText(getApplicationContext(), "You must be connected to " +
                            "the server before you can access your settings.", Toast.LENGTH_LONG)
                            .show();
            }
        });

        sendmessagefab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (connected && txtMessage.getText().toString().length() == 0)
                {
                    txtMessage.setVisibility(View.VISIBLE);
                    txtMessage.requestFocus();
                    txtMessage.post(new Runnable()
                    {
                        public void run()
                        {
                            txtMessage.requestFocusFromTouch();
                            InputMethodManager lManager = (InputMethodManager) Main.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                            lManager.showSoftInput(txtMessage, 0);
                        }
                    });
                }
                else if (connected)
                    sendMessage(txtMessage.getText().toString());
                else
                    Toast.makeText(getApplicationContext(), "You must be connected to " +
                            "the server before you can send messages.", Toast.LENGTH_LONG)
                            .show();
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
                if (event == null)
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE);
                    else if (actionId == EditorInfo.IME_ACTION_NEXT);
                    else return false;
                }
                else if (actionId == EditorInfo.IME_NULL)
                {
                    if (event.getAction() == KeyEvent.ACTION_DOWN);
                    else  return true;
                }
                else  return false;

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                sendMessage(txtMessage.getText().toString());
                txtMessage.setText("");
                txtMessage.setVisibility(View.INVISIBLE);
                return true;
            }
        });
    }
}
