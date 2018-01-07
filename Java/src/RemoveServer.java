//Chat System (RemoveServer)
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class RemoveServer extends JFrame
{
	String title = "Remove Server";
	String version = "v1.3";
	
	String ds = "~";
	String name, ip;
	int port;
	
	String selectedServer;
	ArrayList<String> savedServers;
	
	static Point thislocation;
	
	private JPanel contentPane;
	private JButton btnClose;
	private JScrollPane scrollPane;
	private JList list;
	

	public RemoveServer(Point location)
	{
		setTitle("Settings");
		setResizable(false);
		thislocation = location;
		
		savedServers = new ArrayList<String>();
		checkServers();
		
		initUI();
		this.setLocation(location);
	}
	
	public void checkServers()
	{
		if (savedServers.isEmpty() == false)
			savedServers.clear();
		
		try
		{
			String home = System.getProperty("user.home");
			File file = new File(home + "/Java Program Data/SwiftChat/servers.dat");
			file.getParentFile().mkdirs();
			if (!file.exists())
				file.createNewFile();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String l = "";
			
			while ((l = reader.readLine()) != null)
				savedServers.add(l);
			
			reader.close();
		}
		catch (IOException e)
		{
			createError("Failed to retrieve servers.");
			e.printStackTrace();
		}
	}
	
	public void removeServer()
	{
		if (selectedServer != null && selectedServer != "")
		{
			try
			{
				String home = System.getProperty("user.home");
				File file = new File(home + "/Java Program Data/SwiftChat/servers.dat");
				file.getParentFile().mkdirs();
				if (!file.exists())
					file.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				
				//Rewrite the file without the specified server.
				for (String server : savedServers)
					if (server != selectedServer)
						writer.append(server + "\n");
				writer.close();
				
				selectedServer = null;
				JOptionPane.showMessageDialog(this, new JLabel("Removed server!", 
						JLabel.CENTER), "Success", JOptionPane.PLAIN_MESSAGE);
				
				//Reset the list data.
				checkServers();
				String[] servers = savedServers.toArray(new String[savedServers.size()]);
				list.setListData(servers);
			}
			catch (IOException e)
			{
				createError("Failed to remove server.");
			}
		}
		else
			createError("No server selected.");
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
		contentPane.setLayout(new MigLayout("", "[93.00][185.00,grow][93.00]", "[-18.00][60.00,grow,bottom][62.00]"));
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		JLabel lblRemoveServer = new JLabel(title);
		lblRemoveServer.setFont(new Font("Tahoma", Font.BOLD, 18));
		contentPane.add(lblRemoveServer, "cell 1 0,alignx center");
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				removeServer();
			}
		});
		
		scrollPane = new JScrollPane();
		contentPane.add(scrollPane, "cell 0 1 3 1,grow");
		
		String[] servers = savedServers.toArray(new String[savedServers.size()]);
		list = new JList(servers);
		scrollPane.setViewportView(list);
		contentPane.add(btnRemove, "flowy,cell 1 2,alignx center");
		list.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				try
				{
					if (!e.getValueIsAdjusting())
						selectedServer = savedServers.get(list.getSelectedIndex());
					System.out.println(list.getSelectedIndex());
				}
				catch (ArrayIndexOutOfBoundsException e1)
				{
					//This exception is basically unavoidable,
					//but doesn't cause any damage.
				}
			}
		});
		
		btnClose = new JButton("Close");
		contentPane.add(btnClose, "cell 1 2,alignx center");
		btnClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				RemoveServer.this.setVisible(false);
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
					RemoveServer frame = new RemoveServer(thislocation);
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
