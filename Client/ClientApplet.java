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
 * File: ClientApplet.java
 * 
 * Creates the window for the client and sets connection variables.
 *
 ****************************************************/
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import com.inet.jortho.SpellChecker;

//Client swing window.
//TODO: Rename it to something else. It's not an applet
public class ClientApplet extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7742402292330782311L;
	public static final int debug = 0;

	public Hashtable<String, Integer> userStatus = new Hashtable<String, Integer>();;

	// Define the listModel for the JList
	DefaultListModel listModel = new DefaultListModel();

	// Components for the visual display of the chat windows
	public boolean spellCheckFlag = false;
	public JList userBox = new JList(listModel);
	public JMenuBar menuBar = new JMenuBar();
	public JMenu file, edit, encryption;
	public JMenuItem disconnect, exit, preferences;
	public JPanel panel; // still need this?
	public JFrame imContentFrame, buddyListFrame;
	public JTabbedPane imTabbedPane = new JTabbedPane();
	public Hashtable<String, MapTextArea> tabPanels = new Hashtable<String, MapTextArea>();
	public BufferedImage addUserIcon;
	public Border blackline = BorderFactory.createLineBorder(Color.gray);
	public ImageIcon lockIcon = new ImageIcon("../images/lockicon.png");
	static public JLabel lockIconLabel = new JLabel();
	public TitledBorder buddyBorder = BorderFactory.createTitledBorder(blackline, "Contact List");

	// Method to add users to the JList when they sign on
	public void newBuddyListItems(String availableUser) {
		listModel.addElement(availableUser);
	}

	// Method to remove user from the JList who signs off
	public void buddySignOff(String offlineUser) {
		listModel.removeElement(offlineUser);
	}

	public static Object[] currentSettings = new Object[11];
	
	ClientApplet() {

		// Initialize chat window
		
		//Load preference settings
		Object[] settingsArray = loadSavedPreferences();
		setCurrentSettingsArray(settingsArray);
		boolean allowSystemTray = Boolean.parseBoolean(settingsArray[0].toString());
		try {
			setSystemTrayIcon(allowSystemTray);
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		boolean allowESCTab = Boolean.parseBoolean(settingsArray[1].toString());
		closeTabWithESC(allowESCTab);
		boolean enableSpellCheck = Boolean.parseBoolean(settingsArray[2].toString());
		setSpellCheck(enableSpellCheck);
		boolean enableNotifications = Boolean.parseBoolean(settingsArray[3].toString());
		setEnableNotifications(enableNotifications);
		boolean enableSounds = Boolean.parseBoolean(settingsArray[4].toString());
		int encryptionType = Integer.parseInt(settingsArray[5].toString());
		String fontFace = settingsArray[6].toString();
		boolean fontBold = Boolean.parseBoolean(settingsArray[7].toString());
		boolean fontItalic = Boolean.parseBoolean(settingsArray[8].toString());
		boolean fontUnderline = Boolean.parseBoolean(settingsArray[9].toString());
		int activeTheme = Integer.parseInt(settingsArray[10].toString());
		//This is the main frame for the IMs
		imContentFrame = new JFrame("Athena Chat Application - " + Client.username);
		imContentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		imContentFrame.setSize(813, 610);
		imContentFrame.setResizable(false);

		// Create the file menu.
		file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_A);

		// Create button File -> Disconnect
		disconnect = new JMenuItem("Disconnect");
		disconnect.setMnemonic(KeyEvent.VK_H);
		file.add(disconnect);

		// Create button File -> Exit
		exit = new JMenuItem("Exit");
		exit.setMnemonic(KeyEvent.VK_H);
		file.add(exit);

		// Add the file menu to the menu bar
		menuBar.add(file);

		// Create the edit menu.
		edit = new JMenu("Edit");
		edit.setMnemonic(KeyEvent.VK_A);
		menuBar.add(edit);

		// TODO Add items to the edit menu

		// Create button Edit -> Preferences
		preferences = new JMenuItem("Preferences");
		preferences.setMnemonic(KeyEvent.VK_H);
		edit.add(preferences);

		// Create the encryption menu.
		encryption = new JMenu("Encryption");
		encryption.setMnemonic(KeyEvent.VK_A);
		menuBar.add(encryption);

		// TODO Add items to the encryption menu

		// ActionListener to make the disconnect menu item disconnect
		disconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// Clear the Buddy list when disconnected
				listModel.clear();
				Client.disconnect();
				//Get rid of this window and open a new Login Window
				imContentFrame.dispose();
				try {
					new ClientLogin();
				} catch (AWTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		// ActionListener to make the exit menu item exit
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Client.exit();
			}
		});

		// ActionListener to make the exit menu item exit
		preferences.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				new ClientPreferences();
			}
		});

		// Why is this commented out?
		// frame.setJMenuBar(menuBar);

		// Adds the contact list to a scroll pane
		JScrollPane contactList = new JScrollPane(userBox);
		contactList.setBounds(600, 2, 195, 485);
		contactList.setBorder(buddyBorder);
		// Adds the Icons to Pane
		// TODO Add ActionListeners to the images to bring up the add/remove
		// user windows
		ImageIcon addUserIcon = new ImageIcon("../images/addUser.png");
		ImageIcon removeUserIcon = new ImageIcon("../images/removeUser.png");
		JLabel addContactLabel = new JLabel();
		JLabel removeContactLabel = new JLabel();;
		addContactLabel.setIcon(addUserIcon);
		addContactLabel.setText("Add Contact");
		addContactLabel.setVerticalTextPosition(JLabel.BOTTOM);
		addContactLabel.setHorizontalTextPosition(JLabel.CENTER);
		
		removeContactLabel.setIcon(removeUserIcon);
		removeContactLabel.setText("Remove Contact");
		removeContactLabel.setVerticalTextPosition(JLabel.BOTTOM);
		removeContactLabel.setHorizontalTextPosition(JLabel.CENTER);
		
		addContactLabel.setVisible(true);
		removeContactLabel.setVisible(true);
		addContactLabel.setBounds(600, 495, 100, 50);
		removeContactLabel.setBounds(700, 495, 100, 50);
		
		imTabbedPane.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				//JList theList = (JList) mouseEvent.getSource();
				if (imTabbedPane.getTabCount() > 0)
				{
					JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
					Component[] currentTabComponents = currentTab.getComponents();
					Component textFieldToFocus = currentTabComponents[1];
					textFieldToFocus.requestFocusInWindow();
				}
			}		
		});
		
		// MouseListener for the AddUser image
		MouseListener addBuddyMouseListener = new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				String usernameToAdd = JOptionPane.showInputDialog("Input the user name to add to your contact list:");
				try {
					if(usernameToAdd != null){
						Client.buddyList(usernameToAdd);
						Client.instantiateBuddyList(usernameToAdd);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		addContactLabel.addMouseListener(addBuddyMouseListener);

		// MouseListener for the removeUser image
		MouseListener removeBuddyMouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				
				try {				
					JList theList = (JList) userBox;
					String[] usernames = Client.returnBuddyListArray();
					
					// Find out what was double-clicked
					int index = theList.getSelectedIndex();
					if(debug==1)System.out.println(index);
					if (index >= 0) {

						// Get the buddy that was double-clicked
						Object o = theList.getModel().getElementAt(index);						

						ArrayList<String> list = new ArrayList<String>(Arrays
								.asList(usernames));
						list.removeAll(Arrays.asList(o));
						usernames = list.toArray(new String[0]);
						buddySignOff(o.toString());

						// Print the array back to the file (will overwrite the
						// previous file
						Client.writeBuddyListToFile(usernames);
					}
					//If there wasn't something selected, bring up a new window that will let them choose who they want to remove
					else {
						
						final JFrame removeWindow = new JFrame("Remove user");
						final JPanel contentPane = new JPanel();
						final JComboBox listOfUsersJComboBox = new JComboBox();
						final JButton removeJButton, cancelJButton;
						removeJButton = new JButton("Remove");
						cancelJButton = new JButton("Cancel");
						
						contentPane.setLayout(null);
						
						removeWindow.setSize(200,200);	
						listOfUsersJComboBox.setBounds(45,40,100,25);
						removeJButton.setBounds(45,75,100,25);
						cancelJButton.setBounds(45,115,100,25);
					
						for(int x=0; x<usernames.length;x++) listOfUsersJComboBox.addItem(usernames[x]);
						
						contentPane.add(listOfUsersJComboBox);
						contentPane.add(removeJButton);
						contentPane.add(cancelJButton);
						removeWindow.add(contentPane);
						removeWindow.setVisible(true);
						
						removeJButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent event){
								try {
									String[] usernames = Client.returnBuddyListArray();
									
									Object o = listOfUsersJComboBox.getSelectedItem();
									ArrayList<String> list = new ArrayList<String>(Arrays
											.asList(usernames));
									list.removeAll(Arrays.asList(o));
									usernames = list.toArray(new String[0]);
									buddySignOff(o.toString());

									// Print the array back to the file (will overwrite the
									// previous file
									Client.writeBuddyListToFile(usernames);
									listOfUsersJComboBox.removeItemAt(listOfUsersJComboBox.getSelectedIndex());
																	
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						});
						
						cancelJButton.addActionListener(new ActionListener() { 
							public void actionPerformed(ActionEvent event) {
								removeWindow.dispose();
							}
						});
						
						System.gc();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		removeContactLabel.addMouseListener(removeBuddyMouseListener);

		// MouseListener for the BuddyList
		// Opens a tab or focuses a tab when a user name in the contact list is
		// double-clicked
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				JList theList = (JList) mouseEvent.getSource();

				// If it was double-clicked
				if (mouseEvent.getClickCount() == 2) {

					// Find out what was double-clicked
					int index = theList.locationToIndex(mouseEvent.getPoint());
					if (index >= 0) {

						// Get the buddy that was double-clicked
						Object o = theList.getModel().getElementAt(index);

						// Create a tab for the conversation if it doesn't exist
						if (imTabbedPane.indexOfTab(o.toString()) == -1) {
							makeTab(o.toString());
							JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
							Component[] currentTabComponents = currentTab.getComponents();
							JTextComponent textFieldToFocus = (JTextComponent) currentTabComponents[1];
							
							textFieldToFocus.addKeyListener(new KeyListener() {
								public void keyPressed(KeyEvent e) {
								}
								public void keyReleased(KeyEvent e) {
									if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
										JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
										int tempIndex = imTabbedPane.getSelectedIndex();
										imTabbedPane.remove(currentTab);
										
										if(tempIndex > 0)
										{
											imTabbedPane.setSelectedIndex(tempIndex - 1);
											currentTab = (JPanel) imTabbedPane.getSelectedComponent();
											Component[] currentTabComponents = currentTab.getComponents();
											JTextComponent currentTextField = (JTextComponent) currentTabComponents[1];
											currentTextField.requestFocusInWindow();
										}
										else
										{
											if(imTabbedPane.getTabCount() > 1)
											{
												imTabbedPane.setSelectedIndex(tempIndex);
												currentTab = (JPanel) imTabbedPane.getSelectedComponent();		
												Component[] currentTabComponents = currentTab.getComponents();
												JTextComponent currentTextField = (JTextComponent) currentTabComponents[1];
												currentTextField.requestFocusInWindow();											
											}
											else if(imTabbedPane.getTabCount() > 0)
											{
												imTabbedPane.setSelectedIndex(0);
												currentTab = (JPanel) imTabbedPane.getSelectedComponent();
												Component[] currentTabComponents = currentTab.getComponents();
												JTextComponent currentTextField = (JTextComponent) currentTabComponents[1];
												currentTextField.requestFocusInWindow();											
											}
										}
										}
									}
								public void keyTyped(KeyEvent e) {
								}
								});
							textFieldToFocus.requestFocusInWindow();
							
						} else {
							// Focus the tab for this user name if it already
							// exists
							imTabbedPane.setSelectedIndex(imTabbedPane
									.indexOfTab(o.toString()));
							JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
							Component[] currentTabComponents = currentTab.getComponents();
							Component textFieldToFocus = currentTabComponents[1];
							textFieldToFocus.requestFocusInWindow();
						}
					}
				}
			}
		};

		// Add the mouseListener to the contact list
		userBox.addMouseListener(mouseListener);
		userBox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Places the area for the tabs
		imTabbedPane.setBounds(10, 10, 580, 537);
		lockIconLabel.setIcon(lockIcon);
		lockIconLabel.setVisible(true);
		lockIconLabel.setBounds(148, 10, 580, 537);
		// Generate panel by adding appropriate components
		panel = new JPanel();
		panel.setLayout(null);
		panel.add(contactList);
		panel.add(addContactLabel);
		panel.add(removeContactLabel);
		panel.add(lockIconLabel);
		panel.add(imTabbedPane);

		// Initialize window frame
		imContentFrame.setJMenuBar(menuBar);
		imContentFrame.setContentPane(panel);
		imContentFrame.setVisible(true);

	}

	public Object[] getCurrentSettingsArray()
	{
		return currentSettings;
	}
	
	public void setCurrentSettingsArray(Object[] settingsArray)
	{
		currentSettings = settingsArray;
	}

	// Make a tab for a conversation
	public void makeTab(String user) {
		lockIconLabel.setVisible(false);
		// Create a hash table mapping a user name to the JPanel in a tab
		tabPanels.put(user, new MapTextArea(user, spellCheckFlag));
		// Make a temporary object for that JPanel
		MapTextArea temp = (MapTextArea) tabPanels.get(user);
		// Actually pull the JPanel out
		JPanel tempPanel = temp.getJPanel();
		// Create a tab with that JPanel on it
		imTabbedPane.addTab(user, null, tempPanel, "Something");
		// Add close button to tab
		new CloseTabButton(imTabbedPane, imTabbedPane.indexOfTab(user));
		// Focus the new tab
		imTabbedPane.setSelectedIndex(imTabbedPane.indexOfTab(user));
		//Garbage collect!
		System.gc();
	}
	
	public void setSystemTrayIcon(boolean activated) throws AWTException
	{
		SystemTray tray = SystemTray.getSystemTray();
		TrayIcon[] trayArray = tray.getTrayIcons();
		int tlength = trayArray.length;
		if(activated)
		{
			if(tlength == 0)
			{
				Image trayImage = Toolkit.getDefaultToolkit().getImage("../images/sysTray.gif");
				ActionListener exitListener = new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(debug==1)System.out.println("Exiting...");
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
		else
		{
			for(int x = 0; x < tlength; x++)
				tray.remove(trayArray[x]);
		}
	}
	
	public void closeTabWithESC(boolean activated)
	{
		int tabCount = imTabbedPane.getTabCount();
		JPanel currentTab;
		Component[] currentTabComponents;
		JTextComponent currentTextField;
		if(activated)
		{
			// Assign key listener to all existing text fields
			for(int x = 0; x < tabCount; x++)
			{
				imTabbedPane.setSelectedIndex(x);
				currentTab = (JPanel) imTabbedPane.getSelectedComponent();
				currentTabComponents = currentTab.getComponents();
				currentTextField = (JTextComponent) currentTabComponents[1];
				currentTextField.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
				}
				public void keyReleased(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
						JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
						int tempIndex = imTabbedPane.getSelectedIndex();
						imTabbedPane.remove(currentTab);
						
						if(tempIndex > 0)
						{
							imTabbedPane.setSelectedIndex(tempIndex - 1);
							currentTab = (JPanel) imTabbedPane.getSelectedComponent();
							Component[] currentTabComponents = currentTab.getComponents();
							JTextComponent currentTextField = (JTextComponent) currentTabComponents[1];
							currentTextField.requestFocusInWindow();
						}
						else
						{
							if(imTabbedPane.getTabCount() > 1)
							{
								imTabbedPane.setSelectedIndex(tempIndex);
								currentTab = (JPanel) imTabbedPane.getSelectedComponent();		
								Component[] currentTabComponents = currentTab.getComponents();
								JTextComponent currentTextField = (JTextComponent) currentTabComponents[1];
								currentTextField.requestFocusInWindow();											
							}
							else if(imTabbedPane.getTabCount() > 0)
							{
								imTabbedPane.setSelectedIndex(0);
								currentTab = (JPanel) imTabbedPane.getSelectedComponent();
								Component[] currentTabComponents = currentTab.getComponents();
								JTextComponent currentTextField = (JTextComponent) currentTabComponents[1];
								currentTextField.requestFocusInWindow();											
							}
						}
						}
					}
				public void keyTyped(KeyEvent e) {
				}
				});
			}
		}
		else
		{
			for(int x = 0; x < tabCount; x++)
			{
				imTabbedPane.setSelectedIndex(x);
				currentTab = (JPanel) imTabbedPane.getSelectedComponent();
				currentTabComponents = currentTab.getComponents();
				currentTextField = (JTextComponent) currentTabComponents[1];
				KeyListener[] fieldListeners =  currentTextField.getKeyListeners();
				if (fieldListeners != null)
				{
					currentTextField.removeKeyListener(fieldListeners[0]);
				}
			}
		}
		//Garbage collect!
		System.gc();
	}
	
	// Adjust spell check setting in current and future text fields
	public void setSpellCheck(boolean activated)
	{
		// Retrieve necessary tab and component data
		int tabCount = imTabbedPane.getTabCount();
		JPanel currentTab;
		Component[] currentTabComponents;
		JTextComponent currentTextField;
		if(activated)
		{
			// Register all current text fields for spell check
			for(int x = 0; x < tabCount; x++)
			{
				imTabbedPane.setSelectedIndex(x);
				currentTab = (JPanel) imTabbedPane.getSelectedComponent();
				currentTabComponents = currentTab.getComponents();
				currentTextField = (JTextComponent) currentTabComponents[1];
				SpellChecker.register(currentTextField, true, true, true);
			}
			// Enable future spell check registration
			spellCheckFlag = true;
		}
		else
		{
			// Unregister all current text fields with spell check
			for(int x = 0; x < tabCount; x++)
			{
				imTabbedPane.setSelectedIndex(x);
				currentTab = (JPanel) imTabbedPane.getSelectedComponent();
				currentTabComponents = currentTab.getComponents();
				currentTextField = (JTextComponent) currentTabComponents[1];
				SpellChecker.unregister(currentTextField);
			}
			// Disable future spell check registration
			spellCheckFlag = false;
		}
	}
	
	public void setEnableNotifications(boolean activated)
	{
		int tabCount = imTabbedPane.getTabCount();
		
		//Enable all tabs for notifications
		if(activated)
		{
			for(int x = 0; x < tabCount; x++)
			{
				imTabbedPane.setSelectedIndex(x);
				JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
				Component[] currentTabComponents = currentTab.getComponents();
				JScrollPane currentScrollPane = (JScrollPane) currentTabComponents[0];
				JTextArea currentTextArea = (JTextArea) currentScrollPane.getViewport().getComponent(0);
				
				currentTextArea.getDocument().addDocumentListener(new DocumentListener() {
					public void insertUpdate(DocumentEvent e) {
						if(debug==1)System.out.println("TEXT ADDED!!");
						Icon alertIcon = new ImageIcon("../images/alert.png");
						int currentTabIndex = imTabbedPane.getSelectedIndex();
						JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
						CloseTabButton c = (CloseTabButton)imTabbedPane.getTabComponentAt(currentTabIndex);
						JButton currentButton = (JButton) c.getComponent(1);
						if(debug==1)System.out.println("Current button: " + currentButton.toString());
						currentButton.setIcon(alertIcon);
					}
					public void changedUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
					}
					public void removeUpdate(DocumentEvent e) {
						// TODO Auto-generated method stub
					}
				});
			}
		}
		else
		{
			for(int x = 0; x < tabCount; x++)
			{
				imTabbedPane.setSelectedIndex(x);
				JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
				Component[] currentTabComponents = currentTab.getComponents();
				JScrollPane currentScrollPane = (JScrollPane) currentTabComponents[0];
				JTextArea currentTextArea = (JTextArea) currentScrollPane.getViewport().getComponent(0);
				DocumentListener[] dls = (DocumentListener[]) (currentTab.getListeners(DocumentListener.class));
				if(dls.length > 0)
				{
					currentTextArea.getDocument().removeDocumentListener(dls[0]);
				}
			}
		}
	}
	
	public void setEnableSounds(boolean activated)
	{
		
	}

	// Makes a new hash table with user's online status
	public void mapUserStatus(String username, int status) {
		if(debug==1)System.out.println("Username: " + username + "\nStatus: " + status);
		userStatus.put(username, status);
	}
	//TODO: Fix user folder that preference file writes to!
	private Object[] loadSavedPreferences()
	{	if(debug==1)System.out.println("Importing preferences");
		Object[] settingsArray = new Object[11];
		int arrayCount = 0;
		String line = null;
		String temp = null;
		try {
			
			File newPrefFile = new File("users/" + Client.username + "/athena.conf");
			if(!(newPrefFile.exists())) { 
				if(debug==1)System.out.println("File Not Found! Copying...");
				File oldFile = new File("users/Aegis/athena.conf");
				FileChannel inChannel = new FileInputStream(oldFile).getChannel();
				FileChannel outChannel = new FileOutputStream(newPrefFile).getChannel();
				try {
					inChannel.transferTo(0, inChannel.size(), outChannel);
				} 
				catch (IOException e) {
					throw e;
				}
				finally {
					if (inChannel != null) inChannel.close();
					if (outChannel != null) outChannel.close();
				}
			}
			BufferedReader inPref = new BufferedReader(new FileReader("./users/" + Client.username + "/athena.conf"));
			while((line = inPref.readLine()) != null)
			{
				if(line.equals("[GENERAL]"))
				{
					//Get general settings
					//Get allowSystemTray (boolean)
					temp = inPref.readLine().substring(16);
					settingsArray[arrayCount] = temp;
					arrayCount++;
					//Get allowESCTab (boolean)
					temp = inPref.readLine().substring(12);
					settingsArray[arrayCount] = temp;
					arrayCount++;
					//Get enableSpellCheck (boolean)
					temp = inPref.readLine().substring(17);
					settingsArray[arrayCount] = temp;
					arrayCount++;
				}
				if(line.equals("[NOTIFICATIONS]"))
				{
					//Get notification settings
					//Get enableNotifications (boolean)
					temp = inPref.readLine().substring(20);
					settingsArray[arrayCount] = temp;
					arrayCount++;
					//Get enableSounds (boolean)
					temp = inPref.readLine().substring(13);
					settingsArray[arrayCount] = temp;
					arrayCount++;
				}
				if(line.equals("[ENCRYPTION]"))
				{
					//Get encryption settings
					//Get encryptionType (integer)
					inPref.readLine();
					inPref.readLine();
					temp = inPref.readLine().substring(15);
					settingsArray[arrayCount] = temp;
					arrayCount++;
				}
				if(line.equals("[FORMATTING]"))
				{
					//Get formatting settings
					//Get fontFace (string)
					temp = inPref.readLine().substring(9);
					settingsArray[arrayCount] = temp;
					arrayCount++;
					//Get fontBold (boolean)
					temp = inPref.readLine().substring(9);
					settingsArray[arrayCount] = temp;
					arrayCount++;
					//Get fontItalic (boolean)
					temp = inPref.readLine().substring(11);
					settingsArray[arrayCount] = temp;
					arrayCount++;
					//Get fontUnderline (boolean)
					temp = inPref.readLine().substring(14);
					settingsArray[arrayCount] = temp;
					arrayCount++;
				}
				if(line.equals("[THEME]"))
				{
					//Get theme settings
					//Get activeTheme (integer)
					temp = inPref.readLine().substring(12);
					settingsArray[arrayCount] = temp;
					arrayCount++;
				}
			//inPref.close();
				//Garbage collect!
				System.gc();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return settingsArray;
	}

	// End of class ClientApplet
}

// This class holds all of the JComponents and acts as an interface to each
// conversation's tab
class MapTextArea extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2557115166519071868L;

	// The user name associated with the tab
	String username = null;

	// All of the JComponents in the tab
	public JPanel myJPanel;
	public JTextArea myTA;
	public JTextField myTF;	

	// The index of the tab this lives in
	int tabIndex = -1;
		
	// Constructor
	MapTextArea(String user, boolean spellCheckFlag) { 
		
		 try {
			//Register the dictionaries for the spell checker
			 SpellChecker.registerDictionaries( new URL("file", null, ""), "en,de", "en" );
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//Create the JPanel and put all of the components in it
		myJPanel = new JPanel();
		myJPanel.setLayout(null);

		//Create the text area and the scroll pane around it
		myTA = new JTextArea();
		myTA.setEditable(false);
		myTA.setLineWrap(true);
		myTA.setWrapStyleWord(true);
        // enable the spell checking on the text component with all features


		JScrollPane mySP = new JScrollPane(myTA);
		mySP.setBounds(10,10,559,450);
		mySP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		mySP.setOpaque(true);    
		myJPanel.add(mySP);

		//Create the text field
		myTF = new JTextField();
		myTF.setBounds(10,469,560,30);
		myJPanel.add(myTF);

		//Register the spell checker in the text field
		if (spellCheckFlag)
			SpellChecker.register(myTF, true, true, true);
		
		username = user;

		//Add an actionListener to the text field to send messages
		myTF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(!(myTF.getText().equals(""))) { 
				Client.processMessage(myTF.getText());
				}
			}
		});
		
		//Set font to Arial
		Font font = new Font("Arial",Font.PLAIN,17);
		myTA.setFont(font);
	}

	// Set the user name associated with the tab
	public void setUserName(String user) {
		username = user;
	}

	// Get the user name associated with the tab
	public String getUserName() {
		return username;
	}

	// Set the index of the tab for this JPanel
	public void setTabIndex(int index) {
		tabIndex = index;
	}

	// Get the tab index for this JPanel
	public int getTabIndex() {
		return tabIndex;
	}

	// Get the JPanel for the tab
	public JPanel getJPanel() {
		return myJPanel;
	}

	// Set the text color (does nothing)
	public void setTextColor(Color color) {
		myTA.setForeground(color);
	}

	// Write a string to the text area
	public void writeToTextArea(String message) {
		myTA.append(message);
	}

	// Move the cursor to the end of the ScrollPane
	// TODO: Sometimes it shows highlighted text
	public void moveToEnd() {
		myTA.setCaretPosition(myTA.getText().length());
	}

	// Clear the text out of the text field
	public void clearTextField() {
		myTF.setText("");
	}
}

class CloseTabButton extends JPanel implements ActionListener {
	  /**
	 * 
	 */
	//private static final long serialVersionUID = -6032110177913133517L;
	private JTabbedPane pane;
	  public CloseTabButton(JTabbedPane pane, int index) {
	    this.pane = pane;
	    setOpaque(false);
	    add(new JLabel(
	        pane.getTitleAt(index),
	        pane.getIconAt(index),
	        JLabel.LEFT));
	    Icon closeIcon = new ImageIcon("../images/close_button.png");
	    JButton btClose = new JButton(closeIcon);
	    btClose.setPreferredSize(new Dimension(
	        closeIcon.getIconWidth(), closeIcon.getIconHeight()));
	    add(btClose);
	    btClose.addActionListener(this);
	    pane.setTabComponentAt(index, this);
	  }
	  
	  public void actionPerformed(ActionEvent e) {
	    int i = pane.indexOfTabComponent(this);
	    if (i != -1) {
	      pane.remove(i);
		  ClientApplet.lockIconLabel.setVisible(true);
		  System.gc();
	    }
	  }
	}

