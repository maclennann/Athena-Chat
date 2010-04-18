/****************************************************
 * Athena: Encrypted Messaging Application v.0.0.5
 * By: 	
 * 			Gregory LeBlanc
 * 			Norm Maclennan 
 * 			Stephen Failla
 * 
 * This program allows a user to send encrypted messages over a fully standardized messaging architecture. It uses RSA with (x) bit keys and SHA-256 to 
 * hash the keys on the server side. It also supports fully encrypted emails using a standardized email address. The user can also send "one-off" emails
 * using a randomly generated email address
 * 
 * File: ClientAbout.java
 * 
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
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import sun.misc.BASE64Encoder;

public class ClientAbout extends JFrame { 

	/**
	 * 
	 */
	public static final int debug = 0;
	//Components for the visual display of the aboutWindow window
	public JFrame aboutWindow;
	public JPanel contentPane = new JPanel();
	public JLabel usernameLabel = new JLabel("Athena Chat Client v0.0.5b");
	public JButton cancel = new JButton("OK");
	ImageIcon logoicon = new ImageIcon("../images/splash.png");
	JLabel logo = new JLabel(); 

	//Constructor | Here's where the fun begins
	ClientAbout() throws AWTException { 
		logo.setIcon(logoicon);
		
		//Initialize aboutWindow window
		aboutWindow = new JFrame("About Athena");
		//aboutWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		aboutWindow.setSize(200,300);
		aboutWindow.setResizable(false);
		contentPane.setLayout(null);

		//Adjust font sizes
		cancel.setFont(new Font("Dialog", 1, 10));
		usernameLabel.setFont(new Font("Dialog", 1, 12));

		//Size the components
		usernameLabel.setBounds(20,210,150,25);
		cancel.setBounds(110,235,75,30);
		logo.setBounds(10,10,175,200);
		
		//ActionListener to make the connect menu item connect
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				aboutWindow.dispose();
			}
		});

		//Add the components to the Frame
		contentPane.add(usernameLabel);
		contentPane.add(cancel);
		contentPane.add(logo); 
		//Initialize Frame
		aboutWindow.setContentPane(contentPane);
		aboutWindow.setVisible(true);
	}

}


