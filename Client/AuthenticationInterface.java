/* Athena/Aegis Encrypted Chat Platform
 * AuthenticationInterface.java: Login screen provided on run.
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
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import sun.misc.BASE64Encoder;

/**
 * Creates the login window
 * @author OlympuSoft
 */
public class AuthenticationInterface extends JFrame {
	private static final long serialVersionUID = -1646506272427417952L;
	private static final int debug = 0;
	//Components for the visual display of the login window
	private JFrame login;
	private JPanel contentPane = new JPanel();
	private JTextField username = new JTextField();
	private JPasswordField password = new JPasswordField();
	private JLabel usernameLabel = new JLabel("Username");
	private JLabel passwordLabel = new JLabel("Password");
	private LinkText registerJLabel = new LinkText("Register!");
	private JButton connect = new JButton("Connect");
	private JButton cancel = new JButton("Cancel");
	private ImageIcon logoicon = new ImageIcon("images/logo.png");
	private JLabel logo = new JLabel();
	//Define an icon for the system tray icon
	private  TrayIcon trayIcon;

	//Constructor | Here's where the fun begins
	AuthenticationInterface() throws AWTException {

		// Get the default toolkit
		Toolkit toolkit = Toolkit.getDefaultToolkit();

		// Get the current screen size
		Dimension scrnsize = toolkit.getScreenSize();
		int width = (int) scrnsize.getWidth();
		int height = (int) scrnsize.getHeight();

		logo.setIcon(logoicon);
		//Initialize Login window
		login = new JFrame("Athena");
		login.setLocation(width / 2 - 100, height / 2 - 150);
		login.setIconImage(Toolkit.getDefaultToolkit().getImage("images/logosmall.png"));
		login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		login.setSize(200, 300);
		login.setResizable(false);
		contentPane.setLayout(null);

		//Define the system tray icon
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
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

			trayIcon = new TrayIcon(trayImage, "Tray Demo", popup);
			trayIcon.setImageAutoSize(true);
			tray.add(trayIcon);
		}

		//Adjust font sizes
		connect.setFont(new Font("Dialog", 1, 10));
		cancel.setFont(new Font("Dialog", 1, 10));
		usernameLabel.setFont(new Font("Dialog", 1, 10));
		passwordLabel.setFont(new Font("Dialog", 1, 10));
		registerJLabel.setFont(new Font("Dialog", 1, 11));

		//Size the components
		usernameLabel.setBounds(50, 105, 100, 25);
		username.setBounds(50, 130, 100, 25);
		passwordLabel.setBounds(50, 155, 100, 25);
		password.setBounds(50, 180, 100, 25);
		registerJLabel.setBounds(10, 210, 170, 25);
		registerJLabel.setHorizontalAlignment(JLabel.CENTER);
		connect.setBounds(105, 235, 75, 30);
		cancel.setBounds(10, 235, 75, 30);
		logo.setBounds(60, 10, 100, 100);


		//Let the "Action Begin"
		//ActionListener to make the register JLabel bring up the register window
		//ActionListener to make the connect menu item connect
		registerJLabel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				RegistrationInterface testOfWindow = new RegistrationInterface();
				testOfWindow.setVisible(true);
			}
		});

		//ActionListener to make the connect menu item connect
		connect.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				//Ya'll like some hash?
				try {

					Athena.setUsername(username.getText());
					String passwordToHash = new String(password.getPassword());
					String hashedPassword = computeHash(passwordToHash).toString();
					Athena.connect(username.getText(), hashedPassword);
					login.dispose();
					System.gc();
				} catch (Exception e) {

					e.printStackTrace();
				}

			}
		});


		//ActionListener to make the connect menu item connect
		password.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				//Ya'll like some hash?
				try {

					Athena.setUsername(username.getText());
					String uname = username.getText();
					String passwordToHash = new String(password.getPassword());
					if (!uname.equals("") || !passwordToHash.equals("")) {
						String hashedPassword = computeHash(passwordToHash).toString();
						Athena.connect(username.getText(), hashedPassword);
						login.setVisible(false);
						System.gc();
					} else {
						JOptionPane.showMessageDialog(null, "Please enter both a username and password!\n\n", "Login Error", JOptionPane.ERROR_MESSAGE);
					}
				} catch (Exception e) {

					e.printStackTrace();
				}

			}
		});


		//ActionListener to make the connect menu item connect
		username.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				//Ya'll like some hash?
				try {

					Athena.setUsername(username.getText());
					String uname = username.getText();
					String passwordToHash = new String(password.getPassword());
					if (!uname.equals("") || !passwordToHash.equals("")) {
						String hashedPassword = computeHash(passwordToHash).toString();
						Athena.connect(username.getText(), hashedPassword);
						login.setVisible(false);
						System.gc();
					} else {
						JOptionPane.showMessageDialog(null, "Please enter both a username and password!\n\n", "Login Error", JOptionPane.ERROR_MESSAGE);
					}
				} catch (Exception e) {

					e.printStackTrace();
				}

			}
		});




		//ActionListener to make the connect menu item connect
		cancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});



		//Add the components to the Frame
		contentPane.add(usernameLabel);
		contentPane.add(username);
		contentPane.add(passwordLabel);
		contentPane.add(password);
		contentPane.add(registerJLabel);
		contentPane.add(connect);
		contentPane.add(cancel);
		contentPane.add(logo);
		//Initialize Frame
		login.setContentPane(contentPane);
		login.setVisible(true);
	}

	/**
	 * Compute the SHA-1 hash of a String
	 * @param toHash String to hash
	 * @return toHash hashed with SHA-1
	 * @throws Exception
	 */
	public static String computeHash(String toHash) throws Exception {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1"); //step 2
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(e.getMessage());
		}
		try {
			md.update(toHash.getBytes("UTF-8")); //step 3
		} catch (UnsupportedEncodingException e) {
			throw new Exception(e.getMessage());
		}

		byte raw[] = md.digest(); //step 4
		String hash = (new BASE64Encoder()).encode(raw); //step 5
		return hash; //step 6
	}
}
