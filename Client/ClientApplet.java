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
	
	public JPanel panel;


	ClientApplet () { 
		panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(null);

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

		sendMessage.setBounds(10, 600, 150,30);
		//ActionListener for sendMessage button
		sendMessage.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent event) {  
				//Call Send Message Function				
				Client.processMessage(tf.getText());
			}
		});
		//Add everything to the panel
		panel.add(tf);
		panel.add(mainConsole);
		panel.add(userBox);
		panel.add(sendMessage);
	}
}
