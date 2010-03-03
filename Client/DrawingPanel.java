import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

/****************************************************
 * Athena: Encrypted Messaging Application v.0.0.2
 * By: 	
 * 			Gregory LeBlanc
 * 			Norm Maclennan 
 * 			Stephen Failla
 * 
 * This program allows a user to send encrypted messages over a fully standardized messaging architecture. It uses RSA with (x) bit keys and SHA-256 to 
 * hash the keys on the server side. It also supports fully encrypted emails using a standardized email address. The user can also send "one-off" emails
 * using a randomly generated email address
 * 
 * File: DrawingPanel.java
 * 
 * Creates and returns a DrawingPanel object that is used to display images
 *
 ****************************************************/
//This class instanciates a DrawingPanel which is used to paint icons onto the main Panel
public class DrawingPanel extends JPanel { 
	Image img;
	int x, y;
	
	DrawingPanel (Image img) {
		this.img = img;
	}
	
	public void paintComponent (Graphics g) { 		
		   int dy = getSize ().height;
		   int dx = getSize ().width;		   
		   super.paintComponent(g);
		
		//Draw image 
		g.drawImage(img, 0, 0, this);
	}
}