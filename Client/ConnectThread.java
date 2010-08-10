/* Athena/Aegis Encrypted Chat Platform
 * ConnectThread.java: Connects the client to the server
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JOptionPane;

public class ConnectThread extends Thread {
	String username;     //The file to send
	String password;   //The user to which we are sending the file

	public ConnectThread(String usernameToConnect, String hashedPassword){
		username = usernameToConnect;
		password = hashedPassword;
	}

	public void run() {
		try{
			boolean successs = new File("users/" + username + "/logs/").mkdirs();
			successs = new File("users/" + username + "/downloads/").mkdirs();
			Athena.openLog(new File("users/"+username+"/logs/"+Athena.getCleanDateTime()+"-debugLog.txt"));
			Athena.writeLog("===============================================");
			//Try to connect with and authenticate to the socket
    		LoginProgress loginBar = new LoginProgress();
			loginBar.pack();
			loginBar.setVisible(true);
			try {
				try {

					loginBar.iterate(100,"Establishing Secure Connection");
					//Connect to auth server at defined port over socket
					//This socket is for client -> server coms
					Athena.c2ssocket = new Socket(Athena.serverIP, 7777);
					//This socket is for client -> client coms
					Athena.c2csocket = new Socket(Athena.serverIP, 7778);
					loginBar.iterate(200,"Secure Connection Established");
				} catch (Exception e) {
	                //We can't connect to the server at the specified port for some reason
	                JOptionPane.showMessageDialog(null, "Could not connect to the server.\n"
	                        + "Please check your Internet connection.\n\n", "Connection Error", JOptionPane.ERROR_MESSAGE);
					loginBar.dispose();
	                AuthenticationInterface loginGUI = new AuthenticationInterface();
	                return;
            }

            //Create the DESCrypto object for buddylist and preferences cryptography
            String saltUser;
            if (username.length() >= 8) {
                saltUser = username.substring(0, 8);
            } else {
                saltUser = username;
            }
            Athena.descrypto = new DESCrypto(password, saltUser);

			loginBar.iterate(250,"Decrypting Contact List");
            //Connection established debug code.
            if (Athena.debug >= 1) {
                Athena.writeLog("Connected to " + Athena.c2ssocket + "<- for client to server communication."); //Client to server coms
            }
            if (Athena.debug >= 1) {
                Athena.writeLog("Connected to " + Athena.c2csocket + "<- for client to client communication."); //Client to client coms
            }

            //Bind the datastreams to the socket in order to send/receive data
            //These datastreams are for client -> server coms
            Athena.c2sdin = new DataInputStream(Athena.c2ssocket.getInputStream());
            Athena.c2sdout = new DataOutputStream(Athena.c2ssocket.getOutputStream());
            //These datastreams are for client -> client coms
            Athena.c2cdin = new DataInputStream(Athena.c2csocket.getInputStream());
            Athena.c2cdout = new DataOutputStream(Athena.c2ssocket.getOutputStream());

			loginBar.iterate(400,"Opening Secure Sockets");
            //Read in the server's public key for encryption of headers
            Athena.serverPublic = RSACrypto.readPubKeyFromFile("users/Aegis/keys/Aegis.pub");
			

            Athena.c2sdout.writeUTF(Athena.encryptServerPublic(username));	   //Sending Username
            Athena.c2sdout.writeUTF(Athena.encryptServerPublic(password)); //Sending Password
            String result = Athena.decryptServerPublic(Athena.c2sdin.readUTF()); //Read in the result

			loginBar.iterate(500,"Transmitting Credentials");
            if (Athena.debug >= 1) {
                Athena.writeLog("Login Result: " + result);
            }
            if (result.equals("Failed")) {
                Athena.disconnect();
				loginBar.dispose();
                new LoginFailedInterface();
                return;
            } else {
                //We're connected
                Athena.connected = 1;
				loginBar.iterate(800,"Authentication Successful");
                Athena.clientResource = new CommunicationInterface();
				Athena.clientResource.setVisible(false);
                //Thread created to listen for messages coming in from the server
                Athena.listeningProcedureClientToClient = new Thread(
                        new Runnable() {

                            public void run() {
                                //While we are connected to the server, receive messages
                                if (Athena.c2cdin == null) {
                                    Athena.connected = 0;
                                }
                                while (Athena.connected == 1) {
                                    Athena.recvMesg(Athena.c2cdin); //Listen for incomming messages from another client
                                }
                            }
                        });

                /* Begin start up sequence
                 * 1. Find the status of the buddylist users
                 * 2. Make sure the user's private key exists
                 * 3. Start the listening thread
                 * 4. See if the user's public key exists
                 */

				loginBar.iterate(1400,"Verifying Contact List");
                //Instantiate Buddy List
                Athena.instantiateBuddyList(loginBar);
				loginBar.iterate(1900,"Verifying Private Key");
                //Check to see if the user's private key is there
                File privateKey = new File("users/" + username + "/keys/" + username + ".priv");
                if (!(privateKey.exists())) {
                    //Check to see if the public key is there too
					if(Athena.debug >= 1)Athena.writeLog("ERROR: Private key is not there. Will atempt to retrieve from server.");
                    boolean success = new File("users/" + username + "/keys/").mkdirs();
                    if (success) {
                        try {
                            Athena.receivePrivateKeyFromServer();
							if(Athena.debug >= 1)Athena.writeLog("Private key retreieved. Continuing...");
                        } catch (IOException e) {
                            Athena.sendBugReport(Athena.getStackTraceAsString(e),loginBar);
                            e.printStackTrace();
                        }

                    } else {
                        try {
                            Athena.receivePrivateKeyFromServer();
							if(Athena.debug >= 1)Athena.writeLog("Private key retreieved. Continuing...");
                        } catch (IOException e) {
                            Athena.sendBugReport(Athena.getStackTraceAsString(e),loginBar);
                            e.printStackTrace();
                        }

                    }
                }


			Athena.toUserPublic = RSACrypto.readPubKeyFromFile("users/" + username + "/keys/" + username + ".pub");
			Athena.userPrivate = RSACrypto.readPrivKeyFromFile("users/" + username + "/keys/" + username + ".priv", Athena.descrypto);

            }
            //Start the thread
            if (Athena.listeningProcedureClientToClient != null) {
                Athena.listeningProcedureClientToClient.start();
            }

			loginBar.iterate(1950,"Verifying Public Key");
            //Check to see if the user's public key is there
            File publicKey = new File("users/" + username + "/keys/" + username + ".pub");
            if (!(publicKey.exists())) {
				if(Athena.debug >= 1)Athena.writeLog("ERROR: Public key is not there. Will atempt to retrieve from server.");
                Athena.getUsersPublicKeyFromAegis(username);
				if(Athena.debug >= 1)Athena.writeLog("Public key retreived. Continuing.");
            }
			loginBar.iterate(2000,"Connected!");
			loginBar.dispose();
            //Garbage collect
            System.gc();
			//Athena.clientResource.setVisible(true);
        } catch (Exception e) {
            Athena.sendBugReport(Athena.getStackTraceAsString(e),loginBar);
            e.printStackTrace();
            Athena.loginGUI.dispose();
        }


	}catch (Exception e){}
	}
}
