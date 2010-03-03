import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

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
	public Border blackline;
	public TitledBorder generalTitledBorder, notificationsTitledBorder, encryptionTitledBorder, formattingTitledBorder, themeTitleBorder;

	
	//TODO Create components for each of the preference menu categories
	//Define components for the General Menu Panel
	public JCheckBox systemTrayCheckBox = new JCheckBox("Show Athena in system tray");
	public JCheckBox allowESCCheckBox = new JCheckBox("Allow ESC to close a tab");
	public JCheckBox enableSpellCheckCheckBox = new JCheckBox("Enable spell check");
	
	//Define components for the Notifications Menu Panel
	public JCheckBox enableNotificationsCheckBox = new JCheckBox("Enable Notifications");
	public JCheckBox enableSoundsCheckBox = new JCheckBox("Enable sounds");

	//Define components for the Encryption Menu Panel
	public JLabel generateNewKeyPairJLabel = new JLabel("Generate new encryption key pair");
	public JButton generateNewKeyPairJButton = new JButton("Generate!");
	
	//Define components for the Formatting Menu Panel
	public JComboBox selectFontComboBox = new JComboBox();
	public JButton toggleBoldJButton = new JButton("Bold");
	public JButton toggleItalicsJButton = new JButton("Italics");
	public JButton toggleUnderlineJButton = new JButton("Underlined");
	
	//Define components for the Theme Menu Panel
	public JComboBox selectThemeComboBox = new JComboBox();
	public JLabel selectThemeJLabel = new JLabel("Select theme");
	public JButton installNewThemeJButton = new JButton("Install!");
	public JLabel installNewThemeJLabel = new JLabel("Install new theme");
	
	//Constructor
	ClientPreferences() { 
		//Initialize Preferences Window
		preferences = new JFrame("Preferences");
		preferences.setSize(800,600);
		preferences.setResizable(true);
		contentPane.setLayout(null);
		
		//Initalize borders
		blackline = BorderFactory.createLineBorder(Color.black);
		generalTitledBorder = BorderFactory.createTitledBorder(
			       blackline, "General Options");
		encryptionTitledBorder = BorderFactory.createTitledBorder(
			       blackline, "Encryption Options");
		formattingTitledBorder = BorderFactory.createTitledBorder(
			       blackline, "Formatting Options");
		notificationsTitledBorder = BorderFactory.createTitledBorder(
			       blackline, "Notification Options");
		themeTitleBorder = BorderFactory.createTitledBorder(
			       blackline, "Theme Options");
		
		//Size the default components
		prefScrollPane.setBounds(15, 15, 150, 500);
		apply.setBounds(700,525,75,30);
		cancel.setBounds(615,525,75,30);
		
		//Initialize the JPanels for each of the options
		//General Menu Section
		/*************************************************/			
		generalPanel = new JPanel();
		generalPanel.setLayout(null);
		generalPanel.setBorder(generalTitledBorder);
		generalPanel.setBounds(185,15,500,500);
		generalPanel.setVisible(false);		
		
		systemTrayCheckBox.setBounds(50,15,200,50);
		allowESCCheckBox.setBounds(50,55,200,50);
		enableSpellCheckCheckBox.setBounds(50,95,200,50);
		
		generalPanel.add(systemTrayCheckBox);
		generalPanel.add(allowESCCheckBox);
		generalPanel.add(enableSpellCheckCheckBox);
		/*************************************************/
		
		//Notification Menu Section
		/*************************************************/		
		notificationsPanel = new JPanel();
		notificationsPanel.setLayout(null);
		notificationsPanel.setBorder(notificationsTitledBorder);
		notificationsPanel.setBounds(185,15,500,500);
		notificationsPanel.setVisible(false);
		
		enableNotificationsCheckBox.setBounds(50,15,200,50);
		enableSoundsCheckBox.setBounds(50,55,200,50);
		
		notificationsPanel.add(enableNotificationsCheckBox);
		notificationsPanel.add(enableSoundsCheckBox);
		/*************************************************/		
		
		//Encrpytion Menu Selection
		/*************************************************/	
		encryptionPanel = new JPanel();
		encryptionPanel.setLayout(null);
		encryptionPanel.setBorder(encryptionTitledBorder);
		encryptionPanel.setBounds(185,15,500,500);
		encryptionPanel.setVisible(false);
		
		generateNewKeyPairJLabel.setBounds(50,15,200,50);
		generateNewKeyPairJButton.setBounds(50,65,100,50);

		encryptionPanel.add(generateNewKeyPairJButton);
		encryptionPanel.add(generateNewKeyPairJLabel);
		
		//Formatting Menu Selection
		/*************************************************/			
		formattingPanel = new JPanel();
		formattingPanel.setLayout(null);
		formattingPanel.setBorder(formattingTitledBorder);
		formattingPanel.setBounds(185,15,500,500);
		formattingPanel.setVisible(false);
		
		selectFontComboBox.setBounds(50,35, 200, 50);
		toggleBoldJButton.setBounds(50,95,100,25);
		toggleItalicsJButton.setBounds(50,135,100,25);
		toggleUnderlineJButton.setBounds(50,175,100,25);
		
		formattingPanel.add(selectFontComboBox);
		formattingPanel.add(toggleBoldJButton);
		formattingPanel.add(toggleItalicsJButton);
		formattingPanel.add(toggleUnderlineJButton);
		/*************************************************/	
		
		//Theme Menu Selection
		/*************************************************/	
		themePanel = new JPanel();
		themePanel.setLayout(null);
		themePanel.setBorder(themeTitleBorder);
		themePanel.setBounds(185,15,500,500);
		themePanel.setVisible(false);
		
		//Define components for the Theme Menu Panel
		selectThemeComboBox.setBounds(50,65,200,50);
		selectThemeJLabel.setBounds(50,15,100,50);
		installNewThemeJButton.setBounds(50,165,100,50);
		installNewThemeJLabel.setBounds(50,125,100,50);
		
		themePanel.add(selectThemeComboBox);
		themePanel.add(selectThemeJLabel);
		themePanel.add(installNewThemeJButton);
		themePanel.add(installNewThemeJLabel);
		/*************************************************/	
		
		
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
				generalPanel.setVisible(true); // set to true
				notificationsPanel.setVisible(false);
				encryptionPanel.setVisible(false);
				formattingPanel.setVisible(false);
				themePanel.setVisible(false);
		}};
		
		//MouseListener for the notificationsPreferences
			MouseListener mouseListenerNotifications = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				generalPanel.setVisible(false);
				notificationsPanel.setVisible(true); // set to true
				encryptionPanel.setVisible(false);
				formattingPanel.setVisible(false);
				themePanel.setVisible(false);
		}};
		
		//MouseListener for the encryptionPreferences
			MouseListener mouseListenerEncryption = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {	
				generalPanel.setVisible(false);
				notificationsPanel.setVisible(false);
				encryptionPanel.setVisible(true); // set to true
				formattingPanel.setVisible(false);
				themePanel.setVisible(false);
		}};
		
		//MouseListener for the formattingPreferences
			MouseListener mouseListenerFormatting = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {	
				generalPanel.setVisible(false);
				notificationsPanel.setVisible(false);
				encryptionPanel.setVisible(false);
				formattingPanel.setVisible(true); // set to true
				themePanel.setVisible(false);
		}};
		
		//MouseListener for the themePreferences
			MouseListener mouseListenerTheme = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				generalPanel.setVisible(false);
				notificationsPanel.setVisible(false);
				encryptionPanel.setVisible(false);
				formattingPanel.setVisible(false);
				themePanel.setVisible(true); // set to true
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
