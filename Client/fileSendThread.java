/* Athena/Aegis Encrypted Chat Platform
 * fileSendThread.java: Sender's end of file transfer
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author OlympuSoft
 */
public class fileSendThread extends Thread {
	File myFile;     //The file to send
	String toUser;   //The user to which we are sending the file

	public fileSendThread(File amyFile, String atoUser){
		myFile = amyFile;
		toUser = atoUser;
	}

	public void run() {
		try{
			//Start the "server"
			ServerSocket fileSS = new ServerSocket(7779);

			//Accept a connection from the other user
			Socket fileSocket = fileSS.accept();

			//Get the outputstream from the socket
			OutputStream os = fileSocket.getOutputStream();

			//Create the byte[] that will hold our file
			//TODO file needs to be chunked so we can send
			//     large files without running out of memory
			byte[] mybytearray = new byte[(int) myFile.length()];

			//Open the file and read it into the byte[]
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(mybytearray, 0, mybytearray.length);

			//Encrypt the file
			byte[] encryptedFile = Athena.encryptAES(toUser,mybytearray);

			if (Athena.debug >= 1) {
				Athena.writeLog("Sending...");
			}

			//Send the file
			os.write(encryptedFile, 0, encryptedFile.length);
			os.flush();

			//Button everything up
			os.close();
			fileSocket.close();
			fileSS.close();
			System.gc();
		}catch(Exception e){}
	}
}
