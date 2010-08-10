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
import java.util.logging.Level;
import java.util.logging.Logger;
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
import java.util.Enumeration;
import javax.swing.JComboBox;

/**
 * Submit a bug report or feature request to Aegis
 * @author OlmypuSoft
 */
public class BlockUserInterface extends JPanel {

	/**
	 *
	 */
	//Define components
	private JFrame blockUserJFrame;
	private JPanel contentPane;
	private JLabel descriptionJLabel = new JLabel("User to unblock:");
	private JComboBox buddyList;
	private JButton confirmJButton = new JButton("Confirm");
	private JButton cancelJButton = new JButton("Cancel");

	BlockUserInterface() {
		try{
			buddyList = new JComboBox(Athena.getBlockList());
		}catch(Exception e){}
		
		//Create the Main Frame
		blockUserJFrame = new JFrame("Unblock User");
		blockUserJFrame.setSize(245, 135);
		blockUserJFrame.setResizable(false);
		blockUserJFrame.setLocationRelativeTo(CommunicationInterface.imContentFrame);

		//Create the content Pane
		contentPane = new JPanel();
		contentPane.setLayout(null);
		contentPane.setVisible(true);

		blockUserJFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("images/logosmall.png"));
		descriptionJLabel.setBounds(10,10,200,25);
		buddyList.setBounds(10,30,200,25);
		//buddyList.setSize(200,25);


		//Confirm and Cancel JButtons
		confirmJButton.setBounds(10, 70, 100, 25);
		cancelJButton.setBounds(125, 70, 100, 25);
                if(buddyList.getItemAt(0).toString().trim().equals(""))
                    confirmJButton.setEnabled(false);

		//ActionListener to make the connect menu item connect
		confirmJButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
                                String userToUnblock = buddyList.getSelectedItem().toString();
                                    Athena.unblockUser(userToUnblock);
                                    JOptionPane.showMessageDialog(null,  userToUnblock + " has been unblocked.\nPlease disconnect and reconnect\nto reflect your recent changes.", "Unblock Complete", JOptionPane.INFORMATION_MESSAGE);
                                    blockUserJFrame.dispose();
			}
		});

		cancelJButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				blockUserJFrame.dispose();
			}
		});

		//Add all the components to the contentPane
		contentPane.add(descriptionJLabel);
		contentPane.add(buddyList);
		contentPane.add(cancelJButton);
		contentPane.add(confirmJButton);
		
		//Make sure we can see damn thing
		contentPane.setVisible(true);

		//Let the Frame know what's up
		blockUserJFrame.setContentPane(contentPane);
		blockUserJFrame.setVisible(true);
	}

        BlockUserInterface(boolean blockUser) {
		try{
                        Enumeration userEnumeration = Athena.clientResource.contactListModel.elements();
                        String[] onlineContacts = new String[Athena.clientResource.contactListModel.size()];
                        int count = 0;
                        for (Enumeration<?> e = userEnumeration; e.hasMoreElements();)
                        {
                            onlineContacts[count] = e.nextElement().toString();
                            count++;
                        }
                        buddyList = new JComboBox(onlineContacts);
		}catch(Exception e){}

		//Create the Main Frame
		blockUserJFrame = new JFrame("Block User");
		blockUserJFrame.setSize(245, 135);
		blockUserJFrame.setResizable(false);
		blockUserJFrame.setLocationRelativeTo(CommunicationInterface.imContentFrame);

		//Create the content Pane
		contentPane = new JPanel();
		contentPane.setLayout(null);
		contentPane.setVisible(true);

		blockUserJFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("images/logosmall.png"));
		descriptionJLabel.setBounds(10,10,200,25);
                descriptionJLabel.setText("User to block:");
		buddyList.setBounds(10,30,200,25);
		//buddyList.setSize(200,25);


		//Confirm and Cancel JButtons
		confirmJButton.setBounds(10, 70, 100, 25);
		cancelJButton.setBounds(125, 70, 100, 25);
                if(buddyList.getItemCount() == 0)
                    confirmJButton.setEnabled(false);

		//ActionListener to make the connect menu item connect
		confirmJButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
                try {
                    String userToBlock = buddyList.getSelectedItem().toString();
                    String[] blockedUsers = Athena.getBlockList();
                    System.out.println("Retrieved blocklist with length " + blockedUsers.length);
                    boolean blockFlag = false;

                    if(userToBlock.equals(Athena.username))
                        JOptionPane.showMessageDialog(null,  "Sorry, you cannot block yourself.", "Unblock Failed", JOptionPane.INFORMATION_MESSAGE);
                    else
                    {
                        //Check to see if user is already blocked
                        for(int z = 0; z < blockedUsers.length; z++)
                        {
                            if(blockedUsers[z].equals(userToBlock))
                            {
                                JOptionPane.showMessageDialog(null, userToBlock + " is already blocked.", "Block Complete", JOptionPane.INFORMATION_MESSAGE);
                                blockFlag = true;
                            }

                        }

                        if(!blockFlag)
                        {
                            Athena.blockUser(userToBlock);
                            JOptionPane.showMessageDialog(null, userToBlock + " has been blocked.", "Block Complete", JOptionPane.INFORMATION_MESSAGE);
                            blockUserJFrame.dispose();
                        }
                    }

                } catch (IOException ex) {
                    Logger.getLogger(BlockUserInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
			}
		});

		cancelJButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				blockUserJFrame.dispose();
			}
		});

		//Add all the components to the contentPane
		contentPane.add(descriptionJLabel);
		contentPane.add(buddyList);
		contentPane.add(cancelJButton);
		contentPane.add(confirmJButton);

		//Make sure we can see damn thing
		contentPane.setVisible(true);

		//Let the Frame know what's up
		blockUserJFrame.setContentPane(contentPane);
		blockUserJFrame.setVisible(true);
	}
}
