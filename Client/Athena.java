/* Athena/Aegis Encrypted Chat Platform
 * Athena.java: Client backend operations for logging in and routing incoming/outgoing messages
 *
 * Copyright (C) 2010  OlympuSoft
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

//TODO Code review the three major files.
//	   Athena.java, CommunicationInterface.java, ServerThread.java
import java.awt.AWTException;
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Enumeration;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Hashtable;
import javax.crypto.spec.*;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

/**
 * Provides the backend components for the Athena chat client
 */
public class Athena {

    /**
     * Print debug messages during runtime (0=off,1=on,2=verbose)
     */
    public static final int debug = 2; //Show debug messages?
    public static String[] inviteInformationArray;
    /**
     * The current user's username. Used globally
     */
    public static String username = "null";
    /**
     * The server's public key. Used for server communication
     */
    public static RSAPublicKeySpec serverPublic;
    /**
     * The main window. We use this object to manipulate the window.
     */
    protected static CommunicationInterface clientResource;
    /**
     * The login window
     */
    protected static AuthenticationInterface loginGUI;
    /**
     * A MapTextArea object used all over the place to manipulate IM/Chat tabs
     */
    protected static MapTextArea print;
    //End protected variables
    /**
     * Begin private variables
     */
	private static DataInputStream dpInputStream;
    private static String serverIP = "aegis.athenachat.org"; //IP of the server
    private static int connected = 0; 	//If the client is connect to the server
    private static int away = 0; //Is the user away?
    private static SecretKeySpec chatSessionKey, dpSessionKey; //Secret key for group chat session key
    private static DESCrypto descrypto; //DESCrpyto Object for encrypting with user's password
    private static String toUser; //Recipient for message
    private static String awayText; //Away message text
    private static Socket c2ssocket; // The socket connecting us to the server for client communication
    private static Socket c2csocket; // The socket connecting us to the server for server communication
    private static DataOutputStream c2sdout; // Client to Server DataOutputStream
    private static DataInputStream c2sdin; //Client to Server DataInputStream
    private static DataOutputStream c2cdout; //Client to Client DataOutputStream
    private static DataInputStream c2cdin; //Clien to Client DataInputStream
    private static Thread listeningProcedureClientToClient, listeningProcedureDirectProtect, listeningProcedureConnectDirectProtect; //Thread that will be used to listen for incoming messages
    private static boolean enableSounds; //Flag to control sound notifications
    private static BigInteger modOfBuddy = null;
    private static BigInteger expOfBuddy = null;
    private static ServerSocket directProtectSocket; //Direct protect socket!
    private static Socket dpSocket;
    //private static RSAPrivateKeySpec usersPrivate; //User's public key
    public static Hashtable<String, SecretKeySpec> sessionKeys = new Hashtable<String, SecretKeySpec>();
    //End private variables

    /**
     * Method that connects the user with Aegis
     * @param usernameToConnect the username that is entered in the login window
     * @param hashedPassword the hashed password that is entered in the login window
     * @throws AWTException
     * @throws InterruptedException
     * @throws Exception
     */
    public static void connect(String usernameToConnect, String hashedPassword) throws InterruptedException, AWTException, Exception {
        //Try to connect with and authenticate to the socket
        username = usernameToConnect;
        try {
            try {
                //Connect to auth server at defined port over socket
                //This socket is for client -> server coms
                c2ssocket = new Socket(serverIP, 7777);
                //This socket is for client -> client coms
                c2csocket = new Socket(serverIP, 7778);
            } catch (Exception e) {
                //We can't connect to the server at the specified port for some reason
                JOptionPane.showMessageDialog(null, "Could not connect to the server.\n"
                        + "Please check your Internet connection.\n\n", "Connection Error", JOptionPane.ERROR_MESSAGE);
                loginGUI = new AuthenticationInterface();
                return;
            }

            //Create the DESCrypto object for buddylist and preferences cryptography
            String saltUser;
            if (username.length() >= 8) {
                saltUser = username.substring(0, 8);
            } else {
                saltUser = username;
            }
            descrypto = new DESCrypto(hashedPassword, saltUser);

            //Connection established debug code.
            if (debug >= 1) {
                System.out.println("Connected to " + c2ssocket + "<- for client to server communication."); //Client to server coms
            }
            if (debug >= 1) {
                System.out.println("Connected to " + c2csocket + "<- for client to client communication."); //Client to client coms
            }

            //Bind the datastreams to the socket in order to send/receive data
            //These datastreams are for client -> server coms
            c2sdin = new DataInputStream(c2ssocket.getInputStream());
            c2sdout = new DataOutputStream(c2ssocket.getOutputStream());
            //These datastreams are for client -> client coms
            c2cdin = new DataInputStream(c2csocket.getInputStream());
            c2cdout = new DataOutputStream(c2ssocket.getOutputStream());

            //Read in the server's public key for encryption of headers
            serverPublic = RSACrypto.readPubKeyFromFile("users/Aegis/keys/Aegis.pub");

            //Send username and hashed password over the socket for authentication
            if (debug >= 1) {
                System.out.println("User's hashed password: " + hashedPassword);
            }

            c2sdout.writeUTF(encryptServerPublic(username));	   //Sending Username
            c2sdout.writeUTF(encryptServerPublic(hashedPassword)); //Sending Password
            String result = decryptServerPublic(c2sdin.readUTF()); //Read in the result

            if (debug >= 1) {
                System.out.println("Result: " + result);
            }
            if (result.equals("Failed")) {
                disconnect();
                new LoginFailedInterface();
                return;
            } else {
                //We're connected
                connected = 1;
                clientResource = new CommunicationInterface();

                //Thread created to listen for messages coming in from the server
                listeningProcedureClientToClient = new Thread(
                        new Runnable() {

                            public void run() {
                                //While we are connected to the server, receive messages
                                if (c2cdin == null) {
                                    connected = 0;
                                }
                                while (connected == 1) {
                                    Athena.recvMesg(c2cdin); //Listen for incomming messages from another client
                                }
                            }
                        });

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
                if (!(privateKey.exists())) {
                    //Check to see if the public key is there too
                    boolean success = new File("users/" + username + "/keys/").mkdirs();
                    if (success) {
                        try {
                            receivePrivateKeyFromServer();
                        } catch (IOException e) {
                            sendBugReport(getStackTraceAsString(e));
                            e.printStackTrace();
                        }

                    } else {
                        try {
                            receivePrivateKeyFromServer();
                        } catch (IOException e) {
                            sendBugReport(getStackTraceAsString(e));
                            e.printStackTrace();
                        }

                    }
                }

                //usersPrivate = RSACrypto.readPrivKeyFromFile("users/" + username + "/keys/" + username + ".priv", descrypto);
            }
            //Start the thread
            if (listeningProcedureClientToClient != null) {
                listeningProcedureClientToClient.start();
            }

            //Check to see if the user's public key is there
            File publicKey = new File("users/" + username + "/keys/" + username + ".pub");
            if (!(publicKey.exists())) {
                getUsersPublicKeyFromAegis(username);
            }
            //Garbage collect
            System.gc();
        } catch (Exception e) {
            sendBugReport(getStackTraceAsString(e));
            e.printStackTrace();
            loginGUI.dispose();
        }
    }

    /**
     * Overloaded connect method for adding a user
     * @overloaded
     */
    public static void connect() {

        //Try to connect to and authenticate with the socket
        try {
            try {
                //Connect to auth server at defined port over socket
                c2ssocket = new Socket(serverIP, 7777);
                c2csocket = new Socket(serverIP, 7778);

            } catch (Exception e) {
                //We can't connect to the server at the specified port for some reason
                JOptionPane.showMessageDialog(null, "Could not connect to the server.\nPlease check your Internet connection."
                        + "\n\n", "Connection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            //Connection established debug code.
            if (debug == 1) {
                System.out.println("Connected to " + c2ssocket);
            }

            //Bind the datastreams to the socket in order to send/receive
            c2sdin = new DataInputStream(c2ssocket.getInputStream());
            c2sdout = new DataOutputStream(c2ssocket.getOutputStream());

            //Read in server's public key for encryption of headers
            serverPublic = RSACrypto.readPubKeyFromFile("users/Aegis/keys/Aegis.pub");
            System.gc();
        } catch (IOException ie) {
            sendBugReport(getStackTraceAsString(ie));
        }
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

        if (debug == 2) {
            System.out.println("Hash of local: " + hashOfLocalBuddyList + "\nHash of remote buddylist: " + remoteVals[0]);
        }

        //Now let's compare this hash with the hash on the server
        if ((!(hashOfLocalBuddyList.equals(remoteVals[0])))) {
            long localBuddyListModDate = returnLocalModDateOfBuddyList(username);

            if ((hashOfLocalBuddyList.equals("d41d8cd98f00b204e9800998ecf8427e"))) {
                receiveBuddyListFromServer();
            } else if (localBuddyListModDate > remoteBuddyListModDate) {
                //Send buddylist to server!
                if (debug >= 1) {
                    System.out.println("SEND BUDDY LIST TO SERVER");
                }
                sendBuddyListToServer();
            } else if (localBuddyListModDate == remoteBuddyListModDate) {
                //DO NOTHING
                if (debug >= 1) {
                    System.out.println("DONE");
                }
            } else {
                //Get buddylist from server
                if (debug >= 1) {
                    System.out.println("GET BUDDY LIST FROM SERVER");
                }
                receiveBuddyListFromServer();
            }
        } else {
            if (debug >= 1) {
                System.out.println("Hashes match!");
            }
        }


        //Grab string array of the buddylist.csv file
        String[] usernames = returnBuddyListArray();

        //Check entire buddylist and fill hashtable with user online statuses
        for (int i = 0; i < usernames.length; i++) {
            if (debug == 1) {
                System.out.println("Current Buddy To Check: " + usernames[i]);
            }
            //Check to see if the user's public key is there
            File pubKey = new File("users/" + username + "/keys/" + usernames[i] + ".pub");
            if (!(pubKey.exists())) {
                getUsersPublicKeyFromAegis(usernames[i]);
            }
            checkUserStatus(usernames[i]);
        }
        //Counter
        int y = 0;
        //Loop through the HashTable of available users and place them in the JList
        for (Enumeration<?> e = clientResource.userStatus.keys(), f = clientResource.userStatus.elements(); y < clientResource.userStatus.size(); y++) {
            try {
                String currentE = e.nextElement().toString();
                if (debug >= 1) {
                    System.out.println("E: " + currentE);
                }

                String currentF = f.nextElement().toString();
                if (debug >= 1) {
                    System.out.println("F: " + currentF);
                }

                //If the user is online, add them to your buddylist
                if (currentF.equals("1")) {
                    if (debug >= 1) {
                        System.out.println("Online user:" + currentE);
                    }
                    clientResource.newBuddyListItems(currentE);
                }
            } catch (java.util.NoSuchElementException ie) {
                sendBugReport(getStackTraceAsString(ie));
                ie.printStackTrace();
            } catch (Exception ie) {
                sendBugReport(getStackTraceAsString(ie));
                ie.printStackTrace();
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
     * @param usernameToInstantiate Username of buddy to add to the buddylist
     * @throws IOException
     */
    public static void instantiateBuddyList(String usernameToInstantiate) throws IOException {

        if (debug >= 1) {
            System.out.println("Current Buddy To Check: " + usernameToInstantiate);
        }
        checkUserStatus(usernameToInstantiate, "PauseThread!");
        getUsersPublicKeyFromAegis(usernameToInstantiate);
    }

    /**
     * This method checks to see if the current user is online
     * @param findUserName The username of the user to check
     */
    public static void checkUserStatus(String findUserName) {
        try {
            if (debug >= 1) {
                System.out.println("Checking availability for user: " + findUserName);
            }
            //Initalize Result
            int result = -1;
            //Run the systemMessage Method to let Aegis know what we're about to do
            //First contact with Aegis!
            systemMessage("001");
            //Go ahead and send Aegis the user name we want to find
            c2sdout.writeUTF(encryptServerPublic(findUserName));
            if (debug >= 1) {
                System.out.println("Username sent - now listening for result...");
            }
            //Grab result and turn it into an integer
            result = Integer.parseInt(decryptServerPublic(c2sdin.readUTF()));
            //Print result
            if (debug >= 1) {
                System.out.println("Result for user " + findUserName + " is " + result + ".");
            }
            //Call the mapUserStatus method in ClientApplet to fill the Hashtable of user's statuses
            clientResource.mapUserStatus(findUserName, result);
            if (debug >= 1) {
                System.out.println("SENT SERVER FLAG 001");
            }

        } catch (Exception e) {
            sendBugReport(getStackTraceAsString(e));
            if (debug >= 1) {
                e.printStackTrace();
            }
        }
    }

    /** This method checks to see on a one user basis if the inputted user is online
     * @param usernameToCheck The user to check the status
     * @param checkStatusFlag Boolean flag to designate that the method is overloaded
     */
    public static void checkUserStatus(String usernameToCheck, String checkStatusFlag) {
        try {
            if (debug >= 1) {
                System.out.println("Checking availability for user: " + usernameToCheck);
            }
            //Initialize Result to -1
            int result = -1;

            //Run the systemMessage Method to let Aegis know what we're about to do
            systemMessage("003");
            c2sdout.writeUTF(encryptServerPublic(usernameToCheck));
            if (debug >= 1) {
                System.out.println("Sent username");
            }
            result = Integer.parseInt(decryptServerPublic(c2sdin.readUTF()));
            if (debug >= 1) {
                System.out.println("Got result");
            }
            clientResource.mapUserStatus(usernameToCheck, result);
            if (result == 1) {
                clientResource.newBuddyListItems(usernameToCheck);
            }
            if (debug >= 1) {
                System.out.println("SENT SERVER FLAG 003");
            }

        } catch (Exception e) {
            sendBugReport(getStackTraceAsString(e));
            if (debug >= 1) {
                e.printStackTrace();
            }
        }
    }

    /** This method is run in a thread and will receive and process an incoming message
     * @param din This DataInputStream is where the messages will come from
     */
    public static void recvMesg(DataInputStream din) {
        try {
            // Who is the message from?
            String fromUserCipher = din.readUTF();
            // What is the message?
            String encryptedMessage = din.readUTF();

            if (debug == 2) {
                System.out.println("Encrypted Message: " + encryptedMessage);
            }

            RSAPrivateKeySpec usersPrivateKey = RSACrypto.readPrivKeyFromFile("users/" + username + "/keys/" + username + ".priv", descrypto);

            //Decrypt the fromUser to see what user this message came from!
            String fromUserDecrypted = decryptServerPublic(fromUserCipher);
            //Get the message ready for encryption
            String decryptedMessage;
            byte[] messageBytes = (new BigInteger(encryptedMessage)).toByteArray();

            if (debug >= 1) {
                System.out.println("FROMUSER: " + fromUserDecrypted);
            }
            //If the message is an unavailable user response
            if (fromUserDecrypted.equals("UnavailableUser")) {
                decryptedMessage = decryptServerPublic(encryptedMessage);
                print = (MapTextArea) clientResource.tabPanels.get(decryptedMessage);
                print.writeToTextArea(fromUserDecrypted + ": ", print.getSetHeaderFont(Color.red));
                print.writeToTextArea(decryptedMessage + "\n", print.getTextFont());
                return;
            } //Remove user from Buddylist
            else if (fromUserDecrypted.equals("ServerLogOff")) {
                decryptedMessage = decryptServerPublic(encryptedMessage);
                //Check to see if the user is in your buddy list, if not, don't care
                String[] usernames = returnBuddyListArray();
                for (int x = 0; x < usernames.length; x++) {
                    if (usernames[x].equals(decryptedMessage)) {
                        //We know that the buddy is in his/her buddy list!
                        clientResource.buddySignOff(decryptedMessage);
                        // If enabled, open an input stream  to the audio file.
                        if (getEnableSounds()) {
                            InputStream in = new FileInputStream("sounds/signOff.wav");
                            // Create an AudioStream object from the input stream.
                            AudioStream as = new AudioStream(in);
                            // Use the static class member "player" from class AudioPlayer to play
                            // clip.
                            AudioPlayer.player.start(as);
                        }
                    }
                    if ((usernames[x].equals(decryptedMessage)) && (clientResource.tabPanels.containsKey(decryptedMessage))) {
                        print = (MapTextArea) clientResource.tabPanels.get(decryptedMessage);
                        print.writeToTextArea(decryptedMessage + " has signed off.\n", print.getSetHeaderFont(Color.gray));
						if(sessionKeys.containsKey(decryptedMessage)){
							print.writeToTextArea("DirectProtect session aborted!\n", print.getSetHeaderFont(Color.gray));
							print.encType.setText("Encryption Type: RSA - DirectProtect Inactive");
							sessionKeys.remove(decryptedMessage);
						}
                    }
                }
                return;
            } //Create buddy list entry for user sign on
            else if (fromUserDecrypted.equals("ServerLogOn")) {
                //Decrypt Message
                decryptedMessage = decryptServerPublic(encryptedMessage);
                if (!(decryptedMessage.equals(username))) {
                    //Check to see if the user is in your buddylist, if not, don't care
                    String[] usernames = returnBuddyListArray();
                    for (int x = 0; x < usernames.length; x++) {
                        if (usernames[x].equals(decryptedMessage)) {
                            //We know that the buddy is in his/her buddy list!
                            clientResource.newBuddyListItems(decryptedMessage);
                            //** add this into your application code as appropriate
                            if (getEnableSounds()) {
                                // If enabled, open an input stream  to the audio file.
                                InputStream in = new FileInputStream("sounds/signOn.wav");
                                // Create an AudioStream object from the input stream.
                                AudioStream as = new AudioStream(in);
                                // Use the static class member "player" from class AudioPlayer to play
                                AudioPlayer.player.start(as);
                            }
                        }
                        if ((usernames[x].equals(decryptedMessage)) && (clientResource.tabPanels.containsKey(decryptedMessage))) {
                            print = (MapTextArea) clientResource.tabPanels.get(decryptedMessage);
                            print.writeToTextArea(decryptedMessage + " has signed on.\n", print.getSetHeaderFont(Color.gray));
                        }
                    }
                    return;
                }
            } //Pop up chat invite
            else if (fromUserDecrypted.equals("ChatInvite")) {
                decryptedMessage = decryptServerPublic(encryptedMessage);
                String[] chatName = decryptedMessage.split(",");
                if (debug >= 1) {
                    System.out.println("Invitation message we got: " + decryptedMessage);
                }
                int toJoin = JOptionPane.showConfirmDialog(null, "You've been invited to join the group chat: " + chatName[0] + " , by " + chatName[1] + ".");
                if (toJoin == JOptionPane.YES_OPTION) {
                    //Send server a confirm message
                    systemMessage("14");
                    c2sdout.writeUTF(encryptServerPublic(chatName[2]));

                    //Open a tab for the chat
                    clientResource.makeChatTab(chatName[0], chatName[2], true);

                    //Get the user list for the chat from the server
                    getUserList(Integer.parseInt(chatName[2]));

                    //Put a dummy entry in the hashtable until we get the real session key
                    SecretKeySpec nothing = new SecretKeySpec("lol".getBytes(), "AES");
                    sessionKeys.put(chatName[2], nothing);
                }
                return;
            } else if (fromUserDecrypted.equals("SessionKey")) {
                //Take in the encrypted session key
                decryptedMessage = RSACrypto.rsaDecryptPrivate(messageBytes, usersPrivateKey.getModulus(), usersPrivateKey.getPrivateExponent());

                //Split the chatUID from the session key
                String[] chatInfo = decryptedMessage.split(",");

                //Make sure we are actually in the chat this key is for
                if (sessionKeys.containsKey(chatInfo[0])) {
                    byte[] encoded = new BigInteger(chatInfo[1], 16).toByteArray();
                    if (debug >= 1) {
                        System.out.println("FIRST BYTE: " + encoded[0]);
                    }

                    SecretKeySpec aesKey;
                    //If a leading zero-byte shows up, strip it
                    if (encoded[0] == 0) {
                        if (debug >= 1) {
                            System.out.println("Leading zeroes in session key");
                        }

                        byte[] encoded2 = new byte[16];
                        for (int x = 0, y = -1; x < encoded.length; x++, y++) {
                            if (x >= 1) {
                                encoded2[y] = encoded[x];
                            }
                        }
                        aesKey = new SecretKeySpec(encoded2, "AES");
                    } else {
                        aesKey = new SecretKeySpec(encoded, "AES");
                    }

                    //Replace the dummy session key with the real one
                    sessionKeys.remove(chatInfo[0]);
                    sessionKeys.put(chatInfo[0], aesKey);
                    if (debug >= 1) {
                        System.out.println("Session key: " + chatInfo[1]);
                        System.out.println("Session key: " + AESCrypto.asHex(aesKey.getEncoded()));
                    }

                    //Chat join announcement
                    BigInteger messageBigInt = new BigInteger(AESCrypto.encryptMessage(aesKey, "ChatJoin," + username));

                    //Alert the other users!
                    systemMessage("17");
                    c2sdout.writeUTF(encryptServerPublic(chatInfo[0]));
                    c2sdout.writeUTF(messageBigInt.toString());

                    return;
                }
            } else if (fromUserDecrypted.equals("DPSessionKey")) {
                //Take in the encrypted session key
                decryptedMessage = RSACrypto.rsaDecryptPrivate(messageBytes, usersPrivateKey.getModulus(), usersPrivateKey.getPrivateExponent());

                //Split the chatUID from the session key
                String[] chatInfo = decryptedMessage.split(",");

                //Make sure we are actually in the chat this key is for
                if (sessionKeys.containsKey(chatInfo[0])) {
                    byte[] encoded = new BigInteger(chatInfo[1], 16).toByteArray();
                    if (debug >= 1) {
                        System.out.println("FIRST BYTE: " + encoded[0]);
                    }

                    SecretKeySpec aesKey;
                    //If a leading zero-byte shows up, strip it
                    if (encoded[0] == 0) {
                        if (debug >= 1) {
                            System.out.println("Leading zeroes in session key");
                        }

                        byte[] encoded2 = new byte[16];
                        for (int x = 0, y = -1; x < encoded.length; x++, y++) {
                            if (x >= 1) {
                                encoded2[y] = encoded[x];
                            }
                        }
                        aesKey = new SecretKeySpec(encoded2, "AES");
                    } else {
                        aesKey = new SecretKeySpec(encoded, "AES");
                    }

                    //Replace the dummy session key with the real one
                    sessionKeys.remove(chatInfo[0]);
                    sessionKeys.put(chatInfo[0], aesKey);
                    if (debug >= 1) {
                        System.out.println("Session key: " + chatInfo[1]);
                        System.out.println("Session key: " + AESCrypto.asHex(aesKey.getEncoded()));
                    }
                    return;
                }
            } else if (fromUserDecrypted.equals("DPInvite")) {
                //Read in the information
                String inviteInformation = decryptServerPublic(encryptedMessage);
                //inviteInformationArray = inviteInformation.split(",");

                //Open up an alert!
				if ((clientResource.tabPanels.containsKey(inviteInformation))) {
                        print = (MapTextArea) clientResource.tabPanels.get(inviteInformation);
                        print.writeToTextArea(inviteInformation + " has invited us to a Direct Protect session.\n", print.getSetHeaderFont(Color.gray));
                }
                int toJoin = JOptionPane.showConfirmDialog(null, "You've been invited to direct protect with: " + inviteInformation + ".");
                if (toJoin == JOptionPane.YES_OPTION) {
                    //Send server a confirm message
					systemMessage("20");
					c2sdout.writeUTF(encryptServerPublic(inviteInformation));
					c2sdout.writeUTF(encryptServerPublic("yes"));
					
					//Put a dummy entry in the hashtable until we get the real session key
                    SecretKeySpec nothing = new SecretKeySpec("lol".getBytes(), "AES");
                    sessionKeys.put(inviteInformation, nothing);
					//dpSocket = new Socket(inviteInformationArray[1], 7779);
					//dpInputStream = new DataInputStream(dpSocket.getInputStream());
                    if ((clientResource.tabPanels.containsKey(inviteInformation))) {
                        print = (MapTextArea) clientResource.tabPanels.get(inviteInformation);
                        print.writeToTextArea("Joining Direct Protect session with "+inviteInformation+".\n", print.getSetHeaderFont(Color.gray));
						print.encType.setText("Encryption Type: AES - DirectProtect Active");
					}
                }
				else {
					if ((clientResource.tabPanels.containsKey(inviteInformation))) {
                        print = (MapTextArea) clientResource.tabPanels.get(inviteInformation);
                        print.writeToTextArea("Aborting Direct Protect session with "+inviteInformation+".\n", print.getSetHeaderFont(Color.gray));
						print.encType.setText("Encryption Type: RSA - DirectProtect Inactive");
					}
                   //Send server a confirm message
                    systemMessage("20");
                    c2sdout.writeUTF(encryptServerPublic(inviteInformation));
                    c2sdout.writeUTF(encryptServerPublic("no"));
                }
            } else if (fromUserDecrypted.equals("DPResult")) {
                decryptedMessage = decryptServerPublic(encryptedMessage);
                String[] inviteInformation = decryptedMessage.split(",");
				if(inviteInformation[1].equals("yes")){
					if ((clientResource.tabPanels.containsKey(inviteInformation[0]))) {
						print = (MapTextArea) clientResource.tabPanels.get(inviteInformation[0]);
						print.writeToTextArea("Direct Protect session started!\n", print.getSetHeaderFont(Color.gray));
						print.encType.setText("Encryption Type: AES - DirectProtect Active");
					}
				}
				else{
					if ((clientResource.tabPanels.containsKey(inviteInformation[0]))) {
						print = (MapTextArea) clientResource.tabPanels.get(inviteInformation[0]);
						print.writeToTextArea("DirectProtect session aborted!\n", print.getSetHeaderFont(Color.gray));
						print.encType.setText("Encryption Type: RSA - DirectProtect Inactive");
					}
					if(sessionKeys.containsKey(inviteInformation[0])){
						sessionKeys.remove(inviteInformation[0]);
					}
				}
                             
            }
			 else if (fromUserDecrypted.equals("FileInvite")) {
                //Read in the information
                String inviteInformation = RSACrypto.rsaDecryptPrivate(messageBytes, usersPrivateKey.getModulus(), usersPrivateKey.getPrivateExponent());
                inviteInformationArray = inviteInformation.split(",");

                //Open up an alert!
				if ((clientResource.tabPanels.containsKey(inviteInformation))) {
                        print = (MapTextArea) clientResource.tabPanels.get(inviteInformation);
                        print.writeToTextArea(inviteInformationArray[0] + " wants to send us a file. File name: " + inviteInformationArray[1] + "\n", print.getSetHeaderFont(Color.gray));
                }
                int toJoin = JOptionPane.showConfirmDialog(null, inviteInformationArray[0] + " wants to send us a file. File name: " + inviteInformationArray[1] + ".");
                if (toJoin == JOptionPane.YES_OPTION) {
                    //Send server a confirm message
					systemMessage("22");
					c2sdout.writeUTF(encryptServerPublic(inviteInformationArray[0]));
					String inviteString = username+","+inviteInformationArray[1]+","+"yes";
					//Grab the other user's public key from file
					RSAPublicKeySpec toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + toUser + ".pub");
					//Encrypt the toUser with the Server's public key and send it to the server
					//Encrypt the message with the toUser's public key and send it to the server
					BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPublic(inviteString, toUserPublic.getModulus(), toUserPublic.getPublicExponent()));
					c2sdout.writeUTF(messageCipher.toString());

					if ((clientResource.tabPanels.containsKey(inviteInformationArray[0]))) {
						print = (MapTextArea) clientResource.tabPanels.get(inviteInformationArray[0]);
						print.writeToTextArea("File transfer session started!\n", print.getSetHeaderFont(Color.gray));
						//print.encType.setText("Encryption Type: AES - DirectProtect Active");
					}

					receiveFile(inviteInformationArray[0],inviteInformationArray[1],inviteInformationArray[2]); //Receieve the file!
				}
				else {
					//Send server a confirm message
                    systemMessage("22");
                    c2sdout.writeUTF(encryptServerPublic(inviteInformationArray[0]));
                    String inviteString = username+","+inviteInformationArray[1]+","+"no";
					//Grab the other user's public key from file
					RSAPublicKeySpec toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + toUser + ".pub");
					//Encrypt the toUser with the Server's public key and send it to the server
					//Encrypt the message with the toUser's public key and send it to the server
					BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPublic(inviteString, toUserPublic.getModulus(), toUserPublic.getPublicExponent()));
					c2sdout.writeUTF(messageCipher.toString());
					if ((clientResource.tabPanels.containsKey(inviteInformationArray[0]))) {
						print = (MapTextArea) clientResource.tabPanels.get(inviteInformationArray[0]);
						print.writeToTextArea("File transfer session aborted!\n", print.getSetHeaderFont(Color.gray));
						//print.encType.setText("Encryption Type: AES - DirectProtect Active");
					}
                }
            } else if (fromUserDecrypted.equals("FileResult")) {
                decryptedMessage = RSACrypto.rsaDecryptPrivate(messageBytes, usersPrivateKey.getModulus(), usersPrivateKey.getPrivateExponent());
                String[] inviteInformation = decryptedMessage.split(",");
				if(inviteInformation[2].equals("yes")){
					if ((clientResource.tabPanels.containsKey(inviteInformation[0]))) {
						print = (MapTextArea) clientResource.tabPanels.get(inviteInformation[0]);
						print.writeToTextArea("File transfer session started!\n", print.getSetHeaderFont(Color.gray));
						//print.encType.setText("Encryption Type: AES - DirectProtect Active");
						transferFile(new File(inviteInformation[1]));
					}
				}
				else{
					if ((clientResource.tabPanels.containsKey(inviteInformation[0]))) {
						print = (MapTextArea) clientResource.tabPanels.get(inviteInformation[0]);
						print.writeToTextArea("File transfer session aborted!\n", print.getSetHeaderFont(Color.gray));
						//print.encType.setText("Encryption Type: RSA - DirectProtect Inactive");
					}
				}

            } else { // Need this else in order to hide the system messages coming from Aegis
                //TODO Implement digital signatures
                decryptedMessage = RSACrypto.rsaDecryptPrivate(messageBytes, usersPrivateKey.getModulus(), usersPrivateKey.getPrivateExponent());

                //If there isn't already a tab for the conversation, make one
                if (!(clientResource.tabPanels.containsKey(fromUserDecrypted)) && !(sessionKeys.containsKey(fromUserDecrypted))) {
                    clientResource.makeTab(fromUserDecrypted, false);

                    //We have to actually print the message now because we have a sane if/else setup
                    //Write message to the correct tab
                    print = (MapTextArea) clientResource.tabPanels.get(fromUserDecrypted);
                    print.writeToTextArea(fromUserDecrypted + ": ", print.getSetHeaderFont(new Color(0, 0, 130)));
                    parseMarkdown(decryptedMessage, print);
                    print.moveToEnd();

                    //If we are away send the user our away message
                    if (away == 1) {
                        processMessage(fromUserDecrypted, "Auto reply from user: " + awayText);
                    }

                    //Play laugh if sound is enabled
                    if (decryptedMessage.equalsIgnoreCase("lmao")) {
                        if (getEnableSounds()) {
                            // If enabled, open an input stream  to the audio file.
                            InputStream in = new FileInputStream("sounds/lmaoMesg.wav");
                            // Create an AudioStream object from the input stream.
                            AudioStream as = new AudioStream(in);
                            // Use the static class member "player" from class AudioPlayer to play
                            AudioPlayer.player.start(as);
                        }
                    } /*else if (decryptedMessage.equals("Incoming file transfer...")) {
                    receiveFile();
                    } // If enabled, open an input stream  to the audio file.*/ else if (getEnableSounds()) {
                        InputStream in = new FileInputStream("sounds/recvMesg.wav");
                        // Create an AudioStream object from the input stream.
                        AudioStream as = new AudioStream(in);
                        // Use the static class member "player" from class AudioPlayer to play
                        AudioPlayer.player.start(as);
                    }

                    System.gc();
                } //Write to an open chat tab
                else if (sessionKeys.containsKey(fromUserDecrypted) && !(clientResource.tabPanels.containsKey(fromUserDecrypted))) {
                    if (debug >= 1) {
                        System.out.println("This is a chat message");
                    }
                    //Find the chat tab
                    for (int z = 0; z < clientResource.imTabbedPane.getTabCount(); z++) {
                        JPanel tabToCheck = (JPanel) clientResource.imTabbedPane.getComponentAt(z);
                        if (tabToCheck.getName().equals(fromUserDecrypted)) {
                            //Decrypt the message with the session key
                            decryptedMessage = decryptAES(fromUserDecrypted, encryptedMessage);
                            //Split the username from the message
                            String[] chatMessage = decryptedMessage.split(",", 2);
                            //Get the MapTextArea so we can write the message to it
                            print = (MapTextArea) clientResource.tabPanels.get(clientResource.imTabbedPane.getTitleAt(z));
                            if ((chatMessage[0].equals("ChatLeave"))) {
                                //Format a chat leave message
                                print.writeToTextArea(chatMessage[1] + " has left the chat.\n", print.getSetHeaderFont(Color.gray));
                                clientResource.chatSignOff(chatMessage[1]);
                            } else if (chatMessage[0].equals("ChatJoin")) {
                                //Format a chat join message
                                print.writeToTextArea(chatMessage[1] + " has joined the chat.\n", print.getSetHeaderFont(Color.gray));
                                clientResource.newChatListItems(chatMessage[1]);
                            } else {
                                print.writeToTextArea(chatMessage[0] + ": ", print.getSetHeaderFont(new Color(0, 0, 130)));
                                parseMarkdown(chatMessage[1], print);
                            }
                            print.moveToEnd();
                            break;
                        }
                    }
                } //Write to an open IM tab
                else {
					if(sessionKeys.containsKey(fromUserDecrypted)){
						decryptedMessage = decryptAES(fromUserDecrypted, encryptedMessage);
					}
                    //Write message to the correct tab
                    print = (MapTextArea) clientResource.tabPanels.get(fromUserDecrypted);
                    print.writeToTextArea(fromUserDecrypted + ": ", print.getSetHeaderFont(new Color(0, 0, 130)));
                    parseMarkdown(decryptedMessage, print);
                    print.moveToEnd();

                    //If we are away send the user our away message
                    if (away == 1) {
                        processMessage(fromUserDecrypted, "Auto reply from user: " + awayText);
                    }

                    //Play the lmao sound
                    if (decryptedMessage.equalsIgnoreCase("lmao")) {
                        if (getEnableSounds()) {
                            // If enabled, open an input stream  to the audio file.
                            InputStream in = new FileInputStream("sounds/lmaoMesg.wav");
                            // Create an AudioStream object from the input stream.
                            AudioStream as = new AudioStream(in);
                            // Use the static class member "player" from class AudioPlayer to play
                            AudioPlayer.player.start(as);
                        }
                    }  // If enabled, open an input stream  to the audio file.
                    else if (getEnableSounds()) {
                        InputStream in = new FileInputStream("sounds/recvMesg.wav");
                        // Create an AudioStream object from the input stream.
                        AudioStream as = new AudioStream(in);
                        // Use the static class member "player" from class AudioPlayer to play
                        AudioPlayer.player.start(as);
                    }

                    System.gc();
                }
            }
        } catch (IOException ie) {
            connected = 0;
        } catch (Exception e) {
            sendBugReport(getStackTraceAsString(e));
            if (debug >= 1) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the userlist of a chat
     * @param chatUID The UID of the chat of which you want the userlist
     */
    public static void getUserList(int chatUID) {
        try {
            systemMessage("18");
            c2sdout.writeUTF(encryptServerPublic(String.valueOf(chatUID)));
            String userList = decryptServerPublic(c2sdin.readUTF());

            //Split the userlist and add it to the GUI
            String[] users = userList.split(",");
            for (int x = 0; x < users.length; x++) {
                clientResource.newChatListItems(users[x]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse messages for markdown commands before printing them
     * @param mesg The message you want to parse for markdown
     * @param print The tab to write the parsed message to
     */
    public static void parseMarkdown(String mesg, MapTextArea print) {
        String message = mesg;
        int bold = 0;
        int italic = 0;
        int underline = 0;
        int x = 0;
        int changed = 0;
        char current = ' ';
        char previous = ' ';
        char next = ' ';
        MutableAttributeSet currentAttr = print.getTextFont();

        //Go through the message, character by character
        for (x = 0; x < message.length(); x++) {
            current = message.charAt(x);

            //Only get the previous character if we aren't on the first iteration
            if (x > 0) {
                previous = message.charAt(x - 1);
            }
            //Only get the next character if we aren't on the last
            if (x != message.length() - 1) {
                next = message.charAt(x + 1);
            }

            if (current == '*') {
                if (previous == '\\') {
                    try {
                        //Print an escaped asterisk
                        print.writeToTextArea(String.valueOf(current), print.getTextFont());
                    } catch (Exception e) {
                        sendBugReport(getStackTraceAsString(e));
                        e.printStackTrace();
                    }

                } //Or toggle bold/italic based on the number
                else if (next == '*') {
                    if (bold == 1) {
                        bold = 0;
                        changed = 1;
                        //System.out.print("</b>");
                    } else {
                        bold = 1;
                        changed = 1;
                        //System.out.print("<b>");
                    }
                    x++;
                } else {
                    if (italic == 1) {
                        italic = 0;
                        changed = 1;
                        //System.out.print("</i>");
                    } else {
                        italic = 1;
                        changed = 1;
                        //System.out.print("<i>");
                    }
                }
            } //Check for underline commands
            else if (current == '_') {
                if (previous == '\\') {
                    try {
                        print.writeToTextArea(String.valueOf(current), print.getTextFont());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (underline == 1) {
                        underline = 0;
                    } else {
                        underline = 1;
                    }
                    changed = 1;
                }
            } //Otherwise just print text
            else {
                //update font if need be
                if (changed == 1) {
                    boolean b = (bold != 0);
                    boolean i = (italic != 0);
                    boolean u = (underline != 0);
                    print.setTextFont(b, i, u);
                    changed = 0;
                }
                //Don't print escape characters
                if (current == '\\' && next == '*') {
                } else if (current == '\\' && next == '_') {
                } //Print text in current formatting
                else {
                    try {
                        print.writeToTextArea(String.valueOf(current), print.getTextFont());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        try {
            //Newline after parsing the message
            print.writeToTextArea("\n", print.getTextFont());
        } catch (Exception e) {
            sendBugReport(getStackTraceAsString(e));
            e.printStackTrace();
        }
        //Revert to default font
        print.setTextFont(currentAttr);
    }

    /**
     *  Method for creating a group chat
     * @param chatName The name of the chat to create
     * @return The UID of the created chat, or -1 for failure
     */
    public static String createChat(String chatName) {
        try {
            systemMessage("12");

            try {
                c2sdout.writeUTF(encryptServerPublic(chatName));
                String chatUID = decryptServerPublic(c2sdin.readUTF());
                chatSessionKey = AESCrypto.generateKey();

                //Save the session key to a Hashtable
                sessionKeys.put(chatUID, chatSessionKey);
                return chatUID;
            } catch (IOException e) {
                e.printStackTrace();
                return "-1";
            }
        } catch (NullPointerException npe) {
            return "-1";
        }
    }

	public static void leaveDP(String user) {
		try{
		if(sessionKeys.containsKey(user)){
			sessionKeys.remove(user);
			systemMessage("20");
            c2sdout.writeUTF(encryptServerPublic(user));
            c2sdout.writeUTF(encryptServerPublic("no"));
			if ((clientResource.tabPanels.containsKey(user))) {
				print = (MapTextArea) clientResource.tabPanels.get(user);
				print.writeToTextArea("DirectProtect session has been terminated.\n", print.getSetHeaderFont(Color.gray));
				print.encType.setText("Encryption Type: RSA - DirectProtect Inactive");
           }
		}}catch(Exception e){}
	}
    /**
     *
     */
    public static void directProtect(String inviteUser) {
        try {
            //Create the session key
            dpSessionKey = AESCrypto.generateKey();
            //Alert Aegis of our invite!
            systemMessage("19");
			System.out.println("We're sending a DP invite");

            //Send the user we're connecting to
            c2sdout.writeUTF(encryptServerPublic(inviteUser));
			System.out.println("Username sent");
			
            //Send the user our session key
            String keyString = AESCrypto.asHex(dpSessionKey.getEncoded());
            RSAPublicKeySpec toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + inviteUser + ".pub");
            BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPublic(username + "," + keyString, toUserPublic.getModulus(), toUserPublic.getPublicExponent()));
            c2sdout.writeUTF(messageCipher.toString());
			System.out.println("Session key sent");
			
			sessionKeys.put(inviteUser, dpSessionKey);

			if ((clientResource.tabPanels.containsKey(inviteUser))) {
                        print = (MapTextArea) clientResource.tabPanels.get(inviteUser);
                        print.writeToTextArea("Inviting "+inviteUser+" to a Direct Protect session.\n", print.getSetHeaderFont(Color.gray));
            }

        } catch (Exception ie) {
        }
    }

    /**
     * Invite buddies to a group chat
     * @param inviteUsers An array of usernames to invite
     * @param myChatUID The chatUID to invite the users to
     * @param chatName The name of the chat associated with the chatUID
     * @throws IOException
     */
    public static void inviteUsers(String[] inviteUsers, String myChatUID, String chatName) throws IOException {
        //Get the session key for this chat
        String keyString = AESCrypto.asHex(sessionKeys.get(myChatUID).getEncoded());

        //Send Aegis the information
        systemMessage("16");
        c2sdout.writeUTF(encryptServerPublic(myChatUID));
        if (debug >= 1) {
            System.out.println("Sent chatUID " + myChatUID);
        }
        c2sdout.writeUTF(encryptServerPublic(chatName));
        if (debug >= 1) {
            System.out.println("Sent chatName " + chatName);
        }
        c2sdout.writeUTF(encryptServerPublic(String.valueOf(inviteUsers.length)));
        if (debug >= 1) {
            System.out.println("Sent length " + inviteUsers.length);
        }
        for (int x = 0; x < inviteUsers.length; x++) {
            c2sdout.writeUTF(encryptServerPublic(inviteUsers[x]));
            RSAPublicKeySpec toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + inviteUsers[x] + ".pub");
            BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPublic(myChatUID + "," + keyString, toUserPublic.getModulus(), toUserPublic.getPublicExponent()));
            c2sdout.writeUTF(messageCipher.toString());
            if (debug >= 1) {
                System.out.println("Sent user " + inviteUsers[x]);
            }
        }

    }

    /**
     * Leave a group chat
     * @param myChatUID The chatUID to depart from
     */
    public static void leaveChat(String myChatUID) {
        SecretKeySpec skeySpec = sessionKeys.get(myChatUID);
        BigInteger messageBigInt = new BigInteger(AESCrypto.encryptMessage(skeySpec, "ChatLeave," + username));

        //Alert the other users!
        systemMessage("17");
        try {
            c2sdout.writeUTF(encryptServerPublic(myChatUID));
            c2sdout.writeUTF(messageBigInt.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Let Aegis know that we're leaving the chat
        systemMessage("15");
        //Send Aegis the chatUID
        try {
            c2sdout.writeUTF(encryptServerPublic(myChatUID));
            sessionKeys.remove(myChatUID);
            System.gc();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method sends the file to the other user (must initialize direct-connect first!)
     * @param myFile The filename to send
     * @throws IOException
     */
    public static void sendFile(File myFile) throws IOException {

        toUser = clientResource.imTabbedPane.getTitleAt(clientResource.imTabbedPane.getSelectedIndex());

        //TODO Send the file!
        //Grab the file size
        int fileSize = (int) myFile.length();

        //Use process message to initiate the file transfer
 
		systemMessage("21");
		//Send the server the user to invite, the filename and then the file size
		String inviteString = username + "," + myFile.getPath() + "," + String.valueOf(fileSize);

		//Grab the other user's public key from file
		RSAPublicKeySpec toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + toUser + ".pub");
        //Encrypt the toUser with the Server's public key and send it to the server
        //Encrypt the message with the toUser's public key and send it to the server
        BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPublic(inviteString, toUserPublic.getModulus(), toUserPublic.getPublicExponent()));
        c2sdout.writeUTF(encryptServerPublic(toUser));			
		c2sdout.writeUTF(messageCipher.toString());
    }

	public static void transferFile(File myFile) throws IOException {
		//Grab the other user's public key from file
		toUser = clientResource.imTabbedPane.getTitleAt(clientResource.imTabbedPane.getSelectedIndex());

		ServerSocket fileSS = new ServerSocket(7779);
		Socket fileSocket = fileSS.accept();
		OutputStream os = fileSocket.getOutputStream();
		
		byte[] mybytearray = new byte[(int) myFile.length()];
		
		FileInputStream fis = new FileInputStream(myFile);
		BufferedInputStream bis = new BufferedInputStream(fis);
		bis.read(mybytearray, 0, mybytearray.length);
		//TODO COMMENTS JESUS
		String fileString = new String(mybytearray);
		System.out.println("FILE STRING: " +fileString);
		String encryptedFile = encryptAES(toUser, fileString);
		BigInteger encryptedBigInt = new BigInteger(encryptedFile);
		byte[] encryptedByteArray = encryptedBigInt.toByteArray();

		if (debug >= 1) {
			System.out.println("Sending...");
		}
		os.write(encryptedByteArray, 0, encryptedByteArray.length);
		os.flush();


	}
    /**
     * This method receives a file from a user (must initialize a direct-connect first!)
     * @throws IOException
     */
    public static void receiveFile(String fromUser, String filePath, String fileSize) throws IOException {
		JOptionPane.showMessageDialog(null, "Receiving a file.");
		Socket fileSocket = null;
		while(fileSocket == null){
			fileSocket = new Socket("71.232.78.143", 7779);
		}
		JOptionPane.showMessageDialog(null, "Connected to user.");
		InputStream is = fileSocket.getInputStream();
		
        byte[] mybytearray = new byte[Integer.parseInt(fileSize)];

		//Get the filename
		String filePathReplace = filePath.replace("\\", ",");
		String[] filePathArray = filePathReplace.split(",");
		int arrSize = filePathArray.length;

		//TODO Check to see if the downloads folder exists! 
        FileOutputStream fos = new FileOutputStream("users/" + username + "/downloads/" + filePathArray[arrSize-1]);
		JOptionPane.showMessageDialog(null, "Opened file for writiiiiing.");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
		JOptionPane.showMessageDialog(null, "Reading " +fileSize+" bytes.");
		int bytesRead = is.read(mybytearray, 0, mybytearray.length);
		int current = bytesRead;
		JOptionPane.showMessageDialog(null, "Read "+current+"bytes on first read.");
		//Reconstruct the file
		do {
			try {
				bytesRead =	is.read(mybytearray, current, (mybytearray.length - current));
				//JOptionPane.showMessageDialog(null, "Read "+bytesRead+" more bytes.");
			} catch (IOException e) {

				e.printStackTrace();
			}
			if (bytesRead > 0) {
				current += bytesRead;
			}
		} while (bytesRead > 0);
		JOptionPane.showMessageDialog(null, "Done reading bytes, read "+current+" bytes.");
		//String encryptedFile = is.readUTF(); //Encrypted
		//String fileString = decryptAES(fromUser, encryptedFile); //Encrpyted
		//String fileString = is.readUTF();
		
		//BigInteger fileBInt = new BigInteger(fileString);
		//mybytearray = fileBInt.toByteArray();
		//TODO COMMENTS!!
		BigInteger encryptedBigInt = new BigInteger(mybytearray);
<<<<<<< HEAD
		String encryptedFile = String.valueOf(encryptedBigInt);
=======
		String encryptedFile = new String(new BigInteger(mybytearray).toString());
>>>>>>> 905c3ac1081e4f511a9c402c270c79351c2b2df3
		String decryptedFile = decryptAES(fromUser, encryptedFile);
		byte[] decryptedFileByteArray = decryptedFile.getBytes();

        bos.write(decryptedFileByteArray);
        bos.flush();
		JOptionPane.showMessageDialog(null, "Done/Closing file file.");


		/* byte[] mybytearray = new byte[filesize];
		InputStream is = c2ssocket.getInputStream();
		FileOutputStream fos = new FileOutputStream(filename);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		
		bos.write(mybytearray, 0, current);
		bos.flush();*/



    }

    /**
     * This method returns the cipher text of the message encrypted with a sessionKey
     * @param myChatUID The UID for the chat the message belongs to (to find the session key)
     * @param message The plaintext message
     * @return The encrypted message
     */
    public static String encryptAES(String myChatUID, String message) {
        SecretKeySpec sessionKey = sessionKeys.get(myChatUID);
        if (debug >= 1) {
            System.out.println("SESSIONTEST: " + AESCrypto.asHex(sessionKey.getEncoded()));
        }
        BigInteger messageBigInt = new BigInteger(AESCrypto.encryptMessage(sessionKey, message));
        return messageBigInt.toString();
    }

    /**
     * Decrypt a chat message using AES
     * @param myChatUID The UID the message is associated with (to find the session key)
     * @param message The message to decrypt
     * @return The decrypted message
     */
    public static String decryptAES(String myChatUID, String message) {
        SecretKeySpec sessionKey = sessionKeys.get(myChatUID);
        BigInteger cipherBigInt = new BigInteger(message);
        if (debug >= 1) {
            System.out.println("SESSIONTEST: " + AESCrypto.asHex(sessionKey.getEncoded()));
        }
        return new String(AESCrypto.decryptMessage(sessionKey, cipherBigInt.toByteArray()));
    }

    /**
     * This method takes the message the user types and will get it ready to send
     * @param message The message to send
     * @throws BadLocationException
     * @throws IOException
     */
    public static void processMessage(String message) throws BadLocationException, IOException {
        //Is this a chat or IM tab?
        JPanel currentTab = (JPanel) clientResource.imTabbedPane.getSelectedComponent();
        //This is a chat tab
        if (Integer.parseInt(currentTab.getName()) != -1) {
            if (debug >= 1) {
                System.out.println("THIS IS IN A CHAT TAB! SENDING MESSAGE TO CHAT " + currentTab.getName());
            }
            //Prepend username and comma to message so we know who it's from.
            message = username + "," + message;
            //Sending information to Aegis
            systemMessage("17");
            c2sdout.writeUTF(encryptServerPublic(currentTab.getName()));
            c2sdout.writeUTF(encryptAES(currentTab.getName(), message));
            toUser = clientResource.imTabbedPane.getTitleAt(clientResource.imTabbedPane.getSelectedIndex());
            print = (MapTextArea) clientResource.tabPanels.get(toUser);
            if (username.equals("null")) {
                print.writeToTextArea("Error: You are not connected!\n", print.getSetHeaderFont(new Color(130, 0, 0)));
                print.moveToEnd();
                print.clearTextField();
            } else {
                String[] localMessage = message.split(",", 2);
                //Print the message locally
                print.writeToTextArea(username + ": ", print.getSetHeaderFont(new Color(0, 130, 0)));
                //print.writeToTextArea(message+"\n", print.getTextFont());
                parseMarkdown(localMessage[1], print);
                print.clearTextField();
            }

            //This is an IM tab
        } else {
            if (debug >= 1) {
                System.out.println("THIS IS AN IM TAB!!!");
            }
            //Get user to send message to from active tab
            toUser = clientResource.imTabbedPane.getTitleAt(clientResource.imTabbedPane.getSelectedIndex());

            //Get the JPanel in the active tab
            print = (MapTextArea) clientResource.tabPanels.get(toUser);
            if (debug >= 1) {
                System.out.println("JPANEL : " + print.toString());
            }

            //See if the user is logged in. If yes, send it. If no, error.
            if (debug >= 1) {
                System.out.println("USERNAME: " + username);
            }
            if (username.equals("null")) {
                print.writeToTextArea("Error: You are not connected!\n", print.getSetHeaderFont(new Color(130, 0, 0)));
                print.moveToEnd();
                print.clearTextField();
            } else {
                //Print the message locally
                print.writeToTextArea(username + ": ", print.getSetHeaderFont(new Color(0, 130, 0)));
                //print.writeToTextArea(message+"\n", print.getTextFont());
                parseMarkdown(message, print);
                //Send the message
                try {
					if(sessionKeys.containsKey(toUser)){
						c2sdout.writeUTF(encryptServerPublic(toUser));
                        c2sdout.writeUTF(encryptServerPublic(username));
                        c2sdout.writeUTF(encryptAES(toUser, message));
						// Append own message to IM window
                        print.moveToEnd();
                        // Clear out text input field
                        print.clearTextField();
						
					}
					else if(message.length() > 245) {
                        double messageNumbers = (double) message.length() / 245;
                        double messageNumbersInt = Math.ceil(messageNumbers);
                        if (debug >= 1) {
                            System.out.println("MessageLength: " + message.length() + "\nMessageLength/245: " + messageNumbers + "\nCeiling of that: " + messageNumbersInt);
                        }
                        String[] messageChunks = new String[(int) messageNumbersInt];
                        for (int i = 0; i < messageChunks.length; i++) {
                            int begin = i * 245;
                            int end = begin + 245;
                            if (end > message.length()) {
                                end = message.length() - 1;
                            }
                            messageChunks[i] = message.substring(begin, end);

                            //Check to see if this tab is a chat tab!!!
                            // if(clientResource.)
                            //Grab the other user's public key from file
                            RSAPublicKeySpec toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + toUser + ".pub");
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

                    } else {

                        //Grab the other user's public key from file
                        RSAPublicKeySpec toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + toUser + ".pub");
                        //Encrypt the message with the toUser's public key and send it to the server
                        BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPublic(message, toUserPublic.getModulus(), toUserPublic.getPublicExponent()));
                        c2sdout.writeUTF(encryptServerPublic(toUser));
                        c2sdout.writeUTF(encryptServerPublic(username));
                        c2sdout.writeUTF(messageCipher.toString());
                        //TODO Hash the Message for the digital signature

                        // Append own message to IM window
                        print.moveToEnd();
                        // Clear out text input field
                        print.clearTextField();
                    }
                    //TADA
                } catch (IOException ie) {
                    if (debug >= 1) {
                        System.out.println(ie);
                    }
                    print.writeToTextArea("Error: You probably don't have the user's public key!\n", print.getTextFont());
                    print.moveToEnd();
                    print.clearTextField();
                } catch (Exception e) {
                    sendBugReport(getStackTraceAsString(e));

                    e.printStackTrace();
                }
            }
        }
        System.gc();
    }

    /**
     * Send a message to specified user (used for away message)
     * @param usertoreply
     * @param message
     * @throws BadLocationException
     */
    public static void processMessage(String usertoreply, String message) throws BadLocationException {
        //Get user to send message to from active tab
        toUser = usertoreply;
        //Get the JPanel in the active tab
        print = (MapTextArea) clientResource.tabPanels.get(toUser);
        if (debug >= 1) {
            System.out.println("JPANEL : " + print.toString());
        }

        //See if the user is logged in. If yes, send it. If no, error.
        if (debug >= 1) {
            System.out.println("USERNAME: " + username);
        }
        if (username.equals("null")) {
            print.writeToTextArea("Error: You are not connected!\n", print.getSetHeaderFont(new Color(130, 0, 0)));
            print.moveToEnd();
            print.clearTextField();
        } else {
            //Print the message locally
            print.writeToTextArea(username + ": ", print.getSetHeaderFont(new Color(0, 130, 0)));
            parseMarkdown(message, print);

            //Send the message
            try {
                if (message.length() > 245) {
                    double messageNumbers = (double) message.length() / 245;
                    double messageNumbersInt = Math.ceil(messageNumbers);
                    if (debug >= 1) {
                        System.out.println("MessageLength: " + message.length() + "\nMessageLength/245: " + messageNumbers + "\nCeiling of that: " + messageNumbersInt);
                    }
                    String[] messageChunks = new String[(int) messageNumbersInt];
                    for (int i = 0; i < messageChunks.length; i++) {
                        int begin = i * 245;
                        int end = begin + 245;
                        if (end > message.length()) {
                            end = message.length() - 1;
                        }
                        messageChunks[i] = message.substring(begin, end);

                        //Grab the other user's public key from file
                        RSAPublicKeySpec toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + toUser + ".pub");
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

                } else {


                    //Grab the other user's public key from file
                    RSAPublicKeySpec toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + toUser + ".pub");
                    //Encrypt the message with the toUser's public key and send it to the server
                    BigInteger messageCipher = new BigInteger(RSACrypto.rsaEncryptPublic(message, toUserPublic.getModulus(), toUserPublic.getPublicExponent()));
                    c2sdout.writeUTF(encryptServerPublic(toUser));
                    c2sdout.writeUTF(encryptServerPublic(username));
                    c2sdout.writeUTF(messageCipher.toString());
                    //TODO Hash the Message for the digital signature

                    // Append own message to IM window
                    print.moveToEnd();
                    // Clear out text input field
                    print.clearTextField();
                }
                //TADA
            } catch (IOException ie) {
                if (debug >= 1) {
                    System.out.println(ie);
                }
                print.writeToTextArea("Error: You probably don't have the user's public key. Please add them to your list!\n", print.getSetHeaderFont(new Color(130, 0, 0)));
                print.moveToEnd();
                print.clearTextField();
            } catch (Exception e) {
                sendBugReport(getStackTraceAsString(e));
                e.printStackTrace();
            }
        }
        System.gc();
    }

    /** This method adds a user to the buddylist
     * @param usernameToAdd This is the username you want to add
     * @throws Exception
     */
    public static void buddyList(String usernameToAdd) throws Exception {
        //Add the username to a new line in the file
        //Will take in more inputs as we add other functionality to Athena like Pubkey, group, etc

        //Set exists to 0, this means that the usernameToAdd is not already in the buddylist file
        int exists = 0;
        BufferedWriter out;
        try {
            //Call the returnBuddyListArray method, this reads in the buddylist file and puts the user names into an array for us to use
            String[] usernames = returnBuddyListArray();

            //This for loop checks to see if the usernameToAdd is already in the buddylist file, if so, set exists to 1
            for (int y = 0; y < usernames.length; y++) {
                if (usernames[y].equals(usernameToAdd)) {
                    exists = 1;
                }
            }

            //If the usernameToAdd IS NOT in the buddylist file, add it
            if (exists == 0) {
                BigInteger encryptedUsername;
                //Append to the file the usernameToAdd
                File newFile = new File("users/" + username + "/buddylist.csv");
                if (!(newFile.exists())) {
                    boolean success = new File("users/" + username).mkdirs();
                    if (success) {
                        newFile.createNewFile();
                        out = new BufferedWriter(new FileWriter("./users/" + username + "/buddylist.csv"));
                    } else {
                        newFile.createNewFile();
                    }
                }
                out = new BufferedWriter(new FileWriter("./users/" + username + "/buddylist.csv", true));
                encryptedUsername = new BigInteger(descrypto.encryptData(usernameToAdd.concat(",")));
                out.write(encryptedUsername + "\n");
                out.close();
            }
        } catch (IOException e) {
            sendBugReport(getStackTraceAsString(e));
            e.printStackTrace();
        }
    }

    /**
     * This method writes the buddy list to file
     * @param buddyList String array of the lines of the buddy list
     */
    static void writeBuddyListToFile(String[] buddyList) {
        BigInteger encryptedUsername;
        BufferedWriter out;
        File newFile = new File("users/" + username + "/buddylist.csv");
        try {
            if (!(newFile.exists())) {
                boolean success = new File("users/" + username).mkdirs();
                if (success) {
                    newFile.createNewFile();
                } else {
                    newFile.createNewFile();
                }
            }
            out = new BufferedWriter(new FileWriter("./users/" + username + "/buddylist.csv"));

            for (int i = 0; i < buddyList.length; i++) {
                encryptedUsername = new BigInteger(descrypto.encryptData(buddyList[i].concat(",")));
                out.write(encryptedUsername + "\n");
            }
            out.close();
        } catch (Exception e) {
            sendBugReport(getStackTraceAsString(e));
            if (debug >= 1) {
                System.out.println("ERROR WRITING BUDDYLIST");
            }
            if (debug == 2) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method writes the buddy list to file
     * @param buddyList String array of the lines of the buddy list
     * @param flag Differentiating parameter
     */
    static void writeBuddyListToFile(String[] buddyList, boolean flag) {
        BigInteger encryptedUsername;
        BufferedWriter out;
        File newFile = new File("users/" + username + "/buddylist.csv");
        try {
            if (!(newFile.exists())) {
                boolean success = new File("users/" + username).mkdirs();
                if (success) {
                    newFile.createNewFile();
                } else {
                    newFile.createNewFile();
                }
            }
            out = new BufferedWriter(new FileWriter("./users/" + username + "/buddylist.csv"));

            for (int i = 0; i < buddyList.length; i++) {
                encryptedUsername = new BigInteger(buddyList[i]);
                out.write(encryptedUsername + "\n");
            }
            out.close();
        } catch (Exception e) {
            sendBugReport(getStackTraceAsString(e));
            if (debug >= 1) {
                System.out.println("ERROR WRITING BUDDYLIST");
            }
            if (debug == 2) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the user's private key from the server
     * @throws IOException
     */
    public static void receivePrivateKeyFromServer() throws IOException {
        systemMessage("007");
        //Receive ack message
        System.out.println("Ack message received from server: " + decryptServerPublic(c2sdin.readUTF()));

        int chunks = Integer.parseInt(c2sdin.readUTF());
        if (debug >= 1) {
            System.out.println("How many chunks the mod will be: " + chunks);
        }

        //Grab the private key information from the server
        String finalPrivateMod = "";
        String[] privateModArray = new String[chunks];
        for (int x = 0; x < chunks; x++) {
            String privateMod = c2sdin.readUTF();
            if (debug == 2) {
                System.out.println("PRIVATE MOD: " + privateMod);
            }
            if (!(privateMod.equals("end"))) {
                if (privateModArray.length > 0) {
                    privateModArray[x] = decryptServerPublic(privateMod);
                }
            }
        }

        if (privateModArray.length > 0) {
            finalPrivateMod = privateModArray[0];    // start with the first element
            for (int i = 1; i < privateModArray.length; i++) {
                finalPrivateMod = finalPrivateMod + privateModArray[i];
            }
        }

        int expChunks = Integer.parseInt(c2sdin.readUTF());
        if (debug >= 1) {
            System.out.println("How many chunks the exp will be: " + expChunks);
        }

        //Grab the private key information from the server
        String finalPrivateExp = "";
        String[] privateExpArray = new String[expChunks];
        for (int x = 0; x < expChunks; x++) {
            String privateExp = c2sdin.readUTF();
            if (debug == 2) {
                System.out.println("PRIVATE EXP: " + privateExp);
            }

            if (!(privateExp.equals("end"))) {
                if (privateExpArray.length > 0) {
                    privateExpArray[x] = decryptServerPublic(privateExp);
                }
            }
        }

        if (privateExpArray.length > 0) {
            finalPrivateExp = privateExpArray[0];    // start with the first element
            for (int i = 1; i < privateExpArray.length; i++) {
                finalPrivateExp = finalPrivateExp + privateExpArray[i];
            }
        }

        if (debug == 2) {
            System.out.println("DECRYPTED PRIVATE MOD: " + finalPrivateMod);
        }
        if (debug == 2) {
            System.out.println("DECRYPTED PRIVATE EXP: " + finalPrivateExp);
        }

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
        //if(debug>=1)System.out.println("BuddyList header: " + decryptServerPublic(c2sdin.readUTF()));
        //Parse out how many lines buddylist is
        buddyListLines = new String[(Integer.parseInt(decryptServerPublic(c2sdin.readUTF())))];
        for (int y = 0; y < buddyListLines.length; y++) {
            buddyListLines[y] = decryptServerPublic(c2sdin.readUTF());
            if (debug == 2) {
                System.out.println("Decrypted buddylist lines: " + buddyListLines[y]);
            }
        }
        writeBuddyListToFile(buddyListLines, true);
        if (debug >= 1) {
            System.out.println("Successfully wrote Buddylist to file");
        }

    }

    /**
     * Method gets the user's public key from Aegis
     * @param publicKeyToFind The user to find the key for
     * @throws IOException
     */
    public static void getUsersPublicKeyFromAegis(String publicKeyToFind) throws IOException {
        if (debug >= 1) {
            System.out.println("Getting " + publicKeyToFind + "'s public key!");
        }
        //Send Aegis event code 004 to let it know what we're doing
        systemMessage("004");
        c2sdout.writeUTF(encryptServerPublic(publicKeyToFind));
        modOfBuddy = new BigInteger(c2sdin.readUTF());
        if (debug >= 1) {
            System.out.println("MODOFBUDDY: " + modOfBuddy.toString());
        }
        if (modOfBuddy.toString().equals("-1")) {
            JOptionPane.showMessageDialog(null, "Cannot find user's public key.\n"
                    + "Make sure you typed their username correctly and try again.", "Error Retrieving Key", JOptionPane.ERROR_MESSAGE);
        } else {
            expOfBuddy = new BigInteger(c2sdin.readUTF());
            writeBuddysPubKeyToFile(publicKeyToFind, modOfBuddy, expOfBuddy);
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
        if (!(newFile.exists())) {
            boolean success = new File("users/" + username + "/keys/").mkdirs();
            if (success) {
                newFile.createNewFile();
                is = new BufferedInputStream(new FileInputStream("users/" + username + "/keys/" + buddysUsername + ".pub"));
                RSACrypto.saveToFile("users/" + username + "/keys/" + buddysUsername + ".pub", mod, exp);
            } else {
                newFile.createNewFile();
                RSACrypto.saveToFile("users/" + username + "/keys/" + buddysUsername + ".pub", mod, exp);
            }
        }


    }

    /**
     * Method sends the buddy list to Aegis
     * @throws IOException
     * @return success
     */
    public static boolean sendBuddyListToServer() throws IOException {
        String[] buddylistArray = returnBuddyListArray(true);
        systemMessage("006");

        int numLines = buddylistArray.length;

        //Send Aegis the begin message so it knows that this is beginning of the file
        c2sdout.writeUTF(encryptServerPublic("begin"));
        //Send Aegis the number lines we're sending
        c2sdout.writeUTF(encryptServerPublic(String.valueOf(numLines)));
        for (int x = 0; x < buddylistArray.length; x++) {
            //Now send Aegis the file
            c2sdout.writeUTF(encryptServerPublic(buddylistArray[x]));
        }
        return true;
    }

    /**
     * This encrypts the input string with the server's public key
     * @param plaintext The plaintext
     * @return cipherText - The encrypted String
     */
    public static String encryptServerPublic(String plaintext) {
        BigInteger cipherText = new BigInteger(RSACrypto.rsaEncryptPublic(plaintext, Athena.serverPublic.getModulus(), Athena.serverPublic.getPublicExponent()));
        return cipherText.toString();
    }

    /**
     * This method decrypts the input string with the server's public key
     * @param ciphertext
     * @return decrypted message
     */
    public static String decryptServerPublic(String ciphertext) {
        //Turn the String into a BigInteger. Get the bytes of the BigInteger for a byte[]
        byte[] cipherBytes = (new BigInteger(ciphertext)).toByteArray();
        //Decrypt the byte[], returns a String
        return RSACrypto.rsaDecryptPublic(cipherBytes, Athena.serverPublic.getModulus(), Athena.serverPublic.getPublicExponent());
    }

    /**
     * This method returns a string array of the lines from the buddylist
     * @throws IOException Cannot read the file
     * @return String array of the buddylist
     */
    public static String[] returnBuddyListArray() throws IOException {
        int count;
        int readChars;
        InputStream is;

        //Let's get the number of lines in the file
        File newFile = new File("users/" + username + "/buddylist.csv");
        if (!(newFile.exists())) {
            boolean success = new File("users/" + username).mkdirs();
            if (success) {
                newFile.createNewFile();
                is = new BufferedInputStream(new FileInputStream("users/" + username + "/buddylist.csv"));
            } else {
                newFile.createNewFile();
            }
        }

        is = new BufferedInputStream(new FileInputStream("users/" + username + "/buddylist.csv"));
        byte[] c = new byte[1024];
        count = 0;
        readChars = 0;
        while ((readChars = is.read(c)) != -1) {
            for (int i = 0; i < readChars; ++i) {
                if (c[i] == '\n') {
                    ++count;
                }
            }
        } //End section

        //Make the string array the size of the number of lines in the file
        String[] usernames = new String[count];

        //If there are no lines in the file we know that the user has no buddies! :(
        if (count == 0) {
            return usernames;
        } else {
            File newFile2 = new File("users/" + username + "/buddylist.csv");
            if (!(newFile2.exists())) {
                newFile2.createNewFile();
            }
            BufferedReader in = new BufferedReader(new FileReader("users/" + username + "/buddylist.csv"));
            int x = 0;
            String raw, str;
            BigInteger strNum;

            //Split each line on every ',' then take the string before that and add it to the usernames array | God I love split.
            while ((raw = in.readLine()) != null) {
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

    /**
     * An overload of returnBuddyListArray for some reason
     * @param flag Differentiating overload parameter
     * @return String[] of user's buddies
     * @throws IOException File not found
     */
    public static String[] returnBuddyListArray(boolean flag) throws IOException {
        int count;
        int readChars;
        InputStream is;

        //Let's get the number of lines in the file
        File newFile = new File("users/" + username + "/buddylist.csv");
        if (!(newFile.exists())) {
            boolean success = new File("users/" + username).mkdirs();
            if (success) {
                newFile.createNewFile();
                is = new BufferedInputStream(new FileInputStream("./users/" + username + "/buddylist.csv"));
            } else {
                newFile.createNewFile();
            }
        }

        is = new BufferedInputStream(new FileInputStream("./users/" + username + "/buddylist.csv"));
        byte[] c = new byte[1024];
        count = 0;
        readChars = 0;
        while ((readChars = is.read(c)) != -1) {
            for (int i = 0; i < readChars; ++i) {
                if (c[i] == '\n') {
                    ++count;
                }
            }
        } //End section

        //Make the string array the size of the number of lines in the file
        String[] usernames = new String[count];

        //If there are no lines in the file we know that the user has no buddies! :(
        if (count == 0) {
            return usernames;
        } else {
            File newFile2 = new File("users/" + username + "/buddylist.csv");
            if (!(newFile2.exists())) {
                newFile2.createNewFile();
            }
            BufferedReader in = new BufferedReader(new FileReader("users/" + username + "/buddylist.csv"));
            int x = 0;
            String raw;
            //Split each line on every ',' then take the string before that and add it to the usernames array | God I love split.
            while ((raw = in.readLine()) != null) {
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

    /**
     * Send a message directly to Aegis
     * @param message The message to send
     */
    public static void systemMessage(String message) {
        //Send the message
        try {
            //Send recipient's name and message to server
            c2sdout.writeUTF(encryptServerPublic("Aegis"));
            c2sdout.writeUTF(encryptServerPublic("")); //Blank username field
            c2sdout.writeUTF(encryptServerPublic(message));
        } catch (IOException ie) {
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
     * @return String hash of the buddylist
     * @throws Exception
     */
    public static String returnHashOfLocalBuddyList(String buddyname) throws Exception {
        String path = "users/".concat(buddyname).concat("/buddylist.csv");
        File buddyList = new File(path);
        if (!buddyList.exists()) {
            boolean success = new File("users/" + username).mkdirs();
            if (success) {
                buddyList.createNewFile();
            } else {
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

    /**
     * Method returns the hash of the remote buddylist
     * @param buddyname Buddylist you want to find the hash of
     * @return The hash and date modified
     */
    public static String[] returnHashOfRemoteBuddyList(String buddyname) {
        try {

            systemMessage("005");

            //Send buddyname
            c2sdout.writeUTF(encryptServerPublic(buddyname));
            String[] remoteValues = new String[2];
            //counter
            int x = 0;
            while (x <= 1) {
                remoteValues[x] = decryptServerPublic(c2sdin.readUTF());
                if (debug >= 1) {
                    System.out.println("REMOTE VALS " + x + ": " + remoteValues[x]);
                }
                x++;
            }
            if (debug >= 1) {
                System.out.println("Completed.");
            }
            return remoteValues;

        } catch (Exception e) {
            sendBugReport(getStackTraceAsString(e));
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Enable or disable sound notifications
     * @param activated to enable or disable
     */
    public static void setEnableSounds(boolean activated) {
        if (activated) {
            enableSounds = true;
        } else {
            enableSounds = false;
        }
    }

    /**
     * Are sounds enabled?
     * @return If sounds are enabled or not
     */
    public static boolean getEnableSounds() {
        return enableSounds;
    }

    /**
     * Close all connections, and shut 'er down
     */
    public static void disconnect() {
        try {
            if (c2sdout != null) {
                c2sdout.close();
            }
            if (c2cdout != null) {
                c2cdout.close();
            }
            if (c2sdin != null) {
                c2sdin.close();
            }
            if (c2cdin != null) {
                c2cdin.close();
            }
            c2ssocket.close();
            c2csocket.close();
            connected = 0;
            away = 0;
            if (clientResource != null) {
                clientResource.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Exit the program
     */
    public static void exit() {
        System.exit(0);
    }

    /**
     * Send a bug report to Aegis as a result of an exception
     * @param stackTrace the StackTrace of the exception
     */
    public static void sendBugReport(String stackTrace) {
        int toSend = JOptionPane.showConfirmDialog(null, "Sorry, it looks like something went wrong.\n"
                + "Would you like to submit this as a bug report?", "File a Bug Report?", JOptionPane.YES_NO_OPTION);
        if (toSend == JOptionPane.YES_OPTION) {
            String comments = JOptionPane.showInputDialog("Optional: Any comments about this bug (what were you doing when this happened)?");
            if (comments.equals("")) {
                comments = "No comment entered";
            }

            systemMessage("8");
            try {
                c2sdout.writeUTF(stackTrace);
                c2sdout.writeUTF(comments);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Turn a stackTrace into a string for transmission
     * @param e The stacktrace to convert
     * @return The stacktrace as a string
     */
    public static String getStackTraceAsString(Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        return stackTrace.toString();
    }

    /**
     * Spawn the login GUI
     * @param args nothing
     * @throws AWTException
     */
    public static void main(String[] args) throws AWTException {
        loginGUI = new AuthenticationInterface();

    }
}
