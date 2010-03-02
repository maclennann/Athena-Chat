import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/****************************************************
 * Athena: Encrypted Messaging Application v.0.0.2
 * By: 	
 * 			Gregory LeBlanc
 * 			Norm Maclennan 
 * 			Stephen Failla
 * 
 * This program allows a user to send encrypted messages over a fully standardized messaging architecture. It uses RSA with (x) bit keys and SHA-256 to 
 * hash the keys on the server side. It also supports fully encrypted emails using a standardized email address. The user can also send "one-off" emails
 * using a randomly generated email address
 * 
 * File: ClientPreferences.java
 * 
 * Creates the preferences window invoked from ClientApplet
 *
 ****************************************************/

//Let's make the preferences window
public class ClientPreferences extends JFrame {
	
	//Define components
	public JFrame preferences;
	public JPanel contentPane = new JPanel();
	public JPanel generalPanel, notificationsPanel, encryptionPanel, formattingPanel, themePanel;
	public JScrollPane prefScrollPane = new JScrollPane();
	public JButton apply = new JButton("Apply");
	public JButton cancel = new JButton("Cancel");
	
	//TODO Create components for each of the preference menu categories
	
	
	
	//Constructor
	ClientPreferences() { 
		//Initialize Preferences Window
		preferences = new JFrame("Preferences");
		preferences.setSize(800,600);
		preferences.setResizable(true);
		contentPane.setLayout(null);
		
		//Initialize the JPanels for each of the options
		generalPanel = new JPanel();
		generalPanel.setBounds(185,15,300,445);
		generalPanel.setVisible(false);
		
		notificationsPanel = new JPanel();
		notificationsPanel.setBounds(185,15,300,445);
		notificationsPanel.setVisible(false);
		
		encryptionPanel = new JPanel();
		encryptionPanel.setBounds(175,15,600, 545);
		encryptionPanel.setVisible(false);
		
		formattingPanel = new JPanel();
		formattingPanel.setBounds(175,15,600, 545);
		formattingPanel.setVisible(false);
		
		themePanel = new JPanel();
		themePanel.setBounds(175,15,600, 545);
		themePanel.setVisible(false);
		
		//Size the components
		prefScrollPane.setBounds(15, 15, 150, 545);
		apply.setBounds(700,525,75,30);
		cancel.setBounds(615,525,75,30);
		
		//Add the options to the Scroll Pane
		Image generalPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/generalPref.png");
		Image notificationPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/notificationsPref.png");
		Image encryptionPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/encryptionPref.png");
		Image formattingPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/fontPref.png");
		Image themePreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/themePref.png");
		
		DrawingPanel generalDrawingPanel = new DrawingPanel(generalPreferencesImage);		
		DrawingPanel notificationDrawingPanel = new DrawingPanel(notificationPreferencesImage);
		DrawingPanel encryptionDrawingPanel = new DrawingPanel(encryptionPreferencesImage);
		DrawingPanel formattingDrawingPanel = new DrawingPanel(formattingPreferencesImage);
		DrawingPanel themeDrawingPanel = new DrawingPanel(themePreferencesImage);
				
		generalDrawingPanel.setBounds(30,15,75,75);
		notificationDrawingPanel.setBounds(30,100,75,75);
		encryptionDrawingPanel.setBounds(30,185,75,75);
		formattingDrawingPanel.setBounds(30,270,75,75);
		themeDrawingPanel.setBounds(30,355,75,75);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		//Mouse Listener for the options
		//MouseListener for the generalPreferences
	    	MouseListener mouseListenerGeneral = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				generalPanel.setVisible(true);
				notificationsPanel.setVisible(false);
				System.out.println("OMFG WORK");
		}};
		
		//MouseListener for the notificationsPreferences
			MouseListener mouseListenerNotifications = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				notificationsPanel.setVisible(true);
				generalPanel.setVisible(false);
				System.out.println("OMFG WORKDIE");
		}};
		
		//MouseListener for the encryptionPreferences
			MouseListener mouseListenerEncryption = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {		
		}};
		
		//MouseListener for the formattingPreferences
			MouseListener mouseListenerFormatting = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {		
		}};
		
		//MouseListener for the themePreferences
			MouseListener mouseListenerTheme = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {			
		}};
		
		//Add the mouselisteners to the Drawing Panels
		generalDrawingPanel.addMouseListener(mouseListenerGeneral);
		notificationDrawingPanel.addMouseListener(mouseListenerNotifications);
		encryptionDrawingPanel.addMouseListener(mouseListenerEncryption);
		formattingDrawingPanel.addMouseListener(mouseListenerFormatting);
		themeDrawingPanel.addMouseListener(mouseListenerTheme);
		
		
		//Add the Drawing Panels to the ScrollPane
		prefScrollPane.add(generalDrawingPanel);
		prefScrollPane.add(notificationDrawingPanel);
		prefScrollPane.add(encryptionDrawingPanel);
		prefScrollPane.add(formattingDrawingPanel);
		prefScrollPane.add(themeDrawingPanel);
				
		//Add the JPanels to the ContentPane (set to default until the Drawing Panel is clicked)
		contentPane.add(notificationsPanel);
		contentPane.add(generalPanel);
		contentPane.add(encryptionPanel);
		contentPane.add(formattingPanel);
		contentPane.add(themePanel);
		contentPane.add(prefScrollPane);
		contentPane.add(apply);
		contentPane.add(cancel);

		
		//Initialize Frame
		preferences.setContentPane(contentPane);
		preferences.setVisible(true);
		
	}		
	
}
