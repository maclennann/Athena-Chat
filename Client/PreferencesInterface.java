/* Athena/Aegis Encrypted Chat Platform
 * PreferencesInterface.java: Allows user to choose preferences and save them to a file
 *
 * Copyright (C) 2010  OlympuSoft
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.UIManager;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.SwingUtilities;

/**
 * The preferences window
 * @author OlympuSoft
 */
public class PreferencesInterface extends JPanel {

	private static final long serialVersionUID = 5472264414606126641L;

	//Load preference variables from file into array
	private Object[] settingsArray = Athena.clientResource.getCurrentSettingsArray();
	private boolean allowSystemTray = Boolean.parseBoolean(settingsArray[0].toString());
	private boolean allowESCTab = Boolean.parseBoolean(settingsArray[1].toString());
	private boolean enableSpellCheck = Boolean.parseBoolean(settingsArray[2].toString());
        private String downloadLocation;
        private int debugLog = Integer.parseInt(settingsArray[4].toString());
	private boolean enableSounds = Boolean.parseBoolean(settingsArray[5].toString());
	private String setFontFace = settingsArray[6].toString();
	private boolean enableBold = Boolean.parseBoolean(settingsArray[7].toString());
	private boolean enableItalic = Boolean.parseBoolean(settingsArray[8].toString());
	private boolean enableUnderline = Boolean.parseBoolean(settingsArray[9].toString());
	private int setFontSize = Integer.parseInt(settingsArray[10].toString());

	//Define components
	private GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	//String[] allFontFamilies = ge.getAvailableFontFamilyNames();
	private Font[] allFonts = ge.getAllFonts();
	private String[] themeList = {"javax.swing.plaf.metal.MetalLookAndFeel", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
		"com.sun.java.swing.plaf.gtk.GTKLookAndFeel", "com.sun.java.swing.plaf.mac.MacLookAndFeel", "com.sun.java.swing.plaf.motif.MotifLookAndFeel",
		"com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel"};
	private JFrame preferences;
	private JPanel contentPane = new JPanel();
	private JPanel generalPanel, notificationsPanel, contactPrefPanel, formattingPanel, themePanel;
	private JPanel prefLabelPaneLeft = new JPanel();
	private JPanel prefLabelPaneRight = new JPanel();
	private JButton apply = new JButton("Apply");
	private JButton cancel = new JButton("Close");
	private Border blackline, whiteline;
	private Color originalColor;
	private TitledBorder generalTitledBorder, notificationsTitledBorder, contactPrefTitledBorder, formattingTitledBorder, themeTitleBorder;

	//Define components for the General Menu Panel
	private JTextField downDirTextField = new JTextField(downloadLocation);
	private JButton downDirLaunchButton = new JButton ("Browse...");
	private JLabel downDirJLabel = new JLabel("Download Directory:");
	private JButton generalLabel = new JButton("System", new ImageIcon("images/generalPref.png"));
	private JCheckBox systemTrayCheckBox = new JCheckBox("Show Athena in System Tray", allowSystemTray);
	private JCheckBox allowESCCheckBox = new JCheckBox("Allow ESC Key to Close a Tab", allowESCTab);
	private JCheckBox enableSpellCheckCheckBox = new JCheckBox("Enable Spell Check", enableSpellCheck);
	private JFileChooser fc = new JFileChooser();
	private JLabel logLevelLabel = new JLabel("Set Debug Log Level: ");
	private String[] logLevels = new String[]{"0","1","2"};
	private JComboBox logLevelComboBox;
	private boolean systemTrayVal;
	private boolean allowESCVal;
	private boolean enableSCVal;
	private boolean systemTrayFlag = false;
	private boolean allowESCFlag = false;
	private boolean enableSpellCheckFlag = false;
        private boolean downloadLocationFlag = false;
        private boolean debugLogFlag = false;
        private int debugLogVal = debugLog;
        private String downloadLocationVal;

	//Define components for the Notifications Menu Panel
	private JButton notificationsLabel = new JButton("Notifications", new ImageIcon("images/notificationsPref.png"));
	private JCheckBox enableSoundsCheckBox = new JCheckBox("Enable Sounds", enableSounds);
	private boolean enableSoundsVal;
	private boolean enableSoundsFlag = false;

	//Define components for the Contact List Menu Panel
	private JButton contactPrefLabel = new JButton("Contact List", new ImageIcon("images/contactPref.png"));
	private JLabel contactAliasJLabel = new JLabel("Contact Alias Adjustment");
        private JLabel aliasDropdownLabel = new JLabel("Select A Contact:");
        private JComboBox contactAliasComboBox;
        private JLabel updateAliasLabel = new JLabel("Current Alias:");
        private JLabel newAliasLabel = new JLabel("New Alias:");
        private JLabel currentAliasLabel = new JLabel("N/A");
        private JTextField updateAliasTextField = new JTextField();
        private JButton updateAliasButton = new JButton("Set Alias");

	//Define components for the Formatting Menu Panel
	private JButton formattingLabel = new JButton("Formatting", new ImageIcon("images/fontPref.png"));
	private JLabel selectFontLabel = new JLabel("Font Type:");
	private JLabel fontSizeLabel = new JLabel("Font Size:");
	private JLabel generalFontLabel = new JLabel("Font Style:");
	private JComboBox selectFontComboBox, fontSizeComboBox;
	private JCheckBox setBoldCheckBox = new JCheckBox("Bold", enableBold);
	private JCheckBox setItalicsCheckBox = new JCheckBox("Italics", enableItalic);
	private JCheckBox setUnderlineCheckBox = new JCheckBox("Underlined", enableUnderline);
	private boolean setFontFaceFlag = false;
	private boolean setBoldFlag = false;
	private boolean setItalicsFlag = false;
	private boolean setUnderlineFlag = false;
	private boolean setSizeFlag = false;
	private String setFontFaceVal = setFontFace;
	private boolean setBoldVal = enableBold;
	private boolean setItalicsVal = enableItalic;
	private boolean setUnderlineVal = enableUnderline;
	private int setSizeVal = setFontSize;

	//Define components for the Theme Menu Panel
	private JButton themeLabel = new JButton("Appearance", new ImageIcon("images/themePref.png"));
	private JComboBox selectThemeComboBox = new JComboBox(themeList);
	private JLabel selectThemeJLabel = new JLabel("Select Theme");
	private JButton installNewThemeJButton = new JButton("Install");
	private JLabel installNewThemeJLabel = new JLabel("Install New Theme");

	//Initialize array to hold current file settings and accept all new setting changes
	private Object[] settingsToWrite = settingsArray;

        //Arrays for editing contact aliases
        private String[] currentContacts;
        private String[] currentAliases;

	//Constructor
	PreferencesInterface() throws IOException {

		//Initialize Preferences Window
		preferences = new JFrame("Preferences");
		preferences.setSize(615, 375);
		preferences.setResizable(false);
		contentPane.setLayout(null);
		preferences.setLocationRelativeTo(CommunicationInterface.imContentFrame);
		preferences.setIconImage(Toolkit.getDefaultToolkit().getImage("images/logosmall.png"));
		//Retrieve all available font names and set font combo box
		String[] allFontNames = new String[allFonts.length];
		//Client.clientResource.fontFamilyTable.clear();
		for (int a = 0; a < Athena.clientResource.allFonts.length; a++) {
			allFontNames[a] = Athena.clientResource.allFonts[a].getFontName();
		}

                if(settingsArray[3].toString().equals(""))
                    downloadLocation = Athena.clientResource.getDownloadLocation();
                else
                    downloadLocation = settingsArray[3].toString();

                downloadLocationVal = downloadLocation;


                logLevelComboBox = new JComboBox(logLevels);
                logLevelComboBox.setSelectedItem(Integer.toString(debugLogVal));
                downDirTextField.setText(downloadLocationVal);

                currentContacts = Athena.getContactsArrayFromTable();
                contactAliasComboBox = new JComboBox(currentContacts);

		selectFontComboBox = new JComboBox(allFontNames);
		selectFontComboBox.setSelectedItem(setFontFaceVal);

		fontSizeComboBox = new JComboBox(new String[]{"8", "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48"});
		fontSizeComboBox.setSelectedItem(Integer.toString(setSizeVal));

		//Initialize borders
		blackline = BorderFactory.createLineBorder(Color.black);
		whiteline = BorderFactory.createLineBorder(Color.white);
		Border labelBorder = BorderFactory.createRaisedBevelBorder();
		Border blackBorder = BorderFactory.createBevelBorder(1, Color.darkGray, Color.black);
		Border extraBorder = BorderFactory.createLoweredBevelBorder();
		Border prefAltBorder = BorderFactory.createCompoundBorder(labelBorder, extraBorder);
		Border prefBorder = BorderFactory.createCompoundBorder(blackBorder, labelBorder);
		generalTitledBorder = BorderFactory.createTitledBorder(
				prefBorder, "System Options");
		contactPrefTitledBorder = BorderFactory.createTitledBorder(
				prefBorder, "Contact List Options");
		formattingTitledBorder = BorderFactory.createTitledBorder(
				prefBorder, "Formatting Options");
		notificationsTitledBorder = BorderFactory.createTitledBorder(
				prefBorder, "Notification Options");
		themeTitleBorder = BorderFactory.createTitledBorder(
				prefBorder, "Appearance Options");

		//Size the default components
		prefLabelPaneLeft.setBounds(15, 10, 100, 320);
		prefLabelPaneRight.setBounds(470, 10, 100, 220);
		prefLabelPaneLeft.setBorder(prefAltBorder);
		prefLabelPaneRight.setBorder(prefAltBorder);
		apply.setBounds(470, 250, 100, 30);
		cancel.setBounds(470, 290, 100, 30);

		// Set apply button default to disabled until changes are made
		apply.setEnabled(false);

		//Initialize default button action listeners
		apply.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				//Apply all changes
				try {
					setGeneralSettings(systemTrayFlag, systemTrayVal, allowESCFlag, allowESCVal, enableSpellCheckFlag, enableSCVal,
                                                            downloadLocationFlag, downloadLocationVal, debugLogFlag, debugLogVal);
					setNotificationSettings(enableSoundsFlag, enableSoundsVal);
					setFormattingSettings(setFontFaceFlag, setBoldFlag, setItalicsFlag, setUnderlineFlag, setSizeFlag,
							setFontFaceVal, setBoldVal, setItalicsVal, setUnderlineVal, setSizeVal);
					writeSavedPreferences(settingsToWrite);
				} catch (Exception e) {

					e.printStackTrace();
				}
				apply.setEnabled(false);
			}
		});

		cancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				preferences.dispose();
			}
		});

		//Initialize the JPanels for each of the options
		//General Menu Section
		/*************************************************/
		generalLabel.setForeground(Color.white);
		originalColor = generalLabel.getBackground();
		generalLabel.setBackground(Color.black);
		generalLabel.setBorder(whiteline);
		generalLabel.setVerticalTextPosition(JLabel.BOTTOM);
		generalLabel.setHorizontalTextPosition(JLabel.CENTER);
		generalLabel.setBounds(30, 20, 75, 75);
		generalLabel.setBorder(labelBorder);

		generalPanel = new JPanel();
		generalPanel.setLayout(null);
		generalPanel.setBorder(generalTitledBorder);
		generalPanel.setBounds(140, 15, 300, 300);
		generalPanel.setVisible(true);

		systemTrayCheckBox.setBounds(50, 20, 200, 50);
		allowESCCheckBox.setBounds(50, 60, 200, 50);
		enableSpellCheckCheckBox.setBounds(50, 100, 200, 50);
		downDirJLabel.setBounds(50,150,200,25);
		downDirTextField.setBounds(50,170,200,25);
		downDirLaunchButton.setBounds(155,195,94,25);
		downDirJLabel.setVisible(true);
		downDirLaunchButton.setVisible(true);
		downDirTextField.setVisible(true);
		logLevelLabel.setBounds(50,230,125,25);
		logLevelComboBox.setBounds(199,230,50,25);
		logLevelLabel.setVisible(true);
		logLevelComboBox.setVisible(true);

		generalPanel.add(logLevelComboBox);
		generalPanel.add(logLevelLabel);
		generalPanel.add(downDirJLabel);
		generalPanel.add(downDirTextField);
		generalPanel.add(downDirLaunchButton);
		generalPanel.add(systemTrayCheckBox);
		generalPanel.add(allowESCCheckBox);
		generalPanel.add(enableSpellCheckCheckBox);

		systemTrayCheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				apply.setEnabled(true);
				if (e.getStateChange() == ItemEvent.SELECTED) {
					systemTrayVal = true;
				} else {
					systemTrayVal = false;
				}
				settingsToWrite[0] = systemTrayVal;
				systemTrayFlag = true;
			}
		});
		allowESCCheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				apply.setEnabled(true);
				if (e.getStateChange() == ItemEvent.SELECTED) {
					allowESCVal = true;
				} else {
					allowESCVal = false;
				}
				settingsToWrite[1] = allowESCVal;
				allowESCFlag = true;
			}
		});
		enableSpellCheckCheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				apply.setEnabled(true);
				if (e.getStateChange() == ItemEvent.SELECTED) {
					enableSCVal = true;
				} else {
					enableSCVal = false;
				}
				settingsToWrite[2] = enableSCVal;
				enableSpellCheckFlag = true;
			}
		});

                downDirLaunchButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			//Handle open button action.
			if (e.getSource() == downDirLaunchButton) {
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(null);


				if (returnVal == JFileChooser.APPROVE_OPTION) {
                                        apply.setEnabled(true);
					File file = fc.getSelectedFile();
					downDirTextField.setText(file.getPath() + "\\");
                                        downloadLocationVal = file.getPath() + "\\";
                                        settingsToWrite[3] = downloadLocationVal;
                                        downloadLocationFlag = true;
				}
			}
		}});

                logLevelComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				apply.setEnabled(true);
                                debugLogVal = Integer.parseInt(logLevelComboBox.getSelectedItem().toString());
				settingsToWrite[4] = debugLogVal;
				debugLogFlag = true;
			}
		});
		/*************************************************/
		//Notification Menu Section
		/*************************************************/
		notificationsLabel.setForeground(Color.black);
		notificationsLabel.setVerticalTextPosition(JLabel.BOTTOM);
		notificationsLabel.setHorizontalTextPosition(JLabel.CENTER);
		notificationsLabel.setBounds(30, 100, 75, 75);
		notificationsLabel.setBorder(labelBorder);

		notificationsPanel = new JPanel();
		notificationsPanel.setLayout(null);
		notificationsPanel.setBorder(notificationsTitledBorder);
		notificationsPanel.setBounds(140, 15, 300, 300);
		notificationsPanel.setVisible(false);

		enableSoundsCheckBox.setBounds(50, 20, 200, 50);

		notificationsPanel.add(enableSoundsCheckBox);

		enableSoundsCheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				apply.setEnabled(true);
				if (e.getStateChange() == ItemEvent.SELECTED) {
					enableSoundsVal = true;
				} else {
					enableSoundsVal = false;
				}
				settingsToWrite[5] = enableSoundsVal;
				enableSoundsFlag = true;
			}
		});
		/*************************************************/
		//Contact List Menu Selection
		/*************************************************/
		contactPrefLabel.setForeground(Color.black);
		contactPrefLabel.setVerticalTextPosition(JLabel.BOTTOM);
		contactPrefLabel.setHorizontalTextPosition(JLabel.CENTER);
		contactPrefLabel.setBounds(30, 180, 75, 75);
		contactPrefLabel.setBorder(labelBorder);

		contactPrefPanel = new JPanel();
		contactPrefPanel.setLayout(null);
		contactPrefPanel.setBorder(contactPrefTitledBorder);
		contactPrefPanel.setBounds(140, 15, 300, 300);
		contactPrefPanel.setVisible(false);

		contactAliasJLabel.setBounds(50, 20, 200, 20);
                aliasDropdownLabel.setBounds(50, 60, 200, 20);
                contactAliasComboBox.setBounds(50, 85, 200, 20);
                updateAliasLabel.setBounds(50, 120, 80, 20);
                currentAliasLabel.setBounds(140, 120, 120, 20);
                currentAliasLabel.setForeground(Color.red);
                newAliasLabel.setBounds(50, 150, 80, 20);
                updateAliasTextField.setBounds(140, 150, 120, 20);
                updateAliasButton.setBounds(140, 180, 120, 30);
                updateAliasButton.setEnabled(false);

                contactAliasComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				String currentContact = contactAliasComboBox.getSelectedItem().toString();
                                    if(Athena.contactsTable.containsKey(currentContact))
                                        currentAliasLabel.setText(Athena.contactsTable.get(currentContact));
			}
		});

                updateAliasTextField.addKeyListener(new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                        updateAliasButton.setEnabled(true);
                        if (updateAliasTextField.getText().length() == 16) {
					e.consume();
				}
                    }

                    public void keyPressed(KeyEvent e) {
                        //Do nothing
                     }

                     public void keyReleased(KeyEvent e) {
                         //Do nothing
                     }
                });

                updateAliasButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
			//Handle open button action.

			if(updateAliasTextField.getText().trim().length() < 3)
                            JOptionPane.showMessageDialog(null, "Contact alias must be at least 3 characters long.");
                        else
                        {
                            String newAlias = updateAliasTextField.getText();
                            String currentContact = contactAliasComboBox.getSelectedItem().toString();
                                    if(Athena.contactsTable.containsKey(currentContact))
                                    {
                                        if(Athena.clientResource.aliasListModel.contains(Athena.contactsTable.get(currentContact)))
                                            Athena.clientResource.aliasSignOff(Athena.contactsTable.get(currentContact));

                                        Athena.contactsTable.remove(currentContact);
                                        Athena.contactsTable.put(currentContact, newAlias);
                                        Athena.writeBuddyListToFile(Athena.getContactsArrayFromTable());
                                        Athena.checkUserStatus(currentContact, "PauseThread!");
                                        currentAliasLabel.setText(newAlias);
                                        updateAliasButton.setEnabled(false);
                                        updateAliasTextField.setText("");
                                    }
                        }
		}});

		contactPrefPanel.add(contactAliasJLabel);
                contactPrefPanel.add(contactAliasComboBox);
                contactPrefPanel.add(updateAliasLabel);
                contactPrefPanel.add(updateAliasButton);
                contactPrefPanel.add(currentAliasLabel);
                contactPrefPanel.add(aliasDropdownLabel);
                contactPrefPanel.add(updateAliasTextField);
                contactPrefPanel.add(newAliasLabel);

		/*************************************************/
		//Formatting Menu Selection
		/*************************************************/
		formattingLabel.setForeground(Color.black);
		formattingLabel.setVerticalTextPosition(JLabel.BOTTOM);
		formattingLabel.setHorizontalTextPosition(JLabel.CENTER);
		formattingLabel.setBounds(485, 20, 75, 75);
		formattingLabel.setBorder(labelBorder);

		formattingPanel = new JPanel();
		formattingPanel.setLayout(null);
		formattingPanel.setBorder(formattingTitledBorder);
		formattingPanel.setBounds(140, 15, 300, 300);
		formattingPanel.setVisible(false);

		selectFontComboBox.setBounds(50, 55, 200, 30);
		fontSizeComboBox.setBounds(192, 115, 50, 30);
		setBoldCheckBox.setBounds(50, 125, 100, 30);
		setItalicsCheckBox.setBounds(50, 155, 100, 30);
		setUnderlineCheckBox.setBounds(50, 185, 100, 30);
		selectFontLabel.setBounds(50, 30, 100, 20);
		fontSizeLabel.setBounds(190, 95, 100, 20);
		generalFontLabel.setBounds(50, 95, 100, 20);

		formattingPanel.add(selectFontComboBox);
		formattingPanel.add(setBoldCheckBox);
		formattingPanel.add(setItalicsCheckBox);
		formattingPanel.add(setUnderlineCheckBox);
		formattingPanel.add(fontSizeComboBox);
		formattingPanel.add(selectFontLabel);
		formattingPanel.add(fontSizeLabel);
		formattingPanel.add(generalFontLabel);


		selectFontComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				apply.setEnabled(true);
				setFontFaceVal = selectFontComboBox.getSelectedItem().toString();
				System.out.println("Retrieved font style: " + setFontFaceVal);
				settingsToWrite[7] = setFontFaceVal;
				setFontFaceFlag = true;


			}
		});

		fontSizeComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				apply.setEnabled(true);
				setSizeVal = Integer.parseInt(fontSizeComboBox.getSelectedItem().toString());
				System.out.println("Retrieved font size: " + setSizeVal);
				settingsToWrite[11] = setSizeVal;
				setSizeFlag = true;
			}
		});

		setBoldCheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				apply.setEnabled(true);
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setBoldVal = true;
				} else {
					setBoldVal = false;
				}
				settingsToWrite[8] = setBoldVal;
				setBoldFlag = true;
			}
		});

		setItalicsCheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				apply.setEnabled(true);
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setItalicsVal = true;
				} else {
					setItalicsVal = false;
				}
				settingsToWrite[9] = setItalicsVal;
				setItalicsFlag = true;
			}
		});

		setUnderlineCheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				apply.setEnabled(true);
				if (e.getStateChange() == ItemEvent.SELECTED) {
					setUnderlineVal = true;
				} else {
					setUnderlineVal = false;
				}
				settingsToWrite[10] = setUnderlineVal;
				setUnderlineFlag = true;
			}
		});
		/*************************************************/
		//Theme Menu Selection
		/*************************************************/
		themeLabel.setForeground(Color.black);
		themeLabel.setVerticalTextPosition(JLabel.BOTTOM);
		themeLabel.setHorizontalTextPosition(JLabel.CENTER);
		themeLabel.setBounds(485, 100, 75, 75);
		themeLabel.setBorder(labelBorder);

		themePanel = new JPanel();
		themePanel.setLayout(null);
		themePanel.setBorder(themeTitleBorder);
		themePanel.setBounds(140, 15, 300, 300);
		themePanel.setVisible(false);

		//Define components for the Theme Menu Panel
		selectThemeComboBox.setBounds(50, 70, 200, 50);
		selectThemeJLabel.setBounds(50, 20, 100, 50);
		installNewThemeJButton.setBounds(50, 175, 100, 50);
		installNewThemeJLabel.setBounds(50, 125, 120, 50);

		themePanel.add(selectThemeComboBox);
		themePanel.add(selectThemeJLabel);
		themePanel.add(installNewThemeJButton);
		themePanel.add(installNewThemeJLabel);

		//This settings array update will go in check box action listeners when implemented as seen above in general settings
		//settingsToWrite[10] = "0";
		/*************************************************/
		//ActionListener to make the connect menu item connect
		selectThemeComboBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				try {
					UIManager.setLookAndFeel(selectThemeComboBox.getSelectedItem().toString());
				} catch (Exception e) {
					e.printStackTrace();
					SwingUtilities.updateComponentTreeUI(preferences);
					preferences.pack();
					preferences.repaint();
				}
			}
		});

		//Mouse Listener for the options
		//MouseListener for the generalPreferences
		MouseListener mouseListenerGeneral = new MouseAdapter() {

			public void mouseClicked(MouseEvent mouseEvent) {
				refreshSettingsView(generalLabel);
			}
		};

		//MouseListener for the notificationsPreferences
		MouseListener mouseListenerNotifications = new MouseAdapter() {

			public void mouseClicked(MouseEvent mouseEvent) {
				refreshSettingsView(notificationsLabel);

			}
		};

		//MouseListener for the encryptionPreferences
		MouseListener mouseListenerEncryption = new MouseAdapter() {

			public void mouseClicked(MouseEvent mouseEvent) {
				refreshSettingsView(contactPrefLabel);
			}
		};

		//MouseListener for the formattingPreferences
		MouseListener mouseListenerFormatting = new MouseAdapter() {

			public void mouseClicked(MouseEvent mouseEvent) {
				refreshSettingsView(formattingLabel);
			}
		};

		//MouseListener for the themePreferences
		MouseListener mouseListenerTheme = new MouseAdapter() {

			public void mouseClicked(MouseEvent mouseEvent) {
				refreshSettingsView(themeLabel);
			}
		};


		//Add the mouseListeners to the Labels
		generalLabel.addMouseListener(mouseListenerGeneral);
		notificationsLabel.addMouseListener(mouseListenerNotifications);
		contactPrefLabel.addMouseListener(mouseListenerEncryption);
		formattingLabel.addMouseListener(mouseListenerFormatting);
		themeLabel.addMouseListener(mouseListenerTheme);


		//Add the Drawing Panels to the LabelPane
		prefLabelPaneLeft.add(generalLabel);
		prefLabelPaneLeft.add(notificationsLabel);
		prefLabelPaneLeft.add(contactPrefLabel);
		prefLabelPaneRight.add(formattingLabel);
		prefLabelPaneRight.add(themeLabel);

		//Add the JPanels to the ContentPane (set to default until Label Image is clicked)
		contentPane.add(notificationsPanel);
		contentPane.add(generalPanel);
		contentPane.add(contactPrefPanel);
		contentPane.add(formattingPanel);
		contentPane.add(themePanel);
		contentPane.add(prefLabelPaneLeft);
		contentPane.add(prefLabelPaneRight);
		contentPane.add(apply);
		contentPane.add(cancel);


		//Initialize Frame
		preferences.setContentPane(contentPane);
		preferences.setVisible(true);


	}

	private void refreshSettingsView(JButton activeButton) {
		if (activeButton == generalLabel) {
			generalPanel.setVisible(true);
			generalLabel.setBackground(Color.black);
			generalLabel.setForeground(Color.white);
			notificationsPanel.setVisible(false);
			notificationsLabel.setBackground(originalColor);
			notificationsLabel.setForeground(Color.black);
			contactPrefPanel.setVisible(false);
			contactPrefLabel.setBackground(originalColor);
			contactPrefLabel.setForeground(Color.black);
			formattingPanel.setVisible(false);
			formattingLabel.setBackground(originalColor);
			formattingLabel.setForeground(Color.black);
			themePanel.setVisible(false);
			themeLabel.setBackground(originalColor);
			themeLabel.setForeground(Color.black);
		}
		if (activeButton == notificationsLabel) {
			generalPanel.setVisible(false);
			generalLabel.setBackground(originalColor);
			generalLabel.setForeground(Color.black);
			notificationsPanel.setVisible(true);
			notificationsLabel.setBackground(Color.black);
			notificationsLabel.setForeground(Color.white);
			contactPrefPanel.setVisible(false);
			contactPrefLabel.setBackground(originalColor);
			contactPrefLabel.setForeground(Color.black);
			formattingPanel.setVisible(false);
			formattingLabel.setBackground(originalColor);
			formattingLabel.setForeground(Color.black);
			themePanel.setVisible(false);
			themeLabel.setBackground(originalColor);
			themeLabel.setForeground(Color.black);
		}
		if (activeButton == contactPrefLabel) {
			generalPanel.setVisible(false);
			generalLabel.setBackground(originalColor);
			generalLabel.setForeground(Color.black);
			notificationsPanel.setVisible(false);
			notificationsLabel.setBackground(originalColor);
			notificationsLabel.setForeground(Color.black);
			contactPrefPanel.setVisible(true);
			contactPrefLabel.setBackground(Color.black);
			contactPrefLabel.setForeground(Color.white);
                        if(Athena.contactsTable.size() > 0)
                            currentAliasLabel.setText(Athena.contactsTable.get(contactAliasComboBox.getSelectedItem().toString()));
                        else
                            currentAliasLabel.setText("N/A");
                        updateAliasTextField.setText("");
			formattingPanel.setVisible(false);
			formattingLabel.setBackground(originalColor);
			formattingLabel.setForeground(Color.black);
			themePanel.setVisible(false);
			themeLabel.setBackground(originalColor);
			themeLabel.setForeground(Color.black);
		}
		if (activeButton == formattingLabel) {
			generalPanel.setVisible(false);
			generalLabel.setBackground(originalColor);
			generalLabel.setForeground(Color.black);
			notificationsPanel.setVisible(false);
			notificationsLabel.setBackground(originalColor);
			notificationsLabel.setForeground(Color.black);
			contactPrefPanel.setVisible(false);
			contactPrefLabel.setBackground(originalColor);
			contactPrefLabel.setForeground(Color.black);
			formattingPanel.setVisible(true);
			formattingLabel.setBackground(Color.black);
			formattingLabel.setForeground(Color.white);
			themePanel.setVisible(false);
			themeLabel.setBackground(originalColor);
			themeLabel.setForeground(Color.black);
		}
		if (activeButton == themeLabel) {
			generalPanel.setVisible(false);
			generalLabel.setBackground(originalColor);
			generalLabel.setForeground(Color.black);
			notificationsPanel.setVisible(false);
			notificationsLabel.setBackground(originalColor);
			notificationsLabel.setForeground(Color.black);
			contactPrefPanel.setVisible(false);
			contactPrefLabel.setBackground(originalColor);
			contactPrefLabel.setForeground(Color.black);
			formattingPanel.setVisible(false);
			formattingLabel.setBackground(originalColor);
			formattingLabel.setForeground(Color.black);
			themePanel.setVisible(true);
			themeLabel.setBackground(Color.black);
			themeLabel.setForeground(Color.white);
		}
	}

	private void setGeneralSettings(boolean systemTrayFlag, boolean systemTrayVal, boolean allowESCFlag,
			boolean allowESCVal, boolean enableSpellCheckFlag, boolean enableSCVal,
                        boolean downloadLocationFlag, String downloadLocationVal, boolean debugLogFlag, int debugLogVal) throws AWTException {
		if (systemTrayFlag) {
			if (!(systemTrayVal)) {
				Athena.clientResource.setSystemTrayIcon(false);
			}
			if (systemTrayVal) {
				Athena.clientResource.setSystemTrayIcon(true);
			}
		}
		if (allowESCFlag) {
			//Adjust setting
			if (!(allowESCVal)) {
				Athena.clientResource.closeTabWithESC(false);
			}
			if (allowESCVal) {
				Athena.clientResource.closeTabWithESC(true);
			}
		}
		if (enableSpellCheckFlag) {
			//Adjust setting
			if (!(enableSCVal)) {
				Athena.clientResource.setSpellCheck(false);
			}
			if (enableSCVal) {
				Athena.clientResource.setSpellCheck(true);
			}
		}
                if (downloadLocationFlag) {
                    //Adjust setting
                    Athena.clientResource.setDownloadLocation(downloadLocationVal);
                }
                if (debugLogFlag) {
                    Athena.clientResource.setDebugLog(debugLogVal);
                }
	}

	private void setNotificationSettings(boolean enableSoundsFlag, boolean enableSoundsVal) {
		if (enableSoundsFlag) {
			if (!(enableSoundsVal)) {
				Athena.setEnableSounds(false);
			}
			if (enableSoundsVal) {
				Athena.setEnableSounds(true);
			}
		}
	}

	private void setFormattingSettings(boolean setFontFaceFlag, boolean setBoldFlag, boolean setItalicsFlag, boolean setUnderlineFlag, boolean setSizeFlag,
			String setFontFaceVal, boolean setBoldVal, boolean setItalicsVal, boolean setUnderlineVal, int setSizeVal) {
		if (setFontFaceFlag || setBoldFlag || setItalicsFlag || setUnderlineFlag || setSizeFlag) {
			Athena.clientResource.settingsLoaded = true;
			Athena.print.setTextFont(setFontFaceVal, setBoldVal, setItalicsVal, setUnderlineVal, setSizeVal);
			Athena.clientResource.setNewFontToLoad(setFontFaceVal, setBoldVal, setItalicsVal, setUnderlineVal, setSizeVal);
		}
	}

	private void writeSavedPreferences(Object[] settingsToWrite) {
		try {
			BufferedWriter outPref = new BufferedWriter(new FileWriter("./users/" + Athena.username + "/athena.conf"));

			//Write general settings
			outPref.write("[GENERAL]");
			outPref.newLine();
			outPref.write("allowSystemTray=" + settingsToWrite[0]);
			outPref.newLine();
			outPref.write("allowESCTab=" + settingsToWrite[1]);
			outPref.newLine();
			outPref.write("enableSpellCheck=" + settingsToWrite[2]);
                        outPref.newLine();
                        outPref.write("downloadLocation=" + settingsToWrite[3]);
                        outPref.newLine();
                        outPref.write("debugLog=" + settingsToWrite[4]);
			outPref.newLine();
			outPref.newLine();
			outPref.newLine();

			//Write notification settings
			outPref.write("[NOTIFICATIONS]");
			outPref.newLine();
			outPref.write("enableSounds=" + settingsToWrite[5]);
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
			outPref.write("fontSize=" + settingsToWrite[10]);
			outPref.newLine();
			outPref.newLine();
			outPref.newLine();

			//Write theme settings
			outPref.write("[THEME]");
			outPref.newLine();
			outPref.write("activeTheme=" + settingsToWrite[11]);

			outPref.close();

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}
