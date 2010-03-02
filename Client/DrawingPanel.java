import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

/**
 * 
 */

/**
 * @author sk
 *
 */
//This class instanciates a DrawingPanel which is used to paint icons onto the main Panel
public class DrawingPanel extends JPanel { 
	Image img;
	int x, y;
	
	DrawingPanel (Image img) {
		this.img = img;
	}
	
	public void paintComponent (Graphics g) { 
		super.paintComponent(g);
		
		//Draw image 
		g.drawImage(img, x, y, this);
	}

}