//Chat System (Settings)
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
import java.io.PrintWriter;

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

public class Settings extends JFrame
{
	String title = "Account Settings";
	String version = "v1.3";
	
	String ds = "~";
	
	static String thisusername, thispassword, newusername, newpassword;
	static Point thislocation;
	static PrintWriter thiswriter;
	
	private JPanel contentPane;
	private JTextField txtUsername;
	private JTextField txtPassword;
	private JLabel lblUsername;
	private JLabel lblPassword;
	private JButton btnClose;
	

	public Settings(PrintWriter writer, String username, String password, Point location)
	{
		setTitle("Settings");
		setResizable(false);
		thiswriter = writer;
		thisusername = username;
		thispassword = password;
		thislocation = location;
		initUI();
		this.setLocation(location);
	}
	
	public void sendUpdates()
	{
		if (thiswriter != null && newpassword != "" && thispassword != newpassword)
		{
			//FORMAT: update÷type÷username÷originalpassword÷newpassword
			//INDEX:    0      1     2            3              4
			thiswriter.println("update" + ds + "password" + ds + thisusername + ds + 
					thispassword + ds + newpassword);
			thiswriter.flush();
		}
		if (thiswriter != null && newusername != "" && thisusername != newusername)
		{
			//FORMAT: update÷type÷originalusername÷newusername
			//INDEX:    0      1         2              3
			thiswriter.println("update" + ds + "username" + ds + 
					thisusername + ds + newusername);
			thiswriter.flush();
		}
		//If either of the above cases are true, then the success dialog will show.
		if ((newusername.length() > 0 && newusername != thisusername) ||
				(newpassword.length() > 0 && newpassword != thispassword))
		{
			JOptionPane.showMessageDialog(this, new JLabel("Info updated! Reconnect to apply changes.", 
					JLabel.CENTER), "Success", JOptionPane.PLAIN_MESSAGE);
			JOptionPane.showMessageDialog(this, new JLabel("Admin tag may be temporarily removed.", 
					JLabel.CENTER), "Notice", JOptionPane.PLAIN_MESSAGE);
		}
		else
			JOptionPane.showMessageDialog(this, new JLabel("Invalid fields.", 
					JLabel.CENTER), "Error", JOptionPane.PLAIN_MESSAGE);
		System.out.println(newusername.length() + "   " + newpassword.length());
	}
	
	private void initUI()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 400, 264);
		setUndecorated(true);
		contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[93.00][185.00,grow][93.00]", "[-18.00][60.00,bottom][71.00][][64.00][62.00]"));
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		JLabel lblSettings = new JLabel(title);
		lblSettings.setFont(new Font("Tahoma", Font.BOLD, 18));
		contentPane.add(lblSettings, "cell 1 0,alignx center");
		
		lblUsername = new JLabel("Change Username");
		contentPane.add(lblUsername, "cell 1 1,alignx center");
		
		txtUsername = new JTextField();
		txtUsername.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtUsername, "cell 1 2,growx");
		txtUsername.setColumns(10);
		
		lblPassword = new JLabel("Change Password");
		contentPane.add(lblPassword, "cell 1 3,alignx center");
		
		txtPassword = new JTextField();
		txtPassword.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(txtPassword, "cell 1 4,growx");
		txtPassword.setColumns(10);
		
		JButton btnUpdate = new JButton("Update");
		btnUpdate.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				newusername = txtUsername.getText();
				newpassword = txtPassword.getText();
				sendUpdates();
			}
		});
		contentPane.add(btnUpdate, "flowy,cell 1 5,alignx center");
		
		btnClose = new JButton("Close");
		contentPane.add(btnClose, "cell 1 5,alignx center");
		btnClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Settings.this.setVisible(false);
			}
		});
		
		txtUsername.addMouseListener(new MouseListener()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				txtUsername.setText("");
			}
			public void mouseClicked(MouseEvent e){}
			public void mouseReleased(MouseEvent e){}
			public void mouseEntered(MouseEvent e){}
			public void mouseExited(MouseEvent e){}
		});
		txtPassword.addMouseListener(new MouseListener()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				txtPassword.setText("");
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
					Settings frame = new Settings(thiswriter, thisusername, thispassword, thislocation);
					frame.setVisible(true);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
