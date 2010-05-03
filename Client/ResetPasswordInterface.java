import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import javax.swing.JOptionPane;
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
import java.awt.Toolkit;
import java.awt.AWTException;

/**
 * 
 */

/**
 * @author OlmypuSoft
 *
 */
public class ResetPasswordInterface extends JPanel {


	/**
	 * 
	 */
	//Define components
	public JFrame resetPasswordJFrame;
	public JPanel contentPane, generalInformationJPanel, loginInformationJPanel;
	public JLabel userNameJLabel = new JLabel("Username:");
	public JLabel secretAnswerJLabel = new JLabel("Secret Answer:");
	public JLabel secretQuestionJLabel = new JLabel("Secret Question:");
	public JTextField secretQuestionJTextField = new JTextField();
	public JTextField userNameJTextField;
	public JTextField secretAnswerJTextField;
	public JButton getQuestJButton = new JButton("Get Question");
	public JButton confirmJButton = new JButton("Confirm");
	public JButton cancelJButton = new JButton("Cancel");
	public JButton clearJButton = new JButton("Clear");
	public ImageIcon redX = new ImageIcon("images/redX.png");
	public ImageIcon greenCheck = new ImageIcon("images/greenCheck.png");
	public JLabel newPasswordJLabel = new JLabel("New Password: ");
	public JLabel newPasswordConfirmJLabel = new JLabel("Confirm:");
	public JPasswordField newPasswordJPasswordField = new JPasswordField();
	public JPasswordField newPasswordConfirmJPasswordField = new JPasswordField();
	
	public Border blackline;
	public TitledBorder generalTitledBorder;
	public RSAPublicKeySpec pub;
	public RSAPrivateKeySpec priv;
	public BigInteger publicMod;
	public BigInteger publicExp;
	public BigInteger privateMod;
	public BigInteger privateExp;
	private BigInteger privateModBigInteger;
	private BigInteger privateExpBigInteger;
	public Color goGreen = new Color(51,153,51);
	ResetPasswordInterface() {
		//Create the Main Frame
		resetPasswordJFrame= new JFrame("Reset Password");
		resetPasswordJFrame.setSize(430,250);
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
		userNameJLabel.setBounds(15,20,100,25);
		userNameJTextField.setBounds(130,20,150,25);

		//Secret answer nput
		//TODO Create some way to have an image pop up if they match, etc. Maybe a password strenght meter?
		secretQuestionJTextField = new JTextField();
		secretQuestionJTextField.setEditable(false);
		secretQuestionJTextField.setBounds(130,55,280,25);
		secretQuestionJLabel.setBounds(15,55,120,25);
		secretAnswerJTextField = new JTextField();
		secretAnswerJLabel.setBounds(15,90,100,25);
		secretAnswerJTextField.setBounds(130,90,280,25);
		secretAnswerJTextField.setEditable(false);
		newPasswordJLabel.setBounds(15,125,100,25);
		newPasswordJPasswordField.setBounds(130,125,280,25);
		newPasswordConfirmJLabel.setBounds(15,160,100,25);
		newPasswordConfirmJPasswordField.setBounds(130,160,280,25);
		newPasswordJPasswordField.setEditable(false);
		newPasswordConfirmJPasswordField.setEditable(false);
		
		//Confirm and Cancel JButtons
		confirmJButton.setBounds(200,190,100,25);
		getQuestJButton.setBounds(290,20,120,25);
		cancelJButton.setBounds(310,190,100,25);
		clearJButton.setBounds(10,190,100,25);
		confirmJButton.setEnabled(false);
		getQuestJButton.setEnabled(true);

		resetPasswordJFrame.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(WindowEvent winEvt) {
		        try {
					//Client.loginGUI.dispose();
					AuthenticationInterface loginGUI = new AuthenticationInterface();
					resetPasswordJFrame.dispose();
				} catch (AWTException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
			
		    }
		);	
		clearJButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
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
			public void actionPerformed(ActionEvent event){
				if(userNameJTextField.getText().equals("")){
					JOptionPane.showMessageDialog(null,"Please enter a username","Error",JOptionPane.ERROR_MESSAGE);
				}else{
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
			public void actionPerformed(ActionEvent event){
			try{
				System.out.println("Confirm Clicked!");
				//String password;
				if(secretAnswerJTextField.getText().equals("")){
					JOptionPane.showMessageDialog(null,"Please enter a secret answer","Error",JOptionPane.ERROR_MESSAGE);
				}
				else{
						String pass1 = new String(newPasswordJPasswordField.getPassword());
						String pass2 = new String(newPasswordConfirmJPasswordField.getPassword());
						if(pass1.equals(pass2)){
							System.out.println("Initiating password change");
							int yes = changePassword(AuthenticationInterface.computeHash(secretAnswerJTextField.getText().toUpperCase()), AuthenticationInterface.computeHash(pass1));
							if(yes == 1){
								JOptionPane.showMessageDialog(null,"You've successfully reset your password.","Success!",JOptionPane.INFORMATION_MESSAGE);
							}else{
								JOptionPane.showMessageDialog(null,"Please try again.","Error!",JOptionPane.ERROR_MESSAGE);
							}
						}else{System.out.println("Password mismatcH");}
					}
					
				}catch(Exception e){e.printStackTrace();}
					/*
					//Create the DESCrypto object for buddylist and preferences cryptography
					String saltUser;
					if(userNameJTextField.getText().length()>=8){
						saltUser = userNameJTextField.getText().substring(0,8);
					}else saltUser = userNameJTextField.getText();
					DESCrypto descrypto = new DESCrypto(passwordJPasswordField.getPassword().toString(),saltUser);
										
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

					File buddyList = new File("users/"+newUsername+"/buddylist.csv");
					if(!(buddyList.exists())){
						boolean success = new File("users/"+newUsername + "/").mkdirs();
						System.out.println("Created Directory");
						buddyList.createNewFile();
						System.out.println("Created File");
					}
					privateModBigInteger = new BigInteger(descrypto.encryptData(privateMod.toString()));
					privateExpBigInteger = new BigInteger(descrypto.encryptData(privateExp.toString()));
					//Write the keys to the file
					RSACrypto.saveToFile("users/"+newUsername+"/keys/"+userNameJTextField.getText()+".priv",privateModBigInteger,privateExpBigInteger);
					RSACrypto.saveToFile("users/"+newUsername+"/keys/"+userNameJTextField.getText()+".pub",publicMod,publicExp);

					//System.out.println(firstNameJTextField.getText() + lastNameJTextField.getText() + emailAddressJTextField.getText() + userNameJTextField.getText() + password);
					//Send the information to Aegis
					//sendInfoToAegis(firstNameJTextField.getText(), lastNameJTextField.getText(), emailAddressJTextField.getText(), userNameJTextField.getText(), password);*/
			//	} catch (Exception e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				//}

			}
		});

		cancelJButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					new AuthenticationInterface();
				}catch(Exception ie){ie.printStackTrace();}
				// TODO Auto-generated method stub
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

	//This Method will send all of the information over to Aegis for input into the database
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
			dout.writeUTF(new BigInteger(RSACrypto.rsaEncryptPublic((new String("Interupt")),Athena.serverPublic.getModulus(),Athena.serverPublic.getPublicExponent())).toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
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
	
	public int changePassword(String answerHash, String passwordHash){
		try{
			DataOutputStream dout = Athena.returnDOUT();
			DataInputStream din = Athena.returnDIN();
			
			dout.writeUTF(Athena.encryptServerPublic(answerHash));
			System.out.println("Sent answerhash");
			dout.writeUTF(Athena.encryptServerPublic(passwordHash));
			System.out.println("Sent passwordhahh");
			String success = Athena.decryptServerPublic(din.readUTF());
			System.out.println("Result: "+success);
			//Client.disconnect();
			return Integer.parseInt(success);
			
		}catch(Exception e){e.printStackTrace();Athena.disconnect();return 0;}
	}

}