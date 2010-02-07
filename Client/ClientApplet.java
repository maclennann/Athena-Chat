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
import java.applet.*;
import java.awt.*;
import java.io.*;
import java.net.*;

//Creates the physical applet, calls the client code
public class ClientApplet extends Applet{
	public void init() {
		setLayout( new BorderLayout() );
		
		//Create a client. Auth server IP and port.
		add( "Center", new Client( "192.168.1.4", 7777 ) );
	}
}
