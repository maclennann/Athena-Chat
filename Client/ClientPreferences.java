import java.awt.AWTException;
import java.awt.Color;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.TrayIcon;
import javax.swing.ImageIcon;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.io.*;

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
public class ClientPreferences extends JPanel {
	
	//Load preference variables from file into array
	Object[] settingsArray = Client.clientResource.getCurrentSettingsArray();
	boolean allowSystemTray = Boolean.parseBoolean(settingsArray[0].toString());
	boolean allowESCTab = Boolean.parseBoolean(settingsArray[1].toString());
	boolean enableSpellCheck = Boolean.parseBoolean(settingsArray[2].toString());
	
	//Define components
	public JFrame preferences;
	public JPanel contentPane = new JPanel();
	public JPanel generalPanel, notificationsPanel, encryptionPanel, formattingPanel, themePanel;
	public JPanel prefLabelPane = new JPanel();
	public JButton apply = new JButton("Apply");
	public JButton cancel = new JButton("Close");
	public Border blackline;
	public TitledBorder generalTitledBorder, notificationsTitledBorder, encryptionTitledBorder, formattingTitledBorder, themeTitleBorder;

	//TODO Create components for each of the preference menu categories
	//Define components for the General Menu Panel
	public JLabel generalLabel = new JLabel();
	public JCheckBox systemTrayCheckBox = new JCheckBox("Show Athena in system tray", allowSystemTray);
	public JCheckBox allowESCCheckBox = new JCheckBox("Allow ESC to close a tab", allowESCTab);
	public JCheckBox enableSpellCheckCheckBox = new JCheckBox("Enable spell check", enableSpellCheck);
	public boolean systemTrayVal;
	public boolean allowESCVal;
	public boolean enableSCVal;
	public boolean systemTrayFlag = false;
	public boolean allowESCFlag = false;
	public boolean enableSpellCheckFlag = false;
	
	//Define components for the Notifications Menu Panel
	public JLabel notificationsLabel = new JLabel();
	public JCheckBox enableNotificationsCheckBox = new JCheckBox("Enable Notifications");
	public JCheckBox enableSoundsCheckBox = new JCheckBox("Enable sounds");
	public boolean enableNotificationsFlag = false;
	public boolean enableSoundsFlag = false;

	//Define components for the Encryption Menu Panel
	public JLabel encryptionLabel = new JLabel();
	public JLabel generateNewKeyPairJLabel = new JLabel("Generate new encryption key pair");
	public JButton generateNewKeyPairJButton = new JButton("Generate!");
	
	//Define components for the Formatting Menu Panel
	public JLabel formattingLabel = new JLabel();
	public JComboBox selectFontComboBox = new JComboBox();
	public JButton toggleBoldJButton = new JButton("Bold");
	public JButton toggleItalicsJButton = new JButton("Italics");
	public JButton toggleUnderlineJButton = new JButton("Underlined");
	public boolean selectFontFlag = false;
	public boolean toggleBoldFlag = false;
	public boolean toggleItalicsFlag = false;
	public boolean toggleUnderlineFlag = false;
	
	//Define components for the Theme Menu Panel
	public JLabel themeLabel = new JLabel();
	public JComboBox selectThemeComboBox = new JComboBox();
	public JLabel selectThemeJLabel = new JLabel("Select theme");
	public JButton installNewThemeJButton = new JButton("Install!");
	public JLabel installNewThemeJLabel = new JLabel("Install new theme");
	
	//Initialize array to hold current file settings and accept all new setting changes
	public Object[] settingsToWrite = settingsArray;
	
	//Constructor
	ClientPreferences() {	
		
		//Initialize Preferences Window
		preferences = new JFrame("Preferences");
		preferences.setSize(800,600);
		preferences.setResizable(true);
		contentPane.setLayout(null);
		
		//Initialize borders
		blackline = BorderFactory.createLineBorder(Color.black);
		Border labelBorder = BorderFactory.createRaisedBevelBorder();
		Border extraBorder = BorderFactory.createLoweredBevelBorder();
		Border prefBorder = BorderFactory.createCompoundBorder(labelBorder, extraBorder);
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
		prefLabelPane.setBounds(15, 15, 120, 530);
		prefLabelPane.setBorder(prefBorder);
		apply.setBounds(700,525,75,30);
		cancel.setBounds(615,525,75,30);
		
		// Set apply button default to disabled until changes are made
		apply.setEnabled(false);
		
		//Initialize default button action listeners
		apply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				//Apply all changes
				try {
					setGeneralSettings(systemTrayVal, systemTrayFlag, allowESCFlag, allowESCVal, enableSpellCheckFlag, enableSCVal);
					setNotificationSettings(enableNotificationsFlag, enableSoundsFlag);
					setFormattingSettings(selectFontFlag, toggleBoldFlag, toggleItalicsFlag, toggleUnderlineFlag);
					writeSavedPreferences(settingsToWrite);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				apply.setEnabled(false);
			}
		});
		
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				preferences.dispose();
			}
		});

		//Initialize the JPanels for each of the options
		//General Menu Section
		/*************************************************/		
		ImageIcon generalImageIcon = new ImageIcon("../images/generalPref.png");
		generalLabel.setIcon(generalImageIcon);
		generalLabel.setText("General");
		generalLabel.setVerticalTextPosition(JLabel.BOTTOM);
		generalLabel.setHorizontalTextPosition(JLabel.CENTER);
		generalLabel.setBounds(30,20,85,75);
		generalLabel.setBorder(labelBorder);
		
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
		
		systemTrayCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e){
				apply.setEnabled(true);
				if (e.getStateChange() == ItemEvent.SELECTED)
					systemTrayVal = true;
				else
					systemTrayVal = false;
				settingsToWrite[0] = systemTrayVal;
				systemTrayFlag = true;
			}
		});
		allowESCCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e){
				apply.setEnabled(true);
				if (e.getStateChange() == ItemEvent.SELECTED)
					allowESCVal = true;
				else
					allowESCVal = false;
				settingsToWrite[1] = allowESCVal;
				allowESCFlag = true;
			}
		});
		enableSpellCheckCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e){
				apply.setEnabled(true);
				if (e.getStateChange() == ItemEvent.SELECTED)
					enableSCVal = true;
				else
					enableSCVal = false;
				settingsToWrite[2] = enableSCVal;
				enableSpellCheckFlag = true;
			}
		});
		/*************************************************/
		
		//Notification Menu Section
		/*************************************************/	
		//Image notificationPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/notificationsPref.png");
		//notificationDrawingPanel = new DrawingPanel(notificationPreferencesImage);

		ImageIcon notificationImageIcon = new ImageIcon("../images/notificationsPref.png");
		notificationsLabel.setIcon(notificationImageIcon);
		notificationsLabel.setText("Notifications");
		notificationsLabel.setVerticalTextPosition(JLabel.BOTTOM);
		notificationsLabel.setHorizontalTextPosition(JLabel.CENTER);
		notificationsLabel.setBounds(30,120,85,75);
		notificationsLabel.setBorder(labelBorder);
		
		notificationsPanel = new JPanel();
		notificationsPanel.setLayout(null);
		notificationsPanel.setBorder(notificationsTitledBorder);
		notificationsPanel.setBounds(185,15,500,500);
		notificationsPanel.setVisible(false);
		
		enableNotificationsCheckBox.setBounds(50,15,200,50);
		enableSoundsCheckBox.setBounds(50,55,200,50);
		
		notificationsPanel.add(enableNotificationsCheckBox);
		notificationsPanel.add(enableSoundsCheckBox);
		
		//This settings array update will go in check box action listeners when implemented as seen above in general settings
		//settingsToWrite[3] = "false";
		//settingsToWrite[4] = "false";
		/*************************************************/		
		
		//Encrpytion Menu Selection
		/*************************************************/	
		//Image encryptionPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/encryptionPref.png");
		//encryptionLabel = new DrawingPanel(encryptionPreferencesImage);
		//encryptionLabel.setBounds(30,185,75,75);
		//encryptionLabel.setBorder(blackline);
		
		ImageIcon encryptionImageIcon = new ImageIcon("../images/encryptionPref.png");
		encryptionLabel.setIcon(encryptionImageIcon);
		encryptionLabel.setText("Encryption");
		encryptionLabel.setVerticalTextPosition(JLabel.BOTTOM);
		encryptionLabel.setHorizontalTextPosition(JLabel.CENTER);
		encryptionLabel.setBounds(30,220,85,75);
		encryptionLabel.setBorder(labelBorder);
		
		encryptionPanel = new JPanel();
		encryptionPanel.setLayout(null);
		encryptionPanel.setBorder(encryptionTitledBorder);
		encryptionPanel.setBounds(185,15,500,500);
		encryptionPanel.setVisible(false);
		
		generateNewKeyPairJLabel.setBounds(50,15,200,50);
		generateNewKeyPairJButton.setBounds(50,65,100,50);

		encryptionPanel.add(generateNewKeyPairJButton);
		encryptionPanel.add(generateNewKeyPairJLabel);
		
		//This settings array update will go in check box action listeners when implemented as seen above in general settings
		//settingsToWrite[5] = "0";
		/*************************************************/	
		
		//Formatting Menu Selection
		/*************************************************/	
		//Image formattingPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/fontPref.png");
		//formattingLabel = new DrawingPanel(formattingPreferencesImage);
		//formattingLabel.setBounds(30,270,75,75);
		//formattingLabel.setBorder(blackline);
		
		ImageIcon formattingImageIcon = new ImageIcon("../images/fontPref.png");
		formattingLabel.setIcon(formattingImageIcon);
		formattingLabel.setText("Font Options");
		formattingLabel.setVerticalTextPosition(JLabel.BOTTOM);
		formattingLabel.setHorizontalTextPosition(JLabel.CENTER);
		formattingLabel.setBounds(30,320,85,75);
		formattingLabel.setBorder(labelBorder);
		
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
		
		//This settings array update will go in check box action listeners when implemented as seen above in general settings
		//settingsToWrite[6] = "false";
		//settingsToWrite[7] = "false";
		//settingsToWrite[8] = "false";
		//settingsToWrite[9] = "false";
		/*************************************************/	
		
		//Theme Menu Selection
		/*************************************************/	
		//Image themePreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/themePref.png");
		//themeLabel = new DrawingPanel(themePreferencesImage);
		//themeLabel.setBounds(30,355,75,75);
		//themeLabel.setBorder(blackline);
		
		ImageIcon themeImageIcon = new ImageIcon("../images/themePref.png");
		themeLabel.setIcon(themeImageIcon);
		themeLabel.setText("Appearance");
		themeLabel.setVerticalTextPosition(JLabel.BOTTOM);
		themeLabel.setHorizontalTextPosition(JLabel.CENTER);
		themeLabel.setBounds(30,420,85,75);
		themeLabel.setBorder(labelBorder);
		
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
		
		//This settings array update will go in check box action listeners when implemented as seen above in general settings
		//settingsToWrite[10] = "0";
		/*************************************************/	
	
		
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
		
		
		//Add the mouseListeners to the Labels
		generalLabel.addMouseListener(mouseListenerGeneral);
		notificationsLabel.addMouseListener(mouseListenerNotifications);
		encryptionLabel.addMouseListener(mouseListenerEncryption);
		formattingLabel.addMouseListener(mouseListenerFormatting);
		themeLabel.addMouseListener(mouseListenerTheme);
		
		
		//Add the Drawing Panels to the LabelPane
		prefLabelPane.add(generalLabel);
		prefLabelPane.add(notificationsLabel);
		prefLabelPane.add(encryptionLabel);
		prefLabelPane.add(formattingLabel);
		prefLabelPane.add(themeLabel);
				
		//Add the JPanels to the ContentPane (set to default until Label Image is clicked)
		contentPane.add(notificationsPanel);
		contentPane.add(generalPanel);
		contentPane.add(encryptionPanel);
		contentPane.add(formattingPanel);
		contentPane.add(themePanel);
		contentPane.add(prefLabelPane);
		contentPane.add(apply);
		contentPane.add(cancel);

		
		//Initialize Frame
		preferences.setContentPane(contentPane);
		preferences.setVisible(true);


	}	
	
	private void setGeneralSettings (boolean systemTrayVal, boolean systemTrayFlag, boolean allowESCFlag,
			boolean allowESCVal, boolean enableSpellCheckFlag, boolean enableSCVal) throws AWTException
	{
		if(systemTrayFlag)
		{
			SystemTray tray = SystemTray.getSystemTray();
			TrayIcon[] trayArray = tray.getTrayIcons();
			int tlength = trayArray.length;
			
			if(!(systemTrayVal))
			{
				for(int x = 0; x < tlength; x++)
					tray.remove(trayArray[x]);
			}
			if(systemTrayVal)
			{
				if(tlength == 0)
				{
					Image trayImage = Toolkit.getDefaultToolkit().getImage("../images/sysTray.gif");
					ActionListener exitListener = new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							System.out.println("Exiting...");
							System.exit(0);
						}
					};

					PopupMenu popup = new PopupMenu();
					MenuItem defaultItem = new MenuItem("Exit");
					defaultItem.addActionListener(exitListener);
					popup.add(defaultItem);

					TrayIcon trayIcon = new TrayIcon(trayImage, "Tray Demo", popup);
					trayIcon.setImageAutoSize(true);
					tray.add(trayIcon);
				}
			}
		}
		if (allowESCFlag)
		{
			//Adjust setting
			if(!(allowESCVal))
			{
				Client.clientResource.closeTabWithESC(false);
			}
			if(allowESCVal)
			{
				Client.clientResource.closeTabWithESC(true);
			}
		}
		if (enableSpellCheckFlag)
		{
			//Adjust setting
			if(!(enableSCVal))
			{
				Client.clientResource.setSpellCheck(false);
			}
			if(enableSCVal)
			{
				Client.clientResource.setSpellCheck(true);
			}
		}
	}
	
	private void setNotificationSettings (boolean enableNotificationsFlag, boolean enableSoundsFlag)
	{
		if (enableNotificationsFlag)
		{
			//Adjust setting
		}
		if (enableSoundsFlag)
		{
			//Adjust setting
		}
	}
	
	private void setFormattingSettings(boolean setFontFlag, boolean toggleBoldFlag, boolean toggleItalicsFlag, boolean toggleUnderlineFlag)
	{
		if (setFontFlag)
		{
			//Adjust setting
		}
		if (toggleBoldFlag)
		{
			//Adjust setting
		}
		if (toggleItalicsFlag)
		{
			//Adjust setting
		}
		if (toggleUnderlineFlag)
		{
			//Adjust setting
		}
	}
	
	private void writeSavedPreferences(Object[] settingsToWrite)
	{
		try {
			BufferedWriter outPref = new BufferedWriter(new FileWriter("./users/" + Client.username + "/athena.conf"));
			
			//Write general settings
			outPref.write("[GENERAL]");
			outPref.newLine();
			outPref.write("allowSystemTray=" + settingsToWrite[0]);
			outPref.newLine();
			outPref.write("allowESCTab=" + settingsToWrite[1]);
			outPref.newLine();
			outPref.write("enableSpellCheck=" + settingsToWrite[2]);
			outPref.newLine();
			outPref.newLine();
			outPref.newLine();

			//Write notification settings
			outPref.write("[NOTIFICATIONS]");
			outPref.newLine();
			outPref.write("enableNotifications=" + settingsToWrite[3]);
			outPref.newLine();
			outPref.write("enableNotifications=" + settingsToWrite[4]);
			outPref.newLine();
			outPref.newLine();
			outPref.newLine();
			
			//Write encryption settings
			outPref.write("[ENCRYPTION]");
			outPref.newLine();
			outPref.write(";");
			outPref.newLine();
			outPref.write(";");
			outPref.newLine();
			outPref.write("encryptionType=" + settingsToWrite[5]);
			outPref.newLine();
			outPref.newLine();
			outPref.newLine();
			
			//Write formatting settings
			outPref.write("[FORMATTING]");
			outPref.newLine();
			outPref.write("fontFace=" + settingsToWrite[6]);
			outPref.newLine();
			outPref.write("fontBold=" + settingsToWrite[7]);
			outPref.newLine();
			outPref.write("fontItalic=" + settingsToWrite[8]);
			outPref.newLine();
			outPref.write("fontUnderline=" + settingsToWrite[9]);
			outPref.newLine();
			outPref.newLine();
			outPref.newLine();
			
			//Write theme settings
			outPref.write("[THEME]");
			outPref.newLine();
			outPref.write("activeTheme=" + settingsToWrite[10]);
			
			outPref.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
