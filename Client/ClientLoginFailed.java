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

public class ClientLoginFailed extends JFrame { 
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
	
	ClientLoginFailed() {
		loginFailed = new JFrame("Athena Chat Application");
		loginFailed.setSize(400,250);
		loginFailed.setResizable(false);
		contentPane.setLayout(null);
		
		Image generalPreferencesImage = Toolkit.getDefaultToolkit().getImage("../images/sadFace.jpg");
		failedLoginDrawingPanel = new DrawingPanel(generalPreferencesImage);	
		failedLoginDrawingPanel.setBounds(140,75,100,100);
		
		closeThis.setBounds(160,185,60,25);
		contentPane.add(closeThis);
		failedPasswordJLabel.setBounds(15,15,400,25);
		failedPasswordJLabel.setText("Sorry, your login credentials were not correct. Please try again!");
		contentPane.add(failedPasswordJLabel);
		contentPane.add(failedLoginDrawingPanel);
		loginFailed.add(contentPane);
		
		loginFailed.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(WindowEvent winEvt) {
		        try {
					ClientLogin loginGUI = new ClientLogin();
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
					ClientLogin loginGUI = new ClientLogin();
					loginFailed.dispose();
				} catch (AWTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		
		loginFailed.setVisible(true);
		closeThis.requestFocusInWindow();
	}
		
}