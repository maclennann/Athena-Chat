
import java.io.IOException;
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
				System.out.println("Server-to-Client Connection Established:\n " + c2s);
				System.out.println("Client-to-Client Connection Established:\n" + c2c);

				//Handle the rest of the connection in the new thread
				new	ServerThread(server, c2s, c2c);
				System.gc();
			}
		}catch(Exception e){e.printStackTrace();}
	}
}
