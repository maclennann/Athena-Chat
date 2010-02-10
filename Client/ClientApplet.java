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
	public TextArea mainConsole = new TextArea();
	public JComboBox userBox = new JComboBox(otherUsers);
	public JButton sendMessage = new JButton();
	public JMenuBar menuBar;
	public JMenu file, edit, encryption;
	public JMenuItem connect, disconnect, exit;
	//public JPanel panel;
	public JFrame frame;

	ClientApplet () { 
		frame = new JFrame("MenuLookDemo");
		
        //Display the window.
		frame.setSize(800,670);
        frame.setVisible(true);
        
        //Create the menu bar.
	    menuBar = new JMenuBar();
	    
	  //Build the first menu.
        file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_A);
        file.getAccessibleContext().setAccessibleDescription(
                "The only menu in this program that has menu items");
        menuBar.add(file);
        
        connect = new JCheckBoxMenuItem("Connect");
        connect.setMnemonic(KeyEvent.VK_H);
        file.add(connect);
        
        disconnect = new JCheckBoxMenuItem("Disconnect");
        disconnect.setMnemonic(KeyEvent.VK_H);
        file.add(disconnect);     
        
        exit = new JCheckBoxMenuItem("Disconnect");
        exit.setMnemonic(KeyEvent.VK_H);
        file.add(exit);
        
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
        
	    
		//panel = new JPanel();
		///getContentPane().add(frame);
		//panel.setLayout(null);

		setDefaultCloseOperation(javax.swing.
				WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Athena");
		setSize(800, 670);
		JLabel labelOne = new JLabel("Client Application for Encrypted Messaging Using Rijndael Algorithm\n(CAEMURA) v0.9 June 22, 2009\n\n");
		
		labelOne.setBounds(10,1,800,100);
		mainConsole.setBounds(10, 60, 780, 500);
		mainConsole.setEditable(false);
		
		tf.setBounds(10, 570, 700, 30);
		userBox.setBounds(10, 10, 700, 30);
		
		//Put a ScrollPane over our textarea. <3 scrolling
		JScrollPane scrollPane = new JScrollPane(mainConsole);
		scrollPane.setBounds(10,60,780,500);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		tf.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent event) {
				Client.processMessage(tf.getText());
			}
		});
		
		sendMessage.setBounds(10, 600, 150,30);
		//ActionListener for sendMessage button
		sendMessage.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent event) {  
				//Call Send Message Function				
				Client.processMessage(tf.getText());
			}
		});
		
		//Add everything to the panel
		frame.add(tf);
		frame.add(mainConsole);
		frame.add(userBox);
		frame.add(sendMessage);
		//frame.add(frame);
		frame.setJMenuBar(menuBar);
	}
}
