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
 * File: Client.java
 * 
 * This is where the detailed description of the file will go.
 *
 ****************************************************/

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Enumeration;
import java.security.MessageDigest;
import java.security.DigestInputStream;
import java.security.NoSuchAlgorithmException;
import java.io.StringWriter;
import java.io.PrintWriter;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import sun.security.util.BigInt;

public class Athena
{
	/* Begin public variables 
	 * 
	 */
	public static String username="null"; //Global username variable
	public static RSAPublicKeySpec serverPublic; //Servers public key
	//End public variables
	
	/* Begin protected variables
	 * 
	 */ 
	protected static CommunicationInterface clientResource; //Accessor objects for referencing objects in another class
	protected static AuthenticationInterface loginGUI;		
	protected static MapTextArea print; //Temporary object for the JPanel in a tab	
	//End protected variables
	
	/* Begin private variables
	 * 
	 */
	private static final int debug=1; //Show debug messages?
	private static String serverIP = "205.186.153.44"; //IP of the server
	//private static String serverIP = "10.1.10.49"; //IP of server for Norm. Don't delete this agian.
	private static int connected = 0; 	//If the client is connect to the server
	private static int away = 0; //Is the user away?	
	private static DESCrypto descrypto; //DESCrpyto Object for encrypting with user's password	
	private static String toUser; //Recipient for message	
	public static String awayText; //Away message text	
	private static Socket c2ssocket; // The socket connecting us to the server for client communication
	private static Socket c2csocket; // The socket connecting us to the server for server communication
	private static DataOutputStream c2sdout; // Client to Server DataOutputStream
	private static DataInputStream c2sdin; //Client to Server DataInputStream
	private static DataOutputStream c2cdout; //Client to Client DataOutputStream
	private static DataInputStream c2cdin; //Clien to Client DataInputStream
	private static Thread listeningProcedureClientToClient; //Thread that will be used to listen for incoming messages
	private static boolean enableSounds; //Flag to control sound notifications
	private static BigInteger modOfBuddy = null;
	private static BigInteger expOfBuddy = null;	
	public static RSAPrivateKeySpec usersPrivate; //User's public key
	//End private variables
	
	/**
	 * Method that connects the user with Aegis
	 * @param usernameToConnect the username that is entered in the login window 
	 * @param hashedPassword the hashed password that is entered in the logoin window
	 */
	public static void connect(String usernameToConnect, String hashedPassword) throws InterruptedException, AWTException, Exception { 
		//Try to connect with and authenticate to the socket
		username = usernameToConnect;
		try {
			try{
				//Connect to auth server at defined port over socket
				//This socket is for client -> server coms
				c2ssocket = new Socket( serverIP, 7777 );
				//This socket is for client -> client coms
				c2csocket = new Socket(serverIP, 7778 );
			}catch (Exception e){ 
				//We can't connect to the server at the specified port for some reason
				JOptionPane.showMessageDialog(null,"Could not connect to the server.\nPlease check your Internet connection.\n\n","Connection Error",JOptionPane.ERROR_MESSAGE);
				loginGUI = new AuthenticationInterface();
				return;
			}

			//Create the DESCrypto object for buddylist and preferences cryptography
			String saltUser;
			if(username.length()>=8){
				saltUser = username.substring(0,8);
			}else saltUser = username;
			descrypto = new DESCrypto(hashedPassword,saltUser);

			//Connection established debug code.
			if(debug>=1)System.out.println( "Connected to "+ c2ssocket + "<- for client to server communication." ); //Client to server coms
			if(debug>=1)System.out.println( "Connected to "+ c2csocket + "<- for client to client communication." ); //Client to client coms


			//Bind the datastreams to the socket in order to send/receive data
			//These datastreams are for client -> server coms
			c2sdin = new DataInputStream( c2ssocket.getInputStream() );
			c2sdout = new DataOutputStream( c2ssocket.getOutputStream() );
			//These datastreams are for client -> client coms
			c2cdin = new DataInputStream(c2csocket.getInputStream());
			c2cdout = new DataOutputStream(c2ssocket.getOutputStream());

			//Read in the server's public key for encryption of headers
			serverPublic = RSACrypto.readPubKeyFromFile("users/Aegis/keys/Aegis.pub");

			//Send username and hashed password over the socket for authentication
			if(debug>=1)System.out.println("User's hashed password: " + hashedPassword);
			c2sdout.writeUTF(encryptServerPublic(username)); //Sending Username
			c2sdout.writeUTF(encryptServerPublic(hashedPassword)); //Sending Password
			String result = decryptServerPublic(c2sdin.readUTF()); //Read in the result

			if(debug>=1)System.out.println("Result: " + result);
			if(result.equals("Failed")) { 
				disconnect();
				LoginFailedInterface loginFailed = new LoginFailedInterface();
				return;
			}
			else { 
				//We're connected
				connected=1; 
				clientResource = new CommunicationInterface();
				//Thread created to listen for messages coming in from the server
				listeningProcedureClientToClient = new Thread(
						new Runnable() {
							public void run() {								
								//While we are connected to the server, receive messages
								if(c2cdin == null) connected = 0;
								while(connected ==1) {
									Athena.recvMesg(c2cdin); //Listen for incomming messages from another client
								}
							}});
				/* Begin start up sequence
				 * 1. Find the status of the buddylist users
				 * 2. Make sure the user's private key exists
				 * 3. Start the listening thread
				 * 4. See if the user's public key exists
				 */
				//Instantiate Buddy List
				instantiateBuddyList();	

				//Check to see if the user's private key is there
				File privateKey = new File("users/" + username + "/keys/" + username + ".priv");
				if(!(privateKey.exists())) {
					//Check to see if the public key is there too
					boolean success = new File("users/" + username + "/keys/").mkdirs();
					if(success) { 
						try {
							receivePrivateKeyFromServer();
						} catch (IOException e) {
							sendBugReport(getStackTraceAsString(e));
							e.printStackTrace();
						}

					}
					else { 
						try {
							receivePrivateKeyFromServer();
						} catch (IOException e) {
							sendBugReport(getStackTraceAsString(e));
							e.printStackTrace();
						}

					}				
				}
				
				usersPrivate = RSACrypto.readPrivKeyFromFile("users/" + username + "/keys/" + username + ".priv", descrypto);
			}
			//Start the thread
			if(listeningProcedureClientToClient != null)listeningProcedureClientToClient.start();
			
			//Check to see if the user's public key is there
			File publicKey = new File("users/" + username + "/keys/" + username + ".pub");
			if (!(publicKey.exists())) {
				getUsersPublicKeyFromAegis(username);
			}
			//Garbage collect
			System.gc();
		} catch( IOException ie ) { 
			sendBugReport(getStackTraceAsString(ie));
			ie.printStackTrace(); 
			loginGUI.dispose();
		} catch (NoSuchAlgorithmException e) {
			sendBugReport(getStackTraceAsString(e));
			loginGUI.dispose();
			e.printStackTrace();
		}
	}

/** 
 * Overloaded connect method for adding a user
 * @overloaded 
 */
	public static void connect() { 

		//Try to connect with and authenticate to the socket
		try {
			try{
				//Connect to auth server at defined port over socket
				c2ssocket = new Socket( serverIP, 7777 );
				c2csocket = new Socket( serverIP, 7778 );
			}catch (Exception e){ 
				//We can't connect to the server at the specified port for some reason
				JOptionPane.showMessageDialog(null,"Could not connect to the server.\nPlease check your Internet connection.\n\n","Connection Error",JOptionPane.ERROR_MESSAGE);
				return;
			}

			//Connection established debug code.
			if(debug==1)System.out.println( "Connected to "+ c2ssocket );

			//Bind the datastreams to the socket in order to send/receive
			c2sdin = new DataInputStream( c2ssocket.getInputStream() );
			c2sdout = new DataOutputStream( c2ssocket.getOutputStream() );

			//Read in server's public key for encryption of headers
			serverPublic = RSACrypto.readPubKeyFromFile("users/Aegis/keys/Aegis.pub");
			System.gc();
		} catch( IOException ie ) { sendBugReport(getStackTraceAsString(ie)); }
	}

	/**
	 * Method instantiate the buddy list
	 * @throws Exception
	 */
	public static void instantiateBuddyList() throws Exception { 	
		//First we need to compare the hash of the buddy list we have to the one on the server to make sure nothing has been changed.
		String hashOfLocalBuddyList = returnHashOfLocalBuddyList(username);
		//Now we need to get the hash of the user's buddy list on the server
		String[] remoteVals = returnHashOfRemoteBuddyList(username);
		long remoteBuddyListModDate = Long.parseLong(remoteVals[1].trim());

		if(debug==2)System.out.println("Hash of local: " + hashOfLocalBuddyList + "\nHash of remote buddylist: " + remoteVals[0]);

		//Now let's compare this hash with the hash on the server
		if((!(hashOfLocalBuddyList.equals(remoteVals[0])))) {
			long localBuddyListModDate = returnLocalModDateOfBuddyList(username);
		
			if((hashOfLocalBuddyList.equals("d41d8cd98f00b204e9800998ecf8427e"))){
				receiveBuddyListFromServer();
			}			
			else if(localBuddyListModDate > remoteBuddyListModDate) {
				//Send buddylist to server!
				if (debug >= 1) System.out.println("SEND BUDDY LIST TO SERVER");
				sendBuddyListToServer();
			}
			else if (localBuddyListModDate == remoteBuddyListModDate) { 
				//DO NOTHING
				if (debug >= 1) System.out.println("DONE");
			}
			else { 
				//Get buddylist from server
				if (debug >= 1) System.out.println("GET BUDDY LIST FROM SERVER");
				receiveBuddyListFromServer();
			}
		}
		else { 
			if(debug>=1)System.out.println("Hashes match!");
		}


		//Grab string array of the buddylist.csv file 
		String[] usernames = returnBuddyListArray();

		//Check entire buddylist and fill hashtable with user online statuses
		for (int i=0; i < usernames.length; i++) { 
			if(debug==1)System.out.println("Current Buddy To Check: " + usernames[i]);
			//Check to see if the user's public key is there
			File pubKey = new File("users/" + username + "/keys/" + usernames[i] + ".pub");
			if(!(pubKey.exists())) { 
				getUsersPublicKeyFromAegis(usernames[i]);
			}
			checkUserStatus(usernames[i]);
		}
		//Counter
		int y=0;
		//Loop through the HashTable of available users and place them in the JList
		for (Enumeration<?> e = clientResource.userStatus.keys(), f = clientResource.userStatus.elements(); y < clientResource.userStatus.size(); y++ ) {
			try { 
				String currentE = e.nextElement().toString();
				if(debug>=1)System.out.println("E: " + currentE);

				String currentF = f.nextElement().toString();
				if(debug>=1)System.out.println("F: " + currentF);

				//If the user is online, add them to your buddylist
				if (currentF.equals("1")) {
					if(debug>=1)System.out.println("Online user:" + currentE);
					clientResource.newBuddyListItems(currentE);						
				}
			} catch (java.util.NoSuchElementException ie) {
			sendBugReport(getStackTraceAsString(ie));
				ie.printStackTrace();
			} catch (Exception eix) {
			sendBugReport(getStackTraceAsString(eix));
				eix.printStackTrace();
			} 
		}

		//Send Message to Aegis letting it know we're logged in
		systemMessage("002");
		//Garbage collect!
		System.gc();
	}


	/**
	 * @Overloaded
	 * This method is called when adding a user to ones buddy list, this immediately checks to see if the inputted user is online
	 */
	public static void instantiateBuddyList(String usernameToInstantiate) throws IOException {

		if(debug>=1)System.out.println("Current Buddy To Check: " + usernameToInstantiate);
		checkUserStatus(usernameToInstantiate, "PauseThread!");
		getUsersPublicKeyFromAegis(usernameToInstantiate);
	}

	/**
	 * This method checks to see if the current user is online 
	 * @param findUserName The username of the user to check 
	 */
	public static void checkUserStatus(String findUserName) {
		try { 
			if(debug>=1)System.out.println("Checking availability for user: "+findUserName);
			//Initalize Result
			int result = -1;
			//Run the systemMessage Method to let Aegis know what we're about to do
			//First contact with Aegis!
			systemMessage("001");
			//Go ahead and send Aegis the user name we want to find 
			c2sdout.writeUTF(encryptServerPublic(findUserName));
			if(debug>=1)System.out.println("Username sent - now listening for result...");
			//Grab result and turn it into an integer
			result = Integer.parseInt(decryptServerPublic(c2sdin.readUTF()));
			//Print result 
			if(debug>=1)System.out.println("Result for user " + findUserName + " is " + result + ".");
			//Call the mapUserStatus method in ClientApplet to fill the Hashtable of user's statuses
			clientResource.mapUserStatus(findUserName, result);
			if(debug>=1)System.out.println("SENT SERVER FLAG 001");

		} catch (Exception e) { 
			sendBugReport(getStackTraceAsString(e));
			if(debug>=1)System.out.println(e);
		}	
	}
	/** This method checks to see on a one user basis if the inputted user is online
	 * @param usernameToCheck The user to check the status
	 * @param checkStatusFlag Boolean flag to designate that the method is overloaded
	 */
	public static void checkUserStatus(String usernameToCheck, String checkStatusFlag) {
		try {
			if(debug>=1)System.out.println("Checking availability for user: " + usernameToCheck);
			//Initialize Result to -1
			int result = -1;
			//Run the systemMessage Method to let Aegis know what we're about to do
			systemMessage("003");
			c2sdout.writeUTF(encryptServerPublic(usernameToCheck));
			System.out.println("Sent username");
			result = Integer.parseInt(decryptServerPublic(c2sdin.readUTF()));
			System.out.println("Got result");
			clientResource.mapUserStatus(usernameToCheck,result);
			if(result==1){
				clientResource.newBuddyListItems(usernameToCheck);
			}
			if(debug>=1)System.out.println("SENT SERVER FLAG 003");

		} catch (Exception e) { 
			sendBugReport(getStackTraceAsString(e));
			if(debug==1)System.out.println(e);
		}	
	}

	/** This method is run in a thread and will recieve and process an incoming message
	 * @param din This DataInputStream is where the messages will come from
	 */
	public static void recvMesg(DataInputStream din){
		try{
			// Who is the message from? 
			String fromUserCipher = din.readUTF();
			// What is the message?
			String encryptedMessage = din.readUTF();

			if(debug==2)System.out.println("Encrypted Message: "  + encryptedMessage);

			RSAPrivateKeySpec usersPrivate = RSACrypto.readPrivKeyFromFile("users/" + username + "/keys/" + username + ".priv", descrypto);

			//Decrypt the fromUser to see what user this message came from!
			String fromUserDecrypted = decryptServerPublic(fromUserCipher);
			//Get the message ready for encryption
			String decryptedMessage;		
			byte[] messageBytes = (new BigInteger(encryptedMessage)).toByteArray();

			if(debug>=1)System.out.println("FROMUSER: " + fromUserDecrypted);
			//If the message is an unavailable user response		
			if(fromUserDecrypted.equals("UnavailableUser")){
				decryptedMessage = decryptServerPublic(encryptedMessage);
				print = (MapTextArea)clientResource.tabPanels.get(decryptedMessage);
				print.writeToTextArea(fromUserDecrypted+": ", print.getSetHeaderFont(Color.red));
				print.writeToTextArea(decryptedMessage+"\n", print.getTextFont());
				return;
			}

			//Remove user from Buddylist
			if(fromUserDecrypted.equals("ServerLogOff")) {
				decryptedMessage = decryptServerPublic(encryptedMessage);
				//Check to see if the user is in your buddy list, if not, don't care
				String[] usernames = returnBuddyListArray();
				for(int x=0;x<usernames.length;x++) {
					if(usernames[x].equals(decryptedMessage)) { 
						//We know that the buddy is in his/her buddy list! 
						clientResource.buddySignOff(decryptedMessage);
						// If enabled, open an input stream  to the audio file.
						if(getEnableSounds())
						{
							InputStream in = new FileInputStream("sounds/signOff.wav");
							// Create an AudioStream object from the input stream.
							AudioStream as = new AudioStream(in);         
							// Use the static class member "player" from class AudioPlayer to play
							// clip.
							AudioPlayer.player.start(as);  
						}
					}
					if((usernames[x].equals(decryptedMessage)) && (clientResource.tabPanels.containsKey(decryptedMessage))) { 
						print = (MapTextArea)clientResource.tabPanels.get(decryptedMessage);
						print.writeToTextArea(decryptedMessage + " has signed off.\n", print.getSetHeaderFont(Color.gray));						
					}
				}	
				return;
			}

			//Create buddy list entry for user sign on
			if(fromUserDecrypted.equals("ServerLogOn")) {
				//Decrypt Message
				decryptedMessage = decryptServerPublic(encryptedMessage);
				if(!(decryptedMessage.equals(username))) 	{
					//Check to see if the user is in your buddylist, if not, don't care
					String[] usernames = returnBuddyListArray();
					for(int x=0;x<usernames.length;x++) {
						if(usernames[x].equals(decryptedMessage)) { 
							//We know that the buddy is in his/her buddy list! 
							clientResource.newBuddyListItems(decryptedMessage);
							//** add this into your application code as appropriate
							if(getEnableSounds())
							{
								// If enabled, open an input stream  to the audio file.
								InputStream in = new FileInputStream("sounds/signOn.wav");
								// Create an AudioStream object from the input stream.
								AudioStream as = new AudioStream(in);         
								// Use the static class member "player" from class AudioPlayer to play
								AudioPlayer.player.start(as);
							}
						}
						if((usernames[x].equals(decryptedMessage)) && (clientResource.tabPanels.containsKey(decryptedMessage))) { 
							print = (MapTextArea)clientResource.tabPanels.get(decryptedMessage);
							print.writeToTextArea(decryptedMessage + " has signed on.\n", print.getSetHeaderFont(Color.gray));						
						}
					}					
					return;	
				}
			}
			else { // Need this else in order to hide the system messages coming from Aegis
				//Compare the digital signature to the hashed message to verify integrity of the message!
				decryptedMessage = RSACrypto.rsaDecryptPrivate(messageBytes,usersPrivate.getModulus(),usersPrivate.getPrivateExponent());
				//if(decryptedMessage.equals(ClientLogin.computeHash("Test"))) {


				//If there isn't already a tab for the conversation, make one
				if(!clientResource.tabPanels.containsKey(fromUserDecrypted)){
					clientResource.makeTab(fromUserDecrypted, false);
				}
				
				//Write message to the correct tab
				print = (MapTextArea)clientResource.tabPanels.get(fromUserDecrypted);
				print.writeToTextArea(fromUserDecrypted+": ", print.getSetHeaderFont(new Color(0, 0, 130))); 
				//print.writeToTextArea(decryptedMessage+"\n", print.getTextFont());
				parseMarkdown(decryptedMessage,print);
				print.moveToEnd();
				//If we are away send the user our away message
				if(away == 1) {
					processMessage(fromUserDecrypted,"Auto reply from user: " + awayText);
				}
				
				if(decryptedMessage.equalsIgnoreCase("lmao")){
					if(getEnableSounds())
					{
						// If enabled, open an input stream  to the audio file.
						InputStream in = new FileInputStream("sounds/lmaoMesg.wav");
						// Create an AudioStream object from the input stream.
						AudioStream as = new AudioStream(in);         
						// Use the static class member "player" from class AudioPlayer to play
						AudioPlayer.player.start(as);
					}
				}
				else if(decryptedMessage.equals("Incoming file transfer...")) { 
					receiveFile();
				}
				// If enabled, open an input stream  to the audio file.
				else if(getEnableSounds())
				{
					InputStream in = new FileInputStream("sounds/recvMesg.wav");
					// Create an AudioStream object from the input stream.
					AudioStream as = new AudioStream(in);         
					// Use the static class member "player" from class AudioPlayer to play
					AudioPlayer.player.start(as);
				}
				System.gc();
			}
		}
		catch ( IOException ie ) {
			//If we can't use the inputStream, we probably aren't connected
			//ndBugReport(getStackTraceAsString(ie));
			//They probably just disconnected, get this off of my screen
			//if(debug>=2)ie.printStackTrace();
			connected=0; 
		} catch (Exception e) {
			sendBugReport(getStackTraceAsString(e));
			if(debug>=1)e.printStackTrace();
		}
	}

	
	//Parse messages for markdown commands before printing them
	public static void parseMarkdown(String mesg, MapTextArea print){
		String message = mesg;
		int bold=0;
		int italic=0;
		int underline=0;
		int x=0;
		int changed=0;
		char current=' ';
		char previous=' ';
		char next=' ';
		MutableAttributeSet currentAttr = print.getTextFont();
		
		//Go through the message, character by character
		for(x=0;x<message.length();x++){
			current = message.charAt(x);
			
			//Only get the previous character if we aren't on the first iteration
			if(x>0)	previous = message.charAt(x-1);
			//Only get the next character if we aren't on the last
			if(x!=message.length()-1) next = message.charAt(x+1);
			
			if(current=='*'){
				if(previous=='\\'){
					try{
					//Print an escaped asterisk
					print.writeToTextArea(String.valueOf(current), print.getTextFont());
					}catch(Exception e){sendBugReport(getStackTraceAsString(e));e.printStackTrace();}
					
				}
				//Or toggle bold/italic based on the number
				else if(next=='*'){
					if(bold==1){
						bold=0;
						changed=1;
						//System.out.print("</b>");
					}
					else{
						bold=1;
						changed=1;
						//System.out.print("<b>");
					}
					x++;
				}
				else{
					if(italic==1){
						italic=0;
						changed=1;
						//System.out.print("</i>");
					}
					else{
						italic=1;
						changed=1;
						//System.out.print("<i>");
					}
				}
			}
			//Check for underline commands
			else if(current=='_'){
				if(previous=='\\'){
					try{
						print.writeToTextArea(String.valueOf(current), print.getTextFont());
					}catch(Exception e){e.printStackTrace();}
				}
				else{
					if(underline==1){
						underline=0;
					}else underline=1;
					changed=1;
				}
			}
			//Otherwise just print text
			else{
				//update font if need be
				if(changed==1){
					boolean b = (bold != 0);
					boolean i = (italic != 0);
					boolean u = (underline != 0);
					print.setTextFont(b, i, u);
					changed = 0;
				}
				//Don't print escape characters
				if(current=='\\' && next=='*'){}
				else if(current=='\\' && next=='_'){}
				//Print text in current formatting
				else{
					try{
					print.writeToTextArea(String.valueOf(current), print.getTextFont());
					}catch(Exception e){e.printStackTrace();}
				}
			}
		}
		try{
			//Newline after parsing the message
			print.writeToTextArea("\n", print.getTextFont());
		}catch(Exception e){
			sendBugReport(getStackTraceAsString(e));
			e.printStackTrace();
		}
		//Revert to default font
		print.setTextFont(currentAttr);
	}
	
	
	//Method for group chat
	/** This method does something
	 * 
	 */
	public static void createChat() { 
		//TODO Make this an actual window
		String chatName = JOptionPane.showInputDialog("Input the name of the chat!");
		try {
		Athena.systemMessage("12");
		
		
		try {
			c2sdout.writeUTF(encryptServerPublic(chatName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		} catch (NullPointerException npe) { 
			
		}
		//TODO Make a new tab to display the group chat window 
	}
	
	/**
	 * This method sends the file to the other user (must initialize direct-connect first!) 
	 * @param filename
	 * @throws IOException 
	 */
	public static void sendFile(File myFile) throws IOException {
		
		toUser = clientResource.imTabbedPane.getTitleAt(clientResource.imTabbedPane.getSelectedIndex());
		
		//Make sure the user is directly connected with the user he/she wants to send a file to
		//if (directConnect == 1) { 
			//TODO Send the file!!!!
			//Grab the file size
			int fileSize = (int) myFile.length();
			
			//Use process message to initiate the file transfer
			try {
				processMessage("Incoming file transfer...");
				//Now the other client knows that a file is coming, therefore call the recieveFile method
				//Send the other user the file size
				processMessage((String.valueOf(fileSize)));
				//Send the other user the file name
				processMessage(myFile.getName());
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			byte [] mybytearray  = new byte [(int)myFile.length()];
		    FileInputStream fis = new FileInputStream(myFile);
		    BufferedInputStream bis = new BufferedInputStream(fis);
		    bis.read(mybytearray,0,mybytearray.length);
		    OutputStream os = c2ssocket.getOutputStream();
		    System.out.println("Sending...");
		    os.write(mybytearray,0,mybytearray.length);
		    os.flush();

		}
	
	/**
	 * This method receives a file from a user (must initialize a direct-connect first!)
	 * @throws IOException 
	 * 
	 */
	public static void receiveFile() throws IOException {
		//Grab the file size
		int filesize = c2sdin.readInt();
		//Grab the file name
		String filename = c2sdin.readUTF();
		
	    byte [] mybytearray  = new byte [filesize];
	    InputStream is = c2ssocket.getInputStream();
	    FileOutputStream fos = new FileOutputStream(filename);
	    BufferedOutputStream bos = new BufferedOutputStream(fos);
	    int bytesRead = is.read(mybytearray,0,mybytearray.length);
	    int current = bytesRead;
	    //Reconstruct the file
	    do {
	        try {
				bytesRead =
				   is.read(mybytearray, current, (mybytearray.length-current));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        if(bytesRead >= 0) current += bytesRead;
	     } while(bytesRead > -1);
	    bos.write(mybytearray, 0 , current);
	    bos.flush();


	}
	//Called from the actionListener on the tf textfield
	//User wants to send a message
	/** This method takes the message the user types and will get it ready to send
	 * @param message The message to send
	 */
	public static void processMessage( String message ) throws BadLocationException {	
		//Get user to send message to from active tab
		toUser = clientResource.imTabbedPane.getTitleAt(clientResource.imTabbedPane.getSelectedIndex());
		//Get the JPanel in the active tab
		print = (MapTextArea)clientResource.tabPanels.get(toUser);
		if(debug>=1)System.out.println("JPANEL : " + print.toString());

		//See if the user is logged in. If yes, send it. If no, error.
		if (debug>=1) System.out.println("USERNAME: " + username);
		if(username.equals("null")){
			print.writeToTextArea("Error: You are not connected!\n", print.getSetHeaderFont(new Color(130, 0, 0)));
			print.moveToEnd();
			print.clearTextField();}
		else{
			//Print the message locally
			print.writeToTextArea(username+": ", print.getSetHeaderFont(new Color(0, 130, 0)));
			//print.writeToTextArea(message+"\n", print.getTextFont());
			parseMarkdown(message,print);
			//Send the message
			try{
				if(message.length() > 245){
					double messageNumbers = (double)message.length()/245;
					double messageNumbersInt = Math.ceil(messageNumbers);
					if(debug>=1)System.out.println("MessageLength: "+message.length()+"\nMessageLength/245: "+messageNumbers+"\nCeiling of that: "+messageNumbersInt);
					String[] messageChunks = new String[(int)messageNumbersInt];
					for(int i=0;i<messageChunks.length;i++){
						int begin=i*245;
						int end = begin+245;
						if(end>message.length()){
							end = message.length()-1;
						}
						messageChunks[i] = message.substring(begin,end);

						//Grab the other user's public key from file
						RSAPublicKeySpec toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + toUser+ ".pub");
						//Encrypt the toUser with the Server's public key and send it to the server

						//Encrypt the message with the toUser's public key and send it to the server
						BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPublic(messageChunks[i], toUserPublic.getModulus(), toUserPublic.getPublicExponent()));
						c2sdout.writeUTF(encryptServerPublic(toUser));
						c2sdout.writeUTF(encryptServerPublic(username));
						c2sdout.writeUTF(messageCipher.toString());
						//Hash the Message for the digital signature
						//String hashedMessage = ClientLogin.computeHash(message);

						// Append own message to IM window
						print.moveToEnd();
						// Clear out text input field
						print.clearTextField();
					}

				}else{

					//Grab the other user's public key from file
					RSAPublicKeySpec toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + toUser+ ".pub");
					//Encrypt the message with the toUser's public key and send it to the server
					BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPublic(message, toUserPublic.getModulus(), toUserPublic.getPublicExponent()));
					c2sdout.writeUTF(encryptServerPublic(toUser));
					c2sdout.writeUTF(encryptServerPublic(username));
					c2sdout.writeUTF(messageCipher.toString());
					//Hash the Message for the digital signature
					//String hashedMessage = ClientLogin.computeHash(message);
					//Send the server the digital signature (Hash of the message encrypted with UserA's private key
					//dout.writeUTF(RSACrypto.rsaEncryptPrivate(hashedMessage, (RSACrypto.readPrivKeyFromFile("users/" + username + "/keys/" + username + ".priv").getModulus()), (RSACrypto.readPrivKeyFromFile("users/" + username + "/keys/" + username + ".priv").getPrivateExponent())).toString());

					// Append own message to IM window
					print.moveToEnd();
					// Clear out text input field
					print.clearTextField();
				}
				//TADA
			} catch( IOException ie ) { 
				if(debug>=1)System.out.println(ie);
				print.writeToTextArea("Error: You probably don't have the user's public key!\n", print.getTextFont());
				print.moveToEnd();
				print.clearTextField();
			} catch (Exception e) {
				sendBugReport(getStackTraceAsString(e));
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}System.gc();
	}
	
	public static void processMessage( String usertoreply, String message ) throws BadLocationException {	
		//Get user to send message to from active tab
		toUser = usertoreply;
		//Get the JPanel in the active tab
		print = (MapTextArea)clientResource.tabPanels.get(toUser);
		if(debug>=1)System.out.println("JPANEL : " + print.toString());

		//See if the user is logged in. If yes, send it. If no, error.
		if (debug>=1) System.out.println("USERNAME: " + username);
		if(username.equals("null")){
			print.writeToTextArea("Error: You are not connected!\n", print.getSetHeaderFont(new Color(130, 0, 0)));
			print.moveToEnd();
			print.clearTextField();}
		else{
			//Print the message locally
			print.writeToTextArea(username+": ", print.getSetHeaderFont(new Color(0, 130, 0)));
			parseMarkdown(message,print);

			//Send the message
			try{
				if(message.length() > 245){
					double messageNumbers = (double)message.length()/245;
					double messageNumbersInt = Math.ceil(messageNumbers);
					if(debug>=1)System.out.println("MessageLength: "+message.length()+"\nMessageLength/245: "+messageNumbers+"\nCeiling of that: "+messageNumbersInt);
					String[] messageChunks = new String[(int)messageNumbersInt];
					for(int i=0;i<messageChunks.length;i++){
						int begin=i*245;
						int end = begin+245;
						if(end>message.length()){
							end = message.length()-1;
						}
						messageChunks[i] = message.substring(begin,end);

						//Grab the other user's public key from file
						RSAPublicKeySpec toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + toUser+ ".pub");
						//Encrypt the toUser with the Server's public key and send it to the server

						//Encrypt the message with the toUser's public key and send it to the server
						BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPublic(messageChunks[i], toUserPublic.getModulus(), toUserPublic.getPublicExponent()));
						c2sdout.writeUTF(encryptServerPublic(toUser));
						c2sdout.writeUTF(encryptServerPublic(username));
						c2sdout.writeUTF(messageCipher.toString());
						//Hash the Message for the digital signature
						//String hashedMessage = ClientLogin.computeHash(message);

						// Append own message to IM window
						print.moveToEnd();
						// Clear out text input field
						print.clearTextField();
					}

				}else{


					//Grab the other user's public key from file
					RSAPublicKeySpec toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + toUser+ ".pub");
					//Encrypt the message with the toUser's public key and send it to the server
					BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPublic(message, toUserPublic.getModulus(), toUserPublic.getPublicExponent()));
					c2sdout.writeUTF(encryptServerPublic(toUser));
					c2sdout.writeUTF(encryptServerPublic(username));
					c2sdout.writeUTF(messageCipher.toString());
					//Hash the Message for the digital signature
					//String hashedMessage = ClientLogin.computeHash(message);
					//Send the server the digital signature (Hash of the message encrypted with UserA's private key
					//dout.writeUTF(RSACrypto.rsaEncryptPrivate(hashedMessage, (RSACrypto.readPrivKeyFromFile("users/" + username + "/keys/" + username + ".priv").getModulus()), (RSACrypto.readPrivKeyFromFile("users/" + username + "/keys/" + username + ".priv").getPrivateExponent())).toString());

					// Append own message to IM window
					print.moveToEnd();
					// Clear out text input field
					print.clearTextField();
				}
				//TADA
			} catch( IOException ie ) { 
				if(debug>=1)System.out.println(ie);
				print.writeToTextArea("Error: You probably don't have the user's public key. Please add them to your list!\n", print.getSetHeaderFont(new Color(130, 0, 0)));
				print.moveToEnd();
				print.clearTextField();
			} catch (Exception e) {
				sendBugReport(getStackTraceAsString(e));
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}System.gc();
	}

	/** This method adds a user to the buddylist
	 * @param usernameToAdd This is the username you woant to add
	 */
	public static void buddyList(String usernameToAdd) throws Exception {		
		//Add the username to a new line in the file
		//Will take in more inputs as we add other functionality to Athena like Pubkey, group, etc

		//Set exists to 0, this means that the usernameToAdd is not already in the buddylist file
		int exists=0;
		BufferedWriter out;
		try {
			//Call the returnBuddyListArray method, this reads in the buddylist file and puts the user names into an array for us to use
			String[] usernames = returnBuddyListArray();

			//This for loop checks to see if the usernameToAdd is already in the buddylist file, if so, set exists to 1
			for(int y=0;y<usernames.length;y++) {
				if(usernames[y].equals(usernameToAdd)) {
					exists=1;
				}
			}

			//If the usernameToAdd IS NOT in the buddylist file, add it
			if(exists == 0) { 
				BigInteger encryptedUsername;
				//Append to the file the usernameToAdd
				File newFile = new File("users/" + username + "/buddylist.csv");
				if(!(newFile.exists())) { 
					boolean success = new File("users/" + username).mkdirs();
					if(success) { 
						newFile.createNewFile();
						out = new BufferedWriter(new FileWriter("./users/" + username + "/buddylist.csv"));
					}
					else { 
						newFile.createNewFile();
					}
				}
				out = new BufferedWriter(new FileWriter("./users/" + username + "/buddylist.csv", true));
				encryptedUsername = new BigInteger(descrypto.encryptData(usernameToAdd.concat(",")));
				out.write(encryptedUsername + "\n");
				out.close();
			}
		}
		catch (IOException e) {
			sendBugReport(getStackTraceAsString(e));
			e.printStackTrace();
		} 
	} 

	/**
	 * This method writes the buddy list to file
	 * @param buddyList String array of the lines of the buddy list
	 */
	static void writeBuddyListToFile(String[] buddyList){
		BigInteger encryptedUsername;
		BufferedWriter out;
		File newFile = new File("users/" + username + "/buddylist.csv");
		try{
			if(!(newFile.exists())) { 
				boolean success = new File("users/" + username).mkdirs();
				if(success) { 
					newFile.createNewFile();
				}
				else { 
					newFile.createNewFile();
				}
			}
			out = new BufferedWriter(new FileWriter("./users/" + username + "/buddylist.csv"));

			for(int i = 0; i < buddyList.length;i++){
				encryptedUsername = new BigInteger(descrypto.encryptData(buddyList[i].concat(",")));
				out.write(encryptedUsername + "\n");
			}
			out.close();
		}catch(Exception e)
		{
			sendBugReport(getStackTraceAsString(e));
			if(debug>=1)System.out.println("ERROR WRITING BUDDYLIST");
			if(debug==2)e.printStackTrace();
		}
	}

	/**
	 * This method writes the buddy list to file
	 * @param buddyList String array of the lines of the buddy list
	 */
	static void writeBuddyListToFile(String[] buddyList, boolean flag){
		BigInteger encryptedUsername;
		BufferedWriter out;
		File newFile = new File("users/" + username + "/buddylist.csv");
		try{
			if(!(newFile.exists())) { 
				boolean success = new File("users/" + username).mkdirs();
				if(success) { 
					newFile.createNewFile();
				}
				else { 
					newFile.createNewFile();
				}
			}
			out = new BufferedWriter(new FileWriter("./users/" + username + "/buddylist.csv"));

			for(int i = 0; i < buddyList.length;i++){
				encryptedUsername = new BigInteger(buddyList[i]);
				out.write(encryptedUsername + "\n");
			}
			out.close();
		}catch(Exception e)
		{
			sendBugReport(getStackTraceAsString(e));
			if(debug>=1)System.out.println("ERROR WRITING BUDDYLIST");
			if(debug==2)e.printStackTrace();
		}
	}
	
	public static void receivePrivateKeyFromServer() throws IOException {
		systemMessage("007");
		//Receive ack message
		System.out.println("Ack message received from server: " + decryptServerPublic(c2sdin.readUTF()));

		int chunks = Integer.parseInt(c2sdin.readUTF());
		if(debug>=1) System.out.println("How many chunks the mod will be: " +chunks);

		//Grab the private key information from the server
		String finalPrivateMod="";
		String[] privateModArray = new String[chunks];
		for(int x=0; x<chunks; x++) { 
			String privateMod = c2sdin.readUTF();
			if(debug==2)System.out.println("PRIVATE MOD: " + privateMod);
			if(!(privateMod.equals("end"))) {
				if (privateModArray.length > 0) {					
					privateModArray[x] = decryptServerPublic(privateMod);
				}
			}
		}

		if (privateModArray.length > 0) {
			finalPrivateMod = privateModArray[0];    // start with the first element
			for (int i=1; i<privateModArray.length; i++) {
				finalPrivateMod = finalPrivateMod + privateModArray[i];
			}
		}

		int expChunks = Integer.parseInt(c2sdin.readUTF());
		if(debug>=1)System.out.println("How many chunks the exp will be: " + expChunks);

		//Grab the private key information from the server
		String finalPrivateExp="";
		String[] privateExpArray = new String[expChunks];
		for(int x=0; x<expChunks; x++) { 
			String privateExp = c2sdin.readUTF();
			if(debug==2)System.out.println("PRIVATE EXP: " + privateExp);

			if(!(privateExp.equals("end"))) {
				if (privateExpArray.length > 0) {					
					privateExpArray[x] = decryptServerPublic(privateExp);
				}
			}
		}

		if (privateExpArray.length > 0) {
			finalPrivateExp = privateExpArray[0];    // start with the first element
			for (int i=1; i<privateExpArray.length; i++) {
				finalPrivateExp = finalPrivateExp + privateExpArray[i];
			}
		}

		if(debug==2)System.out.println("DECRYPTED PRIVATE MOD: " + finalPrivateMod);
		if(debug==2)System.out.println("DECRYPTED PRIVATE EXP: " + finalPrivateExp);

		BigInteger privateMod = new BigInteger(finalPrivateMod);
		BigInteger privateExp = new BigInteger(finalPrivateExp);

		//Write it to the file
		RSACrypto.saveToFile("users/" + username + "/keys/" + username + ".priv", privateMod, privateExp);
	}
	/**
	 * Method receives the buddy list from Aegis
	 * @throws IOException
	 */
	private static void receiveBuddyListFromServer() throws IOException {
		systemMessage("8");//Can't go over 007

		//String array of the buddylist
		String[] buddyListLines;

		//Receive buddylist head(should be begin)
		if(debug>=1)System.out.println("BuddyList header: " + decryptServerPublic(c2sdin.readUTF()));
		//Parse out how many lines buddylist is
		buddyListLines = new String[(Integer.parseInt(decryptServerPublic(c2sdin.readUTF())))];
		for(int y=0; y<buddyListLines.length;y++) { 
			buddyListLines[y] = decryptServerPublic(c2sdin.readUTF());
			if(debug==2)System.out.println("Decrypted buddylist lines: " + buddyListLines[y]);
		}
		writeBuddyListToFile(buddyListLines, true);
		if(debug>=1)System.out.println("Successfully wrote Buddylist to file");

	}
	/**
	 * Method gets the user's public key from Aegis
	 * @param usernameToFind
	 * @throws IOException 
	 */
	public static void getUsersPublicKeyFromAegis(String publicKeyToFind) throws IOException {
		if(debug>=1)System.out.println("Getting " + publicKeyToFind + "'s public key!");
		//Send Aegis event code 004 to let it know what we're doing
		systemMessage("004");
		c2sdout.writeUTF(encryptServerPublic(publicKeyToFind));
		modOfBuddy = new BigInteger(c2sdin.readUTF());
		System.out.println("MODOFBUDDY: "+modOfBuddy.toString());
		if(modOfBuddy.toString().equals("-1")){
			JOptionPane.showMessageDialog(null,"Cannot find user's public key.\nMake sure you typed their username correctly and try again.","Error Retrieving Key",JOptionPane.ERROR_MESSAGE);
		}
		else{
			expOfBuddy = new BigInteger(c2sdin.readUTF());
			writeBuddysPubKeyToFile(publicKeyToFind,modOfBuddy,expOfBuddy);
		}

	}
	/**
	 * This method writes the buddy list to file
	 * @param buddysUsername
	 * @param mod
	 * @param exp
	 * @throws IOException
	 */
	public static void writeBuddysPubKeyToFile(String buddysUsername, BigInteger mod, BigInteger exp) throws IOException { 
		BufferedInputStream is;
		//Let's get the number of lines in the file
		File newFile = new File("users/" + username + "/keys/" + buddysUsername + ".pub");
		if(!(newFile.exists())) { 
			boolean success = new File("users/" + username + "/keys/").mkdirs();
			if(success) { 
				newFile.createNewFile();
				is = new BufferedInputStream(new FileInputStream("users/" + username + "/keys/" + buddysUsername + ".pub"));
				RSACrypto.saveToFile("users/" + username + "/keys/" + buddysUsername + ".pub", mod, exp);
			}
			else { 
				newFile.createNewFile();
				RSACrypto.saveToFile("users/" + username + "/keys/" + buddysUsername + ".pub", mod, exp);			
			}
		}


	}

	//This method will send the user's buddy list to the server line by line
	/**
	 * Method sends the buddy list to Aegis
	 */
	public static boolean sendBuddyListToServer() throws IOException {
		String[] buddylistArray = returnBuddyListArray(true);
		systemMessage("006");
			        
		int numLines = buddylistArray.length;

		//Send Aegis the begin message so it knows that this is beginning of the file
		c2sdout.writeUTF(encryptServerPublic("begin"));
		//Send Aegis the number lines we're sending
		c2sdout.writeUTF(encryptServerPublic(new String(String.valueOf(numLines))));
		for(int x=0; x<buddylistArray.length;x++) {         	          
			//Now send Aegis the file
			c2sdout.writeUTF(encryptServerPublic(buddylistArray[x]));
		}
		return true;
	}
	/**
	 * This encrypts the input string with the server's public key
	 * @param plaintext The plaintext 
	 * @return cipherText The encrypted String
	 */
	public static String encryptServerPublic(String plaintext) { 
		BigInteger cipherText = new BigInteger(RSACrypto.rsaEncryptPublic(plaintext,Athena.serverPublic.getModulus(),Athena.serverPublic.getPublicExponent()));
		return cipherText.toString();
	}

	/**
	 * This methid decrypts the input string with the server's public key
	 * @param ciphertext
	 * @return decrypted message
	 */
	public static String decryptServerPublic(String ciphertext) { 
		//Turn the String into a BigInteger. Get the bytes of the BigInteger for a byte[]
		byte[] cipherBytes = (new BigInteger(ciphertext)).toByteArray();
		//Decrypt the byte[], returns a String
		return RSACrypto.rsaDecryptPublic(cipherBytes,Athena.serverPublic.getModulus(),Athena.serverPublic.getPublicExponent());
	}
	/**
	 * This method returns a string array of the lines from the buddylist
	 * @retrun String array of the buddylist
	 */
	//TODO Make this return a multi-dementional array of all the fields in the CSV File
	public static String[] returnBuddyListArray() throws IOException {
		int count;
		int readChars;
		InputStream is;

		//Let's get the number of lines in the file
		File newFile = new File("users/" + username + "/buddylist.csv");
		if(!(newFile.exists())) { 
			boolean success = new File("users/" + username).mkdirs();
			if(success) { 
				newFile.createNewFile();
				is = new BufferedInputStream(new FileInputStream("users/" + username + "/buddylist.csv"));
			}
			else { 
				newFile.createNewFile();
			}
		}

		is = new BufferedInputStream(new FileInputStream("users/" + username + "/buddylist.csv"));
		byte[] c = new byte[1024];
		count = 0;
		readChars = 0;
		while ((readChars = is.read(c)) != -1) {
			for (int i = 0; i < readChars; ++i) {
				if (c[i] == '\n')
					++count;
			}
		} //End section

		//Make the string array the size of the number of lines in the file
		String[] usernames = new String[count];

		//If there are no lines in the file we know that the user has no buddies! :(
		if (count == 0) { 
			return usernames;
		}
		else { 
			File newFile2 = new File("users/" + username + "/buddylist.csv");
			if(!(newFile2.exists())) { 
				newFile2.createNewFile();
			}
			BufferedReader in = new BufferedReader(new FileReader("users/" + username + "/buddylist.csv")); 
			int x=0;
			String raw, str;
			BigInteger strNum;

			//Split each line on every ',' then take the string before that and add it to the usernames array | God I love split.
			while ((raw = in.readLine()) != null) 
			{ 
				// Read in the BigInteger in String form. Turn it to a BigInteger
				// Turn the BigInteger to a byteArray, and decrypt it.
				strNum = new BigInteger(raw);
				str = descrypto.decryptData(strNum.toByteArray());

				String foo[] = str.split(","); 
				usernames[x] = foo[0];
				x++;
			}
			return usernames;
		}

	}
	//This method returns a nice string array full of the usernames (for now) that are in the buddylist file
	//TODO Make this return a multi-dementional array of all the fields in the CSV File
	public static String[] returnBuddyListArray(boolean flag) throws IOException {
		int count;
		int readChars;
		InputStream is;

		//Let's get the number of lines in the file
		File newFile = new File("users/" + username + "/buddylist.csv");
		if(!(newFile.exists())) { 
			boolean success = new File("users/" + username).mkdirs();
			if(success) { 
				newFile.createNewFile();
				is = new BufferedInputStream(new FileInputStream("./users/" + username + "/buddylist.csv"));
			}
			else { 
				newFile.createNewFile();
			}
		}

		is = new BufferedInputStream(new FileInputStream("./users/" + username + "/buddylist.csv"));
		byte[] c = new byte[1024];
		count = 0;
		readChars = 0;
		while ((readChars = is.read(c)) != -1) {
			for (int i = 0; i < readChars; ++i) {
				if (c[i] == '\n')
					++count;
			}
		} //End section

		//Make the string array the size of the number of lines in the file
		String[] usernames = new String[count];

		//If there are no lines in the file we know that the user has no buddies! :(
		if (count == 0) { 
			return usernames;
		}
		else { 
			File newFile2 = new File("users/" + username + "/buddylist.csv");
			if(!(newFile2.exists())) { 
				newFile2.createNewFile();
			}
			BufferedReader in = new BufferedReader(new FileReader("users/" + username + "/buddylist.csv")); 
			int x=0;
			String raw;
			//Split each line on every ',' then take the string before that and add it to the usernames array | God I love split.
			while ((raw = in.readLine()) != null) 
			{ 
				String foo[] = raw.split(","); 
				usernames[x] = foo[0];
				x++;
			}
			return usernames;
		}
	}
	/**
	 * Method sets the away message text
	 * @param toAwayText
	 */
	public static void setAwayText(String toAwayText) {
		awayText = toAwayText;
	}
	/**
	 * Method sets the buddy status
	 * @param status
	 */
	public static void setStatus(int status) { 
		away = status;
	}

	//Use this method if Contact with Aegis is needed
	/**
	 * This method 
	 */
	public static void systemMessage( String message ) {	
		//Send the message
		try{
			//Send recipient's name and message to server
			c2sdout.writeUTF(encryptServerPublic("Aegis"));
			c2sdout.writeUTF(encryptServerPublic("")); //Blank username field
			c2sdout.writeUTF(encryptServerPublic(message));
		} catch( IOException ie ) {
			ie.printStackTrace();
		}
	}
	/**
	 * This sets the username
	 * @param usernameToSet Username you want to set
	 */
	public static void setUsername(String usernameToSet) { 
		username = usernameToSet;
	}
	/**
	 * Method returns a DOUT for other classes to use
	 * @return DataOutputStream c2sdout
	 */
	public static DataOutputStream returnDOUT() { 
		return c2sdout;
	}

	/**
	 * Method returns a DIN for other classes to use
	 * @return DataInputStream c2sdin
	 */
	public static DataInputStream returnDIN() { 
		return c2sdin;
	}


	/**
	 * Method returns a hash of the local buddylist
	 * @param buddyname Buddylist you want to find the hash of
	 * @retrun String hash of the buddylist
	 */
	public static String returnHashOfLocalBuddyList(String buddyname) throws Exception { 
		String path = "users/".concat(buddyname).concat("/buddylist.csv");
		File buddyList = new File(path);
		if(!buddyList.exists()) { 
			boolean success = new File("users/" + username).mkdirs();
			if(success) { 
				buddyList.createNewFile();
			}
			else { 
				buddyList.createNewFile();
			}
		}
		return FileHash.getMD5Checksum(path);
	}

	/**
	 * Method returns the last date modified for the buddylist
	 * @param buddyname Buddylist you want to find the lastModified from
	 * @return long lastModified of the buddylist
	 */
	private static long returnLocalModDateOfBuddyList(String buddyname) {
		File buddylist = new File("users/" + buddyname + "/buddylist.csv");
		return buddylist.lastModified();
	}

	//This method returns a hash of the remote buddy list
	/**
	 * Method returns the hash of the remote buddylist
	 * @param buddyname Buddylist you want to find the hash of
	 */
	public static String[] returnHashOfRemoteBuddyList(String buddyname) { 
		try { 

			systemMessage("005");

			//Send buddyname
			c2sdout.writeUTF(encryptServerPublic(buddyname));
			String[] remoteValues = new String[2];
			//counter
			int x = 0;
			while(x<=1){ 
				remoteValues[x] = decryptServerPublic(c2sdin.readUTF());
				if(debug>=1)System.out.println("REMOTE VALS " + x + ": " + remoteValues[x]);
				x++;
			}
			if(debug>=1)System.out.println("Completed.");
			return remoteValues;

		}catch (Exception e)  {
			sendBugReport(getStackTraceAsString(e));
			e.printStackTrace();
			return null;
		} 

	}
	/**
	 * 
	 * @param activated
	 */
	public static void setEnableSounds(boolean activated)
	{
		if(activated)
			enableSounds = true;
		else
			enableSounds = false;
	}
	/**
	 * 
	 * @return
	 */
	public static boolean getEnableSounds()
	{
		return enableSounds;
	}

	/**
	 * 
	 */
	public static void disconnect() { 
		try{
			if(c2sdout != null)	c2sdout.close();
			if(c2cdout != null)	c2cdout.close();
			if(c2sdin != null)	c2sdin.close();
			if(c2cdin != null)	c2cdin.close();
			c2ssocket.close();
			c2csocket.close();
			connected=0;
			away=0;
			if(clientResource != null){
				clientResource.setVisible(false);
			}
		}catch(Exception e){
			System.out.println("HAII");
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public static void exit(){
		System.exit(0);
	}

	public static void sendBugReport(String stackTrace){
		int toSend = JOptionPane.showConfirmDialog(null, "Sorry, it looks like something went wrong.\nWould you like to submit this as a bug report?", "File a Bug Report?", JOptionPane.YES_NO_OPTION);
		if(toSend == JOptionPane.YES_OPTION){
			String comments = JOptionPane.showInputDialog("Optional: Any comments about this bug (what were you doing when this happened)?");
			if(comments.equals("")){
				comments = "No comment entered";
			}
			
			systemMessage("8");
			try{
			c2sdout.writeUTF(stackTrace);
			c2sdout.writeUTF(comments);
			}catch(Exception e){e.printStackTrace();}
		}
	}
	public static String getStackTraceAsString(Exception e) {
		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));
		return stackTrace.toString();
	}
	
	/**
	 * 
	 * @param args
	 * @throws AWTException
	 */
	public static void main(String[] args) throws AWTException {
		loginGUI = new AuthenticationInterface();

	}


}
