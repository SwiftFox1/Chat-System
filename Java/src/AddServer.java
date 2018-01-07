//Chat System (AddServer)
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

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;

public class AddServer extends JFrame
{
	String title = "Add Server";
	String version = "v1.3";
	
	String ds = "~";
	String name, ip;
	int port;
	
	static Point thislocation;
	
	private JPanel contentPane;
	private JTextField txtIP;
	private JTextField txtPort;
	private JLabel lblIP;
	private JLabel lblPort;
	private JButton btnClose;
	private JLabel lblName;
	private JTextField txtName;
	

	public AddServer(Point location)
	{
		setTitle("Settings");
		setResizable(false);
		thislocation = location;
		initUI();
		this.setLocation(location);
	}
	
	//Formats the server info data and appends it to the servers.dat file
	public void addServer()
	{
		try
		{
			name = txtName.getText();
			ip = txtIP.getText();
			port = Integer.parseInt(txtPort.getText());
		}
		catch (NumberFormatException e)
		{
			createError("Port must be a number greater than zero.");
			return;
		}
		
		if (name != "" && ip != "" && port != 0)
		{
			String data = name + " (" + ip + ":" + port + ")\n";
			try
			{
				String home = System.getProperty("user.home");
				File file = new File(home + "/Java Program Data/SwiftChat/servers.dat");
				file.getParentFile().mkdirs();
				if (!file.exists())
					file.createNewFile();
				Files.write(Paths.get(file.getAbsolutePath()), data.getBytes(), StandardOpenOption.APPEND);
				
				JOptionPane.showMessageDialog(this, new JLabel("Server Added!", 
						JLabel.CENTER), "Success", JOptionPane.PLAIN_MESSAGE);
				return;
			}
			catch (IOException e)
			{
				createError("Unexpected file error. Try again.");
				return;
			}
		}
		createError("All fields must be filled.");
		return;
	}
	
	public void createError(String message)
	{
		JOptionPane.showMessageDialog(this, new JLabel(message, 
				JLabel.CENTER), "Error", JOptionPane.PLAIN_MESSAGE);
	}
	
	private void initUI()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 300, 264);
		setUndecorated(true);
		contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[93.00][185.00,grow][93.00]", "[-18.00][60.00,bottom][42.00][][43.00][62.00]"));
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		JLabel lblAddServer = new JLabel(title);
		lblAddServer.setFont(new Font("Tahoma", Font.BOLD, 18));
		contentPane.add(lblAddServer, "cell 1 0,alignx center");
		
		lblName = new JLabel("Server Name");
		contentPane.add(lblName, "flowy,cell 1 1,alignx center");
		
		txtName = new JTextField();
		txtName.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtName, "cell 1 1,growx");
		txtName.setColumns(10);
		
		lblIP = new JLabel("IP Address");
		contentPane.add(lblIP, "cell 1 1,alignx center");
		
		txtIP = new JTextField();
		txtIP.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtIP, "cell 1 2,growx");
		txtIP.setColumns(10);
		
		lblPort = new JLabel("Port");
		contentPane.add(lblPort, "cell 1 3,alignx center");
		
		txtPort = new JTextField();
		txtPort.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtPort, "cell 1 4,growx");
		txtPort.setColumns(10);
		
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addServer();
			}
		});
		contentPane.add(btnAdd, "flowy,cell 1 5,alignx center");
		
		btnClose = new JButton("Close");
		contentPane.add(btnClose, "cell 1 5,alignx center");
		btnClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				AddServer.this.setVisible(false);
			}
		});
		
		txtIP.addMouseListener(new MouseListener()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				txtIP.setText("");
			}
			public void mouseClicked(MouseEvent e){}
			public void mouseReleased(MouseEvent e){}
			public void mouseEntered(MouseEvent e){}
			public void mouseExited(MouseEvent e){}
		});
		txtPort.addMouseListener(new MouseListener()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				txtPort.setText("");
			}
			public void mouseClicked(MouseEvent e){}
			public void mouseReleased(MouseEvent e){}
			public void mouseEntered(MouseEvent e){}
			public void mouseExited(MouseEvent e){}
		});
		txtName.addMouseListener(new MouseListener()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				txtName.setText("");
			}
			public void mouseClicked(MouseEvent e){}
			public void mouseReleased(MouseEvent e){}
			public void mouseEntered(MouseEvent e){}
			public void mouseExited(MouseEvent e){}
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
					AddServer frame = new AddServer(thislocation);
					frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
