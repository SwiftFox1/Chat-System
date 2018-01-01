//SwiftChat v1.15
//Written By Ethan Rowan
//June 2017
package me.rowan.ethan.swiftchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
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

public class Login extends AppCompatActivity
{
    String username, password;
    boolean isConnected = false;

    Socket socket;
    PrintWriter writer;
    BufferedReader reader;
    String ds = "~";

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText txtIP;
    private EditText txtPort;
    private Button mEmailSignInButton;
    private FloatingActionButton fab;
    private ImageButton helpbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        initUI();

        NestedScrollView sv = (NestedScrollView) findViewById(R.id.login_scrollview);
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
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    new MaterialTapTargetPrompt.Builder(Login.this)
                            .setTarget(findViewById(R.id.changeToSignupFAB))
                            .setPrimaryText("Create your first account")
                            .setSecondaryText("Tap here to sign up for a server.")
                            .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener()
                            {
                                @Override
                                public void onPromptStateChanged(MaterialTapTargetPrompt prompt, int state)
                                {
                                    if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED ||
                                            state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED)
                                    {
                                        new MaterialTapTargetPrompt.Builder(Login.this)
                                                .setTarget(findViewById(R.id.helpbuttonlogin))
                                                .setPrimaryText("Server setup help")
                                                .setSecondaryText("Tap here to learn about creating your own server.")
                                                .show();
                                    }
                                }
                            })
                            .show();
                }
            }, 1000);
        }
    }

    public void createToast(final String message)
    {
        findViewById(R.id.login_scrollview).post(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
                        .show();
            }
        });
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
                isConnected = false;
            }
        }
    }

    class LoginListener implements Runnable
    {
        public void run()
        {
            try
            {
                Thread.sleep(500);
            }
            catch (Exception e) {}
            if (isConnected)
                validateRequest();
            else
            {
                createToast("Connection failed, timed out.");
            }
        }
    }

    class ValidateRequest implements Runnable
    {
        public void run()
        {
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

                    if (data[2].equals("Success"))
                    {
                        username = mEmailView.getText().toString();
                        password = mPasswordView.getText().toString();
                        disconnect();
                        Intent client = new Intent(Login.this, Main.class);
                        client.putExtra("username", username);
                        client.putExtra("password", password);
                        client.putExtra("ip", txtIP.getText().toString());
                        client.putExtra("port", txtPort.getText().toString());
                        Login.this.startActivity(client);
                        break;
                    }
                    else if (data[2].equals("Incorrect login"))
                    {
                        createToast("Invalid login credentials.");
                        break;
                    }
                    else if (data[2].equals("Account in use"))
                    {
                        createToast("Account in use.");
                        break;
                    }
                }
            }
            catch (Exception e1)
            {
                createToast("Unknown connection error.");
                e1.printStackTrace();
            }
        }
    }

    private void tryConnect()
    {
        Thread connect = new Thread(new Connect());
        connect.start();
        Thread loginlistener = new Thread(new LoginListener());
        loginlistener.start();
    }

    private void validateRequest()
    {
        println("request" + ds + "login" + ds + mEmailView.getText().toString() + ds + mPasswordView.getText().toString());
        Thread validate = new Thread(new ValidateRequest());
        validate.start();
    }

    private void disconnect()
    {
        try
        {
            println("disconnect" + ds + "null");
            socket.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
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

    private void initUI()
    {
        setContentView(R.layout.activity_login);
        mEmailView = (EditText) findViewById(R.id.txtUsername);

        txtIP = (EditText) findViewById(R.id.txtIP);
        txtPort = (EditText) findViewById(R.id.txtPort);

        mPasswordView = (EditText) findViewById(R.id.txtPassword);
        fab = (FloatingActionButton) findViewById(R.id.changeToSignupFAB);
        mEmailSignInButton = (Button) findViewById(R.id.btnLogin);

        helpbutton = (ImageButton) findViewById(R.id.helpbuttonlogin);

        createListeners();
    }

    private void createListeners()
    {
        mEmailSignInButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (txtIP.getText().toString().length() > 0 &&
                        txtPort.getText().toString().length() > 0)
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

        mEmailSignInButton.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus == false)
                    mEmailSignInButton.setError(null);
            }
        });

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.login_scrollview || id == EditorInfo.IME_NULL)
                    return true;
                return false;
            }
        });

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent signup = new Intent(Login.this, SignUp.class);
                Login.this.startActivity(signup);
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
                txtIP.clearFocus();
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
}
