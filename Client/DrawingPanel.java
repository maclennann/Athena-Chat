/* Athena/Aegis Encrypted Chat Platform
 * DrawingPanel.java: Creates and returns a drawingpanel to place images
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

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

//This class instanciates a DrawingPanel which is used to paint icons onto the main Panel
public class DrawingPanel extends JPanel { 
	/**
	 * 
	 */
	private static final long serialVersionUID = 7750278403335957719L;
	Image img;
	int x, y;
	
	DrawingPanel (Image img) {
		this.img = img;
	}
	
	public void paintComponent (Graphics g) { 			   
		   super.paintComponent(g);
		
		//Draw image 
		g.drawImage(img, 0, 0, this);
	}
}