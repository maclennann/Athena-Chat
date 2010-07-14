
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;

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

			byte[] mybytearray = new byte[(int) myFile.length()];

			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			bis.read(mybytearray, 0, mybytearray.length);

			byte[] encryptedFile = Athena.encryptAES(toUser,mybytearray);

			if (debug >= 1) {
				System.out.println("Sending...");
			}
			os.write(encryptedFile, 0, encryptedFile.length);
			JOptionPane.showMessageDialog(null,"Server: "+encryptedFile.length);
			os.flush();
			os.close();
		}catch(Exception e){}
	}
}
