import java.awt.AWTException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;

public class LoginFailedInterface extends JFrame { 
	/**
	 * 
	 */
	private static final long serialVersionUID = -5209122311648063644L;
	//Components for the visual display of the login window
	public JFrame loginFailed;
	public JPanel contentPane = new JPanel();
	public JLabel failedPasswordJLabel = new JLabel("Password");
	public DrawingPanel failedLoginDrawingPanel;
	public JButton closeThis = new JButton("OK");
	public JButton forgotPassword = new JButton("Reset Password");
	
	LoginFailedInterface() {
		loginFailed = new JFrame("Athena Chat Application");
		loginFailed.setSize(400,250);
		loginFailed.setResizable(false);
		contentPane.setLayout(null);
		loginFailed.setLocationRelativeTo(Athena.loginGUI);
		
		Image generalPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/sadFace.png");
		failedLoginDrawingPanel = new DrawingPanel(generalPreferencesImage);	
		failedLoginDrawingPanel.setBounds(140,60,100,100);
		
		closeThis.setBounds(80,185,60,25);
		forgotPassword.setBounds(180,185,150,25);
		contentPane.add(closeThis);
		contentPane.add(forgotPassword);
		failedPasswordJLabel.setBounds(15,15,400,25);
		failedPasswordJLabel.setText("Sorry, your login credentials were not correct. Please try again!");
		contentPane.add(failedPasswordJLabel);
		contentPane.add(failedLoginDrawingPanel);
		loginFailed.add(contentPane);
		
		loginFailed.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(WindowEvent winEvt) {
		        try {
					AuthenticationInterface loginGUI = new AuthenticationInterface();
					loginFailed.dispose();
				} catch (AWTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
			
		    }
		);		
		/*failedPasswordJLabel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				try {
					ClientLogin loginGUI = new ClientLogin();
					loginFailed.dispose();
				} catch (AWTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}}});
				
				*/
			closeThis.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event){
					//ClientAddUser testOfWindow = new ClientAddUser();
					try {
					AuthenticationInterface loginGUI = new AuthenticationInterface();
					loginFailed.dispose();
				} catch (AWTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
			forgotPassword.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event){
					//ClientAddUser testOfWindow = new ClientAddUser();
					try {
					ResetPasswordInterface passReset = new ResetPasswordInterface();
					loginFailed.dispose();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		loginFailed.setVisible(true);
		closeThis.requestFocusInWindow();
	}
	
	LoginFailedInterface(String messageToDisplay, boolean status) {
		loginFailed = new JFrame("Athena Chat Application");
		loginFailed.setSize(400,250);
		loginFailed.setResizable(false);
		contentPane.setLayout(null);
		
		//If status is true, that means it was a successful login so don't display the sad face.
		//TODO Add a happy face
		if(!(status)) {
		Image generalPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/sadFace.png");
		failedLoginDrawingPanel = new DrawingPanel(generalPreferencesImage);	
		failedLoginDrawingPanel.setBounds(140,75,100,100);
		contentPane.add(failedLoginDrawingPanel);
		contentPane.add(forgotPassword);
		}
		else{
		Image generalPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/happyface.png");
		failedLoginDrawingPanel = new DrawingPanel(generalPreferencesImage);	
		failedLoginDrawingPanel.setBounds(140,75,100,100);
		contentPane.add(failedLoginDrawingPanel);
		}
		closeThis.setBounds(160,185,60,25);
		contentPane.add(closeThis);
		failedPasswordJLabel.setBounds(15,15,400,75);
		failedPasswordJLabel.setText(messageToDisplay);
		contentPane.add(failedPasswordJLabel);

		loginFailed.add(contentPane);
		
		loginFailed.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(WindowEvent winEvt) {
		        try {
					Athena.loginGUI.dispose();
					AuthenticationInterface loginGUI = new AuthenticationInterface();
					loginFailed.dispose();
				} catch (AWTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
			
		    }
		);		
		/*failedPasswordJLabel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				try {
					ClientLogin loginGUI = new ClientLogin();
					loginFailed.dispose();
				} catch (AWTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}}});
				
				*/
			closeThis.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event){
					//ClientLogin loginGUI = new ClientLogin();
					loginFailed.dispose();
			}
		});
		
		
		loginFailed.setVisible(true);
		closeThis.requestFocusInWindow();
	}
		
}