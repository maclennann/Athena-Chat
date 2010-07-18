/* Athena/Aegis Encrypted Chat Platform
 * ListenerThread.java: Called by Server, listens for Client connections
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
import java.net.ServerSocket;
import java.net.Socket;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Norm
 */
public class ListenerThread extends Thread{
	ServerSocket sSocket;
	ServerSocket cSocket;
	Server server;

	public ListenerThread(ServerSocket serverSocket, ServerSocket clientSocket, Server serv){
		sSocket = serverSocket;
		cSocket = clientSocket;
		server = serv;
	}

	public void run() {
		try{
			//Accept client connections forever
			while (true) {
				//Accept a new connection on the serversocket
				//Create a socket for it
				Socket c2s = sSocket.accept();
				Socket c2c = cSocket.accept();

				//Debug text announcing a new connection
				server.writeLog("Server-to-Client Connection Established:\n " + c2s);
				server.writeLog("Client-to-Client Connection Established:\n" + c2c);

				//Handle the rest of the connection in the new thread
				new	ServerThread(server, c2s, c2c);
				System.gc();
			}
		}catch(Exception e){e.printStackTrace();}
	}
}
