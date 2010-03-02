import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * 
 */

/**
 * @author OlympuSoft
 *
 */
//Let's make the preferences window
public class ClientPreferences extends JFrame {
	
	//Define components
	public JFrame preferences;
	public JPanel contentPane = new JPanel();
	public JPanel generalPane, notificationsPane, encryptionPane, formattingPane, themePane;
	public JScrollPane prefScrollPane = new JScrollPane();
	public JButton apply = new JButton("Apply");
	public JButton cancel = new JButton("Cancel");
	public JLabel test = new JLabel("TEST"); // test
	//public BufferedImage generalPreferences, notificationPreferences, encryptionPreferences, fomattingPreferences, themePreferences;
	
	//Constructor
	ClientPreferences() { 
		//Initialize Preferences Window
		preferences = new JFrame("Preferences");
		preferences.setSize(800,600);
		preferences.setResizable(false);
		contentPane.setLayout(null);
		
		//Initialize the JPanels for each of the options
		generalPane = new JPanel();
		generalPane.add(test); // test
		generalPane.setBounds(185,15,400,445);
		
		notificationsPane = new JPanel();
		notificationsPane.setBounds(175,15,600, 545);
		
		encryptionPane = new JPanel();
		encryptionPane.setBounds(175,15,600, 545);
		
		formattingPane = new JPanel();
		formattingPane.setBounds(175,15,600, 545);
		
		themePane = new JPanel();
		themePane.setBounds(175,15,600, 545);
		
		//Size the components
		prefScrollPane.setBounds(15, 15, 150, 545);
		apply.setBounds(700,525,75,30);
		cancel.setBounds(615,525,75,30);
		
		//Add the options to the Scroll Pane
		//TODO Add ActionListeners to the images to bring up the add/remove user windows
		Image generalPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/generalPref.png");
		Image notificationPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/notificationsPref.png");
		Image encryptionPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/encryptionPref.png");
		Image formattingPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/fontPref.png");
		Image themePreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/themePref.png");
		
		DrawingPanel generalPanel = new DrawingPanel(generalPreferencesImage);		
		DrawingPanel notificationPanel = new DrawingPanel(notificationPreferencesImage);
		DrawingPanel encryptionPanel = new DrawingPanel(encryptionPreferencesImage);
		DrawingPanel formattingPanel = new DrawingPanel(formattingPreferencesImage);
		DrawingPanel themePanel = new DrawingPanel(themePreferencesImage);
				
		generalPanel.setBounds(30,15,75,75);
		notificationPanel.setBounds(30,100,75,75);
		encryptionPanel.setBounds(30,185,75,75);
		formattingPanel.setBounds(30,270,75,75);
		themePanel.setBounds(30,355,75,75);
		
		//Mouse Listener for the options
		//MouseListener for the generalPreferences
	    	MouseListener mouseListenerGeneral = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				generalPane.setVisible(true);
				contentPane.add(generalPane);	
				System.out.println("HAIII"); // test
				
		}};
		
		//MouseListener for the notificationsPreferences
			MouseListener mouseListenerNotifications = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				//notificationsPane = new JPanel();
				contentPane.add(notificationsPane);			
		}};
		
		//MouseListener for the encryptionPreferences
			MouseListener mouseListenerEncryption = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				//encryptionPane = new JPanel();
				contentPane.add(encryptionPane);			
		}};
		
		//MouseListener for the formattingPreferences
			MouseListener mouseListenerFormatting = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				//formattingPane = new JPanel();
				contentPane.add(formattingPane);			
		}};
		
		//MouseListener for the themePreferences
			MouseListener mouseListenerTheme = new MouseAdapter() {
			public void mouseClicked(MouseEvent mouseEvent) {
				//themePane = new JPanel();
				contentPane.add(themePane);			
		}};
		
		//Add the mouselisteners
		generalPanel.addMouseListener(mouseListenerGeneral);
		notificationPanel.addMouseListener(mouseListenerNotifications);
		encryptionPanel.addMouseListener(mouseListenerEncryption);
		formattingPanel.addMouseListener(mouseListenerFormatting);
		themePanel.addMouseListener(mouseListenerTheme);
		
		
		//Add the components to the ScrollPane
		prefScrollPane.add(generalPanel);
		prefScrollPane.add(notificationPanel);
		prefScrollPane.add(encryptionPanel);
		prefScrollPane.add(formattingPanel);
		prefScrollPane.add(themePanel);
		
		
		//Add the components to the ContentPane
		contentPane.add(prefScrollPane);
		contentPane.add(apply);
		contentPane.add(cancel);

		
		//Initialize Frame
		preferences.setContentPane(contentPane);
		preferences.setVisible(true);
		
	}		
	
}
