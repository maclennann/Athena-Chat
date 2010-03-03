import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * 
 */

/**
 * @author OlmypuSoft
 *
 */
public class ClientAddUser extends JPanel {
	
	//Define components
	public JFrame addUserJFrame;
	public JPanel contentPane, generalInformationJPanel, loginInformationJPanel;
	public JLabel firstNameJLabel = new JLabel("First name:");
	public JLabel lastNameJLabel = new JLabel("Last name:");
	public JLabel emailAddressJLabel = new JLabel("Email address:");
	public JLabel confirmEmailAddressJLabel = new JLabel("Confirm Email address:");
	public JLabel userNameJLabel = new JLabel("Username:");
	public JLabel passwordJLabel = new JLabel("Password:");
	public JLabel confirmPasswordJLabel = new JLabel("Confirm Password:");
	public JTextField firstNameJTextField;
	public JTextField lastNameJTextField;
	public JTextField emailAddressJTextField;
	public JTextField confirmEmailAddresJTextField;
	public JTextField userNameJTextField;
	public JPasswordField passwordJPasswordField;
	public JPasswordField confirmpasswordJPasswordField;
	public JButton confirmJButton = new JButton("Confirm");
	public JButton cancelJButton = new JButton("Cancel");
	
	public Border blackline;
	public TitledBorder generalTitledBorder;
	
	ClientAddUser() {
		//Create the Main Frame
		addUserJFrame= new JFrame("User Registration");
		addUserJFrame.setSize(300,375);
		addUserJFrame.setResizable(false);
		
		//Create the content Pane
		contentPane = new JPanel();
		contentPane.setLayout(null);
		contentPane.setVisible(true);
				
		//Initalize borders
		blackline = BorderFactory.createLineBorder(Color.black);
		generalTitledBorder = BorderFactory.createTitledBorder(
			       blackline, "Registration Information");
		
		firstNameJTextField = new JTextField();
		firstNameJLabel.setBounds(15,15,100,25);
		firstNameJTextField.setBounds(85,15,100,25);
		
		lastNameJTextField = new JTextField();
		lastNameJLabel.setBounds(15,55,100,25);
		lastNameJTextField.setBounds(85,55,100,25);
		
		emailAddressJTextField = new JTextField();
		emailAddressJLabel.setBounds(15,95,100,25);
		emailAddressJTextField.setBounds(105,95,100,25);
		
		confirmEmailAddresJTextField = new JTextField();
		confirmEmailAddressJLabel.setBounds(15,135,135,25);
		confirmEmailAddresJTextField.setBounds(155,135,100,25);
		
		userNameJTextField = new JTextField();
		userNameJLabel.setBounds(15,175,100,25);
		userNameJTextField.setBounds(85,175,100,25);
		
		passwordJPasswordField = new JPasswordField();
		passwordJLabel.setBounds(15,215,100,25);
		passwordJPasswordField.setBounds(85,215,100,25);
		
		confirmpasswordJPasswordField = new JPasswordField();
		confirmPasswordJLabel.setBounds(15,255,135,25);
		confirmpasswordJPasswordField.setBounds(135,255,100,25);
		
		confirmJButton.setBounds(25,290,100,25);
		cancelJButton.setBounds(150,290,100,25);
		
		contentPane.add(firstNameJLabel);
		contentPane.add(firstNameJTextField);
		contentPane.add(lastNameJLabel);
		contentPane.add(lastNameJTextField);
		contentPane.add(emailAddressJLabel);
		contentPane.add(emailAddressJTextField);
		contentPane.add(confirmEmailAddressJLabel);
		contentPane.add(confirmEmailAddresJTextField);
		contentPane.add(userNameJLabel);
		contentPane.add(userNameJTextField);
		contentPane.add(passwordJLabel);
		contentPane.add(passwordJPasswordField);
		contentPane.add(confirmPasswordJLabel);
		contentPane.add(confirmpasswordJPasswordField);
		contentPane.add(confirmJButton);
		contentPane.add(cancelJButton);
		contentPane.setVisible(true);
		contentPane.setBorder(generalTitledBorder);
		
		addUserJFrame.setContentPane(contentPane);
		addUserJFrame.setVisible(true);
	}

}
