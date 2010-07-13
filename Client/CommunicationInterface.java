/* Athena/Aegis Encrypted Chat Platform
 * CommunicationInterface.java: Main window. Houses chat tabs and buddylist.
 *
 * Copyright (C) 2010  OlympuSoft
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.swing.text.Document;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import com.inet.jortho.SpellChecker;

/**
 * The main window of Athena: IM/Chat tabs, etc
 * @author OlympuSoft
 */
public class CommunicationInterface extends JFrame {

	/**
	 * Hashtable for buddylist users and their current status
	 */
	public Hashtable<String, Integer> userStatus = new Hashtable<String, Integer>();
	/**
	 * I'm....not entirely sure
	 */
	public Hashtable<Document, JPanel> uniqueIDHash = new Hashtable<Document, JPanel>();
	/**
	 * Used to get and display system fonts in the preferences window
	 */
	public static Hashtable<String, String> fontFamilyTable = new Hashtable<String, String>();
	/**
	 * JFrames, one for the IM tabs, one for the buddylist
	 */
	public static JFrame imContentFrame, buddyListFrame;
	/**
	 * Checks if the settings file has been loaded
	 */
	public boolean settingsLoaded = false;
	/**
	 * TabbedPane for IM and chat tabs
	 */
	public JTabbedPane imTabbedPane = new JTabbedPane();
	/**
	 * Keeps track of the IM/Chat tabs and their names
	 */
	public Hashtable<String, MapTextArea> tabPanels = new Hashtable<String, MapTextArea>();
	/**
	 * Used to get the fonts list
	 */
	public GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	/**
	 * Loads the font list from the GraphicsEnvironment
	 */
	public Font[] allFonts = ge.getAllFonts();

	private static final long serialVersionUID = -7742402292330782311L;
	private static final int debug = 0;
	
	// Define the listModel for the JList
	private DefaultListModel contactListModel = new DefaultListModel();
	private DefaultListModel inviteListModel = new DefaultListModel();

	// Components for the visual display of the chat windows
	private JList userBox = new JList(contactListModel);
	private JList inviteBox = new JList(inviteListModel);
	private JList contactBox;
	private JTextField chatNameField = new JTextField();
	private JFrame chatWindow;
	private JScrollPane contactList, chatList;
	private JMenuBar menuBar = new JMenuBar();
	private JMenu file, edit, encryption, view, help;
	private JMenuItem disconnect, exit, preferences, createChat, sendFile;
	private JPanel panel;
	private JComboBox statusBox = new JComboBox(new String[]{"Available", "Busy"});
	private Border buttonBorder = BorderFactory.createRaisedBevelBorder();
	private Border whiteColor = BorderFactory.createLineBorder(Color.white);
	private Border oneColor = BorderFactory.createLineBorder(Color.black);
	private Border twoColor = BorderFactory.createLineBorder(new Color(0, 0, 120)); //Dark blue
	private Border threeColor = BorderFactory.createLineBorder(new Color(218, 165, 32)); //Goldenrod
	private Border contactListBorder, chatListBorder;
	private ImageIcon lockIcon = new ImageIcon("images/lockicon.png");
	private ImageIcon logoIcon = new ImageIcon("images/logo.png");
	private ImageIcon logoIconBig = new ImageIcon("images/logobig.png");
	private static JLabel lockIconLabel = new JLabel();
	private static JLabel logoIconLabel = new JLabel();
	private TitledBorder buddyBorder;
	private boolean enableSystemTray;
	private boolean enableESCToClose;
	private boolean enableSpellCheck;
	private boolean enableSounds;
	private int encryptionType;
	private String fontFace;
	private boolean fontBold;
	private boolean fontItalic;
	private boolean fontUnderline;
	private int fontSize;
	private int activeTheme;
	private boolean userStatusFlag = false;
	private static Object[] currentSettings = new Object[11];

	/**
	 * Method to add users to the JList when they sign on
	 * @param availableUser The user to add to the buddylist
	 */
	public void newBuddyListItems(String availableUser) {
		if (contactListModel.indexOf(availableUser) == -1) {
			contactListModel.addElement(availableUser);
		}
	}

	/**
	 * Method to remove user from the JList who signs off
	 * @param offlineUser The user to remove from the buddylist
	 */
	public void buddySignOff(String offlineUser) {
		contactListModel.removeElement(offlineUser);
	}

	/**
	 * Method to remove user from the JList who signs off
	 * @param offlineUser The user to remove from the chat userlist
	 */
	public void chatSignOff(String offlineUser) {
		inviteListModel.removeElement(offlineUser);
	}

	/**
	 * Method to add users to the list of chat users
	 * @param availableUser User to add to the chat userlist
	 */
	public void newChatListItems(String availableUser) {
		if (inviteListModel.indexOf(availableUser) == -1) {
			inviteListModel.addElement(availableUser);
		}
	}

	/**
	 * The main window. IM/Chat tabs and buddylist.
	 */
	CommunicationInterface() {

		// Initialize chat window
		UIManager.put("OptionPane.informationIcon", logoIcon);
		UIManager.put("OptionPane.errorIcon", logoIcon);
		UIManager.put("OptionPane.questionIcon", logoIcon);
		UIManager.put("OptionPane.warningIcon", logoIcon);

		// Get the default toolkit
		Toolkit toolkit = Toolkit.getDefaultToolkit();

		// Get the current screen size
		Dimension scrnsize = toolkit.getScreenSize();
		int width = (int) scrnsize.getWidth();
		int height = (int) scrnsize.getHeight();

		//Load the fonts from the graphics environment
		String[] allFontNames = new String[allFonts.length];
		fontFamilyTable.clear();
		for (int a = 0; a < allFonts.length; a++) {
			allFontNames[a] = allFonts[a].getFontName();
			fontFamilyTable.put(allFonts[a].getFontName(), allFonts[a].getFamily());
			//System.out.println("FONT NAME: " + allFonts[a].getFontName() + "\t\tFONT FAMILY: " + allFonts[a].getFamily());
		}

		//Load preference settings
		Object[] settingsArray = loadSavedPreferences();
		setCurrentSettingsArray(settingsArray);
		enableSystemTray = Boolean.parseBoolean(settingsArray[0].toString());
		try {
			setSystemTrayIcon(enableSystemTray);
		} catch (AWTException e1) {

			e1.printStackTrace();
		}

		//Load preferences
		enableESCToClose = Boolean.parseBoolean(settingsArray[1].toString());
		closeTabWithESC(enableESCToClose);
		enableSpellCheck = Boolean.parseBoolean(settingsArray[2].toString());
		setSpellCheck(enableSpellCheck);
		enableSounds = Boolean.parseBoolean(settingsArray[3].toString());
		Athena.setEnableSounds(enableSounds);
		encryptionType = Integer.parseInt(settingsArray[4].toString());
		fontFace = settingsArray[5].toString();
		fontBold = Boolean.parseBoolean(settingsArray[6].toString());
		fontItalic = Boolean.parseBoolean(settingsArray[7].toString());
		fontUnderline = Boolean.parseBoolean(settingsArray[8].toString());
		fontSize = Integer.parseInt(settingsArray[9].toString());
		activeTheme = Integer.parseInt(settingsArray[10].toString());
		
		//This is the main frame for the IMs
		imContentFrame = new JFrame("Athena Chat Application - " + Athena.username);
		imContentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		imContentFrame.setSize(813, 610);
		imContentFrame.setResizable(false);
		imContentFrame.setLocation(width - (width / 2) - 407, height - (height / 2) - 305);
		imContentFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("images/logosmall.png"));

		// Create the file menu.
		file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);

		//Create button File -> Create Chat
		createChat = new JMenuItem("Create Chat");
		createChat.setMnemonic(KeyEvent.VK_C);
		file.add(createChat);

		//Create button File -> Send File
		sendFile = new JMenuItem("Send File");
		sendFile.setMnemonic(KeyEvent.VK_C);
		file.add(sendFile);

		// Create button File -> Disconnect
		disconnect = new JMenuItem("Disconnect");
		disconnect.setMnemonic(KeyEvent.VK_D);
		file.add(disconnect);

		// Create button File -> Exit
		exit = new JMenuItem("Exit");
		exit.setMnemonic(KeyEvent.VK_X);
		file.add(exit);

		// Add the file menu to the menu bar
		menuBar.add(file);

		// Create the edit menu.
		edit = new JMenu("Edit");
		edit.setMnemonic(KeyEvent.VK_E);
		menuBar.add(edit);

		// Create button Edit -> Preferences
		preferences = new JMenuItem("Preferences");
		preferences.setMnemonic(KeyEvent.VK_P);
		edit.add(preferences);

		// Create button Edit -> Change Password
		JMenuItem changePassword = new JMenuItem("Change Password");
		edit.add(changePassword);

		// Create the encryption menu.
		encryption = new JMenu("Encryption");
		encryption.setMnemonic(KeyEvent.VK_C);
		menuBar.add(encryption);

		// Create button Encryption -> Export Key Pair
		JMenuItem exportKey = new JMenuItem("Export Key Pair");
		encryption.add(exportKey);
		JMenuItem startDP = new JMenuItem("Start/Stop DirectProtect Here");
		encryption.add(startDP);

		// Create the view menu
		view = new JMenu("View");
		view.setMnemonic(KeyEvent.VK_V);
		menuBar.add(view);

		// Create button View -> Offline users in contact list
		JMenuItem offlineUsers = new JMenuItem("Offline Contacts in List");
		view.add(offlineUsers);

		// Create button View -> Contact Aliases
		JMenuItem contactAlias = new JMenuItem("Contact Aliases");
		view.add(contactAlias);

		// Create the help menu
		help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		menuBar.add(help);

		// Create the button Help -> About
		JMenuItem about = new JMenuItem("About Athena");
		about.setMnemonic(KeyEvent.VK_A);
		help.add(about);

		JMenuItem web = new JMenuItem("Athena Website");
		web.setMnemonic(KeyEvent.VK_W);
		help.add(web);

		JMenuItem bugReport = new JMenuItem("Report a bug!");
		web.setMnemonic(KeyEvent.VK_R);
		help.add(bugReport);

		// ActionListener to make the disconnect menu item disconnect
		createChat.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				createChatWindow();
			}
		});

		// ActionListener to make the disconnect menu item disconnect
		startDP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(!Athena.sessionKeys.containsKey(imTabbedPane.getTitleAt(imTabbedPane.getSelectedIndex()))){
					System.out.println("Inviting user " + imTabbedPane.getTitleAt(imTabbedPane.getSelectedIndex()) + " to connect directly to us.");
					Athena.directProtect(imTabbedPane.getTitleAt(imTabbedPane.getSelectedIndex()));
				}
				else {
					Athena.leaveDP(imTabbedPane.getTitleAt(imTabbedPane.getSelectedIndex()));
				}
			}
		});

		// ActionListener to make the disconnect menu item disconnect
		sendFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				//Create a file chooser
				final JFileChooser fc = new JFileChooser();
				//Open the file chooser
				int returnVal = fc.showOpenDialog(CommunicationInterface.this);
				try {
					//Establish DP first!
				if(!Athena.sessionKeys.containsKey(imTabbedPane.getTitleAt(imTabbedPane.getSelectedIndex()))){
					System.out.println("Inviting user " + imTabbedPane.getTitleAt(imTabbedPane.getSelectedIndex()) + " to connect directly to us.");
					Athena.directProtect(imTabbedPane.getTitleAt(imTabbedPane.getSelectedIndex()));
					Athena.sendFile(fc.getSelectedFile());
				}
				else {
					Athena.sendFile(fc.getSelectedFile());
				}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		// ActionListener to make the disconnect menu item disconnect
		disconnect.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				// Clear the Buddy list when disconnected
				contactListModel.clear();
				Athena.disconnect();
				//Get rid of this window and open a new Login Window
				imContentFrame.dispose();
				try {
					new AuthenticationInterface();
				} catch (AWTException e) {

					e.printStackTrace();
				}
			}
		});

		// ActionListener to make the exit menu item exit
		exit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				Athena.exit();
			}
		});

		// ActionListener to show Preferences window
		preferences.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				new PreferencesInterface();
			}
		});

		// ActionListener to show About Athena window
		about.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				try {
					new AboutInterface();
				} catch (AWTException e) {
					e.printStackTrace();
				}
			}
		});

		// ActionListener to show About Athena window
		bugReport.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				try {
					new BugReportInterface();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// ActionListener to open browser link to Athena website
		web.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				try {
					String url = "http://athenachat.org";
					java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		changePassword.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(null, "This feature will be implemented during the summer semester, stay tuned!", "To Be Continued...", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		exportKey.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(null, "This feature will be implemented during the summer semester, stay tuned!", "To Be Continued...", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		offlineUsers.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(null, "This feature will be implemented during the summer semester, stay tuned!", "To Be Continued...", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		contactAlias.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(null, "This feature will be implemented during the summer semester, stay tuned!", "To Be Continued...", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		// Adds the contact list to a scroll pane
		userBox.setCellRenderer(new MyCellRenderer());
		contactList = new JScrollPane(userBox);
		chatList = new JScrollPane(inviteBox);
		contactList.setBounds(600, 2, 195, 450);
		chatList.setBounds(600, 2, 195, 450);
		Border contactListBorderA = BorderFactory.createCompoundBorder(oneColor, oneColor);
		Border chatListBorderA = BorderFactory.createCompoundBorder(twoColor, twoColor);
		Border contactListBorderB = BorderFactory.createCompoundBorder(contactListBorderA, threeColor);
		Border contactListBorderC = BorderFactory.createCompoundBorder(contactListBorderB, oneColor);
		Border contactListBorderAA = BorderFactory.createCompoundBorder(contactListBorderC, oneColor);
		Border chatListBorderB = BorderFactory.createCompoundBorder(chatListBorderA, whiteColor);
		Border chatListBorderC = BorderFactory.createCompoundBorder(chatListBorderB, twoColor);
		Border chatListBorderAA = BorderFactory.createCompoundBorder(chatListBorderC, twoColor);
		contactListBorder = contactListBorderAA;
		chatListBorder = chatListBorderAA;
		buddyBorder = BorderFactory.createTitledBorder(contactListBorderAA, Athena.username + "'s Contact List", TitledBorder.CENTER,
				TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.PLAIN, 14), Color.black);
		chatListBorder = BorderFactory.createTitledBorder(chatListBorderAA, "Group Chat List", TitledBorder.CENTER,
				TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.PLAIN, 14), new Color(0, 0, 120));
		contactList.setBorder(buddyBorder);
		chatList.setBorder(chatListBorder);

		JButton addContactLabel = new JButton(new ImageIcon("images/addUser.png"));
		JButton removeContactLabel = new JButton(new ImageIcon("images/removeUser.png"));
		JButton homeListButton = new JButton(new ImageIcon("images/home-icon.png"));

		addContactLabel.setBackground(new Color(240, 240, 240));
		buttonBorder = BorderFactory.createCompoundBorder(buttonBorder, buttonBorder);
		addContactLabel.setBorder(buttonBorder);

		removeContactLabel.setBackground(new Color(240, 240, 240));
		removeContactLabel.setBorder(buttonBorder);

		homeListButton.setBackground(new Color(240, 240, 240));
		homeListButton.setBorder(buttonBorder);

		addContactLabel.setVisible(true);
		removeContactLabel.setVisible(true);
		homeListButton.setVisible(true);
		addContactLabel.setBounds(610, 490, 50, 50);
		removeContactLabel.setBounds(670, 490, 50, 50);
		homeListButton.setBounds(730, 490, 50, 50);

		homeListButton.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent mouseEvent) {
				{
					contactList.setVisible(true);
					chatList.setVisible(false);
				}
			}
		});

		imTabbedPane.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent mouseEvent) {
				if (imTabbedPane.getTabCount() > 0) {
					FocusCurrentTextField();
				}
			}
		});

		// MouseListener for the AddUser image
		MouseListener addBuddyMouseListener = new MouseAdapter() {

			public void mouseClicked(MouseEvent mouseEvent) {
				String usernameToAdd = JOptionPane.showInputDialog("Input the user name to add to your contact list:");
				try {
					if (usernameToAdd != null) {
						Athena.buddyList(usernameToAdd);
						Athena.instantiateBuddyList(usernameToAdd);
					}
				} catch (Exception e) {

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
					String[] usernames = Athena.returnBuddyListArray();

					// Find out what was double-clicked
					int index = theList.getSelectedIndex();
					if (debug == 1) {
						System.out.println(index);
					}
					if (index >= 0) {

						// Get the buddy that was double-clicked
						Object o = theList.getModel().getElementAt(index);

						int ans = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove " + o.toString() + "?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
						if (ans == JOptionPane.YES_OPTION) {
							ArrayList<String> list = new ArrayList<String>(Arrays.asList(usernames));
							list.removeAll(Arrays.asList(o));
							usernames = list.toArray(new String[0]);
							buddySignOff(o.toString());

							// Print the array back to the file (will overwrite the
							// previous file
							Athena.writeBuddyListToFile(usernames);

						} else {
							return;
						}
					} //If there wasn't something selected, bring up a new window that will let them choose who they want to remove
					else {

						final JFrame removeWindow = new JFrame("Remove user");
						final JPanel contentPane = new JPanel();
						final JComboBox listOfUsersJComboBox = new JComboBox();
						final JButton removeJButton, cancelJButton;
						removeJButton = new JButton("Remove");
						removeWindow.setResizable(false);
						removeWindow.setLocationRelativeTo(imContentFrame);
						cancelJButton = new JButton("Done");

						contentPane.setLayout(null);

						removeWindow.setSize(150, 155);
						listOfUsersJComboBox.setBounds(20, 20, 100, 25);
						removeJButton.setBounds(20, 60, 100, 25);
						cancelJButton.setBounds(20, 95, 100, 25);

						for (int x = 0; x < usernames.length; x++) {
							listOfUsersJComboBox.addItem(usernames[x]);
						}

						if (listOfUsersJComboBox.getItemCount() == 0) {
							removeJButton.setEnabled(false);
						}
						contentPane.add(listOfUsersJComboBox);
						contentPane.add(removeJButton);
						contentPane.add(cancelJButton);
						removeWindow.add(contentPane);
						removeWindow.setVisible(true);

						removeJButton.addActionListener(new ActionListener() {

							public void actionPerformed(ActionEvent event) {
								try {
									String[] usernames = Athena.returnBuddyListArray();

									Object o = listOfUsersJComboBox.getSelectedItem();
									ArrayList<String> list = new ArrayList<String>(Arrays.asList(usernames));
									list.removeAll(Arrays.asList(o));
									usernames = list.toArray(new String[0]);
									buddySignOff(o.toString());

									// Print the array back to the file (will overwrite the
									// previous file
									Athena.writeBuddyListToFile(usernames);
									listOfUsersJComboBox.removeItemAt(listOfUsersJComboBox.getSelectedIndex());
									if (listOfUsersJComboBox.getItemCount() == 0) {
										removeJButton.setEnabled(false);
									}
								} catch (Exception e) {

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
				//JList theList = (JList) mouseEvent.getSource();
				JList theList = (JList) userBox;
				Object o;
				// If it was double-clicked
				if (mouseEvent.getClickCount() == 1 && (!(theList.getModel().toString().equals("[]")))) {
					int index = theList.locationToIndex(mouseEvent.getPoint());
					Rectangle r = theList.getCellBounds(index, index);
					if (r.contains(mouseEvent.getPoint())) {
						//Focus selected object
						theList.getSelectionModel().setLeadSelectionIndex(index);
					} else {
						//Clear selection if user clicks outside list selection
						theList.getSelectionModel().setLeadSelectionIndex(theList.getModel().getSize());
						theList.clearSelection();
					}
				}
				if (mouseEvent.getClickCount() == 2 && (!(theList.getModel().toString().equals("[]")))) {
					// Find out what was double-clicked
					int index = theList.locationToIndex(mouseEvent.getPoint());
					Rectangle r = theList.getCellBounds(index, index);
					if (r.contains(mouseEvent.getPoint())) {

						// Get the buddy that was double-clicked
						o = theList.getModel().getElementAt(index);

						// Create a tab for the conversation if it doesn't exist
						if (imTabbedPane.indexOfTab(o.toString()) == -1) {
							makeTab(o.toString(), true);
							if (!(userStatusFlag)) {
								FocusCurrentTextField();
							}
						} else {
							// Focus the tab for this user name if it already
							// exists
							imTabbedPane.setSelectedIndex(imTabbedPane.indexOfTab(o.toString()));
							if (!(userStatusFlag)) {
								FocusCurrentTextField();
							}
						}
					} else {
						//Clear selection if user clicks outside list selection
						theList.getSelectionModel().setLeadSelectionIndex(theList.getModel().getSize());
						theList.clearSelection();
					}
				}
			}
		};

		statusBox.setBounds(602, 452, 191, 25);
		statusBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				if (statusBox.getSelectedItem().equals("Busy")) {
					String ans = JOptionPane.showInputDialog("Please enter an auto-response message:");
					if (ans != null && ans.length() > 0) {
						Athena.setAwayText(ans);
						Athena.setStatus(1);
						setUserStatus(true);
					} else if (ans == null) {
						//If canceled, do nothing
						statusBox.setSelectedItem("Available");
					} else {
						JOptionPane.showMessageDialog(null, "Status message cannot be blank!\n\tPlease try again.", "Input Error", JOptionPane.ERROR_MESSAGE);
						statusBox.setSelectedItem("Available");
					}
				} else {
					Athena.setStatus(0);
					setUserStatus(false);
				}
			}
		});


		// Add the mouseListener to the contact list
		userBox.addMouseListener(mouseListener);
		userBox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Places the area for the tabs
		imTabbedPane.setBounds(10, 10, 580, 537);
		lockIconLabel.setIcon(lockIcon);
		lockIconLabel.setVisible(true);
		lockIconLabel.setBounds(490, 400, 104, 150);
		logoIconLabel.setIcon(logoIconBig);
		logoIconLabel.setVisible(true);
		logoIconLabel.setBounds(200, 100, 305, 300);

		// Generate panel by adding appropriate components
		panel = new JPanel();
		panel.setLayout(null);
		panel.add(contactList);
		contactList.setVisible(true);
		panel.add(chatList);
		chatList.setVisible(false);
		panel.add(addContactLabel);
		panel.add(removeContactLabel);
		panel.add(homeListButton);
		panel.add(statusBox);
		panel.add(lockIconLabel);
		panel.add(logoIconLabel);
		panel.add(imTabbedPane);

		// Initialize window frame
		imContentFrame.setJMenuBar(menuBar);
		imContentFrame.setContentPane(panel);
		imContentFrame.setVisible(true);

	}

	/**
	 * Focuses the TextField of the current tab
	 */
	public void FocusCurrentTextField() {
		//Set default icon
		Icon closeIcon = new ImageIcon("images/close_button.png");
		CloseTabButton c = (CloseTabButton) imTabbedPane.getTabComponentAt(imTabbedPane.getSelectedIndex());
		JButton currentButton = (JButton) c.getComponent(1);
		currentButton.setIcon(closeIcon);

		//Set textfield focus
		JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
		Component[] currentTabComponents = currentTab.getComponents();
		Component textFieldToFocus = currentTabComponents[1];
		textFieldToFocus.requestFocusInWindow();
	}

	/**
	 * Get the current settings as an array
	 * @return An array of the settings
	 */
	public Object[] getCurrentSettingsArray() {
		return currentSettings;
	}

	/**
	 * Set the user's setting
	 * @param settingsArray The new settings to load
	 */
	public void setCurrentSettingsArray(Object[] settingsArray) {
		currentSettings = settingsArray;
	}

	/**
	 * Make a tab for a conversation
	 * @param user The user to make the tab for
	 * @param userCreated boolean flag
	 */
	public void makeTab(String user, boolean userCreated) {
		lockIconLabel.setVisible(false);
		logoIconLabel.setVisible(false);
		int prevIndex = 0;
		// Create a hash table mapping a user name to the JPanel in a tab
		tabPanels.put(user, new MapTextArea(user, enableSpellCheck, uniqueIDHash));
		// Make a temporary object for that JPanel
		MapTextArea temp = (MapTextArea) tabPanels.get(user);
		// Actually pull the JPanel out
		JPanel tempPanel = temp.getJPanel();
		// Create a tab with that JPanel on it and add tab to ID hash table
		if (imTabbedPane.getTabCount() > 0) {
			prevIndex = imTabbedPane.getSelectedIndex();
		}

		imTabbedPane.addTab(user, null, tempPanel, user + " Tab");
		// Add close button to tab
		new CloseTabButton(imTabbedPane, imTabbedPane.indexOfTab(user));
		//Add ESC Key listener
		if (enableESCToClose) {
			addESCKeyListener(imTabbedPane.indexOfTab(user));
		}
		//Add alert notification listener
		addAlertNotificationListener(imTabbedPane.indexOfTab(user));
		// Focus the new tab if first tab or if textarea is empty
		addTextFieldFocusListener(imTabbedPane.indexOfTab(user));
		if (userStatusFlag) {
			disableTextPane(imTabbedPane.indexOfTab(user));
		}

		JPanel currentTab = (JPanel) imTabbedPane.getComponentAt(imTabbedPane.indexOfTab(user));
		currentTab.setName("-1");
		if (debug >= 1) {
			System.out.println("Chat Name = " + currentTab.getName());
		}
		if (debug >= 1) {
			System.out.println("Chat Title = " + imTabbedPane.getTitleAt(imTabbedPane.indexOfTab(user)));
		}

		if (imTabbedPane.indexOfTab(user) == 0 || userCreated) {
			contactList.setVisible(true);
			chatList.setVisible(false);
			imTabbedPane.setSelectedIndex(imTabbedPane.indexOfTab(user));
			if (!(userStatusFlag)) {
				FocusCurrentTextField();
			}
		} else {
			Icon alertIcon = new ImageIcon("images/alert.png");
			CloseTabButton c = (CloseTabButton) imTabbedPane.getTabComponentAt(imTabbedPane.indexOfTab(user));
			JButton currentButton = (JButton) c.getComponent(1);
			currentButton.setIcon(alertIcon);
			imTabbedPane.setSelectedIndex(prevIndex);
			if (!(userStatusFlag)) {
				FocusCurrentTextField();
			}
		}
		//Garbage collect
		System.gc();
	}

	/**
	 * Make a chat tab
	 * @param chatName The name of the chat
	 * @param chatUID The UID of the chat
	 * @param userCreated Boolean flag
	 */
	public void makeChatTab(String chatName, String chatUID, boolean userCreated) {
		lockIconLabel.setVisible(false);
		logoIconLabel.setVisible(false);
		int prevIndex = 0;
		// Create a hash table mapping a user name to the JPanel in a tab
		tabPanels.put(chatName, new MapTextArea(chatName, enableSpellCheck, uniqueIDHash));
		// Make a temporary object for that JPanel
		MapTextArea temp = (MapTextArea) tabPanels.get(chatName);
		// Actually pull the JPanel out
		JPanel tempPanel = temp.getJPanel();
		// Create a tab with that JPanel on it and add tab to ID hash table
		if (imTabbedPane.getTabCount() > 0) {
			prevIndex = imTabbedPane.getSelectedIndex();
		}

		imTabbedPane.addTab(chatName, null, tempPanel, chatName + " Tab");
		// Add close button to tab
		new CloseTabButton(imTabbedPane, imTabbedPane.indexOfTab(chatName), chatUID);
		//Add ESC Key listener
		if (enableESCToClose) {
			addESCKeyListener(imTabbedPane.indexOfTab(chatName));
		}
		//Add alert notification listener
		addAlertNotificationListener(imTabbedPane.indexOfTab(chatName));
		// Focus the new tab if first tab or if textarea is empty
		addChatTextFieldFocusListener(imTabbedPane.indexOfTab(chatName));
		if (userStatusFlag) {
			disableTextPane(imTabbedPane.indexOfTab(chatName));
		}

		JPanel currentTab = (JPanel) imTabbedPane.getComponentAt(imTabbedPane.indexOfTab(chatName));
		currentTab.setName(String.valueOf(chatUID));
		if (debug >= 1) {
			System.out.println("Chat Name = " + currentTab.getName());
		}
		if (debug >= 1) {
			System.out.println("Chat Title = " + imTabbedPane.getTitleAt(imTabbedPane.indexOfTab(chatName)));
		}

		if (imTabbedPane.indexOfTab(chatName) == 0 || userCreated) {
			imTabbedPane.setSelectedIndex(imTabbedPane.indexOfTab(chatName));
			contactList.setVisible(false);
			chatList.setViewportView(inviteBox);
			chatList.setVisible(true);
			if (!(userStatusFlag)) {
				FocusCurrentTextField();
			}
		} else {
			Icon alertIcon = new ImageIcon("images/alert.png");
			CloseTabButton c = (CloseTabButton) imTabbedPane.getTabComponentAt(imTabbedPane.indexOfTab(chatName));
			JButton currentButton = (JButton) c.getComponent(1);
			currentButton.setIcon(alertIcon);
			imTabbedPane.setSelectedIndex(prevIndex);
			if (!(userStatusFlag)) {
				FocusCurrentTextField();
			}
		}
		//Garbage collect!
		System.gc();
	}

	/**
	 * Disable or enable textfield based on availability
	 * @param busy Is the user away?
	 */
	public void setUserStatus(boolean busy) {
		if (busy) {
			int tabCount = imTabbedPane.getTabCount();
			for (int x = 0; x < tabCount; x++) {
				disableTextPane(x);
			}
			userStatusFlag = true;
		} else {
			int tabCount = imTabbedPane.getTabCount();
			for (int x = 0; x < tabCount; x++) {
				enableTextPane(x);
			}
			userStatusFlag = false;
		}
	}

	/**
	 * Disable textfields in a specific tab
	 * @param index The tab index to disable
	 */
	public void disableTextPane(int index) {
		JPanel currentTab = (JPanel) imTabbedPane.getComponentAt(index);
		Component[] currentTabComponents = currentTab.getComponents();
		JTextPane textFieldToFocus = (JTextPane) currentTabComponents[1];
		textFieldToFocus.setEnabled(false);
		textFieldToFocus.setFont(new Font("Arial", Font.ITALIC, 14));
		textFieldToFocus.setBackground(Color.gray);
		textFieldToFocus.setForeground(Color.black);
		textFieldToFocus.setText("Change user status to [Available] to resume communication...");
	}

	/**
	 * Enable textfields in a specific tab
	 * @param index The tab index to enable
	 */
	public void enableTextPane(int index) {
		JPanel currentTab = (JPanel) imTabbedPane.getComponentAt(index);
		Component[] currentTabComponents = currentTab.getComponents();
		JTextPane textFieldToFocus = (JTextPane) currentTabComponents[1];
		textFieldToFocus.setEnabled(true);
		textFieldToFocus.setEditable(true);
		textFieldToFocus.setFont(new Font("Arial", Font.PLAIN, 14));
		textFieldToFocus.setBackground(Color.white);
		textFieldToFocus.setForeground(Color.black);
		textFieldToFocus.setText("");
	}

	/**
	 * Enable or disable the icon in the notification area
	 * @param activated Enabled or disabled
	 * @throws AWTException
	 */
	public void setSystemTrayIcon(boolean activated) throws AWTException {
		SystemTray tray = SystemTray.getSystemTray();
		TrayIcon[] trayArray = tray.getTrayIcons();
		int tlength = trayArray.length;
		if (activated) {
			if (tlength == 0) {
				Image trayImage = Toolkit.getDefaultToolkit().getImage("images/sysTray.gif");
				ActionListener exitListener = new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						if (debug == 1) {
							System.out.println("Exiting...");
						}
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
		} else {
			for (int x = 0; x < tlength; x++) {
				tray.remove(trayArray[x]);
			}
		}
	}

	/**
	 * Enable or disable the ESC-key close for tabs
	 * @param activated Is the setting enabled?
	 */
	public void closeTabWithESC(boolean activated) {
		int tabCount = imTabbedPane.getTabCount();
		enableESCToClose = activated;
		if (activated) {
			// Assign key listener to all existing text fields
			for (int x = 0; x < tabCount; x++) {
				addESCKeyListener(x);
			}
		} else {
			for (int x = 0; x < tabCount; x++) {
				removeESCKeyListener(x);
			}
		}
		//Garbage collect!
		System.gc();
	}

	/**
	 * Enable or disable the spellcheck
	 * @param activated Is the setting enable?
	 */
	public void setSpellCheck(boolean activated) {
		// Retrieve necessary tab and component data
		int tabCount = imTabbedPane.getTabCount();
		JPanel currentTab;
		Component[] currentTabComponents;
		JTextPane currentTextField;
		if (activated) {
			// Register all current text fields for spell check
			for (int x = 0; x < tabCount; x++) {
				imTabbedPane.setSelectedIndex(x);
				currentTab = (JPanel) imTabbedPane.getSelectedComponent();
				currentTabComponents = currentTab.getComponents();
				currentTextField = (JTextPane) currentTabComponents[1];
				SpellChecker.register(currentTextField, true, true, true);
			}
			// Enable future spell check registration
			enableSpellCheck = true;
		} else {
			// Unregister all current text fields with spell check
			for (int x = 0; x < tabCount; x++) {
				imTabbedPane.setSelectedIndex(x);
				currentTab = (JPanel) imTabbedPane.getSelectedComponent();
				currentTabComponents = currentTab.getComponents();
				currentTextField = (JTextPane) currentTabComponents[1];
				SpellChecker.unregister(currentTextField);
			}
			// Disable future spell check registration
			enableSpellCheck = false;
		}
	}

	/**
	 * Add an alert notifier to the specified tab index
	 * @param index Tab index to add the listener on
	 */
	private void addAlertNotificationListener(int index) {
		imTabbedPane.setSelectedIndex(index);
		JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
		Component[] currentTabComponents = currentTab.getComponents();
		JScrollPane currentScrollPane = (JScrollPane) currentTabComponents[0];
		JEditorPane currentEditorPane = (JEditorPane) currentScrollPane.getViewport().getComponent(0);
		if (debug >= 1) {
			System.out.println("Alert listener on: " + currentEditorPane.toString());
		}
		currentEditorPane.getDocument().addDocumentListener(new DocumentListener() {

			public void insertUpdate(DocumentEvent e) {
				JPanel currentTab = uniqueIDHash.get(e.getDocument());
				int currentTabIndex = imTabbedPane.indexOfComponent(currentTab);
				if (currentTab != imTabbedPane.getSelectedComponent() && currentTabIndex != -1) {
					Icon alertIcon = new ImageIcon("images/alert.png");
					CloseTabButton c = (CloseTabButton) imTabbedPane.getTabComponentAt(currentTabIndex);
					JButton currentButton = (JButton) c.getComponent(1);
					currentButton.setIcon(alertIcon);
				}
			}

			public void changedUpdate(DocumentEvent arg0) {
			}

			public void removeUpdate(DocumentEvent arg0) {
			}
		});
	}

	/**
	 * Add an ESC key listener to the specified tab
	 * @param index The tab to add the listener on
	 */
	private void addESCKeyListener(int index) {
		imTabbedPane.setSelectedIndex(index);
		JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
		Component[] currentTabComponents = currentTab.getComponents();
		JTextPane currentTextField = (JTextPane) currentTabComponents[1];
		currentTextField.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				int zz = 0;
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
					int tempIndex = imTabbedPane.getSelectedIndex();
					String userToRemove = imTabbedPane.getTitleAt(tempIndex);
					imTabbedPane.remove(currentTab);
					Component[] currentTabComponents = currentTab.getComponents();
					JScrollPane currentScrollPane = (JScrollPane) currentTabComponents[0];
					JEditorPane currentTextPane = (JEditorPane) currentScrollPane.getViewport().getComponent(0);
					uniqueIDHash.remove(currentTextPane.getDocument());
					tabPanels.remove(userToRemove);

					if (tempIndex > 0) {
						imTabbedPane.setSelectedIndex(tempIndex - 1);
						FocusCurrentTextField();
					} else {
						if (imTabbedPane.getTabCount() > 1) {
							imTabbedPane.setSelectedIndex(tempIndex);
							FocusCurrentTextField();
						} else if (imTabbedPane.getTabCount() > 0) {
							imTabbedPane.setSelectedIndex(0);
							FocusCurrentTextField();
						}
					}
					if (imTabbedPane.getTabCount() == 0) {
						CommunicationInterface.lockIconLabel.setVisible(true);
						CommunicationInterface.logoIconLabel.setVisible(true);
					}
				}
			}

			public void keyTyped(KeyEvent e) {
			}
		});
	}

	/**
	 * Remove the ESC key listener from a tab
	 * @param index Tab to remove it from
	 */
	private void removeESCKeyListener(int index) {
		imTabbedPane.setSelectedIndex(index);
		JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
		Component[] currentTabComponents = currentTab.getComponents();
		JTextPane currentTextField = (JTextPane) currentTabComponents[1];
		KeyListener[] fieldListeners = currentTextField.getKeyListeners();
		if (fieldListeners != null) {
			currentTextField.removeKeyListener(fieldListeners[0]);
		}
	}

	/**
	 * Add a focus listener to the textfield of a tab
	 * @param index The tab index to act on
	 */
	private void addTextFieldFocusListener(int index) {
		JPanel currentTab = (JPanel) imTabbedPane.getComponentAt(index);
		Component[] currentTabComponents = currentTab.getComponents();
		JTextPane currentTextField = (JTextPane) currentTabComponents[1];
		currentTextField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				Icon closeIcon = new ImageIcon("images/close_button.png");
				CloseTabButton c = (CloseTabButton) imTabbedPane.getTabComponentAt(imTabbedPane.getSelectedIndex());
				JButton currentButton = (JButton) c.getComponent(1);
				currentButton.setIcon(closeIcon);
			}

			public void focusLost(FocusEvent e) {
				// Do nothing
			}
		});
	}

	/**
	 * Add the focuslistener to a chat's textfield
	 * @param index The index of the chat tab
	 */
	private void addChatTextFieldFocusListener(int index) {
		JPanel currentTab = (JPanel) imTabbedPane.getComponentAt(index);
		Component[] currentTabComponents = currentTab.getComponents();
		JTextPane currentTextField = (JTextPane) currentTabComponents[1];
		currentTextField.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
				contactList.setVisible(false);
				chatList.setVisible(true);
				Icon closeIcon = new ImageIcon("images/close_button.png");
				CloseTabButton c = (CloseTabButton) imTabbedPane.getTabComponentAt(imTabbedPane.getSelectedIndex());
				JButton currentButton = (JButton) c.getComponent(1);
				currentButton.setIcon(closeIcon);
			}

			public void focusLost(FocusEvent e) {
				contactList.setVisible(true);
				chatList.setVisible(false);
			}
		});
	}

	/**
	 * The Group Chat creation and invitation window
	 */
	public void createChatWindow() {
		chatWindow = new JFrame("Group Chat Initiation");
		chatWindow.setSize(400, 480);

		JPanel chatPanel = new JPanel();
		chatPanel.setBounds(10, 10, 400, 480);
		chatPanel.setLayout(null);

		contactBox = new JList(contactListModel);
		JScrollPane contactList = new JScrollPane(contactBox);
		contactList.setBounds(30, 15, 150, 285);
		TitledBorder chatBorder = BorderFactory.createTitledBorder(contactListBorder, "Available Contacts", TitledBorder.CENTER, TitledBorder.ABOVE_TOP);
		contactList.setBorder(chatBorder);

		inviteListModel.removeAllElements();
		JScrollPane inviteList = new JScrollPane(inviteBox);
		inviteList.setBounds(200, 15, 150, 285);
		TitledBorder inviteBorder = BorderFactory.createTitledBorder(contactListBorder, "Contacts To Invite", TitledBorder.CENTER, TitledBorder.ABOVE_TOP);
		inviteList.setBorder(inviteBorder);

		JButton inviteButton = new JButton("Invite");
		inviteButton.setForeground(Color.black);
		inviteButton.setBackground(new Color(218, 165, 32));
		inviteButton.setBounds(30, 310, 150, 30);
		inviteButton.setBorder(buttonBorder);
		inviteButton.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent mouseEvent) {
				try {
					JList theContactList = (JList) contactBox;

					// Get selected item
					int index = theContactList.getSelectedIndex();

					if (index >= 0) {
						// Add selected item to invite list
						if (inviteListModel.contains(contactListModel.getElementAt(index))) {
							JOptionPane.showMessageDialog(null, contactListModel.getElementAt(index).toString()
									+ " is already invited.", "Attention!", JOptionPane.ERROR_MESSAGE);
						} else if (contactListModel.getElementAt(index).equals(Athena.username)) {
							JOptionPane.showMessageDialog(null, "As chat creator, you are already\n included in the chat roster.",
									"Attention!", JOptionPane.ERROR_MESSAGE);
						} else {
							inviteListModel.addElement(contactListModel.getElementAt(index));
						}
					} //If there wasn't something selected, bring up a new window that will let them choose who they want to remove
					else {
						JOptionPane.showMessageDialog(null, "No contact selected for invite.", "Attention!", JOptionPane.ERROR_MESSAGE);
					}
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		});

		JButton removeButton = new JButton("Remove");
		removeButton.setForeground(Color.white);
		removeButton.setBackground(new Color(0, 0, 120));
		removeButton.setBounds(200, 310, 150, 30);
		removeButton.setBorder(buttonBorder);
		removeButton.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent mouseEvent) {
				try {
					JList theInviteList = (JList) inviteBox;

					// Get selected item
					int index = theInviteList.getSelectedIndex();

					if (index >= 0) {
						// Remove selected item
						inviteListModel.removeElementAt(index);
					} //If there wasn't something selected, bring up a new window that will let them choose who they want to remove
					else {
						JOptionPane.showMessageDialog(null, "No contact selected for removal.", "Attention!", JOptionPane.ERROR_MESSAGE);
					}
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		});

		JLabel chatNameLabel = new JLabel("Chat Room Name:");
		chatNameLabel.setBounds(30, 360, 105, 20);

		chatNameField.setBounds(135, 360, 215, 20);
		chatNameField.setText("");
		chatNameField.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
				if (chatNameField.getText().length() == 30) {
					e.consume();
				}
			}
		});

		JButton createChatButton = new JButton("Create Chat");
		createChatButton.setForeground(Color.black);
		createChatButton.setBackground(new Color(218, 165, 32));
		createChatButton.setBounds(30, 400, 150, 30);
		createChatButton.setBorder(buttonBorder);
		createChatButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				if (inviteListModel.isEmpty()) {
					JOptionPane.showMessageDialog(null, "No contacts selected for group chat.", "Attention!", JOptionPane.ERROR_MESSAGE);
				} else if (chatNameField.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(null, "Please enter a chat room name.", "Attention!", JOptionPane.ERROR_MESSAGE);
				} else {
					//Run the createChat method in Athena, returns the chatUID
					String chatUID = Athena.createChat(chatNameField.getText());
					//Getting the list
					String[] inviteUsers = new String[inviteListModel.size()];
					for (int x = 0; x < inviteListModel.size(); x++) {
						inviteUsers[x] = (String) inviteListModel.getElementAt(x);
					}
					//Invite the other users
					try {
						Athena.inviteUsers(inviteUsers, chatUID, chatNameField.getText());
					} catch (IOException e) {

						e.printStackTrace();
					}
					makeChatTab(chatNameField.getText(), chatUID, true);
					TitledBorder newChatListBorder = BorderFactory.createTitledBorder(chatListBorder, imTabbedPane.getTitleAt(imTabbedPane.getSelectedIndex()) + " Chat List", TitledBorder.CENTER,
							TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.PLAIN, 14), new Color(0, 0, 120));
					chatList.setBorder(newChatListBorder);
					inviteListModel.removeAllElements();
					inviteListModel.addElement(Athena.username);
					chatWindow.dispose();
				}
			}
		});

		JButton cancelChatButton = new JButton("Cancel");
		cancelChatButton.setForeground(Color.white);
		cancelChatButton.setBackground(new Color(0, 0, 120));
		cancelChatButton.setBounds(200, 400, 150, 30);
		cancelChatButton.setBorder(buttonBorder);
		cancelChatButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				chatWindow.dispose();
			}
		});

		chatPanel.add(contactList);
		chatPanel.add(inviteList);
		chatPanel.add(chatNameLabel);
		chatPanel.add(inviteButton);
		chatPanel.add(removeButton);
		chatPanel.add(createChatButton);
		chatPanel.add(cancelChatButton);
		chatPanel.add(chatNameField);

		chatWindow.add(chatPanel);
		chatWindow.setVisible(true);
	}

	/**
	 * Map a buddy to their current status
	 * @param username Buddy's username
	 * @param status Their current status
	 */
	public void mapUserStatus(String username, int status) {
		if (debug == 1) {
			System.out.println("Username: " + username + "\nStatus: " + status);
		}
		userStatus.put(username, status);
	}

	/**
	 * Import preferences from a file
	 * @return The object[] of the current settings
	 */
	private Object[] loadSavedPreferences() {
		if (debug == 1) {
			System.out.println("Importing preferences");
		}
		Object[] settingsArray = new Object[11];
		int arrayCount = 0;
		String line = null;
		String temp = null;
		try {

			File newPrefFile = new File("users/" + Athena.username + "/athena.conf");
			if (!(newPrefFile.exists())) {
				boolean success = new File("users/" + Athena.username + "/").mkdirs();
				if (success) {
					if (debug == 1) {
						System.out.println("File Not Found! Copying...");
					}
					File oldFile = new File("users/Aegis/athena.conf");
					FileChannel inChannel = new FileInputStream(oldFile).getChannel();
					FileChannel outChannel = new FileOutputStream(newPrefFile).getChannel();
					try {
						inChannel.transferTo(0, inChannel.size(), outChannel);
					} catch (IOException e) {
						throw e;
					} finally {
						if (inChannel != null) {
							inChannel.close();
						}
						if (outChannel != null) {
							outChannel.close();
						}
					}
				} else {
					if (debug == 1) {
						System.out.println("File Not Found! Copying...");
					}
					File oldFile = new File("users/Aegis/athena.conf");
					FileChannel inChannel = new FileInputStream(oldFile).getChannel();
					FileChannel outChannel = new FileOutputStream(newPrefFile).getChannel();
					try {
						inChannel.transferTo(0, inChannel.size(), outChannel);
					} catch (IOException e) {
						throw e;
					} finally {
						if (inChannel != null) {
							inChannel.close();
						}
						if (outChannel != null) {
							outChannel.close();
						}
					}
				}
			}
			BufferedReader inPref = new BufferedReader(new FileReader("./users/" + Athena.username + "/athena.conf"));
			while ((line = inPref.readLine()) != null) {
				if (line.equals("[GENERAL]")) {
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
				if (line.equals("[NOTIFICATIONS]")) {
					//Get notification settings
					//Get enableSounds (boolean)
					temp = inPref.readLine().substring(13);
					settingsArray[arrayCount] = temp;
					arrayCount++;
				}
				if (line.equals("[ENCRYPTION]")) {
					//Get encryption settings
					//Get encryptionType (integer)
					inPref.readLine();
					inPref.readLine();
					temp = inPref.readLine().substring(15);
					settingsArray[arrayCount] = temp;
					arrayCount++;
				}
				if (line.equals("[FORMATTING]")) {
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
					//Get fontSize (int)
					temp = inPref.readLine().substring(9);
					settingsArray[arrayCount] = temp;
					arrayCount++;
				}
				if (line.equals("[THEME]")) {
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

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return settingsArray;
	}

	/**
	 * I think this helps us render current user statuses on the buddylist
	 */
	class MyCellRenderer extends JLabel implements ListCellRenderer {

		public Component getListCellRendererComponent(
				JList list,
				Object value, // value to display
				int index, // cell index
				boolean isSelected, // is the cell selected
				boolean cellHasFocus) // the list and the cell have the focus
		{
			String s = value.toString();
			setText(s);
			setIcon(new ImageIcon("images/available.png"));
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}

	/**
	 * The close buttons on every tab. Also holds chatUID (-1 for an IM)
	 */
	class CloseTabButton extends JPanel implements ActionListener, MouseListener {
		//private static final long serialVersionUID = -6032110177913133517L;
		private JTabbedPane pane;
		public JButton btClose;
		public String chatUID = "-1";
		Icon closeIcon = new ImageIcon("images/close_button.png");
		Icon alertIcon = new ImageIcon("images/alert.png");
		int myIndex;
		Icon originalIcon;

		public CloseTabButton(JTabbedPane pane, int index) {
			this.pane = pane;
			myIndex = index;
			setOpaque(false);
			add(new JLabel(
					pane.getTitleAt(index),
					pane.getIconAt(index),
					JLabel.LEFT));
			btClose = new JButton(closeIcon);
			btClose.setPreferredSize(new Dimension(
					closeIcon.getIconWidth(), closeIcon.getIconHeight()));
			add(btClose);
			btClose.addActionListener(this);
			btClose.setToolTipText("Close Tab");
			pane.setTabComponentAt(index, this);
			btClose.setBorder(null);
			btClose.addMouseListener(this);
		}

		public CloseTabButton(JTabbedPane pane, int index, String currentChatUID) {
			this.pane = pane;
			myIndex = index;
			setOpaque(false);
			add(new JLabel(
					pane.getTitleAt(index),
					pane.getIconAt(index),
					JLabel.LEFT));
			btClose = new JButton(closeIcon);
			btClose.setPreferredSize(new Dimension(
					closeIcon.getIconWidth(), closeIcon.getIconHeight()));
			add(btClose);
			btClose.addActionListener(this);
			btClose.setToolTipText("Close Tab");
			pane.setTabComponentAt(index, this);
			btClose.setBorder(null);
			btClose.addMouseListener(this);
			chatUID = currentChatUID;
		}

		public void mouseEntered(MouseEvent evt) {
		}

		public void mouseExited(MouseEvent evt) {
		}

		public void actionPerformed(ActionEvent e) {
			int i = pane.indexOfTabComponent(this);
			if (i != -1) {
				String userToRemove = pane.getTitleAt(i);
				pane.remove(i);
				tabPanels.remove(userToRemove);
				if (debug >= 1) {
					System.out.println("Removed Tab for user: " + userToRemove);
				}
				if (imTabbedPane.getTabCount() > 0) {
					JPanel currentTab = (JPanel) imTabbedPane.getSelectedComponent();
					Component[] currentTabComponents = currentTab.getComponents();
					JScrollPane currentScrollPane = (JScrollPane) currentTabComponents[0];
					JEditorPane currentTextPane = (JEditorPane) currentScrollPane.getViewport().getComponent(0);
					uniqueIDHash.remove(currentTextPane.getDocument());
					//Retrieve the mapTextArea, then see if the tab is a chat tab
					if (debug >= 1) {
						System.out.println("ChatUID: " + chatUID);
					}
					if (!(chatUID.equals("-1"))) {
						if (debug >= 1) {
							System.out.println("Leaving chat!");
						}
						Athena.leaveChat(chatUID);
					}

				}
				if (imTabbedPane.getTabCount() == 0) {
					CommunicationInterface.lockIconLabel.setVisible(true);
					CommunicationInterface.logoIconLabel.setVisible(true);
					if (debug >= 1) {
						System.out.println("ChatUID: " + chatUID);
					}
					if (!(chatUID.equals("-1"))) {
						System.out.println("Leaving chat!");
						Athena.leaveChat(chatUID);
					}
				}
				System.gc();
			}
		}

		@Override
		public void mouseClicked(MouseEvent arg0) {
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
		}
	}

	/**
	 * Get the currently selected fontface
	 * @return Font face name
	 */
	public String getLoadedFontFace() {
		return fontFace;
	}

	/**
	 * Bold currently enabled/disabled
	 * @return True if enabled
	 */
	public boolean getLoadedBold() {
		return fontBold;
	}

	/**
	 * Italics currently enabled/disabled
	 * @return True if enabled
	 */
	public boolean getLoadedItalic() {
		return fontItalic;
	}

	/**
	 * Underline currently enabled/disabled
	 * @return True if enabled
	 */
	public boolean getLoadedUnderline() {
		return fontUnderline;
	}

	/**
	 * Get currently selected font size
	 * @return font size
	 */
	public int getLoadedFontSize() {
		return fontSize;
	}

	/**
	 * Load some new font settings
	 * @param newFontFace Name of new font face
	 * @param newBold Is bold enabled?
	 * @param newItalic Are italics enabled?
	 * @param newUnderline Is underline enabled
	 * @param newSize New font size
	 */
	public void setNewFontToLoad(String newFontFace, boolean newBold, boolean newItalic, boolean newUnderline, int newSize) {
		fontFace = newFontFace;
		fontBold = newBold;
		fontItalic = newItalic;
		fontUnderline = newUnderline;
		fontSize = newSize;
	}
	// End of class ClientApplet
}

/**
 * This class holds all of the JComponents and acts as an interface to each conversation's tab
 * @author OlympuSoft
 */
class MapTextArea extends JFrame {

	private static final long serialVersionUID = 2557115166519071868L;
	//private int chatUID = -1;
	//private boolean isChat = false;
	public JLabel encType = new JLabel("Encryption Type: RSA - DirectProtect Inactive");
	
	// The user name associated with the tab
	private String username = null;

	// All of the JComponents in the tab
	private JPanel myJPanel;
	private JEditorPane myJEP;
	private JTextPane myTP;
	//private JTextArea myTA;
	//private JTextField myTF;
	//private boolean isBold, isItalic, isUnderline;
	//private int fontSize;
	private MutableAttributeSet keyWord = new SimpleAttributeSet();
	private MutableAttributeSet miniKeyWord = new SimpleAttributeSet();
	
	// The index of the tab this lives in
	int tabIndex = -1;

	/**
	 * The JPanel and components in every tab
	 * @param user The user associated with the tab
	 * @param spellCheckFlag Is spellcheck enabled in this tab?
	 * @param uniqueIDHash The UID of this tab
	 */
	MapTextArea(String user, boolean spellCheckFlag, Hashtable<Document, JPanel> uniqueIDHash) {

		try {
			//Register the dictionaries for the spell checker
			SpellChecker.registerDictionaries(new URL("file", null, ""), "en,de", "en");
		} catch (MalformedURLException e) {

			e.printStackTrace();
		}

		//Create the JPanel and put all of the components in it
		myJPanel = new JPanel();
		myJPanel.setLayout(null);

		//Create the styled text area and the scroll pane around it
		StyledEditorKit kit = new StyledEditorKit();
		myJEP = new JEditorPane();
		myJEP.setEditable(false);
		myJEP.setEditorKit(kit);

		myJEP.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {

				myJEP.copy();
				Athena.clientResource.FocusCurrentTextField();
			}
		});


		JScrollPane mySP = new JScrollPane(myJEP);
		mySP.setBounds(10, 10, 559, 410); //9,0
		mySP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		mySP.setOpaque(true);
		myJPanel.add(mySP);
		
		//Create the text pane
		StyledEditorKit miniKit = new StyledEditorKit();
		myTP = new JTextPane();
		myTP.setBounds(10, 440, 560, 50);
		myTP.setEditorKit(miniKit);
		myTP.setBorder(BorderFactory.createLoweredBevelBorder());

		myJPanel.add(myTP);

		uniqueIDHash.put(myJEP.getDocument(), myJPanel);

		if(Athena.sessionKeys.containsKey(user)){
			encType.setText("Encryption Type: AES - DirectProtect Active");
		}
		encType.setBounds(11,418,300,20);
		encType.setVisible(true);
		myJPanel.add(encType);

		//Register the spell checker in the text field
		if (spellCheckFlag) {
			SpellChecker.register(myTP, true, true, true);
		}

		username = user;

		myTP.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && (!(myTP.getText().equals("")))) {
					try {
						try {
							Athena.processMessage(myTP.getText());
						} catch (IOException ex) {
							Logger.getLogger(MapTextArea.class.getName()).log(Level.SEVERE, null, ex);
						}
						myTP.getDocument().remove(0, myTP.getText().length());
						e.consume();
					} catch (BadLocationException e1) {

						e1.printStackTrace();
					}
				}
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}
		});

		//Set default font settings to new text pane
		if (!(Athena.clientResource.settingsLoaded)) {
			if (Athena.debug >= 1) {
				System.out.println("Settings loaded from file, settingsLoaded = " + Athena.clientResource.settingsLoaded);
			}
			setLoadedFont();
			StyledDocument doc = myTP.getStyledDocument();
			if (doc.getLength() > 0) {
				doc.setCharacterAttributes(0, doc.getLength() + 1, miniKeyWord, false);
			} else {
				doc.setCharacterAttributes(0, doc.getLength() + 1, miniKeyWord, true);
			}
		} else {
			if (Athena.debug >= 1) {
				System.out.println("Settings already changed, settingsLoaded = " + Athena.clientResource.settingsLoaded);
			}
			//Dont set default config font, get current font
			setLoadedFont();
			StyledDocument doc = myTP.getStyledDocument();
			if (doc.getLength() > 0) {
				doc.setCharacterAttributes(0, doc.getLength() + 1, miniKeyWord, false);
			} else {
				doc.setCharacterAttributes(0, doc.getLength() + 1, miniKeyWord, true);
			}
		}
	}

	/**
	 * Set the username associated with the tab
	 * @param user The username
	 */
	public void setUserName(String user) {
		username = user;
	}

	/**
	 * Get the username associated with the tab
	 * @return The username
	 */
	public String getUserName() {
		return username;
	}

	/**
	 * Set the index of this tab on the tabbedpane
	 * @param index The index of the tab
	 */
	public void setTabIndex(int index) {
		tabIndex = index;
	}

	/**
	 * Get the index of this tab on the tabbedpane
	 * @return The index
	 */
	public int getTabIndex() {
		return tabIndex;
	}

	/**
	 * Get the JPanel for this tab
	 * @return The JPanel
	 */
	public JPanel getJPanel() {
		return myJPanel;
	}

	/**
	 * Set a new font for this tab
	 * @param fontFace The font face
	 * @param isBold Is it bold
	 * @param isItalic Is it italic
	 * @param isULine Is it underlined
	 * @param ftSize Font size
	 */
	public void setTextFont(String fontFace, boolean isBold, boolean isItalic, boolean isULine, int ftSize) {
		miniKeyWord = myTP.getInputAttributes();
		myTP.setFont(new Font(fontFace, Font.PLAIN, ftSize));
		StyleConstants.setBold(miniKeyWord, isBold);
		StyleConstants.setItalic(miniKeyWord, isItalic);
		StyleConstants.setUnderline(miniKeyWord, isULine);
		StyleConstants.setFontSize(miniKeyWord, ftSize);
		StyleConstants.setFontFamily(miniKeyWord, Athena.clientResource.fontFamilyTable.get(fontFace));
		StyledDocument doc = myTP.getStyledDocument();
		if (doc.getLength() > 0) {
			doc.setCharacterAttributes(0, doc.getLength() + 1, miniKeyWord, false);
		} else {
			doc.setCharacterAttributes(0, doc.getLength() + 1, miniKeyWord, true);
		}
	}

	/**
	 * Set the loaded font
	 */
	public void setLoadedFont() {
		myTP.setFont(new Font(Athena.clientResource.getLoadedFontFace(), Font.PLAIN, Athena.clientResource.getLoadedFontSize()));
		StyleConstants.setBold(miniKeyWord, Athena.clientResource.getLoadedBold());
		StyleConstants.setItalic(miniKeyWord, Athena.clientResource.getLoadedItalic());
		StyleConstants.setUnderline(miniKeyWord, Athena.clientResource.getLoadedUnderline());
		StyleConstants.setForeground(miniKeyWord, Color.black);
		StyleConstants.setBackground(miniKeyWord, Color.white);
		StyleConstants.setFontSize(miniKeyWord, Athena.clientResource.getLoadedFontSize());
		StyleConstants.setFontFamily(miniKeyWord, Athena.clientResource.fontFamilyTable.get(Athena.clientResource.getLoadedFontFace()));
	}

	/**
	 * Change the font style
	 * @param isBold bold
	 * @param isItalic italic
	 * @param isULine underline
	 */
	public void setTextFont(boolean isBold, boolean isItalic, boolean isULine) {
		StyleConstants.setBold(miniKeyWord, isBold);
		StyleConstants.setItalic(miniKeyWord, isItalic);
		StyleConstants.setUnderline(miniKeyWord, isULine);
	}

	/**
	 * Set the color of the font
	 * @param color Color to change to
	 * @return the MutableAttributeSet of the font
	 */
	public MutableAttributeSet getSetHeaderFont(Color color) {
		StyleConstants.setBold(keyWord, true);
		StyleConstants.setItalic(keyWord, false);
		StyleConstants.setUnderline(keyWord, false);
		StyleConstants.setForeground(keyWord, color);
		StyleConstants.setBackground(keyWord, Color.white);
		StyleConstants.setFontSize(keyWord, 14);
		StyleConstants.setFontFamily(keyWord, Athena.clientResource.fontFamilyTable.get(Athena.clientResource.getLoadedFontFace()));
		return keyWord;
	}

	/**
	 * Set the font
	 * @param currentAttr MAS of the font
	 */
	public void setTextFont(MutableAttributeSet currentAttr) {
		miniKeyWord = currentAttr;
	}

	/**
	 * Get the current font
	 * @return MAS of the current font
	 */
	public MutableAttributeSet getTextFont() {
		return miniKeyWord;
	}

	/**
	 * Set the color of the text
	 * @param color Color to change to
	 */
	public void setTextColor(Color color) {
		StyleConstants.setForeground(keyWord, color);
		StyleConstants.setBold(keyWord, false);
		StyleConstants.setFontSize(keyWord, 12);
		StyleConstants.setFontFamily(keyWord, "Times");
	}

	/**
	 * Write a string to the editorpane
	 * @param message Message to print
	 * @param attributes Which font to print it in
	 * @throws BadLocationException
	 */
	public void writeToTextArea(String message, MutableAttributeSet attributes) throws BadLocationException {
		myJEP.getDocument().insertString(myJEP.getDocument().getLength(), message, attributes);
	}

	/**
	 * Move the caret to the end of the editorpane
	 */
	public void moveToEnd() {
		myJEP.setCaretPosition(myJEP.getDocument().getLength());
	}

	/**
	 * Clear the text input area
	 */
	public void clearTextField() {
		myTP.setText("");
	}
}