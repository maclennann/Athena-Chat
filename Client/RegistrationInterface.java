/* Athena/Aegis Encrypted Chat Platform
 * RegistrationInterface.java: Allows users to create accounts for the service
 *
 * Copyright (C) 2010  OlympuSoft
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
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
 * Allows users to create an account
 * @author OlmypuSoft
 */
public class RegistrationInterface extends JPanel {

	private static final long serialVersionUID = -4233506965211815944L;

	//Define components
	private JFrame addUserJFrame;
	private JPanel contentPane;//, generalInformationJPanel, loginInformationJPanel;
	private JLabel firstNameJLabel = new JLabel("First name:");
	private JLabel lastNameJLabel = new JLabel("Last name:");
	private JLabel emailAddressJLabel = new JLabel("Email address:");
	private JLabel confirmEmailAddressJLabel = new JLabel("Confirm Email:");
	private JLabel emailMatchesJLabel = new JLabel();
	private JLabel emailMessageJLabel = new JLabel("Email is a required field.");
	private JLabel userNameJLabel = new JLabel("Username:");
	private JLabel userNameGreaterJLabel = new JLabel();
	private JLabel userNameMessageJLabel = new JLabel();
	private JLabel passwordJLabel = new JLabel("Password:");
	private JLabel confirmPasswordJLabel = new JLabel("Confirm Password:");
	private JLabel passwordMessageJLabel = new JLabel("Password is a required field.");
	private JLabel passwordMatchesJLabel = new JLabel();
	private JLabel secretQuestionJLabel = new JLabel("Secret Question: ");
	private JLabel secretAnswerJLabel = new JLabel("Secret Answer: ");
	private JTextField firstNameJTextField;
	private JTextField lastNameJTextField;
	private JTextField emailAddressJTextField;
	private JTextField confirmEmailAddresJTextField;
	private JTextField userNameJTextField;
	private JTextField secretQuestionJTextField;
	private JTextField secretAnswerJTextField;
	private JPasswordField passwordJPasswordField;
	private JPasswordField confirmpasswordJPasswordField;
	private JButton confirmJButton = new JButton("Confirm");
	private JButton cancelJButton = new JButton("Cancel");
	private ImageIcon redX = new ImageIcon("images/redX.png");
	private ImageIcon greenCheck = new ImageIcon("images/greenCheck.png");
	private Border blackline;
	private TitledBorder generalTitledBorder;
	private RSAPublicKeySpec pub;
	private RSAPrivateKeySpec priv;
	private BigInteger publicMod;
	private BigInteger publicExp;
	private BigInteger privateMod;
	private BigInteger privateExp;
	private BigInteger privateModBigInteger;
	private BigInteger privateExpBigInteger;
	private Color goGreen = new Color(51, 153, 51);

	RegistrationInterface() {
		//Create the Main Frame
		addUserJFrame = new JFrame("User Registration");
		addUserJFrame.setSize(310, 550);
		addUserJFrame.setResizable(false);
		addUserJFrame.setLocationRelativeTo(Athena.loginGUI);

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
		firstNameJLabel.setBounds(15, 25, 100, 25);
		firstNameJTextField.setBounds(130, 25, 120, 25);

		//Last Name Input
		lastNameJTextField = new JTextField();
		lastNameJLabel.setBounds(15, 65, 100, 25);
		lastNameJTextField.setBounds(130, 65, 120, 25);

		//Email Addres Input
		emailAddressJTextField = new JTextField();
		emailAddressJLabel.setBounds(15, 105, 100, 25);
		emailAddressJTextField.setBounds(130, 105, 120, 25);

		//Confirm Email Address Input
		confirmEmailAddresJTextField = new JTextField();
		confirmEmailAddressJLabel.setBounds(15, 145, 135, 25);
		confirmEmailAddresJTextField.setBounds(130, 145, 120, 25);
		emailMatchesJLabel.setBounds(260, 145, 25, 25);
		emailMatchesJLabel.setIcon(redX);
		emailMessageJLabel.setBounds(15, 385, 300, 25);
		emailMessageJLabel.setForeground(Color.RED);

		//Username Input
		userNameJTextField = new JTextField();
		userNameJLabel.setBounds(15, 185, 100, 25);
		userNameJTextField.setBounds(130, 185, 120, 25);
		userNameGreaterJLabel.setBounds(260, 185, 25, 25);
		userNameGreaterJLabel.setIcon(redX);
		userNameMessageJLabel.setBounds(15, 435, 300, 25);
		userNameMessageJLabel.setText("Username is a requied field.");
		userNameMessageJLabel.setForeground(Color.RED);
		userNameGreaterJLabel.setIcon(redX);


		//Password Input
		passwordJPasswordField = new JPasswordField();
		passwordJLabel.setBounds(15, 225, 100, 25);
		passwordJPasswordField.setBounds(130, 225, 120, 25);

		secretQuestionJLabel.setBounds(15, 305, 100, 25);
		secretQuestionJTextField = new JTextField();
		secretQuestionJTextField.setBounds(130, 305, 120, 25);

		secretAnswerJLabel.setBounds(15, 345, 100, 25);
		secretAnswerJTextField = new JTextField();
		secretAnswerJTextField.setBounds(130, 345, 120, 25);

		//Confirm Password Input
		confirmpasswordJPasswordField = new JPasswordField();
		confirmPasswordJLabel.setBounds(15, 265, 135, 25);
		confirmpasswordJPasswordField.setBounds(130, 265, 120, 25);
		passwordMatchesJLabel.setBounds(260, 265, 25, 25);
		passwordMatchesJLabel.setIcon(redX);
		passwordMessageJLabel.setBounds(15, 410, 400, 25);
		passwordMessageJLabel.setForeground(Color.RED);

		//Confirm and Cancel JButtons
		confirmJButton.setBounds(35, 480, 100, 25);
		cancelJButton.setBounds(160, 480, 100, 25);
		confirmJButton.setEnabled(false);

		//ActionListener to make the connect menu item connect
		confirmJButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				String password;
				try {

					//Create the DESCrypto object for buddylist and preferences cryptography
					String saltUser;
					if (userNameJTextField.getText().length() >= 8) {
						saltUser = userNameJTextField.getText().substring(0, 8);
					} else {
						saltUser = userNameJTextField.getText();
					}
					DESCrypto descrypto = new DESCrypto(passwordJPasswordField.getPassword().toString(), saltUser);

					//Hash the password
					password = AuthenticationInterface.computeHash(new String(passwordJPasswordField.getPassword()));
					String secAns = AuthenticationInterface.computeHash(secretAnswerJTextField.getText().toUpperCase());
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
					File pubKeyFile = new File("users/" + newUsername + "/keys/" + newUsername + ".pub");
					File privKeyFile = new File("users/" + newUsername + "/keys/" + newUsername + ".priv");
					if (!(pubKeyFile.exists())) {
						boolean success = new File("users/" + newUsername + "/keys").mkdirs();
						System.out.println("Created Directory");
						pubKeyFile.createNewFile();
						System.out.println("Created File");
					}
					if (!(privKeyFile.exists())) {
						boolean success = new File("users/" + newUsername + "/keys").mkdirs();
						privKeyFile.createNewFile();
					}

					File buddyList = new File("users/" + newUsername + "/buddylist.csv");
					if (!(buddyList.exists())) {
						boolean success = new File("users/" + newUsername + "/").mkdirs();
						System.out.println("Created Directory");
						buddyList.createNewFile();
						System.out.println("Created File");
					}
					privateModBigInteger = new BigInteger(descrypto.encryptData(privateMod.toString()));
					privateExpBigInteger = new BigInteger(descrypto.encryptData(privateExp.toString()));
					//Write the keys to the file
					RSACrypto.saveToFile("users/" + newUsername + "/keys/" + userNameJTextField.getText() + ".priv", privateModBigInteger, privateExpBigInteger);
					RSACrypto.saveToFile("users/" + newUsername + "/keys/" + userNameJTextField.getText() + ".pub", publicMod, publicExp);

					//System.out.println(firstNameJTextField.getText() + lastNameJTextField.getText() + emailAddressJTextField.getText() + userNameJTextField.getText() + password);
					//Send the information to Aegis
					sendInfoToAegis(firstNameJTextField.getText(), lastNameJTextField.getText(), emailAddressJTextField.getText(), userNameJTextField.getText(), password, secretQuestionJTextField.getText(), secAns);
				} catch (Exception e) {

					e.printStackTrace();
				}

			}
		});

		cancelJButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				addUserJFrame.dispose();
			}
		});

		confirmEmailAddresJTextField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {


				if (confirmEmailAddresJTextField.getText().equals(emailAddressJTextField.getText()) && confirmEmailAddresJTextField.getText().indexOf("@") > -1) {
					emailMatchesJLabel.setIcon(greenCheck);
					emailMessageJLabel.setText("The email address is valid.");
					emailMessageJLabel.setForeground(goGreen);
					confirmJButton.setEnabled(true);
				} else {
					emailMatchesJLabel.setIcon(redX);
					emailMessageJLabel.setText("You must enter a valid email address");
					emailMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {


				if (confirmEmailAddresJTextField.getText().equals(emailAddressJTextField.getText()) && confirmEmailAddresJTextField.getText().indexOf("@") > -1) {
					emailMatchesJLabel.setIcon(greenCheck);
					emailMessageJLabel.setText("The email address is valid.");
					emailMessageJLabel.setForeground(goGreen);
					confirmJButton.setEnabled(true);
				} else {
					emailMatchesJLabel.setIcon(redX);
					emailMessageJLabel.setText("You must enter a valid email address");
					emailMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				}
			}
		});

		emailAddressJTextField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (confirmEmailAddresJTextField.getText().equals(emailAddressJTextField.getText()) && confirmEmailAddresJTextField.getText().indexOf("@") > -1) {
					emailMatchesJLabel.setIcon(greenCheck);
					emailMessageJLabel.setText("The email address is valid.");
					emailMessageJLabel.setForeground(goGreen);
					confirmJButton.setEnabled(true);
				} else {
					emailMatchesJLabel.setIcon(redX);
					emailMessageJLabel.setText("You must enter a valid email address");
					emailMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (confirmEmailAddresJTextField.getText().equals(emailAddressJTextField.getText()) && confirmEmailAddresJTextField.getText().indexOf("@") > -1) {
					emailMatchesJLabel.setIcon(greenCheck);
					emailMessageJLabel.setText("The email address is valid.");
					emailMessageJLabel.setForeground(goGreen);
					confirmJButton.setEnabled(true);
				} else {
					emailMatchesJLabel.setIcon(redX);
					emailMessageJLabel.setText("You must enter a valid email address");
					emailMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				}
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
				if (passwordOne.equals(passwordTwo) && passwordOne.length() >= 6) {
					passwordMatchesJLabel.setIcon(greenCheck);
					confirmJButton.setEnabled(true);
					passwordMessageJLabel.setText("Your Password is valid.");
					passwordMessageJLabel.setForeground(goGreen);
					confirmJButton.setEnabled(true);
				} else if (passwordOne.length() == 0 || passwordTwo.length() == 0) {
					passwordMatchesJLabel.setIcon(redX);
					passwordMessageJLabel.setText("Password is a required field.");
					passwordMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				} else if ((passwordOne.length() >= 6 && passwordTwo.length() >= 6) && (!(passwordOne.equals(passwordTwo)))) {
					passwordMatchesJLabel.setIcon(redX);
					passwordMessageJLabel.setText("Passwords don't match!");
					passwordMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				} else {
					passwordMatchesJLabel.setIcon(redX);
					passwordMessageJLabel.setText("Password must be more than 5 characters.");
					passwordMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				}

			}

			@Override
			public void keyPressed(KeyEvent e) {
				String passwordOne = new String(passwordJPasswordField.getPassword());
				String passwordTwo = new String(confirmpasswordJPasswordField.getPassword());
				if (passwordOne.equals(passwordTwo) && passwordOne.length() >= 6) {
					passwordMatchesJLabel.setIcon(greenCheck);
					confirmJButton.setEnabled(true);
					passwordMessageJLabel.setText("Your Password is valid.");
					passwordMessageJLabel.setForeground(goGreen);
					confirmJButton.setEnabled(true);
				} else if (passwordOne.length() == 0 || passwordTwo.length() == 0) {
					passwordMatchesJLabel.setIcon(redX);
					passwordMessageJLabel.setText("Password is a required field.");
					passwordMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				} else if ((passwordOne.length() >= 6 && passwordTwo.length() >= 6) && (!(passwordOne.equals(passwordTwo)))) {
					passwordMatchesJLabel.setIcon(redX);
					passwordMessageJLabel.setText("Passwords don't match!");
					passwordMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				} else {
					passwordMatchesJLabel.setIcon(redX);
					passwordMessageJLabel.setText("Password must be more than 5 characters.");
					passwordMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				}
			}
		});


		userNameJTextField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (userNameJTextField.getText().length() > 25 || userNameJTextField.getText().length() == 0) {
					if (userNameJTextField.getText().length() == 0) {
						userNameMessageJLabel.setText("Username is a required field.");
					} else {
						userNameMessageJLabel.setText("Username must be less than 26 characters.");
					}
					userNameMessageJLabel.setForeground(Color.RED);
					userNameGreaterJLabel.setIcon(redX);
					confirmJButton.setEnabled(false);

				} else {
					userNameMessageJLabel.setText("Username is valid.");
					userNameMessageJLabel.setForeground(goGreen);
					userNameGreaterJLabel.setIcon(greenCheck);
					confirmJButton.setEnabled(true);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (userNameJTextField.getText().length() > 25 || userNameJTextField.getText().length() == 0) {
					if (userNameJTextField.getText().length() == 0) {
						userNameMessageJLabel.setText("Username is a required field.");
					} else {
						userNameMessageJLabel.setText("Username must be less than 26 characters.");
					}
					userNameMessageJLabel.setText("Username must be less than 26 characters.");
					userNameMessageJLabel.setForeground(Color.RED);
					userNameGreaterJLabel.setIcon(redX);
					confirmJButton.setEnabled(false);

				} else {
					userNameMessageJLabel.setText("Username is valid.");
					userNameMessageJLabel.setForeground(goGreen);
					userNameGreaterJLabel.setIcon(greenCheck);
					confirmJButton.setEnabled(true);
				}
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
				if (passwordOne.equals(passwordTwo) && passwordOne.length() >= 6) {
					passwordMatchesJLabel.setIcon(greenCheck);
					confirmJButton.setEnabled(true);
					passwordMessageJLabel.setText("Your Password is valid.");
					passwordMessageJLabel.setForeground(goGreen);
					confirmJButton.setEnabled(true);

				} else if (passwordOne.length() == 0 || passwordTwo.length() == 0) {
					passwordMatchesJLabel.setIcon(redX);
					passwordMessageJLabel.setText("Password is a required field.");
					passwordMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				} else if ((passwordOne.length() >= 6 && passwordTwo.length() >= 6) && (!(passwordOne.equals(passwordTwo)))) {
					passwordMatchesJLabel.setIcon(redX);
					passwordMessageJLabel.setText("Passwords don't match!");
					passwordMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				} else {
					passwordMatchesJLabel.setIcon(redX);
					passwordMessageJLabel.setText("Password must be more than 5 characters.");
					passwordMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				String passwordOne = new String(passwordJPasswordField.getPassword());
				String passwordTwo = new String(confirmpasswordJPasswordField.getPassword());
				if (passwordOne.equals(passwordTwo) && passwordOne.length() >= 6) {
					passwordMatchesJLabel.setIcon(greenCheck);
					confirmJButton.setEnabled(true);
					passwordMessageJLabel.setText("Your Password is valid.");
					passwordMessageJLabel.setForeground(goGreen);
					confirmJButton.setEnabled(true);
				} else if (passwordOne.length() == 0 || passwordTwo.length() == 0) {
					passwordMatchesJLabel.setIcon(redX);
					passwordMessageJLabel.setText("Password is a required field.");
					passwordMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				} else if ((passwordOne.length() >= 6 && passwordTwo.length() >= 6) && (!(passwordOne.equals(passwordTwo)))) {
					passwordMatchesJLabel.setIcon(redX);
					passwordMessageJLabel.setText("Passwords don't match!");
					passwordMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				} else {
					passwordMatchesJLabel.setIcon(redX);
					passwordMessageJLabel.setText("Password must be more than 5 characters.");
					passwordMessageJLabel.setForeground(Color.RED);
					confirmJButton.setEnabled(false);
				}
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
		contentPane.add(emailMessageJLabel);
		contentPane.add(passwordMessageJLabel);
		contentPane.add(secretQuestionJLabel);
		contentPane.add(secretQuestionJTextField);
		contentPane.add(secretAnswerJLabel);
		contentPane.add(secretAnswerJTextField);
		contentPane.add(userNameGreaterJLabel);
		contentPane.add(userNameMessageJLabel);

		//Make sure we can see damn thing
		contentPane.setVisible(true);
		contentPane.setBorder(generalTitledBorder);

		//Let the Frame know what's up
		addUserJFrame.setContentPane(contentPane);
		addUserJFrame.setVisible(true);
	}

	/**
	 * This Method will send all of the information over to Aegis for input into the database
	 * @param firstName User's first name
	 * @param lastName User's last name
	 * @param emailAddress User's email address
	 * @param userName User's desired username
	 * @param password User's password
	 * @param secretQuestion User's secret Question
	 * @param secretAnswer User's secret answer
	 */
	public void sendInfoToAegis(String firstName, String lastName, String emailAddress, String userName, String password, String secretQuestion, String secretAnswer) {

		//Get a connection
		Athena.connect();

		//Give me back my filet of DataOutputStream + DataInputStream
		DataOutputStream dout = Athena.returnDOUT();
		DataInputStream din = Athena.returnDIN();


		try {
			//Tell the server we're not going to log in
			//Maybe we should try encrypting this first!
			//dout.writeUTF("Interupt");
			dout.writeUTF(new BigInteger(RSACrypto.rsaEncryptPublic("Interupt", Athena.serverPublic.getModulus(), Athena.serverPublic.getPublicExponent())).toString());
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		//Invoke Client's systemMessage to tell it what we're about to do, if you know what I mean.	
		Athena.systemMessage("000");



		//Send Aegis the goods
		try {
			//Encrypt information to send to Aegis. Turn them into BigIntegers so we can move them
			BigInteger firstNameCipher = new BigInteger(RSACrypto.rsaEncryptPublic(firstName, Athena.serverPublic.getModulus(), Athena.serverPublic.getPublicExponent()));
			BigInteger lastNameCipher = new BigInteger(RSACrypto.rsaEncryptPublic(lastName, Athena.serverPublic.getModulus(), Athena.serverPublic.getPublicExponent()));
			BigInteger emailAddressCipher = new BigInteger(RSACrypto.rsaEncryptPublic(emailAddress, Athena.serverPublic.getModulus(), Athena.serverPublic.getPublicExponent()));
			BigInteger userNameCipher = new BigInteger(RSACrypto.rsaEncryptPublic(userName, Athena.serverPublic.getModulus(), Athena.serverPublic.getPublicExponent()));
			BigInteger passwordCipher = new BigInteger(RSACrypto.rsaEncryptPublic(password, Athena.serverPublic.getModulus(), Athena.serverPublic.getPublicExponent()));


			//Send the server the pieces of our public key to be assembled at the other end
			//For this test the server needs these numbers to decrypt things
			dout.writeUTF(publicMod.toString());
			dout.writeUTF(publicExp.toString());

			//Send the server the pieces of our encrypted private key to write to a file
			dout.writeUTF(privateModBigInteger.toString());
			dout.writeUTF(privateExpBigInteger.toString());

			//Create the DESCrypto object for the private key sync
			String saltUser;
			if (userName.length() >= 8) {
				saltUser = userName.substring(0, 8);
			} else {
				saltUser = userName;
			}
			DESCrypto descrypto = new DESCrypto(password, saltUser);

			//Turn the encrypted data into numbers for
			//BigInteger firstNameNumber = new BigInteger(firstNameCipher);

			//Send encrypted data to Aegis
			dout.writeUTF(firstNameCipher.toString());
			dout.writeUTF(lastNameCipher.toString());
			dout.writeUTF(emailAddressCipher.toString());
			dout.writeUTF(userNameCipher.toString());
			dout.writeUTF(passwordCipher.toString());
			dout.writeUTF(Athena.encryptServerPublic(secretQuestion));
			dout.writeUTF(Athena.encryptServerPublic(secretAnswer));

			//Grab the result
			String result = din.readUTF();
			byte[] resultBytes = (new BigInteger(result)).toByteArray();
			String resultDecrypted = RSACrypto.rsaDecryptPublic(resultBytes, Athena.serverPublic.getModulus(), Athena.serverPublic.getPublicExponent());
			if (resultDecrypted.equals("Account has been successfully created.")) {
				LoginFailedInterface successfulUserRegistration = new LoginFailedInterface(resultDecrypted, true);
				addUserJFrame.dispose();
				//Garbage collect!
				System.gc();
			} else {
				LoginFailedInterface failureUserRegistration = new LoginFailedInterface(resultDecrypted, false);
				//Garbage collect!
				System.gc();
			}
			//Close the connection
			dout.close();
			Athena.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
