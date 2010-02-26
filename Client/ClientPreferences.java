import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

/**
 * 
 */

/**
 * @author OlympuSoft
 *
 */
//Let's make the preferences window
public class ClientPreferences extends JFrame {
	
	//Define components
	public JFrame preferences;
	public JPanel contentPane = new JPanel();
	public JScrollPane prefScrollBar = new JScrollPane();
	public JButton apply = new JButton("Apply");
	public JButton cancel = new JButton("Cancel");
	//Need tons more when we figure out each option set
	
	//Constructor
	ClientPreferences() { 
		//Initialize Preferences Window
		preferences = new JFrame("Preferences");
		preferences.setSize(800,600);
		preferences.setResizable(false);
		contentPane.setLayout(null);
		
		//Size the components
		prefScrollBar.setBounds(15, 15, 150, 545);
		apply.setBounds(700,525,75,30);
		cancel.setBounds(615,525,75,30);
		
		//Add the components to the ContentPane
		contentPane.add(prefScrollBar);
		contentPane.add(apply);
		contentPane.add(cancel);
		
		//Initialize Frame
		preferences.setContentPane(contentPane);
		preferences.setVisible(true);
	}
}
