//Chat System (Client)
//Running SwiftProtocol v1.06
//Written By Ethan Rowan
//June 2017
/*
 * DISCLAIMER:
 * This is my first time working with socket programming,
 * meaning that this version likely has many major security flaws.
 * The focal point of this program is not security, but
 * customizability and reliability. With that said, feel free to
 * improve upon any of my code and submit it back to me.
 */
package me.rowan.ethan;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLDocument;

import net.miginfocom.swing.MigLayout;

public class Client extends JFrame
{
	String title = "SwiftChat";
	String version = "v1.06";
	
	String username = "";
	static String thisusername, 
	thispassword, thisip;
	static int thisport;
	static Point thislocation;
	boolean connected = false;
	boolean debug = false;
	
	String[] data;
	String ds = "~";
	String lastmessage = "";
	
	ArrayList<String> users;
	StringBuilder chatbuilder;
	
	Socket socket;
	PrintWriter writer;
	BufferedReader reader;
	
	private JPanel contentPane;
	private JButton btnDisconnect;
	private JTextField txtMessage;
	private JButton btnSend;
	private JScrollPane scrollPane;
	private JEditorPane chatArea;
	private JButton btnConnect;


	public Client(String username, String password, 
			String ip, int port, Point location)
	{
		thisusername = username;
		thispassword = password;
		thislocation = location;
		thisip = ip;
		thisport = port;
		initUI();
		
		users = new ArrayList<String>();
		chatbuilder = new StringBuilder();
		
		connect();
	}
	
	class Listener implements Runnable
	{
		String stream = "";
		
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
																		data[i] + "<br>", Color.GREEN);
						}
						
						if (data[0].equals("error"))
						{
							appendChat("<b>SERVER: </b>" + data[2] + ". <br>", Color.RED);
							disconnect(false);
						}
						if (data[0].equals("connect"))
						{
							users.add(data[1]);
							if (data[1].equals(username))
								appendChat("<b>You</b>" + " have connected. <br>", Color.BLUE);
							else
								appendChat("<b>" + data[1] + "</b>" + " has connected. <br>", Color.BLUE);
						}
						if (data[0].equals("disconnect"))
						{
							users.remove(data[1]);
							if (data[1].equals(username))
							{
								appendChat("<b>You</b>" + " have disconnected. <br>", Color.BLUE);
								disconnect(false);
							}
							else if (!data[1].equals("null"))
								appendChat("<b>" + data[1] + "</b>" + " has disconnected. <br>", Color.BLUE);
						}
						if (data[0].equals("chat"))
						{
							appendChat("<b>" + data[1] + "</b>" + ": " + data[2] + " <br>", Color.BLACK);
						}
						
						chatArea.setCaretPosition(chatArea.getDocument().getLength());
					}
				}
			} 
			catch (Exception e) 
			{
				//chatArea.append("ERROR: Failed to read server data.");
			}
		}
	}
	
	private void connect()
	{
		if (thisip.length() > 0)
		{
			try
			{
				socket = new Socket(thisip, thisport);
				username = thisusername;
				writer = new PrintWriter(socket.getOutputStream());
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				connected = true;
				chatArea.setText("");
				toggleUIActions(true);
				
				writer.println("connect" + ds + username + ds + thispassword);
				writer.flush();
				
				Thread listener = new Thread(new Listener());
				listener.start();
			}
			catch (Exception e)
			{
				appendChat("Error connecting to the server. <br>", Color.RED);
			}
		}
	}
	
	private void disconnect(boolean sendDisconnect)
	{
		try
		{
			if (sendDisconnect)
			{
				writer.println("disconnect" + ds + username);
				writer.flush();
			}
			users.clear();
			toggleUIActions(false);
			connected = false;
		}
		catch(Exception e)
		{
			appendChat("Failed to disconnect. <br>", Color.RED);
		}
	}
	
	private void appendChat(String message, Color color)
	{
		String colorcode = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
		
		if (chatbuilder.length() > 0)
		{
			chatbuilder.replace(chatbuilder.length() - 7, chatbuilder.length() - 1, 
								"<font color = " + colorcode + ">" + message + "</html>");
		}
		else
		{
			chatbuilder.append("<html><font size = 5><font face = \"Tahoma\">" + "<font color = " + colorcode + ">" + 
								message + "</html>");
		}
		chatArea.setText(chatbuilder.toString());
	}
	
	private boolean checkClientCommand(String message)
	{
		message = message.toLowerCase();
		if (message.startsWith("/debug"))
		{
			if (debug)
			{
				appendChat("Debug mode disabled. <br>", Color.BLUE);
				debug = false;
			}
			else
			{
				appendChat("Debug mode enabled. <br>", Color.BLUE);
				debug = true;
			}
			return true;
		}
		else if (message.startsWith("/disconnect"))
		{
			disconnect(true);
			return true;
		}
		else if (message.startsWith("/clear"))
		{
			chatbuilder = new StringBuilder();
			chatArea.setText("");
			return true;
		}
		
		return false;
	}
	
	private void sendMessage(String message)
	{
		if (message.length() > 0)
		{
			if (checkClientCommand(message) == false)
			{
				writer.println("chat" + ds + username + ds + message);
				writer.flush();
			}
			txtMessage.setText("");
			txtMessage.requestFocus();
			lastmessage = message;
		}
	}
	
	private void toggleUIActions(boolean enabled)
	{
		btnConnect.setEnabled(!enabled);
		btnDisconnect.setEnabled(enabled);
		btnSend.setEnabled(enabled);
		txtMessage.setEnabled(enabled);
	}
	
	private void initUI()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(thislocation.x, thislocation.y, 500, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		setTitle(title + " " + version);
		contentPane.setLayout(new MigLayout("", "[468px,grow]", "[][379px,grow][29px]"));
		
		btnConnect = new JButton("Connect");
		contentPane.add(btnConnect, "flowx,cell 0 0,alignx right,grow");
		
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setEnabled(false);
		contentPane.add(btnDisconnect, "cell 0 0,alignx right,grow");
		
		scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "cell 0 1,grow");
		
		chatArea = new JEditorPane();
		chatArea.setEditable(false);
		chatArea.setContentType("text/html");
		scrollPane.setViewportView(chatArea);
		Font font = new Font("Tagoma", Font.PLAIN, 5);
	    String bodyRule = "body { font-family: " + font.getFamily() + "; " +
	            "font-size: " + font.getSize() + "pt; }";
	    ((HTMLDocument)chatArea.getDocument()).getStyleSheet().addRule(bodyRule);
		
		txtMessage = new JTextField();
		txtMessage.setEnabled(false);
		contentPane.add(txtMessage, "flowx,cell 0 2,growx");
		txtMessage.setColumns(10);
		
		btnSend = new JButton("Send");
		btnSend.setEnabled(false);
		contentPane.add(btnSend, "cell 0 2");
		createListeners();
	}
	
	private void createListeners()
	{
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				disconnect(true);
			}
		});
		
		btnDisconnect.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				disconnect(true);
			}
		});
		
		txtMessage.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "Enter");
		txtMessage.getActionMap().put("Enter", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (connected) sendMessage(txtMessage.getText());
			}
		});
		
		txtMessage.getInputMap().put(KeyStroke.getKeyStroke("UP"), "Up");
		txtMessage.getActionMap().put("Up", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (connected) txtMessage.setText(lastmessage);
			}
		});
		
		txtMessage.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "Down");
		txtMessage.getActionMap().put("Down", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (connected) txtMessage.setText("");
			}
		});
		
		btnSend.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				sendMessage(txtMessage.getText());
			}
		});
		
		btnConnect.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				connect();
			}
		});
	}
	
	public static void main(String[] args)
	{
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch (Exception e1) {}
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					Client frame = new Client(thisusername, thispassword, thisip, thisport, thislocation);
					frame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
