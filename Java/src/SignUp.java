//Chat System (SignUp)
//Written By Ethan Rowan
//June-October 2017
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

import javax.swing.*;
import javax.swing.border.*;
import net.miginfocom.swing.MigLayout;

public class SignUp extends JFrame
{
	String title = "SwiftChat";
	String version = "v1.1";
	
	String username, password;
	boolean isConnected = false;
	
	Socket socket;
	PrintWriter writer;
	BufferedReader reader;
	String ds = "~";
	
	static Point location;
	int width, height;
	Dimension dimension;
	
	private JPanel contentPane;
	private JTextField txtUsername;
	private JTextField txtPassword;
	private JButton btnLogin;
	private JLabel lblUsername;
	private JLabel lblPassword;
	private JButton btnSignUp;
	private JLabel lblHostIP;
	private JTextField txtIP;
	private JLabel lblHostPort;
	private JTextField txtPort;


	public SignUp(Point location)
	{
		dimension = getToolkit().getScreenSize();
		width = dimension.width;
		height = dimension.height;
		
		this.location = new Point((width / 2) - 400 / 2, (height / 2) - 350 / 2);
		if (location != null) this.location = location;
		
		initUI();
	}
	
	private boolean tryConnect()
	{
		try
		{
			socket = new Socket(txtIP.getText(), Integer.parseInt(txtPort.getText()));
			writer = new PrintWriter(socket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			isConnected = true;
			return true;
		} 
		catch (Exception e)
		{
			displayConnectionError();
		}
		return false;
	}
	
	private void displayConnectionError()
	{
		btnLogin.setEnabled(false);
		btnSignUp.setEnabled(false);
		txtUsername.setText("Connection Error.");
		txtPassword.setText("Connection Error.");
		txtUsername.setEnabled(false);
		txtPassword.setEnabled(false);
		txtIP.setEnabled(false);
		txtPort.setEnabled(false);
	}
	
	private void connectionHandler()
	{
		writer.println("request" + ds + "signup" + ds + txtUsername.getText() + ds + txtPassword.getText());
		writer.flush();
		
		try
		{
			while (true)
			{
				String[] data = reader.readLine().split(ds);
				
				if (data[2].equals("Account Created"))
				{
					username = txtUsername.getText();
					password = txtPassword.getText();
					String ip = txtIP.getText();
					int port = Integer.parseInt(txtPort.getText());
					System.out.println("Account Created");
					SignUp.this.setVisible(false);
					Client client = new Client(username, password, ip, 
							port, new Point(SignUp.this.getX(), SignUp.this.getY()));
					client.setVisible(true);
					break;
				}
				else
				{
					txtUsername.setText(data[2] + ".");
					txtPassword.setText(data[2] + ".");
					break;
				}
			}
		}
		catch(Exception e1)
		{
			displayConnectionError();
		}
	}
	
	private void initUI()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(location.x, location.y, 400, 350);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		setResizable(false);
		setTitle("Sign Up");
		contentPane.setLayout(new MigLayout("", "[66.00][grow][]", "[][][][][][][][][][][][][]"));
		
		JLabel lblSwiftchatV = new JLabel(title + " " + version);
		lblSwiftchatV.setFont(new Font("Tahoma", Font.BOLD, 18));
		contentPane.add(lblSwiftchatV, "flowx,cell 1 0,alignx center");
		
		btnLogin = new JButton("Login");
		contentPane.add(btnLogin, "cell 2 0,alignx right");
		
		lblUsername = new JLabel("Username");
		contentPane.add(lblUsername, "cell 1 2,alignx center");
		
		txtUsername = new JTextField();
		txtUsername.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtUsername, "cell 1 3,alignx center");
		txtUsername.setColumns(10);
		
		lblPassword = new JLabel("Password");
		contentPane.add(lblPassword, "cell 1 4,alignx center");
		
		txtPassword = new JTextField();
		txtPassword.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtPassword, "cell 1 5,alignx center");
		txtPassword.setColumns(10);
		
		lblHostIP = new JLabel("Host IP");
		contentPane.add(lblHostIP, "cell 1 7,alignx center");
		
		txtIP = new JTextField();
		txtIP.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtIP, "cell 1 8,growx");
		txtIP.setColumns(10);
		
		lblHostPort = new JLabel("Host Port");
		contentPane.add(lblHostPort, "cell 1 9,alignx center");
		
		txtPort = new JTextField();
		txtPort.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtPort, "cell 1 10,alignx center");
		txtPort.setColumns(10);
		
		btnSignUp = new JButton("Sign Up");
		contentPane.add(btnSignUp, "cell 1 11,alignx center");
		
		createListeners();
	}
	
	private void createListeners()
	{
		btnSignUp.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (txtUsername.getText().length() > 0 && txtPassword.getText().length() > 0 &&
						txtIP.getText().length() > 0 && txtPort.getText().length() > 0 && tryConnect())
				{
					connectionHandler();
				}
			}
		});
		
		btnLogin.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Login login = new Login(new Point(SignUp.this.getX(), 
												  SignUp.this.getY()));
				login.setVisible(true);
				SignUp.this.setVisible(false);
			}
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
		txtIP.addMouseListener(new MouseListener()
		{
			public void mousePressed(MouseEvent e)
			{
				txtIP.setText("");
			}
			public void mouseReleased(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
		});
		txtPort.addMouseListener(new MouseListener()
		{
			public void mousePressed(MouseEvent e)
			{
				txtPort.setText("");
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
					SignUp frame = new SignUp(location);
					frame.setVisible(true);
				} 
				catch (Exception e)
					{e.printStackTrace();}
			}
		});
	}
}
