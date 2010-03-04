import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
		
		//First Name Input
		firstNameJTextField = new JTextField();
		firstNameJLabel.setBounds(15,15,100,25);
		firstNameJTextField.setBounds(85,15,100,25);
		
		//Last Name Input
		lastNameJTextField = new JTextField();
		lastNameJLabel.setBounds(15,55,100,25);
		lastNameJTextField.setBounds(85,55,100,25);
		
		//Email Addres Input
		emailAddressJTextField = new JTextField();
		emailAddressJLabel.setBounds(15,95,100,25);
		emailAddressJTextField.setBounds(105,95,100,25);
		
		//Confirm Email Address Input
		confirmEmailAddresJTextField = new JTextField();
		confirmEmailAddressJLabel.setBounds(15,135,135,25);
		confirmEmailAddresJTextField.setBounds(155,135,100,25);
		
		//Username Input
		userNameJTextField = new JTextField();
		userNameJLabel.setBounds(15,175,100,25);
		userNameJTextField.setBounds(85,175,100,25);
		
		//Password Input
		//TODO Create some way to have an image pop up if they match, etc. Maybe a password strenght meter?
		passwordJPasswordField = new JPasswordField();
		passwordJLabel.setBounds(15,215,100,25);
		passwordJPasswordField.setBounds(85,215,100,25);
		
		//Confirm Password Input
		confirmpasswordJPasswordField = new JPasswordField();
		confirmPasswordJLabel.setBounds(15,255,135,25);
		confirmpasswordJPasswordField.setBounds(135,255,100,25);
		
		//Confirm and Cancel JButtons
		confirmJButton.setBounds(25,290,100,25);
		cancelJButton.setBounds(150,290,100,25);
		
		//ActionListener to make the connect menu item connect
		confirmJButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				String password = new String(passwordJPasswordField.getPassword());
				System.out.println(firstNameJTextField.getText() + lastNameJTextField.getText() + emailAddressJTextField.getText() + userNameJTextField.getText() + password);
				sendInfoToAegis(firstNameJTextField.getText(), lastNameJTextField.getText(), emailAddressJTextField.getText(), userNameJTextField.getText(), password);
				}
		});
		
		//Add all the components to the contentPane
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
		
		//Make sure we can see damn thing
		contentPane.setVisible(true);
		contentPane.setBorder(generalTitledBorder);
		
		//Let the Frame know what's up
		addUserJFrame.setContentPane(contentPane);
		addUserJFrame.setVisible(true);
	}
	
	//This Method will send all of the information over to Aegis for input into the database
	public void sendInfoToAegis(String firstName, String lastName, String emailAddress, String userName, String password) { 

		//Get a connection
		Client.connect();
		
		//Give me back my Fillet of DataOutputStream.
		DataOutputStream dout = Client.returnDOUT();
		
		try {
			//Tell the server we're not going to log in
			dout.writeUTF("Interupt");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//Invoke Client's systeMessage to tell it what we're about to do, if you know what I mean.	
		Client.systemMessage("000");
		

		
		//Send Aegis the goods
		try {
			dout.writeUTF(firstName);
			dout.writeUTF(lastName);
			dout.writeUTF(emailAddress);
			dout.writeUTF(userName);
			dout.writeUTF(password);
			Client.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
