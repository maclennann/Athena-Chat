import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4233506965211815944L;
	//Define components
	public JFrame addUserJFrame;
	public JPanel contentPane, generalInformationJPanel, loginInformationJPanel;
	public JLabel firstNameJLabel = new JLabel("First name:");
	public JLabel lastNameJLabel = new JLabel("Last name:");
	public JLabel emailAddressJLabel = new JLabel("Email address:");
	public JLabel confirmEmailAddressJLabel = new JLabel("Confirm Email address:");
	public JLabel emailMatchesJLabel = new JLabel();
	public JLabel userNameJLabel = new JLabel("Username:");
	public JLabel passwordJLabel = new JLabel("Password:");
	public JLabel confirmPasswordJLabel = new JLabel("Confirm Password:");
	public JLabel passwordMatchesJLabel = new JLabel();
	public JTextField firstNameJTextField;
	public JTextField lastNameJTextField;
	public JTextField emailAddressJTextField;
	public JTextField confirmEmailAddresJTextField;
	public JTextField userNameJTextField;
	public JPasswordField passwordJPasswordField;
	public JPasswordField confirmpasswordJPasswordField;
	public JButton confirmJButton = new JButton("Confirm");
	public JButton cancelJButton = new JButton("Cancel");
	public ImageIcon redX = new ImageIcon("../images/redX.png");
	public ImageIcon greenCheck = new ImageIcon("../images/greenCheck.png");
	
	public Border blackline;
	public TitledBorder generalTitledBorder;
	public RSAPublicKeySpec pub;
	public RSAPrivateKeySpec priv;
	public BigInteger publicMod;
	public BigInteger publicExp;
	public BigInteger privateMod;
	public BigInteger privateExp;
	
	ClientAddUser() {
		//Create the Main Frame
		addUserJFrame= new JFrame("User Registration");
		addUserJFrame.setSize(400,375);
		addUserJFrame.setResizable(true);
		
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
		emailMatchesJLabel.setBounds(260,135,25,25);
		emailMatchesJLabel.setIcon(redX);
	
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
		passwordMatchesJLabel.setBounds(245,255,25,25);
		passwordMatchesJLabel.setIcon(redX);
		
		//Confirm and Cancel JButtons
		confirmJButton.setBounds(25,290,100,25);
		cancelJButton.setBounds(150,290,100,25);
		confirmJButton.setEnabled(false);
		
		//ActionListener to make the connect menu item connect
		confirmJButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				String password;
				try {
					//Hash the password
					password = ClientLogin.computeHash(new String(passwordJPasswordField.getPassword()));
					
					//Generate the public and private keypair
					RSACrypto.generateRSAKeyPair();
					pub = RSACrypto.getPublicKey();
					priv = RSACrypto.getPrivateKey();
					
					String newUsername = userNameJTextField.getText();
					
					//Pull out the key components
					publicMod = pub.getModulus();
					publicExp = pub.getPublicExponent();
					privateMod = priv.getModulus();
					privateExp = priv.getPrivateExponent();
					File pubKeyFile = new File("users/"+newUsername+"/keys/"+newUsername+".pub");
					File privKeyFile = new File("users/"+newUsername+"/keys/"+newUsername+".priv");
					if(!(pubKeyFile.exists())){
						boolean success = new File("users/"+newUsername+"/keys").mkdirs();
							System.out.println("Created Directory");
							pubKeyFile.createNewFile();
							System.out.println("Created File");
							}
					if(!(privKeyFile.exists())){
						boolean success = new File("users/"+newUsername+"/keys").mkdirs();
							privKeyFile.createNewFile();
					}
					
					//Write the keys to the file
					RSACrypto.saveToFile("users/"+newUsername+"/keys/"+userNameJTextField.getText()+".pub",publicMod,publicExp);
					RSACrypto.saveToFile("users/"+newUsername+"/keys/"+userNameJTextField.getText()+".priv",privateMod,privateExp);
					
					//System.out.println(firstNameJTextField.getText() + lastNameJTextField.getText() + emailAddressJTextField.getText() + userNameJTextField.getText() + password);
					//Send the information to Aegis
					sendInfoToAegis(firstNameJTextField.getText(), lastNameJTextField.getText(), emailAddressJTextField.getText(), userNameJTextField.getText(), password);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				}
		});
		
		confirmEmailAddresJTextField.addKeyListener(new KeyListener() {			
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				if(confirmEmailAddresJTextField.getText().equals(emailAddressJTextField.getText()) && confirmEmailAddresJTextField.getText().indexOf("@") > -1) { 
					emailMatchesJLabel.setIcon(greenCheck);
				} else { 
					emailMatchesJLabel.setIcon(redX);
				}				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
			}
		});
		
		emailAddressJTextField.addKeyListener(new KeyListener() {			
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				if(confirmEmailAddresJTextField.getText().equals(emailAddressJTextField.getText()) && confirmEmailAddresJTextField.getText().indexOf("@") > -1) { 
					emailMatchesJLabel.setIcon(greenCheck);
				} else { 
					emailMatchesJLabel.setIcon(redX);
				}				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				
			}
		});
		
		passwordJPasswordField.addKeyListener(new KeyListener() {			
			@Override
			public void keyTyped(KeyEvent e) {
		
			}
		
			@Override
			public void keyReleased(KeyEvent e) {
				String passwordOne = new String(passwordJPasswordField.getPassword());
				String passwordTwo = new String(confirmpasswordJPasswordField.getPassword());
				if(passwordOne.equals(passwordTwo) && passwordOne.length() >= 6) { 
					passwordMatchesJLabel.setIcon(greenCheck);
					confirmJButton.setEnabled(true);
				} else { 
					passwordMatchesJLabel.setIcon(redX);					
				}	
			
			}
			
	
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		
		confirmpasswordJPasswordField.addKeyListener(new KeyListener() {			
			@Override
			public void keyTyped(KeyEvent e) {
				
			}
		
			@Override
			public void keyReleased(KeyEvent e) {
				String passwordOne = new String(passwordJPasswordField.getPassword());
				String passwordTwo = new String(confirmpasswordJPasswordField.getPassword());
				if(passwordOne.equals(passwordTwo) && passwordOne.length() >= 6) { 
					passwordMatchesJLabel.setIcon(greenCheck);
					confirmJButton.setEnabled(true);
				} else { 
					passwordMatchesJLabel.setIcon(redX);					
				}	
			
			}
			
	
			@Override
			public void keyPressed(KeyEvent e) {
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
		contentPane.add(emailMatchesJLabel);
		contentPane.add(userNameJLabel);
		contentPane.add(userNameJTextField);
		contentPane.add(passwordJLabel);
		contentPane.add(passwordJPasswordField);
		contentPane.add(passwordMatchesJLabel);
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
		
		//Give me back my filet of DataOutputStream.
		DataOutputStream dout = Client.returnDOUT();
		
		try {
			//Tell the server we're not going to log in
			//Maybe we should try encrypting this first!
			//dout.writeUTF("Interupt");
			dout.writeUTF(new BigInteger(RSACrypto.rsaEncryptPublic((new String("Interupt")),Client.serverPublic.getModulus(),Client.serverPublic.getPublicExponent())).toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//Invoke Client's systemMessage to tell it what we're about to do, if you know what I mean.	
		Client.systemMessage("000");
		

		
		//Send Aegis the goods
		try {
			//Encrypt information to send to Aegis. Turn them into BigIntegers so we can move them
			//TODO These should be encrypted with Aegis' public key
			BigInteger firstNameCipher = new BigInteger(RSACrypto.rsaEncryptPublic(firstName,Client.serverPublic.getModulus(),Client.serverPublic.getPublicExponent()));
			BigInteger lastNameCipher = new BigInteger(RSACrypto.rsaEncryptPublic(lastName,Client.serverPublic.getModulus(),Client.serverPublic.getPublicExponent()));
			BigInteger emailAddressCipher = new BigInteger(RSACrypto.rsaEncryptPublic(emailAddress,Client.serverPublic.getModulus(),Client.serverPublic.getPublicExponent()));
			BigInteger userNameCipher = new BigInteger(RSACrypto.rsaEncryptPublic(userName,Client.serverPublic.getModulus(),Client.serverPublic.getPublicExponent()));
			BigInteger passwordCipher = new BigInteger(RSACrypto.rsaEncryptPublic(password,Client.serverPublic.getModulus(),Client.serverPublic.getPublicExponent()));
			
			//Send the server the pieces of our public key to be assembled at the other end
			//TODO These should be encrypted along with everything else
			//For this test the server needs these numbers to decrypt things
			dout.writeUTF(publicMod.toString());
			dout.writeUTF(publicExp.toString());
			
			//Turn the encrypted data into numbers for
			//BigInteger firstNameNumber = new BigInteger(firstNameCipher);
			
			//Send encrypted data to Aegis
			dout.writeUTF(firstNameCipher.toString());
			dout.writeUTF(lastNameCipher.toString());
			dout.writeUTF(emailAddressCipher.toString());
			dout.writeUTF(userNameCipher.toString());
			dout.writeUTF(passwordCipher.toString());
			
			//Test decryption
			//String firstNamePlain = RSACrypto.rsaDecryptPublic(firstNameCipher,pub.getModulus(),pub.getPublicExponent());
			
			//Close the connection
			dout.close();
			Client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
