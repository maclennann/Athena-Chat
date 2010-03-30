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
/**
 * @author OlmypuSoft
 *
 */
import java.awt.AWTException;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.ImageIcon;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class ClientLogin extends JFrame { 

	//Components for the visual display of the login window
	public JFrame login;
	public JPanel contentPane = new JPanel();
	public JTextField username = new JTextField();
	public JPasswordField password = new JPasswordField();
	public JLabel usernameLabel = new JLabel("Username");
	public JLabel passwordLabel = new JLabel("Password");
	public JButton connect = new JButton("Connect");
	public JButton cancel = new JButton("Cancel");

	ImageIcon logoicon = new ImageIcon("../images/logo.png");
	JLabel logo = new JLabel(); 
	

	//Define an icon for the system tray icon
	public TrayIcon trayIcon;

	public static ClientApplet clientResource;

	//Constructor | Here's where the fun begins
	ClientLogin() throws AWTException { 
		logo.setIcon(logoicon);
		//Initialize Login window
		login = new JFrame("Athena Chat Application");
		login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		login.setSize(200,300);
		login.setResizable(false);
		contentPane.setLayout(null);

		//Define the system tray icon
		if (SystemTray.isSupported()) { 
			SystemTray tray = SystemTray.getSystemTray();
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

			trayIcon = new TrayIcon(trayImage, "Tray Demo", popup);
			trayIcon.setImageAutoSize(true);
			tray.add(trayIcon);
		}

		//Adjust font sizes
		connect.setFont(new Font("Dialog", 1, 10));
		cancel.setFont(new Font("Dialog", 1, 10));
		usernameLabel.setFont(new Font("Dialog", 1, 10));
		passwordLabel.setFont(new Font("Dialog", 1, 10));

		//Size the components
		usernameLabel.setBounds(50,115,100,25);
		username.setBounds(50,140,100,25);
		passwordLabel.setBounds(50,165,100,25);
		password.setBounds(50,190,100,25);
		cancel.setBounds(10,235,75,30);
		connect.setBounds(105,235,75,30);
		logo.setBounds(60,10,100,100);
		//Let the "Action Begin"

		//ActionListener to make the connect menu item connect
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				//Ya'll like some hash?
				try {
					Client.setUsername(username.getText());
					
					Client.connect(username.getText(),password.getPassword());
					login.setVisible(false);									
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		
		//ActionListener to make the connect menu item connect
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				ClientAddUser testOfWindow = new ClientAddUser();
				testOfWindow.setVisible(true);
			}
		});


		//Add the components to the Frame
		contentPane.add(usernameLabel);
		contentPane.add(username);
		contentPane.add(passwordLabel);
		contentPane.add(password);
		contentPane.add(connect);
		contentPane.add(cancel);
		contentPane.add(logo); 
		//Initialize Frame
		login.setContentPane(contentPane);
		login.setVisible(true);
	}

	//This will return the hashed input string
	public static byte[] computeHash(String toHash) throws Exception { 
		MessageDigest d = null;
		d = MessageDigest.getInstance("SHA-1");
		d.reset();
		d.update(toHash.getBytes());
		return d.digest();	
	}

	//This will turn a byteArray to a String
	public static String byteArrayToHexString(byte[] b) { 
		StringBuffer sb = new StringBuffer(b.length * 2);
		for (int i = 0; i < b.length; i++) { 
			int v = b[i] & 0xff;
			if (v < 16) { 
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}

}
