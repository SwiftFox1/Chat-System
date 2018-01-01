//SwiftChat v1.15
//Written By Ethan Rowan
//June 2017
package me.rowan.ethan.swiftchat;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import java.io.*;
import java.net.*;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

public class SignUp extends AppCompatActivity
{
    String username, password;
    boolean isConnected = false;

    Socket socket;
    PrintWriter writer;
    BufferedReader reader;
    String ds = "~";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText txtIP;
    private EditText txtPort;
    private FloatingActionButton fab;
    private Button mEmailSignInButton;
    private ImageButton helpbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI()
    {
        setContentView(R.layout.activity_sign_up);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.txtUsername);

        mPasswordView = (EditText) findViewById(R.id.txtPassword);

        helpbutton = (ImageButton) findViewById(R.id.helpbuttonsignup);

        mEmailSignInButton = (Button) findViewById(R.id.btnSignup);
        mEmailSignInButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mEmailView.getText().toString().length() > 0 && mPasswordView.getText().toString().length() > 0 &&
                        txtIP.getText().toString().length() > 0 && txtPort.getText().toString().length() > 0)
                {
                    if (mEmailView.getText().toString().length() > 0) mEmailView.setError(null);
                    if (mPasswordView.getText().toString().length() > 0) mPasswordView.setError(null);
                    if (txtIP.getText().toString().length() > 0) txtIP.setError(null);
                    if (txtPort.getText().toString().length() > 0) txtPort.setError(null);
                    mEmailSignInButton.setError(null);

                    tryConnect();
                }
                else
                {
                    if (mEmailView.getText().toString().length() < 1) mEmailView.setError("Username is too short.");
                    if (mPasswordView.getText().toString().length() < 1) mPasswordView.setError("Password is too short.");
                    if (txtIP.getText().toString().length() < 1) txtIP.setError("IP Address is too short.");
                    if (txtPort.getText().toString().length() < 1) txtPort.setError("Port is too short.");
                }
            }
        });

        txtIP = (EditText) findViewById(R.id.txtIP);
        txtPort = (EditText) findViewById(R.id.txtPort);

        fab = (FloatingActionButton) findViewById(R.id.changeToLoginFAB);

        createListeners();

        NestedScrollView sv = (NestedScrollView) findViewById(R.id.signup_scrollview);
        sv.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent e)
            {
                return true;
            }
        });

        if (new PrefManager(this).isTaptargetFirstTimeLaunch())
        {
            new MaterialTapTargetPrompt.Builder(SignUp.this)
                    .setTarget(findViewById(R.id.txtUsername))
                    .setPrimaryText("Username")
                    .setSecondaryText("Choose a name to uniquely identify yourself. Remember, this username will " +
                            "only work for the server you are connecting to now.")
                    .setPromptBackground(new RectanglePromptBackground())
                    .setPromptFocal(new RectanglePromptFocal())
                    .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                    {
                        @Override
                        public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                        {
                            if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED ||
                                    state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED)
                            {
                                new MaterialTapTargetPrompt.Builder(SignUp.this)
                                        .setTarget(findViewById(R.id.txtPassword))
                                        .setPrimaryText("Password")
                                        .setSecondaryText("Create a unique password for this server. Remember, " +
                                                "this password will only work for this server you are connecting to now.")
                                        .setPromptBackground(new RectanglePromptBackground())
                                        .setPromptFocal(new RectanglePromptFocal())
                                        .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                                        {
                                            @Override
                                            public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                                            {
                                                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED ||
                                                        state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED)
                                                {
                                                    new MaterialTapTargetPrompt.Builder(SignUp.this)
                                                            .setTarget(findViewById(R.id.txtIP))
                                                            .setPrimaryText("IP Address")
                                                            .setSecondaryText("The unique set of numbers that identifies the server.")
                                                            .setPromptBackground(new RectanglePromptBackground())
                                                            .setPromptFocal(new RectanglePromptFocal())
                                                            .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                                                            {
                                                                @Override
                                                                public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                                                                {
                                                                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED ||
                                                                            state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED)
                                                                    {
                                                                        new MaterialTapTargetPrompt.Builder(SignUp.this)
                                                                                .setTarget(findViewById(R.id.txtPort))
                                                                                .setPrimaryText("Port Number")
                                                                                .setSecondaryText("The other number that identifies the server.")
                                                                                .setPromptBackground(new RectanglePromptBackground())
                                                                                .setPromptFocal(new RectanglePromptFocal())
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

    public void createToast(final String message)
    {
        findViewById(R.id.signup_scrollview).post(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    private void createListeners()
    {
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.login_scrollview || id == EditorInfo.IME_NULL)
                {
                    return true;
                }
                return false;
            }
        });
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                mPasswordView.requestFocus();
                return false;
            }
        });
        txtIP.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                txtIP.clearFocus();
                txtPort.requestFocus();
                return false;
            }
        });

        helpbutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("http://swiftfoxgames.weebly.com/swiftchat.html"));
                startActivity(browser);
            }
        });
    }

    private void tryConnect()
    {
        Thread connect = new Thread(new Connect());
        connect.start();
        Thread loginlistener = new Thread(new SignUpListener());
        loginlistener.start();
    }

    class SignUpListener implements Runnable
    {
        public void run()
        {
            try
            {
                Thread.sleep(500);
            }
            catch (Exception e) {}
            if (isConnected)
                connectionHandler();
            else
            {
                createToast("Connection failed, timed out.");
            }
        }
    }

    class Connect implements Runnable
    {
        public void run()
        {
            try
            {
                socket = new Socket(txtIP.getText().toString(), Integer.parseInt(txtPort.getText().toString()));
                writer = new PrintWriter(socket.getOutputStream());
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                isConnected = true;
            }
            catch (Exception e)
            {

            }
        }
    }

    private void connectionHandler()
    {
        println("request" + ds + "signup" + ds + mEmailView.getText().toString() + ds + mPasswordView.getText().toString());
        Thread connectionhandler = new Thread(new ConnectionHandler());
        connectionhandler.start();
    }

    class ConnectionHandler implements Runnable
    {
        public void run()
        {
            mEmailView.post(new Runnable()
            {
                public void run()
                {
                    mEmailView.setError(null);
                }
            });
            mEmailSignInButton.post(new Runnable()
            {
                public void run()
                {
                    mEmailSignInButton.setError(null);
                }
            });

            try
            {
                while (true)
                {
                    String[] data = reader.readLine().split(ds);

                    if (data[2].equals("Account created"))
                    {
                        username = mEmailView.getText().toString();
                        password = mPasswordView.getText().toString();
                        Intent client = new Intent(SignUp.this, Main.class);
                        client.putExtra("username", username);
                        client.putExtra("password", password);
                        client.putExtra("ip", txtIP.getText().toString());
                        client.putExtra("port", txtPort.getText().toString());
                        SignUp.this.startActivity(client);
                        break;
                    }
                    else if (data[2].equals("Username is taken"))
                    {
                        mEmailView.post(new Runnable()
                        {
                            public void run()
                            {
                                mEmailView.setError("Username is taken.");
                            }
                        });
                        break;
                    }
                    else if (data[2].equals("Illegal characters"))
                    {
                        mEmailView.post(new Runnable()
                        {
                            public void run()
                            {
                                mEmailView.setError("Illegal characters used. (~)");
                            }
                        });
                        mPasswordView.post(new Runnable()
                        {
                            public void run()
                            {
                                mPasswordView.setError("Illegal characters used. (~)");
                            }
                        });
                    }
                }
            }
            catch (Exception e1)
            {
                createToast("Unknown connection error.");
            }
        }
    }

    public void println(String message)
    {
        Thread writerWrite = new Thread(new Writer(message));
        writerWrite.start();
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
}
