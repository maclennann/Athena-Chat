/* Athena/Aegis Encrypted Chat Platform
 * BugReportInterface.java: Allows users to submit bug reports and feature requests.
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
import java.io.DataOutputStream;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Toolkit;

/**
 * Submit a bug report or feature request to Aegis
 * @author OlmypuSoft
 */
public class SendEmailInterface extends JPanel {

	/**
	 *
	 */
	//Define components
	private JFrame sendEmailJFrame;
	private JPanel contentPane;
	private JLabel toJLabel = new JLabel("TO:");
	private JLabel reJLabel = new JLabel("RE:");
	private JLabel bodyJLabel = new JLabel("Body:");
	private JTextField toJTextField = new JTextField();
	private JTextField reJTextField;
	private JTextArea bodyJTextArea;
	private JButton confirmJButton = new JButton("Confirm");
	private JButton cancelJButton = new JButton("Cancel");
	private JButton clearJButton = new JButton("Clear");
	private Border blackline;
	private TitledBorder generalTitledBorder;

	SendEmailInterface() {
		//Create the Main Frame
		sendEmailJFrame = new JFrame("Send an Anonymous Email");
		sendEmailJFrame.setSize(500, 350);
		sendEmailJFrame.setResizable(false);
		sendEmailJFrame.setLocationRelativeTo(CommunicationInterface.imContentFrame);

		//Create the content Pane
		contentPane = new JPanel();
		contentPane.setLayout(null);
		contentPane.setVisible(true);

		sendEmailJFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("images/logosmall.png"));

		//Initalize borders
		blackline = BorderFactory.createLineBorder(Color.black);
		generalTitledBorder = BorderFactory.createTitledBorder(
				blackline, "Anonymous Email Submission Form");

		//Username Input
		toJTextField = new JTextField();
		toJLabel.setBounds(15, 20, 150, 25);
		toJTextField.setBounds(15, 40, 470, 25);

		reJLabel.setBounds(15, 80, 400, 25);
		reJTextField = new JTextField();
		reJTextField.setBounds(15, 100, 470, 25);
		
		bodyJLabel.setBounds(15, 140, 400, 25);
		bodyJTextArea = new JTextArea();
		bodyJTextArea.setLineWrap(true);
		bodyJTextArea.setWrapStyleWord(true);
		JScrollPane bodySP = new JScrollPane(bodyJTextArea);
		bodySP.setBounds(15, 165, 470, 100);
		bodySP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		bodySP.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		//Confirm and Cancel JButtons
		confirmJButton.setBounds(280, 290, 100, 25);
		cancelJButton.setBounds(385, 290, 100, 25);
		clearJButton.setBounds(10, 290, 100, 25);


		clearJButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				reJTextField.setText("");
				toJTextField.setText("");
				bodyJTextArea.setText("");
			}
		});

		//ActionListener to make the connect menu item connect
		confirmJButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				try{
				if (!(toJTextField.getText().equals("")) && !(reJTextField.getText().equals(""))) {
					Athena.sendEmail(toJTextField.getText(),reJTextField.getText(),bodyJTextArea.getText());
					//sendInfoToAegis(descriptionJTextField.getText(), recreationJTextArea.getText(), expectedJTextArea.getText(), actualJTextArea.getText());
					JOptionPane.showMessageDialog(null, "Your anonymous email has been sent.", "Thanks!", JOptionPane.INFORMATION_MESSAGE);
					sendEmailJFrame.dispose();

				} else {
					JOptionPane.showMessageDialog(null, "Please fill in all required fields.", "Error!", JOptionPane.ERROR_MESSAGE);
				}}catch (Exception e){e.printStackTrace();}

			}
		});

		cancelJButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				sendEmailJFrame.dispose();
			}
		});

		//Add all the components to the contentPane
		contentPane.add(toJLabel);
		contentPane.add(toJTextField);
		contentPane.add(reJLabel);
		contentPane.add(reJTextField);
		contentPane.add(cancelJButton);
		contentPane.add(bodyJLabel);
		contentPane.add(bodySP);

		contentPane.add(confirmJButton);
		contentPane.add(clearJButton);

		//Make sure we can see damn thing
		contentPane.setVisible(true);
		contentPane.setBorder(generalTitledBorder);

		//Let the Frame know what's up
		sendEmailJFrame.setContentPane(contentPane);
		sendEmailJFrame.setVisible(true);
	}

	/**
	 * Compile the report information and send it to Aegis
	 * @param titles Summary of bug/feature
	 * @param recreates Recreation steps for bug
	 * @param expecteds Expected outcome of action
	 * @param actuals The bug
	 */
	public void sendInfoToAegis(String titles, String recreates, String expecteds, String actuals) {

		//Get a connection
		//Client.connect();

		//Give me back my filet of DataOutputStream + DataInputStream
		DataOutputStream dout = Athena.returnDOUT();
		//DataInputStream din = Client.returnDIN();


		try {
			Athena.systemMessage("10");
			dout.writeUTF(Athena.encryptServerPublic(titles));
			dout.writeUTF(Athena.encryptServerPublic(recreates));
			dout.writeUTF(Athena.encryptServerPublic(expecteds));
			dout.writeUTF(Athena.encryptServerPublic(actuals));
			//Close the connection
			//dout.close();
			//Client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
