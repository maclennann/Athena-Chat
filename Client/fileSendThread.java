
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
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
public class fileSendThread extends Thread {
	int debug=1;
	File myFile;
	String toUser;
	String username;

	public fileSendThread(File amyFile, String atoUser){
		myFile = amyFile;
		toUser = atoUser;
	}

	public void run() {
		try{
			ServerSocket fileSS = new ServerSocket(7779);
			Socket fileSocket = fileSS.accept();
			OutputStream os = fileSocket.getOutputStream();

			//TODO file needs to be chunked so we can send
			//     large files without running out of memory
			byte[] mybytearray = new byte[(int) myFile.length()];

			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(mybytearray, 0, mybytearray.length);

			byte[] encryptedFile = Athena.encryptAES(toUser,mybytearray);

			if (debug >= 1) {
				System.out.println("Sending...");
			}
			os.write(encryptedFile, 0, encryptedFile.length);
			//JOptionPane.showMessageDialog(null,"Server: "+encryptedFile.length);
			os.flush();
			os.close();
			fileSocket.close();
			fileSS.close();
			System.gc();
		}catch(Exception e){}
	}
}
