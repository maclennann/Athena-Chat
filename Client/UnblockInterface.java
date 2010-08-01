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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
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
import javax.swing.JComboBox;

/**
 * Submit a bug report or feature request to Aegis
 * @author OlmypuSoft
 */
public class UnblockInterface extends JPanel {

	/**
	 *
	 */
	//Define components
	private JFrame submitBugJFrame;
	private JPanel contentPane, generalInformationJPanel, loginInformationJPanel;
	private JLabel descriptionJLabel = new JLabel("User to Unblock:");
	private JComboBox buddyList;
	private JButton confirmJButton = new JButton("Confirm");
	private JButton cancelJButton = new JButton("Cancel");
	private JButton clearJButton = new JButton("Clear");

	UnblockInterface() {
		try{
			buddyList = new JComboBox(Athena.getBlockList());
		}catch(Exception e){}
		
		//Create the Main Frame
		submitBugJFrame = new JFrame("Unblock Someone");
		submitBugJFrame.setSize(250, 125);
		submitBugJFrame.setResizable(false);
		submitBugJFrame.setLocationRelativeTo(CommunicationInterface.imContentFrame);

		//Create the content Pane
		contentPane = new JPanel();
		contentPane.setLayout(null);
		contentPane.setVisible(true);

		submitBugJFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("images/logosmall.png"));
		descriptionJLabel.setBounds(10,10,200,25);
		buddyList.setBounds(10,30,200,25);
		//buddyList.setSize(200,25);


		//Confirm and Cancel JButtons
		confirmJButton.setBounds(10, 70, 100, 25);
		cancelJButton.setBounds(125, 70, 100, 25);
		//clearJButton.setBounds(10, 490, 100, 25);

		//ActionListener to make the connect menu item connect
		confirmJButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				Athena.unblockUser(buddyList.getSelectedItem().toString());
				JOptionPane.showMessageDialog(null, "Thank you for submitting this report. It has been added to our database", "Thanks!", JOptionPane.INFORMATION_MESSAGE);
				submitBugJFrame.dispose();
			}
		});

		cancelJButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				submitBugJFrame.dispose();
			}
		});

		//Add all the components to the contentPane
		contentPane.add(descriptionJLabel);
		contentPane.add(buddyList);
		contentPane.add(cancelJButton);
		contentPane.add(confirmJButton);
		
		//contentPane.add(clearJButton);
		
		//Make sure we can see damn thing
		contentPane.setVisible(true);

		//Let the Frame know what's up
		submitBugJFrame.setContentPane(contentPane);
		//submitBugJFrame.pack();
		submitBugJFrame.setVisible(true);
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
