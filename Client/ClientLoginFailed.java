import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ClientLoginFailed extends JFrame { 
	//Components for the visual display of the login window
	public JFrame loginFailed;
	public JPanel contentPane = new JPanel();
	public JLabel failedPasswordJLabel = new JLabel("Password");
	public DrawingPanel failedLoginDrawingPanel;
	
	ClientLoginFailed() {
		loginFailed = new JFrame("Athena Chat Application");
		loginFailed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loginFailed.setSize(400,250);
		loginFailed.setResizable(false);
		contentPane.setLayout(null);
		
		Image generalPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/sadFace.jpg");
		failedLoginDrawingPanel = new DrawingPanel(generalPreferencesImage);	
		failedLoginDrawingPanel.setBounds(140,75,100,100);
		
		failedPasswordJLabel.setBounds(15,15,400,25);
		failedPasswordJLabel.setText("Sorry, your login credentials were not correct. Please try again!");
		contentPane.add(failedPasswordJLabel);
		contentPane.add(failedLoginDrawingPanel);
		loginFailed.add(contentPane);
		
		loginFailed.setVisible(true);		
	}


}