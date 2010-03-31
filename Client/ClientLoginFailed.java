import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ClientLoginFailed extends JFrame { 
	//Components for the visual display of the login window
	public JFrame loginFailed;
	public JPanel contentPane = new JPanel();
	public JLabel failedPasswordJLabel = new JLabel("Password");
	
	ClientLoginFailed() {
		loginFailed = new JFrame("Athena Chat Application");
		loginFailed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loginFailed.setSize(200,250);
		loginFailed.setResizable(false);
		contentPane.setLayout(null);
		
		failedPasswordJLabel.setBounds(15,15,150,150);
		failedPasswordJLabel.setText("Sorry, your login credentials were not correct. Please try again!");
		contentPane.add(failedPasswordJLabel);
		loginFailed.add(contentPane);
		
		loginFailed.setVisible(true);		
	}


}