/* Athena/Aegis Encrypted Chat Platform
 * LoginProgress.java: Splashscreen/Progress bar displayed while logging in
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

import java.awt.*;
import javax.swing.*;

public class LoginProgress extends JFrame {

    JProgressBar current;
	JFrame progressWindow;
	ImageIcon logoicon = new ImageIcon("images/splash.png");
	JLabel logo = new JLabel();
    JButton find;
	JLabel currentAction;
    Thread runner;
    int num = 0;

    public LoginProgress() {
        super("Logging in...");
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		super.setIconImage(Toolkit.getDefaultToolkit().getImage("images/logosmall.png"));
		Dimension scrnsize = toolkit.getScreenSize();
		int width = (int) scrnsize.getWidth();
		int height = (int) scrnsize.getHeight();
		logo.setIcon(logoicon);
        JPanel pane = new JPanel();

        pane.setLayout(new BorderLayout());
		currentAction = new JLabel(": ");
		currentAction.setHorizontalAlignment(JLabel.CENTER);
        current = new JProgressBar(0, 2000);
        current.setValue(0);
        current.setStringPainted(true);
		pane.add(logo,BorderLayout.PAGE_START);
		pane.add(current,BorderLayout.CENTER);
        pane.add(currentAction,BorderLayout.PAGE_END);
        setContentPane(pane);
		super.setLocation(width - (width / 2) - 100, height - (height / 2)- 250);
    }


    public void iterate(int currNum, String currAct) {
        current.setValue(currNum);
		currentAction.setText(currAct);
		this.pack();
		super.toFront();
    }

}