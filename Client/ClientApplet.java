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
 * File: ClientApplet.java
 * 
 * Creates the window for the client and sets connection variables.
 *
 ****************************************************/
import java.awt.Color;
import java.applet.*;
import java.awt.*;
import java.util.Enumeration;
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

//Client swing window.
//TODO: Rename it to something else. It's not an applet
public class ClientApplet extends JFrame 
{
	public static Hashtable userStatus;

	//String array of buddies for choosing user to send message to
	public static String[] otherUsers = {"Norm", "Steve", "Greg", "Aegis"};	
	
	// Components for the visual display of the chat windows
	public JList userBox;
	public JMenuBar menuBar = new JMenuBar();
	public JMenu file, edit, encryption;
	public JMenuItem connect, disconnect, exit;
	public JPanel panel; //still need this?
	public JFrame imContentFrame, buddyListFrame;
	public JTabbedPane imTabbedPane = new JTabbedPane();
	public Hashtable tabPanels = new Hashtable();

	public JList newBuddyList(String[] availableUsers) { 
		userBox = new JList(availableUsers);
		return userBox;
	}
		

	ClientApplet () { 

		//Initialize chat window
		//This is the main frame for the IMs
		imContentFrame = new JFrame("Athena Chat Application");
		imContentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		imContentFrame.setSize(800, 605);
		imContentFrame.setResizable(true);
		
		//Create the file menu.
	        file = new JMenu("File");
	        file.setMnemonic(KeyEvent.VK_A);
	    
	        //Create button File -> Connect
	        connect = new JMenuItem("Connect");
	        connect.setMnemonic(KeyEvent.VK_H);
	        file.add(connect);
	        
	        //Create button File -> Disconnect
	        disconnect = new JMenuItem("Disconnect");
	        disconnect.setMnemonic(KeyEvent.VK_H);
	        file.add(disconnect);     
	        
	        //Create button File -> Exit
	        exit = new JMenuItem("Exit");
	        exit.setMnemonic(KeyEvent.VK_H);
        	file.add(exit);
        
		//Add the file menu to the menubar
	        menuBar.add(file);
	        
		//Create the edit menu.
	        edit = new JMenu("Edit");
	        edit.setMnemonic(KeyEvent.VK_A);
	        menuBar.add(edit);
	        
	        //TODO Add items to the edit menu
	       
		//Create the encryption menu.
	        encryption = new JMenu("Encryption");
	        encryption.setMnemonic(KeyEvent.VK_A);
	        menuBar.add(encryption);
	        
	        //TODO Add itemsto the encryption menu
	
		//ActionListener to make the connect menu item connect
		connect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				Client.connect();
			}
		});

		//ActionListener to make the disconnect menu item disconnect
		disconnect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				Client.disconnect();
			}
		});
		
		//ActionListener to make the exit menu item exit
		exit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				Client.exit();
			}
		});

		//Why is this commented out?	
		//frame.setJMenuBar(menuBar);

		//Adds the buddylist to a scroll pane
		JScrollPane buddylist = new JScrollPane(userBox);
		buddylist.setBounds(595,10,195,538);
		
		//MouseListener for the BuddyList
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
						
						//Create a tab for the conversation if it doesn't exist
						if(imTabbedPane.indexOfTab(o.toString())==-1){
							makeTab(o.toString());
						}else{
							//Focus the tab for this username if it already exists
							imTabbedPane.setSelectedIndex(imTabbedPane.indexOfTab(o.toString()));
						}
		}}}};

		//Add the mouselistener to the buddylist        
		//userBox.addMouseListener(mouseListener);
		//userBox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//Places the area for the tabs
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
	
	//Make a tab for a conversation
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
	//Makes a new hashtable with user's online status
	public void mapUserStatus(String username, int status) { 
		System.out.println("Username: " + username + "\nStatus: " + status);
		userStatus.put(username, status);
	}
			
		
}

//This class holds all of the JComponents and acts as an interface to each conversation's tab
class MapTextArea { 

	//All of the JComponents in the tab
	public JPanel myJPanel;
	public JTextArea myTA;
	public JTextField myTF;

	//The username associated with the tab
	String username=null;

	//The index of the tab this lives in
	int tabIndex=-1;

	//Constructor
	MapTextArea(String user) { 
		//Create the JPanel and put all of the components in it
		myJPanel = new JPanel();
		myJPanel.setLayout(null);
		
		//Create the textarea and the scrollpane around it
		myTA = new JTextArea();
		myTA.setEditable(false);
		myTA.setLineWrap(true);
		myTA.setWrapStyleWord(true);
		JScrollPane mySP = new JScrollPane(myTA);
		mySP.setBounds(10,10,559,450);
		mySP.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		mySP.setOpaque(true);
		myJPanel.add(mySP);
		
		//Create the textfield
		myTF = new JTextField();
		myTF.setBounds(10,469,560,30);
		myJPanel.add(myTF);
		
		username = user;
		
		//Add an actionlistener to the textfield to send messages
		myTF.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                                Client.processMessage(myTF.getText());
                        }
        	});

		//Set font to Arial
	        Font font = new Font("Arial",Font.PLAIN,17);
        	myTA.setFont(font);
	}
	
	//Set the username associated with the tab
	public void setUserName(String user){
		username = user;
	}
	
	//Get the username associated with the tab
	public String getUserName(){
		return username;
	}
	
	//Set the index of the tab for this JPanel
	public void setTabIndex(int index){
		tabIndex = index;
	}

	//Get the tab index for this JPanel
	public int getTabIndex() {
		return tabIndex;
	}

	//Get the JPanel for the tab
	public JPanel getJPanel() { 
		return myJPanel;
	}
	
	//Set the text color (does nothing)
	public void setTextColor(Color color){
		myTA.setForeground(color);
	}
    
	//Write a string to the text area
	public void writeToTextArea(String message){
		myTA.append(message);
	}
	
	//Move the cursor to the end of the ScrollPane
	//TODO: Sometimes it shows highlighted text
	public void moveToEnd(){
		myTA.selectAll();	
	}
    
	//Clear the text out of the textfield
	public void clearTextField(){
		myTF.setText("");
	}
}
