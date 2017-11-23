//Chat System (Server)
//Written By Ethan Rowan
//June-October 2017
/*
 * DISCLAIMER:
 * This is my first time working with socket programming,
 * meaning that this version likely has many major security
 * flaws. The focal point of this project is not security,
 * but customizability and reliability. With that said,
 * feel free to improve upon any of my code.
 */
package me.rowan.ethan;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import net.miginfocom.swing.MigLayout;

public class Server extends JFrame
{
	String title = "SwiftChat";
	String version = "v1.1";
			
	int port = 5555;
	String[] data;    //The data is information received by each
					  //client. This is how the server communicates
					  //with each individual client.
	String ds = "~";  //This is the "splitter". It divides
					  //the datainto readable information.
	
	//Serverside Objects
	ServerSocket servsocket;
	//The clientoutputstreams list allows the server to
	//ommunicate withany client using their clientID.
	ArrayList<PrintWriter> clientOutputStreams;
	//The clientsockets and clientthreads lists store
	//the sockets and running threads of each client
	//so that they can be accessed and closed remotely.
	ArrayList<Socket> clientSockets;
	ArrayList<Thread> clientThreads;
	//The clientID is the identification number of each individual
	//client. This allows the client to be identified.
	HashMap<Integer, Integer> clientIDs;
	//The users hashmap stores the clientID and name of each user in both orders.
	//This allows ease of access to both data from anywhere in the class.
	HashMap users;
	HashMap usercolor;
	//The users and admin models store the information
	//that is displayed in the JLists of the UI.
	DefaultListModel<String> usersModel;
	DefaultListModel<String> adminModel;
	//Logs the chat window slightly differently than what is displayed.
	//This can be chosen to be saved or destroyed when the server is closed.
	StringBuilder chatlog;
	
	//UI Objects
	private JPanel contentPane;
	private JButton btnStart;
	private JButton btnStop;
	private JTextArea consoleArea;
	private JScrollPane scrollPane;
	private JLabel lblOnlineUsers;
	private JLabel lblConsole;
	private JScrollPane scrollPane_1;
	private JList<String> usersList;
	private JList<String> adminsList;
	private JScrollPane scrollPane_2;
	private JLabel lblAdmins;
	private JButton btnAddAdmin;
	private JButton btnRemoveAdmin;
	private JTextField txtAdminName;
	private JLabel lblPort;
	private JTextField txtPort;

	
	public Server()
	{
		initUI();
		
		clientOutputStreams = new ArrayList<PrintWriter>();
		clientSockets = new ArrayList<Socket>();
		clientThreads = new ArrayList<Thread>();
		clientIDs = new HashMap<Integer, Integer>();
		users = new HashMap();
		usercolor = new HashMap();
		chatlog = new StringBuilder();
	}
	
   //A listener is created for each client so that the
   //server canhear and respond to all client activity.
	class Listener implements Runnable
	{
		int clientID;
		boolean isConnected = true;
		Socket clientSocket;
		PrintWriter writer;
		BufferedReader reader;
		
		public Listener(int clientID, Socket clientSocket, PrintWriter writer)
		{
			this.clientSocket = clientSocket;
			this.writer = writer;
			this.clientID = clientID;
			clientSockets.add(clientSocket);
			try
			{
				reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} 
			catch (IOException e)
			{
				consoleArea.append("Failed to create client reader. Has user disconnected? \n");
			}
		}
		
		public void run()
		{
			String stream = "";
				
			try
			{
				while((stream = reader.readLine()) != null)
				{				
					try
					{
						data = stream.split(ds);
						
						for (int i = 0; i < data.length; i++)
							consoleArea.append("Data recieved: " + data[i] + "\n");
						
						//A connect messaage will add a specified user to the
						//server's cache, and notify all other connected users.
						if (data[0].equals("connect"))
						{
							try
							{
								//FORMAT: connect÷username÷password
								//INDEX:     0       1         2
								ArrayList<String> banlist = readFileToList("bans.list");
								if (users.containsKey(data[1]))
								{
									//Force disconnects the user if they have an invalid username.
									writer.println("error" + ds + "disconnect" + ds + "Invalid Username");
									writer.flush();
									clientOutputStreams.get(clientID).close();
									clientSocket.close();
									clientIDs.remove(clientID);
								}
								if (banlist.contains(data[1]))
								{
									//Force disconnects the user if they are banned.
									writer.println("error" + ds + "disconnect" + ds + "You are banned from this server");
									writer.flush();
									clientOutputStreams.get(clientID).close();
									clientSocket.close();
									clientIDs.remove(clientID);
								}
								else
								{
									//Grabs the latest username and password lists from the local file.
									ArrayList<String> usernamesList = readAccountsList("username");
									ArrayList<String> passwordsList = readAccountsList("password");
								
									//Compares the credentials to the main accounts file.
									if (usernamesList.contains(data[1]) &&
											passwordsList.contains(data[2]) &&
											(usernamesList.indexOf(data[1])) == (passwordsList.indexOf(data[2])))
									{
										if (!users.containsKey(data[2]))
										{
											//Successful connects the user if their credentials match.
											users.put(data[1], clientID);
											users.put(clientID, data[1]);
											usercolor.put(data[1], "black");
											usersModel.addElement(data[1]);
											if (data[1].equals("null") == false)
											{
												broadcast("connect" + ds + users.get(clientID));
												chatlog.append("[" + getTime() + "]  -" + 
												users.get(clientID) + " connected to the server-\n");
											}
											if (isAdmin(data[1]))
												addAdmin(data[1]);
										}
										else
										{
											//Force disconnects the user if their account 
											//is already in use by another client.
											writer.println("error" + ds + "disconnect" + ds + "Account in use");
											writer.flush();
											clientOutputStreams.get(clientID).close();
											clientSocket.close();
											clientIDs.remove(clientID);
										}
									}
									else
									{
										//Force disconnects the user if they have invalid credentials.
										writer.println("error" + ds + "disconnect" + ds + "Incorrect login");
										writer.flush();
										clientOutputStreams.get(clientID).close();
										clientSocket.close();
										clientIDs.remove(clientID);
									}
								}
							}
							catch (Exception e)
							{
								//Force disconnects the user if anything at all goes wrong in this process.
								//The most common case of this error is if the user's client is modified or outdated.
								writer.println("error" + ds + "disconnect" + ds + 
										"Internal error. Check your client version");
								writer.flush();
								clientOutputStreams.get(clientID).close();
								clientSocket.close();
								clientIDs.remove(clientID);
							}
						}
						if (data[0].equals("disconnect") && isConnected)
						{
							//FORMAT: disconnect÷username
							//INDEX:       0        1
							if (data[1].equals("null") == false)
							{
								broadcast("disconnect" + ds + "</font>" + users.get(clientID));
								chatlog.append("[" + getTime() + "]  -" + 
										users.get(clientID) + " disconnected from the server-\n");
							}
							clientIDs.remove(clientID);
							clientOutputStreams.get(clientID).close();
							clientSocket.close();
							users.remove(data[1]);
							users.remove(clientID);
							usercolor.remove(data[1]);
							usersModel.removeElement(data[1]);
							if (isAdmin(data[1]))
								adminModel.removeElement(data[1]);
							break;
						}
						//Any user communication data that is sent from one
						//endpoint to another is considered a "chat" message.
						if (data[0].equals("chat"))
						{
							//FORMAT: chat÷username÷message
							//INDEX:    0      1       2
							if (users.containsKey(data[1]))
							{
								if (data[2].startsWith("/"))
								{
									//Debug info that displays the
									//exact data recieved from the client.
									String[] args = data[2].split(" ");
									for (int i = 0; i < args.length; i++)
										consoleArea.append("Data recieved: " + args[i] + "\n");
									
									//ADMIN COMMANDS
									if (checkAdminCommands(clientID, args));
									
									//NON-ADMIN COMMANDS
									else if (checkNonAdminCommands(clientID, args));
									
									//SHARED COMMANDS
									else if (checkSharedCommands(clientID, args));
									
									else
										pmUser(clientID, "<font color = red>Invalid command.</font>");
								}
								else
								{
									for (int i = 0; i < readFileToList("filterwords.list").size(); i++)
									{
										if (data[2].contains(readFileToList("filterwords.list").get(i)))
										{
											String word = readFileToList("filterwords.list").get(i);
											String newword = "";
											for (int t = 0; t < word.length(); t++) newword += "*";
											int index = data[2].indexOf(word);
											data[2] = data[2].substring(0, index) + newword + 
													data[2].substring(index + word.length(), data[2].length());
										}
									}
									
									if (data[2].contains("<font color"))
									{
										broadcast("chat" + ds + "(" + getUserPrefix(data[1]) + 
												") </font><font color = " + usercolor.get(data[1]) + ">" + 
												data[1] + ds + "</font>" + data[2] + "</font>");
									}
									else
									{
										broadcast("chat" + ds + "(" + getUserPrefix(data[1]) + ") </font><font color = " + 
												usercolor.get(data[1]) + ">" + data[1] + ds + 
												"</font><font color = black>" + data[2] + "</font>");
									}
									
									chatlog.append("[" + getTime() + "]  " + data[1] + ": " + data[2] + "\n");
								}
							}
							else
							{
								consoleArea.append(data[1] + "[" + clientSocket.getInetAddress() + "]" + 
										" tried to send a message illegally.\nThey may be using a modified client.\n");
								chatlog.append("[" + getTime() + "]  -----{" + clientSocket.getInetAddress() + "}" + 
										" tried to send a message illegally.\nThey may be using a modified client-----\n");
							}
						}
						//A request can be made from the client when it needs
						//data, for example when signing up and loging in.
						if (data[0].equals("request"))
						{
							//FORMAT: request÷type÷username÷password
							//INDEX:    0      1      2         3
							if (data[1].equals("signup"))
							{
								File file = new File("accounts.list");
								if (!file.exists()) file.createNewFile();
								
								BufferedReader filereader = new BufferedReader(new FileReader(file));
								String line = "", accounts = "";
								while ((line = filereader.readLine()) != null)
									accounts += line + "\n";
								filereader.close();
								
								//Check the data string for any illegal characters.
								for (int i = 0; i < data.length; i++)
								{
									if (data[i].contains(" "))
									{
										consoleArea.append("Failed to complete signup process for client " + clientID + ". \n" +
												" Username or password contained illegal characters. \n");
										writer.println("response" + ds + "signup" + ds + "Illegal characters");
										writer.flush();
									}
								}
								
								ArrayList<String> usernamesList = readAccountsList("username");
								//Check the username and password for divider characters.
								if (data.length > 4 || data[2].equals("") || data[3].equals(""))
								{
									consoleArea.append("Failed to complete signup process for client " + clientID + ". \n" +
											" Username or password contained illegal characters. \n");
									writer.println("response" + ds + "signup" + ds + "Illegal characters");
									writer.flush();
								}
								
								//Check that the username isn't already taken.
								else if (usernamesList.contains(data[2]))
								{
									consoleArea.append("Failed to complete signup process for client " + clientID + ". \n" +
											" Username was taken. \n");
									writer.println("response" + ds + "signup" + ds + "Username is taken");
									writer.flush();
								}
								
								else
								{	
									BufferedWriter filewriter = new BufferedWriter(new FileWriter(file));
									filewriter.write(accounts + data[2] + "  " + data[3] + "  " + "notadmin  " + "\n");
									filewriter.close();
									
									writer.println("response" + ds + "signup" + ds + "Account created");
									writer.flush();
								}
							}
							else if (data[1].equals("login"))
							{
								ArrayList<String> usernamesList = readAccountsList("username");
								ArrayList<String> passwordsList = readAccountsList("password");
								
								if (usernamesList.contains(data[2]) &&
										passwordsList.contains(data[3]) &&
										(usernamesList.indexOf(data[2])) == (passwordsList.indexOf(data[3])))
								{
									if (!users.containsKey(data[2]))
									{
										writer.println("response" + ds + "login" + ds + "Success");
										writer.flush();
										consoleArea.append("Sent response success to client " + clientID + ". \n");
									}
									else
									{
										writer.println("response" + ds + "login" + ds + "Account in use");
										writer.flush();
										consoleArea.append("Sent response success to client " + clientID + ". \n");
									}
								}
								else
								{
									writer.println("response" + ds + "login" + ds + "Incorrent login");
									writer.flush();
									consoleArea.append("Sent response incorrect to client " + clientID + ". \n");
								}
							}
							
							clientIDs.remove(clientID);
							clientOutputStreams.get(clientID).close();
							clientSocket.close();
							break;
						}
						else if (data[0].equals("update"))
						{
							if (data[1].equals("username"))
							{
								ArrayList<String> usernamesList = readAccountsList("username");
								if (usernamesList.contains(data[2]))
									if (usernamesList.contains(data[3]) == false)
										editAccountsFile("username", data[2], data[3]);
							}
							else if (data[1].equals("password"))
							{
								ArrayList<String> usernamesList = readAccountsList("username");
								ArrayList<String> passwordsList = readAccountsList("password");
							
								//Compares the credentials to the main accounts file.
								if (usernamesList.contains(data[2]) &&
										passwordsList.contains(data[3]) &&
										(usernamesList.indexOf(data[2])) == (passwordsList.indexOf(data[3])))
								{
									editAccountsFile("password", data[2], data[4]);
								}
							}
						}
					}
					catch (Exception e)
					{
						if (clientSocket.isClosed() == false)
						{
							consoleArea.append("ERROR: Failed to read a client's data. \n");
							chatlog.append("[" + getTime() + "]  ERROR: Failed to read a client's data.\n");
							e.printStackTrace();
						}
					}
					consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
    //Checks for special commands for administrators and moderators.
    //(Will only run for admins.)
	//Returns true to be marked as a valid command.
	private boolean checkAdminCommands(int clientID, String[] args)
	{
		if (isAdmin((String)users.get(clientID)))
		{
			if (args[0].equalsIgnoreCase("/help") && args.length == 1)
			{
				//I tried to make this easier to read.
				pmUser(clientID, "<font color = black><br>"
				   + "/help<br> "
				   + "/debug<br> "
				   + "/clear<br> "
				   + "/disconnect<br> "
				   + "/pm &lt;user&gt; &lt;message&gt;<br> "
				   + "/listusers<br> "
				   + "/version<br> "
				   + "/broadcast &lt;message&gt;<br> "
				   + "/kick &lt;user&gt;<br> "
				   + "/ban &lt;user&gt;<br> "
				   + "/unban &lt;user&gt;<br> "
				   + "/stopserver<br> "
				   + "/restartserver");
				return true;
			}
			
			else if (args[0].equalsIgnoreCase("/broadcast") && args.length >= 2)
			{
				String message = "";
				for (int i = 1; i < args.length; i++) message += (args[i] + " ");
				broadcast("chat" + ds + "</font><font color = \"red\">SERVER" + ds + 
						"</font><font color = \"red\"></font>" + message);
				return true;
			}
			
			else if (args[0].equalsIgnoreCase("/stopserver") && args.length == 1)
			{
				broadcast("chat" + ds + "</font><font color = \"red\">SERVER" + ds + 
							"</font><font color = \"red\">Server has been closed.</font>");
				disconnectAllUsers();
				stopServer();
				System.exit(0);
				return true;
			}
			
			else if (args[0].equalsIgnoreCase("/restartserver") && args.length == 1)
			{
				broadcast("chat" + ds + "</font><font color = \"red\">SERVER" + ds + 
							"</font><font color = \"red\">Server is restarting.</font>");
				stopServer();
				disconnectAllUsers();
				
				users.clear();
				usercolor.clear();
				clientOutputStreams.clear();
				usersModel.clear();
				adminModel.clear();
				//Stops the main thread so that all others threads
				//can be closed while the main thread is sleeping.
				try
					{ Thread.sleep(5000); }
				catch(Exception e)
					{ System.exit(0); }
				
				startServer();
				return true;
			}
			
			else if (args[0].equalsIgnoreCase("/kick"))
			{
				if (args.length == 2)
				{
					if (users.containsKey(args[1]))
						kickUser(args[1], "kick", args, (int)users.get(args[1]));
					else
						pmUser(clientID, "</font><font color = red>That user does not exist.</font>");
				}
				else if (args.length < 2)
					pmUser(clientID, "</font><font color = red>You need to specify a user to kick.</font>");
				else if (args.length > 2)
					pmUser(clientID, "</font><font color = red>That user does not exist.</font>");
				return true;
			}
			
			else if (args[0].equalsIgnoreCase("/ban"))
			{
				if (args.length == 2)
				{
					if (users.containsKey(args[1]))
					{
						int otherclientID = (int)users.get(args[1]);
						kickUser(args[1], "ban", args, otherclientID);
						banUser(args[1], otherclientID);
					}
					else
						pmUser(clientID, "</font><font color = red>That user does not exist.</font>");
				}
				else if (args.length < 2)
					pmUser(clientID, "</font><font color = red>You need to specify a user to ban.</font>");
				else if (args.length > 2)
					pmUser(clientID, "</font><font color = red>That user does not exist.</font>");
				return true;
			}
			
			else if (args[0].equalsIgnoreCase("/unban"))
			{
				if (args.length == 2)
					unbanUser(args[1], clientID);
				else if (args.length < 2)
					pmUser(clientID, "</font><font color = red>You need to specify a user to unban.</font>");
				else if (args.length > 2)
					pmUser(clientID, "</font><font color = red>That user does not exist.</font>");
				return true;
			}
		}
		return false;
	}
	
    //Checks for regular commands for anyone who is not admin.
    //(Will only run for non-admins. Will not run for admins.)
	//Returns true to be marked as a valid command.
	private boolean checkNonAdminCommands(int clientID, String[] args)
	{
		if (isAdmin((String)users.get(clientID)) == false)
		{
			if (args[0].equalsIgnoreCase("/help") && args.length == 1)
			{
				//I tried to make this easier to read.
				pmUser(clientID, "<font color = black><br>"
					+ "/help<br> "
					+ "/debug<br> "
					+ "/clear<br> "
				    + "/pm &lt;user&gt; &lt;message&gt;<br> "
					+ "/disconnect<br> "
					+ "/listusers");
				return true;
			}
		}
		return false;
	}
	
    //Checks for commands that don't require admin or non-admin.
    //(Will run for admin and non admins.)
    //Returns true to be marked as a valid command.
	private boolean checkSharedCommands(int clientID, String[] args)
	{
		if (args[0].equalsIgnoreCase("/pm"))
		{
			if (args.length >= 3)
			{
				if (users.containsKey(args[1]))
				{
					String message = "";
					for (int i = 2; i < args.length; i++) message += (args[i] + " ");
					pmOtherUser(clientID, args[1], message);
					pmUser(clientID, message);
				}
				else
					pmUser(clientID, "</font><font color = red>That user does not exist.</font>");
			}
			else if (args.length == 2)
				pmUser(clientID, "</font><font color = red>You have to specify a message to send.</font>");
			else if (args.length == 1)
				pmUser(clientID, "</font><font color = red>You have to specify a message to send<br>" + 
							"and a user to send the message to.</font>");
			return true;
		}
		
		else if (args[0].equalsIgnoreCase("/listusers") && args.length == 1)
		{
			String onlineusers = "";
			for (int i = 0; i < usersModel.size(); i++)
			{
				onlineusers += "<font color = " + (usercolor.get(usersModel.get(i)) + ">" + usersModel.get(i));
				if (i + 1 < usersModel.size()) onlineusers += "</font>, ";
			}
			pmUser(clientID, "</font><font color = red></font>" + onlineusers + "<font size = 5>");
			return true;
		}
		
		else if (args[0].equalsIgnoreCase("/version") && args.length == 1)
		{
			pmUser(clientID, "</font><font color = red></font>The server is running " + version + ".<font size = 5>");
			return true;
		}
		return false;
	}
	
	//Reads any specified file and outputs its contents to an arraylist.
	//Note that this is a more general way to read from system file.
	private ArrayList<String> readFileToList(String filename)
	{
		ArrayList<String> words = new ArrayList<String>();
		
		try
		{
			File file = new File(filename);
			if (!file.exists()) file.createNewFile();
		
			BufferedReader filereader = new BufferedReader(new FileReader(file));
			String line = "";
			while ((line = filereader.readLine()) != null)
				words.add(line.replace("\n", ""));
			filereader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return words;
	}
	
    //Returns a list of any data related to the user
    //that the server holds on file.
	private ArrayList<String> readAccountsList(String type)
	{
		ArrayList<String> accountsList = new ArrayList<String>();
		
		try
		{
			File file = new File("accounts.list");
			if (!file.exists()) file.createNewFile();
		
			BufferedReader filereader = new BufferedReader(new FileReader(file));
			String line = "";
			while ((line = filereader.readLine()) != null)
			{
				String[] acc = line.split("  ");
				
				if (type.equalsIgnoreCase("all"))
					accountsList.add(line);
				else if (type.equalsIgnoreCase("username"))
					accountsList.add(acc[0]);
				else if (type.equalsIgnoreCase("password"))
					accountsList.add(acc[1]);
				else if (type.equalsIgnoreCase("admin"))
					accountsList.add(acc[2]);
				else if (type.equalsIgnoreCase("prefix"))
					accountsList.add(acc[3]);
			}
			filereader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return accountsList;
	}
	
	private void editAccountsFile(String type, String username, String newitem)
	{
		ArrayList<String> accounts = readAccountsList("all");
		String accountsString = "";
		int index = 0;
		for (int i = 0; i < accounts.size(); i++)
		{
			if (accounts.get(i).startsWith(username) && 
					accounts.get(i).charAt(username.length()) == ' ') 
					index = i;
		}
		String[] acc = accounts.get(index).split("  ");
		String accString = "";
		
		for (int i = 0; i < acc.length; i++)
		{
			if (i == 0 && type == "username") 
				accString += (newitem + "  ");
			else if (i == 1 && type == "password")
				accString += (newitem + "  ");
			else if (i == 2 && type == "isadmin")
				accString += (newitem + "  ");
			else accString += acc[i] + "  ";
		}
		accounts.set(index, accString);
		
		for (int i = 0; i < accounts.size(); i++)
			accountsString += accounts.get(i) + "\n";
		try
		{
			File file = new File("accounts.list");
			if (!file.exists()) file.createNewFile();
			BufferedWriter filewriter = new BufferedWriter(new FileWriter(file));
			filewriter.write(accountsString);
			filewriter.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//Checks the accounts.list file first to see if the specified
	//user has the keyword "isadmin". It otherwise checks if the
	//user is already on the admin list from the server's memory.
	private boolean isAdmin(String username)
	{
		ArrayList<String> accounts = readAccountsList("all");
		String[] acc = { "" };
		
		for (int i = 0; i < accounts.size(); i++)
		{
			if (accounts.get(i).startsWith(username) && 
					accounts.get(i).charAt(username.length()) == ' ')
				acc = accounts.get(i).split("  ");
		}
		if (acc[2].equalsIgnoreCase("isadmin"))
			return true;
		
		return adminModel.contains(username);
	}
	
    //Broadcasts the specified message to all connected users
    //using the clientOutputStream. Type: chat
	private void broadcast(String message)
	{
		int count = 0;
		for (int i = 0; i < clientOutputStreams.size(); i++)
		{
			if (clientIDs.containsKey(i))
			{
				clientOutputStreams.get(i).println(message);
				clientOutputStreams.get(i).flush();
				count++;
			}
		}
		consoleArea.append("Sent message to " + count + " clients. \n");
	}
	
    //Sends the specified message to the specified user.
    //Used by the server to address a user.
	private void pmUser(int clientID, String message)
	{
		PrintWriter writer = clientOutputStreams.get(clientID);
		writer.println("chat" + ds + "</font><font color = \"red\">SERVER" + ds + "</font>" + message + "</font>");
		writer.flush();
		consoleArea.append("Sent message from " + clientID + " to client " + clientID + "\n");
	}
	
    //Send the specified message to the specified user from the specified client.
    //Used in the "pm" command. Could also be used to "impersonate" someone.
	private void pmOtherUser(int clientID, String username, String message)
	{
		int otherClientID = (int)users.get(username);
		PrintWriter writer = clientOutputStreams.get(otherClientID);
		PrintWriter thisClientWriter = clientOutputStreams.get(clientID);
		
		for (int i = 0; i < readFileToList("filterwords.list").size(); i++)
		{
			if (data[2].contains(readFileToList("filterwords.list").get(i)))
			{
				String word = readFileToList("filterwords.list").get(i);
				String newword = "";
				for (int t = 0; t < word.length(); t++) newword += "*";
				int index = message.indexOf(word);
				message = message.substring(0, index) + newword + 
						message.substring(index + word.length(), message.length());
			}
		}
		
		if (data[2].contains("<font color"))
		{
			writer.println("chat" + ds + "<font color = \"red\">[PM]" + 
						"</font> (" + getUserPrefix((String)users.get(clientID)) + ") </font>" + "<font color = " + 
						usercolor.get(users.get(clientID)) + ">" + users.get(clientID) + ds + "</font>" + 
						message + "</font>");
			thisClientWriter.println("chat" + ds + "<font color = \"red\">[PM]" + 
					"</font> (" + getUserPrefix((String)users.get(clientID)) + ") </font>" + "<font color = " + 
					usercolor.get(users.get(clientID)) + ">" + users.get(clientID) + ds + "</font>" + 
					message + "</font>");
		}
		else
		{
			writer.println("chat" + ds + "<font color = \"red\">[PM]" + 
					"</font> (" + getUserPrefix((String)users.get(clientID)) + ") </font><font color = " + 
					usercolor.get(users.get(clientID)) + ">" + users.get(clientID) + ds + 
					"</font><font color = black>" + message + "</font>");
			thisClientWriter.println("chat" + ds + "<font color = \"red\">[PM]" + 
					"</font> (" + getUserPrefix((String)users.get(clientID)) + ") </font><font color = " + 
					usercolor.get(users.get(clientID)) + ">" + users.get(clientID) + ds + 
					"</font><font color = black>" + message + "</font>");
		}
		writer.flush();
		consoleArea.append("Sent message from " + clientID + " to client " + otherClientID + "\n");
	}
	
	//Returns the HTML formatted prefix string for the specified user.
	private String getUserPrefix(String username)
	{
		String prefix = "<font color = #6E6E6E>Guest</font><font color = black>";
		ArrayList<String> accounts = readAccountsList("all");
		int index = 0;
		for (int i = 0; i < accounts.size(); i++)
		{
			if (accounts.get(i).startsWith(username) && 
					accounts.get(i).charAt(username.length()) == ' ') 
					index = i;
		}
		String[] acc = accounts.get(index).split("  ");
		if (acc.length >= 4)
			prefix = acc[3] + "</font>";
		return prefix;
	}
	
	//Closes the sockets for each client.
	//The server remains open.
	private void disconnectAllUsers()
	{
		for (int i = 0; i < clientOutputStreams.size(); i++)
		{
			if (clientIDs.containsKey(i)) broadcast("disconnect" + ds + "</font>" + users.get(i));
			clientOutputStreams.get(i).close();
			try 
				{ clientSockets.get(i).close(); } 
			catch (IOException e)
				{ e.printStackTrace(); }
		}
	}
	
   //Marks the specified user as an admin.
   //This gives them special permissions and commands.
	private void addAdmin(String username)
	{
		if (username.length() > 0 && !adminModel.contains(username))
		{
			adminModel.addElement(username);
			usercolor.put(username, "red");
			PrintWriter writer = clientOutputStreams.get((int)users.get(username));
			writer.println("chat" + ds + "<font color = \"red\">SERVER" + 
							ds + "<font color = \"red\">You are now an admin.");
			writer.flush();
			editAccountsFile("isadmin", username, "isadmin");
		}
	}
	
	//Removes special permissions and commands
	//from the specified user.
	private void removeAdmin(String username)
	{
		if (username.length() > 0 && adminModel.contains(username))
		{
			adminModel.removeElement(username);
			usercolor.put(username, "black");
			PrintWriter writer = clientOutputStreams.get((int)users.get(username));
			writer.println("chat" + ds + "<font color = \"red\">SERVER" + 
							ds + "<font color = \"red\">You are no longer an admin.");
			writer.flush();
			editAccountsFile("isadmin", username, "notadmin");
		}
	}
	
	//Kicks the user from the server, and gives them a warning.
	//This method is also used to assist the ban method.
	private void kickUser(String username, String type, String args[], int clientID)
	{
		broadcast("disconnect" + ds + users.get(users.get(args[1])));
		if (type.equalsIgnoreCase("kick"))
			broadcast("chat" + ds + "</font><font color = \"red\">SERVER" + ds + 
						"</font><font color = \"red\">User \"" + args[1] + "\" has been kicked.</font>");
		else if (type.equalsIgnoreCase("ban"))
			
		clientOutputStreams.get((int)users.get(args[1])).close();
		users.remove(users.get(args[1]));
		users.remove(args[1]);
		usercolor.remove(args[1]);
		usersModel.removeElement(args[1]);
		clientIDs.remove(clientID);
	}
	
	//Adds the specified user to a "banlist". Any user
	//on the list is prevented from joining the server.
	//All players are notified of this event.
	private void banUser(String username, int clientID)
	{
		ArrayList<String> banlist = readFileToList("bans.list");
		if (!banlist.contains(username.replace(System.getProperty("line.separator"), "")))
		{
			banlist.add(username + "\n");
			writeBanList(banlist);
			
			broadcast("chat" + ds + "</font><font color = \"red\">SERVER" + ds + 
					"</font><font color = \"red\">User \"" + username + "\" has been banned.</font>");
		}
		else
			pmUser(clientID, "<font color = red>User is already banned.</font>");
	}
	
	//Removed the specified user from the banlist.
	//All players are notified of this event.
	private void unbanUser(String username, int clientID)
	{
		ArrayList<String> banlist = readFileToList("bans.list");
			
		if (banlist.contains(username))
		{
			banlist.remove(username);
			writeBanList(banlist);
			
			broadcast("chat" + ds + "</font><font color = \"red\">SERVER" + ds + 
					"</font><font color = \"red\">User \"" + username + "\" has been unbanned</font>");
		}
		else
			pmUser(clientID, "<font color = red>User has not been banned.</font>");
	}
	
	//Writes all the contents of the specified banlist into the banlist file.
	private void writeBanList(ArrayList<String> newBanList)
	{
		ArrayList<String> bannedUsers = newBanList;
		File file = new File("bans.list");
		
		try
		{
			if (!file.exists()) file.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i < bannedUsers.size(); i++)
				writer.write(bannedUsers.get(i) + "\n");
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//Creates a seperate thread to initially start the server,
	//and to listen for incoming connections.
	private void startServer()
	{
		Thread startServer = new Thread(new ServerStart());
		startServer.start();
	}
	
	//Attempts to stop the server, and send a message to
	//each connected client. Any error that occurs will cancel
	//the operation, and "disconnect" all clients from the server.
	private void stopServer()
	{
		broadcast("chat" + ds + "</font><font color = \"red\">SERVER" + ds + 
				"</font><font color = \"red\">Server has been closed.</font>");
		consoleArea.append("Stopping server... \n");
		chatlog.append("[" + getTime() + "]  Stopping server...\n");
		
		disconnectAllUsers();
		users.clear();
		usersModel.clear();
		adminModel.clear();
		
		try
		{
			servsocket.close();
			toggleUIActions(false);
			
			consoleArea.append("Server stopped. \n");
			chatlog.append("[" + getTime() + "]  Server stopped.\n");
		}
		catch (Exception e) { consoleArea.append("ERROR: Failed to stop server."); }
	}
	
	class ServerStart implements Runnable
	{
		public void run()
		{
			port = Integer.parseInt(txtPort.getText());
			consoleArea.append("Starting server on port " + port + "... \n");
			chatlog.append("[" + getTime() + "]  Starting server on port " + port + "...\n");
			
			try
			{
				servsocket = new ServerSocket(port);
				toggleUIActions(true);
				consoleArea.append("Server started. \n");
				chatlog.append("[" + getTime() + "]  Server started.\n");
				
				consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
				
				if (!new File("bans.list").exists())
					new File("bans.list").createNewFile();
				if (!new File("accounts.list").exists())
					new File("accounts.list").createNewFile();
				if (!new File("filterwords.list").exists())
					new File("filterwords.list").createNewFile();
				
				while(true)
				{
					Socket clientSocket = servsocket.accept();
					PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
					
					clientOutputStreams.add(writer);
					consoleArea.append("Got a connection. Connecting client #" + 
									  (clientOutputStreams.size() -1) + "\n");
					clientThreads.add(new Thread(new Listener(clientOutputStreams.size() - 1, 
															clientSocket, writer)));
					clientThreads.get(clientThreads.size() - 1).start();
					clientIDs.put(clientOutputStreams.size() - 1, clientOutputStreams.size() - 1);
				}
			}
			catch (Exception e)
			{
				//consoleArea.append("ERROR: Failed to create server. \n");
			}
		}
	}
	
	//Simply toggles the UI on or off in a specific configuration.
	private void toggleUIActions(boolean enabled)
	{
		btnStart.setEnabled(!enabled);
		btnStop.setEnabled(enabled);
		btnAddAdmin.setEnabled(enabled);
		btnRemoveAdmin.setEnabled(enabled);
		txtAdminName.setEnabled(enabled);
		txtPort.setEnabled(!enabled);
	}
	
	//Returns the current time (properly formatted).
	private String getTime()
	{
		Calendar c = Calendar.getInstance();
		SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
		return time.format(c.getTime());
	}
	
	//Setup swing components.
	private void initUI()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 750, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		setTitle(title + " " + version);
		
		btnStop = new JButton("Stop");
		btnStop.setEnabled(false);
		contentPane.setLayout(new MigLayout("", "[371.00px,grow][123.00,grow][133.00,grow]", "[29px][376px,grow][29px][]"));
		
		lblConsole = new JLabel("Console");
		contentPane.add(lblConsole, "cell 0 0");
		
		lblOnlineUsers = new JLabel("Users");
		contentPane.add(lblOnlineUsers, "cell 1 0");
		
		lblAdmins = new JLabel("Admins");
		contentPane.add(lblAdmins, "cell 2 0");
		
		scrollPane_1 = new JScrollPane();
		contentPane.add(scrollPane_1, "cell 1 1,grow");
		
		usersModel = new DefaultListModel<String>();
		usersList = new JList(usersModel);
		scrollPane_1.setViewportView(usersList);
		
		scrollPane_2 = new JScrollPane();
		contentPane.add(scrollPane_2, "cell 2 1,grow");
		
		adminModel = new DefaultListModel<String>();
		adminsList = new JList(adminModel);
		scrollPane_2.setViewportView(adminsList);
		
		btnStart = new JButton("Start");
		contentPane.add(btnStart, "flowx,cell 0 2,growx,aligny top");
		contentPane.add(btnStop, "cell 0 2,growx,aligny top");
		
		scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "cell 0 1,grow");
		consoleArea = new JTextArea();
		consoleArea.setEditable(false);
		consoleArea.setFont(new Font("Tahoma", Font.PLAIN, 16));
		scrollPane.setViewportView(consoleArea);
		
		txtAdminName = new JTextField();
		txtAdminName.setEnabled(false);
		contentPane.add(txtAdminName, "cell 2 2,growx");
		txtAdminName.setColumns(10);
		
		btnRemoveAdmin = new JButton("Remove");
		btnRemoveAdmin.setEnabled(false);
		
		btnAddAdmin = new JButton("Add");
		btnAddAdmin.setEnabled(false);
		
		lblPort = new JLabel("Port:");
		contentPane.add(lblPort, "flowx,cell 0 3");
		contentPane.add(btnAddAdmin, "flowx,cell 2 3,alignx right");
		contentPane.add(btnRemoveAdmin, "cell 2 3");
		
		txtPort = new JTextField();
		txtPort.setText("5555");
		contentPane.add(txtPort, "cell 0 3 2 1");
		txtPort.setColumns(10);
		
		
		createListeners();
	}
	
	//Separated from the initUI() for easier access.
	private void createListeners()
	{
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				if (servsocket != null && !servsocket.isClosed())
					stopServer();
				consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
				
				JOptionPane savelog = new JOptionPane();
				int option = savelog.showConfirmDialog(Server.this, 
						"Would you like to save the log from this session?", 
						"Save Log", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				
				if (option == 0)
				{
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
					LocalDate localDate = LocalDate.now();
					try
					{
						BufferedWriter writer = new BufferedWriter(new FileWriter(
								new File("Server Log [" + dtf.format(localDate) + "]")));
						writer.write(chatlog.toString());
						writer.close();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		});
		
		btnStop.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				stopServer();
				consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
			}
		});
		
		btnStart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				startServer();
			}
		});
		
		btnRemoveAdmin.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				removeAdmin(txtAdminName.getText());
			}
		});
		
		btnAddAdmin.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addAdmin(txtAdminName.getText());
			}
		});
		
		//Checks if the user double clicks on a player's
		//name in the server console from the admin list.
		//This grants them admin privileges.
		usersList.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				JList list = (JList)e.getSource();
				if (e.getClickCount() == 2)
				{
					int index = list.locationToIndex(e.getPoint());
					addAdmin(usersModel.getElementAt(index));
					usersList.clearSelection();
				}
			}
		});
		
		//Checks if the user double clicks on a players's
		//name in the server console from the admin list.
		//This removes their admin privileges.
		adminsList.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				JList list = (JList)e.getSource();
				if (e.getClickCount() == 2)
				{
					int index = list.locationToIndex(e.getPoint());
					removeAdmin(adminModel.getElementAt(index));
					adminsList.clearSelection();
				}
			}
		});
		
		this.addComponentListener(new ComponentListener()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				int xdiff = (e.getComponent().getWidth() - 750) / 10;
				int ydiff = (e.getComponent().getHeight() - 500) / 10;
				int fontdiff = (xdiff + ydiff) / 20;
				Font newfont = new Font("Tahoma", Font.PLAIN, 16 + (fontdiff));
				
				lblAdmins.setFont(newfont);
				lblConsole.setFont(newfont);
				lblOnlineUsers.setFont(newfont);
				lblPort.setFont(newfont);
				
				btnAddAdmin.setFont(newfont);
				btnRemoveAdmin.setFont(newfont);
				btnStart.setFont(newfont);
				btnStop.setFont(newfont);
				
				txtAdminName.setFont(newfont);
				txtPort.setFont(newfont);
			}
			public void componentMoved(ComponentEvent e) {}
			public void componentShown(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {}
		});
	}
	
	public static void main(String[] args)
	{
		//Sets the theme of the UI to the current OS theme.
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch (Exception e1) {}
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					Server frame = new Server();
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
