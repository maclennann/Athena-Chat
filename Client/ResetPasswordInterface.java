/* Athena/Aegis Encrypted Chat Platform
 * ResetPasswordInterface.java: Allows user to reset forgotten password
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
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.Toolkit;
import java.awt.AWTException;

/**
 * Window where user's reset password
 * @author OlmypuSoft
 */
public class ResetPasswordInterface extends JPanel {
	//Define components
	private JFrame resetPasswordJFrame;
	private JPanel contentPane;
	private JLabel userNameJLabel = new JLabel("Username:");
	private JLabel secretAnswerJLabel = new JLabel("Secret Answer:");
	private JLabel secretQuestionJLabel = new JLabel("Secret Question:");
	private JTextField secretQuestionJTextField = new JTextField();
	private JTextField userNameJTextField;
	private JTextField secretAnswerJTextField;
	private JButton getQuestJButton = new JButton("Get Question");
	private JButton confirmJButton = new JButton("Confirm");
	private JButton cancelJButton = new JButton("Cancel");
	private JButton clearJButton = new JButton("Clear");
	private JLabel newPasswordJLabel = new JLabel("New Password: ");
	private JLabel newPasswordConfirmJLabel = new JLabel("Confirm:");
	private JPasswordField newPasswordJPasswordField = new JPasswordField();
	private JPasswordField newPasswordConfirmJPasswordField = new JPasswordField();
	private Border blackline;
	private TitledBorder generalTitledBorder;

	ResetPasswordInterface() {
		//Create the Main Frame
		resetPasswordJFrame = new JFrame("Reset Password");
		resetPasswordJFrame.setSize(430, 250);
		resetPasswordJFrame.setResizable(false);
		resetPasswordJFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("images/logosmall.png"));
		resetPasswordJFrame.setLocationRelativeTo(Athena.loginGUI);
		//Create the content Pane
		contentPane = new JPanel();
		contentPane.setLayout(null);
		contentPane.setVisible(true);

		//Initalize borders
		blackline = BorderFactory.createLineBorder(Color.black);
		generalTitledBorder = BorderFactory.createTitledBorder(
				blackline, "Reset Password");

		//Username Input
		userNameJTextField = new JTextField();
		userNameJLabel.setBounds(15, 20, 100, 25);
		userNameJTextField.setBounds(130, 20, 150, 25);

		//Secret answer nput
		secretQuestionJTextField = new JTextField();
		secretQuestionJTextField.setEditable(false);
		secretQuestionJTextField.setBounds(130, 55, 280, 25);
		secretQuestionJLabel.setBounds(15, 55, 120, 25);
		secretAnswerJTextField = new JTextField();
		secretAnswerJLabel.setBounds(15, 90, 100, 25);
		secretAnswerJTextField.setBounds(130, 90, 280, 25);
		secretAnswerJTextField.setEditable(false);
		newPasswordJLabel.setBounds(15, 125, 100, 25);
		newPasswordJPasswordField.setBounds(130, 125, 280, 25);
		newPasswordConfirmJLabel.setBounds(15, 160, 100, 25);
		newPasswordConfirmJPasswordField.setBounds(130, 160, 280, 25);
		newPasswordJPasswordField.setEditable(false);
		newPasswordConfirmJPasswordField.setEditable(false);

		//Confirm and Cancel JButtons
		confirmJButton.setBounds(200, 190, 100, 25);
		getQuestJButton.setBounds(290, 20, 120, 25);
		cancelJButton.setBounds(310, 190, 100, 25);
		clearJButton.setBounds(10, 190, 100, 25);
		confirmJButton.setEnabled(false);
		getQuestJButton.setEnabled(true);

		resetPasswordJFrame.addWindowListener(new java.awt.event.WindowAdapter() {

			public void windowClosing(WindowEvent winEvt) {
				try {
					//Client.loginGUI.dispose();
					AuthenticationInterface loginGUI = new AuthenticationInterface();
					resetPasswordJFrame.dispose();
				} catch (AWTException e) {

					e.printStackTrace();
				}
			}
		});
		clearJButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				confirmJButton.setEnabled(false);
				getQuestJButton.setEnabled(true);
				userNameJTextField.setEditable(true);
				secretQuestionJTextField.setText("");
				userNameJTextField.setText("");
				secretAnswerJTextField.setText("");
				secretAnswerJTextField.setEditable(false);
				newPasswordJPasswordField.setEditable(false);
				newPasswordConfirmJPasswordField.setEditable(false);
				newPasswordConfirmJPasswordField.setText("");
				newPasswordJPasswordField.setText("");
			}
		});


		getQuestJButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				if (userNameJTextField.getText().equals("")) {
					JOptionPane.showMessageDialog(null, "Please enter a username", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					confirmJButton.setEnabled(true);
					getQuestJButton.setEnabled(false);
					userNameJTextField.setEditable(false);

					secretQuestionJTextField.setText(getQuestion(userNameJTextField.getText()));
					secretAnswerJTextField.setEditable(true);
					newPasswordConfirmJPasswordField.setEditable(true);
					newPasswordJPasswordField.setEditable(true);
				}
			}
		});

		//ActionListener to make the connect menu item connect
		confirmJButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				try {
					System.out.println("Confirm Clicked!");
					//String password;
					if (secretAnswerJTextField.getText().equals("")) {
						JOptionPane.showMessageDialog(null, "Please enter a secret answer", "Error", JOptionPane.ERROR_MESSAGE);
					} else {
						String pass1 = new String(newPasswordJPasswordField.getPassword());
						String pass2 = new String(newPasswordConfirmJPasswordField.getPassword());
						if (pass1.equals(pass2)) {
							System.out.println("Initiating password change");
							int yes = changePassword(AuthenticationInterface.computeHash(secretAnswerJTextField.getText().toUpperCase()), AuthenticationInterface.computeHash(pass1));
							if (yes == 1) {
								JOptionPane.showMessageDialog(null, "You've successfully reset your password.", "Success!", JOptionPane.INFORMATION_MESSAGE);
							} else {
								JOptionPane.showMessageDialog(null, "Please try again.", "Error!", JOptionPane.ERROR_MESSAGE);
							}
						} else {
							System.out.println("Password mismatcH");
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		cancelJButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new AuthenticationInterface();
				} catch (Exception ie) {
					ie.printStackTrace();
				}

				resetPasswordJFrame.dispose();
			}
		});

		//Add all the components to the contentPane
		contentPane.add(userNameJLabel);
		contentPane.add(userNameJTextField);
		contentPane.add(secretAnswerJLabel);
		contentPane.add(secretAnswerJTextField);
		contentPane.add(getQuestJButton);
		contentPane.add(cancelJButton);
		contentPane.add(secretQuestionJLabel);
		contentPane.add(secretQuestionJTextField);
		contentPane.add(confirmJButton);
		contentPane.add(clearJButton);
		contentPane.add(newPasswordJLabel);
		contentPane.add(newPasswordJPasswordField);
		contentPane.add(newPasswordConfirmJLabel);
		contentPane.add(newPasswordConfirmJPasswordField);

		//Make sure we can see damn thing
		contentPane.setVisible(true);
		contentPane.setBorder(generalTitledBorder);

		//Let the Frame know what's up
		resetPasswordJFrame.setContentPane(contentPane);
		resetPasswordJFrame.setVisible(true);
	}

	/**
	 * Get the secret question from the server
	 * @param userToReset Username to get the secret question for
	 * @return The secret question returned from the server
	 */
	public String getQuestion(String userToReset) {

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
		System.out.println("sent 11");
		Athena.systemMessage("11");



		//Send Aegis the goods
		try {
			dout.writeUTF(Athena.encryptServerPublic(userToReset));
			System.out.println("sent username " + userToReset);
			String question = Athena.decryptServerPublic(din.readUTF());
			return (question);
			//Client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Change the user's password if his secret answer is right
	 * @param answerHash Hash of his secret answer
	 * @param passwordHash Hash of his new password
	 * @return
	 */
	public int changePassword(String answerHash, String passwordHash) {
		try {
			DataOutputStream dout = Athena.returnDOUT();
			DataInputStream din = Athena.returnDIN();

			dout.writeUTF(Athena.encryptServerPublic(answerHash));
			System.out.println("Sent answerhash");
			dout.writeUTF(Athena.encryptServerPublic(passwordHash));
			System.out.println("Sent passwordhahh");
			String success = Athena.decryptServerPublic(din.readUTF());
			System.out.println("Result: " + success);
			//Client.disconnect();
			return Integer.parseInt(success);

		} catch (Exception e) {
			e.printStackTrace();
			Athena.disconnect();
			return 0;
		}
	}
}
