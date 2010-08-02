/* Athena/Aegis Encrypted Chat Platform
 * Progress.java: Shows a Progress Bar in a window. Used for file transfers.
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

public class Progress extends JFrame {

    JProgressBar current;
    JTextArea out;
    JButton find;
	JLabel filename;
	JLabel elapsedTime;
    Thread runner;
    int num = 0;

    public Progress(String fileName, int total) {
        super("File Transfer Progress");

        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel pane = new JPanel();
        pane.setLayout(new FlowLayout());
		filename = new JLabel(fileName+": ");
		elapsedTime = new JLabel("Time: 0 ms");
        current = new JProgressBar(0, total);
        current.setValue(0);
        current.setStringPainted(true);
		pane.add(filename);
        pane.add(current);
		pane.add(elapsedTime);
        setContentPane(pane);
    }


    public void iterate(int currNum, int currTime) {
        current.setValue(currNum);
		elapsedTime.setText("Time: "+currTime+" ms");
		this.pack();
    }

  /*  public static void main(String[] arguments) {
        Progress frame = new Progress();
        frame.pack();
        frame.setVisible(true);
        frame.iterate();
   }*/
}