//SwiftChat v1.04
//Written By Ethan Rowan
//June 2017
package me.rowan.ethan.swiftchat;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import java.io.*;
import java.net.*;

public class Login extends AppCompatActivity
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
    private Button mEmailSignInButton;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initUI();
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
                e.printStackTrace();
            }
        }
    }

    private boolean tryConnect()
    {
        Thread connect = new Thread(new Connect());
        connect.start();
        try
        {
            Thread.sleep(500);
        }
        catch (Exception e){}
        if (isConnected) return true;
        else return false;
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
                    else
                    {
                        mEmailSignInButton.post(new Runnable()
                        {
                            public void run()
                            {
                                mEmailSignInButton.setFocusable(true);
                                mEmailSignInButton.setFocusableInTouchMode(true);
                                mEmailSignInButton.requestFocus();
                                mEmailSignInButton.setError("Invalid login credentials.");
                            }
                        });
                        break;
                    }
                }
            } catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
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
        mEmailView = (AutoCompleteTextView) findViewById(R.id.txtUsername);

        txtIP = (EditText) findViewById(R.id.txtIP);
        txtPort = (EditText) findViewById(R.id.txtPort);

        mPasswordView = (EditText) findViewById(R.id.txtPassword);

        fab = (FloatingActionButton) findViewById(R.id.changeToSignupFAB);

        mEmailSignInButton = (Button) findViewById(R.id.btnLogin);
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

                    if (tryConnect())
                    {
                        mEmailSignInButton.setError(null);
                        validateRequest();
                    }
                    else
                    {
                        mEmailSignInButton.setFocusable(true);
                        mEmailSignInButton.setFocusableInTouchMode(true);
                        mEmailSignInButton.requestFocus();
                        mEmailSignInButton.setError("Connection Error.");
                    }
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

        createListeners();
    }

    private void createListeners()
    {
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == R.id.login || id == EditorInfo.IME_NULL)
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
                Intent signup = new Intent(Login.this, SignUp.class);
                Login.this.startActivity(signup);
            }
        });
    }
}
