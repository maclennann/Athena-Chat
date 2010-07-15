
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
				System.out.println("Sending...");
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
