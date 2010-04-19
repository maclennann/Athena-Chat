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
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Enumeration;
import java.security.MessageDigest;
import java.security.DigestInputStream;
import java.security.NoSuchAlgorithmException;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import sun.security.util.BigInt;



public class Client
{
	//DESCrypto object for cryptography of local files
	static DESCrypto descrypto;

	//Print debug messages?
	public static final int debug=1;

	//Global username variable
	public static String username="null";

	//Splash Screen
	static ClientSplash screen;

	//Recipient for message
	private static String toUser;

	//Client's GUI
	public static ClientApplet clientResource;
	public static ClientLogin loginGUI;

	//TODO: Make otherUsers array read from buddylist.xml
	//DONE 3/30/2010

	// The socket connecting us to the server
	public static Socket socket;

	// The datastreams we use to move data through the socket
	private static DataOutputStream dout;
	private static DataInputStream din;

	//Temporary object for the JPanel in a tab
	static MapTextArea print;

	//Thread that will be used to listen for incoming messages
	static Thread listeningProcedure;
	
	//Flag to control sound notifications
	public static boolean enableSounds;

	//If the client is connect to the server
	static int connected = 0;

	public static String userNameToCheck = null;
	public static String publicKeyToFind = null;
	public static BigInteger modOfBuddy = null;
	public static BigInteger expOfBuddy = null;

	//Aegis' public key
	static RSAPublicKeySpec serverPublic;

	//Exit the program
	public static void exit(){
		System.exit(0);
	}

	//Called from the actionListener on the tf textfield
	//User wants to send a message
	public static void processMessage( String message ) {	
		//Get user to send message to from active tab
		toUser = clientResource.imTabbedPane.getTitleAt(clientResource.imTabbedPane.getSelectedIndex());

		//Get the JPanel in the active tab
		print = (MapTextArea)clientResource.tabPanels.get(toUser);

		//See if the user is logged in. If yes, send it. If no, error.
		if (debug == 1) System.out.println("USERNAME: " + username);
		if(username.equals("null")){
			print.writeToTextArea("Error: You are not connected!\n");
			print.moveToEnd();
			print.clearTextField();}
		else{
			//Print the message locally
			print.writeToTextArea(username+": ");
			print.writeToTextArea(message+"\n");

			//Send the message
			try{
				if(message.length() > 245){
					double messageNumbers = (double)message.length()/245;
					double messageNumbersInt = Math.ceil(messageNumbers);
					System.out.println("MessageLength: "+message.length()+"\nMessageLength/245: "+messageNumbers+"\nCeiling of that: "+messageNumbersInt);
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
						dout.writeUTF(encryptServerPublic(toUser));
						dout.writeUTF(messageCipher.toString());
						//Hash the Message for the digital signature
						String hashedMessage = ClientLogin.computeHash(message);

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
				dout.writeUTF(encryptServerPublic(toUser));
				dout.writeUTF(messageCipher.toString());
				//Hash the Message for the digital signature
				String hashedMessage = ClientLogin.computeHash(message);
				//Send the server the digital signature (Hash of the message encrypted with UserA's private key
				//dout.writeUTF(RSACrypto.rsaEncryptPrivate(hashedMessage, (RSACrypto.readPrivKeyFromFile("users/" + username + "/keys/" + username + ".priv").getModulus()), (RSACrypto.readPrivKeyFromFile("users/" + username + "/keys/" + username + ".priv").getPrivateExponent())).toString());

				// Append own message to IM window
				print.moveToEnd();
				// Clear out text input field
				print.clearTextField();
}
				//TADA
			} catch( IOException ie ) { 
				if(debug==1)System.out.println(ie);
				print.writeToTextArea("Error: You are not connfected!\n");
				print.moveToEnd();
				print.clearTextField();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}System.gc();
	}

	//This method will be used to add to the buddy list
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
		catch (IOException e) { } 
	} 

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
		{if(debug==1)System.out.println("ERROR WRITING BUDDYLIST");
		}
	}

	//When the client receives a message.
	public static void recvMesg(DataInputStream din){
		try{
			// Who is the message from? 
			String fromUserCipher = din.readUTF();
			// What is the message?
			String encryptedMessage = din.readUTF();
			if(debug==1)System.out.println("DFSAFSFD: "  + encryptedMessage);
			// Grab the digital signature 
			//String digitalSignatureCipher = din.readUTF();

			//Don't have to do the below, waste of memory, simple do String.getBytes()
			//BigInteger fromUserBytes = new BigInteger(fromUserCipher);

			//Grab the user's private key - SHHH!!
			String privateKeyPath = "users/" + username + "/keys/" + username + ".priv";
			File privateKey = new File(privateKeyPath);
			if(!(privateKey.exists())) { 
				boolean success = new File("users/" + username + "/keys/").mkdirs();
				if(success) { 
					receivePrivateKeyFromServer();
					
				}
				else { 
					receivePrivateKeyFromServer();
					
				}				
			}
			RSAPrivateKeySpec usersPrivate = RSACrypto.readPrivKeyFromFile("users/" + username + "/keys/" + username + ".priv", descrypto);

			//Decrypt the fromUser to see what user this message came from!
			String fromUserDecrypted = decryptServerPublic(fromUserCipher);
			//Get the message ready for encryption
			String decryptedMessage;		
			byte[] messageBytes = (new BigInteger(encryptedMessage)).toByteArray();
			if(debug==1)System.out.println("FROMUSER: " + fromUserDecrypted);
			//If the message is an unavailabe user response		
			if(fromUserDecrypted.equals("UnavailableUser")){
				decryptedMessage = decryptServerPublic(encryptedMessage);
				print = (MapTextArea)clientResource.tabPanels.get(decryptedMessage);
				print.writeToTextArea(fromUserDecrypted+": ");
				print.writeToTextArea(decryptedMessage+"\n");
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
						//** add this into your application code as appropriate
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
				}

				return;
			}

			if(fromUserDecrypted.equals("CheckUserStatus"))
			{
				decryptedMessage = decryptServerPublic(encryptedMessage);
				if(debug==1)System.out.println(decryptedMessage);
				dout.writeUTF(encryptServerPublic(userNameToCheck));
				return;
			}
			if(fromUserDecrypted.equals("CheckUserStatusResult"))
			{
				decryptedMessage = decryptServerPublic(encryptedMessage);
				int result = Integer.parseInt(decryptedMessage);
				clientResource.mapUserStatus(userNameToCheck, result);
				if (result == 1)
				{
					clientResource.newBuddyListItems(userNameToCheck);						
				}
				getUsersPublicKeyFromAegis(userNameToCheck);
				return;
			}

			if(fromUserDecrypted.equals("ReturnPublicKey")) {
				decryptedMessage = decryptServerPublic(encryptedMessage);
				if(debug==1)System.out.println(decryptedMessage);
				if(debug==1)System.out.println(publicKeyToFind);
				dout.writeUTF(encryptServerPublic(publicKeyToFind));
				return;
			}
			if(fromUserDecrypted.equals("ReturnPublicKeyMod")) { 
				//decryptedMessage = RSACrypto.rsaDecryptPublic(messageBytes,serverPublic.getModulus(),serverPublic.getPublicExponent());
				//String str = decryptedMessage;
				modOfBuddy = new BigInteger(encryptedMessage);
				return;
			}
			if(fromUserDecrypted.equals("ReturnPublicKeyExp")) { 
				//decryptedMessage = RSACrypto.rsaDecryptPublic(messageBytes,serverPublic.getModulus(),serverPublic.getPublicExponent());
				//String str = decryptedMessage;
				expOfBuddy = new BigInteger(encryptedMessage);
				writeBuddysPubKeyToFile(publicKeyToFind, modOfBuddy, expOfBuddy);
				return;
			}

			//Create buddy list entry for user sign on
			if(fromUserDecrypted.equals("ServerLogOn")) {
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
								// clip.
								AudioPlayer.player.start(as);
							}
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
					print.setTextColor(Color.blue);
					print.writeToTextArea(fromUserDecrypted+": "); 
					print.setTextColor(Color.black);
					print.writeToTextArea(decryptedMessage+"\n");
					print.moveToEnd();

					// If enabled, open an input stream  to the audio file.
					if(getEnableSounds())
					{
						InputStream in = new FileInputStream("sounds/recvMesg.wav");
						// Create an AudioStream object from the input stream.
						AudioStream as = new AudioStream(in);         
						// Use the static class member "player" from class AudioPlayer to play
						// clip.
						AudioPlayer.player.start(as);
					}
					//AudioPlayer.player.stop(as);
					System.gc();
					/*} else { 
				//TODO Make some type of alert to the user.
				if(debug==1)System.out.println("MESSAGE COMPROMISED. RUN");
			}*/
				}
			//}
		}
		catch ( IOException ie ) {
			//If we can't use the inputStream, we probably aren't connected
			connected=0; 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void receivePrivateKeyFromServer() throws IOException {
		systemMessage("007");
		//Receive ack message
		System.out.println("Message received from server: " + decryptServerPublic(din.readUTF()));
		
		//Grab the private key information from the server
		String privateMod1 = decryptServerPublic(din.readUTF());
		String privateMod2 = decryptServerPublic(din.readUTF());
		String privateMod3 = decryptServerPublic(din.readUTF());
		String privateMod4 = decryptServerPublic(din.readUTF());
		BigInteger privateMod = new BigInteger(privateMod1.concat(privateMod2).concat(privateMod3).concat(privateMod4));
		BigInteger privateExp = new BigInteger(decryptServerPublic(din.readUTF()));
		
		//Write it to the file
		RSACrypto.saveToFile("users/" + username + "/keys/" + username + ".priv", privateMod, privateExp);
	}

	// Method to connect the user
	public static void connect(String user_name, String password) throws InterruptedException, AWTException, Exception { 
		//Try to connect with and authenticate to the socket
		username = user_name;
		try {
			try{
				//Connect to auth server at defined port over socket
				socket = new Socket( "71.234.132.9", 7777 );
			}catch (Exception e){ 
				//We can't connect to the server at the specified port for some reason
				JOptionPane.showMessageDialog(null,"Could not connect to the server.\nPlease check your Internet connection.\n\n","Connection Error",JOptionPane.ERROR_MESSAGE);
				return;
			}

			//Create the DESCrypto object for buddylist and preferences cryptography
			String saltUser;
			if(username.length()>=8){
				saltUser = username.substring(0,8);
			}else saltUser = username;
			descrypto = new DESCrypto(password,saltUser);

			//Connection established debug code.
			if(debug==1)System.out.println( "Connected to "+socket );


			//Bind the datastreams to the socket in order to send/receive
			din = new DataInputStream( socket.getInputStream() );
			dout = new DataOutputStream( socket.getOutputStream() );

			//Read in the server's public key for encryption of headers
			serverPublic = RSACrypto.readPubKeyFromFile("users/Aegis/keys/Aegis.pub");

			//Send username and password over the socket for authentication
			//FOR NOW MAKE A NEW STRING OUT OF THE CHAR[] BUT WE NEED TO HASH THIS!!!! 
			//String plainTextPassword = new String(password);
			if(debug==1)System.out.println(password);
			dout.writeUTF(encryptServerPublic(username)); //Sending Username
			dout.writeUTF(encryptServerPublic(password)); //Sending Password
			String result = decryptServerPublic(din.readUTF());

			if(debug==1)System.out.println("RESSULTTTT DECRYPTEDDDD: " + result);
			if(result.equals("Failed")) { 
				ClientLoginFailed loginFailed = new ClientLoginFailed();
			}
			else { 
				connected=1;


				clientResource = new ClientApplet();
				//Thread created to listen for messages coming in from the server
				listeningProcedure = new Thread(
						new Runnable() {
							public void run() {
								//While we are connected to the server, receive messages
								while(connected ==1) {
									Client.recvMesg(din);
								}
							}});
				//Instantiate Buddy List
				instantiateBuddyList();	
				//System.gc();
				//Start the thread
				listeningProcedure.start();
			}
		} catch( IOException ie ) { ie.printStackTrace(); } catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Method to connect the user
	public static void connect() { 

		//Try to connect with and authenticate to the socket
		try {
			try{
				//Connect to auth server at defined port over socket
				socket = new Socket( "71.234.132.9", 7777 );
			}catch (Exception e){ 
				//We can't connect to the server at the specified port for some reason
				JOptionPane.showMessageDialog(null,"Could not connect to the server.\nPlease check your Internet connection.\n\n","Connection Error",JOptionPane.ERROR_MESSAGE);
				return;
			}

			//Connection established debug code.
			if(debug==1)System.out.println( "Connected to "+socket );

			//Bind the datastreams to the socket in order to send/receive
			din = new DataInputStream( socket.getInputStream() );
			dout = new DataOutputStream( socket.getOutputStream() );

			//Read in server's public key for encryption of headers
			serverPublic = RSACrypto.readPubKeyFromFile("users/Aegis/keys/Aegis.pub");
			System.gc();
		} catch( IOException ie ) { if(debug==1)System.out.println( ie ); }
	}

	// Disconnect from the server
	public static void disconnect() { 
		try{
			socket.close();
			dout.close();
			din.close();
			connected=0;
			clientResource.setVisible(false);
		}catch(Exception e){}
	}


	// Startup method to initiate the buddy list
	//TODO Make sure the user's status gets changed when they sign on/off
	//DONE 3/30/2010
	public static void instantiateBuddyList() throws Exception { 	
		//First we need to compare the hash of the buddy list we have to the one on the server to make sure nothing has been changed.
		String hashOfLocalBuddyList = returnHashOfLocalBuddyList(username);
		//Now we need to get the hash of the user's buddylist on the server
		String[] remoteVals = returnHashOfRemoteBuddyList(username);
		long remoteBuddyListModDate = Long.parseLong(remoteVals[1].trim());
		
		System.out.println("Hash of local: " + hashOfLocalBuddyList + "\nHash of remote buddylist: " + remoteVals[0]);
		//Now let's compare this hash with the hash on the server
		if(!(hashOfLocalBuddyList.equals(remoteVals[0]))) { 
			long localBuddyListModDate = returnLocalModDateOfBuddyList(username);
			if(localBuddyListModDate < remoteBuddyListModDate) {
				//TODO send buddylist to server!
				System.out.println("SEND BUDDY LIST TO SERVER");
				sendBuddyListToServer();
			}
			else if (localBuddyListModDate == remoteBuddyListModDate) { 
				//TODO NOTHING
				System.out.println("DONE");
			}
			else { 
				//TODO 
				System.out.println("GET BUDDY LIST FROM SERVER");
			}
		}
		else { 
			System.out.println("Hashes match!");
		}
		
		
		//Grab string array of the buddylist.csv file 
		String[] usernames = returnBuddyListArray();

		//Check entire buddylist and fill hashtable with user online statuses
		for (int i=0; i < usernames.length; i++) { 
			if(debug==1)System.out.println("Current Buddy To Check: " + usernames[i]);
			checkUserStatus(usernames[i]);
		}
		//Counter
		int y=0;
		//Loop through the HashTable of available users and place them in the JList
		for (Enumeration<?> e = clientResource.userStatus.keys(), f = clientResource.userStatus.elements(); y < clientResource.userStatus.size(); y++ ) {
			try { 
				String currentE = e.nextElement().toString();
				if(debug==1)System.out.println("E: " + currentE);

				String currentF = f.nextElement().toString();
				if(debug==1)System.out.println("F: " + currentF);

				//If the user is online, add them to your buddylist
				if (currentF.equals("1")) {
					if(debug==1)System.out.println("Online user:" + currentE);
					clientResource.newBuddyListItems(currentE);						
				}
			} catch (java.util.NoSuchElementException ie) { } catch (Exception eix) {
				// TODO Auto-generated catch block
				eix.printStackTrace();
			} 
		}

		//Send Message to Aegis letting it know we're logged in
		systemMessage("002");
		//Garbage collect!
		System.gc();
	}

	// Startup method to initiate the buddy list
	//TODO Make sure the user's status gets changed when they sign on/off
	/*
	 * @Overloaded
	 * This method is called when adding a user to ones buddy list, this immediately checks to see if the inputted user is online
	 */
	public static void instantiateBuddyList(String usernameToCheck) throws IOException {

		if(debug==1)System.out.println("Current Buddy To Check: " + usernameToCheck);
		checkUserStatus(usernameToCheck, "PauseThread!");
	}

	public static void getUsersPublicKeyFromAegis(String usernameToFind) {
		if(debug==1)System.out.println("Getting " + usernameToFind + "'s public key!");
		publicKeyToFind = usernameToFind;
		//Send Aegis event code 004 to let it know what we're doing
		systemMessage("004");

	}

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
	public static boolean sendBuddyListToServer() throws IOException {
			String[] buddylistArray = returnBuddyListArray(true);
			systemMessage("006");
	        System.out.println("Message received from server" + din.readUTF());	        
	        int numLines = buddylistArray.length;
	        
	        //Send Aegis the begin message so it knows that this is beginning of the file
	        dout.writeUTF(encryptServerPublic("begin"));
	        //Send Aegis the number lines we're sending
	        dout.writeUTF(encryptServerPublic(new String(String.valueOf(numLines))));
	        for(int x=0; x<buddylistArray.length;x++) {         	          
	            //Now send Aegis the file
	            dout.writeUTF(encryptServerPublic(buddylistArray[x]));
	        }
	        return true;
	}
	//This method encrypts the plaintext with the server's public key
	public static String encryptServerPublic(String plaintext) { 
		 BigInteger cipherText = new BigInteger(RSACrypto.rsaEncryptPublic(plaintext,Client.serverPublic.getModulus(),Client.serverPublic.getPublicExponent()));
		 return cipherText.toString();
	}
	
	//This method decrypts the ciphertext with the server's public key
	public static String decryptServerPublic(String ciphertext) { 
		//Turn the String into a BigInteger. Get the bytes of the BigInteger for a byte[]
		byte[] cipherBytes = (new BigInteger(ciphertext)).toByteArray();
		//Decrypt the byte[], returns a String
		return RSACrypto.rsaDecryptPublic(cipherBytes,Client.serverPublic.getModulus(),Client.serverPublic.getPublicExponent());
	}
	//This method returns a nice string array full of the usernames (for now) that are in the buddylist file
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
			BufferedReader in = new BufferedReader(new FileReader("./users/" + username + "/buddylist.csv")); 
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
			BufferedReader in = new BufferedReader(new FileReader("./users/" + username + "/buddylist.csv")); 
			int x=0;
			String raw;
			//Split each line on every ',' then take the string before that and add it to the usernames array | God I love split.
			while ((raw = in.readLine()) != null) 
			{ 
				// Read in the BigInteger in String form. Turn it to a BigInteger
				// Turn the BigInteger to a byteArray, and decrypt it.
				//strNum = new BigInteger(raw);
				//str = descrypto.decryptData(strNum.toByteArray());

				String foo[] = raw.split(","); 
				usernames[x] = foo[0];
				x++;
			}
			return usernames;
		}
	}

	public static void checkUserStatus(String findUserName) {
		try { 
			if(debug==1)System.out.println("Checking availability for user: "+findUserName);
			//Initalize Result
			int result = -1;
			//Run the systemMessage Method to let Aegis know what we're about to do
			//First contact with Aegis!
			systemMessage("001");
			//Listen for the incoming Acknowledge message
			din.readUTF(); 
			if(debug==1)System.out.println("Acknowledge message received from server.");
			//Go ahead and send Aegis the user name we want to find 
			dout.writeUTF(encryptServerPublic(findUserName));
			if(debug==1)System.out.println("Username sent - now listening for result...");
			//Grab result and turn it into an integer
			result = Integer.parseInt(decryptServerPublic(din.readUTF()));
			//Print result 
			if(debug==1)System.out.println("Result for user " + findUserName + " is " + result + ".");
			//Call the mapUserStatus method in ClientApplet to fill the Hashtable of user's statuses
			clientResource.mapUserStatus(findUserName, result);
			if(debug==1)System.out.println("SENT SERVER FLAG 001");

		} catch (Exception e) { 
			if(debug==1)System.out.println(e);
		}	
	}
	public static void checkUserStatus(String findUserName, String checkStatusFlag) {
		try {
			userNameToCheck = findUserName;
			//DataInputStream din = new DataInputStream(socket.getInputStream());
			if(debug==1)System.out.println("Checking availability for user: "+userNameToCheck);
			//Initialize Result
			int result = -1;
			//Run the systemMessage Method to let Aegis know what we're about to do
			//First contact with Aegis!
			systemMessage("003");

			if(debug==1)System.out.println("SENT SERVER FLAG 003");
			//listeningProcedure.start();

		} catch (Exception e) { 
			if(debug==1)System.out.println(e);
		}	
	}	

	//Use this method if Contact with Aegis is needed
	public static void systemMessage( String message ) {	
		//Send the message
		try{
			//Send recipient's name and message to server
			dout.writeUTF(encryptServerPublic("Aegis"));
			dout.writeUTF(encryptServerPublic(message));
			//dout.writeUTF("TEST");
		} catch( IOException ie ) {
		}
	}
	public static void setUsername(String usernameToSet) { 
		username = usernameToSet;
	}
	//This method returns a DOUT for other classes to use
	public static DataOutputStream returnDOUT() { 
		return dout;
	}
	//This method returns a DIN for other classes to use
	public static DataInputStream returnDIN() { 
		return din;
	}
	
	//This method returns a hash of the buddy list 
	public static String returnHashOfLocalBuddyList(String buddyname) throws Exception { 
		String path = "users/".concat(buddyname).concat("/buddylist.csv");
		File buddyList = new File(path);
		if(!buddyList.exists()) { 
			boolean success = new File("users/" + username).mkdirs();
			if(success) { 
				buddyList.createNewFile();
				//getBuddyListFromServer();
			}
			else { 
				buddyList.createNewFile();
			}
		}
		return FileHash.getMD5Checksum(path);
	}
	
	//Returns a long of the last day the file was modified
	private static long returnLocalModDateOfBuddyList(String buddyname) {
		File buddylist = new File("users/" + buddyname + "/buddylist.csv");
		return buddylist.lastModified();
	}
	
	//This method returns a hash of the remote buddy list
	public static String[] returnHashOfRemoteBuddyList(String buddyname) { 
		try { 
		
		systemMessage("005");
		
		//Get acknowledge message
		System.out.println(din.readUTF()); 
		
		//Send buddyname
		dout.writeUTF(encryptServerPublic(buddyname));
		String[] remoteValues = new String[2];
		//counter
		int x = 0;
		while(x<=1){ 
		remoteValues[x] = decryptServerPublic(din.readUTF());
		System.out.println("REMOTE VALSSS " + remoteValues[x]);
		x++;
		}
		System.out.println("Completed.");
		return remoteValues;
		
		}catch (Exception e)  {
			System.out.println(e);
			return null;
		} 
		
	}

	private static void splashScreenInit() {
		ImageIcon myImage = new ImageIcon(("splash.jpg"));
		screen = new ClientSplash(myImage);
		screen.setLocationRelativeTo(null);
		screen.setProgressMax(100);
		screen.setScreenVisible(true);
	}
	
	public static void setEnableSounds(boolean activated)
	{
		if(activated)
			enableSounds = true;
		else
			enableSounds = false;
	}
	
	public static boolean getEnableSounds()
	{
		return enableSounds;
	}

	// Create the GUI for the client.
	public static void main(String[] args) throws AWTException {
		//splashScreenInit();

		/*for (int i = 0; i <= 100; i++)
		{
			for (long j=0; j<50000; ++j)
			{
				String takeMoreTime = " " + (j + i);
			}
			screen.setProgress("Loading:" + i, i);  // progress bar with a message
		}*/  
		//splashScreenDestruct();
		//clientResource = new ClientApplet();
		loginGUI = new ClientLogin();

	}
}
