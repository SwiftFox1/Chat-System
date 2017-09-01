//Chat System (Login)
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

import javax.swing.*;
import javax.swing.border.*;
import net.miginfocom.swing.MigLayout;

public class Login extends JFrame
{
	String title = "SwiftChat";
	String version = "v1.06";
	
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
	private JButton btnSignup;
	private JButton btnLogin;
	private JLabel lblHostIP;
	private JTextField txtIP;
	private JLabel lblHostPort;
	private JTextField txtPort;


	public Login(Point location)
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
			setAllTextBoxes("Connection Error.");
		}
		return false;
	}
	
	private void disconnect()
	{
		try
		{
			writer.println("disconnect" + ds + "null");
			writer.flush();
			socket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void setAllTextBoxes(String message)
	{
		txtUsername.setText(message);
		txtPassword.setText(message);
		txtIP.setText(message);
		txtPort.setText(message);
	}
	
	private void validateRequest()
	{
		writer.println("request" + ds + "login" + ds + txtUsername.getText() + ds + txtPassword.getText());
		writer.flush();
		
		try
		{
			while(true)
			{
				String[] data = reader.readLine().split(ds);
				
				if (data[2].equals("Success"))
				{
					username = txtUsername.getText();
					password = txtPassword.getText();
					String ip = txtIP.getText();
					int port = Integer.parseInt(txtPort.getText());
					disconnect();
					Client client = new Client(username, password, ip, 
							port, new Point(Login.this.getX() - (Login.this.getWidth() / 8), 
									Login.this.getY() - (Login.this.getHeight() / 8)));
					client.setVisible(true);
					Login.this.setVisible(false);
					break;
				}
				else
				{
					setAllTextBoxes(data[2] + ".");
					disconnect();
					break;
				}
			}
		}
		catch(Exception e1)
		{
			setAllTextBoxes("Connection Error.");
			e1.printStackTrace();
		}
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
		contentPane.setLayout(new MigLayout("", "[81.00][grow][]", "[][][][][][][][][][][][]"));
		
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
		
		lblHostIP = new JLabel("Host IP");
		contentPane.add(lblHostIP, "cell 1 6,alignx center");
		
		txtIP = new JTextField();
		txtIP.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtIP, "cell 1 7,growx");
		txtIP.setColumns(10);
		
		lblHostPort = new JLabel("Host Port");
		contentPane.add(lblHostPort, "cell 1 8,alignx center");
		
		txtPort = new JTextField();
		txtPort.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtPort, "cell 1 9,alignx center,aligny top");
		txtPort.setColumns(10);
		
		btnLogin = new JButton("Login");
		contentPane.add(btnLogin, "cell 1 10,alignx center,aligny center");
		
		
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
				if (isConnected) disconnect();
			}
		});
		
		btnLogin.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (txtUsername.getText().length() > 0 && txtPassword.getText().length() > 0 && 
						txtIP.getText().length() > 0 && txtPort.getText().length() > 0 && tryConnect())
				{
					validateRequest();
				}
			}
		});
		
		txtUsername.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e)
			{
				txtUsername.setText("");
			}
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
		});
		txtPassword.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e)
			{
				txtPassword.setText("");
			}
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
		});
		txtIP.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e)
			{
				txtIP.setText("");
			}
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
		});
		txtPort.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e)
			{
				txtPort.setText("");
			}
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
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
