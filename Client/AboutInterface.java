/* Athena/Aegis Encrypted Chat Platform
 * AboutInterface.java: "About" window with program versioning information
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

import java.awt.AWTException;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 'About' window with product and version information
 * @author Norm
 */
public class AboutInterface extends JFrame {
	//Components for the visual display of the aboutWindow window
	private JFrame aboutWindow;
	private JPanel contentPane = new JPanel();
	private JLabel usernameLabel = new JLabel("Athena Chat Client v1.0.1b");
	private JButton cancel = new JButton("OK");
	private ImageIcon logoicon = new ImageIcon("images/splash.png");
	private JLabel logo = new JLabel();

	//Constructor | Here's where the fun begins
	AboutInterface() throws AWTException {
		logo.setIcon(logoicon);

		//Initialize aboutWindow window
		aboutWindow = new JFrame("About Athena");
		//aboutWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		aboutWindow.setSize(200, 300);
		aboutWindow.setResizable(false);
		contentPane.setLayout(null);
		aboutWindow.setLocationRelativeTo(CommunicationInterface.imContentFrame);

		//Adjust font sizes
		cancel.setFont(new Font("Dialog", 1, 10));
		usernameLabel.setFont(new Font("Dialog", 1, 12));

		//Size the components
		usernameLabel.setBounds(20, 210, 150, 25);
		cancel.setBounds(110, 235, 75, 30);
		logo.setBounds(10, 10, 175, 200);

		//ActionListener to make the connect menu item connect
		cancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				aboutWindow.dispose();
			}
		});

		//Add the components to the Frame
		contentPane.add(usernameLabel);
		contentPane.add(cancel);
		contentPane.add(logo);
		//Initialize Frame
		aboutWindow.setContentPane(contentPane);
		aboutWindow.setVisible(true);
	}
}
