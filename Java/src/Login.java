//Chat System (Login)
//Written By Ethan Rowan
//June-January 2017-2018
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
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import net.miginfocom.swing.MigLayout;

public class Login extends JFrame
{
	String title = "SwiftChat";
	String version = "v1.3";
	
	String username, password;
	boolean isConnected = false;
	
	Socket socket;
	PrintWriter writer;
	BufferedReader reader;
	String ds = "~";
	
	String savedIP;
	int savedPort;
	ArrayList<String> savedServers;
	
	static Point location;
	int width, height;
	Dimension dimension;
	
	private JPanel contentPane;
	private JTextField txtUsername;
	private JTextField txtPassword;
	private JButton btnSignup;
	private JButton btnLogin;
	private JComboBox cmbxServer;
	private JLabel lblServer;


	public Login(Point location)
	{	
		dimension = getToolkit().getScreenSize();
		width = dimension.width;
		height = dimension.height;
		
		//Positions the login window at the location of the previous window, if possible.
		this.location = new Point((width / 2) - 400 / 2, (height / 2) - 350 / 2);
		if (location != null) this.location = location;
		
		savedServers = new ArrayList<String>();
		checkServers();
		
		initUI();
	}
	
	private boolean tryConnect()
	{
		try
		{
			/*
			 * Establishes a connection to the server as a client, but not as a user.
			 * This prevents login connections from appearing as users.
			 * Note that there are other failsafes built into the server to
			 * catch this issue incase this code is modified.
			 */
			socket = new Socket(savedIP, savedPort);
			writer = new PrintWriter(socket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			isConnected = true;
			return true;
		} 
		catch (Exception e)
		{
			createError("Connection error: Unable to connect to the server. " +
							"Check server details.");
		}
		return false;
	}
	
	private void disconnect()
	{
		try
		{
			//Attempts to send a disconnect message to the server to safely disconnect.
			//There is a failsafe built into the server incase this doesn't work.
			writer.println("disconnect" + ds + "null");
			writer.flush();
			socket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void createError(String message)
	{
		JOptionPane.showMessageDialog(this, new JLabel(message, 
				JLabel.CENTER), "Error", JOptionPane.PLAIN_MESSAGE);
	}
	
	//Checks the servers.dat file for server info data,
	//then stores it in the savedServers arraylist.
	public void checkServers()
	{
		savedServers.clear();
		savedServers.add("Select A Server...");
		
		try
		{
			File file = new File("/Java Program Data/SwiftChat/servers.dat");
			file.getParentFile().mkdirs();
			if (!file.exists())
				file.createNewFile();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String l = "";
			
			while ((l = reader.readLine()) != null)
				savedServers.add(l);
			reader.close();
			
			updateComboBox();
		}
		catch (IOException e)
		{
			createError("Failed to retrieve servers.");
			e.printStackTrace();
		}
	}
	
	public void updateComboBox()
	{
		if (cmbxServer != null)
		{
			//Add items to the combobox
			cmbxServer.removeItem("Add New Server...");
			cmbxServer.removeItem("Remove Server...");
			
			for (int i = 0; i < savedServers.size(); i++)
				if (!modelContains(savedServers.get(i)))
					cmbxServer.addItem(savedServers.get(i));
			
			
			savedServers.add("Add New Server...");
			savedServers.add("Remove Server...");
			cmbxServer.addItem("Add New Server...");
			cmbxServer.addItem("Remove Server...");
					
			
			//Remove items from the combobox
			ArrayList<String> removal = new ArrayList<String>();
			if (cmbxServer != null)
				for (int i = 0; i < cmbxServer.getItemCount(); i++)
					if (!savedServers.contains(cmbxServer.getItemAt(i)))
						removal.add((String)cmbxServer.getItemAt(i));
			
			for (int i = 0; i < removal.size(); i++)
			{
				String item = (String)removal.get(i);
				if (modelContains(item))
					cmbxServer.removeItem(item);
			}
		}
	}
	
	private void validateRequest()
	{
		/*
		 * Sends a login request to the server which then validates the credentials.
		 * This request returns "success" when the credentials match the account on file.
		 * This request can return multiple types of errors.
		 */
		writer.println("request" + ds + "login" + ds + txtUsername.getText() + ds + txtPassword.getText());
		writer.flush();
		
		try
		{
			while(true)
			{
				String[] data = reader.readLine().split(ds);
				
				//Logs the user in if the server returns a success message
				if (data[2].equals("Success"))
				{
					username = txtUsername.getText();
					password = txtPassword.getText();
					
					disconnect();
					Client client = new Client(username, password, savedIP, 
							savedPort, new Point(Login.this.getX() - (Login.this.getWidth() / 8), 
									Login.this.getY() - (Login.this.getHeight() / 8)));
					client.setVisible(true);
					Login.this.setVisible(false);
					break;
				}
				//Disconnects the login client if any error is returned
				else
				{
					createError(data[2]);
					disconnect();
					break;
				}
			}
		}
		catch(Exception e1)
		{
			createError("Connection error: Unable to connect to the server. " +
					"Check server details.");
			e1.printStackTrace();
		}
	}
	
	public boolean modelContains(String string)
	{
		for (int i = 0; i < cmbxServer.getItemCount(); i++)
			if (cmbxServer.getItemAt(i).equals(string))
				return true;
		return false;
	}
	
	private void initUI()
	{
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(location.x, location.y, 400, 350);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		setTitle("Login");
		contentPane.setLayout(new MigLayout("", "[81.00][grow][]", "[][][][][][][][][][][]"));
		
		JLabel lblTitle = new JLabel(title + " " + version);
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 18));
		contentPane.add(lblTitle, "cell 1 0,alignx center");
		
		btnSignup = new JButton("Sign Up");
		contentPane.add(btnSignup, "cell 2 0");
		
		JLabel lblUsername = new JLabel("Username");
		contentPane.add(lblUsername, "cell 1 2,alignx center,aligny center");
		
		txtUsername = new JTextField();
		txtUsername.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtUsername, "cell 1 3,alignx center,aligny center");
		txtUsername.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password");
		contentPane.add(lblPassword, "cell 1 4,alignx center,aligny center");
		
		txtPassword = new JTextField();
		txtPassword.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtPassword, "cell 1 5,alignx center,aligny center");
		txtPassword.setColumns(10);
		
		lblServer = new JLabel("Server");
		contentPane.add(lblServer, "cell 1 6,alignx center");
		
		String[] servers = savedServers.toArray(new String[savedServers.size()]);
		cmbxServer = new JComboBox(servers);
		contentPane.add(cmbxServer, "cell 1 7,growx");
		
		btnLogin = new JButton("Login");
		contentPane.add(btnLogin, "cell 1 9,alignx center,aligny center");
		
		
		createListeners();
	}
	
	private void createListeners()
	{
		btnSignup.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SignUp signup = new SignUp(new Point(Login.this.getX(), 
													 Login.this.getY()));
				signup.setVisible(true);
				Login.this.setVisible(false);
				if (isConnected)
					disconnect();
			}
		});
		
		btnLogin.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//All four fields are filled out
				if (txtUsername.getText().length() > 0 && txtPassword.getText().length() > 0 && 
						(savedIP != "" || savedIP == null) && savedPort != 0 && tryConnect())
				{
					validateRequest();
				}
				//IP and port fields are not filled out.
				else if (((savedIP == "" || savedIP == null) && savedPort == 0) &&
						(txtUsername.getText().length() > 0 && txtPassword.getText().length() > 0))
					createError("Select a server before logging in!");
				//Username and password fields are not filled out.
				else if (txtUsername.getText().length() == 0 && txtPassword.getText().length() == 0)
					createError("Fill out all fields before logging in.");
			}
		});
		
		cmbxServer.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String item = (String)cmbxServer.getSelectedItem();
				cmbxServer.setSelectedItem(item);
				
				if (item == "Add New Server...")
				{
					new AddServer(new Point(Login.this.getX() + ((getWidth() - 300) / 2), 
							Login.this.getY() + ((getHeight() - 264) / 2))).setVisible(true);
					cmbxServer.setSelectedItem("Select A Server...");
				}
				else if (item == "Remove Server...")
				{
					new RemoveServer(new Point(Login.this.getX() + ((getWidth() - 300) / 2), 
							Login.this.getY() + ((getHeight() - 264) / 2))).setVisible(true);
					cmbxServer.setSelectedItem("Select A Server...");
				}
				else if (item != "Select A Server...")
				{
					savedIP = item.substring(item.indexOf('(') + 1, item.indexOf(':'));
					savedPort = Integer.parseInt(item.substring(item.indexOf(':') + 1, item.indexOf(')')));
				}
				else
				{
					savedIP = "";
					savedPort = 0;
				}
				checkServers();
			}
		});
		cmbxServer.addPopupMenuListener(new PopupMenuListener()
		{
			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				checkServers();
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
			public void popupMenuCanceled(PopupMenuEvent e) {}
		});
		
		txtUsername.addMouseListener(new MouseListener()
		{
			public void mousePressed(MouseEvent e)
			{
				txtUsername.setText("");
			}
			public void mouseReleased(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
		});
		txtPassword.addMouseListener(new MouseListener()
		{
			public void mousePressed(MouseEvent e)
			{
				txtPassword.setText("");
			}
			public void mouseReleased(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
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
					Login frame = new Login(location);
					frame.setVisible(true);
				}
				catch (Exception e)
					{e.printStackTrace();}
			}
		});
	}
}
