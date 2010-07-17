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
		super.setLocation(width - (width / 2) - 185, height - (height / 2)-200);
    }


    public void iterate(int currNum, String currAct) {
        current.setValue(currNum);
		currentAction.setText(currAct);
		this.pack();
		super.toFront();
    }

}