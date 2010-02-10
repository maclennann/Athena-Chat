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
	public JComboBox userBox = new JComboBox(otherUsers);
	public JButton sendMessage = new JButton("Send Message");
	public JMenuBar menuBar = new JMenuBar();
	public JMenu file, edit, encryption;
	public JMenuItem connect, disconnect, exit;
	public JPanel panel;
	public JFrame frame;

	ClientApplet () { 
		
		//Initialize chat window
		frame = new JFrame("Athena Chat Application");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 700);
		frame.setResizable(true);
	    
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
		mainConsole.setSize(750, 500);
		mainConsole.setEditable(false);
	
		//Create text response area and listener function
		tf.setSize(700, 50);
		userBox.setSize(700, 30);
		
		tf.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent event) {
				Client.processMessage(tf.getText());
			}
		});
		
		//sendMessage.setBounds(0, 750, 50, 20);
		sendMessage.setSize(50, 100);
		//ActionListener for sendMessage button
		sendMessage.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent event) {  
				//Call Send Message Function				
				Client.processMessage(tf.getText());
			}});
		
		//frame.setJMenuBar(menuBar);
		
		//Put a ScrollPane over our textarea. <3 scrolling
		JScrollPane scrollPane = new JScrollPane(mainConsole);
		scrollPane.setBounds(10,60,780,500);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setOpaque(true);
	
		//Generate panel by adding appropriate components
		panel = new JPanel(new BorderLayout());
		panel.add(userBox, BorderLayout.NORTH);
		panel.add(tf, BorderLayout.SOUTH);
		panel.add(sendMessage, BorderLayout.EAST);
		panel.add(scrollPane, BorderLayout.CENTER);
    
		//Initialize window frame
        frame.setJMenuBar(menuBar);
        frame.setContentPane(panel);
        frame.setVisible(true);
        
	}
}
