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