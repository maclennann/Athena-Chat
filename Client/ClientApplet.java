/****************************************************
 * Athena: Encrypted Messaging Application v.0.0.1
 * By: 	
 * 			Gregory LeBlanc
 * 			Norm Maclennan 
 * 			Stephen Failla
 * 
 * This program allows a user to send encrypted messages over a fully standardized messaging architecture. It uses RSA with (x) bit keys and SHA-256 to 
 * hash the keys on the server side. It also supports fully encrypted emails using a standardized email address. The user can also send "one-off" emails
 * using a randomly generated email address
 * 
 * File: ClientApplet.java
 * 
 * Creates the window for the client and sets connection variables.
 *
 ****************************************************/
import java.applet.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import javax.swing.*;

//Creates the physical applet, calls the client code
public class ClientApplet extends JFrame 
{
	
	//String array of buddies for choosing user to send message to
	public static String[] otherUsers = {"Norm", "Steve", "Greg", "Aegis"};
	
	// Components for the visual display of the chat windows
	public TextField tf = new TextField();
	public JTextArea mainConsole = new JTextArea();
	public JList userBox = new JList(otherUsers);
	//public JButton sendMessage = new JButton("Send Message");
	public JMenuBar menuBar = new JMenuBar();
	public JMenu file, edit, encryption;
	public JMenuItem connect, disconnect, exit;
	public JPanel panel;
	public JFrame imContentFrame, buddyListFrame; //Is that it?

	ClientApplet () { 

		//Initialize chat window
		//This is the main frame for the IMs
		imContentFrame = new JFrame("Athena Chat Application");
		imContentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		imContentFrame.setSize(800, 605);
		imContentFrame.setResizable(true);
		    
		//Build the first menu.
	        file = new JMenu("File");
	        file.setMnemonic(KeyEvent.VK_A);
	        file.getAccessibleContext().setAccessibleDescription(
	                "The only menu in this program that has menu items");
	    
	        //Initialize menu button
	        connect = new JMenuItem("Connect");
	        connect.setMnemonic(KeyEvent.VK_H);
	        file.add(connect);
	        
	        //Initialize menu button
	        disconnect = new JMenuItem("Disconnect");
	        disconnect.setMnemonic(KeyEvent.VK_H);
	        file.add(disconnect);     
	        
	        //Initialize menu button
	        exit = new JMenuItem("Exit");
	        exit.setMnemonic(KeyEvent.VK_H);
        	file.add(exit);
        
	        menuBar.add(file);
	        
		//Build the second menu.
	        edit = new JMenu("Edit");
	        edit.setMnemonic(KeyEvent.VK_A);
        	edit.getAccessibleContext().setAccessibleDescription(
	                "The only menu in this program that has menu items");
	        menuBar.add(edit);
	       
		//Build the third menu.
	        encryption = new JMenu("Encryption");
	        encryption.setMnemonic(KeyEvent.VK_A);
	        encryption.getAccessibleContext().setAccessibleDescription(
	                "The only menu in this program that has menu items");
	        menuBar.add(encryption);
	
		//Create universal text area 
		mainConsole.setEditable(false);
		
		//Create text response area and listener function
		tf.setBounds(10, 515, 580, 30);	
		tf.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent event) {
				Client.processMessage(tf.getText());
			}
		});
		
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				Client.connect();
			}
		});

		disconnect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				Client.disconnect();
			}
		});
			
		exit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				Client.exit();
			}
		});
			
		//frame.setJMenuBar(menuBar);
		JScrollPane buddylist = new JScrollPane(userBox);
		buddylist.setBounds(595,10,195,538);	

		//Put a ScrollPane over our textarea. <3 scrolling
		JScrollPane scrollPane = new JScrollPane(mainConsole);
		scrollPane.setBounds(10,10,580,500);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setOpaque(true);
		userBox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//Generate panel by adding appropriate components
		panel = new JPanel();
		panel.setLayout(null);
		panel.add(buddylist);
		panel.add(tf);
		panel.add(scrollPane);

		//Initialize window frame
		imContentFrame.setJMenuBar(menuBar);
	        imContentFrame.setContentPane(panel);
	        imContentFrame.setVisible(true);
        
	}
}
