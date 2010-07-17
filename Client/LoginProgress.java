import java.awt.*;
import javax.swing.*;

public class LoginProgress extends JFrame {

    JProgressBar current;
	JFrame progressWindow;
	ImageIcon logoicon = new ImageIcon("images/splash.png");
	JLabel logo = new JLabel();
    //JTextArea out;
    JButton find;
	JLabel currentAction;
    Thread runner;
    int num = 0;

    public LoginProgress() {
        super("Logging in...");
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension scrnsize = toolkit.getScreenSize();
		int width = (int) scrnsize.getWidth();
		int height = (int) scrnsize.getHeight();
		logo.setIcon(logoicon);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel pane = new JPanel();
		
		//JOptionPane.showMessageDialog(null,"Window is at: "+(width/2)+"x"+(height/2));
        pane.setLayout(new BorderLayout());
		currentAction = new JLabel(": ");
        current = new JProgressBar(0, 2000);
        current.setValue(0);
        current.setStringPainted(true);
		pane.add(logo,BorderLayout.PAGE_START);
		pane.add(current,BorderLayout.CENTER);
        pane.add(currentAction,BorderLayout.PAGE_END);
        setContentPane(pane);
		super.setLocation(width - (width / 2) - 200, height - (height / 2)-300);
    }


    public void iterate(int currNum, String currAct) {
        current.setValue(currNum);
		currentAction.setText(currAct);
		this.pack();
		super.toFront();
    }

  /*  public static void main(String[] arguments) {
        Progress frame = new Progress();
        frame.pack();
        frame.setVisible(true);
        frame.iterate();
   }*/
}