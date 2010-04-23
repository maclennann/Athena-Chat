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
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Toolkit;

/**
 * 
 */

/**
 * @author OlmypuSoft
 *
 */
public class ClientBugReport extends JPanel {


	/**
	 * 
	 */
	//Define components
	public JFrame submitBugJFrame;
	public JPanel contentPane, generalInformationJPanel, loginInformationJPanel;
	public JLabel descriptionJLabel = new JLabel("Brief Description:");
	public JLabel recreationJLabel = new JLabel("How can we recreate this bug?");
	public JLabel expectedJLabel = new JLabel("What did you expect to happen?");
	public JLabel actualJLabel = new JLabel("What actually happened?");
	
	public JTextField descriptionJTextField = new JTextField();
	public JTextArea recreationJTextArea;
	public JTextArea expectedJTextArea;
	public JTextArea actualJTextArea;

	public JButton confirmJButton = new JButton("Confirm");
	public JButton cancelJButton = new JButton("Cancel");
	public JButton clearJButton = new JButton("Clear");

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

	ClientBugReport() {
		//Create the Main Frame
		submitBugJFrame= new JFrame("Submit Bug Report");
		submitBugJFrame.setSize(500,550);
		submitBugJFrame.setResizable(false);
		submitBugJFrame.setLocationRelativeTo(ClientApplet.imContentFrame);
		
		//Create the content Pane
		contentPane = new JPanel();
		contentPane.setLayout(null);
		contentPane.setVisible(true);
		
		submitBugJFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("../images/logosmall.png"));
		
		//Initalize borders
		blackline = BorderFactory.createLineBorder(Color.black);
		generalTitledBorder = BorderFactory.createTitledBorder(
				blackline, "Submit a Bug Report");

		//Username Input
		descriptionJTextField = new JTextField();
		descriptionJLabel.setBounds(15,20,100,25);
		descriptionJTextField.setBounds(15,40,470,25);

		recreationJLabel.setBounds(15,80,400,25);
		recreationJTextArea = new JTextArea();
		JScrollPane recreationSP = new JScrollPane(recreationJTextArea);
		recreationSP.setBounds(15,100,470,100);
		recreationSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		expectedJLabel.setBounds(15,210,400,25);
		expectedJTextArea = new JTextArea();
		JScrollPane expectedSP = new JScrollPane(expectedJTextArea);
		expectedSP.setBounds(15,230,470,100);
		expectedSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		actualJLabel.setBounds(15,340,400,25);
		actualJTextArea = new JTextArea();
		JScrollPane actualSP = new JScrollPane(actualJTextArea);
		actualSP.setBounds(15,360,470,100);
		actualSP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		//Secret answer nput
		//TODO Create some way to have an image pop up if they match, etc. Maybe a password strenght meter?
		
		
		
		
		


		//Confirm and Cancel JButtons
		confirmJButton.setBounds(280,490,100,25);
		cancelJButton.setBounds(385,490,100,25);
		clearJButton.setBounds(10,490,100,25);

		
		clearJButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
					recreationJTextArea.setText("");
					descriptionJTextField.setText("");
					expectedJTextArea.setText("");
					actualJTextArea.setText("");
				}
		});
		
		//ActionListener to make the connect menu item connect
		confirmJButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
					sendInfoToAegis(descriptionJTextField.getText(),recreationJTextArea.getText(),expectedJTextArea.getText(),actualJTextArea.getText());
					JOptionPane.showMessageDialog(null,"Thank you for submitting this report. It has been added to our database","Thanks!",JOptionPane.INFORMATION_MESSAGE);
					submitBugJFrame.dispose();
			}
		});

		cancelJButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				submitBugJFrame.dispose();
			} 
		});
		
		//Add all the components to the contentPane
		contentPane.add(descriptionJLabel);
		contentPane.add(descriptionJTextField);
		contentPane.add(recreationJLabel);
		contentPane.add(recreationSP);
		contentPane.add(cancelJButton);
		contentPane.add(expectedJLabel);
		contentPane.add(expectedSP);
		contentPane.add(actualJLabel);
		contentPane.add(actualSP);
		contentPane.add(confirmJButton);
		contentPane.add(clearJButton);

		//Make sure we can see damn thing
		contentPane.setVisible(true);
		contentPane.setBorder(generalTitledBorder);

		//Let the Frame know what's up
		submitBugJFrame.setContentPane(contentPane);
		submitBugJFrame.setVisible(true);
	}

	//This Method will send all of the information over to Aegis for input into the database
	public void sendInfoToAegis(String titles, String recreates, String expecteds, String actuals) { 

		//Get a connection
		//Client.connect();

		//Give me back my filet of DataOutputStream + DataInputStream
		DataOutputStream dout = Client.returnDOUT();
		//DataInputStream din = Client.returnDIN();


		try {
			Client.systemMessage("10");
			dout.writeUTF(Client.encryptServerPublic(titles));
			dout.writeUTF(Client.encryptServerPublic(recreates));
			dout.writeUTF(Client.encryptServerPublic(expecteds));
			dout.writeUTF(Client.encryptServerPublic(actuals));
			//Close the connection
			//dout.close();
			//Client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}