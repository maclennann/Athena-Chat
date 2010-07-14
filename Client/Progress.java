import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Progress extends JFrame {

    JProgressBar current;
    JTextArea out;
    JButton find;
	JLabel filename;
    Thread runner;
    int num = 0;

    public Progress(String fileName, int total) {
        super("Progress");

        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel pane = new JPanel();
        pane.setLayout(new FlowLayout());
		filename = new JLabel(fileName+": ");
        current = new JProgressBar(0, total);
        current.setValue(0);
        current.setStringPainted(true);
		pane.add(filename);
        pane.add(current);
        setContentPane(pane);
    }


    public void iterate(int currNum) {
        current.setValue(currNum);
    }

  /*  public static void main(String[] arguments) {
        Progress frame = new Progress();
        frame.pack();
        frame.setVisible(true);
        frame.iterate();
   }*/
}