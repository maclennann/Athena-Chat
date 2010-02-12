/****************************************************
 * Athena: Encrypted Messaging Application v.0.0.1
 * By: 	
 * 			Gregory LeBlanc
 * 			Norm Maclennan 
 * 			Stephen Failla
 * 
 * This program allows a user to send encrypted messages over a fully standardized messaging architecture. It uses RSA with (x) bit keys and SHA-256 to 
 * hash the keys on the server side. It also supports fully encrypted emails using a standardized email address. The user can also send "one-off" emails
 * using a randomly generated email address
 * 
 * File: ClientApplet.java
 * 
 * Creates the window for the client and sets connection variables.
 *
 ****************************************************/
import java.awt.Color;
import java.applet.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.Hashtable;
//Creates the physical applet, calls the client code
public class ClientApplet extends JFrame 
{
	
	//String array of buddies for choosing user to send message to
	public static String[] otherUsers = {"Norm", "Steve", "Greg", "Aegis"};
	
	// Components for the visual display of the chat windows
    public JList userBox = new JList(otherUsers);
	public JMenuBar menuBar = new JMenuBar();
	public JMenu file, edit, encryption;
	public JMenuItem connect, disconnect, exit;
	public JPanel panel; //still need this?
	public JFrame imContentFrame, buddyListFrame;
	public JTabbedPane imTabbedPane = new JTabbedPane();
	public Hashtable tabPanels = new Hashtable();

	ClientApplet () { 

		//Initialize chat window
		//This is the main frame for the IMs
		imContentFrame = new JFrame("Athena Chat Application");
		imContentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		imContentFrame.setSize(800, 605);
		imContentFrame.setResizable(true);
		
		//Build the first menu.
	        file = new JMenu("File");
	        file.setMnemonic(KeyEvent.VK_A);
	        file.getAccessibleContext().setAccessibleDescription(
	                "The only menu in this program that has menu items");
	    
	        //Initialize menu button
	        connect = new JMenuItem("Connect");
	        connect.setMnemonic(KeyEvent.VK_H);
	        file.add(connect);
	        
	        //Initialize menu button
	        disconnect = new JMenuItem("Disconnect");
	        disconnect.setMnemonic(KeyEvent.VK_H);
	        file.add(disconnect);     
	        
	        //Initialize menu button
	        exit = new JMenuItem("Exit");
	        exit.setMnemonic(KeyEvent.VK_H);
        	file.add(exit);
        
	        menuBar.add(file);
	        
		//Build the second menu.
	        edit = new JMenu("Edit");
	        edit.setMnemonic(KeyEvent.VK_A);
        	edit.getAccessibleContext().setAccessibleDescription(
	                "The only menu in this program that has menu items");
	        menuBar.add(edit);
	       
		//Build the third menu.
	        encryption = new JMenu("Encryption");
	        encryption.setMnemonic(KeyEvent.VK_A);
	        encryption.getAccessibleContext().setAccessibleDescription(
	                "The only menu in this program that has menu items");
	        menuBar.add(encryption);
	
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				Client.connect();
			}
		});

		disconnect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				Client.disconnect();
			}
		});
			
		exit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				Client.exit();
			}
		});
			
		//frame.setJMenuBar(menuBar);

		JScrollPane buddylist = new JScrollPane(userBox);
		buddylist.setBounds(595,10,195,538);
		//Actionlistener for the Buddylist
		
		//Opens a tab or focuses a tab when a username in the buddylist is double-clicked
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                JList theList = (JList) mouseEvent.getSource();
                //If it was doubleclicked
                if (mouseEvent.getClickCount() == 2) {
                    //Find out what was double-clicked
                    int index = theList.locationToIndex(mouseEvent.getPoint());
                    if (index >= 0) {
                        //Get the buddy that was double-clicked
                        Object o = theList.getModel().getElementAt(index);
                        if(imTabbedPane.indexOfTab(o.toString())==-1){
                            makeTab(o.toString());
                        }else{
                            //Focust the tab for this username
                            imTabbedPane.setSelectedIndex(imTabbedPane.indexOfTab(o.toString()));
                        }
        }}}};
        
		userBox.addMouseListener(mouseListener);
		userBox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		imTabbedPane.setBounds(10,10,580,537);

		//Generate panel by adding appropriate components
		panel = new JPanel();
		panel.setLayout(null);
		panel.add(buddylist);
		panel.add(imTabbedPane);

		//Initialize window frame
		imContentFrame.setJMenuBar(menuBar);
	    imContentFrame.setContentPane(panel);
	    imContentFrame.setVisible(true);
        
	}
	
	public void makeTab(String user){
	    //Create a hashtable mapping a username to the jpanel in a tab
        tabPanels.put(user,new MapTextArea(user));
        //Make a temporary object for that jpanel
        MapTextArea temp = (MapTextArea)tabPanels.get(user);
        //Actually pull the jpanel out
        JPanel tempPanel = temp.getJPanel();
        //Create a tab with that jpanel on it
        imTabbedPane.addTab(user,null,tempPanel,"Something");
        //Focus the new tab
        imTabbedPane.setSelectedIndex(imTabbedPane.indexOfTab(user));
    }
}
//Write comments when I figure out what this actually does
class MapTextArea { 

	public JPanel myJPanel;
	public JTextArea myTA;
	public JTextField myTF;
	String username=null;
	int tabIndex=-1;

	//Constructor
	MapTextArea(String user) { 
		myJPanel = new JPanel();
		myJPanel.setLayout(null);
		myTA = new JTextArea();
		myTF = new JTextField();
		myTF.setBounds(10,469,560,30);
		JScrollPane mySP = new JScrollPane(myTA);
		mySP.setBounds(10,10,559,450);
		mySP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		mySP.setOpaque(true);
		myTA.setEditable(false);
		myTA.setLineWrap(true);
		username = user;
		myJPanel.add(mySP);
		myJPanel.add(myTF);
		myTF.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                                Client.processMessage(myTF.getText());
                        }
        	});

	        Font font = new Font("Arial",Font.PLAIN,17);
        	myTA.setFont(font);
	}
	
	public void setUserName(String user){
		username = user;
	}
	
	public String getUserName(){
		return username;
	}
	
	public void setTabIndex(int index){
		tabIndex = index;
	}

	public int getTabIndex() {
		return tabIndex;
	}

	public JPanel getJPanel() { 
		return myJPanel;
	}
	
	public void setTextColor(Color color){
	    myTA.setForeground(color);
    }
    
	public void writeToTextArea(String message){
		myTA.append(message);
	}
	
	public void moveToEnd(){
	    myTA.selectAll();	
    }
    
    public void clearTextField(){
        myTF.setText("");
    }
}
